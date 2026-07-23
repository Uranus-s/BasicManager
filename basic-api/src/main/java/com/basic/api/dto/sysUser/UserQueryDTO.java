package com.basic.api.dto.sysUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "用户分页查询参数")
public class UserQueryDTO {

    /**
     * 登录账号（模糊查询）
     */
    @Schema(description = "登录账号，支持模糊查询", example = "zhang")
    private String username;

    /**
     * 昵称（模糊查询）
     */
    @Schema(description = "用户昵称，支持模糊查询", example = "张三")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号码", example = "13800000000")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "用户状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页条数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
