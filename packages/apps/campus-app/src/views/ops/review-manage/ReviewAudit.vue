<template>
  <OpsUnifiedTable
    :title="tabTitle"
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

    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索评价内容/用户名"
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

    <!-- 列插槽 -->
    <template #reviewer="{ record }">
      <div class="author-cell">
        <a-avatar :size="24" :style="{ backgroundColor: getAvatarColor(record) }">
          {{ (record.buyerName || record.userName || record.authorName || '用')[0]?.toUpperCase() }}
        </a-avatar>
        <a-tooltip :content="record.buyerName || record.userName || record.authorName || '未知'" position="top">
          <span class="ellipsis-text author-name">{{ record.buyerName || record.userName || record.authorName || '未知' }}</span>
        </a-tooltip>
      </div>
    </template>

    <template #target="{ record }">
      <a-tooltip :content="record.itemTitle || record.postTitle || `#ID${record.itemId || record.postId}`" position="top">
        <span class="ellipsis-text">{{ record.itemTitle || record.postTitle || `#ID${record.itemId || record.postId || '-'}` }}</span>
      </a-tooltip>
    </template>
    
    <template #itemImage="{ record }">
      <template v-if="activeTab.startsWith('item')">
        <a-image v-if="record.itemImage" :src="record.itemImage" width="60" height="60" fit="cover" style="border-radius:6px" />
        <span v-else>-</span>
      </template>
      <template v-else>
        <span style="color:#86909C;font-size:12px">圈子评论</span>
      </template>
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
      <a-tag size="small" :color="getStatusColor(record.status)">{{ getStatusLabel(record.status) }}</a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看</a-button>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="success" @click="approveReview(record)">通过</a-button>
        <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="danger" @click="rejectReview(record)">拒绝</a-button>
        <a-popconfirm v-if="record.status !== 'PENDING'" content="确定删除该评价吗？" @ok="deleteReview(record)">
          <a-button type="text" size="small" status="danger">删除</a-button>
        </a-popconfirm>
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
const keyword = ref('')
const tableData = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
})

// 根据当前Tab显示标题
const tabTitle = computed(() => {
  const titles = {
    'item-pending': '商品评价 - 待审核',
    'item-reviewed': '商品评价 - 已审核',
    'circle-pending': '圈子评价 - 待审核',
    'circle-reviewed': '圈子评价 - 已审核',
  }
  return titles[activeTab.value] || '评价审核'
})

// 表格列定义 (对齐圈子管理格式)
const tableColumns = computed(() => {
  const isItem = activeTab.value.startsWith('item')
  const baseColumns = [
    { title: isItem ? '商品图片' : '评论对象', dataIndex: 'itemImage', width: 80, slotName: 'itemImage', align: 'center' },
    { title: '评价人', dataIndex: 'reviewer', width: 140, slotName: 'reviewer' },
    { title: isItem ? '评价对象' : '所属帖子', dataIndex: 'target', width: 160, slotName: 'target' },
    { title: '评价内容', dataIndex: 'content', slotName: 'content' },
  ]
  
  // 商品评价显示评分列
  if (isItem) {
    baseColumns.push({ title: '评分', dataIndex: 'rating', width: 120, slotName: 'rating', align: 'center' })
  }
  
  baseColumns.push(
    { title: '状态', dataIndex: 'status', width: 90, slotName: 'status', align: 'center' },
    { title: '评价时间', dataIndex: 'createdAt', width: 150, slotName: 'createdAt' },
    { title: '操作', width: 180, slotName: 'operations', fixed: 'right', align: 'center' }
  )
  
  return baseColumns
})

function getStatusLabel(status) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status || '-'
}

function getStatusColor(status) {
  return { PENDING: 'orange', APPROVED: 'green', REJECTED: 'red' }[status] || 'gray'
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
  
  // 商品评价使用 /ops/reviews，圈子评论使用 /circle/comments
  if (isItem) return '/ops/reviews'
  return '/circle/comments/list'
}

function getApiParams() {
  const isPending = activeTab.value.endsWith('pending')
  return {
    status: isPending ? 'PENDING' : 'APPROVED',
    keyword: keyword.value || undefined,
    pageNo: pagination.current,
    pageSize: pagination.pageSize,
  }
}

async function loadData() {
  loading.value = true
  try {
    const endpoint = getApiEndpoint()
    const params = getApiParams()
    const isItem = activeTab.value.startsWith('item')
    
    console.log('[ReviewAudit] 请求参数:', { endpoint, params, isItem })
    const res = await http.post(endpoint, params)
    console.log('[ReviewAudit] API返回:', res)
    
    const data = res?.data?.data ?? res?.data ?? res
    let items = data?.reviews || data?.comments || data?.items || data?.rows || []
    
    // 数据处理和字段映射
    if (isItem) {
      // 商品评价字段映射
      items = items.map(item => ({
        ...item,
        buyerName: item.buyerName || item.userName || item.buyerNickname || '未知',
        itemTitle: item.itemTitle || item.targetTitle || null,
        itemImage: item.itemImage || null
      }))
    } else {
      // 圈子评论字段映射
      items = items.map(item => ({
        ...item,
        buyerName: item.userName || item.authorName || '未知',
        postTitle: item.postTitle || `帖子#${item.postId}` || null,
        itemImage: null,
        rating: null // 圈子评论没有评分
      }))
    }
    
    tableData.value = items
    pagination.total = data?.totalCount ?? data?.total ?? tableData.value.length
    
    console.log('[ReviewAudit] 数据加载完成:', { 
      数据量: tableData.value.length, 
      总数: pagination.total,
      当前Tab: activeTab.value 
    })
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
  keyword.value = ''
  loadData()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  handleSearch()
}

function viewDetail(record) {
  if (record.itemId) {
    window.open(`/portal/item/${record.itemId}`, '_blank')
  } else {
    Message.info('无法查看详情：缺少关联信息')
  }
}

async function approveReview(item) {
  try {
    console.log('[ReviewAudit] 通过评价:', item.id)
    const isItem = activeTab.value.startsWith('item')
    if (isItem) {
      await http.post(`/ops/reviews/${item.id}/approve`)
    } else {
      await http.post(`/circle/comments/${item.id}/approve`)
    }
    Message.success('已通过审核')
    loadData()
  } catch (e) {
    console.error('[ReviewAudit] 通过失败:', e)
    Message.error('操作失败')
  }
}

async function rejectReview(item) {
  try {
    console.log('[ReviewAudit] 拒绝评价:', item.id)
    const isItem = activeTab.value.startsWith('item')
    if (isItem) {
      await http.post(`/ops/reviews/${item.id}/reject`, { reason: '不符合评价规范' })
    } else {
      await http.post(`/circle/comments/${item.id}/reject`)
    }
    Message.success('已拒绝')
    loadData()
  } catch (e) {
    console.error('[ReviewAudit] 拒绝失败:', e)
    Message.error('操作失败')
  }
}

async function deleteReview(item) {
  try {
    console.log('[ReviewAudit] 删除评价:', item.id)
    const isItem = activeTab.value.startsWith('item')
    if (isItem) {
      await http.post(`/ops/reviews/${item.id}/reject`, { reason: '运营删除' })
    } else {
      await http.post(`/circle/comments/${item.id}/reject`)
    }
    Message.success('已删除')
    loadData()
  } catch (e) {
    console.error('[ReviewAudit] 删除失败:', e)
    Message.error('删除失败')
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

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .author-name {
    max-width: 100px;
    font-weight: 500;
  }
}

.content-text {
  max-width: 200px;
  color: #4e5969;
  font-size: 13px;
}
</style>