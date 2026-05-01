<template>
  <div class="portal-layout" :class="{ 'portal-layout--qiankun': isQiankunMode }">
    <!-- 顶部导航栏 -->
    <header v-if="!isQiankunMode" class="portal-nav">
      <div class="portal-nav__inner">
        <!-- Logo -->
        <div class="portal-nav__brand" @click="$router.push('/home')">
          <span class="portal-nav__logo">🎓</span>
          <span class="portal-nav__title">校园集市</span>
        </div>

        <!-- 搜索框 -->
        <div class="portal-nav__search">
          <a-input-search
            v-model="searchKeyword"
            placeholder="搜索你想要的宝贝..."
            search-button
            size="large"
            @search="handleSearch"
            @press-enter="handleSearch"
          />
        </div>

        <!-- 导航链接 -->
        <nav class="portal-nav__links">
          <a
            v-for="link in navLinks"
            :key="link.path"
            class="portal-nav__link"
            :class="{ 'portal-nav__link--active': isActive(link.path) }"
            @click="navigate(link.path)"
          >
            <span class="portal-nav__link-icon">{{ link.iconEmoji }}</span>
            <span>{{ link.label }}</span>
          </a>
        </nav>

        <!-- 用户区 -->
        <div class="portal-nav__user">
          <a-dropdown trigger="hover">
            <div class="portal-nav__avatar">
              <a-avatar :size="32">{{ currentUser?.username?.[0] || 'U' }}</a-avatar>
              <span class="portal-nav__username">{{ currentUser?.username || '用户' }}</span>
            </div>
            <template #content>
              <a-doption @click="$router.push('/orders')">
                <template #icon><icon-list /></template>我的订单
              </a-doption>
              <a-doption v-if="isSeller" @click="$router.push('/seller/items')">
                <template #icon><icon-subscription /></template>卖家中心
              </a-doption>
              <a-doption v-if="isSeller" @click="$router.push('/seller/stats')">
                <template #icon><icon-chart /></template>销售统计
              </a-doption>
              <a-doption @click="$router.push('/addresses')">
                <template #icon><icon-location /></template>收货地址
              </a-doption>
              <a-doption v-if="isBuyer" @click="$router.push('/favorites')">
                <template #icon><icon-heart /></template>我的收藏
              </a-doption>
              <a-doption @click="logout" class="portal-nav__logout">
                <template #icon><icon-export /></template>退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="portal-content">
      <router-view></router-view>
    </main>

    <!-- 底部 -->
    <footer v-if="!isQiankunMode" class="portal-footer">
      <p>© 2026 校园集市 — 让闲置转起来 ♻️</p>
    </footer>

    <!-- 迷你购物车 -->
    <MiniCart v-if="isBuyer" ref="miniCartRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconList,
  IconPlus,
  IconExport,
  IconSubscription,
  IconChart,
  IconLocation,
  IconHeart,
} from "@arco-design/web-vue/es/icon";
import { getCurrentUser, logout as logoutSdk, onUserChange } from "commonprovide/auth-sdk";
import { qiankunWindow } from "vite-plugin-qiankun/dist/helper";
import MiniCart from "../components/MiniCart.vue";

const isQiankunMode = qiankunWindow.__POWERED_BY_QIANKUN__;
const router = useRouter();
const route = useRoute();

const currentUser = ref(getCurrentUser());
const searchKeyword = ref("");
const miniCartRef = ref(null);

let unwatch = null;
onMounted(() => {
  unwatch = onUserChange((user) => { currentUser.value = user; });
});
onUnmounted(() => { unwatch?.(); });

const isSeller = computed(() => {
  const roles = currentUser.value?.roles || [];
  return roles.includes("SELLER");
});

const isBuyer = computed(() => {
  const roles = currentUser.value?.roles || [];
  return roles.includes("BUYER");
});

const navLinks = computed(() => {
  const roles = currentUser.value?.roles || [];
  const links = [
    { path: "/home", label: "首页", iconEmoji: "🏠" },
  ];
  if (roles.includes("BUYER")) {
    links.push({ path: "/buyer/items", label: "淘好物", iconEmoji: "🔍" });
    links.push({ path: "/cart", label: "购物车", iconEmoji: "🛒" });
  }
  if (roles.includes("SELLER")) {
    links.push({ path: "/seller/items", label: "我的闲置", iconEmoji: "📦" });
  }
  links.push({ path: "/orders", label: "订单", iconEmoji: "✅" });
  return links;
});

function isActive(path) {
  return route.path.startsWith(path);
}

function navigate(path) {
  router.push(path);
}

function handleSearch(val) {
  if (val.trim()) {
    router.push({ path: "/home", query: { keyword: val.trim() } });
  }
}

function logout() {
  logoutSdk();
  router.push("/login");
  Message.success("已退出登录");
}
</script>

<style lang="scss" scoped>
$portal-primary: #0fc6c2;
$portal-primary-light: #e8faf9;
$portal-orange: #f0a838;
$portal-bg: #f5f5f5;
$portal-card: #ffffff;
$portal-text: #1d2129;
$portal-text-secondary: #4e5969;
$portal-text-muted: #86909c;
$portal-nav-height: 64px;

.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $portal-bg;

  &--qiankun .portal-content {
    min-height: 100%;
  }
}

// ── 顶部导航 ────────────────────────────
.portal-nav {
  position: sticky;
  top: 0;
  z-index: 999;
  background: $portal-card;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    height: $portal-nav-height;
    padding: 0 24px;
    display: flex;
    align-items: center;
    gap: 24px;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    flex-shrink: 0;
  }

  &__logo {
    font-size: 28px;
  }

  &__title {
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  &__search {
    flex: 1;
    max-width: 480px;

    :deep(.arco-input-search) {
      border-radius: 24px;
      border: 2px solid #e5e6eb;
      transition: border-color 0.2s;

      &:focus-within { border-color: #7c3aed; }
    }
  }

  &__links {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  &__link {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 14px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: $portal-text-secondary;
    cursor: pointer;
    transition: all 0.2s;

    &-icon { font-size: 16px; }

    &:hover {
      color: #7c3aed;
      background: rgba(124, 58, 237, 0.06);
    }

    &--active {
      color: #7c3aed;
      background: rgba(124, 58, 237, 0.06);
      font-weight: 600;
    }
  }

  &__user {
    flex-shrink: 0;
  }

  &__avatar {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 20px;
    transition: background 0.2s;

    &:hover { background: #f2f3f5; }
  }

  &__username {
    font-size: 14px;
    color: $portal-text;
  }

  &__logout {
    color: #f53f3f !important;
  }
}

// ── 内容区 ────────────────────────────
.portal-content {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 24px;
}

// ── 底部 ────────────────────────────
.portal-footer {
  text-align: center;
  padding: 20px;
  color: $portal-text-muted;
  font-size: 13px;
  border-top: 1px solid #e5e6eb;
  background: $portal-card;
}
</style>
