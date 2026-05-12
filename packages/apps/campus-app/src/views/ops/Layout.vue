<template>
  <div class="ops-layout">
    <header class="ops-header">
      <div class="header-left">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="20" cy="20" r="20" fill="#165DFF"/>
              <path d="M20 10L26 18H14L20 10Z" fill="white"/>
              <rect x="14" y="19" width="12" height="11" rx="2" fill="white"/>
            </svg>
          </div>
          <span class="platform-name">校园运营后台</span>
        </div>
      </div>

      <div class="header-right">
        <button class="header-btn" @click="goToHome">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <span class="btn-text">首页</span>
        </button>

        <NotificationDropdown ref="notificationRef" />

        <a-dropdown trigger="click" position="br">
          <div class="user-info">
            <div class="user-avatar">
              <span class="avatar-placeholder">{{ userInitial }}</span>
            </div>
            <span class="username">{{ userName }}</span>
            <svg class="dropdown-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
          <template #content>
            <a-doption @click="router.push('/portal/profile')">
              <template #icon>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </template>
              个人信息
            </a-doption>
            <a-doption class="logout-option" @click="handleLogout">
              <template #icon>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <polyline points="16 17 21 12 16 7" />
                  <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
              </template>
              退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </header>

    <div class="main-container">
      <aside class="sidebar">
        <nav class="nav-menu">
          <MainMenu show-icon />
        </nav>
      </aside>

      <main class="main-content">
        <div class="content-body">
          <router-view v-slot="{ Component }">
            <keep-alive :include="['OpsDashboard']">
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { useAuthStore } from '../../stores/auth'
import MainMenu from '../../components/ops/MainMenu.vue'
import NotificationDropdown from '../../components/common/NotificationDropdown.vue'

const router = useRouter()
const authStore = useAuthStore()
const notificationRef = ref(null)

const userName = ref('运营管理员')
const userInitial = computed(() => userName.value ? userName.value.charAt(0).toUpperCase() : 'O')

const pendingCounts = ref({
  items: 12,
  orders: 3,
  circle: 5,
  reviews: 8,
})

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出运营后台吗？',
    okText: '确认退出',
    cancelText: '取消',
    onOk: async () => {
      try {
        authStore.logout()
        Message.success('已成功退出登录')
        router.push('/ops/login')
      } catch (error) {
        console.error('[Logout] error:', error)
        router.push('/ops/login')
      }
    }
  })
}

function goToHome() {
  window.open('/', '_blank')
}

onMounted(() => {
  router.afterEach(() => {
    notificationRef.value?.refresh()
  })
})
</script>

<style lang="scss" scoped>
.ops-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f2f5;
}

.ops-header {
  height: 48px;
  background: linear-gradient(90deg, #165dff 0%, #4c6ef5 100%);
  box-shadow: var(--shadow-s1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
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

.platform-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  svg {
    width: 18px;
    height: 18px;
  }
}

.btn-text {
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .avatar-placeholder {
      font-size: 14px;
      font-weight: 600;
      color: #fff;
    }
  }

  .username {
    font-size: 14px;
    font-weight: 500;
    color: #fff;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .dropdown-arrow {
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
    transition: transform 0.2s;
  }
}

.main-container {
  display: flex;
  margin-top: 48px;
  min-height: calc(100vh - 48px);
}

.sidebar {
  width: 220px;
  background: #ffffff;
  border-right: 1px solid var(--color-border-2);
  padding: 16px 0;
  overflow-y: auto;
  transition: all 0.2s ease;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.nav-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    border-color: var(--color-bg-0);
    &:hover {
      border-color: var(--color-bg-0);
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #fff;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media screen and (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 64px;
    height: calc(100vh - 64px);
    transform: translateX(0);
    z-index: 998;
  }
}
</style>