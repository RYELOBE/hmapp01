<template>
  <a-dropdown trigger="click" position="br" @select="handleSelect">
    <a-badge :count="unreadCount" :max-count="99" :dot="unreadCount > 0 && unreadCount < 10">
      <button class="notification-btn">
        <icon-notification />
      </button>
    </a-badge>
    <template #content>
      <div class="notification-dropdown">
        <div class="dropdown-header">
          <span class="title">消息通知</span>
          <a-button v-if="unreadCount > 0" type="text" size="mini" @click.stop="handleMarkAllRead">
            全部已读
          </a-button>
        </div>
        
        <a-spin :loading="loading" style="min-height: 100px">
          <div v-if="notifications.length > 0" class="notification-list">
            <div
              v-for="msg in notifications"
              :key="msg.id"
              class="notification-item"
              :class="{ unread: !msg.isRead }"
              @click="handleClick(msg)"
            >
              <div class="msg-icon" :class="`icon--${getType(msg.type)}`">
                <component :is="getIcon(msg.type)" />
              </div>
              <div class="msg-content">
                <div class="msg-title">{{ msg.title }}</div>
                <div class="msg-text">{{ msg.content }}</div>
                <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
              </div>
              <div v-if="!msg.isRead" class="unread-dot"></div>
            </div>
          </div>
          <a-empty v-else-if="!loading" description="暂无消息" :style="{ padding: '20px 0' }" />
        </a-spin>
        
        <div class="dropdown-footer">
          <a-button type="text" long @click="goMessageCenter">
            查看全部消息
            <template #icon><icon-right /></template>
          </a-button>
        </div>
      </div>
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconNotification,
  IconInfoCircle,
  IconApps,
  IconCheckCircle,
  IconHeartFill,
  IconRight,
} from '@arco-design/web-vue/es/icon';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '../../services/notifications';

const router = useRouter();

const loading = ref(false);
const notifications = ref([]);
const unreadCount = ref(0);

function getType(type) {
  const map = {
    ORDER: 'order',
    ITEM: 'item',
    REVIEW: 'review',
    CIRCLE: 'circle',
  };
  return map[type] || 'order';
}

function getIcon(type) {
  const map = {
    ORDER: IconApps,
    ITEM: IconCheckCircle,
    REVIEW: IconCheckCircle,
    CIRCLE: IconHeartFill,
  };
  return map[type] || IconApps;
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  // 1小时内
  if (diff < 3600000) {
    const mins = Math.floor(diff / 60000);
    return mins <= 1 ? '刚刚' : `${mins}分钟前`;
  }
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  // 昨天
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  // 其他
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
}

async function loadNotifications() {
  loading.value = true;
  try {
    const res = await getNotifications({ page: 1, size: 5 });
    console.log('[NotificationDropdown] 响应:', res);
    const data = res?.data?.data ?? res?.data ?? res;
    const list = data?.records || data?.list || (Array.isArray(data) ? data : []);
    notifications.value = list.map(msg => ({
      id: msg.id,
      type: msg.type || 'SYSTEM',
      title: msg.title || '系统通知',
      content: msg.content || '',
      isRead: msg.isRead !== false,
      createdAt: msg.createdAt || msg.created_at || new Date().toISOString(),
      link: msg.link || '',
      businessType: msg.businessType,
      businessId: msg.businessId,
    }));
    console.log('[NotificationDropdown] 消息数量:', notifications.value.length);
  } catch (e) {
    console.error('[NotificationDropdown] 加载失败:', e);
  } finally {
    loading.value = false;
  }
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount();
    console.log('[NotificationDropdown] 未读数响应:', res);
    unreadCount.value = res?.data?.count ?? res?.data ?? 0;
  } catch (e) {
    console.warn('[NotificationDropdown] 获取未读数失败:', e);
  }
}

async function handleClick(msg) {
  if (!msg.isRead) {
    try {
      await markAsRead(msg.id);
      msg.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch (e) {
      console.error('标记已读失败:', e);
    }
  }
  
  // 根据消息类型跳转
  if (msg.businessType === 'ORDER' && msg.businessId) {
    router.push(`/portal/orders?highlight=${msg.businessId}`);
  } else if (msg.link) {
    router.push(msg.link);
  } else {
    router.push('/portal/messages');
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead();
    notifications.value.forEach(msg => msg.isRead = true);
    unreadCount.value = 0;
    Message.success('已全部标记为已读');
  } catch (e) {
    Message.error('操作失败');
  }
}

function handleSelect() {
  // 阻止下拉菜单关闭
}

function goMessageCenter() {
  router.push('/portal/messages');
}

// 定时刷新未读数
let refreshTimer = null;

onMounted(async () => {
  await Promise.all([loadNotifications(), loadUnreadCount()]);

  // 每30秒刷新一次未读数
  refreshTimer = setInterval(loadUnreadCount, 30000);

  // 监听通知刷新事件
  window.addEventListener('notification-refresh', handleRefresh);
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
  window.removeEventListener('notification-refresh', handleRefresh);
});

function handleRefresh() {
  loadNotifications();
  loadUnreadCount();
}

// 暴露刷新方法供外部调用
defineExpose({
  refresh: () => Promise.all([loadNotifications(), loadUnreadCount()]),
});
</script>

<style lang="scss" scoped>
.notification-btn {
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

.notification-dropdown {
  width: 360px;
  max-height: 480px;
  display: flex;
  flex-direction: column;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);
  
  .title {
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  position: relative;

  &:hover {
    background-color: var(--color-fill-1, #F7F8FA);
  }

  &.unread {
    background-color: rgba(22, 93, 255, 0.02);
  }
}

.msg-icon {
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

.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-text {
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  margin-bottom: 4px;
}

.msg-time {
  font-size: 12px;
  color: var(--color-text-4, #C9CDD4);
}

.unread-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #165DFF;
}

.dropdown-footer {
  padding: 8px 0;
  border-top: 1px solid var(--color-border-1, #E5E6EB);
  text-align: center;
}

@media (max-width: 767px) {
  .notification-dropdown {
    width: 300px;
    max-height: 400px;
  }
  
  .notification-list {
    max-height: 260px;
  }
}
</style>
