package com.basic.api.dto.sysLoginLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录日志查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "登录日志分页查询条件")
public class LoginLogQueryDTO {

    /**
     * 用户名（模糊查询）
     */
    @Schema(description = "用户名，支持模糊匹配", example = "admin")
    private String username;

    /**
     * IP地址
     */
    @Schema(description = "登录IP地址", example = "192.168.1.10")
    private String ip;

    /**
     * 状态 0=失败 1=成功
     */
    @Schema(description = "登录状态：0失败，1成功", example = "1", allowableValues = {"0", "1"})
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
