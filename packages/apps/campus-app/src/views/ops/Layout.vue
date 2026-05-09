<template>
  <div class="ops-layout">
    <!-- 顶部头部 -->
    <header class="ops-header">
      <!-- 左侧：Logo + 平台名称 -->
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

      <!-- 右侧：首页 + 消息 + 用户头像 -->
      <div class="header-right">
        <a-button type="text" class="header-btn" @click="goToHome">
          <template #icon><icon-home /></template>
          <span class="btn-text">首页</span>
        </a-button>
        
        <a-dropdown position="bottom" trigger="click" @select="handleMessageSelect">
          <a-button type="text" class="header-btn notification-btn">
            <template #icon>
              <icon-notification />
              <a-badge v-if="unreadCount > 0" :count="unreadCount" :max-count="99" class="notification-badge" />
            </template>
            <span class="btn-text">消息</span>
          </a-button>
          <template #content>
            <a-doption v-for="msg in recentMessages" :key="msg.id" :value="msg.id">
              <div class="message-item">
                <div class="message-title">{{ msg.title }}</div>
                <div class="message-time">{{ formatMessageTime(msg.createdAt) }}</div>
              </div>
            </a-doption>
            <a-doption value="view-all" class="view-all-msg">
              <div class="view-all-text">查看全部消息</div>
            </a-doption>
          </template>
        </a-dropdown>

        <a-dropdown position="bottomRight" trigger="click" @select="handleUserAction">
          <div class="user-avatar-section">
            <a-avatar :size="32" class="user-avatar">{{ userInitial }}</a-avatar>
            <span class="user-name">{{ userName }}</span>
            <icon-down class="dropdown-arrow" />
          </div>
          <template #content>
            <a-doption value="profile">
              <template #icon><icon-user /></template>
              个人信息
            </a-doption>
            <a-doption class="logout-option" value="logout">
              <template #icon><icon-export /></template>
              退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </header>

    <!-- 主体内容区 -->
    <div class="main-container">
      <!-- 左侧导航 -->
      <aside class="sidebar">
        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <MainMenu show-icon />
        </nav>


      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <div class="content-body">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { useAuthStore } from '../../stores/auth'
import { getUnreadCount, getRecentNotifications } from '../../services/notifications'
import MainMenu from '../../components/ops/MainMenu.vue'
import {
  IconHome,
  IconNotification,
  IconUser,
  IconExport,
  IconDown,
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const userName = ref('运营管理员')
const userInitial = computed(() => userName.value ? userName.value.charAt(0).toUpperCase() : 'O')

const pendingCounts = ref({
  items: 12,
  orders: 3,
  circle: 5,
  reviews: 8,
})

// 消息通知相关
const unreadCount = ref(0)
const recentMessages = ref([])

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
        router.push('/login')
      } catch (error) {
        console.error('[Logout] error:', error)
        router.push('/login')
      }
    }
  })
}

function goToHome() {
  window.open('/', '_blank')  // ✅ 跳转到门户首页（新窗口打开）
}

function handleMessageSelect(value) {
  if (value === 'view-all') {
    router.push('/ops/messages/center')
  } else {
    // 处理单个消息点击
    console.log('Message selected:', value)
  }
}

function handleUserAction(value) {
  if (value === 'logout') {
    handleLogout()
  } else if (value === 'profile') {
    router.push('/portal/profile')
  }
}

function formatMessageTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 加载消息数据
async function loadNotificationData() {
  try {
    // 加载未读数量
    const unreadResponse = await getUnreadCount()
    unreadCount.value = unreadResponse.data || 0
    
    // 加载最近消息
    const recentResponse = await getRecentNotifications(5)
    recentMessages.value = recentResponse.data || []
  } catch (error) {
    console.error('[Layout] loadNotificationData error:', error)
  }
}

onMounted(() => {
  // 加载消息数据
  loadNotificationData()
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
  gap: 8px;
}

.header-btn {
  color: rgba(255, 255, 255, 0.85);
  border: none;
  background: transparent;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }
}

.notification-btn {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  
  :deep(.arco-badge-number) {
    background: #ff4d4f;
    border: 2px solid #165dff;
  }
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.user-name {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.dropdown-arrow {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  transition: transform 0.2s;
}

.message-item {
  padding: 4px 0;
  
  .message-title {
    font-size: 14px;
    color: #1d2129;
    margin-bottom: 2px;
  }
  
  .message-time {
    font-size: 12px;
    color: #86909c;
  }
}

.view-all-msg {
  border-top: 1px solid #e5e6eb;
  margin-top: 4px;
  padding-top: 8px;
}

.view-all-text {
  text-align: center;
  color: #165dff;
  font-size: 13px;
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

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  margin: 2px 8px;
  border-radius: 4px;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: rgba(255, 255, 255, 0.95);
  }

  &.active {
    background: #1890ff;
    color: #fff;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: #fff;
      border-radius: 0 2px 2px 0;
    }
  }
}

.menu-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 18px;
}

.menu-text {
  flex: 1;
  margin-left: 12px;
  font-size: 14px;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.3s;
  margin-left: auto;

  &.rotated {
    transform: rotate(90deg);
  }
}

.menu-group {
  margin: 2px 0;
}

.sub-menu {
  padding-left: 16px;
  margin-top: 4px;
}

.sub-item {
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 32px;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s;
  border-radius: 4px;
  margin: 2px 8px;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: rgba(255, 255, 255, 0.95);
  }

  &.active {
    background: rgba(24, 144, 255, 0.15);
    color: #40a9ff;

    .sub-dot {
      background: #40a9ff;
    }
  }
}

.sub-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
  margin-right: 10px;
  flex-shrink: 0;
}

.sub-badge {
  margin-left: auto;
  
  :deep(.arco-badge-number) {
    background: #ff4d4f;
    font-size: 11px;
    height: 18px;
    line-height: 18px;
    padding: 0 6px;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.user-details {
  flex: 1;
  min-width: 0;

  .user-name {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .user-role {
    display: block;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
    margin-top: 2px;
  }
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 4px;
  background: transparent;
  color: rgba(255, 255, 255, 0.75);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;

  &:hover {
    background: rgba(245, 80, 80, 0.15);
    border-color: rgba(245, 80, 80, 0.4);
    color: #ff7875;
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
