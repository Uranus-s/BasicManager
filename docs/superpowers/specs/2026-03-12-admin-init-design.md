# Admin 用户初始化接口设计

## 概述

为系统提供手动触发式的 Admin 用户初始化功能，支持创建管理员角色和完整权限菜单。

## 需求

- 手动调用接口触发初始化
- 使用配置密钥防止滥用
- 首次初始化创建完整数据：Admin用户、管理员角色、基础权限菜单

## 接口设计

### 请求

```
POST /auth/init
Content-Type: application/json
```

**请求体：**
```json
{
  "initKey": "admin-init-key",
  "adminPassword": "123456"
}
```

### 响应

**成功：**
```json
{
  "code": 200,
  "msg": "初始化成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "roleId": 1,
    "roleCode": "admin"
  }
}
```

**失败（密钥错误）：**
```json
{
  "code": 401,
  "msg": "初始化密钥错误"
}
```

**失败（已存在用户）：**
```json
{
  "code": 400,
  "msg": "系统已存在用户，无需初始化"
}
```

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `system.init-key` | `admin-init-key` | 初始化密钥 |
| `system.init-default-password` | `123456` | 默认管理员密码 |

## 初始化数据

### 1. 用户 (sys_user)

| 字段 | 值 |
|------|-----|
| username | admin |
| password | BCrypt加密后的密码 |
| nickname | 管理员 |
| status | 1 (正常) |

### 2. 角色 (sys_role)

| 字段 | 值 |
|------|-----|
| role_code | admin |
| role_name | 管理员 |
| status | 1 (正常) |
| remark | 系统超级管理员 |

### 3. 权限菜单 (sys_permission)

创建基础菜单结构：
- 系统管理 (parent_id=0)
  - 用户管理
  - 角色管理
  - 菜单管理
  - 部门管理
  - 字典管理
  - 参数配置
  - 日志管理
    - 登录日志
    - 操作日志

### 4. 关联关系

- 用户-角色：admin -> admin
- 角色-权限：admin -> 所有权限

## 安全性

1. **密钥验证**：请求必须携带正确的 initKey
2. **幂等性**：检测到已存在用户时拒绝重复初始化
3. **密码强度**：建议实际使用时使用强密码

## 实现组件

| 组件 | 路径 |
|------|------|
| API接口 | basic-api/controller/AuthApi.java |
| DTO | basic-api/dto/auth/InitAdminDTO.java |
| VO | basic-api/vo/auth/InitResultVO.java |
| Controller | basic-web/controller/AuthController.java |
| Service | basic-service/sericve/sysUser/impl/SysUserServiceImpl.java |
| 配置 | application.yml |
