package com.basic.api.dto.sysUser;

import com.basic.common.validate.annotation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "修改用户请求参数")
public class UserUpdateDTO {

    /**
     * 用户ID
     */
    @Schema(description = "用户 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 昵称
     */
    @Schema(description = "登录账号", example = "zhangsan")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String username;

    /**
     * 手机号
     */
    @Schema(description = "手机号码", example = "13800000000")
    @Mobile
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
    @Schema(description = "头像地址", example = "https://example.com/avatar/zhangsan.png")
    private String avatar;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "用户状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 部门ID列表
     */
    @Schema(description = "所属部门 ID 列表", example = "[1, 2]")
    private java.util.List<Long> deptIds;

    /**
     * 角色ID列表
     */
    @Schema(description = "拥有的角色 ID 列表", example = "[1, 2]")
    private java.util.List<Long> roleIds;
}
