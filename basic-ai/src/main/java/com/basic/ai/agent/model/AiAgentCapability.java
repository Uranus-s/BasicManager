package com.basic.ai.agent.model;

import java.util.Map;

/**
 * 当前页面向模型公开的单个语义动作能力。
 *
 * @param actionType 供客户端执行器识别的受控动作类型，模型不能扩展该集合
 * @param target 动作允许操作的页面语义目标，不是 CSS 选择器或任意地址
 * @param parameterSchema 参数名称、类型和取值边界，供模型在受限范围内填写参数
 */
public record AiAgentCapability(
        String actionType,
        String target,
        Map<String, Object> parameterSchema) {
}
