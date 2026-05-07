<template>
  <header class="campus-header">
    <div class="header-container">
      <!-- 左侧 Logo 区域 -->
      <div class="header-left">
        <div class="logo" @click="handleLogoClick">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="20" cy="20" r="20" fill="#165DFF"/>
              <path d="M20 10L26 18H14L20 10Z" fill="white"/>
              <rect x="14" y="19" width="12" height="11" rx="2" fill="white"/>
            </svg>
          </div>
          <span class="logo-text">校园二手市场</span>
        </div>

        <!-- 中间搜索框 -->
        <div class="search-wrapper">
          <a-input-search
            v-model="searchKeyword"
            :placeholder="searchPlaceholder"
            allow-clear
            @search="handleSearch"
            @press-enter="handleSearch"
          />
        </div>
      </div>

      <!-- 右侧导航区域 -->
      <div class="header-right">
        <!-- 导航菜单 -->
        <nav class="main-nav" :class="{ 'nav-open': mobileMenuOpen }">
          <router-link
            v-for="(item, index) in navItems"
            :key="index"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActiveRoute(item.path) }"
            @click="closeMobileMenu"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <!-- 消息图标 -->
        <a-badge :count="unreadCount" :max-count="99">
          <button class="icon-btn notification-btn" @click="handleNotification">
            <icon-notification />
          </button>
        </a-badge>

        <!-- 用户下拉菜单 -->
        <template v-if="isLoggedIn">
          <a-dropdown trigger="click" position="br" @select="handleUserAction">
            <div class="user-info">
              <div class="user-avatar">
                <img v-if="userInfo.avatar" :src="userInfo.avatar" :alt="userInfo.username || '用户'" />
                <span v-else class="avatar-placeholder">{{ (userInfo.username || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <span class="username">{{ userInfo.username || '用户' }}</span>
              <icon-down class="dropdown-arrow" />
            </div>
            <template #content>
              <a-doption value="profile">
                <template #icon><icon-user /></template>
                个人中心
              </a-doption>
              <a-doption value="orders">
                <template #icon><icon-file /></template>
                我的订单
              </a-doption>
              <a-doption value="favorites">
                <template #icon><icon-star /></template>
                收藏夹
              </a-doption>
              <a-doption divider />
              <a-doption value="logout" class="logout-option">
                <template #icon><icon-export /></template>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </template>

        <template v-else>
          <a-button type="primary" size="small" @click="handleLogin">登录 / 注册</a-button>
        </template>

        <!-- 移动端汉堡菜单按钮 -->
        <button class="mobile-menu-btn" @click="toggleMobileMenu" aria-label="菜单">
          <icon-menu v-if="!mobileMenuOpen" />
          <icon-close v-else />
        </button>
      </div>
    </div>

    <!-- 移动端导航遮罩 -->
    <transition name="fade">
      <div v-if="mobileMenuOpen" class="mobile-overlay" @click="closeMobileMenu"></div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IconNotification,
  IconUser,
  IconFile,
  IconStar,
  IconExport,
  IconDown,
  IconMenu,
  IconClose
} from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'

const props = defineProps({
  searchPlaceholder: {
    type: String,
    default: '搜索商品、卖家...'
  },
  unreadCount: {
    type: Number,
    default: 0
  },
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => ({
      username: '',
      avatar: ''
    })
  }
})

const emit = defineEmits(['search', 'notification', 'login', 'logout', 'logo-click'])

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const mobileMenuOpen = ref(false)

const navItems = [
  { label: '首页', path: '/portal/home' },
  { label: '商品', path: '/portal/buyer/items' },
  { label: '圈子', path: '/portal/circle' },
  { label: '关于我们', path: '#' }
]

function isActiveRoute(path) {
  if (!path || path === '#') return false
  const cleanPath = path.split('?')[0]
  if (route.path === cleanPath) return true
  if (route.path.startsWith(cleanPath + '/')) return true
  return false
}

function handleSearch(value) {
  emit('search', value || searchKeyword.value)
}

function handleLogoClick() {
  emit('logo-click')
  router.push('/portal/home')
}

function handleNotification() {
  emit('notification')
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

function closeMobileMenu() {
  mobileMenuOpen.value = false
}

function handleUserAction(key) {
  switch (key) {
    case 'profile':
      router.push('/portal/profile')
      break
    case 'orders':
      router.push('/portal/orders')
      break
    case 'favorites':
      router.push('/portal/favorites')
      break
    case 'logout':
      emit('logout')
      Message.success('已退出登录')
      router.push('/portal/home')
      break
  }
}

function handleLogin() {
  emit('login')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}
</script>

<style lang="scss" scoped>
.campus-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 64px;
  background-color: var(--bg-card, #FFFFFF);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid var(--color-border-2, #E5E6EB);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.85;
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--gray-600, #1D2129);
    white-space: nowrap;
  }
}

.search-wrapper {
  flex: 1;
  max-width: 480px;

  :deep(.arco-input-search) {
    width: 100%;

    .arco-input-wrapper {
      border-radius: 20px;
      background-color: var(--gray-50, #F7F8FA);

      &:hover {
        border-color: var(--primary-500, #165DFF);
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 28px;

  .nav-item {
    font-size: 15px;
    font-weight: 400;
    color: var(--gray-500, #4E5969);
    text-decoration: none;
    padding: 6px 4px;
    position: relative;
    transition: all 0.2s ease;
    white-space: nowrap;

    &:hover {
      color: var(--primary-500, #165DFF);
    }

    &.active {
      color: var(--primary-500, #165DFF);
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: -20px;
        left: 50%;
        transform: translateX(-50%);
        width: 24px;
        height: 3px;
        background-color: var(--primary-500, #165DFF);
        border-radius: 2px;
      }
    }
  }
}

.icon-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  background: none;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-400, #86909C);
  transition: all 0.2s ease;
  font-size: 18px;

  &:hover {
    background-color: var(--gray-50, #F7F8FA);
    color: var(--primary-500, #165DFF);
  }
}

.notification-btn {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: var(--gray-50, #F7F8FA);
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    overflow: hidden;
    background-color: var(--primary-500, #165DFF);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-placeholder {
      font-size: 14px;
      font-weight: 600;
      color: #FFFFFF;
    }
  }

  .username {
    font-size: 14px;
    font-weight: 500;
    color: var(--gray-600, #1D2129);
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .dropdown-arrow {
    font-size: 12px;
    color: var(--gray-400, #86909C);
  }
}

.mobile-menu-btn {
  display: none;
  width: 36px;
  height: 36px;
  padding: 0;
  background: none;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  color: var(--gray-600, #1D2129);
  font-size: 20px;

  &:hover {
    background-color: var(--gray-50, #F7F8FA);
  }
}

.mobile-overlay {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1023px) {
  .search-wrapper {
    max-width: 320px;
  }

  .main-nav {
    gap: 20px;

    .nav-item {
      font-size: 14px;
    }
  }

  .main-nav .nav-item:nth-child(n+4) {
    display: none;
  }
}

@media (max-width: 767px) {
  .campus-header {
    height: 56px;
  }

  .header-container {
    padding: 0 16px;
  }

  .header-left {
    gap: 12px;
  }

  .logo .logo-text {
    font-size: 14px;
  }

  .search-wrapper {
    display: none;
  }

  .main-nav {
    display: none;
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    background-color: var(--bg-card, #FFFFFF);
    flex-direction: column;
    align-items: flex-start;
    padding: 16px 24px;
    gap: 0;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    z-index: 1001;

    &.nav-open {
      display: flex;

      .nav-item {
        width: 100%;
        padding: 14px 0;
        font-size: 15px;
        border-bottom: 1px solid var(--gray-100, #F2F3F5);

        &::after {
          display: none;
        }

        &.active {
          color: var(--primary-500, #165DFF);
          font-weight: 600;
          padding-left: 12px;
          border-left: 3px solid var(--primary-500, #165DFF);
        }
      }
    }
  }

  .mobile-menu-btn {
    display: flex;
  }

  .header-right {
    gap: 8px;
  }

  .user-info .username {
    display: none;
  }
}
</style>
