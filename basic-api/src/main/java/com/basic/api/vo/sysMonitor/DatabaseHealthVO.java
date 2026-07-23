package com.basic.api.vo.sysMonitor;

import lombok.Data;

/**
 * 数据库健康状态VO
 *
 * @author Gas
 */
@Data
public class DatabaseHealthVO {

    /**
     * 状态：UP/DOWN
     */
    private String status;

    /**
     * 响应耗时，毫秒
     */
    private Long responseTimeMs;

    /**
     * 数据库产品名称
     */
    private String databaseProductName;

    /**
     * 数据库产品版本
     */
    private String databaseProductVersion;

    /**
     * JDBC地址，已脱敏
     */
    private String jdbcUrl;

    /**
     * 错误信息
     */
    private String errorMessage;
}
