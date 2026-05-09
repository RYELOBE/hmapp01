<template>
  <OpsUnifiedTable
    title="圈子管理"
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
        placeholder="搜索帖子标题/作者"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="tagFilter" placeholder="话题标签" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="tag in tags" :key="tag.value" :value="tag.value">{{ tag.label }}</a-option>
      </a-select>
      <a-select v-model="statusFilter" placeholder="帖子状态" style="width: 140px" allow-clear @change="handleSearch">
        <a-option value="PENDING">待审核</a-option>
        <a-option value="APPROVED">已发布</a-option>
        <a-option value="REJECTED">已拒绝</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 -->
    <template #tags="{ record }">
      <a-space wrap>
        <a-tag v-for="tag in parseTags(record.tags)" :key="tag" size="small" color="arcoblue">{{ tag }}</a-tag>
      </a-space>
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
        <a-button v-if="record.status === 'APPROVED'" type="text" size="small" status="warning" @click="hidePost(record)">隐藏</a-button>
        <a-popconfirm content="确定删除该帖子吗？" @ok="deletePost(record)">
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
import { loadEnums } from '../../../services/enums'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const tagFilter = ref('')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })
const tags = ref([])

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '帖子标题', dataIndex: 'title', width: 250, ellipsis: true },
  { title: '作者', dataIndex: 'authorName', width: 120 },
  { title: '话题标签', dataIndex: 'tags', width: 160, slotName: 'tags' },
  { title: '点赞数', dataIndex: 'likeCount', width: 80, align: 'center' },
  { title: '评论数', dataIndex: 'commentCount', width: 80, align: 'center' },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 180, fixed: 'right', slotName: 'operations' }
])

async function loadDict() {
  try {
    const dict = await loadEnums()
    tags.value = dict?.tags || []
  } catch (e) {
    console.error('[CircleManage] loadDict error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      tag: tagFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    const res = await http.post('/ops/circle/posts', params)
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.posts || data?.items || data?.rows || []
    pagination.total = data?.totalCount ?? data?.total ?? 0
  } catch (e) {
    console.error('[CircleManage] load error:', e)
    Message.error('加载帖子列表失败')
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
  tagFilter.value = ''
  statusFilter.value = ''
  handleSearch()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

function viewDetail(record) {
  window.open(`/portal/circle/${record.id}`, '_blank')
}

async function hidePost(record) {
  try {
    await http.post(`/ops/circle/${record.id}/reject`, { reason: '运营隐藏' })
    Message.success('帖子已隐藏')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

async function deletePost(record) {
  try {
    await http.post(`/ops/circle/${record.id}/reject`, { reason: '运营删除' })
    Message.success('帖子已处理')
    loadData()
  } catch (e) {
    Message.error('删除失败')
  }
}

// 解析 tags 字段（可能是逗号分隔字符串或数组）
function parseTags(tags) {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  return String(tags).split(',').map(t => t.trim()).filter(Boolean)
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
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

onMounted(() => {
  loadDict()
  loadData()
})
</script>

<style lang="scss" scoped>
</style>
