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

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ArcoVue);
app.use(ArcoVueIcon);

// 在挂载前先恢复登录状态（必须在 mount 之前，否则组件渲染时 authStore 未初始化）
const authStore = useAuthStore();
authStore.hydrate();

app.mount("#app");
