import axios from "axios";
import { getToken } from "./auth-sdk.js";

const baseURL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export const http = axios.create({
  baseURL,
  timeout: 12000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers["Authorization"] = token;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error?.response?.status;
    const message = error?.response?.data?.message || "Request failed";
    const err = new Error(message);
    err.status = status;
    return Promise.reject(err);
  }
);
