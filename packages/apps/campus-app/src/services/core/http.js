import axios from "axios";
import { STORAGE_KEYS } from "@campus/common";
import {
  isJWT,
  formatJWTToken,
  isJWTExpired,
  logJWTDebugInfo,
} from "../../utils/jwt";

const API_BASE_URL = "/api";

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN);

  console.log(`[HTTP Request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);

  if (token) {
    if (isJWT(token)) {
      config.headers.Authorization = formatJWTToken(token);
      console.log(`[HTTP Request] ✅ 携带JWT Token:`, token.substring(0, 50) + '...');

      if (isJWTExpired(token)) {
        console.warn(`[HTTP Request] ⚠️ JWT已过期！可能导致请求失败`);
      }
    } else {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`[HTTP Request] ✅ 携带Token:`, token.substring(0, 30) + '... (非JWT格式)');
    }
  } else {
    console.warn(`[HTTP Request] ⚠️ 无Token！`);
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const url = error.config?.url;
    const status = error.response?.status;

    console.error(`[HTTP Error] ${error.config?.method?.toUpperCase()} ${url} → 状态码: ${status}`);

    if (status === 401) {
      console.error(`[HTTP] ❌ 收到401未授权响应！`);
      console.error(`[HTTP] 请求URL: ${url}`);
      console.error(`[HTTP] 完整错误:`, error.response?.data || error.message);

      const currentToken = localStorage.getItem(STORAGE_KEYS.TOKEN);
      console.error(`[HTTP] 当前Token状态:`, currentToken ? '存在' : '❌ 不存在');

      if (currentToken) {
        console.error(`[HTTP] Token值:`, currentToken.substring(0, 50) + '...');

        logJWTDebugInfo(currentToken);
      }

      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);

      console.error(`[HTTP] 已清除认证信息，准备跳转到登录页...`);

      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default http;
