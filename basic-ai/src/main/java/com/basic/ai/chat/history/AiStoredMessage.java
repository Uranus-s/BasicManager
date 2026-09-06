package com.basic.ai.chat.history;

import java.time.LocalDateTime;

/**
 * AI 模块内部的已持久化消息，不承担对外接口契约。
 */
public record AiStoredMessage(Long id, String role, String content,
                              boolean partial, LocalDateTime createTime) {
}
