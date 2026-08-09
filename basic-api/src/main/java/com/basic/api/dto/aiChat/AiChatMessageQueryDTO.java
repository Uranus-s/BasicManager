package com.basic.api.dto.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * AI 聊天历史游标查询参数，默认读取最近 50 条消息。
 */
@Data
@Schema(description = "AI 聊天历史查询参数")
public class AiChatMessageQueryDTO {

    @Schema(description = "上一页最小消息 ID，首次查询时留空")
    @Positive(message = "历史消息游标必须大于 0")
    private Long beforeId;

    @Schema(description = "单页消息数", defaultValue = "50")
    @Min(value = 1, message = "单页消息数不能小于 1")
    @Max(value = 100, message = "单页消息数不能超过 100")
    private Integer limit = 50;
}
