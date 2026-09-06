package com.basic.sericve.ai.chat.impl;

import com.basic.ai.assistant.model.AssistantSignal;
import com.basic.ai.chat.history.AiHistoryPage;
import com.basic.ai.chat.history.AiStoredMessage;
import com.basic.ai.chat.history.DatabaseChatHistoryStore;
import com.basic.ai.chat.memory.AiChatMemoryManager;
import com.basic.api.vo.aiChat.AiPendingActionVO;
import com.basic.api.vo.aiChat.AiActionPreviewFieldVO;
import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatMessageVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.IResult;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.ai.assistant.action.AssistantActionService;
import com.basic.sericve.ai.assistant.action.model.ActionPreviewField;
import com.basic.sericve.ai.assistant.action.model.PendingAssistantAction;
import com.basic.sericve.ai.assistant.AiAssistantOrchestrator;
import com.basic.sericve.ai.assistant.AssistantRequestContext;
import com.basic.sericve.ai.chat.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 聊天流式业务实现。
 *
 * <p>同一用户只允许一个生成订阅；所有用户统一进入 AI 编排，当前登录权限仅决定
 * 本次运行可见的工具集合。回答在正常结束前落库，取消或上游异常时保存已生成的部分文本。</p>
 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private static final int DEFAULT_HISTORY_LIMIT = 50;
    private final DatabaseChatHistoryStore historyStore;
    private final AiChatMemoryManager memoryManager;
    private final AiAssistantOrchestrator assistantOrchestrator;
    private final AssistantActionService actionService;
    private final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    /**
     * 建立当前登录用户的 SSE 流，并保证用户消息、助手回答和活跃锁的生命周期一致。
     */
    @Override
    public Flux<ServerSentEvent<AiChatStreamVO>> stream(LoginUser loginUser, String message) {
        String question = message == null ? "" : message.trim();
        if (!StringUtils.hasText(question)) {
            return Flux.just(errorEvent(ResultEnum.PARAM_INVALID));
        }
        Long userId = loginUser.getUserId();
        Set<String> permissions = loginUser.getPermissions() == null
                ? Set.of() : Set.copyOf(loginUser.getPermissions());
        return Flux.defer(() -> {
            // Set.add 的原子语义同时覆盖同一进程内的多标签页请求。
            if (!activeUsers.add(userId)) {
                return Flux.just(errorEvent(ResultEnum.AI_CHAT_BUSY));
            }

            StringBuilder answer = new StringBuilder();
            AtomicBoolean assistantPersisted = new AtomicBoolean();
            Flux<ServerSentEvent<AiChatStreamVO>> generated = Flux.defer(() -> {
                        // 用户消息先落库，完整日志和待审批 Action 才能引用稳定的触发消息主键。
                        Long triggerMessageId = historyStore.saveUserMessage(userId, question);
                        AssistantRequestContext requestContext = new AssistantRequestContext(
                                triggerMessageId, userId, permissions);
                        return assistantOrchestrator.stream(
                                        requestContext,
                                        question,
                                        actionService.findPending(userId))
                                .doOnNext(signal -> appendAnswer(answer, signal))
                                .map(this::assistantEvent)
                                .switchIfEmpty(Flux.error(
                                        new BusinessException(ResultEnum.REMOTE_RESPONSE_ERROR)))
                                .concatWith(Mono.fromCallable(() -> {
                                    // 正常结束先持久化助手文本，再通知前端 done 后刷新历史。
                                    persistAssistantOnce(
                                            userId, answer, false, assistantPersisted);
                                    return doneEvent(false);
                                }));
                    })
                    .onErrorResume(error -> {
                        // 上游异常保留已经生成的文本，并只返回映射后的稳定错误。
                        persistAssistantOnce(userId, answer, true, assistantPersisted);
                        return Mono.just(errorEvent(mapError(error)));
                    })
                    .doOnCancel(() -> persistAssistantOnce(
                            userId, answer, true, assistantPersisted));

            return Flux.concat(Mono.just(startEvent()), generated)
                    .doFinally(signal -> activeUsers.remove(userId));
        });
    }

    /** 返回历史游标页，并附带与消息列表独立的当前待审批预览。 */
    @Override
    public AiChatHistoryVO getMessages(Long userId, Long beforeId, Integer limit) {
        AiHistoryPage page = historyStore.getHistory(
                userId, beforeId, limit == null ? DEFAULT_HISTORY_LIMIT : limit);
        AiChatHistoryVO result = new AiChatHistoryVO();
        result.setMessages(page.messages().stream().map(this::toMessageVo).toList());
        result.setHasMore(page.hasMore());
        result.setNextBeforeId(page.nextBeforeId());
        actionService.findPending(userId)
                .map(this::toPendingActionVo)
                .ifPresent(result::setPendingAction);
        return result;
    }

    /**
     * 在独占用户活跃槽位期间同时清理业务历史、模型记忆和待审批操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMemory(Long userId) {
        if (!activeUsers.add(userId)) {
            throw new BusinessException(ResultEnum.AI_CHAT_BUSY);
        }
        try {
            historyStore.clear(userId);
            memoryManager.clear(userId.toString());
            actionService.cancelAllPending(userId);
        }
        finally {
            activeUsers.remove(userId);
        }
    }

    @Override
    public void testConnection() {
        try {
            assistantOrchestrator.testConnection();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Throwable error) {
            throw new BusinessException(mapError(error));
        }
    }

    private void persistAssistantOnce(Long userId, StringBuilder answer,
                                      boolean partial, AtomicBoolean persisted) {
        synchronized (persisted) {
            if (persisted.get()) {
                return;
            }
            // 数据库写入成功后再标记，首次写入失败时允许错误链路再次尝试。
            historyStore.saveAssistantMessage(userId, answer.toString(), partial);
            persisted.set(true);
        }
    }

    private ServerSentEvent<AiChatStreamVO> startEvent() {
        return ServerSentEvent.<AiChatStreamVO>builder()
                .event("start")
                .data(new AiChatStreamVO())
                .build();
    }

    /** 将内部 AssistantSignal 转换为前端约定的 SSE 事件名和安全载荷。 */
    private ServerSentEvent<AiChatStreamVO> assistantEvent(AssistantSignal signal) {
        // SSE 事件名是前后端稳定协议；不同类型只填充其需要的字段。
        AiChatStreamVO data = new AiChatStreamVO();
        data.setContent(signal.content());
        data.setToolName(signal.toolName());
        data.setSummary(signal.summary());
        String eventName = switch (signal.type()) {
            case DELTA -> "delta";
            case TOOL_START -> "tool_start";
            case TOOL_RESULT -> "tool_result";
            case APPROVAL_REQUIRED -> {
                if (signal.data() instanceof PendingAssistantAction pendingAction) {
                    data.setPendingAction(toPendingActionVo(pendingAction));
                }
                yield "approval_required";
            }
        };
        return ServerSentEvent.<AiChatStreamVO>builder().event(eventName).data(data).build();
    }

    /** 只累计 DELTA 文本，工具进度与审批结构化数据不进入聊天记录。 */
    private static void appendAnswer(StringBuilder answer, AssistantSignal signal) {
        // 工具状态和审批预览严禁混入最终助手正文及聊天历史。
        if (signal.type() == AssistantSignal.Type.DELTA && StringUtils.hasText(signal.content())) {
            answer.append(signal.content());
        }
    }

    /** 将内部待审批对象投影为不含序列化细节的接口 VO。 */
    private AiPendingActionVO toPendingActionVo(PendingAssistantAction action) {
        // 只展开 Handler 生成的安全预览，不传输 payload JSON 和内部分类摘要。
        AiPendingActionVO vo = new AiPendingActionVO();
        vo.setId(action.id());
        vo.setActionType(action.actionType());
        vo.setTitle(action.preview().title());
        vo.setContent(action.preview().content());
        vo.setFields(action.preview().fields().stream()
                .map(this::toPreviewFieldVo)
                .toList());
        vo.setExpiresAt(action.expiresAt());
        return vo;
    }

    private AiActionPreviewFieldVO toPreviewFieldVo(ActionPreviewField field) {
        AiActionPreviewFieldVO vo = new AiActionPreviewFieldVO();
        vo.setLabel(field.label());
        vo.setValue(field.value());
        return vo;
    }

    private ServerSentEvent<AiChatStreamVO> doneEvent(boolean stopped) {
        AiChatStreamVO data = new AiChatStreamVO();
        data.setStopped(stopped);
        return ServerSentEvent.<AiChatStreamVO>builder().event("done").data(data).build();
    }

    private ServerSentEvent<AiChatStreamVO> errorEvent(IResult error) {
        AiChatStreamVO data = new AiChatStreamVO();
        data.setCode(error.getCode());
        data.setMessage(error.getMessage());
        return ServerSentEvent.<AiChatStreamVO>builder().event("error").data(data).build();
    }

    private AiChatMessageVO toMessageVo(AiStoredMessage message) {
        AiChatMessageVO vo = new AiChatMessageVO();
        vo.setId(message.id());
        vo.setRole(message.role());
        vo.setContent(message.content());
        vo.setPartial(message.partial());
        vo.setCreateTime(message.createTime());
        return vo;
    }

    /**
     * 将上游异常压缩为稳定业务错误，禁止把第三方响应正文带到接口和日志。
     */
    private IResult mapError(Throwable error) {
        if (error instanceof BusinessException businessException) {
            return businessException.getError();
        }
        Throwable current = error;
        while (current != null) {
            Integer status = null;
            if (current instanceof RestClientResponseException response) {
                status = response.getStatusCode().value();
            } else if (current instanceof WebClientResponseException response) {
                status = response.getStatusCode().value();
            }
            if (status != null) {
                if (status == 401 || status == 403) {
                    return ResultEnum.THIRD_PARTY_AUTH_FAILED;
                }
                if (status == 429) {
                    return ResultEnum.REQUEST_TOO_FREQUENT;
                }
                return ResultEnum.REMOTE_SERVICE_ERROR;
            }
            if (current instanceof TimeoutException || current instanceof SocketTimeoutException) {
                return ResultEnum.REMOTE_SERVICE_TIMEOUT;
            }
            current = current.getCause();
        }
        return ResultEnum.REMOTE_SERVICE_ERROR;
    }
}
