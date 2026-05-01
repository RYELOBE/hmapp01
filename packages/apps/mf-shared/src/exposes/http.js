import axios from "axios";
import { getToken, getOpsToken, logout, opsLogout } from "./auth-sdk.js";

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

function isOpsPath(url) {
  return url.startsWith("/ops/") || url.includes("/ops/");
}

function getAuthToken(url) {
  if (isOpsPath(url)) {
    return getOpsToken();
  }
  return getToken();
}

function handleLogout(url) {
  if (isOpsPath(url)) {
    opsLogout();
  } else {
    logout();
  }
}

export function createHttp({
  isGlobalShowErrorMsg = true,
  gotoLoginCallback = undefined,
  requestCallback = undefined,
  responseCallback = undefined,
  baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout = 12000,
} = {}) {
  const instance = axios.create({ baseURL, timeout, withCredentials: false });

  instance.interceptors.request.use(
    (conf) => {
      if (typeof navigator !== "undefined" && !navigator.onLine) {
        showErrorMsg("network", "网络已断开，请检查网络！");
        return Promise.reject(new Error("网络已断开，请检查网络！"));
      }
      if (conf.method === "get") {
        conf.params = { t: Date.now(), ...conf.params };
      }
      const token = getAuthToken(conf.url);
      if (token) {
        conf.headers["Authorization"] = token;
      }
      if (requestCallback && typeof requestCallback === "function") {
        requestCallback(conf);
      }
      return conf;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (response) => {
      const data = response.data;
      const url = response.config.url;

      if (data && typeof data === "object" && "code" in data) {
        if (
          !response.config.isTokenNotAuth &&
          getAuthToken(url) &&
          (data.code === 401 || data.code === 402)
        ) {
          handleLogout(url);
          if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
            gotoLoginCallback(response);
          } else {
            window.location.hash = isOpsPath(url) ? "#/ops/login" : "#/login";
          }
          return Promise.reject(data);
        }

        if (responseCallback && typeof responseCallback === "function") {
          const cbResult = responseCallback(response);
          if (cbResult && cbResult.resState === "reject") return Promise.reject(cbResult.data);
          if (cbResult && cbResult.resState === "resolve") return cbResult.data;
          if (cbResult) return cbResult;
        }

        if (data.code === 0 || data.code === 200) {
          return data.data !== undefined ? data.data : data;
        }

        if (isGlobalShowErrorMsg && !response.config.isHideErrorMsg) {
          const msg = data.message || data.msg || "请求失败";
          showErrorMsg(data.code, msg);
        }
        const err = new Error(data.message || data.msg || "请求失败");
        err.code = data.code;
        return Promise.reject(err);
      }

      return data;
    },
    (error) => {
      const status = error?.response?.status;
      const data = error?.response?.data;
      const url = error?.config?.url;
      const message = data?.message || data?.msg || STATUS_MSG_MAP[status] || "请求失败";

      if (status === 401) {
        handleLogout(url);
        if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
          gotoLoginCallback(error.response);
        } else {
          window.location.hash = isOpsPath(url) ? "#/ops/login" : "#/login";
        }
      }

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

const { http, CancelToken } = createHttp();
export { http, CancelToken };

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
