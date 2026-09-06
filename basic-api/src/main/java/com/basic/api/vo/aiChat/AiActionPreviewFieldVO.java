package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** AI 待确认操作预览中的通用标签和值。 */
@Data
@Schema(description = "AI 待确认操作预览字段")
public class AiActionPreviewFieldVO {

    private String label;
    private String value;
}
