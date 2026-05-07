<template>
  <div class="message-list-page">
    <PageContainer title="消息中心" subtitle="查看所有通知消息">
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
    </PageContainer>
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
} from '@arco-design/web-vue/es/icon';
import PageContainer from '../../components/layout/PageContainer/PageContainer.vue';

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

    const response = await fetch(`/api/messages?${new URLSearchParams(params)}`);
    const data = await response.json();

    messages.value = data.list || data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    console.error('加载消息失败:', e);
    // 使用模拟数据
    messages.value = getMockMessages();
    total.value = 25;
  } finally {
    loading.value = false;
  }
}

function getMockMessages() {
  const baseMessages = [
    {
      id: 1,
      type: 'SYSTEM',
      title: '系统维护公告',
      content: '平台将于今晚22:00-23:00进行系统维护升级，届时部分功能可能无法使用，请提前做好准备。',
      isRead: false,
      createdAt: new Date().toISOString(),
      link: '/portal/home',
    },
    {
      id: 2,
      type: 'TRANSACTION',
      title: '订单发货通知',
      content: '您的订单 #20240115001 已发货，快递单号：SF1234567890，请注意查收。',
      isRead: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      link: '/portal/orders',
    },
    {
      id: 3,
      type: 'REVIEW',
      title: '商品审核通过',
      content: '您发布的《二手MacBook Pro 2020款》已通过审核，已上架展示。',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
      link: '/portal/seller/items',
    },
    {
      id: 4,
      type: 'INTERACTION',
      title: '收到新评论',
      content: '用户 小明 评论了您的帖子：《大学学习资料分享合集》',
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
    {
      id: 6,
      type: 'SYSTEM',
      title: '新功能上线',
      content: '校园圈子功能正式上线啦！快来发布你的第一条动态吧~',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
      link: '/portal/circle',
    },
  ];

  // 根据当前页返回不同的数据
  if (page.value > 1) {
    return baseMessages.map((m, i) => ({
      ...m,
      id: m.id + (page.value - 1) * size.value + i * 10,
      createdAt: new Date(
        Date.now() - 1000 * 60 * 60 * (24 + i)
      ).toISOString(),
    }));
  }

  return baseMessages;
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
      await fetch(`/api/messages/${message.id}/read`, { method: 'POST' });
      message.isRead = true;
    } catch (e) {
      console.error('标记已读失败:', e);
    }
  }

  // 跳转链接
  if (message.link) {
    router.push(message.link);
  }
}

async function handleMarkRead(msg) {
  try {
    await fetch(`/api/messages/${msg.id}/read`, { method: 'POST' });
    msg.isRead = !msg.isRead;
    Message.success(msg.isRead ? '已标记为已读' : '已标记为未读');
  } catch (e) {
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
        await fetch(`/api/messages/${msg.id}`, { method: 'DELETE' });
        messages.value = messages.value.filter((m) => m.id !== msg.id);
        selectedIds.value = selectedIds.value.filter((id) => id !== msg.id);
        Message.success('删除成功');
      } catch (e) {
        Message.error('删除失败');
      }
    },
  });
}

async function handleBatchRead() {
  try {
    await fetch('/api/messages/read-all', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids: selectedIds.value }),
    });

    messages.value.forEach((msg) => {
      if (selectedIds.value.includes(msg.id)) {
        msg.isRead = true;
      }
    });

    selectedIds.value = [];
    Message.success('批量标记已读成功');
  } catch (e) {
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
        await fetch('/api/messages/batch-delete', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ids: selectedIds.value }),
        });

        messages.value = messages.value.filter(
          (m) => !selectedIds.value.includes(m.id)
        );
        selectedIds.value = [];
        Message.success('批量删除成功');
      } catch (e) {
        Message.error('删除失败');
      }
    },
  });
}

onMounted(loadMessages);
</script>

<style lang="scss" scoped>
.message-list-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
  padding: 20px;
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
