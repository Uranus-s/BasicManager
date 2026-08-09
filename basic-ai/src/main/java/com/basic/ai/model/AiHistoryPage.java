package com.basic.ai.model;

import java.util.List;

/**
 * 基于消息 ID 游标的内部历史页，消息顺序为从旧到新。
 */
public record AiHistoryPage(List<AiStoredMessage> messages,
                            boolean hasMore, Long nextBeforeId) {
}
