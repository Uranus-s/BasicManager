package com.basic.api.vo.sysOperLog;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author Gas
 */
@Data
public class OperLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 模块名
     */
    private String module;

    /**
     * 方法名
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 返回结果
     */
    private String responseResult;

    /**
     * 状态 0=失败 1=成功
     */
    private Byte status;

    /**
     * 耗时(ms)
     */
    private Long costTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
