package com.basic.api.vo.auth;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户VO
 *
 * @author Gas
 */
@Data
public class OnlineUserVO {

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
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 脱敏后的登录IP
     */
    private String loginIp;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;
}
