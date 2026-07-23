package com.basic.api.dto.sysOperLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "操作日志分页查询条件")
public class OperLogQueryDTO {

    /**
     * 模块名（模糊查询）
     */
    @Schema(description = "业务模块名称，支持模糊匹配", example = "用户管理")
    private String module;

    /**
     * 操作人
     */
    @Schema(description = "操作用户名", example = "admin")
    private String operationUser;

    /**
     * 请求方式
     */
    @Schema(description = "HTTP 请求方法", example = "POST", allowableValues = {"GET", "POST", "PUT", "DELETE", "PATCH"})
    private String requestMethod;

    /**
     * 状态 0=失败 1=成功
     */
    @Schema(description = "执行状态：0失败，1成功", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码，从1开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页记录数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
