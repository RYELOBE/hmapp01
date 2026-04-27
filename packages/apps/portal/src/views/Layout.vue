<template>
  <div class="portal-main" :class="{ 'portal-main--qiankun': isQiankunMode }">
    <div v-if="!isQiankunMode" class="portal-main_header">
      <div class="brand" @click="$router.push('/buyer/home')">Campus Trade</div>
      <a-space>
        <a-tag color="arcoblue">{{ currentRole }}</a-tag>
        <a-button type="text" status="danger" @click="logout">退出</a-button>
      </a-space>
    </div>
    <div class="portal-main_body">
      <a-menu
        v-if="showLeftMenu($route)"
        class="portal-main_body_sider"
        :selected-keys="[activeKey]"
        @menu-item-click="navigate"
      >
        <a-menu-item
          v-for="item in filteredMenuConfig"
          :key="item.path"
        >
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
      <div class="portal-main_body_content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getCurrentUser, logout as logoutSdk, onUserChange } from "commonprovide/auth-sdk";
import { qiankunWindow } from 'vite-plugin-qiankun/dist/helper';

const isQiankunMode = qiankunWindow.__POWERED_BY_QIANKUN__;

const router = useRouter();
const route = useRoute();

const currentUser = ref(getCurrentUser());

// 监听用户登录/登出变化，保持 Layout 与 auth 状态同步
let unwatch = null;
onMounted(() => {
  unwatch = onUserChange((user) => {
    currentUser.value = user;
  });
});
onUnmounted(() => {
  unwatch?.();
});

const currentRole = computed(() => {
  const roles = currentUser.value?.roles || [];
  if (roles.includes('SELLER')) return '卖家';
  if (roles.includes('BUYER')) return '买家';
  return '用户';
});

const activeKey = computed(() => route.path);

const menuConfig = [
  { path: '/buyer/home', label: '买家首页', roles: ['BUYER'] },
  { path: '/buyer/items', label: '商品列表', roles: ['BUYER'] },
  { path: '/seller/center', label: '卖家中心', roles: ['SELLER'] },
  { path: '/seller/publish', label: '发布商品', roles: ['SELLER'] },
  { path: '/seller/items', label: '我的商品', roles: ['SELLER'] },
  { path: '/orders', label: '我的订单', roles: ['BUYER', 'SELLER'] }
];

const filteredMenuConfig = computed(() => {
  const userRoles = currentUser.value?.roles || [];
  return menuConfig.filter(item => {
    if (!item.roles || !Array.isArray(item.roles)) return false;
    return item.roles.some(r => userRoles.includes(r));
  });
});

function showLeftMenu(route) {
  if (route.meta.hideMenu !== undefined) {
    return !route.meta.hideMenu;
  }
  return true;
}

function navigate(path) {
  router.push(path);
}

function logout() {
  logoutSdk();
  router.push('/login');
  Message.success('已退出登录');
}
</script>

<style lang="scss" scoped>
.portal-main {
  width: 100%;
  height: 100vh;

  &--qiankun &_body {
    height: 100%;
  }

  &_header {
    width: 100%;
    height: 48px;
    background: linear-gradient(135deg, #336ad8 0%, #4a7feb 100%);
    box-sizing: border-box;
    display: flex;
    align-items: center;
    flex-flow: row nowrap;
    justify-content: space-between;
    box-shadow: 0 2px 8px rgba(51, 106, 216, 0.2);
    position: relative;
    z-index: 999;

    .brand {
      padding: 0 20px;
      font-size: 18px;
      font-weight: bold;
      color: #fff;
      cursor: pointer;
    }
  }

  &_body {
    display: flex;
    height: calc(100% - 48px);

    &_content {
      flex: 1;
      overflow: auto;
      padding: 20px;
      background: #f5f8ff;
    }

    &_sider {
      width: 220px;
      background-color: #fff;
      height: 100%;
      overflow: auto;
      border-right: 1px solid #e5efff;
    }
  }
}
</style>