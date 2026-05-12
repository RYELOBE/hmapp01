<template>
  <OpsUnifiedTable
    title="消息中心"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
    @row-click="handleRowClick"
  >
    <template #actions>
      <a-input-search v-model="keyword" placeholder="搜索消息内容" style="width: 260px" search-button @search="handleSearch" allow-clear />
      <a-select v-model="typeFilter" placeholder="消息类型" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="type in typeOptions" :key="type.value" :value="type.value">{{ type.label }}</a-option>
      </a-select>
      <a-select v-model="priorityFilter" placeholder="优先级" style="width: 120px" allow-clear @change="handleSearch">
        <a-option v-for="priority in priorityOptions" :key="priority.value" :value="priority.value">{{ priority.label }}</a-option>
      </a-select>
      <a-select v-model="readFilter" placeholder="阅读状态" style="width: 120px" allow-clear @change="handleSearch">
        <a-option value="unread">未读</a-option>
        <a-option value="read">已读</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="primary" status="danger" @click="handleMarkAllRead" :disabled="!hasUnread">全部已读</a-button>
      <span class="unread-count">未读: {{ unreadCount }}</span>
    </template>

    <!-- 列插槽 -->
    <template #status="{ record }">
      <div class="status-indicator">
        <div v-if="!record.isRead" class="unread-dot"></div>
        <span v-else class="read-icon">✓</span>
      </div>
    </template>

    <template #type="{ record }">
      <a-tag :color="getNotificationTypeColor(record.type)" size="small">{{ getNotificationTypeLabel(record.type) }}</a-tag>
    </template>

    <template #priority="{ record }">
      <a-tag :color="getPriorityColor(record.priority)" size="small">{{ getPriorityLabel(record.priority) }}</a-tag>
    </template>

    <template #title="{ record }">
      <span :class="{ 'unread-title': !record.isRead }">{{ record.title }}</span>
    </template>

    <template #createdAt="{ record }">
      {{ formatDateTime(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button v-if="!record.isRead" type="text" size="small" status="success" @click.stop="handleMarkAsRead(record.id)">标已读</a-button>
        <a-button type="text" size="small" status="danger" @click.stop="deleteMessage(record)">删除</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <!-- 消息详情弹窗 -->
  <a-modal v-model:visible="detailVisible" :title="currentMessage?.title" :width="600" @ok="closeDetail" @cancel="closeDetail">
    <div v-if="currentMessage" class="message-detail">
      <div class="detail-meta">
        <a-tag :color="getNotificationTypeColor(currentMessage.type)">{{ getNotificationTypeLabel(currentMessage.type) }}</a-tag>
        <a-tag :color="getPriorityColor(currentMessage.priority)">{{ getPriorityLabel(currentMessage.priority) }}</a-tag>
        <span class="detail-time">{{ formatDateTime(currentMessage.createdAt) }}</span>
      </div>
      <div class="detail-content"><p>{{ currentMessage.content }}</p></div>
      <div class="detail-sender"><span>发送者: {{ currentMessage.senderName || '系统' }}</span></div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import {
  getNotifications,
  getUnreadCount,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
  deleteNotification,
  getNotificationTypeLabel,
  getNotificationTypeColor,
  getPriorityLabel,
  getPriorityColor
} from '../../../services/notifications'

const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const priorityFilter = ref('')
const readFilter = ref('')
const tableData = ref([])
const unreadCount = ref(0)
const detailVisible = ref(false)
const currentMessage = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
})

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = [
  { title: '状态', dataIndex: 'status', width: 80, align: 'center', slotName: 'status' },
  { title: '类型', dataIndex: 'type', width: 100, align: 'center', slotName: 'type' },
  { title: '优先级', dataIndex: 'priority', width: 90, align: 'center', slotName: 'priority' },
  { title: '标题', dataIndex: 'title', width: 300, ellipsis: true, slotName: 'title' },
  { title: '内容', dataIndex: 'content', width: 400, ellipsis: true },
  { title: '发送者', dataIndex: 'senderName', width: 120 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 150, fixed: 'right', align: 'center', slotName: 'operations' }
]

const typeOptions = [
  { label: '系统通知', value: 'SYSTEM' },
  { label: '审核通知', value: 'REVIEW' },
  { label: '订单通知', value: 'ORDER' },
  { label: '用户通知', value: 'USER' },
  { label: '业务通知', value: 'BUSINESS' }
]

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'URGENT' }
]

const hasUnread = computed(() => unreadCount.value > 0)

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      priority: priorityFilter.value || undefined,
      unread: readFilter.value === 'unread' ? true : readFilter.value === 'read' ? false : undefined
    }
    console.log('[MessageCenter] 请求参数:', params)

    const response = await getNotifications(params)
    console.log('[MessageCenter] 响应:', response)
    const data = response?.data?.data ?? response?.data ?? response
    tableData.value = data?.records || data?.list || []
    pagination.total = data?.totalCount ?? data?.total ?? 0
    console.log('[MessageCenter] 数据条数:', tableData.value.length)
  } catch (error) {
    console.error('[MessageCenter] load error:', error)
    Message.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

async function loadUnreadCount() {
  try {
    const response = await getUnreadCount()
    console.log('[MessageCenter] 未读数响应:', response)
    unreadCount.value = response?.data?.count ?? response?.data ?? 0
  } catch (error) {
    console.error('[MessageCenter] loadUnreadCount error:', error)
  }
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; typeFilter.value = ''; priorityFilter.value = ''; readFilter.value = ''; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }
function refreshData() { loadData(); loadUnreadCount() }

async function handleMarkAsRead(id) {
  try {
    await apiMarkAsRead(id)
    Message.success('已标记为已读')
    const message = tableData.value.find(item => item.id === id)
    if (message) { message.isRead = true; message.readAt = new Date().toISOString() }
    loadUnreadCount()
  } catch (error) { Message.error('标记失败') }
}

async function handleMarkAllRead() {
  try {
    await apiMarkAllAsRead()
    Message.success('已标记全部为已读')
    loadData()
    loadUnreadCount()
  } catch (error) { Message.error('操作失败') }
}

function deleteMessage(record) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条消息吗？',
    okText: '确认删除',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteNotification(record.id)
        Message.success('消息已删除')
        loadData()
        loadUnreadCount()
      } catch (error) { Message.error('删除失败') }
    }
  })
}

function handleRowClick(record) {
  currentMessage.value = record
  detailVisible.value = true
  if (!record.isRead) { handleMarkAsRead(record.id) }
}

function closeDetail() { detailVisible.value = false; currentMessage.value = null }

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => { loadData(); loadUnreadCount() })
</script>

<style lang="scss" scoped>
.unread-count {
  font-size: 14px;
  color: #f53f3f;
  font-weight: 500;
}

.status-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #f53f3f;
  border-radius: 50%;
}

.read-icon {
  color: #86909c;
  font-size: 12px;
}

.unread-title {
  font-weight: 600;
  color: #1d2129;
}

.message-detail {
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e6eb;
  }

  .detail-time {
    margin-left: auto;
    color: #86909c;
    font-size: 13px;
  }

  .detail-content {
    margin-bottom: 16px;
    line-height: 1.6;
    color: #1d2129;
  }

  .detail-sender {
    color: #86909c;
    font-size: 13px;
  }
}
</style>
