import { createApp, nextTick } from "vue";
import { createPinia } from "pinia";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import { renderWithQiankun, qiankunWindow } from "vite-plugin-qiankun/dist/helper";
import App from "./App.vue";
import { createPortalRouter } from "./router";
import "./style.css";

let appInstance = null;

function render(props = {}) {
  const { container } = props;
  window.__FRAME_PROPS__ = props;
  const app = createApp(App);
  const pinia = createPinia();
  const router = createPortalRouter(qiankunWindow.__POWERED_BY_QIANKUN__);
  app.use(pinia);
  app.use(router);
  app.use(ArcoVue);
  const mountNode = container ? container.querySelector("#app") : document.querySelector("#app");
  app.mount(mountNode);
  appInstance = app;
}

renderWithQiankun({
  mount(props) {
    render(props);
  },
  bootstrap() {},
  async unmount(props) {
    try {
      if (appInstance) {
        await nextTick();
        appInstance.unmount();
        appInstance = null;
      }
    } catch (e) {
      console.warn("[portal] unmount error (safe to ignore):", e.message);
      appInstance = null;
    }
    try {
      const mountNode = props?.container
        ? props.container.querySelector("#app")
        : document.querySelector("#app");
      if (mountNode) {
        mountNode.innerHTML = "";
      }
    } catch {
      // mountNode may already be detached
    }
  }
});

if (!qiankunWindow.__POWERED_BY_QIANKUN__) {
  render();
}
