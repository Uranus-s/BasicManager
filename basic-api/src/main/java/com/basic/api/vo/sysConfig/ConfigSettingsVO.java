package com.basic.api.vo.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统设置视图，汇总基础设置与账号安全设置。
 *
 * @author Gas
 */
@Data
@Schema(description = "系统设置")
public class ConfigSettingsVO {

    /**
     * 系统名称，用于管理端及公开页面展示系统身份。
     */
    @Schema(description = "系统名称", example = "基础管理系统")
    private String systemName;

    /**
     * Token 有效期，单位为小时，用于表示登录凭证的可用时长。
     */
    @Schema(description = "Token 有效期，单位：小时", example = "24")
    private Integer tokenExpireHours;
}
