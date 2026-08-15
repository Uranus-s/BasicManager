package com.basic.ai.agent.model;

/**
 * 上一步动作的脱敏结果，供模型决定下一步但不包含业务敏感正文。
 *
 * @param actionId 上一步动作的幂等标识，用于关联本次决策与执行记录
 * @param status 上一步执行状态，模型据此判断是否需要调整后续步骤
 * @param summary 可供模型理解结果的脱敏摘要，不包含页面原文或敏感业务数据
 */
public record AiAgentPreviousResult(String actionId, String status, String summary) {
}
