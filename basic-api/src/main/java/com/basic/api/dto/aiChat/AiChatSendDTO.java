package com.basic.api.dto.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 聊天发送请求，限制单次输入长度以控制模型调用成本和上下文规模。
 */
@Data
@Schema(description = "AI 聊天发送请求")
public class AiChatSendDTO {

    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息不能为空")
    @Size(max = 4000, message = "消息不能超过 4000 个字符")
    private String message;
}
