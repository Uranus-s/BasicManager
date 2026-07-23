package com.basic.api.dto.auth;

import com.basic.common.validate.annotation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterDTO {

    /**
     * 用户名
     */
    @Schema(description = "注册用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "登录密码", example = "UserP@ssw0rd!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号码", example = "13800000000")
    @Mobile(required = false)
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像
     */
    @Schema(description = "头像访问地址", example = "/uploads/avatar/default.png")
    private String avatar;
}
