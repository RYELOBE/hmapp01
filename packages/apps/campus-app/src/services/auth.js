import axios from "axios";
import { STORAGE_KEYS } from "@campus/common";

const API_BASE_URL = "/api";

const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

instance.interceptors.response.use((response) => {
  return response.data;
}, (error) => {
  if (error.response?.status === 401) {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "/login";
  }
  return Promise.reject(error);
});

export async function login(payload) {
  const result = await instance.post("/auth/login", payload);
  if (result.token && result.user) {
    localStorage.setItem(STORAGE_KEYS.TOKEN, result.token);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(result.user));
  }
  return result;
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

export default instance;