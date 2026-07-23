package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库健康状态VO
 *
 * @author Gas
 */
@Data
@Schema(description = "数据库健康信息")
public class DatabaseHealthVO {

    /**
     * 状态：UP/DOWN
     */
    @Schema(description = "健康状态", example = "UP", allowableValues = {"UP", "DOWN"})
    private String status;

    /**
     * 响应耗时，毫秒
     */
    @Schema(description = "响应耗时，单位：毫秒", example = "12")
    private Long responseTimeMs;

    /**
     * 数据库产品名称
     */
    @Schema(description = "数据库产品名称", example = "MySQL")
    private String databaseProductName;

    /**
     * 数据库产品版本
     */
    @Schema(description = "数据库产品版本", example = "8.4.0")
    private String databaseProductVersion;

    /**
     * JDBC地址，已脱敏
     */
    @Schema(description = "JDBC 地址", example = "jdbc:mysql://localhost:3306/basic_project")
    private String jdbcUrl;

    /**
     * 错误信息
     */
    @Schema(description = "异常信息，健康时为空")
    private String errorMessage;
}
