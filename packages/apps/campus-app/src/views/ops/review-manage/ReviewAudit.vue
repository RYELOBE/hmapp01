<template>
  <OpsUnifiedTable
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
  >
    <template #tabs>
      <a-tabs v-model:active-key="activeTab" type="line" @change="handleTabChange">
        <a-tab-pane key="item-pending" title="商品评价-待审核"></a-tab-pane>
        <a-tab-pane key="item-reviewed" title="商品评价-已审核"></a-tab-pane>
        <a-tab-pane key="circle-pending" title="圈子评价-待审核"></a-tab-pane>
        <a-tab-pane key="circle-reviewed" title="圈子评价-已审核"></a-tab-pane>
      </a-tabs>
    </template>

    <template #itemImage="{ record }">
      <a-image v-if="getItemImage(record)" :src="getItemImage(record)" width="60" height="60" fit="cover" style="border-radius:6px" />
      <span v-else>-</span>
    </template>

    <template #reviewer="{ record }">
      <a-space>
        <a-avatar :size="24" :style="{ backgroundColor: getAvatarColor(record) }">
          {{ (record.buyerName || record.userName || record.authorName || '用')[0] }}
        </a-avatar>
        <span>{{ record.buyerName || record.userName || record.authorName }}</span>
      </a-space>
    </template>

    <template #target="{ record }">
      <a-tooltip :content="record.itemTitle || record.postTitle || `#ID${record.itemId || record.postId}`" position="top">
        <span class="ellipsis-text">{{ record.itemTitle || record.postTitle || `#ID${record.itemId || record.postId}` }}</span>
      </a-tooltip>
    </template>

    <template #content="{ record }">
      <a-tooltip :content="record.content" position="top">
        <span class="ellipsis-text content-text">{{ truncate(record.content, 50) }}</span>
      </a-tooltip>
    </template>

    <template #rating="{ record }">
      <a-rate v-if="record.rating" :model-value="record.rating" disabled allow-half :count="5" size="small" />
      <span v-else>-</span>
    </template>

    <template #status="{ record }">
      <a-tag :color="getStatusColor(record.status)" size="small">
        {{ getLabel(record.status) }}
      </a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="success" @click="approveReview(record)">通过</a-button>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="danger" @click="rejectReview(record)">拒绝</a-button>
        <a-button type="text" size="small">详情</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { opsHttp as http } from '../../../services/http'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'

const loading = ref(false)
const activeTab = ref('item-pending')
const tableData = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
})

const tableColumns = computed(() => [
  { title: '商品图片', dataIndex: 'itemImage', width: 80, slotName: 'itemImage', align: 'center' },
  { title: '评价人', dataIndex: 'reviewer', width: 140, slotName: 'reviewer' },
  { title: '评价对象', dataIndex: 'target', width: 160, slotName: 'target' },
  { title: '评价内容', dataIndex: 'content', slotName: 'content' },
  { title: '评分', dataIndex: 'rating', width: 120, slotName: 'rating', align: 'center' },
  { title: '状态', dataIndex: 'status', width: 90, slotName: 'status', align: 'center' },
  { title: '评价时间', dataIndex: 'createdAt', width: 150, slotName: 'createdAt' },
  { title: '操作', width: 140, slotName: 'operations', fixed: 'right', align: 'center' }
])

const statusMap = {
  PENDING: { label: '待审核', color: 'orange' },
  APPROVED: { label: '已通过', color: 'green' },
  REJECTED: { label: '已拒绝', color: 'red' },
}

function getLabel(status) {
  return statusMap[status]?.label || status || '-'
}

function getStatusColor(status) {
  return statusMap[status]?.color || 'gray'
}

function getAvatarColor(record) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1', '#14C9C9']
  const name = record.buyerName || record.userName || record.authorName || ''
  const index = name ? name.charCodeAt(0) % colors.length : 0
  return colors[index]
}

function truncate(text, len) {
  if (!text) return '-'
  return text.length > len ? text.substring(0, len) + '...' : text
}

function getItemImage(item) {
  const img = item.itemImage || item.itemImageUrl || ''
  return img
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getApiEndpoint() {
  const isItem = activeTab.value.startsWith('item')
  const isPending = activeTab.value.endsWith('pending')
  
  if (isItem && isPending) return '/reviews/pending'
  if (isItem && !isPending) return '/reviews/list'
  if (!isItem && isPending) return '/circle/comments/pending'
  return '/circle/comments/list'
}

async function loadData() {
  loading.value = true
  try {
    const endpoint = getApiEndpoint()
    const res = await http.post(endpoint, {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    })
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.reviews || data?.comments || data?.rows || []
    pagination.total = data?.totalCount ?? data?.total ?? tableData.value.length
  } catch (e) {
    console.error('[ReviewAudit] load error:', e)
    Message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  tableData.value = []
  pagination.current = 1
  loadData()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

async function approveReview(item) {
  try {
    const isItem = activeTab.value.startsWith('item')
    const endpoint = isItem ? `/reviews/${item.id}/approve` : `/circle/comments/${item.id}/approve`
    await http.post(endpoint)
    Message.success('已通过审核')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

async function rejectReview(item) {
  try {
    const isItem = activeTab.value.startsWith('item')
    const endpoint = isItem ? `/reviews/${item.id}/reject` : `/circle/comments/${item.id}/reject`
    await http.post(endpoint, { reason: '不符合评价规范' })
    Message.success('已拒绝')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  
  &:hover {
    color: #165DFF;
  }
}

.content-text {
  max-width: 200px;
  color: #4e5969;
  font-size: 13px;
}
</style>
