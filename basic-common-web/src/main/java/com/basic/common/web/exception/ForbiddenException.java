package com.basic.common.web.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("无权限访问");
    }
}
