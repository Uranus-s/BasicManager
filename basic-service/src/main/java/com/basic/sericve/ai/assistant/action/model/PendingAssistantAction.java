package com.basic.sericve.ai.assistant.action.model;

import java.time.LocalDateTime;

/**
 * 编排层可见的通用待审批操作，不暴露 Payload JSON。
 */
public record PendingAssistantAction(
        Long id,
        String actionType,
        LocalDateTime expiresAt,
        ActionPreview preview) {
}
