package com.basic.api.vo.sysLoginLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志VO
 *
 * @author Gas
 */
@Data
@Schema(description = "登录日志信息")
public class LoginLogVO {

    /**
     * 日志ID
     */
    @Schema(description = "日志 ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "登录账号", example = "zhangsan")
    private String username;

    /**
     * IP地址
     */
    @Schema(description = "登录 IP", example = "192.168.1.10")
    private String ip;

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

    /**
     * 状态 0=失败 1=成功
     */
    @Schema(description = "登录状态：0-失败，1-成功", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 提示消息
     */
    @Schema(description = "登录消息", example = "登录成功")
    private String msg;

    /**
     * 创建时间
     */
    @Schema(description = "登录时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;
}
