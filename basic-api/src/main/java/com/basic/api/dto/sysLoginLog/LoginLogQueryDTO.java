package com.basic.api.dto.sysLoginLog;

import lombok.Data;

/**
 * 登录日志查询DTO
 *
 * @author Gas
 */
@Data
public class LoginLogQueryDTO {

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 状态 0=失败 1=成功
     */
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
