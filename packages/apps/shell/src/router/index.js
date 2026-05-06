import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { hasAnyRole } from "@campus/common/roles";
import framePinia from "../minFrame/pinia/framePinia";

const LoginView = () => import(/* webpackChunkName: "shell-login" */ "../views/LoginView.vue");
const ForbiddenView = () => import(/* webpackChunkName: "shell-forbidden" */ "../views/ForbiddenView.vue");
const ShellHomeView = () => import(/* webpackChunkName: "shell-home" */ "../views/ShellHomeView.vue");
const LayoutView = () => import(/* webpackChunkName: "shell-layout" */ "../views/container/Layout.vue");
const MicroView = () => import(/* webpackChunkName: "shell-micro" */ "../views/MicroView.vue");

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
    component: MicroView,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const whiteList = ["/login", "/forbidden"];
const redirectCountMap = new Map(); // 防止无限重定向

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  const frame = framePinia();

  // --- 防止无限重定向的安全检查 ---
  const now = Date.now();
  const lastRedirect = redirectCountMap.get(to.fullPath) || 0;
  if (now - lastRedirect < 3000) {
    console.warn("[router] Possible infinite redirect detected, stopping:", to.fullPath);
    return true;
  }
  redirectCountMap.set(to.fullPath, now);

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

    // 需认证页面 → 检查当前路由及所有父路由的 meta
    const requiresAuth = to.matched.some(record =>
      record.meta.requiresAuth || record.meta.roles
    );

    if (requiresAuth) {
      return { name: "login", query: { redirect: to.fullPath } };
    }

    // 其他页面也不等待 configs 加载
    return true;
  }

  // === 已登录：Token有效性预检 ===
  if (authStore.isLoggedIn && !frame.loaded) {
    try {
      await frame.initFrameWithRoutes();
      
      // 如果访问的是首页，直接重定向到门户
      if (to.path === '/' || to.name === 'home') {
        return { path: '/portal/home', replace: true };
      }
      
      // 动态路由添加后，需要重新匹配
      if (to.matched.length === 0 || to.name === "fallback") {
        // 检查是否已经添加了对应的动态路由
        const hasDynamicRoute = router.getRoutes().some(route => {
          const pathPrefix = `/${to.path.split('/')[1]}`;
          return route.path.startsWith(pathPrefix);
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
      if (status === 401 || msg.includes("Unauthorized") || msg.includes("未登录") || msg.includes("登录") || msg.includes("expired")) {
        authStore.logout();
        return { name: "login", query: { redirect: to.fullPath, reason: 'session_expired' } };
      }
      // 其他错误继续导航，不要阻塞
      return true;
    }
  }

  // 角色权限校验
  if (to.meta.roles && !hasAnyRole(authStore.roles, to.meta.roles)) {
    return { name: "forbidden" };
  }
  return true;
});

// 全局后置钩子：记录页面访问日志（可选）
router.afterEach((to) => {
  document.title = to.meta?.title || 'CampusTrade - Shell';
});

export default router;
