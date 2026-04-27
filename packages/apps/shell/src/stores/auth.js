import { defineStore } from "pinia";
import { getCurrentUser, getToken, logout as sdkLogout, login } from "commonprovide/auth-sdk";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: "",
    user: null,
  }),
  getters: {
    roles: (state) => state.user?.roles || [],
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    hydrate() {
      this.token = getToken();
      this.user = getCurrentUser();
    },
    async login(payload) {
      const result = await login(payload);
      this.token = result.token;
      this.user = result.user;
      return result;
    },
    logout() {
      sdkLogout();
      this.token = "";
      this.user = null;
    },
  },
});
