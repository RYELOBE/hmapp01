import axios from "axios";
import { getToken, getOpsToken, logout, opsLogout } from "./auth-sdk.js";
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

// ── 重复请求取消管理 ──────────────────────────────
const pendingRequests = new Map();

function getPendingKey(config) {
  const { method, url, params } = config;
  return `${method}:${url}:${JSON.stringify(params || {})}`;
}

function removePendingRequest(config) {
  const key = getPendingKey(config);
  if (pendingRequests.has(key)) {
    pendingRequests.get(key).cancel('取消重复请求');
    pendingRequests.delete(key);
  }
}

// ── 简单缓存策略 ──────────────────────────────
const apiCache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5分钟缓存

export function cachedFetch(cacheKey, fetcher, ttl = CACHE_TTL) {
  const cached = apiCache.get(cacheKey);
  if (cached && Date.now() - cached.time < ttl) {
    return Promise.resolve(cached.data);
  }

  return fetcher().then(data => {
    apiCache.set(cacheKey, { data, time: Date.now() });
    return data;
  });
}

export function clearApiCache(pattern) {
  if (pattern) {
    for (const key of apiCache.keys()) {
      if (key.includes(pattern)) {
        apiCache.delete(key);
      }
    }
  } else {
    apiCache.clear();
  }
}

function isOpsPath(url) {
  return url.startsWith("/ops/") || url.includes("/ops/");
}

function getAuthToken(url, appType) {
  if (appType === 'ops') {
    return getOpsToken();
  }
  if (isOpsPath(url)) {
    return getOpsToken();
  }
  return getToken();
}

function handleLogout(url, appType) {
  if (appType === 'ops') {
    opsLogout();
  } else if (isOpsPath(url)) {
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
  appType = '',
} = {}) {
  const instance = axios.create({ baseURL, timeout, withCredentials: false });

  instance.interceptors.request.use(
    (conf) => {
      if (typeof navigator !== "undefined" && !navigator.onLine) {
        showErrorMsg("network", "网络已断开，请检查网络！");
        return Promise.reject(new Error("网络已断开，请检查网络！"));
      }

      // 取消之前的相同请求（防止重复提交）
      if (!conf.isAllowDuplicate) {
        removePendingRequest(conf);
        const source = axios.CancelToken.source();
        conf.cancelToken = source.token;
        pendingRequests.set(getPendingKey(conf), source);
      }

      if (conf.method === "get") {
        conf.params = { t: Date.now(), ...conf.params };
      }
      const token = getAuthToken(conf.url, appType);
      if (token) {
        conf.headers["Authorization"] = token;
      }
      if (requestCallback && typeof requestCallback === "function") {
        requestCallback(conf);
      }
      return conf;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  instance.interceptors.response.use(
    (response) => {
      // 清除已完成的请求
      const key = getPendingKey(response.config);
      pendingRequests.delete(key);

      const data = response.data;
      const url = response.config.url;

      if (data && typeof data === "object" && "code" in data) {
        if (
          !response.config.isTokenNotAuth &&
          getAuthToken(url, appType) &&
          (data.code === 401 || data.code === 402)
        ) {
          handleLogout(url, appType);
          if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
            gotoLoginCallback(response);
          } else {
            // 检查当前是否已经在登录页
            const currentPath = window.location.pathname;
            const loginPath = (appType === 'ops' || isOpsPath(url)) ? "/ops/login" : "/login";
            if (currentPath !== loginPath) {
              window.location.href = loginPath;
            }
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
        handleLogout(url, appType);
        if (gotoLoginCallback && typeof gotoLoginCallback === "function") {
          gotoLoginCallback(error.response);
        } else {
          const currentPath = window.location.pathname;
          const loginPath = (appType === 'ops' || isOpsPath(url)) ? "/ops/login" : "/login";
          if (currentPath !== loginPath) {
            window.location.href = loginPath;
          }
        }
      }

      const isNetworkError = !error?.response && (
        error?.code === 'ERR_NETWORK' ||
        error?.code === 'ECONNREFUSED' ||
        error?.code === 'ERR_CONNECTION_REFUSED' ||
        error?.message?.includes('Network Error') ||
        error?.message?.includes('connect')
      );

      if (isNetworkError && !error?.config?.isHideErrorMsg) {
        console.warn(`[http] 后端服务不可用 (${baseURL}): ${url}`);
        return Promise.reject({ ...error, isBackendUnavailable: true, code: 'BACKEND_UNAVAILABLE' });
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
const { http: opsHttp } = createHttp({ appType: 'ops' });
export { http, CancelToken, opsHttp };

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

// ── 防抖工具函数 ──────────────────────────────
export function debounce(fn, delay = 300) {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
      timer = null;
    }, delay);
  };
}
