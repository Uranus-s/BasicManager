package com.basic.common.result;

public enum ResultEnum implements IResult {
    /* ===================== 成功 ===================== */
    SUCCESS(0, "成功"),

    /* ===================== 1xxxx 参数校验 ===================== */
    PARAM_MISSING(10001, "参数缺失"),
    PARAM_INVALID(10002, "参数格式错误"),
    PARAM_ILLEGAL(10003, "参数值非法"),
    PARAM_OUT_OF_RANGE(10004, "参数超出范围"),
    REQUEST_BODY_ERROR(10005, "请求体解析失败"),
    VALIDATE_FAILED(10006, "参数校验未通过"),

    /* ===================== 2xxxx 认证 / 权限 ===================== */
    UNAUTHORIZED(20001, "未登录或登录已过期"),
    FORBIDDEN(20002, "无访问权限"),
    TOKEN_INVALID(20003, "Token 无效"),
    TOKEN_EXPIRED(20004, "Token 已过期"),
    ACCOUNT_DISABLED(20005, "账号被禁用"),
    ACCOUNT_LOCKED(20006, "账号被锁定"),

    /* ===================== 3xxxx 用户 / 身份 ===================== */
    USER_NOT_EXIST(30001, "用户不存在"),
    USER_ALREADY_EXIST(30002, "用户已存在"),
    PASSWORD_ERROR(30003, "密码错误"),
    USER_STATUS_ERROR(30004, "用户状态异常"),
    PHONE_ALREADY_BIND(30005, "手机号已绑定"),
    EMAIL_ALREADY_BIND(30006, "邮箱已绑定"),

    /* ===================== 4xxxx 业务状态 / 操作冲突 ===================== */
    STATUS_NOT_ALLOWED(40001, "当前状态不允许该操作"),
    DATA_VERSION_EXPIRED(40002, "数据已被修改，请刷新后重试"),
    REPEAT_SUBMIT(40003, "请勿重复提交"),
    REQUEST_TOO_FREQUENT(40004, "请求过于频繁"),
    IDEMPOTENT_CHECK_FAILED(40005, "幂等校验失败"),

    /* ===================== 5xxxx 数据 / 资源 ===================== */
    DATA_NOT_EXIST(50001, "数据不存在"),
    DATA_ALREADY_EXIST(50002, "数据已存在"),
    DATA_NOT_UNIQUE(50003, "数据不唯一"),
    DATA_STATUS_ERROR(50004, "数据状态异常"),
    RELATION_DATA_NOT_EXIST(50005, "关联数据不存在"),

    /* ===================== 6xxxx 第三方 / 远程调用 ===================== */
    REMOTE_SERVICE_ERROR(60001, "远程服务异常"),
    REMOTE_SERVICE_TIMEOUT(60002, "远程服务超时"),
    REMOTE_RESPONSE_ERROR(60003, "远程服务返回异常"),
    RPC_CALL_FAILED(60004, "RPC 调用失败"),
    THIRD_PARTY_AUTH_FAILED(60005, "第三方鉴权失败"),

    /* ===================== 9xxxx 系统异常 ===================== */
    SYSTEM_ERROR(90001, "系统异常"),
    DATABASE_ERROR(90002, "数据库异常"),
    CACHE_ERROR(90003, "缓存异常"),
    IO_ERROR(90004, "IO 异常"),
    SERIALIZE_ERROR(90005, "序列化/反序列化异常"),
    UNKNOWN_ERROR(99999, "未知异常");

    private final Integer code;
    private final String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
