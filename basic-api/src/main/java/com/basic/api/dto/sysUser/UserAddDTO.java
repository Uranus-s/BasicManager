package com.basic.api.dto.sysUser;

import com.basic.common.validate.annotation.Mobile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户新增DTO
 *
 * @author Gas
 */
@Data
public class UserAddDTO {

    /**
     * 登录账号
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /**
     * 手机号
     */
    @Mobile
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 部门ID列表
     */
    private java.util.List<Long> deptIds;

    /**
     * 角色ID列表
     */
    private java.util.List<Long> roleIds;
}
