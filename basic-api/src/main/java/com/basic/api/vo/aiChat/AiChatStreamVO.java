package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 流式事件数据，兼容增量内容、停止状态和业务错误。
 */
@Data
@Schema(description = "AI 聊天流式事件")
public class AiChatStreamVO {
    private String content;
    private Boolean stopped;
    private Integer code;
    private String message;
}
