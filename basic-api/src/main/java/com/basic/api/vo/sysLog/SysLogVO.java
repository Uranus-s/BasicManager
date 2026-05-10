package com.basic.api.vo.sysLog;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一日志VO
 *
 * @author Gas
 */
@Data
public class SysLogVO {

    /**
     * 源日志ID
     */
    private Long id;

    /**
     * 日志类型 LOGIN=登录日志 OPER=操作日志
     */
    private String logType;

    /**
     * 列表标题
     */
    private String title;

    /**
     * 列表内容
     */
    private String content;

    /**
     * 方法名
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * IP地址
     */
    private String ip;

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
