package com.basic.api.vo.auth;

import lombok.Data;

import java.util.List;

/**
 * 登录响应VO
 *
 * @author Gas
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色编码列表
     */
    private List<String> roles;

    /**
     * 权限标识列表
     */
    private List<String> permissions;
}
