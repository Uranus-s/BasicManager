package com.basic.dao.sysLog.model;

import lombok.Data;

/**
 * 统一日志查询条件
 *
 * @author Gas
 */
@Data
public class SysLogQuery {

    /**
     * 日志类型 LOGIN=登录日志 OPER=操作日志
     */
    private String logType;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 状态 0=失败 1=成功
     */
    private Byte status;
}
