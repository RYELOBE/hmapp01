<template>
  <div class="portal-layout" :class="{ 'portal-layout--qiankun': isQiankunMode }">
    <header v-if="!isQiankunMode" class="portal-nav">
      <div class="portal-nav__inner">
        <div class="portal-nav__brand" @click="$router.push('/home')">
          <span class="portal-nav__logo">🎓</span>
          <span class="portal-nav__title">校园二手平台</span>
        </div>

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
                <template #icon><icon-notification /></template>卖家中心
              </a-doption>
              <a-doption v-if="isSeller" @click="$router.push('/seller/stats')">
                <template #icon><icon-apps /></template>销售统计
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

    <main class="portal-content">
      <router-view></router-view>
    </main>

    <footer v-if="!isQiankunMode" class="portal-footer">
      <div class="portal-footer__inner">
        <p class="portal-footer__copyright">© 2026 校园二手平台 — 让闲置转起来 ♻️</p>
        <p class="portal-footer__links">
          <a href="javascript:void(0)">关于我们</a>
          <span class="portal-footer__divider">|</span>
          <a href="javascript:void(0)">使用帮助</a>
          <span class="portal-footer__divider">|</span>
          <a href="javascript:void(0)">联系客服</a>
        </p>
      </div>
    </footer>

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
  IconNotification,
  IconApps,
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
:root {
  --shadow-nav: 0 2px 8px rgba(0, 0, 0, 0.04);
}

$primary-color: #165DFF;
$bg-color: #F5F6F7;
$card-bg: #FFFFFF;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-muted: #86909C;
$nav-height: 64px;

.portal-layout {
  display: flex;
  flex-direction: column;
  background: $bg-color;

  &:not(.portal-layout--qiankun) {
    min-height: 100vh;
  }
}

.portal-nav {
  position: sticky;
  top: 0;
  z-index: 999;
  background: $card-bg;
  box-shadow: var(--shadow-nav);

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    height: $nav-height;
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
    color: $primary-color;
  }

  &__search {
    flex: 1;
    max-width: 480px;

    :deep(.arco-input-search) {
      border-radius: 24px;
      border: 2px solid #E5E6EB;
      transition: border-color 0.2s ease;

      &:focus-within {
        border-color: $primary-color;
      }
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
    color: $text-secondary;
    cursor: pointer;
    transition: all 0.2s ease;

    &-icon {
      font-size: 16px;
    }

    &:hover {
      color: $primary-color;
      background: rgba(22, 93, 255, 0.06);
    }

    &--active {
      color: $primary-color;
      background: rgba(22, 93, 255, 0.06);
      font-weight: 700;
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
    transition: background 0.2s ease;

    &:hover {
      background: #F2F3F5;
    }
  }

  &__username {
    font-size: 14px;
    color: $text-primary;
  }

  &__logout {
    color: #F53F3F !important;
  }
}

.portal-content {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 24px;
}

.portal-footer {
  background: $card-bg;
  border-top: 1px solid #E5E6EB;
  padding: 24px 0;

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 24px;
    text-align: center;
  }

  &__copyright {
    margin: 0 0 8px;
    font-size: 14px;
    color: $text-muted;
  }

  &__links {
    margin: 0;
    font-size: 13px;
    color: $text-secondary;

    a {
      color: $text-secondary;
      text-decoration: none;
      transition: color 0.2s ease;

      &:hover {
        color: $primary-color;
      }
    }
  }

  &__divider {
    margin: 0 12px;
    color: #E5E6EB;
  }
}
</style>
