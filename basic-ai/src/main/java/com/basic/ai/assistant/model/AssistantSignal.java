package com.basic.ai.assistant.model;

/**
 * AI 助手运行过程中向上层发布的业务无关信号。
 *
 * @param type     信号类型
 * @param runId    运行标识
 * @param content  增量文本内容
 * @param toolName 工具名称
 * @param summary  面向调用方的简要说明
 * @param data     信号附带的结构化数据
 */
public record AssistantSignal(Type type, Long runId, String content, String toolName, String summary, Object data) {

    public enum Type {
        /** 可直接拼接到助手回答的文本增量。 */
        DELTA,
        /** 工具开始执行，仅用于进度展示和审计起点。 */
        TOOL_START,
        /** 工具执行结束，仅携带脱敏摘要和稳定错误码。 */
        TOOL_RESULT,
        /** 写工具已生成持久化快照，需要用户再次明确确认。 */
        APPROVAL_REQUIRED
    }

    /** 创建模型文本增量信号。 */
    public static AssistantSignal delta(Long runId, String content) {
        return new AssistantSignal(Type.DELTA, runId, content, null, null, null);
    }

    /** 创建工具开始信号，data 不得放入原始调用参数。 */
    public static AssistantSignal toolStart(Long runId, String toolName, String summary, Object data) {
        return new AssistantSignal(Type.TOOL_START, runId, null, toolName, summary, data);
    }

    /** 创建工具结果信号，summary 应为可安全展示的短文本。 */
    public static AssistantSignal toolResult(Long runId, String toolName, String summary, Object data) {
        return new AssistantSignal(Type.TOOL_RESULT, runId, null, toolName, summary, data);
    }

    /** 创建待审批信号，data 只允许携带面向业务层的安全对象。 */
    public static AssistantSignal approvalRequired(Long runId, String toolName, String summary, Object data) {
        return new AssistantSignal(Type.APPROVAL_REQUIRED, runId, null, toolName, summary, data);
    }
}
