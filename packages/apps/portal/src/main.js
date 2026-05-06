import { createApp, nextTick } from "vue";
import { createPinia } from "pinia";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import { renderWithQiankun, qiankunWindow } from "vite-plugin-qiankun/dist/helper";
import App from "./App.vue";
import { createPortalRouter } from "./router";
import "./style.css";

let appInstance = null;
let piniaInstance = null;

// 全局保存从主应用传递过来的 props
window.__FRAME_PROPS__ = null; 

function render(props = {}) {
  const { container, authStore } = props;
  window.__FRAME_PROPS__ = props;

  const app = createApp(App);
  const pinia = createPinia();
  piniaInstance = pinia;

  // 如果有从主应用传递的认证状态，初始化 Pinia store
  if (authStore) {
    // 这里可以初始化一个本地的 auth store
  }

  const router = createPortalRouter(qiankunWindow.__POWERED_BY_QIANKUN__);
  app.use(pinia);
  app.use(router);
  app.use(ArcoVue);

  // 确定挂载点
  let mountNode;
  if (container) {
    // 在 qiankun 环境下，在容器内创建 #app
    mountNode = container.querySelector("#app");
    if (!mountNode) {
      mountNode = document.createElement("div");
      mountNode.id = "app";
      container.appendChild(mountNode);
    }
  } else {
    // 独立运行时使用全局 #app
    mountNode = document.querySelector("#app");
  }

  app.mount(mountNode);
  appInstance = app;
}

renderWithQiankun({
  mount(props) {
    render(props);
  },
  bootstrap() {
  },
  async unmount(props) {
    try {
      if (appInstance) {
        await nextTick();
        appInstance.unmount();
        appInstance = null;
        piniaInstance = null;
      }
    } catch (e) {
      console.warn("[portal] unmount error (safe to ignore):", e.message);
      appInstance = null;
      piniaInstance = null;
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
  },
  update(props) {
    window.__FRAME_PROPS__ = props;
    // 可以在这里处理 props 更新
  },
});

if (!qiankunWindow.__POWERED_BY_QIANKUN__) {
  render();
}
