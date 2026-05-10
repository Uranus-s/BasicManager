package com.basic.dao.sysLog.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一日志查询记录
 *
 * @author Gas
 */
@Data
public class SysLogRecord {

    private Long id;

    private String logType;

    private String title;

    private String content;

    private String method;

    private String requestMethod;

    private String ip;

    private Byte status;

    private Long costTime;

    private LocalDateTime createTime;
}
