import { addGlobalUncaughtErrorHandler, registerMicroApps, start } from "qiankun";
import router from "../router";
import MessageManager from "./Message";
import ShellRouter from "./ShellRouter";
import framePinia from "./pinia/framePinia";

const shellRouter = new ShellRouter();
let started = false;
let registered = false;
const MicroRoutePlaceholder = { template: "<div></div>" };

function buildActiveRule(pathPrefix) {
  return (location) => location.pathname.startsWith(pathPrefix);
}

export function createConfigs(apps = []) {
  return apps.map((app) => ({
    name: app.appCode,
    entry: app.entry,
    container: "#FRAME_WINDOW",
    activeRule: buildActiveRule(app.pathPrefix),
    props: {
      messageManager: MessageManager,
      frameRouter: router,
      shellRouter,
      gWin: window
    }
  }));
}

export function createRoutes(resources = []) {
  const routeNames = router.getRoutes().map((item) => item.name);
  resources.forEach((resource) => {
    const routeName = `micro_${resource.appCode}`;
    if (routeNames.includes(routeName)) {
      return;
    }
    router.addRoute("frameRoot", {
      path: `${resource.pathPrefix.replace(/^\//, "")}/:pathMatch(.*)*`,
      name: routeName,
      component: MicroRoutePlaceholder,
      meta: {
        requiresAuth: true,
        roles: resource.roles || [],
        isMinProject: true,
        appCode: resource.appCode
      }
    });
  });
}

export function startMinFrame() {
  const frame = framePinia();
  if (!frame.registConfigs || !frame.registConfigs.length) {
    return;
  }
  if (!registered) {
    registerMicroApps(frame.registConfigs);
    registered = true;
  }
  if (!started) {
    start({
      prefetch: false,
      sandbox: {
        strictStyleIsolation: false,
        experimentalStyleIsolation: false
      }
    });
    started = true;
  }
}

addGlobalUncaughtErrorHandler((event) => {
  const message = event?.message || "";
  if (message.includes("qiankun")) {
    const current = router.currentRoute.value.path;
    router.push(`/forbidden?redirect=${encodeURIComponent(current)}`);
  }
});
