package com.basic.core.log.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作日志记录。
 */
@Getter
@Setter
@ToString
public class OperateLogRecord {

    private String module;

    private String method;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseResult;

    private Byte status;

    private Long costTime;
}
