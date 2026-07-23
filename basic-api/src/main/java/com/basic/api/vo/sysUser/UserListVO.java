package com.basic.api.vo.sysUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表VO
 *
 * @author Gas
 */
@Data
@Schema(description = "用户列表信息")
public class UserListVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户 ID", example = "1")
    private Long id;

    /**
     * 登录账号
     */
    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "用户昵称", example = "张三")
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
     * 头像
     */
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 部门名称列表
     */
    @Schema(description = "所属部门名称列表", example = "[\"研发部\"]")
    private List<String> deptNames;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表", example = "[\"系统管理员\"]")
    private List<String> roleNames;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;
}
