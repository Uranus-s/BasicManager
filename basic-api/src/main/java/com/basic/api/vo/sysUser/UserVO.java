package com.basic.api.vo.sysUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情VO
 *
 * @author Gas
 */
@Data
@Schema(description = "用户详细信息")
public class UserVO {

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
    @Schema(description = "所属部门名称列表")
    private List<String> deptNames;

    /**
     * 角色列表
     */
    @Schema(description = "拥有的角色列表")
    private List<RoleInfo> roles;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-07-23T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 角色信息
     */
    @Data
    @Schema(description = "用户角色摘要")
    public static class RoleInfo {
        @Schema(description = "角色 ID", example = "1")
        private Long id;
        @Schema(description = "角色编码", example = "system_admin")
        private String roleCode;
        @Schema(description = "角色名称", example = "系统管理员")
        private String roleName;
    }
}
