package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 已持久化的 AI 聊天消息。
 */
@Data
@Schema(description = "AI 聊天消息")
public class AiChatMessageVO {
    private Long id;
    private String role;
    private String content;
    private Boolean partial;
    private LocalDateTime createTime;
}
