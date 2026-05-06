<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="logo">
        <icon-common style="font-size: 24px; margin-right: 8px;" />
        <span>校园二手交易</span>
      </div>
      <a-menu
        :default-selected-keys="[currentMenu]"
        class="sidebar-menu"
        @menu-item-click="handleMenuClick"
      >
        <a-menu-item key="dashboard">
          <template #icon>
            <icon-dashboard />
          </template>
          控制台
        </a-menu-item>
        <a-menu-item key="users">
          <template #icon>
            <icon-user />
          </template>
          用户管理
        </a-menu-item>
        <a-menu-item key="products">
          <template #icon>
            <icon-app />
          </template>
          商品管理
        </a-menu-item>
        <a-menu-item key="orders">
          <template #icon>
          </template>
          订单管理
        </a-menu-item>
        <a-menu-item key="categories">
          <template #icon>
            <icon-menu />
          </template>
          分类管理
        </a-menu-item>
      </a-menu>
    </div>
    <div class="main-wrapper">
      <div class="header">
        <div class="header-left">
          <a-breadcrumb>
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ breadcrumbMap[currentMenu] }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-dropdown trigger="click">
            <div class="user-info">
              <a-avatar :size="32">
                <icon-user />
              </a-avatar>
              <span class="username">管理员</span>
              <icon-down />
            </div>
            <template #content>
              <a-doption>个人中心</a-doption>
              <a-doption>账号设置</a-doption>
              <a-doption @click="handleLogout">退出登录</a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const currentMenu = ref('dashboard')

const breadcrumbMap = {
  dashboard: '控制台',
  users: '用户管理',
  products: '商品管理',
  orders: '订单管理',
  categories: '分类管理'
}

const handleMenuClick = (key) => {
  currentMenu.value = key
  const routeMap = {
    dashboard: '/admin/dashboard',
    users: '/admin/users',
    products: '/admin/products',
    orders: '/admin/orders',
    categories: '/admin/categories'
  }
  router.push(routeMap[key])
}

const handleLogout = () => {
  router.push('/login')
}

watch(
  () => route.path,
  (newPath) => {
    const pathMap = {
      '/admin/dashboard': 'dashboard',
      '/admin/users': 'users',
      '/admin/products': 'products',
      '/admin/orders': 'orders',
      '/admin/categories': 'categories'
    }
    currentMenu.value = pathMap[newPath] || 'dashboard'
  },
  { immediate: true }
)
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f6f7;
}

.sidebar {
  width: 220px;
  background-color: #1d2129;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  left: 0;
  top: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-menu {
  flex: 1;
  background-color: #1d2129;
  border-right: none;
  margin-top: 8px;
}

:deep(.arco-menu-inner) {
  padding: 8px;
}

:deep(.arco-menu-item) {
  color: rgba(255, 255, 255, 0.65);
  border-radius: 6px;
  margin-bottom: 4px;
}

:deep(.arco-menu-item:hover) {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.08);
}

:deep(.arco-menu-item.arco-menu-selected) {
  color: #ffffff;
  background-color: #165dff;
}

:deep(.arco-menu-item .arco-icon) {
  color: inherit;
}

.main-wrapper {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  height: 60px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f2f3f5;
}

.username {
  color: #1d2129;
  font-size: 14px;
}

.content {
  flex: 1;
  padding: 24px;
}
</style>
