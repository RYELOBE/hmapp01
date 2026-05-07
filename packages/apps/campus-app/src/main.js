import { createApp } from "vue";
import { createPinia } from "pinia";
import ArcoVue from "@arco-design/web-vue";
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import "@arco-design/web-vue/dist/arco.css";
import App from "./App.vue";
import router from "./router";
import { useAuthStore } from "./stores/auth";
import "./style.css";
import "./styles/main.scss";

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
  console.error('[App Global Error]', err, info);
};

app.config.warnHandler = (msg, instance, trace) => {
  console.warn('[App Warning]', msg, trace);
};

window.addEventListener('error', (event) => {
  console.error('[App Uncaught Error]', event.error || event.message);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('[App Unhandled Promise Rejection]', event.reason);
});

const authStore = useAuthStore();
authStore.hydrate();

app.mount("#app");

if (IS_DEV) {
  logger.log(`[App Boot] completed`);
}