import http from "../core/http";
import { STORAGE_KEYS } from "@campus/common";
import {
  isJWT,
  parseJWT,
  getJWTUserInfo,
  getJWTRoles,
  logJWTDebugInfo,
} from "../../utils/jwt";

export async function login(payload) {
  const response = await http.post("/auth/login", payload);

  let result = response;

  if (result.data && (result.data.token || result.data.user)) {
    result = result.data;
  }

  console.log("[Auth] ===== 登录响应数据 =====");
  console.log("[Auth] 完整响应:", JSON.stringify(result, null, 2));

  if (result.token) {
    localStorage.setItem(STORAGE_KEYS.TOKEN, result.token);
    console.log("[Auth] ✅ Token已保存");

    if (isJWT(result.token)) {
      console.log("[Auth] ✅ 检测到JWT格式Token");
      logJWTDebugInfo(result.token);
    } else {
      console.log("[Auth] ⚠️ Token不是JWT格式（可能是UUID或其他格式）:", result.token.substring(0, 30));
    }
  }

  if (result.user) {
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(result.user));
    console.log("[Auth] ✅ 用户信息已保存:", result.user.username || result.user.nickname);

    if (isJWT(result.token)) {
      const jwtInfo = getJWTUserInfo(result.token);
      console.log("[Auth] 📋 JWT中的用户信息:", jwtInfo);
      console.log("[Auth] 📋 JWT中的角色:", getJWTRoles(result.token));
    }
  }

  return result;
}

export async function register(payload) {
  return await http.post("/auth/register", payload);
}

export function logout() {
  localStorage.removeItem(STORAGE_KEYS.TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER);
}

export function getToken() {
  return localStorage.getItem(STORAGE_KEYS.TOKEN) || "";
}

export function getCurrentUser() {
  try {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
}

export function getCurrentUserFromJWT() {
  const token = getToken();
  if (!token || !isJWT(token)) {
    return null;
  }

  return getJWTUserInfo(token);
}