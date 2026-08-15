/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description router全局配置，业务导航由后端/auth/routes返回
 */

import { createRouter, createWebHashHistory } from "vue-router";
import { publicPath } from "@/config";

// Layout 会加载 AI Agent runtime，而 runtime 的请求层又依赖 router。
// 在实际路由导航时再加载 Layout，避免应用启动阶段形成循环依赖并捕获未初始化的 router。
const Layout = () => import("@/layouts/index.vue");

export const constantRoutes = [
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    hidden: true,
  },
  {
    path: "/register",
    component: () => import("@/views/register/index.vue"),
    hidden: true,
  },
  {
    path: "/forgot-password",
    component: () => import("@/views/forgotPassword/index.vue"),
    hidden: true,
  },
  {
    path: "/no-permission",
    name: "NoPermission",
    component: () => import("@/views/noPermission/index.vue"),
    hidden: true,
    meta: {
      title: "暂无权限",
    },
  },
  {
    path: "/personal-center",
    name: "PersonalCenter",
    component: Layout,
    hidden: true,
    children: [
      {
        path: "",
        name: "PersonalCenterIndex",
        component: () => import("@/views/personalCenter/index.vue"),
        meta: {
          title: "个人中心",
        },
      },
    ],
  },
  {
    path: "/",
    component: Layout,
    redirect: "/index",
    hidden: false,
    children: [
      {
        path: "index",
        name: "Index",
        component: () => import("@/views/index/index.vue"),
        meta: {
          title: "首页",
          icon: "home",
          affix: true,
        },
      },
    ],
  },
  {
    path: "/notice",
    component: Layout,
    hidden: true,
    children: [
      {
        path: "",
        name: "NoticeCenter",
        component: () => import("@/views/notice/index.vue"),
        meta: { title: "公告中心" },
      },
      {
        path: ":id",
        name: "NoticeDetail",
        component: () => import("@/views/notice/detail.vue"),
        meta: { title: "公告详情" },
      },
    ],
  },
  {
    path: "/401",
    name: "401",
    component: () => import("@/views/401.vue"),
    hidden: true,
  },
  {
    path: "/404",
    name: "404",
    component: () => import("@/views/404.vue"),
    hidden: true,
  },
];

export const asyncRoutes = [];

export const fallbackRoute = {
  path: "/:pathMatch(.*)*",
  redirect: "/404",
  hidden: true,
};

const router = createRouter({
  history: createWebHashHistory(publicPath),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

const dynamicRouteRemovers = new Set();

export function addDynamicRoute(route) {
  const removeRoute = router.addRoute(route);
  dynamicRouteRemovers.add(removeRoute);
  return removeRoute;
}

export function resetRouter() {
  try {
    dynamicRouteRemovers.forEach((removeRoute) => {
      removeRoute();
    });
    dynamicRouteRemovers.clear();
  } catch (error) {
    // 强制刷新浏览器，不要用这种方式
    window.location.reload();
  }
}

export default router;
