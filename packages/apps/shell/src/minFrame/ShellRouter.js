import router from "../router";

export default class ShellRouter {
  push(path) {
    router.push(path).catch(() => undefined);
  }

  replace(path) {
    router.replace(path).catch(() => undefined);
  }

  back() {
    router.back();
  }

  go(step) {
    router.go(step);
  }
}
