package com.basic.api.vo.sysUser;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情VO
 *
 * @author Gas
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
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
     * 部门名称列表
     */
    private List<String> deptNames;

    /**
     * 角色列表
     */
    private List<RoleInfo> roles;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 角色信息
     */
    @Data
    public static class RoleInfo {
        private Long id;
        private String roleCode;
        private String roleName;
    }
}
