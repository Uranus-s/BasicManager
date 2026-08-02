# AGENTS.md

本文件为在 `basic-ui` 项目中工作的编码代理提供项目约定和操作指引。文档输出优先使用中文。

## 项目概览

- 这是一个 Vue 3 后台管理项目，使用 Rspack、Element Plus、Vuex、Vue Router 和 Axios。
- 包管理器以 `pnpm-lock.yaml` 为准，优先使用 `pnpm` 执行项目脚本。
- 业务源码位于 `src/`。
- 后端接口文档由运行中的 Spring Boot 服务动态生成，Swagger UI 位于 `{后端地址}/swagger-ui.html`，OpenAPI JSON 位于 `{后端地址}/v3/api-docs`。
- 开发服务器配置位于 `rspack.config.js`，启动入口是 `rspack.js`。
- 当前项目已移除 mock 运行链路，接口默认访问真实后端。

## 目录职责

- `src/api/`：接口封装。新增或修改后端请求时优先在这里处理。
- `src/api/system/`：系统管理相关接口。
- `src/utils/request.js`：Axios 实例、请求/响应拦截、错误处理、重试、Token 注入等公共请求逻辑。
- `src/store/`：Vuex store，认证态主要在 `src/store/modules/user.js`。
- `src/router/`：路由配置和路由守卫相关逻辑。
- `src/views/`：页面级 Vue 单文件组件。
- `src/components/`：项目内复用组件。
- `src/layouts/`：后台管理布局与导航框架。
- `src/config/`：项目配置，网络配置位于 `src/config/net.config.js`。
- `src/styles/`：全局样式、主题变量和 Element Plus 样式覆盖。
- `public/`：静态资源目录。
- `layouts/`：本地 file 依赖，对应 `package.json` 中的 `layouts: file:layouts`。

## 常用命令

- 安装依赖：`pnpm install`
- 启动开发服务器：`pnpm run serve:rspack`
- 构建项目：`pnpm run build`
- 构建并打包 zip：`pnpm run build:zip`
- 检查 JavaScript 文件语法：`node --check <file>`
- 查看 Git 变更：`git status --short`

当前 `package.json` 中没有专门的 lint 或 test 脚本。不要声称已运行 lint/test，除非实际执行了对应命令。

## 本地服务与代理

- 默认开发地址为 `http://localhost:8091`。
- 开发端口来自 `rspack.config.js` 中的 `devServer.port`，当前默认 `8091`。
- 本地 API 前缀通过 `rspack.config.js` 的 `setupMiddlewares` 转发到真实后端。
- 当前代理目标在 `rspack.config.js` 中配置为 `192.168.0.110:8080`。
- 修改 `rspack.config.js` 后必须重启 `pnpm run serve:rspack`，开发服务器配置不会热更新。
- 如果验证接口，请确认响应确实来自真实后端，而不是浏览器缓存、前端静态文件或旧开发服务器。

## 接口规则

- 默认 `baseURL` 为 `""`，走同源真实后端接口。
- 修改接口时，优先修改 `src/api/` 中对应的接口封装。
- 修改接口路径、请求方法、请求体或响应处理前，直接读取运行中后端的 Swagger/OpenAPI 文档。
- 开发环境默认启用 Swagger/OpenAPI，生产环境默认关闭；文档不可访问时先确认后端使用开发环境配置。
- 服务端接口登录后使用 `Authorization: Bearer token` 获取数据。
- 测试需要认证的接口时，可以使用 `dev-master-token` 作为超级 Token，请求头写为 `Authorization: Bearer dev-master-token`。
- 公共请求逻辑优先复用 `src/utils/request.js`，不要在页面组件中重复创建 Axios 实例。
- 不要恢复 mock 逻辑，不要新增 mock 依赖或 mock 运行链路，除非用户明确要求。
- 小型代理或请求改动不要轻易新增依赖。

## 当前认证相关说明

- `src/api/user.js` 中 `/auth/*` 接口使用 `baseURL: ""`。
- `src/store/modules/user.js` 登录逻辑兼容 `data.accessToken` 和 `data.token`。
- `src/utils/request.js` 会在存在 `store.state.user.accessToken` 时同时写入 `tokenName` 对应请求头和 `Authorization: Bearer <token>`。
- 认证失效、Token 过期、账号禁用/锁定等状态由 `src/utils/request.js` 统一处理并跳转登录页。
- 修改登录、退出、用户信息、头像上传、权限菜单等认证相关流程时，同时检查 `src/api/user.js`、`src/store/modules/user.js`、`src/utils/request.js` 和路由守卫。

## 编码约定

- 遵循项目现有的 Vue 单文件组件、Vuex、Vue Router 和 API 封装写法。
- 修改范围保持聚焦，不做无关重构。
- 避免无关格式化变更。
- 不要覆盖用户已有改动；编辑前后使用 `git status --short` 确认变更范围。
- 代码风格遵循 `.editorconfig`：UTF-8、LF、2 空格缩进、文件末尾换行。
- JavaScript 字符串风格以所在文件现有写法为准，不为统一风格做无关改动。
- Vue 单文件组件中优先沿用已有结构、命名和 Element Plus 组件使用方式。
- 新增页面或业务模块时，尽量同步考虑对应的 `src/api/` 封装、路由、权限和 store 状态边界。
- 不要把真实后端地址、账号、密码、Token 等敏感信息硬编码进业务代码。

## 前端实现注意事项

- 页面级功能放在 `src/views/`，跨页面复用能力放在 `src/components/` 或 `src/utils/`。
- 表格、表单、弹窗、分页等交互优先沿用项目已有 Element Plus 写法。
- 请求状态、错误提示、权限失效等优先依赖公共请求拦截器，页面内只处理业务必要状态。
- 上传文件时使用 `FormData`，并按现有接口封装方式设置 `multipart/form-data`。
- 涉及时间字段时注意 `src/utils/request.js` 会将形如 `YYYY-MM-DDTHH:mm:ss` 的字符串规范为 `YYYY-MM-DD HH:mm:ss`。

## 验证要求

接口封装变更后：

- 对修改过的 JavaScript 文件运行 `node --check <file>`。
- 如果修改了 Vue 单文件组件，至少运行一次构建或通过开发服务器做页面验证；仅 `node --check` 不能检查 `.vue` 文件。
- 如果开发服务器正在运行，通过 `http://localhost:8091` 验证接口。
- 测试登录后接口时，可使用 `Authorization: Bearer dev-master-token` 直接验证真实后端响应。
- 明确确认接口响应来自真实后端后，再说明结果。

影响构建配置、公共请求逻辑、路由、store 或布局的变更后：

- 视情况运行 `pnpm run build`。
- 注意：`pnpm run build` 会通过脚本删除 `dist`。
- 如果只改文档，可以不运行构建，但应说明未运行的原因。

## Git 说明

- 当前项目已在本地初始化为 Git 仓库。
- 提交前运行 `git status --short` 查看变更范围。
- 不准代理擅自执行 `git commit` 提交代码；所有提交必须先由用户审核并明确确认后再执行。
- 提交尽量保持聚焦，避免把无关改动混在一起。
- 不要使用 `git reset --hard`、`git checkout -- <file>` 等会丢弃用户改动的命令，除非用户明确要求。
- 如果工作区已有与当前任务无关的修改，保留并绕开它们。
