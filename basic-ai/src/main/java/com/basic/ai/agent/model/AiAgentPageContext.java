package com.basic.ai.agent.model;

import java.util.List;
import java.util.Map;

/**
 * 模型单步决策上下文，页面状态已由业务层脱敏并限制能力集合。
 *
 * @param goal 用户目标的脱敏摘要，不承载身份、凭据等敏感输入
 * @param routeName 当前路由名称，用于限制决策适用的页面范围
 * @param pageVersion 页面状态版本标识，用于识别快照是否已过期
 * @param pageState 仅包含允许模型理解任务所需的脱敏页面状态
 * @param capabilities 当前快照明确公开的语义动作白名单
 * @param previousResult 上一步动作的脱敏结果，模型据此决定重试、调整或结束
 */
public record AiAgentPageContext(
        String goal,
        String routeName,
        String pageVersion,
        Map<String, Object> pageState,
        List<AiAgentCapability> capabilities,
        AiAgentPreviousResult previousResult) {
}
