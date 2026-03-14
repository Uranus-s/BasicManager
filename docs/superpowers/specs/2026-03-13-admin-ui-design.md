# 后台管理系统 Web 页面设计

## 1. 项目概述

- **项目名称**: basic-ui
- **项目类型**: 企业后台管理系统 Web 前端
- **核心功能**: 基于 Spring Boot 后端的管理系统，提供用户、角色、部门、菜单、字典、日志等管理功能
- **目标用户**: 企业内部管理人员

## 2. 技术栈

| 层级 | 选择 | 说明 |
|------|------|------|
| 框架 | Vue 3 + Element Plus | 主流企业级 UI 框架 |
| 构建工具 | Vite | 快速开发体验 |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| HTTP 请求 | Axios | 企业级请求封装 |
| 样式 | SCSS | 支持嵌套和变量 |
| 国际化 | vue-i18n | 中英文支持 |
| API 生成 | openapi-typescript | 基于 openapi.json 自动生成 |
| 主题 | 亮色 + 暗色 | 支持主题切换 |

## 3. 项目结构

```
basic-ui/
├── public/                    # 静态资源
├── src/
│   ├── api/                   # API 自动生成
│   ├── assets/                # 静态资源
│   ├── components/            # 公共组件
│   │   ├── Layout/            # 布局组件
│   │   │   ├── AppLayout.vue  # 主布局
│   │   │   ├── AppHeader.vue  # 顶部Header
│   │   │   ├── AppSidebar.vue # 侧边栏菜单
│   │   │   └── AppBreadcrumb.vue
│   │   ├── Common/            # 通用组件
│   │   │   ├── SearchForm.vue
│   │   │   ├── DataTable.vue
│   │   │   ├── FormDialog.vue
│   │   │   └── StatusTag.vue
│   │   └── Icons/             # 图标组件
│   ├── composables/           # 组合式函数
│   ├── layouts/               # 页面布局
│   ├── locales/               # 国际化语言文件
│   │   ├── zh-CN.json
│   │   └── en-US.json
│   ├── router/                # 路由配置
│   │   ├── index.ts
│   │   ├── static.ts
│   │   └── dynamic.ts
│   ├── stores/                # Pinia stores
│   │   ├── user.ts
│   │   ├── menu.ts
│   │   ├── theme.ts
│   │   └── settings.ts
│   ├── styles/                # 全局样式
│   │   ├── variables.scss
│   │   └── mixins.scss
│   ├── types/                 # TypeScript 类型定义
│   ├── utils/                 # 工具函数
│   │   ├── request.ts         # Axios 封装
│   │   ├── storage.ts
│   │   └── auth.ts
│   ├── views/                 # 页面视图
│   │   ├── login/
│   │   ├── error/
│   │   └── system/
│   │       ├── user/
│   │       ├── role/
│   │       ├── dept/
│   │       ├── menu/
│   │       ├── dict/
│   │       ├── config/
│   │       └── log/
│   ├── App.vue
│   └── main.ts
├── package.json
├── vite.config.ts
├── tsconfig.json
└── openapi.json
```

## 4. 核心组件设计

### 4.1 布局组件

- **AppLayout**: 主布局容器，响应式设计
  - 屏幕宽度 < 768px 时自动折叠侧边栏
  - Header 高度 60px
  - Sidebar 宽度 220px（可折叠至 64px）

- **AppHeader**: 顶部导航
  - Logo 和系统名称
  - 面包屑导航
  - 用户信息下拉
  - 主题切换按钮
  - 语言切换按钮

- **AppSidebar**: 侧边栏菜单
  - 动态菜单渲染
  - 支持多级菜单
  - 折叠/展开功能

### 4.2 通用组件封装

| 组件 | 功能 |
|------|------|
| SearchForm | 通用搜索表单，支持动态配置表单项 |
| DataTable | 通用表格组件，支持分页、排序、选择 |
| FormDialog | 通用表单对话框，新增/编辑复用 |
| StatusTag | 状态标签，显示启用/禁用等状态 |

## 5. 页面模块

根据 openapi.json 设计以下模块：

| 模块 | 路径 | 功能 |
|------|------|------|
| 登录 | /login | 登录、退出 |
| 用户管理 | /system/user | 增删改查、分配角色 |
| 角色管理 | /system/role | 增删改查、分配权限 |
| 部门管理 | /system/dept | 树形结构管理 |
| 菜单管理 | /system/menu | 菜单配置 |
| 字典管理 | /system/dict | 字典配置 |
| 字典项 | /system/dict/item | 字典项配置 |
| 参数配置 | /system/config | 系统参数 |
| 操作日志 | /system/log/oper | 操作记录 |
| 登录日志 | /system/log/login | 登录记录 |
| 文件管理 | /system/file | 文件上传/下载 |

## 6. 设计原则

1. **模块化开发**: 各模块独立，便于维护和扩展
2. **组件封装**: 通用功能抽取为公共组件，减少重复代码
3. **响应式布局**: 适配不同屏幕尺寸
4. **国际化**: 中英文切换
5. **类型安全**: TypeScript + 自动生成 API 类型
