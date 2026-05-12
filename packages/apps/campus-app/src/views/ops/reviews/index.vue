<template>
  <OpsUnifiedTable
    title="商品审核"
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
      <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
        <a-tab-pane key="pending">
          <template #title>待审核</template>
        </a-tab-pane>
        <a-tab-pane key="approved">
          <template #title>已通过</template>
        </a-tab-pane>
        <a-tab-pane key="rejected">
          <template #title>已拒绝</template>
        </a-tab-pane>
      </a-tabs>
    </template>

    <!-- 操作栏 -->
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索商品名称/卖家"
        style="width: 200px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="categoryFilter" placeholder="商品分类" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="cat in categories" :key="cat.value" :value="cat.value">{{ cat.label }}</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 -->
    <template #image="{ record }">
      <a-image :src="getFirstImage(record)" width="60" height="60" fit="cover" style="border-radius:6px" />
    </template>

    <template #price="{ record }">
      <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(record.price) }}</span>
    </template>

    <template #category="{ record }">
      <a-tag size="small" color="blue">{{ getCategoryLabel(record.category) }}</a-tag>
    </template>

    <template #status="{ record }">
      <a-tag size="small" :color="getStatusColor(record.reviewStatus)">{{ getStatusLabel(record.reviewStatus) }}</a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
        <a-button v-if="record.reviewStatus === 'PENDING_REVIEW'" type="text" size="small" status="success" @click="approveItem(record)">通过</a-button>
        <a-button v-if="record.reviewStatus === 'PENDING_REVIEW'" type="text" size="small" status="danger" @click="rejectItem(record)">拒绝</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <!-- 商品详情抽屉 -->
  <a-drawer v-model:visible="detailVisible" :width="600" title="商品详情" placement="right" unmount-on-close>
    <div v-if="currentItem">
      <div style="margin-bottom:16px">
        <a-image-preview-group>
          <a-space wrap>
            <a-image v-for="(img, i) in getImages(currentItem)" :key="i" :src="img" width="90" height="90" fit="cover" style="border-radius:6px" />
          </a-space>
        </a-image-preview-group>
      </div>
      <a-descriptions :column="2" bordered size="medium">
        <a-descriptions-item label="商品名称" :span="2"><span style="font-weight:600">{{ currentItem.title }}</span></a-descriptions-item>
        <a-descriptions-item label="价格"><span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(currentItem.price) }}</span></a-descriptions-item>
        <a-descriptions-item label="分类">{{ getCategoryLabel(currentItem.category) }}</a-descriptions-item>
        <a-descriptions-item label="卖家">{{ currentItem.sellerName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag size="small" :color="getStatusColor(currentItem.reviewStatus)">{{ getStatusLabel(currentItem.reviewStatus) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="发布时间">{{ formatDate(currentItem.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="商品描述" :span="2"><span style="white-space:pre-wrap">{{ currentItem.description || '-' }}</span></a-descriptions-item>
      </a-descriptions>
    </div>
    <template #footer><a-button @click="detailVisible = false">关闭</a-button></template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { opsHttp as http } from '../../../services/http'
import { loadEnums } from '../../../services/enums'

const loading = ref(false)
const keyword = ref('')
const categoryFilter = ref('')
const categories = ref([])
const activeTab = ref('pending')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })
const detailVisible = ref(false)
const currentItem = ref(null)

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = [
  { title: '商品图片', dataIndex: 'image', width: 100, slotName: 'image' },
  { title: '商品名称', dataIndex: 'title', width: 200, ellipsis: true },
  { title: '卖家', dataIndex: 'sellerName', width: 120 },
  { title: '价格', dataIndex: 'price', width: 100, slotName: 'price' },
  { title: '分类', dataIndex: 'category', width: 120, slotName: 'category' },
  { title: '状态', dataIndex: 'reviewStatus', width: 100, slotName: 'status' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 200, fixed: 'right', slotName: 'operations' }
]

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    console.log('[ItemReview] 当前Tab:', activeTab.value, '请求参数:', params)

    let res
    if (activeTab.value === 'pending') {
      // 待审核商品调用 /ops/pending-items
      res = await http.post('/ops/pending-items', params)
    } else {
      // 已通过/已拒绝调用商品列表接口，传入对应状态
      const status = activeTab.value === 'approved' ? 'APPROVED' : 'REJECTED'
      params.status = status
      res = await http.post('/items/list', params)
    }

    console.log('[ItemReview] 响应:', res)
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.items || data?.rows || []
    pagination.total = data?.totalCount ?? data?.total ?? 0
    console.log('[ItemReview] 数据条数:', tableData.value.length, '状态:', tableData.value.map(i => i.reviewStatus))
  } catch (e) {
    console.error('[ItemReview] load error:', e)
    Message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pagination.current = 1
  loadData()
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; categoryFilter.value = ''; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }

function viewDetail(record) { currentItem.value = record; detailVisible.value = true }

async function approveItem(record) {
  try {
    await http.post(`/ops/items/${record.id}/approve`)
    Message.success('商品已通过审核')
    loadData()
  } catch (e) {
    console.error('[ItemReview] approve error:', e)
    Message.error('操作失败')
  }
}

async function rejectItem(record) {
  try {
    await http.post(`/ops/items/${record.id}/reject`, { reason: '不符合规范' })
    Message.success('商品已拒绝')
    loadData()
  } catch (e) {
    console.error('[ItemReview] reject error:', e)
    Message.error('操作失败')
  }
}

function getFirstImage(record) {
  const urls = record.imageUrls || record.images || ''
  if (!urls) return ''
  if (typeof urls === 'string') { try { return JSON.parse(urls)[0] || '' } catch { return urls || '' } }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : ''
}

function getImages(record) {
  const urls = record.imageUrls || record.images || ''
  if (!urls) return []
  if (typeof urls === 'string') { try { return JSON.parse(urls) } catch { return [urls] } }
  return Array.isArray(urls) ? urls : []
}

function formatPrice(price) { const n = Number(price); return isNaN(n) ? '0.00' : n.toFixed(2) }

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function getStatusColor(status) {
  return { APPROVED: 'green', PENDING_REVIEW: 'orange', REJECTED: 'red' }[status] || 'gray'
}

function getStatusLabel(status) {
  return { APPROVED: '已通过', PENDING_REVIEW: '待审核', REJECTED: '已拒绝' }[status] || status
}

async function loadDict() {
  try {
    const dict = await loadEnums()
    categories.value = dict?.categories || []
  } catch (e) {
    console.error('[ItemReview] load dict error:', e)
  }
}

function getCategoryLabel(category) {
  const cat = categories.value.find(c => c.value === category)
  return cat?.label || category
}

onMounted(() => { loadDict(); loadData() })
</script>

<style lang="scss" scoped>
</style>
