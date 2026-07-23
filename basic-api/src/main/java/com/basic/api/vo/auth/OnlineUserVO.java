package com.basic.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户VO
 *
 * @author Gas
 */
@Data
@Schema(description = "在线用户信息")
public class OnlineUserVO {

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
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2026-07-23T10:30:00")
    private LocalDateTime loginTime;

    /**
     * 脱敏后的登录IP
     */
    @Schema(description = "登录 IP", example = "192.168.1.10")
    private String loginIp;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome 138")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 11")
    private String os;
}
