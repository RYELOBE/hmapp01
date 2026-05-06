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

app.config.errorHandler = (err, instance, info) => {
  console.error('[Shell Global Error]', err, info);
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

if (IS_DEV) {
  const perfObserver = new PerformanceObserver((list) => {
    for (const entry of list.getEntries()) {
      logger.log(`[Perf] ${entry.name}: ${entry.duration.toFixed(2)}ms`);
    }
  });
  perfObserver.observe({ entryTypes: ['measure', 'navigation'] });
}

const bootStartTime = performance.now();

window.addEventListener('error', (event) => {
  console.error('[Shell Uncaught Error]', event.error || event.message);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('[Shell Unhandled Promise Rejection]', event.reason);
});

const authStore = useAuthStore();
authStore.hydrate();

app.mount("#app");

if (IS_DEV) {
  logger.log(`[Shell Boot] ${(performance.now() - bootStartTime).toFixed(2)}ms`);
}
