<template>
  <div class="shell-layout">
    <!-- 统一导航栏 -->
    <header class="shell-nav">
      <div class="shell-nav__inner">
        <div class="shell-nav__brand" @click="$router.push('/')">
          <span class="shell-nav__logo">🎓</span>
          <span class="shell-nav__title">校园二手平台</span>
        </div>

        <div class="shell-nav__links">
          <a
            v-for="link in navLinks"
            :key="link.path"
            class="shell-nav__link"
            :class="{ 'shell-nav__link--active': isActive(link.path) }"
            @click="navigate(link.path)"
          >
            <span class="shell-nav__link-icon">{{ link.iconEmoji }}</span>
            <span>{{ link.label }}</span>
          </a>
        </div>

        <div class="shell-nav__user" v-if="authStore.isLoggedIn">
          <a-dropdown trigger="hover">
            <div class="shell-nav__avatar">
              <a-avatar :size="32">{{
                authStore.user?.username?.[0] || "U"
              }}</a-avatar>
              <span class="shell-nav__username">{{
                authStore.user?.username || "用户"
              }}</span>
            </div>
            <template #content>
              <a-doption @click="logout">
                <template #icon><icon-export /></template>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
    </header>

    <!-- 子路由视图：渲染 ShellHomeView、MicroView 等 -->
    <main class="shell-content">
      <router-view />
    </main>

    <!-- 全局组件 -->
    <AppMain />
    <ToAppMain v-if="authStore.isLoggedIn" />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { IconExport } from "@arco-design/web-vue/es/icon";
import AppMain from "./AppMain.vue";
import ToAppMain from "./ToAppMain.vue";
import { useAuthStore } from "../../stores/auth";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const navLinks = computed(() => {
  const roles = authStore.roles || [];
  const links = [];

  links.push({ path: "/portal/home", label: "首页", iconEmoji: "🏠" });

  if (roles.includes("BUYER")) {
    links.push({
      path: "/portal/buyer/items",
      label: "淘好物",
      iconEmoji: "🔍",
    });
  }

  if (roles.includes("SELLER")) {
    links.push({
      path: "/portal/seller/items",
      label: "我的闲置",
      iconEmoji: "📦",
    });
  }

  links.push({ path: "/portal/orders", label: "订单", iconEmoji: "✅" });
  links.push({ path: "/ops", label: "运营中心", iconEmoji: "⚙️" });

  return links;
});

function isActive(path) {
  return route.path.startsWith(path);
}

function navigate(path) {
  router.push(path);
}

function logout() {
  authStore.logout();
  router.push("/login");
}
</script>

<style lang="scss" scoped>
:root {
  --shadow-nav: 0 2px 8px rgba(0, 0, 0, 0.04);
}

$primary-color: #165dff;
$bg-color: #f5f6f7;
$card-bg: #ffffff;
$text-primary: #1d2129;
$text-secondary: #4e5969;
$text-muted: #86909c;
$nav-height: 64px;

.shell-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: $bg-color;
}

.shell-nav {
  position: sticky;
  top: 0;
  z-index: 999;
  background: $card-bg;
  box-shadow: var(--shadow-nav);
}

.shell-nav__inner {
  max-width: 1400px;
  margin: 0 auto;
  height: $nav-height;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.shell-nav__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.shell-nav__logo {
  font-size: 28px;
}

.shell-nav__title {
  font-size: 20px;
  font-weight: 700;
  color: $primary-color;
}

.shell-nav__links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.shell-nav__link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.2s ease;
}

.shell-nav__link-icon {
  font-size: 16px;
}

.shell-nav__link:hover {
  color: $primary-color;
  background: rgba(22, 93, 255, 0.06);
}

.shell-nav__link--active {
  color: $primary-color;
  background: rgba(22, 93, 255, 0.06);
  font-weight: 700;
}

.shell-nav__user {
  flex-shrink: 0;
}

.shell-nav__avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s ease;
}

.shell-nav__avatar:hover {
  background: #f2f3f5;
}

.shell-nav__username {
  font-size: 14px;
  color: $text-primary;
}

.shell-content {
  flex: 1;
  width: 100%;
}
</style>
