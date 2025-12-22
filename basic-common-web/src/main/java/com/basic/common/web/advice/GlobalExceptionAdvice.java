package com.basic.common.web.advice;

import com.basic.common.exception.BusinessException;
import com.basic.common.result.Result;
import com.basic.common.result.ResultEnum;
import com.basic.common.web.exception.ForbiddenException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类，用于统一处理系统中抛出的各种异常
 * 作用范围：com.basic包及其子包下的所有控制器
 */
@RestControllerAdvice(basePackages = "com.basic")
public class GlobalExceptionAdvice {

    /**
     * ---------------- 业务异常 ----------------
     */

    /**
     * 处理业务异常
     *
     * @param ex 业务异常对象
     * @return 统一响应结果，包含业务错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex) {
        return Result.failed(ex.getError());
    }

    /**
     * ---------------- Web 异常 ----------------
     */

    /**
     * 处理权限拒绝异常
     *
     * @param ex 权限拒绝异常对象
     * @return 统一响应结果，返回权限拒绝错误信息
     */
    @ExceptionHandler(ForbiddenException.class)
    public Result<?> handleForbiddenException(ForbiddenException ex) {
        return Result.failed(ResultEnum.FORBIDDEN);
    }

    /**
     * ---------------- 参数校验异常 ----------------
     */

    /**
     * 处理方法参数校验异常
     *
     * @param ex 方法参数校验异常对象
     * @return 统一响应结果，包含参数校验失败的具体字段信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        // 提取第一个校验失败的字段信息作为错误消息
        String msg = bindingResult.getFieldErrors().stream().map(e -> e.getField() + "：" + e.getDefaultMessage()).findFirst().orElse(ResultEnum.VALIDATE_FAILED.getMessage());

        return Result.failed(ResultEnum.VALIDATE_FAILED.getCode(), msg);
    }

    /**
     * 处理约束违反异常
     *
     * @param ex 约束违反异常对象
     * @return 统一响应结果，包含约束违反的错误信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.failed(ResultEnum.VALIDATE_FAILED.getCode(), ex.getMessage());
    }

    /**
     * ---------------- 系统异常兜底 ----------------
     */

    /**
     * 处理系统未知异常（兜底处理）
     *
     * @param ex 异常对象
     * @return 统一响应结果，返回系统错误信息
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        // 日志输出
//        log.error("系统异常", ex);
        return Result.failed(ResultEnum.SYSTEM_ERROR);
    }
}
