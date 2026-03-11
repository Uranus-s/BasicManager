package com.basic.api.vo.sysLoginLog;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志VO
 *
 * @author Gas
 */
@Data
public class LoginLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 状态 0=失败 1=成功
     */
    private Byte status;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
