import { defineStore } from "pinia";
import {
  getCurrentUser,
  getToken,
  logout as sdkLogout,
  login,
  register as sdkRegister,
  getCurrentUserFromJWT,
} from "../services/auth";
import { isJWT, getJWTRoles, isJWTExpired } from "../utils/jwt";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: "",
    user: null,
  }),
  getters: {
    roles: (state) => {
      if (isJWT(state.token)) {
        return getJWTRoles(state.token);
      }

      const rawRoles = state.user?.roles;
      if (Array.isArray(rawRoles)) return rawRoles;
      if (typeof rawRoles === "string" && rawRoles.trim())
        return [rawRoles.trim()];
      return [];
    },
    isLoggedIn: (state) => Boolean(state.token),
    isJWTToken: (state) => isJWT(state.token),
    isTokenExpired: (state) => {
      if (!state.token) return true;
      if (isJWT(state.token)) {
        return isJWTExpired(state.token);
      }
      return false;
    },
  },
  actions: {
    hydrate() {
      this.token = getToken();
      this.user = getCurrentUser();

      console.log("[Auth Store] ===== Hydrate 状态恢复 =====");
      console.log("[Auth Store] Token:", this.token ? this.token.substring(0, 30) + "..." : "❌ 无");
      console.log("[Auth Store] User:", this.user);

      if (this.token && isJWT(this.token)) {
        console.log("[Auth Store] ✅ 检测到JWT Token");

        const jwtUser = getCurrentUserFromJWT();
        if (jwtUser && !this.user) {
          this.user = jwtUser;
          console.log("[Auth Store] ✅ 从JWT恢复用户信息:", jwtUser.username || jwtUser.id);
        }

        if (isJWTExpired(this.token)) {
          console.warn("[Auth Store] ⚠️ JWT已过期！");
        } else {
          console.log("[Auth Store] ✅ JWT有效");
        }

        console.log("[Auth Store] JWT角色:", this.roles);
      } else if (this.token) {
        console.warn("[Auth Store] ⚠️ Token不是JWT格式（可能是UUID）");
      } else {
        console.log("[Auth Store] ❌ 无Token，未登录");
      }
    },

    async login(payload) {
      const result = await login(payload);

      if (result.token) {
        this.token = result.token;
        console.log("[Auth Store] ✅ Token已设置到Store");

        if (isJWT(result.token)) {
          console.log("[Auth Store] ✅ Token是有效的JWT格式");
        } else {
          console.warn("[Auth Store] ⚠️ Token不是JWT格式");
        }
      }

      if (result.user) {
        this.user = result.user;
        console.log(
          "[Auth Store] ✅ 用户信息已设置到Store, 角色:",
          result.user.roles
        );
      }

      console.log("[Auth Store] 登录完成 - isLoggedIn:", this.isLoggedIn, "roles:", this.roles);
      console.log("[Auth Store] isJWTToken:", this.isJWTToken, "isTokenExpired:", this.isTokenExpired);

      return result;
    },

    async register(payload) {
      const result = await sdkRegister(payload);
      return result;
    },

    logout() {
      sdkLogout();
      this.token = "";
      this.user = null;
      console.log("[Auth Store] 已登出");
    },
  },
});
