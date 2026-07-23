package com.basic.api.vo.sysOperLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author Gas
 */
@Data
@Schema(description = "操作日志信息")
public class OperLogVO {

    /**
     * 日志ID
     */
    @Schema(description = "日志 ID", example = "1")
    private Long id;

    /**
     * 模块名
     */
    @Schema(description = "操作模块", example = "用户管理")
    private String module;

    /**
     * 方法名
     */
    @Schema(description = "调用方法", example = "SysUserController.add")
    private String method;

    /**
     * 请求URL
     */
    @Schema(description = "请求地址", example = "/system/user")
    private String requestUrl;

    /**
     * 请求方式
     */
    @Schema(description = "HTTP 请求方法", example = "POST")
    private String requestMethod;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParams;

    /**
     * 返回结果
     */
    @Schema(description = "响应结果")
    private String responseResult;

    /**
     * 状态 0=失败 1=成功
     */
    @Schema(description = "执行状态：0-失败，1-成功", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 耗时(ms)
     */
    @Schema(description = "耗时，单位：毫秒", example = "25")
    private Long costTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;
}
