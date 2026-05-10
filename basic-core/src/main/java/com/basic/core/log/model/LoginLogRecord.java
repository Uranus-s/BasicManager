package com.basic.core.log.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 登录日志记录。
 */
@Getter
@Setter
@ToString
public class LoginLogRecord {

    private String username;

    private String ip;

    private String browser;

    private String os;

    private Byte status;

    private String msg;
}
