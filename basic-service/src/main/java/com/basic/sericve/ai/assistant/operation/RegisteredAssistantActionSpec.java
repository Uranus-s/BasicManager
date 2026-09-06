package com.basic.sericve.ai.assistant.operation;

import java.util.Objects;

/** 带业务能力归属的写操作定义，供 Action 路由和通用审计使用。 */
public record RegisteredAssistantActionSpec<I, P>(
        String capabilityId,
        AssistantActionSpec<I, P> spec) {

    public RegisteredAssistantActionSpec {
        capabilityId = Objects.requireNonNull(capabilityId, "能力标识不能为空");
        spec = Objects.requireNonNull(spec, "Action Spec 不能为空");
    }
}
