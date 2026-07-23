package com.basic.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    /**
     * 用户名
     */
    @Schema(description = "登录用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "登录密码", example = "AdminP@ssw0rd!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "图形验证码内容", example = "8K4M")
    private String captcha;

    /**
     * 验证码UUID
     */
    @Schema(description = "图形验证码唯一标识", example = "0e2f42f2-ec7d-4e89-9515-f7ba2c612345")
    private String uuid;
}
