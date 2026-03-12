package com.basic.api.dto.sysUser;

import com.basic.common.validate.annotation.Mobile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新DTO
 *
 * @author Gas
 */
@Data
public class UserUpdateDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String username;

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
