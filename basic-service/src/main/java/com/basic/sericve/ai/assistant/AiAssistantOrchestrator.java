package com.basic.sericve.ai.assistant;

import com.basic.ai.assistant.gateway.AiAssistantGateway;
import com.basic.ai.assistant.model.ApprovalIntent;
import com.basic.ai.assistant.model.AssistantSignal;
import com.basic.ai.chat.memory.AiChatMemoryManager;
import com.basic.sericve.ai.assistant.action.AssistantActionService;
import com.basic.sericve.ai.assistant.action.model.ActionExecutionResult;
import com.basic.sericve.ai.assistant.action.model.PendingAssistantAction;
import com.basic.sericve.ai.assistant.approval.ActionApprovalRouter;
import com.basic.sericve.ai.assistant.operation.AssistantOperationToolFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Optional;

/**
 * 统一编排 AI 助手调用和待审批操作，写操作只执行已持久化且再次确认的快照。
 *
 * <p>聊天消息主键作为单次运行的关联标识，完整模型过程写入会话日志；
 * 数据库只保留真正参与写操作状态机的 Action。</p>
 */
@Component
@RequiredArgsConstructor
public class AiAssistantOrchestrator {

    private final AssistantActionService actionService;
    private final ActionApprovalRouter approvalRouter;
    private final AiAssistantGateway assistantGateway;
    private final AssistantOperationToolFactory operationToolFactory;
    private final AiChatMemoryManager memoryManager;

    /** 使用统一模型网关验证当前 AI 配置。 */
    public void testConnection() {
        assistantGateway.testConnection();
    }

    /**
     * 编排一次用户消息；存在待审批操作时优先处理确认意图，否则启动统一助手模型。
     */
    public Flux<AssistantSignal> stream(AssistantRequestContext context,
                                    String message,
                                    Optional<PendingAssistantAction> pending) {
        return Flux.defer(() -> {
            Long runId = Objects.requireNonNull(context.triggerMessageId(), "triggerMessageId 不能为空");
            return pending.isPresent()
                    ? handlePending(context, message, pending.get(), runId)
                    : startAssistantRun(context, message, runId);
        });
    }

    /** 先分类用户对现有快照的回复，再进入不含模型工具调用的确定性分支。 */
    private Flux<AssistantSignal> handlePending(AssistantRequestContext context,
                                             String message,
                                             PendingAssistantAction pending,
                                             Long runId) {
        return approvalRouter.classify(message, pending)
                .flatMapMany(intent -> handlePendingIntent(context, message, pending, runId, intent));
    }

    /** 根据审批意图执行确认、取消、重新规划或保守追问。 */
    private Flux<AssistantSignal> handlePendingIntent(AssistantRequestContext context,
                                                   String message,
                                                   PendingAssistantAction pending,
                                                   Long runId,
                                                   ApprovalIntent intent) {
        if (intent == ApprovalIntent.CONFIRM) {
            //同意
            ActionExecutionResult result = actionService.confirm(
                    context.userId(), context.permissions(), pending.id());
            return approvalReply(context, message, runId, result.reply());
        }
        if (intent == ApprovalIntent.CANCEL) {
            //放弃
            actionService.cancel(context.userId(), pending.id());
            return approvalReply(context, message, runId, "已取消本次操作。");
        }
        if (intent == ApprovalIntent.REVISE) {
            //修改
            String revisionContext = actionService.cancelForRevision(context.userId(), pending.id());
            return startAssistantRun(context, buildRevisionPrompt(revisionContext, message), runId);
        }
        return approvalReply(context, message, runId,
                "当前有一项操作等待确认，请明确回复确认执行、修改内容或取消。");
    }

    /** 按当前登录权限构造工具白名单并启动模型流。 */
    private Flux<AssistantSignal> startAssistantRun(
            AssistantRequestContext context, String message, Long runId) {
        return assistantGateway.stream(
                runId,
                String.valueOf(context.userId()),
                message,
                operationToolFactory.createTools(context));
    }

    /** 将 Action Spec 生成的不可信旧操作上下文和本次修改要求交给模型重新规划。 */
    private static String buildRevisionPrompt(String revisionContext, String message) {
        return revisionContext + "。修改要求：" + message;
    }

    /** 生成无需再次调用主模型的审批结果回复，并尽力同步聊天记忆。 */
    private Flux<AssistantSignal> approvalReply(AssistantRequestContext context,
                                              String message,
                                              Long runId,
                                              String reply) {
        try {
            memoryManager.addExchange(String.valueOf(context.userId()), message, reply);
        }
        catch (RuntimeException ignored) {
            // 审批业务已经完成时，记忆同步失败不得改变发布或取消结果。
        }
        return Flux.just(AssistantSignal.delta(runId, reply));
    }

}
