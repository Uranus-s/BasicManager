package com.basic.ai.agent.gateway;

import com.basic.ai.agent.model.AiAgentDecision;
import com.basic.ai.agent.model.AiAgentPageContext;

/**
 * AI 网页代理的模型调用边界，隔离业务编排与具体模型提供商实现。
 * 每次调用只返回一个结构化决策，调用方据此管理任务状态，提供商实现不得执行页面动作。
 */
public interface AiAgentGateway {

    /**
     * 根据脱敏页面上下文生成下一步决策。
     *
     * @param context 包含当前允许动作的页面快照，不能携带原始敏感页面数据
     * @return 目标完成结论或一个待校验的下一步语义动作
     */
    AiAgentDecision decide(AiAgentPageContext context);
}
