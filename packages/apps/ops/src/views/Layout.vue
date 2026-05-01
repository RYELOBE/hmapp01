<template>
  <div class="ops-layout" :class="{ 'ops-layout--qiankun': isQiankunMode }">
    <header v-if="!isQiankunMode" class="ops-header">
      <div class="ops-header__brand" @click="$router.push('/ops/dashboard')">
        <icon-fire class="brand-icon" />
        <span class="brand-text">运营中心</span>
      </div>
      
      <div class="ops-header__right">
        <a-dropdown @select="handleUserMenuSelect">
          <div class="user-info">
            <a-avatar :size="32" class="user-avatar">
              <icon-user />
            </a-avatar>
            <div class="user-detail">
              <span class="user-name">{{ currentUser?.username || currentUser?.name || '管理员' }}</span>
              <a-tag color="orange" size="small">运营管理员</a-tag>
            </div>
            <icon-down class="dropdown-arrow" />
          </div>
          <template #content>
            <a-doption value="profile">
              <template #icon><icon-user /></template>
              个人中心
            </a-doption>
            <a-doption value="settings">
              <template #icon><icon-settings /></template>
              账号设置
            </a-doption>
            <a-divider />
            <a-doption value="logout" class="logout-option">
              <template #icon><icon-export /></template>
              退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </header>

    <div class="ops-body">
      <aside v-if="showLeftMenu($route)" class="ops-sider">
        <a-menu
          :selected-keys="[activeKey]"
          :default-selected-keys="['/ops/dashboard']"
          @menu-item-click="navigate"
          class="ops-menu"
        >
          <template v-for="item in menuConfig" :key="item.path">
            <a-menu-item :key="item.path">
              <template #icon>
                <component :is="item.iconComponent" />
              </template>
              <span>{{ item.label }}</span>
              <template v-if="item.badge" #extra>
                <a-badge :count="item.badge" :max-count="99" />
              </template>
            </a-menu-item>
          </template>
        </a-menu>
      </aside>

      <main class="ops-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconFire,
  IconUser,
  IconDown,
  IconSettings,
  IconExport,
  IconDashboard,
  IconCheckCircle,
  IconShoppingCart,
  IconUserGroup,
  IconShop,
  IconApp,
  IconEdit,
  IconSafe,
  IconMenu,
} from '@arco-design/web-vue/es/icon';
import { getOpsCurrentUser as getCurrentUser, opsLogout as logoutSdk } from "commonprovide/auth-sdk";
import { qiankunWindow } from 'vite-plugin-qiankun/dist/helper';

const isQiankunMode = qiankunWindow.__POWERED_BY_QIANKUN__;
const router = useRouter();
const route = useRoute();
const currentUser = getCurrentUser();

const activeKey = computed(() => {
  const path = route.path;
  const menuPath = menuConfig.find(m => path.startsWith(m.path));
  return menuPath ? menuPath.path : path;
});

const menuConfig = [
  { 
    path: '/ops/dashboard', 
    label: '数据概览', 
    iconComponent: IconDashboard,
  },
  { 
    path: '/ops/reviews', 
    label: '商品审核', 
    iconComponent: IconCheckCircle,
    badge: 0,
  },
  { 
    path: '/ops/orders', 
    label: '订单监控', 
    iconComponent: IconShoppingCart,
  },
  { 
    path: '/ops/vendor', 
    label: '供方管理', 
    iconComponent: IconUserGroup,
  },
  { 
    path: '/ops/buyer', 
    label: '需方管理', 
    iconComponent: IconShop,
  },
  { 
    path: '/ops/app-register', 
    label: '子应用管理', 
    iconComponent: IconApp,
  },
  { 
    path: '/ops/portal-design', 
    label: '门户设计', 
    iconComponent: IconEdit,
  },
  { 
    path: '/ops/role-manage', 
    label: '角色管理', 
    iconComponent: IconSafe,
  },
  { 
    path: '/ops/route-manage', 
    label: '路由配置', 
    iconComponent: IconMenu,
  },
];

function showLeftMenu(r) {
  if (r.meta?.hideMenu !== undefined) return !r.meta.hideMenu;
  return true;
}

function navigate(path) {
  router.push(path);
}

function handleUserMenuSelect(value) {
  switch (value) {
    case 'logout':
      logoutSdk();
      Message.success('已退出登录');
      router.push('/login');
      break;
    case 'profile':
      Message.info('个人中心开发中');
      break;
    case 'settings':
      Message.info('账号设置开发中');
      break;
  }
}
</script>

<style lang="scss" scoped>
.ops-layout {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6f7;

  &--qiankun {
    height: 100%;
  }
}

.ops-header {
  height: var(--ops-header-height, 56px);
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.2);
  position: relative;
  z-index: 100;

  &__brand {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.9;
    }

    .brand-icon {
      font-size: 28px;
      color: #fff;
    }

    .brand-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      letter-spacing: 1px;
    }
  }

  &__right {
    display: flex;
    align-items: center;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .user-avatar {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }

  .user-detail {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;

    .user-name {
      font-size: 14px;
      color: #fff;
      font-weight: 500;
    }

    :deep(.arco-tag) {
      font-size: 10px;
      padding: 0 4px;
      height: 16px;
      line-height: 16px;
    }
  }

  .dropdown-arrow {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.ops-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.ops-sider {
  width: var(--ops-sider-width, 220px);
  background: #fff;
  border-right: 1px solid #e5e6eb;
  overflow-y: auto;
  flex-shrink: 0;
}

.ops-menu {
  border-right: none;
  padding: 12px 8px;

  :deep(.arco-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 4px 0;
    border-radius: 8px;
    padding: 0 16px;
    font-size: 14px;

    &:hover {
      background: #f2f3f5;
    }

    &.arco-menu-selected {
      background: #e6f1ff;
      color: #165dff;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 8px;
        bottom: 8px;
        width: 3px;
        background: #165dff;
        border-radius: 0 3px 3px 0;
      }
    }
  }

  :deep(.arco-menu-icon) {
    margin-right: 10px;
    font-size: 16px;
  }
}

.ops-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f6f7;
}

.logout-option {
  color: #f53f3f;
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
