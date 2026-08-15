package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 配置更新请求。空值表示保留现有密钥，接口不会接收或返回已保存的明文。
 */
@Data
@Schema(description = "AI 配置更新请求")
public class ConfigAiUpdateDTO {

    @Schema(description = "是否启用站内网页代理", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "网页代理开关不能为空")
    private Boolean agentEnabled;

    @Schema(description = "新的 DeepSeek API Key，留空时保留现有配置", format = "password")
    @Size(max = 255, message = "DeepSeek API Key 不能超过 255 个字符")
    private String apiKey;
}
