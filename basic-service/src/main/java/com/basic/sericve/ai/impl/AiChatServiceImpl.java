package com.basic.sericve.ai.impl;

import com.basic.ai.gateway.AiChatGateway;
import com.basic.ai.memory.AiChatMemoryManager;
import com.basic.ai.memory.DatabaseChatHistoryStore;
import com.basic.ai.model.AiHistoryPage;
import com.basic.ai.model.AiStoredMessage;
import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatMessageVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.IResult;
import com.basic.common.result.ResultEnum;
import com.basic.sericve.ai.service.IAiChatService;
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
 * <p>同一用户只允许一个生成订阅；跨标签页并发会收到业务错误事件。
 * 回答在正常结束前落库，取消或上游异常时保存已生成的部分文本。</p>
 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private static final int DEFAULT_HISTORY_LIMIT = 50;

    private final DatabaseChatHistoryStore historyStore;
    private final AiChatMemoryManager memoryManager;
    private final AiChatGateway gateway;
    private final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    @Override
    public Flux<ServerSentEvent<AiChatStreamVO>> stream(Long userId, String message) {
        String question = message == null ? "" : message.trim();
        if (!StringUtils.hasText(question)) {
            return Flux.just(errorEvent(ResultEnum.PARAM_INVALID));
        }
        return Flux.defer(() -> {
            if (!activeUsers.add(userId)) {
                return Flux.just(errorEvent(ResultEnum.AI_CHAT_BUSY));
            }

            StringBuilder answer = new StringBuilder();
            AtomicBoolean persisted = new AtomicBoolean();
            Flux<ServerSentEvent<AiChatStreamVO>> generated = Flux.defer(
                            () -> gateway.stream(userId.toString(), question))
                    .filter(StringUtils::hasText)
                    .doOnNext(answer::append)
                    .map(this::deltaEvent)
                    .switchIfEmpty(Flux.error(new BusinessException(ResultEnum.REMOTE_RESPONSE_ERROR)))
                    .concatWith(Mono.fromCallable(() -> {
                        persistRoundOnce(userId, question, answer, false, persisted);
                        return doneEvent(false);
                    }))
                    .onErrorResume(error -> {
                        persistRoundOnce(userId, question, answer, true, persisted);
                        return Mono.just(errorEvent(mapError(error)));
                    })
                    .doOnCancel(() -> persistRoundOnce(userId, question, answer, true, persisted));

            return Flux.concat(Mono.just(startEvent()), generated)
                    .doFinally(signal -> activeUsers.remove(userId));
        });
    }

    @Override
    public AiChatHistoryVO getMessages(Long userId, Long beforeId, Integer limit) {
        AiHistoryPage page = historyStore.getHistory(
                userId, beforeId, limit == null ? DEFAULT_HISTORY_LIMIT : limit);
        AiChatHistoryVO result = new AiChatHistoryVO();
        result.setMessages(page.messages().stream().map(this::toMessageVo).toList());
        result.setHasMore(page.hasMore());
        result.setNextBeforeId(page.nextBeforeId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMemory(Long userId) {
        if (activeUsers.contains(userId)) {
            throw new BusinessException(ResultEnum.AI_CHAT_BUSY);
        }
        historyStore.clear(userId);
        memoryManager.clear(userId.toString());
    }

    @Override
    public void testConnection() {
        try {
            gateway.testConnection();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Throwable error) {
            throw new BusinessException(mapError(error));
        }
    }

    private void persistRoundOnce(Long userId, String question, StringBuilder answer,
                                  boolean partial, AtomicBoolean persisted) {
        synchronized (persisted) {
            if (persisted.get()) {
                return;
            }
            // 数据库写入成功后再标记，首次写入失败时允许错误链路再次尝试。
            historyStore.saveRound(userId, question, answer.toString(), partial);
            persisted.set(true);
        }
    }

    private ServerSentEvent<AiChatStreamVO> startEvent() {
        return ServerSentEvent.<AiChatStreamVO>builder()
                .event("start")
                .data(new AiChatStreamVO())
                .build();
    }

    private ServerSentEvent<AiChatStreamVO> deltaEvent(String content) {
        AiChatStreamVO data = new AiChatStreamVO();
        data.setContent(content);
        return ServerSentEvent.<AiChatStreamVO>builder().event("delta").data(data).build();
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
