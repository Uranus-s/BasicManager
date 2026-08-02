/**
 * @description 导出默认网路配置
 **/
const network = {
  // 默认走同源真实后端接口，开发环境由 rspack devServer 按路由前缀代理。
  baseURL: "",
  //配后端数据的接收方式application/json;charset=UTF-8或者application/x-www-form-urlencoded;charset=UTF-8
  contentType: "application/json;charset=UTF-8",
  //消息框消失时间
  messageDuration: 3000,
  //最长请求时间
  requestTimeout: 15000,
  //操作正常code，支持String、Array、int多种类型
  successCode: [200, 0],
  //登录失效code
  invalidCode: 402,
  //无权限code
  noPermissionCode: 401,
  // 后端统一返回码，保持与 ResultEnum 一致
  resultCode: {
    // 成功
    success: 0,

    // 1xxxx 参数校验
    // 参数缺失
    paramMissing: 10001,
    // 参数格式错误
    paramInvalid: 10002,
    // 参数值非法
    paramIllegal: 10003,
    // 参数超出范围
    paramOutOfRange: 10004,
    // 请求体解析失败
    requestBodyError: 10005,
    // 参数校验未通过
    validateFailed: 10006,

    // 2xxxx 认证 / 权限
    // 未登录或登录已过期
    unauthorized: 20001,
    // 无访问权限
    forbidden: 20002,
    // Token 无效
    tokenInvalid: 20003,
    // Token 已过期
    tokenExpired: 20004,
    // 账号被禁用
    accountDisabled: 20005,
    // 账号被锁定
    accountLocked: 20006,

    // 3xxxx 用户 / 身份
    // 用户不存在
    userNotExist: 30001,
    // 用户已存在
    userAlreadyExist: 30002,
    // 密码错误
    passwordError: 30003,
    // 用户状态异常
    userStatusError: 30004,
    // 手机号已绑定
    phoneAlreadyBind: 30005,
    // 邮箱已绑定
    emailAlreadyBind: 30006,

    // 4xxxx 业务状态 / 操作冲突
    // 当前状态不允许该操作
    statusNotAllowed: 40001,
    // 数据已被修改，请刷新后重试
    dataVersionExpired: 40002,
    // 请勿重复提交
    repeatSubmit: 40003,
    // 请求过于频繁
    requestTooFrequent: 40004,
    // 幂等校验失败
    idempotentCheckFailed: 40005,

    // 5xxxx 数据 / 资源
    // 数据不存在
    dataNotExist: 50001,
    // 数据已存在
    dataAlreadyExist: 50002,
    // 数据不唯一
    dataNotUnique: 50003,
    // 数据状态异常
    dataStatusError: 50004,
    // 关联数据不存在
    relationDataNotExist: 50005,

    // 6xxxx 第三方 / 远程调用
    // 远程服务异常
    remoteServiceError: 60001,
    // 远程服务超时
    remoteServiceTimeout: 60002,
    // 远程服务返回异常
    remoteResponseError: 60003,
    // RPC 调用失败
    rpcCallFailed: 60004,
    // 第三方鉴权失败
    thirdPartyAuthFailed: 60005,

    // 9xxxx 系统异常
    // 系统异常
    systemError: 90001,
    // 数据库异常
    databaseError: 90002,
    // 缓存异常
    cacheError: 90003,
    // IO 异常
    ioError: 90004,
    // 序列化/反序列化异常
    serializeError: 90005,
    // 未知异常
    unknownError: 99999,
  },
};
module.exports = network;
