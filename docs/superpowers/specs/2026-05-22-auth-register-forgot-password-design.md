# 登录认证模块注册与忘记密码设计

## 背景

当前认证模块已经支持登录、登出、在线会话管理、初始化管理员、获取当前用户信息和路由权限。用户管理模块已经支持后台新增用户、管理员重置密码、登录用户修改密码。

本次新增两个公开认证能力：

- 注册账号密码接口。
- 忘记密码后的重置密码接口。

设计目标是复用现有用户表、密码加密、认证服务和分层结构，先实现不依赖短信、邮件、验证码服务的基础可用版本。

## 范围

### 包含

- 新增 `POST /auth/register`。
- 新增 `POST /auth/forgotPassword/reset`。
- 注册时创建普通启用用户。
- 忘记密码重置时使用 `username + contact + newPassword` 校验身份，其中 `contact` 必须匹配用户手机号或邮箱。
- 重置密码成功后，使该用户当前在线会话失效。
- 将两个新增接口加入 Spring Security 匿名访问白名单。
- 为核心业务行为补充服务层单元测试。

### 不包含

- 短信验证码、邮件验证码、图片验证码或验证码发送能力。
- 注册后自动登录。
- 注册时分配角色、部门或后台管理权限。
- 找回密码令牌、重置链接或限流风控。

## 接口设计

### 注册接口

路径：`POST /auth/register`

请求字段：

- `username`：必填，3 到 20 个字符。
- `password`：必填，6 到 20 个字符。
- `nickname`：选填，最多 50 个字符。
- `phone`：选填，使用现有手机号校验规则。
- `email`：选填，使用邮箱格式校验。
- `avatar`：选填。

处理规则：

1. 校验用户名不能重复。
2. 如填写手机号，校验手机号不能被其他用户绑定。
3. 如填写邮箱，校验邮箱不能被其他用户绑定。
4. 使用现有 `PasswordEncoder` 加密密码。
5. 创建 `SysUser`，默认 `status = 1`。
6. 不写入用户角色、用户部门关联。

返回：统一 `Result` 成功响应，可返回注册用户 ID，便于前端后续提示或跳转。

### 忘记密码重置接口

路径：`POST /auth/forgotPassword/reset`

请求字段：

- `username`：必填，3 到 20 个字符。
- `contact`：必填，用户已绑定的手机号或邮箱。
- `newPassword`：必填，6 到 20 个字符。

处理规则：

1. 根据用户名查询用户，用户不存在时返回用户不存在。
2. 将 `contact` 与用户手机号、邮箱做精确匹配。
3. 如果两者都不匹配，返回参数非法或用户状态类业务错误。
4. 使用现有 `PasswordEncoder` 加密新密码并更新用户。
5. 调用现有在线会话能力，强制该用户下线，避免旧 Token 继续使用。

返回：统一 `Result.success()`。

## 分层设计

### basic-api

- 新增 `RegisterDTO`。
- 新增 `ForgotPasswordResetDTO`。
- 在 `AuthApi` 中声明注册和忘记密码重置接口。

DTO 命名放在 `com.basic.api.dto.auth` 包内，保持认证请求模型与用户管理请求模型分离，避免把后台用户新增字段暴露给公开注册入口。

### basic-web

- `AuthController` 实现 `AuthApi` 中新增的方法。
- Controller 只做参数接收、`@Valid` 校验和统一响应包装。
- 不在 Controller 中写用户名唯一性、密码加密或身份校验逻辑。

### basic-service

- `IAuthService` 增加注册和忘记密码重置方法。
- `AuthServiceImpl` 负责编排注册、联系人校验、密码加密、用户保存和会话失效。
- 用户查询与保存继续复用 `ISysUserService`。

### basic-core

- `SecurityConfig` 将 `/auth/register` 与 `/auth/forgotPassword/reset` 加入匿名访问白名单。
- 不新增过滤器或认证处理器。

## 错误处理

- 用户名重复：`ResultEnum.USER_ALREADY_EXIST`。
- 手机号重复：`ResultEnum.PHONE_ALREADY_BIND`。
- 邮箱重复：`ResultEnum.EMAIL_ALREADY_BIND`。
- 用户不存在：`ResultEnum.USER_NOT_EXIST`。
- 联系方式不匹配：`ResultEnum.PARAM_ILLEGAL`。

以上错误通过现有 `BusinessException` 和全局异常处理返回。

## 数据流

注册：

`RegisterDTO -> AuthController -> IAuthService.register -> SysUser -> sys_user`

忘记密码重置：

`ForgotPasswordResetDTO -> AuthController -> IAuthService.resetForgottenPassword -> SysUser -> sys_user -> AuthTokenService.forceLogout`

## 测试策略

优先添加服务层单元测试，按 TDD 流程先写失败测试，再实现生产代码。

测试覆盖：

- 注册成功时保存启用用户，并加密密码。
- 注册用户名重复时抛出 `USER_ALREADY_EXIST`。
- 注册手机号重复时抛出 `PHONE_ALREADY_BIND`。
- 注册邮箱重复时抛出 `EMAIL_ALREADY_BIND`。
- 忘记密码重置时，联系人不匹配会失败。
- 忘记密码重置成功时更新加密密码，并强制用户下线。

完成后运行相关 Maven 测试，并至少执行一次相关模块编译。
