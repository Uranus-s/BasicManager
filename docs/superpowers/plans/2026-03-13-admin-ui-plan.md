<parameter name="content"># Vue 3 + Element Plus 后台管理系统实施计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建 Vue 3 + Element Plus 后台管理系统 Web 界面

**Architecture:** 使用 Vite 构建工具，基于 Vue 3 + TypeScript + Element Plus 组件库，实现响应式布局、国际化、动态路由

**Tech Stack:** Vue 3, TypeScript, Vite, Element Plus, Pinia, Axios, vue-i18n, SCSS

---

## Chunk 1: 项目初始化

### Task 1: 创建 Vite + Vue 3 项目

**Files:**
- Create: `package.json`
- Create: `vite.config.ts`
- Create: `tsconfig.json`
- Create: `index.html`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "basic-ui",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.6.0",
    "vue-i18n": "^9.9.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0",
    "typescript": "^5.3.0",
    "vue-tsc": "^1.8.0",
    "sass": "^1.69.0",
    "unplugin-vue-components": "^0.26.0",
    "unplugin-auto-import": "^0.17.0"
  }
}
```

- [ ] **Step 2: 创建 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

- [ ] **Step 3: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 4: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Basic Admin</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

- [ ] **Step 5: 安装依赖**

```bash
cd /mnt/g/Java/BasicProject/basic-ui
npm install
```

- [ ] **Step 6: Commit**

```bash
git add package.json vite.config.ts tsconfig.json index.html
git commit -m "feat(ui): 初始化 Vite + Vue 3 项目"
```

---

## Chunk 2: 基础架构

### Task 2: 入口文件和基础配置

**Files:**
- Create: `src/main.ts`
- Create: `src/App.vue`
- Create: `src/env.d.ts`

- [ ] **Step 1: 创建 src/env.d.ts**

```typescript
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
```

- [ ] **Step 2: 创建 src/main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import i18n from './locales'
import 'element-plus/dist/index.css'
import '@/styles/index.scss'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.use(i18n)

app.mount('#app')
```

- [ ] **Step 3: 创建 src/App.vue**

```vue
<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add src/main.ts src/App.vue src/env.d.ts
git commit -m "feat(ui): 添加入口文件和基础配置"
```

---

### Task 3: 工具函数封装

**Files:**
- Create: `src/utils/request.ts`
- Create: `src/utils/storage.ts`
- Create: `src/utils/auth.ts`

- [ ] **Step 1: 创建 src/utils/request.ts**

```typescript
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data

    if (code === 200 || code === undefined) {
      return data
    }

    if (code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(message || '登录已过期'))
    }

    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error: AxiosError) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
```

- [ ] **Step 2: 创建 src/utils/storage.ts**

```typescript
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const THEME_KEY = 'theme'
const LANGUAGE_KEY = 'language'

export const storage = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  setToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token)
  },

  removeToken() {
    localStorage.removeItem(TOKEN_KEY)
  },

  getUserInfo() {
    const info = localStorage.getItem(USER_INFO_KEY)
    return info ? JSON.parse(info) : null
  },

  setUserInfo(info: any) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  },

  removeUserInfo() {
    localStorage.removeItem(USER_INFO_KEY)
  },

  getTheme() {
    return localStorage.getItem(THEME_KEY) || 'light'
  },

  setTheme(theme: string) {
    localStorage.setItem(THEME_KEY, theme)
  },

  getLanguage() {
    return localStorage.getItem(LANGUAGE_KEY) || 'zh-CN'
  },

  setLanguage(lang: string) {
    localStorage.setItem(LANGUAGE_KEY, lang)
  }
}

export default storage
```

- [ ] **Step 3: 创建 src/utils/auth.ts**

```typescript
import { storage } from './storage'

export function getToken() {
  return storage.getToken()
}

export function setToken(token: string) {
  storage.setToken(token)
}

export function removeToken() {
  storage.removeToken()
}

export function getUserInfo() {
  return storage.getUserInfo()
}

export function setUserInfo(info: any) {
  storage.setUserInfo(info)
}

export function removeUserInfo() {
  storage.removeUserInfo()
}

export default {
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo
}
```

- [ ] **Step 4: Commit**

```bash
git add src/utils/
git commit -m "feat(ui): 添加工具函数封装"
```

---

### Task 4: 路由配置

**Files:**
- Create: `src/router/index.ts`
- Create: `src/router/static.ts`
- Create: `src/router/dynamic.ts`

- [ ] **Step 1: 创建 src/router/static.ts**

```typescript
import type { RouteRecordRaw } from 'vue-router'

const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

export default staticRoutes
```

- [ ] **Step 2: 创建 src/router/dynamic.ts**

```typescript
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout/AppLayout.vue'

// 动态路由表
export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Document' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '参数配置', icon: 'Tools' }
      }
    ]
  }
]

export default dynamicRoutes
```

- [ ] **Step 3: 创建 src/router/index.ts**

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import staticRoutes from './static'
import dynamicRoutes from './dynamic'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [...staticRoutes]
})

// 路由白名单
const whiteList = ['/login', '/404']

let isRoutesAdded = false

export async function setupRoutes() {
  const token = getToken()
  const userStore = useUserStore()

  if (token && !userStore.userInfo) {
    try {
      // 获取用户信息
      await userStore.getUserInfoAction()
    } catch (error) {
      userStore.logout()
    }
  }

  if (!isRoutesAdded) {
    // 添加动态路由
    dynamicRoutes.forEach(route => {
      router.addRoute(route)
    })
    isRoutesAdded = true
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next('/')
    } else {
      await setupRoutes()
      next({ ...to, replace: true })
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
```

- [ ] **Step 4: Commit**

```bash
git add src/router/
git commit -m "feat(ui): 添加路由配置"
```

---

### Task 5: 状态管理 (Pinia)

**Files:**
- Create: `src/stores/user.ts`
- Create: `src/stores/theme.ts`

- [ ] **Step 1: 创建 src/stores/user.ts**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { setToken, removeToken, setUserInfo, removeUserInfo } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<any>(null)

  async function login(data: { username: string; password: string }) {
    const res = await loginApi(data)
    token.value = res.token
    setToken(res.token)
    return res
  }

  async function getUserInfoAction() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    setUserInfo(res.data)
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      // 忽略错误
    } finally {
      token.value = ''
      userInfo.value = null
      removeToken()
      removeUserInfo()
      router.push('/login')
    }
  }

  return {
    token,
    userInfo,
    login,
    getUserInfoAction,
    logout
  }
})
```

- [ ] **Step 2: 创建 src/stores/theme.ts**

```typescript
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { storage } from '@/utils/storage'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(storage.getTheme())
  const language = ref(storage.getLanguage())

  function setTheme(newTheme: string) {
    theme.value = newTheme
    storage.setTheme(newTheme)
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  function setLanguage(lang: string) {
    language.value = lang
    storage.setLanguage(lang)
  }

  // 初始化主题
  function initTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return {
    theme,
    language,
    setTheme,
    setLanguage,
    initTheme
  }
})
```

- [ ] **Step 3: Commit**

```bash
git add src/stores/
git commit -m "feat(ui): 添加 Pinia 状态管理"
```

---

### Task 6: 国际化配置

**Files:**
- Create: `src/locales/index.ts`
- Create: `src/locales/zh-CN.json`
- Create: `src/locales/en-US.json`

- [ ] **Step 1: 创建 src/locales/zh-CN.json**

```json
{
  "common": {
    "add": "新增",
    "edit": "编辑",
    "delete": "删除",
    "save": "保存",
    "cancel": "取消",
    "confirm": "确认",
    "search": "搜索",
    "reset": "重置",
    "export": "导出",
    "import": "导入",
    "submit": "提交",
    "operation": "操作"
  },
  "login": {
    "title": "系统登录",
    "username": "请输入用户名",
    "password": "请输入密码",
    "loginBtn": "登录"
  },
  "user": {
    "addUser": "新增用户",
    "editUser": "编辑用户"
  },
  "role": {
    "addRole": "新增角色"
  },
  "dept": {
    "addDept": "新增部门"
  },
  "dict": {
    "addDict": "新增字典"
  }
}
```

- [ ] **Step 2: 创建 src/locales/en-US.json**

```json
{
  "common": {
    "add": "Add",
    "edit": "Edit",
    "delete": "Delete",
    "save": "Save",
    "cancel": "Cancel",
    "confirm": "Confirm",
    "search": "Search",
    "reset": "Reset",
    "export": "Export",
    "import": "Import",
    "submit": "Submit",
    "operation": "Operation"
  },
  "login": {
    "title": "System Login",
    "username": "Please enter username",
    "password": "Please enter password",
    "loginBtn": "Login"
  },
  "user": {
    "addUser": "Add User",
    "editUser": "Edit User"
  },
  "role": {
    "addRole": "Add Role"
  },
  "dept": {
    "addDept": "Add Department"
  },
  "dict": {
    "addDict": "Add Dict"
  }
}
```

- [ ] **Step 3: 创建 src/locales/index.ts**

```typescript
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.json'
import enUS from './en-US.json'
import { storage } from '@/utils/storage'

const i18n = createI18n({
  legacy: false,
  locale: storage.getLanguage(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export default i18n
```

- [ ] **Step 4: Commit**

```bash
git add src/locales/
git commit -m "feat(ui): 添加国际化配置"
```

---

### Task 7: 全局样式

**Files:**
- Create: `src/styles/variables.scss`
- Create: `src/styles/mixins.scss`
- Create: `src/styles/index.scss`

- [ ] **Step 1: 创建 src/styles/variables.scss**

```scss
// 主题色
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;

// 布局
$header-height: 60px;
$sidebar-width: 220px;
$sidebar-collapsed-width: 64px;

// 颜色
$bg-color: #f5f7fa;
$border-color: #e4e7ed;
```

- [ ] **Step 2: 创建 src/styles/mixins.scss**

```scss
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
```

- [ ] **Step 3: 创建 src/styles/index.scss**

```scss
@import './variables.scss';
@import './mixins.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
}

// 暗色主题
[data-theme='dark'] {
  --bg-color: #1a1a1a;
  --text-color: #e5e5e5;
  --border-color: #404040;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/styles/
git commit -m "feat(ui): 添加全局样式"
```

---

## Chunk 3: 布局组件

### Task 8: 布局组件开发

**Files:**
- Create: `src/components/Layout/AppLayout.vue`
- Create: `src/components/Layout/AppHeader.vue`
- Create: `src/components/Layout/AppSidebar.vue`

- [ ] **Step 1: 创建 src/components/Layout/AppLayout.vue**

```vue
<template>
  <el-container class="app-layout">
    <el-aside :width="sidebarWidth" class="sidebar">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="header">
        <AppHeader />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

const settingsStore = useSettingsStore()

const sidebarWidth = computed(() =>
  settingsStore.sidebarCollapsed ? '64px' : '220px'
)
</script>

<style scoped lang="scss">
.app-layout {
  width: 100%;
  height: 100%;

  .sidebar {
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;
  }

  .header {
    background-color: white;
    border-bottom: 1px solid #e4e7ed;
    padding: 0 20px;
  }

  .main-content {
    background-color: #f5f7fa;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>
```

- [ ] **Step 2: 创建 src/components/Layout/AppHeader.vue**

```vue
<template>
  <div class="app-header">
    <div class="header-left">
      <el-icon class="fold-icon" @click="toggleSidebar">
        <Fold v-if="!settingsStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" :src="userStore.userInfo?.avatar || defaultAvatar" />
          <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">系统设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSettingsStore } from '@/stores/settings'

const route = useRoute()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

function toggleSidebar() {
  settingsStore.toggleSidebar()
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .fold-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
      }
    }
  }
}
</style>
```

- [ ] **Step 3: 创建 src/components/Layout/AppSidebar.vue**

```vue
<template>
  <div class="app-sidebar">
    <div class="logo">
      <img v-if="!collapsed" src="@/assets/logo.png" alt="logo" class="logo-img" />
      <span v-if="!collapsed" class="logo-title">Basic Admin</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/home">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>
      <el-sub-menu index="/system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">用户管理</el-menu-item>
        <el-menu-item index="/system/role">角色管理</el-menu-item>
        <el-menu-item index="/system/dept">部门管理</el-menu-item>
        <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        <el-menu-item index="/system/dict">字典管理</el-menu-item>
        <el-menu-item index="/system/config">参数配置</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useSettingsStore } from '@/stores/settings'

const route = useRoute()
const settingsStore = useSettingsStore()

const collapsed = computed(() => settingsStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.app-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3a4a;

    .logo-img {
      width: 32px;
      height: 32px;
    }

    .logo-title {
      color: white;
      font-size: 16px;
      font-weight: bold;
      margin-left: 8px;
    }
  }

  .el-menu {
    border-right: none;
    flex: 1;
    overflow-y: auto;
  }
}
</style>
```

- [ ] **Step 4: 创建 src/stores/settings.ts**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const sidebarCollapsed = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    sidebarCollapsed,
    toggleSidebar
  }
})
```

- [ ] **Step 5: Commit**

```bash
git add src/components/Layout/ src/stores/settings.ts
git commit -m "feat(ui): 添加布局组件"
```

---

## Chunk 4: 通用组件

### Task 9: 通用组件封装

**Files:**
- Create: `src/components/Common/SearchForm.vue`
- Create: `src/components/Common/DataTable.vue`
- Create: `src/components/Common/FormDialog.vue`
- Create: `src/components/Common/StatusTag.vue`

- [ ] **Step 1: 创建 src/components/Common/SearchForm.vue**

```vue
<template>
  <el-form :inline="true" :model="formData" class="search-form">
    <el-form-item
      v-for="item in schema"
      :key="item.prop"
      :label="item.label"
    >
      <el-input
        v-if="item.type === 'input'"
        v-model="formData[item.prop]"
        :placeholder="item.placeholder"
        clearable
      />
      <el-select
        v-else-if="item.type === 'select'"
        v-model="formData[item.prop]"
        :placeholder="item.placeholder"
        clearable
      >
        <el-option
          v-for="option in item.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-date-picker
        v-else-if="item.type === 'date'"
        v-model="formData[item.prop]"
        type="date"
        :placeholder="item.placeholder"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

interface SchemaItem {
  prop: string
  label: string
  type: 'input' | 'select' | 'date'
  placeholder?: string
  options?: { label: string; value: any }[]
}

interface Props {
  schema: SchemaItem[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'search', data: any): void
  (e: 'reset'): void
}>()

const formData = reactive<Record<string, any>>({})

function handleSearch() {
  emit('search', { ...formData })
}

function handleReset() {
  Object.keys(formData).forEach(key => {
    formData[key] = undefined
  })
  emit('reset')
}
</script>

<style scoped lang="scss">
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: white;
  border-radius: 4px;
}
</style>
```

- [ ] **Step 2: 创建 src/components/Common/DataTable.vue**

```vue
<template>
  <div class="data-table">
    <el-table
      v-loading="loading"
      :data="data"
      stripe
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="showSelection" type="selection" width="55" />
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :align="col.align || 'left'"
      >
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" :row="scope.row" />
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="showPagination"
      class="pagination"
      :current-page="page"
      :page-size="limit"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup lang="ts">
interface Column {
  prop: string
  label: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  slot?: string
}

interface Props {
  data: any[]
  columns: Column[]
  loading?: boolean
  total?: number
  page?: number
  limit?: number
  showSelection?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  total: 0,
  page: 1,
  limit: 10,
  showSelection: false
})

const emit = defineEmits<{
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
  (e: 'selection-change', selection: any[]): void
}>()

const showPagination = props.total > 0

function handlePageChange(page: number) {
  emit('page-change', page)
}

function handleSizeChange(size: number) {
  emit('size-change', size)
}

function handleSelectionChange(selection: any[]) {
  emit('selection-change', selection)
}
</script>

<style scoped lang="scss">
.data-table {
  background: white;
  padding: 16px;
  border-radius: 4px;

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
```

- [ ] **Step 3: 创建 src/components/Common/FormDialog.vue**

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item
        v-for="item in schema"
        :key="item.prop"
        :label="item.label"
        :prop="item.prop"
      >
        <el-input
          v-if="item.type === 'input'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder"
        />
        <el-select
          v-else-if="item.type === 'select'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder"
        >
          <el-option
            v-for="option in item.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-switch
          v-else-if="item.type === 'switch'"
          v-model="formData[item.prop]"
          :active-value="item.activeValue"
          :inactive-value="item.inactiveValue"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface SchemaItem {
  prop: string
  label: string
  type: 'input' | 'select' | 'switch'
  placeholder?: string
  options?: { label: string; value: any }[]
  activeValue?: any
  inactiveValue?: any
}

interface Props {
  modelValue: boolean
  title: string
  schema: SchemaItem[]
  defaultData?: Record<string, any>
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: any): void
}>()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()
const formData = ref<Record<string, any>>({})
const rules: FormRules = {}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultData) {
    formData.value = { ...props.defaultData }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
  formData.value = {}
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (valid) {
      emit('submit', { ...formData.value })
    }
  })
}
</script>
```

- [ ] **Step 4: 创建 src/components/Common/StatusTag.vue**

```vue
<template>
  <el-tag :type="tagType" :effect="effect">
    {{ text }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: string | number | boolean
  type?: 'success' | 'warning' | 'danger' | 'info' | ''
  activeValue?: any
  inactiveValue?: any
  activeText?: string
  inactiveText?: string
  effect?: 'light' | 'dark' | 'plain'
}

const props = withDefaults(defineProps<Props>(), {
  type: '',
  effect: 'light'
})

const tagType = computed(() => {
  if (props.type) return props.type

  const isActive = props.status === props.activeValue ||
    (props.status === true || props.status === 1 || props.status === '1')

  return isActive ? 'success' : 'danger'
})

const text = computed(() => {
  if (props.status === props.activeValue ||
      props.status === true || props.status === 1 || props.status === '1') {
    return props.activeText || '启用'
  }
  return props.inactiveText || '禁用'
})
</script>
```

- [ ] **Step 5: Commit**

```bash
git add src/components/Common/
git commit -m "feat(ui): 添加通用组件"
```

---

## Chunk 5: 页面视图

### Task 10: 登录页和首页

**Files:**
- Create: `src/views/login/index.vue`
- Create: `src/views/home/index.vue`
- Create: `src/views/error/404.vue`

- [ ] **Step 1: 创建 src/views/login/index.vue**

```vue
<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>{{ t('login.title') }}</h2>
      </div>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="t('login.username')"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="t('login.password')"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ t('login.loginBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    font-size: 24px;
    color: #333;
  }
}

.login-form {
  .login-button {
    width: 100%;
  }
}
</style>
```

- [ ] **Step 2: 创建 src/views/home/index.vue**

```vue
<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">用户总数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
            </div>
            <el-icon class="stat-icon" color="#409EFF"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">角色总数</div>
              <div class="stat-value">{{ stats.roleCount }}</div>
            </div>
            <el-icon class="stat-icon" color="#67C23A"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">部门总数</div>
              <div class="stat-value">{{ stats.deptCount }}</div>
            </div>
            <el-icon class="stat-icon" color="#E6A23C"><OfficeBuilding /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">菜单总数</div>
              <div class="stat-value">{{ stats.menuCount }}</div>
            </div>
            <el-icon class="stat-icon" color="#F56C6C"><Menu /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const stats = reactive({
  userCount: 0,
  roleCount: 0,
  deptCount: 0,
  menuCount: 0
})
</script>

<style scoped lang="scss">
.home {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .stat-info {
      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
    }

    .stat-icon {
      font-size: 48px;
    }
  }
}
</style>
```

- [ ] **Step 3: 创建 src/views/error/404.vue**

```vue
<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped lang="scss">
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f5f5;

  .error-content {
    text-align: center;

    h1 {
      font-size: 120px;
      color: #409EFF;
      margin-bottom: 20px;
    }

    p {
      font-size: 24px;
      color: #606266;
      margin-bottom: 30px;
    }
  }
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add src/views/login/ src/views/home/ src/views/error/
git commit -m "feat(ui): 添加登录页和首页"
```

---

### Task 11: 用户管理页面

**Files:**
- Create: `src/api/auth.ts`
- Create: `src/api/types.ts`
- Create: `src/views/system/user/index.vue`
- Create: `src/views/system/user/types.ts`

- [ ] **Step 1: 创建基础 API 类型文件 src/api/types.ts**

```typescript
// 登录相关
export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  token: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar?: string
  isAdmin: boolean
  roles?: string[]
  permissions?: string[]
}

// 分页参数
export interface PageParams {
  page: number
  limit: number
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
}

// 菜单
export interface MenuRecord {
  id: number
  name: string
  path: string
  component?: string
  icon?: string
  parentId?: number
  children?: MenuRecord[]
}
```

- [ ] **Step 2: 创建 src/api/auth.ts**

```typescript
import request from '@/utils/request'
import type { LoginDTO, LoginVO, UserInfo } from './types'

export function login(data: LoginDTO) {
  return request<LoginVO>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getUserInfo() {
  return request<{ data: UserInfo }>({
    url: '/auth/info',
    method: 'get'
  })
}
```

- [ ] **Step 3: 创建 src/views/system/user/types.ts**

```typescript
export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  status: number
  createTime?: string
  updateTime?: string
}
```

- [ ] **Step 4: 创建 src/views/system/user/index.vue**

```vue
<template>
  <div class="user-management">
    <SearchForm
      :schema="searchSchema"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ t('user.addUser') }}
      </el-button>
    </div>

    <DataTable
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :page="queryParams.page"
      :limit="queryParams.limit"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #action="{ row }">
        <el-button type="primary" link @click="handleEdit(row)">
          {{ t('common.edit') }}
        </el-button>
        <el-button type="danger" link @click="handleDelete(row)">
          {{ t('common.delete') }}
        </el-button>
      </template>
    </DataTable>

    <FormDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :schema="formSchema"
      :default-data="formData"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchForm from '@/components/Common/SearchForm.vue'
import DataTable from '@/components/Common/DataTable.vue'
import FormDialog from '@/components/Common/FormDialog.vue'
import StatusTag from '@/components/Common/StatusTag.vue'
import type { User } from './types'

const { t } = useI18n()

// 搜索表单配置
const searchSchema = [
  { prop: 'username', label: '用户名', type: 'input', placeholder: '请输入用户名' },
  { prop: 'nickname', label: '昵称', type: 'input', placeholder: '请输入昵称' },
  { prop: 'status', label: '状态', type: 'select', placeholder: '请选择状态',
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

// 表格列配置
const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'username', label: '用户名', minWidth: 120 },
  { prop: 'nickname', label: '昵称', minWidth: 120 },
  { prop: 'email', label: '邮箱', minWidth: 150 },
  { prop: 'phone', label: '手机', minWidth: 120 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' }
]

// 表单配置
const formSchema = [
  { prop: 'username', label: '用户名', type: 'input', placeholder: '请输入用户名' },
  { prop: 'nickname', label: '昵称', type: 'input', placeholder: '请输入昵称' },
  { prop: 'email', label: '邮箱', type: 'input', placeholder: '请输入邮箱' },
  { prop: 'phone', label: '手机', type: 'input', placeholder: '请输入手机号' },
  { prop: 'status', label: '状态', type: 'switch', activeValue: 1, inactiveValue: 0 }
]

// 数据
const loading = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  limit: 10,
  username: '',
  nickname: '',
  status: undefined as number | undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = ref<Partial<User>>({})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    // TODO: 调用实际 API
    // const res = await getUserList(queryParams)
    // tableData.value = res.list
    // total.value = res.total
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch(params: any) {
  Object.assign(queryParams, params)
  queryParams.page = 1
  loadData()
}

function handleReset() {
  Object.keys(queryParams).forEach(key => {
    if (key !== 'page' && key !== 'limit') {
      (queryParams as any)[key] = undefined
    }
  })
  queryParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.limit = size
  loadData()
}

function handleAdd() {
  dialogTitle.value = t('user.addUser')
  formData.value = {}
  dialogVisible.value = true
}

function handleEdit(row: User) {
  dialogTitle.value = t('user.editUser')
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleDelete(row: User) {
  ElMessageBox.confirm(`确定删除用户 ${row.username} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // TODO: 调用删除 API
    ElMessage.success('删除成功')
    loadData()
  })
}

function handleSubmit(data: any) {
  console.log('提交数据:', data)
  // TODO: 调用保存 API
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 5: Commit**

```bash
git add src/api/ src/views/system/user/
git commit -m "feat(ui): 添加用户管理页面"
```

---

### Task 12: 其他系统管理页面

**Files:**
- Create: `src/views/system/role/index.vue`
- Create: `src/views/system/dept/index.vue`
- Create: `src/views/system/menu/index.vue`
- Create: `src/views/system/dict/index.vue`
- Create: `src/views/system/config/index.vue`

- [ ] **Step 1: 创建角色管理页面 src/views/system/role/index.vue**

```vue
<template>
  <div class="role-management">
    <SearchForm
      :schema="searchSchema"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ t('role.addRole') }}
      </el-button>
    </div>

    <DataTable
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :page="queryParams.page"
      :limit="queryParams.limit"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #action="{ row }">
        <el-button type="primary" link @click="handleEdit(row)">
          {{ t('common.edit') }}
        </el-button>
        <el-button type="danger" link @click="handleDelete(row)">
          {{ t('common.delete') }}
        </el-button>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/Common/SearchForm.vue'
import DataTable from '@/components/Common/DataTable.vue'
import StatusTag from '@/components/Common/StatusTag.vue'

const { t } = useI18n()

const searchSchema = [
  { prop: 'roleName', label: '角色名称', type: 'input', placeholder: '请输入角色名称' },
  { prop: 'roleKey', label: '角色编码', type: 'input', placeholder: '请输入角色编码' }
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'roleName', label: '角色名称', minWidth: 120 },
  { prop: 'roleKey', label: '角色编码', minWidth: 120 },
  { prop: 'sort', label: '排序', width: 80 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' }
]

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  limit: 10,
  roleName: '',
  roleKey: ''
})

function handleSearch(params: any) {
  Object.assign(queryParams, params)
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.roleName = ''
  queryParams.roleKey = ''
  queryParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.limit = size
  loadData()
}

function handleAdd() {
  ElMessage.info('新增角色')
}

function handleEdit(row: any) {
  ElMessage.info('编辑角色: ' + row.roleName)
}

function handleDelete(row: any) {
  ElMessage.info('删除角色: ' + row.roleName)
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    total.value = 0
    loading.value = false
  }, 300)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.role-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 2: 创建部门管理页面 src/views/system/dept/index.vue**

```vue
<template>
  <div class="dept-management">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ t('dept.addDept') }}
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      stripe
      border
    >
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="150" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <StatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            {{ t('common.edit') }}
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/Common/StatusTag.vue'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref<any[]>([])

function handleAdd() {
  ElMessage.info('新增部门')
}

function handleEdit(row: any) {
  ElMessage.info('编辑部门: ' + row.deptName)
}

function handleDelete(row: any) {
  ElMessage.info('删除部门: ' + row.deptName)
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    loading.value = false
  }, 300)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dept-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 3: 创建菜单管理页面 src/views/system/menu/index.vue**

```vue
<template>
  <div class="menu-management">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增菜单
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      stripe
      border
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="150" />
      <el-table-column prop="icon" label="图标" width="60" align="center" />
      <el-table-column prop="path" label="路由路径" min-width="150" />
      <el-table-column prop="component" label="组件路径" min-width="150" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="visible" label="可见" width="80">
        <template #default="{ row }">
          <el-tag :type="row.visible ? 'success' : 'info'">
            {{ row.visible ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            {{ t('common.edit') }}
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref<any[]>([])

function handleAdd() {
  ElMessage.info('新增菜单')
}

function handleEdit(row: any) {
  ElMessage.info('编辑菜单: ' + row.menuName)
}

function handleDelete(row: any) {
  ElMessage.info('删除菜单: ' + row.menuName)
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    loading.value = false
  }, 300)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.menu-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 4: 创建字典管理页面 src/views/system/dict/index.vue**

```vue
<template>
  <div class="dict-management">
    <SearchForm
      :schema="searchSchema"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ t('dict.addDict') }}
      </el-button>
    </div>

    <DataTable
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :page="queryParams.page"
      :limit="queryParams.limit"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #action="{ row }">
        <el-button type="primary" link @click="handleEdit(row)">
          {{ t('common.edit') }}
        </el-button>
        <el-button type="primary" link @click="handleItems(row)">
          字典项
        </el-button>
        <el-button type="danger" link @click="handleDelete(row)">
          {{ t('common.delete') }}
        </el-button>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/Common/SearchForm.vue'
import DataTable from '@/components/Common/DataTable.vue'

const { t } = useI18n()

const searchSchema = [
  { prop: 'dictName', label: '字典名称', type: 'input', placeholder: '请输入字典名称' },
  { prop: 'dictCode', label: '字典编码', type: 'input', placeholder: '请输入字典编码' }
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'dictName', label: '字典名称', minWidth: 150 },
  { prop: 'dictCode', label: '字典编码', minWidth: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  limit: 10,
  dictName: '',
  dictCode: ''
})

function handleSearch(params: any) {
  Object.assign(queryParams, params)
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.dictName = ''
  queryParams.dictCode = ''
  queryParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.limit = size
  loadData()
}

function handleAdd() {
  ElMessage.info('新增字典')
}

function handleEdit(row: any) {
  ElMessage.info('编辑字典: ' + row.dictName)
}

function handleItems(row: any) {
  ElMessage.info('字典项: ' + row.dictName)
}

function handleDelete(row: any) {
  ElMessage.info('删除字典: ' + row.dictName)
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    total.value = 0
    loading.value = false
  }, 300)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dict-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 5: 创建参数配置页面 src/views/system/config/index.vue**

```vue
<template>
  <div class="config-management">
    <SearchForm
      :schema="searchSchema"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </div>

    <DataTable
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :page="queryParams.page"
      :limit="queryParams.limit"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    >
      <template #status="{ row }">
        <StatusTag :status="row.status" />
      </template>
      <template #action="{ row }">
        <el-button type="primary" link @click="handleEdit(row)">
          {{ t('common.edit') }}
        </el-button>
        <el-button type="danger" link @click="handleDelete(row)">
          {{ t('common.delete') }}
        </el-button>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import SearchForm from '@/components/Common/SearchForm.vue'
import DataTable from '@/components/Common/DataTable.vue'
import StatusTag from '@/components/Common/StatusTag.vue'

const { t } = useI18n()

const searchSchema = [
  { prop: 'configName', label: '参数名称', type: 'input', placeholder: '请输入参数名称' },
  { prop: 'configKey', label: '参数键名', type: 'input', placeholder: '请输入参数键名' }
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'configName', label: '参数名称', minWidth: 150 },
  { prop: 'configKey', label: '参数键名', minWidth: 150 },
  { prop: 'configValue', label: '参数值', minWidth: 150 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' }
]

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  limit: 10,
  configName: '',
  configKey: ''
})

function handleSearch(params: any) {
  Object.assign(queryParams, params)
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.configName = ''
  queryParams.configKey = ''
  queryParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.limit = size
  loadData()
}

function handleAdd() {
  ElMessage.info('新增配置')
}

function handleEdit(row: any) {
  ElMessage.info('编辑配置: ' + row.configName)
}

function handleDelete(row: any) {
  ElMessage.info('删除配置: ' + row.configName)
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    total.value = 0
    loading.value = false
  }, 300)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.config-management {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 6: Commit**

```bash
git add src/views/system/
git commit -m "feat(ui): 添加系统管理页面"
```

---

## 实施完成

计划已保存到 `docs/superpowers/plans/2026-03-13-admin-ui-plan.md`。Ready to execute?
