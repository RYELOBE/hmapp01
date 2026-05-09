<template>
  <OpsUnifiedTable
    title="圈子审核"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
  >
    <!-- Tabs 插槽 -->
    <template #tabs>
      <a-tabs v-model:active-key="activeTab" type="line" @change="handleTabChange">
        <a-tab-pane key="PENDING" title="待审核"></a-tab-pane>
        <a-tab-pane key="APPROVED" title="已通过"></a-tab-pane>
        <a-tab-pane key="REJECTED" title="已拒绝"></a-tab-pane>
      </a-tabs>
    </template>

    <!-- 搜索栏 -->
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索标题/内容/作者"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 - 标题（带Tooltip） -->
    <template #title="{ record }">
      <a-tooltip :content="record.title" position="top">
        <span class="ellipsis-text">{{ record.title }}</span>
      </a-tooltip>
    </template>

    <!-- 列插槽 - 作者（修复显示问题） -->
    <template #author="{ record }">
      <div class="author-cell">
        <a-avatar :size="24" :style="{ backgroundColor: getAvatarColor(record.authorName || record.userName || '用') }">
          {{ (record.authorName || record.userName || record.nickname || '用')[0]?.toUpperCase() }}
        </a-avatar>
        <a-tooltip :content="record.authorName || record.userName || record.nickname || '未知'" position="top">
          <span class="ellipsis-text author-name">{{ record.authorName || record.userName || record.nickname || '未知' }}</span>
        </a-tooltip>
      </div>
    </template>

    <!-- 列插槽 - 话题标签 -->
    <template #tags="{ record }">
      <a-space wrap>
        <a-tag v-for="tag in parseTags(record.tags)" :key="tag" size="small" color="arcoblue">{{ tag }}</a-tag>
      </a-space>
    </template>

    <!-- 列插槽 - 内容预览（带Tooltip） -->
    <template #content="{ record }">
      <a-tooltip :content="record.content" position="top">
        <span class="ellipsis-text content-preview">{{ truncate(record.content, 50) }}</span>
      </a-tooltip>
    </template>

    <!-- 列插槽 - 状态 -->
    <template #status="{ record }">
      <a-tag size="small" :color="getStatusColor(record.status)">{{ getStatusLabel(record.status) }}</a-tag>
    </template>

    <!-- 列插槽 - 时间 -->
    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <!-- 操作列 -->
    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="success" @click="approvePost(record)">通过</a-button>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="danger" @click="openRejectModal(record)">拒绝</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <!-- 详情抽屉 -->
  <a-drawer
    v-model:visible="detailVisible"
    :title="`帖子详情 - ${currentItem?.title || ''}`"
    :width="640"
    placement="right"
    unmount-on-close
  >
    <a-descriptions v-if="currentItem" :column="1" bordered size="medium">
      <a-descriptions-item label="帖子标题" :span="1">{{ currentItem.title }}</a-descriptions-item>
      <a-descriptions-item label="作者">
        {{ currentItem.authorName || currentItem.userName || currentItem.nickname || '未知' }}
      </a-descriptions-item>
      <a-descriptions-item label="话题标签">
        <a-space wrap>
          <a-tag v-for="tag in parseTags(currentItem.tags)" :key="tag" size="small" color="arcoblue">{{ tag }}</a-tag>
        </a-space>
      </a-descriptions-item>
      <a-descriptions-item label="点赞数">{{ currentItem.likeCount || 0 }}</a-descriptions-item>
      <a-descriptions-item label="评论数">{{ currentItem.commentCount || 0 }}</a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-tag size="small" :color="getStatusColor(currentItem.status)">{{ getStatusLabel(currentItem.status) }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="发布时间">{{ formatDate(currentItem.createdAt) }}</a-descriptions-item>
      <a-descriptions-item label="内容" :span="1">
        <div class="detail-content">{{ currentItem.content }}</div>
      </a-descriptions-item>
      <a-descriptions-item label="图片" :span="1" v-if="getFirstImage(currentItem)">
        <a-image :src="getFirstImage(currentItem)" width="200" fit="contain" style="border-radius:8px" />
      </a-descriptions-item>
    </a-descriptions>
    <template #footer>
      <a-space>
        <a-button v-if="currentItem?.status === 'PENDING'" type="primary" status="success" @click="approvePost(currentItem); detailVisible=false">通过</a-button>
        <a-button v-if="currentItem?.status === 'PENDING'" type="primary" status="danger" @click="openRejectModal(currentItem); detailVisible=false">拒绝</a-button>
        <a-button @click="detailVisible = false">关闭</a-button>
      </a-space>
    </template>
  </a-drawer>

  <!-- 拒绝弹窗 -->
  <a-modal v-model:visible="rejectModalVisible" title="拒绝原因" @ok="doReject" :ok-loading="rejecting">
    <a-textarea v-model="rejectReason" placeholder="请输入拒绝原因" :max-length="200" show-word-limit :auto-size="{ minRows: 3 }" />
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { opsHttp as http } from '../../../services/http'

const loading = ref(false)
const activeTab = ref('PENDING')
const keyword = ref('')
const tableData = ref([])
const rejectModalVisible = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const currentRejectItem = ref(null)
const detailVisible = ref(false)
const currentItem = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50]
})

// 表格列定义（对齐用户管理页面风格）
const tableColumns = computed(() => [
  { title: '帖子标题', dataIndex: 'title', width: 200, slotName: 'title', ellipsis: true },
  { title: '作者', dataIndex: 'authorName', width: 150, slotName: 'author' },
  { title: '话题标签', dataIndex: 'tags', width: 160, slotName: 'tags' },
  { title: '点赞数', dataIndex: 'likeCount', width: 80, align: 'center' },
  { title: '评论数', dataIndex: 'commentCount', width: 80, align: 'center' },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status', align: 'center' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 200, fixed: 'right', align: 'center', slotName: 'operations' }
])

function getAvatarColor(name) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1', '#14C9C9']
  const index = name ? name.charCodeAt(0) % colors.length : 0
  return colors[index]
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: activeTab.value !== 'PENDING' ? activeTab.value : undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    
    let url = '/circle/posts/list'
    if (activeTab.value === 'PENDING') {
      url = '/ops/circle/pending'
    }
    
    const res = await http.post(url, params)
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.posts || data?.items || data?.rows || []
    
    // 处理作者字段映射
    tableData.value = tableData.value.map(item => ({
      ...item,
      authorName: item.authorName || item.userName || item.nickname || item.author || '未知'
    }))
    
    pagination.total = data?.totalCount ?? data?.total ?? 0
  } catch (e) {
    console.error('[CircleReview] load error:', e)
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

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  activeTab.value = 'PENDING'
  handleSearch()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

function viewDetail(record) {
  currentItem.value = record
  detailVisible.value = true
}

async function approvePost(item) {
  try {
    await http.post(`/ops/circle/${item.id}/approve`)
    Message.success('帖子已通过审核')
    loadData()
    if (detailVisible.value) detailVisible.value = false
  } catch (e) { 
    Message.error('操作失败') 
  }
}

function openRejectModal(item) {
  currentRejectItem.value = item
  rejectReason.value = ''
  rejectModalVisible.value = true
}

async function doReject() {
  if (!rejectReason.value.trim()) { 
    Message.warning('请输入拒绝原因'); 
    return 
  }
  rejecting.value = true
  try {
    await http.post(`/ops/circle/${currentRejectItem.value.id}/reject`, { reason: rejectReason.value })
    Message.success('帖子已拒绝')
    rejectModalVisible.value = false
    loadData()
    if (detailVisible.value) detailVisible.value = false
  } catch (e) { 
    Message.error('操作失败') 
  } finally { 
    rejecting.value = false 
  }
}

function parseTags(rawTags) {
  if (!rawTags) return []
  if (Array.isArray(rawTags)) return rawTags
  return String(rawTags).split(',').map(t => t.trim()).filter(Boolean)
}

function getFirstImage(record) {
  const imgs = record.images || record.imageUrls || ''
  if (!imgs) return ''
  if (typeof imgs === 'string') {
    try { 
      const arr = JSON.parse(imgs); 
      return Array.isArray(arr) ? arr[0] : imgs 
    } catch { 
      return imgs 
    }
  }
  return Array.isArray(imgs) ? imgs[0] : ''
}

function getStatusColor(status) {
  return { APPROVED: 'green', PENDING: 'orange', REJECTED: 'red' }[status] || 'gray'
}

function getStatusLabel(status) {
  return { APPROVED: '已发布', PENDING: '待审核', REJECTED: '已拒绝' }[status] || status
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

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
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

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .author-name {
    max-width: 100px;
    font-weight: 500;
  }
}

.content-preview {
  max-width: 200px;
  color: #86909c;
  font-size: 13px;
}

.detail-content {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style>
