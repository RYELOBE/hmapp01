<template>
  <div class="portal-layout">
    <header class="portal-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="router.push('/portal/home')">
            <div class="logo-icon">
              <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="20" cy="20" r="20" fill="#165DFF"/>
                <path d="M20 10L26 18H14L20 10Z" fill="white"/>
                <rect x="14" y="19" width="12" height="11" rx="2" fill="white"/>
              </svg>
            </div>
            <span class="logo-text">校园二手平台</span>
          </div>

          <nav class="main-nav" :class="{ 'nav-open': mobileMenuOpen }">
            <a
              v-for="(item, index) in navItems"
              :key="index"
              class="nav-item"
              :class="{ active: isActive(item.path) }"
              @click.prevent="handleNavClick(item)"
            >
              {{ item.label }}
            </a>
          </nav>

          <button
            class="mobile-menu-btn"
            @click="toggleMobileMenu"
            aria-label="菜单"
          >
            <span :class="{ open: mobileMenuOpen }"></span>
          </button>
        </div>

        <div class="header-right">
          <button class="icon-btn search-btn" @click="handleSearch" aria-label="搜索">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
          </button>

          <button class="icon-btn notification-btn" aria-label="通知">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span class="notification-badge">3</span>
          </button>

          <template v-if="authStore.isLoggedIn">
            <a-dropdown trigger="click" position="br">
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    v-if="authStore.user?.avatar"
                    :src="authStore.user.avatar"
                    :alt="authStore.user?.username || '用户'"
                  />
                  <span v-else class="avatar-placeholder">
                    {{ (authStore.user?.username || 'U').charAt(0).toUpperCase() }}
                  </span>
                </div>
                <span class="username">{{ authStore.user?.username || '用户' }}</span>
                <svg class="dropdown-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </div>
              <template #content>
                <a-doption @click="router.push('/portal/profile')">
                  <template #icon>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                  </template>
                  个人中心
                </a-doption>
                <a-doption @click="router.push('/portal/orders')">
                  <template #icon>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                      <line x1="3" y1="6" x2="21" y2="6"/>
                      <path d="M16 10a4 4 0 0 1-8 0"/>
                    </svg>
                  </template>
                  我的订单
                </a-doption>
                <a-doption
                  v-if="authStore.roles.includes('SELLER')"
                  @click="router.push('/portal/seller/publish')"
                >
                  <template #icon>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="5" x2="12" y2="19"/>
                      <line x1="5" y1="12" x2="19" y2="12"/>
                    </svg>
                  </template>
                  发布商品
                </a-doption>
                <a-doption divider />
                <a-doption class="logout-option" @click="handleLogout">
                  <template #icon>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                      <polyline points="16 17 21 12 16 7"/>
                      <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                  </template>
                  退出登录
                </a-doption>
              </template>
            </a-dropdown>
          </template>
          <template v-else>
            <a-button type="text" class="login-btn" @click="handleLogin">
              登录 / 注册
            </a-button>
          </template>
        </div>
      </div>
    </header>

    <main class="portal-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- AI助手悬浮窗 -->
    <AiAssistant />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { Message } from '@arco-design/web-vue'
import AiAssistant from '../../components/AiAssistant.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const mobileMenuOpen = ref(false)

const navItems = [
  { label: '首页', path: '/portal/home' },
  { label: '全部商品', path: '/portal/buyer/items' },
  { label: '卖家精选', path: '/portal/home?filter=featured' },
  { label: '校园圈子', path: '#' },
  { label: '客服中心', path: '#' }
]

const isActive = (path) => {
  if (!path || path === '#') return false;
  
  const currentPath = route.path;
  const currentQuery = route.query;
  
  // 提取纯净路径（去除查询参数）
  const cleanPath = path.split('?')[0];
  
  // 精确匹配逻辑
  if (cleanPath === '/portal/home') {
    // 首页和卖家精选的特殊处理
    if (path === '/portal/home' || path === '/portal') {
      // 只有当前页正好是 /portal/home 时才激活（排除带参数的情况）
      return currentPath === '/portal/home' || currentPath === '/portal';
    } else if (path.includes('filter=featured')) {
      // 卖家精选：需要检查是否有 filter 参数
      return currentPath === '/portal/home' && currentQuery.filter === 'featured';
    }
    return currentPath === cleanPath;
  }
  
  // 其他路径的精确匹配
  if (currentPath === cleanPath) return true;
  
  // 子路径匹配（如 /portal/seller/items 匹配 /portal/seller）
  if (currentPath.startsWith(cleanPath + '/')) return true;
  
  return false;
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

const handleSearch = () => {
  router.push('/portal/buyer/items?search=')
}

const handleNavClick = (item) => {
  console.log('[Portal Nav] 点击导航:', item.label, '→', item.path)
  
  // 关闭移动端菜单
  closeMobileMenu()
  
  if (item.path === '#' || !item.path) {
    Message.info(`${item.label}功能开发中，敬请期待！`)
    return
  }
  
  // 处理带参数的路由
  if (item.path.includes('?')) {
    const [path, query] = item.path.split('?')
    const params = new URLSearchParams(query)
    const queryObj = {}
    for (const [key, value] of params) {
      queryObj[key] = value
    }
    
    router.push({ path, query: queryObj })
  } else {
    router.push(item.path)
  }
}

const handleLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath }
  })
}

const handleLogout = () => {
  authStore.logout()
  Message.success('已退出登录')
  router.push('/portal/home')
}
</script>

<style lang="scss" scoped>
.portal-layout {
  min-height: 100vh;
  background-color: var(--bg-gray);
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 64px;
  background-color: var(--bg-white);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-color);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 48px;
  flex: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity var(--transition-fast);

  &:hover {
    opacity: 0.85;
  }

  .logo-icon {
    width: 36px;
    height: 36px;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    white-space: nowrap;
  }
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 32px;

  .nav-item {
    font-size: 15px;
    font-weight: 400;
    color: var(--text-secondary);
    text-decoration: none;
    padding: 4px 0;
    position: relative;
    transition: all var(--transition-fast);
    white-space: nowrap;

    &:hover {
      color: var(--primary-blue);
    }

    &.active {
      color: var(--primary-blue);
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: -22px;
        left: 0;
        right: 0;
        height: 2px;
        background-color: var(--primary-blue);
        border-radius: 1px;
      }
    }
  }
}

.mobile-menu-btn {
  display: none;
  width: 32px;
  height: 32px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;

  span,
  span::before,
  span::after {
    display: block;
    width: 20px;
    height: 2px;
    background-color: var(--text-primary);
    border-radius: 1px;
    position: absolute;
    transition: all var(--transition-normal);
  }

  span {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);

    &::before {
      content: '';
      top: -6px;
      left: 0;
    }

    &::after {
      content: '';
      top: 6px;
      left: 0;
    }

    &.open {
      background-color: transparent;

      &::before {
        top: 0;
        transform: rotate(45deg);
      }

      &::after {
        top: 0;
        transform: rotate(-45deg);
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  background: none;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    background-color: var(--bg-gray-light);
    color: var(--primary-blue);
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

.notification-btn {
  .notification-badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    font-size: 11px;
    font-weight: 600;
    color: var(--text-white);
    background-color: #F53F3F;
    border-radius: var(--radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid var(--bg-white);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background-color: var(--bg-gray-light);
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: var(--radius-full);
    overflow: hidden;
    background-color: var(--primary-blue);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-placeholder {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-white);
    }
  }

  .username {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .dropdown-arrow {
    color: var(--text-tertiary);
    transition: transform var(--transition-fast);
  }
}

.login-btn {
  font-size: 14px;
  font-weight: 500;
  color: var(--primary-blue) !important;
  padding: 6px 16px !important;
  border-radius: var(--radius-md) !important;
  transition: all var(--transition-fast) !important;

  &:hover {
    background-color: rgba(22, 93, 255, 0.06) !important;
  }
}

.portal-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
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

@media (max-width: 1023px) {
  .header-left {
    gap: 32px;
  }

  .main-nav {
    gap: 24px;

    .nav-item {
      font-size: 14px;
    }
  }

  .main-nav .nav-item:nth-child(n+4) {
    display: none;
  }
}

@media (max-width: 767px) {
  .portal-header {
    height: 56px;
  }

  .header-container {
    padding: 0 16px;
  }

  .logo .logo-text {
    font-size: 15px;
  }

  .main-nav {
    display: none;
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: var(--bg-white);
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
    gap: 0;
    box-shadow: var(--shadow-lg);
    z-index: 999;

    &.nav-open {
      display: flex;

      .nav-item {
        width: 100%;
        padding: 16px 0;
        font-size: 16px;
        border-bottom: 1px solid var(--border-color-light);

        &.active::after {
          bottom: auto;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 60%;
          right: auto;
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

  .notification-btn {
    display: none;
  }

  .user-info .username {
    display: none;
  }

  .portal-content {
    padding: 16px;
  }
}
</style>
