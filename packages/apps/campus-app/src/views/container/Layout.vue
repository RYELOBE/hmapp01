<template>
  <div class="app-layout">
    <a-layout class="layout-wrapper">
      <a-layout-sider
        v-if="showSidebar"
        :collapsible="true"
        :collapsed="collapsed"
        class="sidebar"
      >
        <div class="logo">
          <span v-if="!collapsed">CampusTrade</span>
          <span v-else>CT</span>
        </div>
        <a-menu
          :selected-keys="[selectedKey]"
          :open-keys="openKeys"
          mode="inline"
          class="sidebar-menu"
          @menu-item-click="handleMenuClick"
        >
          <template v-for="menu in menuItems" :key="menu.key">
            <a-menu-item 
              v-if="!menu.children" 
              :key="menu.key"
            >
              <component :is="menu.icon" />
              <span>{{ menu.label }}</span>
            </a-menu-item>
            <a-sub-menu v-else :key="menu.key">
              <template #title>
                <component :is="menu.icon" />
                <span>{{ menu.label }}</span>
              </template>
              <a-menu-item 
                v-for="child in menu.children" 
                :key="child.key"
              >
                <span>{{ child.label }}</span>
              </a-menu-item>
            </a-sub-menu>
          </template>
        </a-menu>
      </a-layout-sider>
      <a-layout>
        <a-layout-header class="header">
          <div class="header-left">
            <a-button
              v-if="showSidebar"
              type="text"
              @click="collapsed = !collapsed"
              class="toggle-btn"
            >
              <component :is="collapsed ? IconMenu : IconMenuFold" />
            </a-button>
            <span class="title">{{ pageTitle }}</span>
          </div>
          <div class="header-right">
            <a-dropdown>
              <a-button type="text" class="user-btn">
                <IconUser />
                <span>{{ authStore.user?.username || '用户' }}</span>
                <IconCaretDown />
              </a-button>
              <template #content>
                <a-dropdown-menu>
                  <a-dropdown-item @click="handleLogout">退出登录</a-dropdown-item>
                </a-dropdown-menu>
              </template>
            </a-dropdown>
          </div>
        </a-layout-header>
        <a-layout-content class="content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  IconMenu,
  IconMenuFold,
  IconUser,
  IconCaretDown,
  IconHome,
  IconList,
  IconBarChart,
} from "@arco-design/web-vue/es/icon";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const collapsed = ref(false);
const openKeys = ref(['ops-users', 'ops-orders', 'ops-items', 'ops-circles', 'ops-review-manage', 'portal', 'seller']);

const showSidebar = computed(() => {
  return !["login", "forbidden"].includes(route.name);
});

const pageTitle = computed(() => {
  return route.meta?.title || "CampusTrade";
});

// 映射：路径 -> 菜单key（包含父级key）
const menuKeyMap = {
  '/ops/dashboard': 'ops-dashboard',
  '/ops/users/vendor-manage': 'ops-vendor',
  '/ops/users/buyer-manage': 'ops-buyer',
  '/ops/users/user-manage': 'ops-user-manage',
  '/ops/orders/list': 'ops-orders-list',
  '/ops/orders/review': 'ops-order-review',
  '/ops/items/manage': 'ops-items-manage',
  '/ops/items/review': 'ops-reviews',
  '/ops/circles/manage': 'ops-circle',
  '/ops/circles/review': 'ops-circle-review',
  '/ops/reviews/manage': 'ops-review-manage-list',
  '/ops/reviews/audit': 'ops-review-audit',
  '/ops/messages/center': 'ops-messages',
  '/portal/home': 'portal-home',
  '/portal/orders': 'portal-orders',
  '/portal/cart': 'portal-cart',
  '/portal/favorites': 'portal-favorites',
  '/portal/addresses': 'portal-addresses',
  '/portal/seller/publish': 'seller-publish',
  '/portal/seller/items': 'seller-items',
  '/portal/seller/orders': 'seller-orders',
  '/portal/seller/stats': 'seller-stats',
};

const selectedKey = computed(() => {
  const path = route.path;
  return menuKeyMap[path] || route.name || 'home';
});

// 查找所有菜单项（包括子菜单）的完整列表
function flattenMenuItems(items) {
  const result = [];
  items.forEach(item => {
    if (item.children) {
      result.push(...flattenMenuItems(item.children));
    } else {
      result.push(item);
    }
  });
  return result;
}

function handleMenuClick(key) {
  const allItems = flattenMenuItems(menuItems.value);
  const menuItem = allItems.find(item => item.key === key);
  
  if (menuItem && menuItem.path) {
    router.push(menuItem.path).catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        console.error('[Menu Error] 导航失败:', err);
      }
    });
  } else if (key === 'home') {
    router.push('/').catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        console.error('[Menu Error] 导航失败:', err);
      }
    });
  }
}

const menuItems = computed(() => {
  const items = [];
  const roles = authStore.roles;

  items.push({
    key: "home",
    label: "首页",
    icon: IconHome,
    path: "/",
  });

  if (roles.includes("BUYER") || roles.includes("SELLER")) {
    items.push({
      key: "portal",
      label: "交易中心",
      icon: IconList,
      children: [
        { key: "portal-home", label: "首页", path: "/portal/home" },
        { key: "portal-orders", label: "我的订单", path: "/portal/orders" },
        { key: "portal-cart", label: "购物车", path: "/portal/cart", roles: ["BUYER"] },
        { key: "portal-favorites", label: "收藏夹", path: "/portal/favorites", roles: ["BUYER"] },
        { key: "portal-addresses", label: "收货地址", path: "/portal/addresses" },
      ].filter(item => !item.roles || item.roles.some(r => roles.includes(r))),
    });
  }

  if (roles.includes("SELLER")) {
    items.push({
      key: "seller",
      label: "卖家中心",
      icon: IconBarChart,
      children: [
        { key: "seller-publish", label: "发布商品", path: "/portal/seller/publish" },
        { key: "seller-items", label: "我的商品", path: "/portal/seller/items" },
        { key: "seller-orders", label: "我的订单", path: "/portal/seller/orders" },
        { key: "seller-stats", label: "数据统计", path: "/portal/seller/stats" },
      ],
    });
  }

  if (roles.includes("OPS")) {
    items.push({
      key: "ops-dashboard",
      label: "数据概览",
      icon: IconBarChart,
      path: "/ops/dashboard",
    });
    items.push({
      key: "ops-users",
      label: "用户管理",
      icon: IconList,
      children: [
        { key: "ops-vendor", label: "卖家管理", path: "/ops/users/vendor-manage" },
        { key: "ops-buyer", label: "买家管理", path: "/ops/users/buyer-manage" },
        { key: "ops-user-manage", label: "用户管理", path: "/ops/users/user-manage" },
      ],
    });
    items.push({
      key: "ops-orders",
      label: "订单管理",
      icon: IconList,
      children: [
        { key: "ops-orders-list", label: "订单列表", path: "/ops/orders/list" },
        { key: "ops-order-review", label: "订单审核", path: "/ops/orders/review" },
      ],
    });
    items.push({
      key: "ops-items",
      label: "商品管理",
      icon: IconList,
      children: [
        { key: "ops-items-manage", label: "商品管理", path: "/ops/items/manage" },
        { key: "ops-reviews", label: "商品审核", path: "/ops/items/review" },
      ],
    });
    items.push({
      key: "ops-circles",
      label: "圈子管理",
      icon: IconList,
      children: [
        { key: "ops-circle", label: "圈子管理", path: "/ops/circles/manage" },
        { key: "ops-circle-review", label: "圈子审核", path: "/ops/circles/review" },
      ],
    });
    items.push({
      key: "ops-review-manage",
      label: "评价管理",
      icon: IconList,
      children: [
        { key: "ops-review-manage-list", label: "评价管理", path: "/ops/reviews/manage" },
        { key: "ops-review-audit", label: "评价审核", path: "/ops/reviews/audit" },
      ],
    });
    items.push({
      key: "ops-messages",
      label: "消息",
      icon: IconList,
      path: "/ops/messages/center",
    });
  }

  return items;
});

function handleLogout() {
  authStore.logout();
  router.push("/login");
}
</script>

<style scoped>
.app-layout {
  height: 100%;
}

.layout-wrapper {
  height: 100%;
}

.sidebar {
  background: #001529;
}

.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
}

/* 左侧菜单激活态 - 蓝色高亮 */
:deep(.arco-menu-item-active),
:deep(.arco-menu-inline-item-active) {
  color: #165DFF !important;
  background-color: rgba(22, 93, 255, 0.1) !important;
  border-right: 3px solid #165DFF !important;
  font-weight: 500;
}

:deep(.arco-menu-item:hover) {
  background-color: rgba(22, 93, 255, 0.05);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-btn {
  font-size: 20px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
