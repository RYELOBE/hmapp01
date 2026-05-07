<template>
  <div class="message-center" ref="messageCenterRef">
    <!-- 触发器：消息图标 + 未读角标 -->
    <a-badge :count="unreadCount" :max-count="99">
      <button
        class="trigger-btn"
        @click="toggleDropdown"
        aria-label="消息通知"
      >
        <icon-notification />
      </button>
    </a-badge>

    <!-- 下拉面板 -->
    <transition name="dropdown">
      <div v-if="isOpen" class="dropdown-panel">
        <!-- 面板头部 -->
        <div class="panel-header">
          <h3 class="panel-title">消息中心</h3>
          <div class="header-actions">
            <a
              href="javascript:void(0)"
              class="action-link"
              @click="handleReadAll"
            >
              全部标记已读
            </a>
            <router-link
              to="/portal/messages"
              class="action-link"
              @click="closeDropdown"
            >
              查看全部
            </router-link>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-list" ref="messagesListRef">
          <!-- 加载状态 -->
          <div v-if="loadingMessages" class="loading-state">
            <a-spin />
          </div>

          <!-- 消息项 -->
          <template v-else-if="messages.length > 0">
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message-item', { unread: !msg.isRead }]"
              @click="handleMessageClick(msg)"
            >
              <div class="message-icon" :class="`icon--${getMessageType(msg.type)}`">
                <component :is="getMessageIcon(msg.type)" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span :class="['message-title', { 'is-unread': !msg.isRead }]">
                    {{ msg.title }}
                  </span>
                  <span class="message-time">{{ formatRelativeTime(msg.createdAt) }}</span>
                </div>
                <p class="message-summary">{{ msg.content }}</p>
              </div>
              <div v-if="!msg.isRead" class="unread-dot"></div>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <icon-notification :size="48" color="#C9CDD4" />
            <p>暂无消息</p>
          </div>
        </div>

        <!-- 面板底部 -->
        <div class="panel-footer">
          <router-link
            to="/portal/messages"
            class="view-all-link"
            @click="closeDropdown"
          >
            查看所有消息
          </router-link>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  IconNotification,
  IconInfoCircle,
  IconApps,
  IconCheckCircle,
  IconHeartFill,
} from '@arco-design/web-vue/es/icon';

const props = defineProps({
  unreadCount: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['read-all', 'click-message']);

const router = useRouter();
const messageCenterRef = ref(null);
const messagesListRef = ref(null);

const isOpen = ref(false);
const loadingMessages = ref(false);
const messages = ref([]);

function toggleDropdown() {
  isOpen.value = !isOpen.value;
  if (isOpen.value && messages.value.length === 0) {
    loadMessages();
  }
}

function closeDropdown() {
  isOpen.value = false;
}

function handleClickOutside(event) {
  if (messageCenterRef.value && !messageCenterRef.value.contains(event.target)) {
    closeDropdown();
  }
}

function getMessageType(type) {
  const typeMap = {
    SYSTEM: 'system',
    TRANSACTION: 'transaction',
    REVIEW: 'review',
    INTERACTION: 'interaction',
  };
  return typeMap[type] || 'system';
}

function getMessageIcon(type) {
  const iconMap = {
    SYSTEM: IconInfoCircle,
    TRANSACTION: IconApps,
    REVIEW: IconCheckCircle,
    INTERACTION: IconHeartFill,
  };
  return iconMap[type] || IconNotification;
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  });
}

async function loadMessages() {
  loadingMessages.value = true;
  try {
    const response = await fetch('/api/messages?pageSize=5');
    const data = await response.json();
    messages.value = data.list || data.records || [];
  } catch (e) {
    console.error('加载消息失败:', e);
    // 使用模拟数据
    messages.value = getMockMessages();
  } finally {
    loadingMessages.value = false;
  }
}

function getMockMessages() {
  return [
    {
      id: 1,
      type: 'SYSTEM',
      title: '系统公告',
      content: '平台将于今晚22:00-23:00进行系统维护，届时部分功能可能无法使用。',
      isRead: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      link: '/portal/home',
    },
    {
      id: 2,
      type: 'TRANSACTION',
      title: '订单状态更新',
      content: '您的订单 #20240115001 已发货，请注意查收。',
      isRead: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      link: '/portal/orders',
    },
    {
      id: 3,
      type: 'REVIEW',
      title: '商品审核通过',
      content: '您发布的《二手MacBook Pro》已通过审核，已上架展示。',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
      link: '/portal/seller/items',
    },
    {
      id: 4,
      type: 'INTERACTION',
      title: '收到新评论',
      content: '用户 小明 评论了您的帖子：《学习资料分享》',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
      link: '/portal/circle/1',
    },
    {
      id: 5,
      type: 'INTERACTION',
      title: '获得新点赞',
      content: '用户 红红 点赞了您的帖子：《校园生活好物推荐》',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      link: '/portal/circle/2',
    },
  ];
}

async function handleMessageClick(message) {
  // 标记为已读
  if (!message.isRead) {
    try {
      await fetch(`/api/messages/${message.id}/read`, { method: 'POST' });
      message.isRead = true;
    } catch (e) {
      console.error('标记已读失败:', e);
    }
  }

  emit('click-message', message);

  // 跳转到链接
  if (message.link) {
    router.push(message.link);
    closeDropdown();
  }
}

async function handleReadAll() {
  try {
    await fetch('/api/messages/read-all', { method: 'POST' });
    messages.value.forEach((msg) => {
      msg.isRead = true;
    });
    emit('read-all');
    Message?.success('已全部标记为已读');
  } catch (e) {
    console.error('标记已读失败:', e);
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style lang="scss" scoped>
.message-center {
  position: relative;
  display: inline-block;
}

.trigger-btn {
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
  font-size: 18px;
  transition: all 0.25s ease;

  &:hover {
    background-color: var(--gray-50, #F7F8FA);
    color: var(--primary-500, #165DFF);
  }
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  max-height: 480px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 1050;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.dropdown-enter-active,
  &.dropdown-leave-active {
    transition: all 0.25s ease;
  }

  &.dropdown-enter-from,
  &.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);
  background: var(--color-bg-2, #F7F8FA);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  .panel-title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }

  .action-link {
    font-size: 13px;
    color: var(--primary-500, #165DFF);
    text-decoration: none;
    cursor: pointer;
    white-space: nowrap;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 0.75;
    }
  }
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
  max-height: 340px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 240px;
  gap: 12px;
  color: var(--color-text-4, #C9CDD4);

  p {
    margin: 0;
    font-size: 14px;
  }
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid var(--color-fill-1, #F7F8FA);
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--color-fill-1, #F7F8FA);
  }

  &.unread {
    background-color: #F0F5FF;
  }
}

.message-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;

  &--system {
    background: #E8F3FF;
    color: #165DFF;
  }

  &--transaction {
    background: #FFF7E8;
    color: #FF7D00;
  }

  &--review {
    background: #E8FFEC;
    color: #00B42A;
  }

  &--interaction {
    background: #FFECE8;
    color: #F53F3F;
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.message-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &.is-unread {
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }
}

.message-time {
  font-size: 12px;
  color: var(--color-text-4, #C9CDD4);
  flex-shrink: 0;
  white-space: nowrap;
}

.message-summary {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  position: absolute;
  top: 18px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #165DFF;
  flex-shrink: 0;
}

.panel-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border-1, #E5E6EB);
  background: var(--color-bg-2, #F7F8FA);
  text-align: center;
  flex-shrink: 0;
}

.view-all-link {
  display: block;
  font-size: 14px;
  color: var(--primary-500, #165DFF);
  text-decoration: none;
  font-weight: 500;
  padding: 6px 0;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.75;
  }
}
</style>
