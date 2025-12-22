package com.basic.common.exception;

import com.basic.common.result.IResult;

public class BusinessException extends RuntimeException {

    private final IResult error;

    public BusinessException(IResult error) {
        super(error.getMessage());
        this.error = error;
    }

    public IResult getError() {
        return error;
    }
}