package com.basic.sericve.ai.assistant;

import java.util.Set;

/**
 * 单次 AI 助手请求的用户、权限和关联消息快照。
 *
 * @param triggerMessageId 触发本次运行的用户消息 ID
 * @param userId 当前登录用户 ID
 * @param permissions 本次请求读取到的不可变权限快照
 */
public record AssistantRequestContext(
        Long triggerMessageId,
        Long userId,
        Set<String> permissions) {

    public AssistantRequestContext {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
