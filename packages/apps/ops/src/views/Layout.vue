<template>
  <div class="ops-main" :class="{ 'ops-main--qiankun': isQiankunMode }">
    <div v-if="!isQiankunMode" class="ops-main_header">
      <div class="brand" @click="$router.push('/ops/dashboard')">运营中心</div>
      <a-space>
        <a-tag color="orange">运营管理员</a-tag>
        <a-button type="text" status="danger" @click="logout">退出</a-button>
      </a-space>
    </div>
    <div class="ops-main_body">
      <a-menu
        v-if="showLeftMenu($route)"
        class="ops-main_body_sider"
        :selected-keys="[activeKey]"
        @menu-item-click="navigate"
      >
        <a-menu-item
          v-for="item in menuConfig"
          :key="item.path"
        >
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
      <div class="ops-main_body_content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getCurrentUser, logout as logoutSdk } from "commonprovide/auth-sdk";
import { qiankunWindow } from 'vite-plugin-qiankun/dist/helper';

const isQiankunMode = qiankunWindow.__POWERED_BY_QIANKUN__;

const router = useRouter();
const route = useRoute();

const currentUser = getCurrentUser();
const activeKey = computed(() => route.path);

const menuConfig = [
  { path: '/ops/dashboard', label: '数据概览' },
  { path: '/ops/reviews', label: '商品审核' },
  { path: '/ops/orders', label: '订单监控' },
  { path: '/ops/app-register', label: '子应用管理' },
  { path: '/ops/portal-design', label: '门户设计' },
  { path: '/ops/role-manage', label: '角色管理' }
];

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
.ops-main {
  width: 100%;
  height: 100vh;

  &--qiankun &_body {
    height: 100%;
  }

  &_header {
    width: 100%;
    height: 48px;
    background: linear-gradient(135deg, #d88c1f 0%, #f0a838 100%);
    box-sizing: border-box;
    display: flex;
    align-items: center;
    flex-flow: row nowrap;
    justify-content: space-between;
    box-shadow: 0 2px 8px rgba(216, 140, 31, 0.2);
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
      background: #fff5e6;
    }

    &_sider {
      width: 220px;
      background-color: #fff;
      height: 100%;
      overflow: auto;
      border-right: 1px solid #ffe8c9;
    }
  }
}
</style>
