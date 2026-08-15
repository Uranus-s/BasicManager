package com.basic.sericve.ai.agent.model;

import java.util.Set;

/**
 * 由服务端登录态构建的代理用户上下文，不接受客户端提交的身份或权限。
 *
 * @param userId 用户 ID
 * @param permissions 当前用户权限集合
 */
public record AiAgentUserContext(Long userId, Set<String> permissions) {

    public AiAgentUserContext {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
