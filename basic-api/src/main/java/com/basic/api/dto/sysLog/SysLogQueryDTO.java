package com.basic.api.dto.sysLog;

import lombok.Data;

/**
 * 统一日志查询DTO
 *
 * @author Gas
 */
@Data
public class SysLogQueryDTO {

    /**
     * 日志类型 LOGIN=登录日志 OPER=操作日志
     */
    private String logType;

    /**
     * 关键字（用户名、IP、消息、模块、方法、请求地址）
     */
    private String keyword;

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
