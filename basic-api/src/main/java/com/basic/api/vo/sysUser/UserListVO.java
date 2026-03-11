package com.basic.api.vo.sysUser;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表VO
 *
 * @author Gas
 */
@Data
public class UserListVO {

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
     * 角色名称列表
     */
    private List<String> roleNames;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
