package com.basic.ai.agent.model;

import java.util.Map;

/**
 * 模型返回的结构化单步决策，不允许携带脚本、选择器或任意请求。
 *
 * @param completed 目标是否已经完成；为 {@code true} 时不应再下发动作
 * @param completionSummary 完成时提供的脱敏说明，未完成时允许为空
 * @param actionType 下一步动作类型，必须由调用上下文的能力集合提供
 * @param target 下一步语义目标，必须与动作类型共同匹配一项已公开能力
 * @param arguments 动作参数，只能符合该能力的参数约束
 * @param reason 未完成时说明选择依据，供服务端审计与无效响应识别
 */
public record AiAgentDecision(
        boolean completed,
        String completionSummary,
        String actionType,
        String target,
        Map<String, Object> arguments,
        String reason) {
}
