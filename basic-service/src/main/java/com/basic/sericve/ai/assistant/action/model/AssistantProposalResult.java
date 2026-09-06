package com.basic.sericve.ai.assistant.action.model;

import java.time.LocalDateTime;

/** 所有写工具共用的提议结果，不暴露内部 Payload。 */
public record AssistantProposalResult(
        boolean success,
        Long actionId,
        LocalDateTime expiresAt,
        String message) {

    public static AssistantProposalResult failed(String message) {
        return new AssistantProposalResult(false, null, null, message);
    }

    public static AssistantProposalResult pending(Long actionId, LocalDateTime expiresAt) {
        return new AssistantProposalResult(true, actionId, expiresAt, "等待用户确认");
    }
}
