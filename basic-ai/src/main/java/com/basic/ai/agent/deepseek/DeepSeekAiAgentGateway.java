package com.basic.ai.agent.deepseek;

import com.basic.ai.agent.gateway.AiAgentGateway;
import com.basic.ai.agent.model.AiAgentDecision;
import com.basic.ai.agent.model.AiAgentPageContext;
import com.basic.ai.deepseek.DeepSeekChatClientFactory;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * DeepSeek 网页代理网关，负责将通用代理决策请求适配为 DeepSeek 调用。
 *
 * <p>网关不持有任务状态，也不执行页面动作；它只接受结构化单步响应，并确保模型
 * 不能绕过业务层公开的能力集合自行创造动作或目标。</p>
 */
@Component
@RequiredArgsConstructor
public class DeepSeekAiAgentGateway implements AiAgentGateway {

    private final DeepSeekChatClientFactory factory;
    private final AiAgentPromptFactory promptFactory;

    /**
     * 请求模型规划下一步，并将反序列化后的响应收敛为受当前页面能力约束的决策。
     * 模型调用或结构化反序列化失败时直接向业务层传播异常，由任务编排统一失败关闭；
     * 网关不在缺少新页面上下文的情况下重试，也不把格式错误的响应降级为可执行动作。
     *
     * @param context 业务层构造的脱敏页面上下文
     * @return 已校验的完成结论或单个可执行语义动作
     */
    @Override
    public AiAgentDecision decide(AiAgentPageContext context) {
        // 模型响应必须先映射为固定结构，再进入白名单校验，不能直接作为客户端指令下发。
        AiAgentDecision decision = factory.current().prompt()
                .system(promptFactory.systemPrompt())
                .user(promptFactory.userPrompt(context))
                .call()
                .entity(AiAgentDecision.class);
        return validate(context, decision);
    }

    /**
     * 校验模型响应完整性，并拒绝模型自行创造动作或页面目标。
     * 已完成的响应只能给出摘要；未完成的响应必须精确匹配当前快照暴露的动作和目标，
     * 从而在模型响应缺失、格式不完整或越权时统一阻断后续执行。
     */
    AiAgentDecision validate(AiAgentPageContext context, AiAgentDecision decision) {
        if (decision == null) {
            throw new BusinessException(ResultEnum.REMOTE_RESPONSE_ERROR);
        }
        if (decision.completed()) {
            if (!StringUtils.hasText(decision.completionSummary())) {
                throw new BusinessException(ResultEnum.REMOTE_RESPONSE_ERROR);
            }
            return decision;
        }
        boolean allowed = context.capabilities().stream().anyMatch(capability ->
                capability.actionType().equals(decision.actionType())
                        && capability.target().equals(decision.target()));
        if (!allowed || !StringUtils.hasText(decision.reason())) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }
        return decision;
    }
}
