<template>
  <OpsUnifiedTable
    title="评价管理"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
  >
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索评价内容/用户名"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="typeFilter" placeholder="评价类型" style="width: 140px" allow-clear @change="handleSearch">
        <a-option value="ITEM">商品评价</a-option>
        <a-option value="ORDER">订单评价</a-option>
      </a-select>
      <a-select v-model="statusFilter" placeholder="评价状态" style="width: 140px" allow-clear @change="handleSearch">
        <a-option value="PENDING">待审核</a-option>
        <a-option value="APPROVED">已通过</a-option>
        <a-option value="REJECTED">已拒绝</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 -->
    <template #type="{ record }">
      <a-tag size="small" :color="record.type === 'ITEM' ? 'arcoblue' : 'green'">
        {{ record.type === 'ITEM' ? '商品评价' : (record.type || '-') }}
      </a-tag>
    </template>

    <template #content="{ record }">
      <a-tooltip :content="record.content" position="top">
        <span class="ellipsis-text">{{ record.content || '-' }}</span>
      </a-tooltip>
    </template>

    <template #buyerName="{ record }">
      <div class="author-cell">
        <a-avatar :size="24" :style="{ backgroundColor: getAvatarColor(record.buyerName || record.userName || '用') }">
          {{ (record.buyerName || record.userName || record.authorName || '用')[0]?.toUpperCase() }}
        </a-avatar>
        <a-tooltip :content="record.buyerName || record.userName || record.nickname || '未知'" position="top">
          <span class="ellipsis-text author-name">{{ record.buyerName || record.userName || record.nickname || '未知' }}</span>
        </a-tooltip>
      </div>
    </template>

    <template #rating="{ record }">
      <a-rate v-if="record.rating && record.type === 'ITEM'" :model-value="record.rating" disabled allow-half :count="5" size="small" />
      <span v-else>-</span>
    </template>

    <template #targetTitle="{ record }">
      <a-tooltip :content="record.targetTitle || record.itemTitle || `#ID${record.itemId}`" position="top">
        <span class="ellipsis-text">{{ record.targetTitle || record.itemTitle || `#ID${record.itemId || '-'}` }}</span>
      </a-tooltip>
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
        <a-popconfirm content="确定删除该评价吗？" @ok="deleteReview(record)">
          <a-button type="text" size="small" status="danger">删除</a-button>
        </a-popconfirm>
      </a-space>
    </template>
  </OpsUnifiedTable>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { opsHttp as http } from '../../../services/http'

const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })

// 表格列定义 (对齐圈子管理格式)
const tableColumns = computed(() => [
  { title: '评价类型', dataIndex: 'type', width: 100, slotName: 'type', align: 'center' },
  { title: '评价内容', dataIndex: 'content', width: 250, slotName: 'content', ellipsis: true },
  { title: '评价人', dataIndex: 'buyerName', width: 140, slotName: 'buyerName' },
  { title: '评分', dataIndex: 'rating', width: 120, slotName: 'rating', align: 'center' },
  { title: '关联对象', dataIndex: 'targetTitle', width: 180, slotName: 'targetTitle', ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status', align: 'center' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 180, fixed: 'right', slotName: 'operations' }
])

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      category: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    console.log('[ReviewManage] 请求参数:', params)
    const res = await http.post('/ops/reviews', params)
    console.log('[ReviewManage] API返回:', res)
    const data = res?.data?.data ?? res?.data ?? res
    
    let items = data?.reviews || data?.items || data?.rows || []
    
    // 数据处理和字段映射
    items = items.map(item => ({
      ...item,
      buyerName: item.buyerName || item.userName || item.buyerNickname || item.authorName || '未知',
      targetTitle: item.targetTitle || item.itemTitle || null,
      type: item.type || 'ITEM'
    }))
    
    tableData.value = items
    pagination.total = data?.totalCount ?? data?.total ?? 0
    console.log('[ReviewManage] 数据加载完成:', { 
      数据量: tableData.value.length, 
      总数: pagination.total,
      样本数据: tableData.value[0] ? Object.keys(tableData.value[0]) : [] 
    })
  } catch (e) {
    console.error('[ReviewManage] load error:', e)
    Message.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
  handleSearch()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

function viewDetail(record) {
  if (record.itemId) {
    window.open(`/portal/item/${record.itemId}`, '_blank')
  } else {
    Message.info('无法查看详情：缺少关联信息')
  }
}

async function approveReview(record) {
  try {
    console.log('[ReviewManage] 通过评价:', record.id)
    const res = await http.post(`/ops/reviews/${record.id}/approve`)
    console.log('[ReviewManage] 通过成功:', res)
    Message.success('评价已通过')
    loadData()
  } catch (e) {
    console.error('[ReviewManage] 通过失败:', e)
    Message.error('操作失败: ' + (e.message || '未知错误'))
  }
}

async function deleteReview(record) {
  try {
    console.log('[ReviewManage] 删除评价:', record.id)
    const res = await http.post(`/ops/reviews/${record.id}/reject`, { reason: '运营删除' })
    console.log('[ReviewManage] 删除成功:', res)
    Message.success('评价已删除')
    loadData()
  } catch (e) {
    console.error('[ReviewManage] 删除失败:', e)
    Message.error('删除失败: ' + (e.message || '未知错误'))
  }
}

function getAvatarColor(name) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1', '#14C9C9']
  const index = name ? name.charCodeAt(0) % colors.length : 0
  return colors[index]
}

function getStatusColor(status) {
  return { APPROVED: 'green', PENDING: 'orange', REJECTED: 'red' }[status] || 'gray'
}

function getStatusLabel(status) {
  return { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已拒绝' }[status] || status || '-'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
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
</style>