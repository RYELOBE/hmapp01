import { addGlobalUncaughtErrorHandler, registerMicroApps, start } from "qiankun";
import router from "../router";
import MessageManager from "./Message";
import ShellRouter from "./ShellRouter";
import framePinia from "./pinia/framePinia";

const shellRouter = new ShellRouter();
let started = false;
let registered = false;
const MicroRoutePlaceholder = { template: "<div></div>" };

// 加载状态和错误管理
const loadingState = {
  isLoading: false,
  error: null,
  retryCount: 0,
  maxRetries: 3,
  retryDelay: 2000,
  loadingTimeout: 30000, // 30秒超时
  timeoutId: null,
  listeners: [],
};

// 监听状态变化
function emitStateChange() {
  loadingState.listeners.forEach((listener) => listener({ ...loadingState }));
}

export function addLoadingListener(listener) {
  loadingState.listeners.push(listener);
  return () => {
    loadingState.listeners = loadingState.listeners.filter((l) => l !== listener);
  };
}

export function getLoadingState() {
  return { ...loadingState };
}

function buildActiveRule(pathPrefix) {
  return (location) => location.pathname.startsWith(pathPrefix);
}

export function createConfigs(apps = [], authStore = null) {
  return apps.map((app) => ({
    name: app.appCode,
    entry: app.entry,
    container: "#FRAME_WINDOW",
    activeRule: buildActiveRule(app.pathPrefix),
    props: {
      messageManager: MessageManager,
      frameRouter: router,
      shellRouter,
      gWin: window,
      // 传递认证状态给子应用
      authStore: authStore ? {
        token: authStore.token,
        user: authStore.user,
        roles: authStore.roles,
        isLoggedIn: authStore.isLoggedIn,
      } : null,
    },
    loader: (loading) => {
      loadingState.isLoading = loading;
      loadingState.error = null;
      emitStateChange();
    },
  }));
}

export function createRoutes(resources = []) {
  const routeNames = router.getRoutes().map((item) => item.name);
  resources.forEach((resource) => {
    const routeName = `micro_${resource.appCode}`;
    if (routeNames.includes(routeName)) {
      return;
    }
    // 动态添加子应用路由，使用 MicroView 组件
    router.addRoute("frameRoot", {
      path: `${resource.pathPrefix.replace(/^\//, "")}/:pathMatch(.*)*`,
      name: routeName,
      component: () => import("../views/MicroView.vue"),
      meta: {
        requiresAuth: true,
        roles: resource.roles || [],
        isMinProject: true,
        appCode: resource.appCode,
      },
    });
  });
}

// 清除超时
function clearLoadingTimeout() {
  if (loadingState.timeoutId) {
    clearTimeout(loadingState.timeoutId);
    loadingState.timeoutId = null;
  }
}

// 设置加载超时
function setLoadingTimeout() {
  clearLoadingTimeout();
  loadingState.timeoutId = setTimeout(() => {
    if (loadingState.isLoading) {
      loadingState.error = "加载超时，请检查网络连接后重试";
      loadingState.isLoading = false;
      emitStateChange();
      console.error("[minFrame] 子应用加载超时");
    }
  }, loadingState.loadingTimeout);
}

// 重置状态
function resetLoadingState() {
  clearLoadingTimeout();
  loadingState.isLoading = false;
  loadingState.error = null;
  loadingState.retryCount = 0;
  emitStateChange();
}

// 重试加载当前子应用
export function retryLoadMicroApp() {
  if (loadingState.retryCount >= loadingState.maxRetries) {
    loadingState.error = "重试次数已达上限，请刷新页面重试";
    emitStateChange();
    return;
  }

  loadingState.retryCount++;
  loadingState.error = null;
  loadingState.isLoading = true;
  emitStateChange();

  const currentPath = router.currentRoute.value.fullPath;
  setTimeout(() => {
    router.replace("/").then(() => {
      setTimeout(() => router.replace(currentPath), 100);
    });
  }, loadingState.retryDelay);
}

export function startMinFrame() {
  const frame = framePinia();
  if (!frame.registConfigs || !frame.registConfigs.length) {
    console.warn("[minFrame] 没有可注册的子应用配置");
    return;
  }

  if (!registered) {
    try {
      registerMicroApps(frame.registConfigs, {
        beforeLoad: (app) => {
          console.log(`[minFrame] 开始加载子应用: ${app.name}`);
          resetLoadingState();
          loadingState.isLoading = true;
          setLoadingTimeout();
          emitStateChange();
        },
        beforeMount: (app) => {
          console.log(`[minFrame] 子应用即将挂载: ${app.name}`);
          clearLoadingTimeout();
        },
        afterMount: (app) => {
          console.log(`[minFrame] 子应用挂载完成: ${app.name}`);
          resetLoadingState();
        },
        beforeUnmount: (app) => {
          console.log(`[minFrame] 子应用即将卸载: ${app.name}`);
        },
        afterUnmount: (app) => {
          console.log(`[minFrame] 子应用卸载完成: ${app.name}`);
        },
      });
      registered = true;
      console.log("[minFrame] 子应用注册成功");
    } catch (error) {
      console.error("[minFrame] 子应用注册失败:", error);
      loadingState.error = "子应用注册失败: " + (error.message || String(error));
      emitStateChange();
      return;
    }
  }

  if (!started) {
    try {
      start({
        prefetch: false,
        sandbox: {
          strictStyleIsolation: false,
          experimentalStyleIsolation: false,
          strictPropertyAccess: false,
        },
        singular: false,
      });
      started = true;
      console.log("[minFrame] qiankun 启动成功");
    } catch (error) {
      console.error("[minFrame] qiankun 启动失败:", error);
      loadingState.error = "微前端框架启动失败: " + (error.message || String(error));
      emitStateChange();
    }
  }
}

function getMicroAppErrorMessage(event) {
  return event?.message
    || event?.reason?.message
    || event?.error?.message
    || event?.detail?.message
    || String(event || "子应用加载失败");
}

addGlobalUncaughtErrorHandler((event) => {
  const current = router.currentRoute.value.fullPath;
  const reason = getMicroAppErrorMessage(event);
  console.error("[minFrame] 子应用加载错误:", reason, event);

  clearLoadingTimeout();
  loadingState.isLoading = false;
  loadingState.error = `子应用加载失败: ${reason}`;
  emitStateChange();
});
