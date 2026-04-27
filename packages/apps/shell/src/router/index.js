import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import ForbiddenView from "../views/ForbiddenView.vue";
import ShellHomeView from "../views/ShellHomeView.vue";
import LayoutView from "../views/container/Layout.vue";
import { useAuthStore } from "../stores/auth";
import { hasAnyRole } from "@campus/common/roles";
import framePinia from "../minFrame/pinia/framePinia";

const MicroRoutePlaceholder = { template: "<div></div>" };

const routes = [
  {
    path: "/login",
    name: "login",
    component: LoginView,
  },
  {
    path: "/forbidden",
    name: "forbidden",
    component: ForbiddenView,
  },
  {
    path: "/",
    name: "frameRoot",
    component: LayoutView,
    children: [
      {
        path: "",
        name: "home",
        component: ShellHomeView,
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "fallback",
    component: MicroRoutePlaceholder,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const whiteList = ["/login", "/forbidden"];

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  const frame = framePinia();

  // 恢复本地存储的 token
  if (!authStore.user && !authStore.token) {
    authStore.hydrate();
  }

  // === 未登录：仅初始化 qiankun（注册微应用），不加载权限路由 ===
  if (!authStore.isLoggedIn) {
    // 公开页面直接放行
    if (whiteList.includes(to.path)) {
      // 尝试加载 configs，但不阻塞导航
      if (!frame.configsLoaded) {
        frame.initFrame().catch(err => {
          console.error("Failed to init frame in whitelist:", err);
        });
      }
      return true;
    }
    // 需认证页面 → 跳转登录，不等待 configs 加载
    if (to.meta.requiresAuth || to.meta.roles) {
      return { name: "login", query: { redirect: to.fullPath } };
    }
    // 其他页面也不等待 configs 加载
    return true;
  }

  // === 已登录：加载 configs + 权限路由 ===
  if (authStore.isLoggedIn && !frame.loaded) {
    try {
      await frame.initFrameWithRoutes();
      // 动态路由添加后，需要重新匹配
      if (to.matched.length === 0 || to.name === "fallback") {
        // 检查是否已经添加了对应的动态路由
        const hasDynamicRoute = router.getRoutes().some(route => {
          return route.path.startsWith(to.path.split('/')[1]);
        });
        if (hasDynamicRoute) {
          return { path: to.fullPath, replace: true };
        }
      }
    } catch (error) {
      console.error("Failed to init frame with routes:", error);
      // 如果是 401（token 过期/无效），清除登录状态并跳转登录页
      const status = error?.response?.status;
      const msg = error?.message || "";
      if (status === 401 || msg.includes("Unauthorized") || msg.includes("未登录") || msg.includes("登录")) {
        authStore.logout();
        return { name: "login", query: { redirect: to.fullPath } };
      }
      // 其他错误继续导航
      return true;
    }
  }

  // 角色权限校验
  if (to.meta.roles && !hasAnyRole(authStore.roles, to.meta.roles)) {
    return { name: "forbidden" };
  }
  return true;
});

export default router;
