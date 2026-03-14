# 用户路由权限接口设计

## 1. 需求概述

为前端提供独立接口，获取当前登录用户拥有的路由权限，用于动态生成侧边栏菜单。

## 2. 接口设计

### 2.1 接口信息

| 项目 | 说明 |
|------|------|
| URL | GET /auth/routes |
| 认证 | 需要登录（JWT Token） |
| 频率 | 登录后调用一次，路由变化时可刷新 |

### 2.2 响应数据结构

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "path": "/system",
      "component": "Layout",
      "icon": "Setting",
      "type": "MENU",
      "sort": 1,
      "visible": 1,
      "status": 1,
      "children": [
        {
          "id": 10,
          "name": "用户管理",
          "path": "/system/user",
          "component": "system/user/index",
          "icon": "User",
          "type": "MENU",
          "permission": "sys:user:list",
          "sort": 1,
          "visible": 1,
          "status": 1,
          "children": [
            {
              "id": 101,
              "name": "新增用户",
              "type": "BUTTON",
              "permission": "sys:user:add",
              "sort": 1,
              "visible": 1,
              "status": 1
            },
            {
              "id": 102,
              "name": "编辑用户",
              "type": "BUTTON",
              "permission": "sys:user:edit",
              "sort": 2,
              "visible": 1,
              "status": 1
            }
          ]
        }
      ]
    }
  ]
}
```

### 2.3 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 权限ID |
| name | String | 名称 |
| path | String | 路由路径 |
| component | String | 前端组件路径 |
| icon | String | 图标 |
| type | String | 类型：MENU/BUTTON/API |
| permission | String | 权限标识 |
| sort | Integer | 排序 |
| visible | Byte | 是否显示：0=隐藏 1=显示 |
| status | Byte | 状态：0=禁用 1=正常 |
| children | List | 子权限列表 |

## 3. 核心设计

### 3.1 数据来源

根据当前登录用户的角色，查询关联的权限，构建树形结构返回。

### 3.2 树形构建逻辑

1. 根据用户ID查询关联的角色
2. 根据角色查询关联的权限列表
3. 过滤出所有 MENU 类型的权限作为根节点
4. 按 parentId 递归构建父子关系
5. BUTTON 类型挂载到对应 MENU 的 children
6. API 类型同样挂载到对应 MENU 的 children
7. 按 sort 字段排序

### 3.3 模块设计

**basic-api 模块：**
- 新建 `RouteVO` - 路由权限响应类

**basic-service 模块：**
- `ISysUserService` 新增 `getUserRoutes()` 方法
- `SysUserServiceImpl` 实现查询和树形构建逻辑

**basic-web 模块：**
- `AuthApi` 新增 `getUserRoutes()` 接口声明
- `AuthController` 实现 `/auth/routes` 接口

## 4. 错误处理

| 场景 | 返回码 | 消息 |
|------|--------|------|
| 未登录 | 401 | 用户未登录 |
| 用户不存在 | 404 | 用户不存在 |

## 5. 安全性

- 接口需要携带有效 JWT Token
- 只返回当前用户拥有的权限
- 不返回已禁用的权限（status=0）
- 不返回隐藏的菜单（visible=0）
