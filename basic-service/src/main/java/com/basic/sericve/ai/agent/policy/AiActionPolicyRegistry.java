package com.basic.sericve.ai.agent.policy;

import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.sericve.ai.agent.model.AiActionPolicy;
import com.basic.sericve.ai.agent.model.AiAgentRiskLevel;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element Plus 通用页面动作策略。
 *
 * <p>页面 target 只能由公共扫描器生成，服务端根据固定格式校验动作类型和风险标记。
 * 具体业务权限仍由被点击页面发起的原业务接口进行最终校验。</p>
 */
@Component
public class AiActionPolicyRegistry {

    private static final Pattern TARGET_PATTERN = Pattern.compile(
            "^auto\\.(click|fill|select)\\.(low|high)\\.([1-9]\\d*)$");

    /**
     * 校验公共页面动作并生成本次动作策略。
     */
    public AiActionPolicy require(
            String actionType, String target, String routeName, Set<String> permissions) {
        if (actionType == null || target == null || routeName == null || routeName.isBlank()) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }
        Matcher matcher = TARGET_PATTERN.matcher(target);
        if (!matcher.matches()) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }

        String kind = matcher.group(1);
        String risk = matcher.group(2);
        if (!actionType.equals("ui." + kind)) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }
        // 填写和选择不得伪装为高风险动作；只有点击可能触发业务状态变更。
        if (!"click".equals(kind) && !"low".equals(risk)) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }

        AiAgentRiskLevel riskLevel = "high".equals(risk)
                ? AiAgentRiskLevel.HIGH
                : AiAgentRiskLevel.LOW;
        boolean retryable = riskLevel == AiAgentRiskLevel.LOW;
        return new AiActionPolicy(
                actionType, target, routeName, riskLevel, retryable, Set.of());
    }
}
