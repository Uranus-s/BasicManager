package com.basic.ai.assistant.runtime;

import com.basic.ai.assistant.logging.AssistantConversationLogger;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在 Spring AI 执行工具前应用单次 AI 助手运行的安全边界。
 *
 * <p>该管理器包裹 Spring AI 默认实现：执行前校验工具调用总数，执行后在写工具生成
 * 待审批快照时强制终止 ReAct 循环，避免模型继续生成未经确认的成功描述。</p>
 */
@Component
public final class GuardedToolCallingManager implements ToolCallingManager {

    /** 仅记录服务端诊断信息，避免将底层工具异常细节返回给客户端。 */
    private static final Logger LOG = LoggerFactory.getLogger(GuardedToolCallingManager.class);

    /** Spring AI 原生工具调度器，实际的工具解析和调用均委托给它完成。 */
    private final ToolCallingManager delegate;
    /** 统一记录模型轮次与工具执行结果，便于按一次运行关联排查问题。 */
    private final AssistantConversationLogger conversationLogger;

    /**
     * 创建受控的工具调度器，并将底层工具执行异常转换为对外稳定的业务错误。
     *
     * <p>已有 {@link BusinessException} 会原样透传，保证工具主动声明的业务错误码不被覆盖；
     * 其余异常仅写入服务端日志，防止实现细节泄露给客户端。</p>
     */
    @Autowired
    public GuardedToolCallingManager(AssistantConversationLogger conversationLogger) {
        this(ToolCallingManager.builder()
                .toolExecutionExceptionProcessor(exception -> {
                    BusinessException businessException = findBusinessException(exception);
                    if (businessException != null) {
                        throw businessException;
                    }
                    // 服务端保留工具名和完整异常链，对客户端仍只暴露稳定错误码。
                    LOG.error("AI 工具执行异常，tool={}",exception.getToolDefinition().name(), exception);
                    throw new BusinessException(ResultEnum.REMOTE_SERVICE_ERROR);
                })
                .build(), conversationLogger);
    }

    /**
     * 注入底层调度器和会话日志器。包级可见性用于在同包测试或装配场景中替换底层实现。
     */
    GuardedToolCallingManager(ToolCallingManager delegate,
                              AssistantConversationLogger conversationLogger) {
        this.delegate = Objects.requireNonNull(delegate, "delegate 不能为空");
        this.conversationLogger = Objects.requireNonNull(
                conversationLogger, "conversationLogger 不能为空");
    }

    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions options) {
        // 不改变 Spring AI 的工具解析规则，仅在执行阶段补充运行时安全边界。
        return delegate.resolveToolDefinitions(options);
    }

    /**
     * 执行当前模型响应请求的工具批次，并控制审批场景下的 ReAct 循环。
     *
     * <p>批次执行前先累计工具调用数，防止模型通过多轮递归绕过单次运行上限。
     * 写工具生成待审批快照后，保留工具历史并直接返回，使模型无法在用户确认前继续推理或
     * 生成“已成功”的描述。</p>
     */
    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        AssistantRunContext context = resolveRunContext(prompt);
        context.beforeToolBatch(chatResponse);
        try {
            ToolExecutionResult result = delegate.executeToolCalls(prompt, chatResponse);
            if (context.isWaitingApproval() && !result.returnDirect()) {
                // 保留工具对话历史，但强制把本轮作为直接返回结束模型后续推理。
                result = ToolExecutionResult.builder()
                        .conversationHistory(result.conversationHistory())
                        .returnDirect(true)
                        .build();
            }
            conversationLogger.logToolResult(
                    context.getRunId(), context.getModelRoundCount(), result);
            return result;
        }
        catch (BusinessException exception) {
            throw exception;
        }
        catch (RuntimeException exception) {
            BusinessException businessException = findBusinessException(exception);
            if (businessException != null) {
                throw businessException;
            }
            // 批次调度异常同样记录完整堆栈，但不得向客户端泄露内部实现细节。
            LOG.error("AI 工具调用批次异常，runId={}，round={}",
                    context.getRunId(), context.getModelRoundCount(), exception);
            throw new BusinessException(ResultEnum.REMOTE_SERVICE_ERROR);
        }
    }

    /** 沿异常链提取已有业务错误，避免框架包装后被误改写为远程服务异常。 */
    private static BusinessException findBusinessException(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof BusinessException businessException) {
                return businessException;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 从 Prompt 的工具上下文中取得本次运行状态。
     *
     * <p>工具调用必须绑定受控运行上下文；缺失或类型不符通常意味着调用链配置错误，
     * 因此按系统错误终止，而不是在没有调用次数和审批状态保护的情况下继续执行。</p>
     */
    private static AssistantRunContext resolveRunContext(Prompt prompt) {
        if (!(prompt.getOptions() instanceof ToolCallingChatOptions options)) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
        Map<String, Object> toolContext = options.getToolContext();
        Object context = toolContext == null ? null : toolContext.get(AssistantRunContext.TOOL_CONTEXT_KEY);
        if (!(context instanceof AssistantRunContext assistantRunContext)) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
        return assistantRunContext;
    }
}
