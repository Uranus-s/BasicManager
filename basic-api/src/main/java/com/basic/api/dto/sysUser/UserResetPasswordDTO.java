package com.basic.api.dto.sysUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码请求，用于为指定用户设置新的临时密码。
 *
 * @author Gas
 */
@Data
@Schema(description = "管理员重置用户密码请求")
public class UserResetPasswordDTO {

    /**
     * 新的临时密码，用户后续登录时使用该密码完成身份验证。
     */
    @Schema(description = "新的临时密码", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "临时密码不能为空")
    @Size(min = 6, max = 20, message = "临时密码长度必须在6-20之间")
    private String newPassword;
}
