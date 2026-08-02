# Basic UI

`basic-ui` 是 Basic Project 的配套后台管理前端，与仓库中的 Spring Boot 后端共同组成完整的管理系统。

前端基于 Vue 3、Rspack 和 Element Plus 开发，负责登录认证、动态菜单、权限路由、系统管理、监控看板等 Web 端交互。接口默认访问真实后端，不包含 mock 运行链路。

## 技术栈

| 分类 | 技术 |
|------|------|
| 核心框架 | Vue 3 |
| 构建工具 | Rspack |
| UI 组件 | Element Plus |
| 状态管理 | Vuex |
| 路由 | Vue Router |
| 网络请求 | Axios |
| 图表 | ECharts、Vue ECharts |
| 包管理器 | pnpm |

## 前后端关系

```text
浏览器
  -> basic-ui（开发端口 8091）
  -> Rspack 开发代理
  -> basic-web（后端端口 8080）
  -> MySQL / Redis
```

- 后端启动模块：`basic-web`
- 前端开发地址：`http://localhost:8091`
- 后端默认端口：`8080`
- 当前开发代理目标：`http://192.168.0.110:8080`
- Swagger UI：`{后端地址}/swagger-ui.html`
- OpenAPI JSON：`{后端地址}/v3/api-docs`

开发环境代理配置位于 `rspack.config.js`。后端地址发生变化时，需要同步修改代理目标并重启前端开发服务器。

## 环境要求

- Node.js 16 或更高版本
- pnpm
- Java 23
- 可用的 MySQL、Redis 和已初始化的 `basic_project` 数据库

运行前端前，建议先按照仓库根目录 `README.md` 启动后端服务。

## 快速开始

在仓库根目录执行：

```bash
cd basic-ui
pnpm install
pnpm run serve:rspack
```

启动成功后访问 `http://localhost:8091`。

## 常用命令

```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm run serve:rspack

# 生产构建，输出到 dist
pnpm run build

# 构建并生成 zip 包
pnpm run build:zip
```

当前项目未配置独立的 lint 或 test 脚本。修改 Vue 组件、路由、状态管理或构建配置后，至少运行一次 `pnpm run build`。

## 目录结构

```text
basic-ui
├─ public                 # 静态资源
├─ scripts                # 构建辅助脚本
├─ src
│  ├─ api                 # 后端接口封装
│  ├─ assets              # 图片等源码资源
│  ├─ config              # 网络、主题、权限和项目配置
│  ├─ layouts             # 后台管理布局与导航框架
│  ├─ plugins             # Vue 插件注册
│  ├─ router              # 路由配置
│  ├─ store               # Vuex 状态管理
│  ├─ styles              # 全局样式和主题
│  ├─ utils               # 请求、Token、路由等通用工具
│  └─ views               # 页面级组件
├─ tests                  # 现有测试资源
├─ package.json           # 依赖与脚本
├─ pnpm-lock.yaml         # pnpm 锁文件
├─ rspack.config.js       # Rspack 与开发代理配置
└─ rspack.js              # 构建和开发服务器入口
```

## 接口与认证

- Axios 公共实例位于 `src/utils/request.js`，页面组件不要重复创建请求实例。
- 默认 `baseURL` 为空，请求使用当前站点同源地址，开发环境由 Rspack 按接口前缀转发到后端。
- 登录成功后，请求拦截器会写入 `accessToken` 请求头和 `Authorization: Bearer <token>`。
- 认证失效、Token 过期、账号禁用或锁定等状态由公共响应逻辑统一处理。
- 修改请求路径、方法、请求体或响应结构前，直接读取运行中后端生成的 Swagger/OpenAPI 文档。
- 开发环境默认启用 Swagger UI 和 OpenAPI JSON；生产环境默认关闭。无法访问文档时，先确认后端使用开发环境配置。

## 开发约定

- 页面级功能放在 `src/views`，复用组件放在 `src/components` 或 `src/utils`。
- 后端请求统一封装在 `src/api`，不要在页面中散落接口地址。
- 登录、退出、用户信息、头像和权限菜单改动需要同时检查 API、Vuex、请求拦截器和路由守卫。
- 表格、表单、弹窗和分页优先沿用现有 Element Plus 实现。
- 文件上传使用 `FormData`，并按现有接口封装设置 `multipart/form-data`。
- 不要在源码中写入真实密码、生产密钥或私有 Token。
- 修改 `rspack.config.js` 后必须重启开发服务器，构建配置不会热更新。

## 来源与许可

本前端基于 [vue-admin-better](https://github.com/zxwk1998/vue-admin-better) 演进，并针对 Basic Project 后端接口进行了适配。许可证及原作者声明见 [LICENSE](./LICENSE)。
