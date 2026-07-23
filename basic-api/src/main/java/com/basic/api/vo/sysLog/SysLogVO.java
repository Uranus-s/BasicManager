package com.basic.api.vo.sysLog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一日志VO
 *
 * @author Gas
 */
@Data
@Schema(description = "系统日志信息")
public class SysLogVO {

    /**
     * 源日志ID
     */
    @Schema(description = "日志 ID", example = "1")
    private Long id;

    /**
     * 日志类型 LOGIN=登录日志 OPER=操作日志
     */
    @Schema(description = "日志类型", example = "OPERATE")
    private String logType;

    /**
     * 列表标题
     */
    @Schema(description = "日志标题", example = "新增用户")
    private String title;

    /**
     * 列表内容
     */
    @Schema(description = "日志内容", example = "新增用户成功")
    private String content;

    /**
     * 方法名
     */
    @Schema(description = "调用方法", example = "com.basic.web.controller.SysUserController.add")
    private String method;

    /**
     * 请求方式
     */
    @Schema(description = "HTTP 请求方法", example = "POST")
    private String requestMethod;

    /**
     * IP地址
     */
    @Schema(description = "客户端 IP", example = "192.168.1.10")
    private String ip;

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
