import { STORAGE_KEYS } from "@campus/common/constants";

const TOKEN_KEY = STORAGE_KEYS.TOKEN;
const USER_KEY = STORAGE_KEYS.USER;
const OPS_TOKEN_KEY = STORAGE_KEYS.OPS_TOKEN;
const OPS_USER_KEY = STORAGE_KEYS.OPS_USER;

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getOpsToken() {
  return localStorage.getItem(OPS_TOKEN_KEY) || "";
}

export function setOpsToken(token) {
  localStorage.setItem(OPS_TOKEN_KEY, token);
}

export function clearOpsToken() {
  localStorage.removeItem(OPS_TOKEN_KEY);
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
}

export function setCurrentUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearCurrentUser() {
  localStorage.removeItem(USER_KEY);
}

export function getOpsCurrentUser() {
  const raw = localStorage.getItem(OPS_USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
}

export function setOpsCurrentUser(user) {
  localStorage.setItem(OPS_USER_KEY, JSON.stringify(user));
}

export function clearOpsCurrentUser() {
  localStorage.removeItem(OPS_USER_KEY);
}

export function isLoggedIn() {
  return Boolean(getToken());
}

export function isOpsLoggedIn() {
  return Boolean(getOpsToken());
}

export function logout() {
  clearToken();
  clearCurrentUser();
  _userChangeListeners.forEach((fn) => fn(null));
}

export function opsLogout() {
  clearOpsToken();
  clearOpsCurrentUser();
  _opsUserChangeListeners.forEach((fn) => fn(null));
}

/** 注册用户登录/登出变化的监听器 */
const _userChangeListeners = [];
export function onUserChange(callback) {
  _userChangeListeners.push(callback);
  const handler = (e) => {
    if (e.key === USER_KEY || e.key === TOKEN_KEY) {
      callback(getCurrentUser());
    }
  };
  window.addEventListener("storage", handler);
  return () => {
    const idx = _userChangeListeners.indexOf(callback);
    if (idx > -1) _userChangeListeners.splice(idx, 1);
    window.removeEventListener("storage", handler);
  };
}

const _opsUserChangeListeners = [];
export function onOpsUserChange(callback) {
  _opsUserChangeListeners.push(callback);
  const handler = (e) => {
    if (e.key === OPS_USER_KEY || e.key === OPS_TOKEN_KEY) {
      callback(getOpsCurrentUser());
    }
  };
  window.addEventListener("storage", handler);
  return () => {
    const idx = _opsUserChangeListeners.indexOf(callback);
    if (idx > -1) _opsUserChangeListeners.splice(idx, 1);
    window.removeEventListener("storage", handler);
  };
}

export async function login({ username, password }) {
  const baseURL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
  const response = await fetch(`${baseURL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "登录失败" }));
    throw new Error(error.message || "登录失败");
  }

  const result = await response.json();
  setToken(result.token);
  setCurrentUser(result.user);
  _userChangeListeners.forEach((fn) => fn(result.user));
  return result;
}

export async function opsLogin({ username, password }) {
  const baseURL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
  const response = await fetch(`${baseURL}/auth/ops/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "运营登录失败" }));
    throw new Error(error.message || "运营登录失败");
  }

  const result = await response.json();
  setOpsToken(result.token);
  setOpsCurrentUser(result.user);
  _opsUserChangeListeners.forEach((fn) => fn(result.user));
  return result;
}

export async function register({ username, password, nickname, roles }) {
  const baseURL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
  const response = await fetch(`${baseURL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password, nickname, roles }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "注册失败" }));
    throw new Error(error.message || "注册失败");
  }

  const result = await response.json();
  setToken(result.token);
  setCurrentUser(result.user);
  _userChangeListeners.forEach((fn) => fn(result.user));
  return result;
}
