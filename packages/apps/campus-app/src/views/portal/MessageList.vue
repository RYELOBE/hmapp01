<template>
  <div class="message-list-page">
    <!-- 统一页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" class="back-btn" @click="$router.back()">
          <template #icon><icon-arrow-left /></template>
          返回
        </a-button>
        <h2 class="page-title">消息中心</h2>
      </div>
      <div class="header-right">
        <span class="item-count">
          {{ unreadCount > 0 ? `${unreadCount} 条未读` : `共 ${total} 条消息` }}
        </span>
        <a-button
          v-if="unreadCount > 0"
          type="outline"
          size="small"
          class="mark-all-btn"
          @click="handleMarkAllRead"
        >
          全部标记已读
        </a-button>
      </div>
    </div>
      <!-- 分类Tab -->
      <div class="tab-bar">
        <span
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </span>
      </div>

      <!-- 批量操作栏 -->
      <div class="batch-actions">
        <a-checkbox
          :model-value="isAllSelected"
          :indeterminate="isIndeterminate"
          @change="handleSelectAll"
        >
          全选
        </a-checkbox>
        <a-button
          type="outline"
          size="small"
          :disabled="selectedIds.length === 0"
          @click="handleBatchRead"
        >
          批量标记已读
        </a-button>
        <a-button
          status="danger"
          size="small"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </a-button>
      </div>

      <!-- 消息列表 -->
      <a-spin :loading="loading">
        <div v-if="messages.length > 0" class="messages-container">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-card', { unread: !msg.isRead }]"
          >
            <a-checkbox
              :model-value="selectedIds.includes(msg.id)"
              @change="(val) => handleSelect(msg.id, val)"
              class="message-checkbox"
            />

            <div class="message-icon" :class="`icon--${getMessageType(msg.type)}`">
              <component :is="getMessageIcon(msg.type)" />
            </div>

            <div class="message-body" @click="handleMessageClick(msg)">
              <div class="message-header">
                <h4 :class="['message-title', { 'is-unread': !msg.isRead }]">
                  {{ msg.title }}
                </h4>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <p class="message-content">{{ msg.content }}</p>
              <div class="message-status">
                <a-tag
                  v-if="!msg.isRead"
                  color="arcoblue"
                  size="small"
                >
                  未读
                </a-tag>
                <a-tag v-else color="gray" size="small">已读</a-tag>
              </div>
            </div>

            <div class="message-actions">
              <a-dropdown trigger="click">
                <button class="more-btn">
                  <icon-more />
                </button>
                <template #content>
                  <a-doption @click="handleMarkRead(msg)">
                    {{ msg.isRead ? '标为未读' : '标记已读' }}
                  </a-doption>
                  <a-doption status="danger" @click="handleDelete(msg)">
                    删除消息
                  </a-doption>
                </template>
              </a-dropdown>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <a-empty
          v-else-if="!loading"
          description="暂无消息"
        />
      </a-spin>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <a-pagination
          :current="page"
          :page-size="size"
          :total="total"
          show-total
          show-page-size
          @change="handlePageChange"
          @page-size-change="handleSizeChange"
        />
      </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Modal, Message } from '@arco-design/web-vue';
import {
  IconInfoCircle,
  IconApps,
  IconCheckCircle,
  IconHeartFill,
  IconMore,
  IconArrowLeft,
} from '@arco-design/web-vue/es/icon';
import PageContainer from '../../components/layout/PageContainer/PageContainer.vue';
import {
  getNotifications,
  getUnreadCount as fetchUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '../../services/notifications';

const router = useRouter();

const loading = ref(false);
const activeTab = ref('');
const page = ref(1);
const size = ref(10);
const total = ref(0);
const messages = ref([]);
const selectedIds = ref([]);

const tabs = [
  { label: '全部', value: '' },
  { label: '系统通知', value: 'SYSTEM' },
  { label: '交易消息', value: 'TRANSACTION' },
  { label: '审核消息', value: 'REVIEW' },
  { label: '互动消息', value: 'INTERACTION' },
];

const isAllSelected = computed(() => {
  return (
    messages.value.length > 0 &&
    selectedIds.value.length === messages.value.length
  );
});

const isIndeterminate = computed(() => {
  return (
    selectedIds.value.length > 0 &&
    selectedIds.value.length < messages.value.length
  );
});

const unreadCount = ref(0);

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
  return iconMap[type] || IconInfoCircle;
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

async function loadMessages() {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      size: size.value,
    };
    if (activeTab.value) {
      params.type = activeTab.value;
    }

    console.log('[MessageList] 开始加载消息, 参数:', params);
    const response = await getNotifications(params);
    console.log('[MessageList] API响应:', response);

    if (response?.data) {
      const data = Array.isArray(response.data) ? response.data : (response.data.records || response.data.list || []);
      console.log('[MessageList] 解析到消息数量:', data.length);

      messages.value = data.map(msg => ({
        id: msg.id,
        type: msg.type || 'SYSTEM',
        title: msg.title || '系统通知',
        content: msg.content || '',
        isRead: msg.isRead !== false,
        createdAt: msg.createdAt || msg.created_at || new Date().toISOString(),
        link: msg.link || '',
      }));
      total.value = response.data.total || messages.value.length;

      console.log('[MessageList] 消息列表已更新, 总数:', total.value);

      // 获取未读数量
      await loadUnreadCount();
    } else {
      console.error('[MessageList] 数据格式错误, response:', response);
      throw new Error('数据格式错误');
    }
  } catch (e) {
    console.error('[MessageList] 加载消息失败:', e);
    Message.error('加载消息失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function loadUnreadCount() {
  try {
    console.log('[MessageList] 开始获取未读数量...');
    const res = await fetchUnreadCount();
    console.log('[MessageList] 未读数量API响应:', res);

    const count = res?.data?.count ?? res?.data ?? 0;
    unreadCount.value = count > 99 ? '99+' : count;
    console.log('[MessageList] 未读数量更新为:', unreadCount.value);
  } catch (e) {
    console.warn('[MessageList] 加载未读数量失败:', e);
    unreadCount.value = 0;
  }
}

function handleTabChange(tab) {
  activeTab.value = tab;
  page.value = 1;
  selectedIds.value = [];
  loadMessages();
}

function handlePageChange(p) {
  page.value = p;
  selectedIds.value = [];
  loadMessages();
}

function handleSizeChange(s) {
  size.value = s;
  page.value = 1;
  selectedIds.value = [];
  loadMessages();
}

function handleSelect(id, val) {
  if (val) {
    selectedIds.value.push(id);
  } else {
    selectedIds.value = selectedIds.value.filter((i) => i !== id);
  }
}

function handleSelectAll(val) {
  if (val) {
    selectedIds.value = messages.value.map((m) => m.id);
  } else {
    selectedIds.value = [];
  }
}

async function handleMessageClick(message) {
  // 标记为已读
  if (!message.isRead) {
    try {
      await markAsRead(message.id);
      message.isRead = true;
      // 更新未读数量
      if (unreadCount.value > 0 && unreadCount.value !== '99+') {
        unreadCount.value = unreadCount.value - 1;
      }
    } catch (e) {
      console.error('标记已读失败:', e);
      Message.error('标记已读失败');
    }
  }

  // 跳转链接
  if (message.link) {
    router.push(message.link);
  }
}

async function handleMarkRead(msg) {
  try {
    await markAsRead(msg.id);
    msg.isRead = !msg.isRead;
    Message.success(msg.isRead ? '已标记为已读' : '已标记为未读');

    // 更新未读数量
    await loadUnreadCount();
  } catch (e) {
    console.error('操作失败:', e);
    Message.error('操作失败');
  }
}

async function handleDelete(msg) {
  Modal.warning({
    title: '确认删除',
    content: `确定要删除消息"${msg.title}"吗？`,
    hideCancel: false,
    onOk: async () => {
      try {
        await deleteNotification(msg.id);
        messages.value = messages.value.filter((m) => m.id !== msg.id);
        selectedIds.value = selectedIds.value.filter((id) => id !== msg.id);
        total.value = Math.max(0, total.value - 1);
        Message.success('删除成功');
      } catch (e) {
        console.error('删除失败:', e);
        Message.error('删除失败');
      }
    },
  });
}

async function handleMarkAllRead() {
  try {
    const unreadMessages = messages.value.filter(msg => !msg.isRead);

    if (unreadMessages.length === 0) {
      Message.info('没有未读消息');
      return;
    }

    await markAllAsRead();

    // 更新所有消息为已读
    messages.value.forEach(msg => {
      msg.isRead = true;
    });

    unreadCount.value = 0;
    Message.success(`已将 ${unreadMessages.length} 条消息标记为已读`);
  } catch (e) {
    console.error('全部标记已读失败:', e);
    Message.error('操作失败，请稍后重试');
  }
}

async function handleBatchRead() {
  if (selectedIds.value.length === 0) {
    Message.warning('请先选择消息');
    return;
  }

  try {
    for (const id of selectedIds.value) {
      await markAsRead(id);
    }

    messages.value.forEach((msg) => {
      if (selectedIds.value.includes(msg.id)) {
        msg.isRead = true;
      }
    });

    selectedIds.value = [];
    await loadUnreadCount();
    Message.success('批量标记已读成功');
  } catch (e) {
    console.error('批量标记已读失败:', e);
    Message.error('操作失败');
  }
}

async function handleBatchDelete() {
  Modal.warning({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedIds.value.length} 条消息吗？`,
    hideCancel: false,
    onOk: async () => {
      try {
        for (const id of selectedIds.value) {
          await deleteNotification(id);
        }

        messages.value = messages.value.filter(
          (m) => !selectedIds.value.includes(m.id)
        );
        total.value = Math.max(0, total.value - selectedIds.value.length);
        selectedIds.value = [];
        Message.success('批量删除成功');
      } catch (e) {
        console.error('批量删除失败:', e);
        Message.error('删除失败');
      }
    },
  });
}

onMounted(async () => {
  await loadMessages();
  await loadUnreadCount();
});
</script>

<style lang="scss" scoped>
.message-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f7f8fa;
  min-height: 100vh;
}

/* ========== 统一页面头部 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 50%, #0E42D2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    color: rgba(255, 255, 255, 0.9);
    &:hover {
      color: white;
      background: rgba(255, 255, 255, 0.15);
    }
  }

  .page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .item-count {
      font-size: 14px;
      opacity: 0.9;
    }

    .mark-all-btn {
      background: white;
      color: #165DFF;
      border: none;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }
    }
  }
}

.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;

  .tab-item {
    padding: 8px 20px;
    border-radius: 20px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.25s ease;
    background: var(--color-fill-1, #F7F8FA);
    color: var(--color-text-2, #4E5969);
    border: 1px solid transparent;
    user-select: none;

    &:hover {
      background: #E8F3FF;
      color: #165DFF;
      border-color: #B8D2FF;
    }

    &.active {
      background: #165DFF;
      color: #FFFFFF;
      border-color: #165DFF;
      font-weight: 500;
    }
  }
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--color-fill-1, #F7F8FA);
  border-radius: 8px;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
  background: #FFFFFF;
  border-radius: 8px;
  border: 1px solid var(--color-border-1, #E5E6EB);
  transition: all 0.25s ease;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &.unread {
    background: #FAFCFF;
    border-left: 3px solid #165DFF;
  }
}

.message-checkbox {
  margin-top: 4px;
  flex-shrink: 0;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;

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

.message-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.message-title {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);

  &.is-unread {
    font-weight: 600;
  }
}

.message-time {
  font-size: 13px;
  color: var(--color-text-4, #C9CDD4);
  white-space: nowrap;
  flex-shrink: 0;
}

.message-content {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--color-text-2, #4E5969);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-status {
  display: flex;
  gap: 8px;
}

.message-actions {
  flex-shrink: 0;
  margin-top: 2px;
}

.more-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-3, #86909C);
  transition: all 0.2s ease;

  &:hover {
    background: var(--color-fill-1, #F7F8FA);
    color: var(--color-text-1, #1D2129);
  }
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 767px) {
  .message-list-page {
    padding: 12px;
  }

  .tab-bar {
    gap: 6px;

    .tab-item {
      padding: 6px 14px;
      font-size: 13px;
    }
  }

  .batch-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .message-card {
    padding: 14px 12px;
    gap: 10px;
  }

  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .message-time {
    align-self: flex-end;
  }
}
</style>
