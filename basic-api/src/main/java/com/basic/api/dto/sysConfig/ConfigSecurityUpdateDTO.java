package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账号安全设置更新请求，用于维护登录凭证的安全策略。
 *
 * @author Gas
 */
@Data
@Schema(description = "账号安全设置更新请求")
public class ConfigSecurityUpdateDTO {

    /**
     * Token 有效期，单位为小时，用于控制登录凭证的最长可用时长。
     */
    @Schema(description = "Token 有效期，单位：小时", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Token有效期不能为空")
    @Min(value = 1, message = "Token有效期不能小于1小时")
    @Max(value = 168, message = "Token有效期不能超过168小时")
    private Integer tokenExpireHours;
}
