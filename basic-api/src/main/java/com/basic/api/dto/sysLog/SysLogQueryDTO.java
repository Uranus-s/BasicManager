package com.basic.api.dto.sysLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一日志查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "统一系统日志分页查询条件")
public class SysLogQueryDTO {

    /**
     * 日志类型 LOGIN=登录日志 OPER=操作日志
     */
    @Schema(description = "日志类型：LOGIN登录日志，OPER操作日志", example = "OPER", allowableValues = {"LOGIN", "OPER"})
    private String logType;

    /**
     * 关键字（用户名、IP、消息、模块、方法、请求地址）
     */
    @Schema(description = "用户名、IP、消息、模块、方法或请求地址关键字", example = "用户管理")
    private String keyword;

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
