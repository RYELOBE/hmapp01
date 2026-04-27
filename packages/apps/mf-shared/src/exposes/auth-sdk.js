import { STORAGE_KEYS } from "@campus/common/constants";

const TOKEN_KEY = STORAGE_KEYS.TOKEN;
const USER_KEY = STORAGE_KEYS.USER;

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
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

export function isLoggedIn() {
  return Boolean(getToken());
}

export function logout() {
  clearToken();
  clearCurrentUser();
  _userChangeListeners.forEach((fn) => fn(null));
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
