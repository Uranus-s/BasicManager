package com.basic.sericve.ai.agent.model;

import java.util.Set;

/**
 * 服务端动作白名单条目，绑定动作、页面、风险等级和所需权限。
 *
 * @param actionType 动作类型
 * @param target 页面语义目标
 * @param routeName 前端路由名称
 * @param riskLevel 风险等级
 * @param retryable 是否允许安全重试
 * @param authorities 任一满足即可执行的权限集合
 */
public record AiActionPolicy(
        String actionType,
        String target,
        String routeName,
        AiAgentRiskLevel riskLevel,
        boolean retryable,
        Set<String> authorities) {

    public AiActionPolicy {
        authorities = Set.copyOf(authorities);
    }
}
