import http from "../services/core/http";
import { getToken, getOpsToken, logout as sdkLogout, opsLogout } from "./auth-sdk.js";
import { Message } from "@arco-design/web-vue";

const throttleMap = new Map();
function throttle(fn, delay) {
  return function (key, ...args) {
    if (throttleMap.has(key)) return;
    fn(...args);
    throttleMap.set(key, true);
    setTimeout(() => throttleMap.delete(key), delay);
  };
}

const showErrorMsg = throttle((msg) => {
  try {
    Message.error({ content: msg, resetOnHover: true });
  } catch {
    console.error("[http]", msg);
  }
}, 300);

export { http };
export const opsHttp = http;

export default http;
