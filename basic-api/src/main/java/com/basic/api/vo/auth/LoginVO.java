package com.basic.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户信息 VO
 *
 * @author Gas
 */
@Data
@Schema(description = "登录用户信息")
public class LoginVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像地址", example = "https://example.com/avatar/zhangsan.png")
    private String avatar;

    /**
     * 部门名称列表
     */
    @Schema(description = "所属部门名称列表", example = "[\"研发部\"]")
    private List<String> deptNames;

    /**
     * 角色编码列表
     */
    @Schema(description = "角色编码列表", example = "[\"system_admin\"]")
    private List<String> roles;

    /**
     * 权限标识列表
     */
    @Schema(description = "权限标识列表", example = "[\"system:user:list\"]")
    private List<String> permissions;
}
