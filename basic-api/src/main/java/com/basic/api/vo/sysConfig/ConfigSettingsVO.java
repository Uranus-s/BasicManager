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

    /**
     * 是否启用站内网页代理；未配置时默认关闭。
     */
    @Schema(description = "是否启用站内网页代理")
    private Boolean agentEnabled;

    /**
     * 是否已经保存 DeepSeek API Key，不暴露密钥原值。
     */
    @Schema(description = "是否已配置 DeepSeek API Key")
    private Boolean deepSeekApiKeyConfigured;

    /**
     * DeepSeek API Key 掩码，仅用于帮助管理员确认当前配置。
     */
    @Schema(description = "DeepSeek API Key 掩码")
    private String deepSeekApiKeyMasked;
}
