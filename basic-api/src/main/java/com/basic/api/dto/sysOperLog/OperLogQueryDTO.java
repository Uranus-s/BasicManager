package com.basic.api.dto.sysOperLog;

import lombok.Data;

/**
 * 操作日志查询DTO
 *
 * @author Gas
 */
@Data
public class OperLogQueryDTO {

    /**
     * 模块名（模糊查询）
     */
    private String module;

    /**
     * 操作人
     */
    private String operationUser;

    /**
     * 请求方式
     */
    private String requestMethod;

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
