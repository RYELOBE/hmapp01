import axios from "axios";
import { getToken, logout } from "./auth-sdk.js";

// ── 节流错误提示：避免批量请求时刷屏 ────────────────────────
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
    const { Message } = require("@arco-design/web-vue");
    Message.error({ content: msg, resetOnHover: true });
  } catch {
    console.error("[http]", msg);
  }
}, 300);

// ── HTTP 状态码映射 ──────────────────────────────────────
const STATUS_MSG_MAP = {
  400: "请求错误(400)",
  401: "未授权，请重新登录(401)",
  403: "拒绝访问(403)",
  404: "请求出错(404)",
  408: "请求超时(408)",
  500: "服务器错误(500)",
  501: "服务未实现(501)",
  502: "网络错误(502)",
  503: "服务不可用(503)",
  504: "网络超时(504)",
  505: "HTTP版本不受支持(505)",
};

// ── createHttp 工厂函数 ─────────────────────────────────
/**
 * 创建 axios 实例（参考 CommonEngine-web baseRequest.js）
 * @param {Object} options
 * @param {boolean}  options.isGlobalShowErrorMsg  - 业务码非200时是否全局提示（默认 true）
 * @param {Function} options.gotoLoginCallback     - 401时跳转登录回调（默认 logout + 跳 /login）
 * @param {Function} options.requestCallback       - 请求拦截额外回调
 * @param {Function} options.responseCallback      - 响应拦截额外回调
 * @param {string}   options.baseURL               - 基础路径（默认取环境变量）
 * @param {number}   options.timeout               - 超时时间（默认 12000ms）
 * @returns {{ http: AxiosInstance, CancelToken }}
 */
export function createHttp({
  isGlobalShowErrorMsg = true,
  gotoLoginCallback = undefined,
  requestCallback = undefined,
  responseCallback = undefined,
  baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout = 12000,
} = {}) {
  const instance = axios.create({ baseURL, timeout, withCredentials: false });

  // ── 请求拦截 ────────────────────────────────────────
  instance.interceptors.request.use(
    (conf) => {
      // 网络断开检测
      if (typeof navigator !== "undefined" && !navigator.onLine) {
        showErrorMsg("network", "网络已断开，请检查网络！");
        return Promise.reject(new Error("网络已断开，请检查网络！"));
      }
      // GET 请求防缓存（加时间戳）
      if (conf.method === "get") {
        conf.params = { t: Date.now(), ...conf.params };
      }
      // 注入 token
      const token = getToken();
      if (token) {
        conf.headers["Authorization"] = token;
      }
      // 额外请求回调
      if (requestCallback && typeof requestCallback === "function") {
        requestCallback(conf);
      }
      return conf;
    },
    (error) => Promise.reject(error)
  );

  // ── 响应拦截 ────────────────────────────────────────
  instance.interceptors.response.use(
    (response) => {
      const data = response.data;

      // 业务码判断
      if (data && typeof data === "object" && "code" in data) {
        // 401 / 402 在 token 存在时触发登出
        if (
          !response.config.isTokenNotAuth &&
          getToken() &&
          (data.code === 401 || data.code === 402)
        ) {
          logout();
          if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
            gotoLoginCallback(response);
          } else {
            window.location.hash = "#/login";
          }
          return Promise.reject(data);
        }

        // 额外响应回调
        if (responseCallback && typeof responseCallback === "function") {
          const cbResult = responseCallback(response);
          if (cbResult && cbResult.resState === "reject") return Promise.reject(cbResult.data);
          if (cbResult && cbResult.resState === "resolve") return cbResult.data;
          if (cbResult) return cbResult;
        }

        // 成功
        if (data.code === 0 || data.code === 200) {
          return data.data !== undefined ? data.data : data;
        }

        // 业务错误
        if (isGlobalShowErrorMsg && !response.config.isHideErrorMsg) {
          const msg = data.message || data.msg || "请求失败";
          showErrorMsg(data.code, msg);
        }
        const err = new Error(data.message || data.msg || "请求失败");
        err.code = data.code;
        return Promise.reject(err);
      }

      // 非 { code, data } 格式，直接返回
      return data;
    },
    (error) => {
      const status = error?.response?.status;
      const data = error?.response?.data;
      const message = data?.message || data?.msg || STATUS_MSG_MAP[status] || "请求失败";

      // 401 处理
      if (status === 401) {
        logout();
        if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
          gotoLoginCallback(error.response);
        } else {
          window.location.hash = "#/login";
        }
      }

      // 非 401 的 HTTP 错误也弹提示
      if (status && status !== 401 && !error?.config?.isHideErrorMsg) {
        showErrorMsg(status, message);
      }

      const err = new Error(message);
      err.status = status;
      err.code = data?.code;
      return Promise.reject(err);
    }
  );

  return { http: instance, CancelToken: axios.CancelToken };
}

// ── 默认实例（向后兼容） ─────────────────────────────────
const { http, CancelToken } = createHttp();
export { http, CancelToken };

// ── 快捷方法（保持现有导出兼容） ──────────────────────────
export function post(url, data = {}, params = {}, optionConfig = {}) {
  return http({ method: "post", url, data, params, ...optionConfig });
}

export function get(url, params = {}, optionConfig = {}) {
  return http({ method: "get", url, params, ...optionConfig });
}

export function put(url, data = {}, params = {}, optionConfig = {}) {
  return http({ method: "put", url, data, params, ...optionConfig });
}

export function _delete(url, params = {}, optionConfig = {}) {
  return http({ method: "delete", url, params, ...optionConfig });
}
