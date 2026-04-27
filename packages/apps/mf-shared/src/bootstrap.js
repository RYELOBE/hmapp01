// MF 实际应用入口（异步边界内）
import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import "@arco-design/web-vue/dist/arco.css";

const app = createApp(App);
app.use(createPinia());
app.mount("#app");
