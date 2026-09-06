package com.basic.ai.assistant.runtime;

import com.basic.ai.assistant.model.AssistantSignal;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 保存一次 AI 助手运行的工具调用计数与信号出口。
 *
 * <p>实例通过 Spring AI 的 ToolContext 只在当前请求内传递。工具批次在执行前统一计数，
 * 防止模型循环突破单次运行允许的工具调用总数。</p>
 */
public final class AssistantRunContext {

    public static final String TOOL_CONTEXT_KEY = AssistantRunContext.class.getName();

    private final Long runId;
    private final AssistantRuntimeProperties properties;
    private final Consumer<AssistantSignal> signalConsumer;
    private final AtomicInteger modelRoundCount = new AtomicInteger();
    private int toolCallCount;
    private final AtomicBoolean waitingApproval = new AtomicBoolean();
    private final AtomicReference<AssistantSignal> pendingApprovalSignal = new AtomicReference<>();

    /** 创建绑定到单次流订阅的运行上下文。 */
    public AssistantRunContext(Long runId,
                               AssistantRuntimeProperties properties,
                               Consumer<AssistantSignal> signalConsumer) {
        this.runId = Objects.requireNonNull(runId, "runId 不能为空");
        this.properties = Objects.requireNonNull(properties, "properties 不能为空");
        this.signalConsumer = Objects.requireNonNull(signalConsumer, "signalConsumer 不能为空");
    }

    /**
     * 在工具批次真正执行前统一校验并提交调用总数。
     */
    public synchronized void beforeToolBatch(ChatResponse chatResponse) {
        List<AssistantMessage.ToolCall> toolCalls = extractToolCalls(chatResponse);
        int nextToolCallCount = toolCallCount + toolCalls.size();
        if (nextToolCallCount > properties.maxToolCalls()) {
            throw loopLimitException();
        }
        toolCallCount = nextToolCallCount;
    }

    /** 将已脱敏的运行信号发送到当前 SSE 流。 */
    public void emit(AssistantSignal signal) {
        signalConsumer.accept(Objects.requireNonNull(signal, "signal 不能为空"));
    }

    /**
     * 从 Spring AI 工具上下文中读取当前 Run，禁止工具脱离受控运行时执行。
     */
    public static AssistantRunContext from(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
        Object context = toolContext.getContext().get(TOOL_CONTEXT_KEY);
        if (!(context instanceof AssistantRunContext assistantRunContext)) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
        return assistantRunContext;
    }

    /** 返回本次运行的日志关联标识。 */
    public Long getRunId() {
        return runId;
    }

    /** 返回下一次真实模型调用的轮次，编号从 1 开始。 */
    public int nextModelRound() {
        return modelRoundCount.incrementAndGet();
    }

    /** 返回最近一次已经开始的模型轮次。 */
    public int getModelRoundCount() {
        return modelRoundCount.get();
    }

    /**
     * 标记写工具已生成待审批快照；信号由工具包装器在结果信号之后发送。
     */
    public void markWaitingApproval(String toolName, String summary, Object data) {
        // 先保存信号再置位，保证其他线程观察到 waitingApproval 时信号已经可读取。
        pendingApprovalSignal.set(AssistantSignal.approvalRequired(runId, toolName, summary, data));
        waitingApproval.set(true);
    }

    /** 判断写工具是否已经生成待审批快照。 */
    public boolean isWaitingApproval() {
        return waitingApproval.get();
    }

    /**
     * 在 TOOL_RESULT 之后发送一次待审批信号，使客户端看到的事件顺序稳定。
     */
    void emitPendingApprovalIfPresent() {
        AssistantSignal signal = pendingApprovalSignal.getAndSet(null);
        if (signal != null) {
            emit(signal);
        }
    }

    /** 从模型本批响应中提取非空工具调用列表，格式异常按系统错误处理。 */
    private static List<AssistantMessage.ToolCall> extractToolCalls(ChatResponse chatResponse) {
        Objects.requireNonNull(chatResponse, "chatResponse 不能为空");
        List<Generation> results = chatResponse.getResults();
        if (results != null) {
            for (Generation result : results) {
                if (result == null || result.getOutput() == null) {
                    continue;
                }
                List<AssistantMessage.ToolCall> toolCalls = result.getOutput().getToolCalls();
                if (toolCalls != null && !toolCalls.isEmpty()) {
                    return toolCalls;
                }
            }
        }
        throw new BusinessException(ResultEnum.SYSTEM_ERROR);
    }

    private static BusinessException loopLimitException() {
        return new BusinessException(ResultEnum.AI_AGENT_LOOP_LIMIT);
    }
}
