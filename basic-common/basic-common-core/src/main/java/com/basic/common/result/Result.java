package com.basic.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一结果返回封装类
 * 用于封装接口返回结果，包含成功/失败状态、状态码、消息和数据
 *
 * @param <T> 数据泛型类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 请求是否成功标识
     */
    private boolean success;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 构建成功结果对象（无返回值）
     *
     * @return 成功的结果对象
     */
    public static Result<?> success() {
        Result<?> result = new Result<>();
        result.setSuccess(true);
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(ResultEnum.SUCCESS.getMessage());
        result.setData(null);
        return result;
    }

    /**
     * 构建成功结果对象
     *
     * @param data 成功时返回的数据
     * @param <T>  数据泛型类型
     * @return 成功的结果对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(ResultEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 构建成功结果对象（自定义消息）
     *
     * @param message 自定义成功消息
     * @param data   成功时返回的数据
     * @param <T>    数据泛型类型
     * @return 成功的结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 构建失败结果对象（使用默认错误）
     *
     * @return 失败的结果对象
     */
    public static Result<?> failed() {
        return failed(ResultEnum.SYSTEM_ERROR);
    }

    /**
     * 构建失败结果对象（指定错误类型）
     *
     * @param errorResult 错误结果枚举
     * @return 失败的结果对象
     */
    public static Result<?> failed(IResult errorResult) {
        Result<?> result = new Result<>();
        result.setSuccess(false);
        result.setCode(errorResult.getCode());
        result.setMessage(errorResult.getMessage());
        result.setData(null);
        return result;
    }

    /**
     * 构建失败结果对象（指定错误类型）
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 失败的结果对象
     */
    public static Result<?> failed(Integer code, String message) {
        Result<?> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 构建结果对象实例
     *
     * @param success 是否成功
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据泛型类型
     * @return 结果对象实例
     */
    public static <T> Result<T> build(boolean success, Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(success);
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
