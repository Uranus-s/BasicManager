package com.basic.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 初始化管理员请求DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "初始化管理员请求")
public class InitAdminDTO {

    /**
     * 初始化密钥
     */
    @Schema(description = "系统管理员初始化密钥", example = "replace-with-init-key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "初始化密钥不能为空")
    private String initKey;

    /**
     * 管理员密码
     */
    @Schema(description = "初始管理员登录密码", example = "AdminP@ssw0rd!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String adminPassword;
}
