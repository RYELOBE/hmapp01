// Shell 实际应用入口（MF 异步边界内）
import { createApp } from "vue";
import { createPinia } from "pinia";
import ArcoVue from "@arco-design/web-vue";
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import "@arco-design/web-vue/dist/arco.css";
import App from "./App.vue";
import router from "./router";
import { useAuthStore } from "./stores/auth";
import "./style.css";

const IS_DEV = process.env.NODE_ENV === 'development';

const logger = {
  log: (...args) => { if (IS_DEV) console.log(...args); },
  warn: (...args) => console.warn(...args),
  error: (...args) => console.error(...args),
};

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ArcoVue);
app.use(ArcoVueIcon);

// ═══════════════════════════════════════════════════════
// 全局错误处理
// ═══════════════════════════════════════════════════════

app.config.errorHandler = (err, instance, info) => {
  console.error('[Shell Global Error]', err, info);
  // 可以上报错误监控系统（如 Sentry）
  if (window.__ERROR_REPORT__) {
    window.__ERROR_REPORT__({
      error: err,
      component: instance?.$options?.name || 'Unknown',
      info,
      timestamp: Date.now(),
    });
  }
};

app.config.warnHandler = (msg, instance, trace) => {
  console.warn('[Shell Warning]', msg, trace);
};

// ═══════════════════════════════════════════════════════
// 性能监控（可选）
// ═══════════════════════════════════════════════════════

if (IS_DEV) {
  const perfObserver = new PerformanceObserver((list) => {
    for (const entry of list.getEntries()) {
      logger.log(`[Perf] ${entry.name}: ${entry.duration.toFixed(2)}ms`);
    }
  });
  perfObserver.observe({ entryTypes: ['measure', 'navigation'] });
}

// 记录应用启动时间
const bootStartTime = performance.now();

// ═══════════════════════════════════════════════════════
// Qiankun 生命周期钩子增强
// ═══════════════════════════════════════════════════════

let qiankunLifecycle = null;

if (window.__POWERED_BY_QIANKUN__) {
  qiankunLifecycle = {
    async beforeLoad(props) {
      logger.log('[Shell Qiankun] beforeLoad', props?.name || 'unknown');
      // 预加载提示
      const loadingEl = document.getElementById('qiankun-loading');
      if (loadingEl) {
        loadingEl.textContent = `正在准备 ${props?.name || '应用'}...`;
      }
    },

    async beforeMount(props) {
      logger.log('[Shell Qiankun] beforeMount', props?.name || 'unknown');
    },

    async afterMount(props) {
      logger.log('[Shell Qiankun] afterMount', props?.name || 'unknown');
      // 埋点上报
      if (window.__TRACKING__) {
        window.__TRACKING__('subapp_mounted', {
          appName: props?.name,
          timestamp: Date.now(),
          loadTime: (performance.now() - bootStartTime).toFixed(2),
        });
      }
    },

    async beforeUnmount(props) {
      logger.log('[Shell Qiankun] beforeUnmount', props?.name || 'unknown');
      // 清理确认
      return new Promise((resolve) => {
        // 给予清理时间窗口
        setTimeout(() => {
          resolve();
        }, 100);
      });
    },

    async afterUnmount(props) {
      logger.log('[Shell Qiankun] afterUnmount', props?.name || 'unknown');
    },
  };

  // 暴露生命周期给 Qiankun
  window.__SHELL_LIFECYCLE__ = qiankunLifecycle;
}

// ═══════════════════════════════════════════════════════
// 未捕获异常处理
// ═══════════════════════════════════════════════════════

window.addEventListener('error', (event) => {
  console.error('[Shell Uncaught Error]', event.error || event.message);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('[Shell Unhandled Promise Rejection]', event.reason);
});

// ═══════════════════════════════════════════════════════
// 应用挂载
// ═══════════════════════════════════════════════════════

// 在挂载前先恢复登录状态（必须在 mount 之前，否则组件渲染时 authStore 未初始化）
const authStore = useAuthStore();
authStore.hydrate();

app.mount("#app");

// 输出启动耗时（仅开发环境）
if (IS_DEV) {
  logger.log(`[Shell Boot] ${(performance.now() - bootStartTime).toFixed(2)}ms`);
}