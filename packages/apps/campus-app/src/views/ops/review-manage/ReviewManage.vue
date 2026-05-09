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
        <a-option v-for="t in reviewTypes.filter(x => x.value)" :key="t.value" :value="t.value">{{ t.label }}</a-option>
      </a-select>
      <a-select v-model="statusFilter" placeholder="评价状态" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="s in reviewModerationStatuses.filter(x => x.value)" :key="s.value" :value="s.value">{{ s.label }}</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 -->
    <template #type="{ record }">
      <a-tag size="small" :color="getReviewTypeColor(record.type, reviewTypes)">
        {{ getReviewTypeLabel(record.type, reviewTypes) }}
      </a-tag>
    </template>

    <template #rating="{ record }">
      <a-rate v-if="record.type === 'ITEM'" :model-value="record.rating || 0" disabled allow-half :count="5" />
      <span v-else>-</span>
    </template>

    <template #status="{ record }">
      <a-tag size="small" :color="getStatusColor(record.status, reviewModerationStatuses)">
        {{ getStatusLabel(record.status, reviewModerationStatuses) }}
      </a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ record.createdAt }}
    </template>

    <template #operations="{ record }">
      <a-space>
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
import { opsHttp as http } from '../../../services/http'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { getReviewTypes, getReviewModerationStatuses, getStatusLabel, getStatusColor } from '../../../services/enums'

const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })

const reviewTypes = ref([])
const reviewModerationStatuses = ref([])

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '评价类型', dataIndex: 'type', width: 100, slotName: 'type' },
  { title: '评价内容', dataIndex: 'content', width: 300, ellipsis: true },
  { title: '评价人', dataIndex: 'buyerName', width: 120 },
  { title: '评分', dataIndex: 'rating', width: 120, slotName: 'rating' },
  { title: '关联对象', dataIndex: 'targetTitle', width: 180, ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 160, fixed: 'right', slotName: 'operations' }
])

async function loadEnums() {
  try {
    const [types, statuses] = await Promise.all([
      getReviewTypes(),
      getReviewModerationStatuses(),
    ])
    reviewTypes.value = types || []
    reviewModerationStatuses.value = statuses || []
  } catch (e) {
    console.error('[ReviewManage] loadEnums error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    console.log('[ReviewManage] 请求参数:', params)
    const res = await http.post('/ops/reviews', params)
    console.log('[ReviewManage] API返回:', res)
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.reviews || data?.rows || data?.items || []
    pagination.total = data?.totalCount ?? data?.total ?? 0
    console.log('[ReviewManage] 数据加载完成:', { 
      数据量: tableData.value.length, 
      总数: pagination.total 
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

function getReviewTypeLabel(type, typeList = []) {
  const item = typeList.find(item => item.value === type)
  return item ? item.label : type
}

function getReviewTypeColor(type, typeList = []) {
  const item = typeList.find(item => item.value === type)
  return item ? (item.color || 'blue') : 'gray'
}

onMounted(async () => {
  await loadEnums()
  loadData()
})
</script>

<style lang="scss" scoped>
</style>
