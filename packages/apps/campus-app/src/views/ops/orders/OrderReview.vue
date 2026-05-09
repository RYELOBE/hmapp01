<template>
  <OpsUnifiedTable
    title="订单审核"
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
        placeholder="搜索订单号/买家/商品"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="statusFilter" placeholder="订单状态" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="s in orderStatuses" :key="s.value" :value="s.value">{{ s.label }}</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <template #orderNo="{ record }">
      <a-typography-text code style="font-size:12px">{{ record.orderNo || record.id }}</a-typography-text>
    </template>

    <template #totalAmount="{ record }">
      <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(record.totalAmount) }}</span>
    </template>

    <template #status="{ record }">
      <a-tag size="small" :color="getStatusColor(record.status, orderStatuses)">{{ getStatusLabel(record.status, orderStatuses) }}</a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
        <a-button v-if="record.status === 'REFUNDING'" type="primary" size="small" status="success" @click="approveOrder(record)">通过退款</a-button>
        <a-button v-if="record.status === 'REFUNDING'" type="primary" size="small" status="danger" @click="rejectOrder(record)">拒绝退款</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <!-- 详情抽屉 -->
  <a-drawer
    v-model:visible="detailVisible"
    :title="`订单详情 - ${currentOrder?.orderNo || ''}`"
    :width="560"
    placement="right"
    unmount-on-close
  >
    <a-descriptions v-if="currentOrder" :column="2" bordered size="medium">
      <a-descriptions-item label="订单号" :span="2">
        <a-typography-text code>{{ currentOrder.orderNo || currentOrder.id }}</a-typography-text>
      </a-descriptions-item>
      <a-descriptions-item label="商品名称" :span="2">{{ currentOrder.itemTitle }}</a-descriptions-item>
      <a-descriptions-item label="买家">{{ currentOrder.buyerName }}</a-descriptions-item>
      <a-descriptions-item label="卖家">{{ currentOrder.sellerName }}</a-descriptions-item>
      <a-descriptions-item label="商品价格">¥{{ formatPrice(currentOrder.price) }}</a-descriptions-item>
      <a-descriptions-item label="订单金额">
        <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(currentOrder.totalAmount) }}</span>
      </a-descriptions-item>
      <a-descriptions-item label="订单状态">
        <a-tag size="small" :color="getStatusColor(currentOrder.status, orderStatuses)">{{ getStatusLabel(currentOrder.status, orderStatuses) }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="下单时间">{{ formatDate(currentOrder.createdAt) }}</a-descriptions-item>
      <a-descriptions-item label="收货人">{{ currentOrder.receiverName || '-' }}</a-descriptions-item>
      <a-descriptions-item label="收货电话">{{ currentOrder.receiverPhone || '-' }}</a-descriptions-item>
      <a-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress || '-' }}</a-descriptions-item>
    </a-descriptions>
    <template #footer>
      <a-space>
        <a-button v-if="currentOrder?.status === 'REFUNDING'" type="primary" status="success" @click="approveOrder(currentOrder); detailVisible=false">通过退款</a-button>
        <a-button v-if="currentOrder?.status === 'REFUNDING'" type="primary" status="danger" @click="rejectOrder(currentOrder); detailVisible=false">拒绝退款</a-button>
        <a-button @click="detailVisible = false">关闭</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { opsHttp as http } from '../../../services/http'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { getOrderStatuses, getStatusLabel, getStatusColor } from '../../../services/enums'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('REFUNDING')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })
const detailVisible = ref(false)
const currentOrder = ref(null)

const orderStatuses = ref([])

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '订单号', dataIndex: 'orderNo', width: 160, slotName: 'orderNo' },
  { title: '商品', dataIndex: 'itemTitle', width: 200, ellipsis: true },
  { title: '买家', dataIndex: 'buyerName', width: 100 },
  { title: '卖家', dataIndex: 'sellerName', width: 100 },
  { title: '金额', dataIndex: 'totalAmount', width: 100, slotName: 'totalAmount' },
  { title: '状态', dataIndex: 'status', width: 110, slotName: 'status' },
  { title: '申请时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 200, fixed: 'right', slotName: 'operations' }
])

async function loadEnums() {
  try {
    orderStatuses.value = await getOrderStatuses()
  } catch (e) {
    console.error('[OrderReview] loadEnums error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await http.post('/ops/orders', {
      keyword: keyword.value || undefined,
      status: statusFilter.value || 'REFUNDING',
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    })
    const data = res?.data?.data ?? res?.data ?? res
    tableData.value = data?.orders || data?.rows || []
    pagination.total = data?.totalCount ?? data?.total ?? 0
  } catch (e) {
    console.error('[OrderReview] load error:', e)
    Message.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; statusFilter.value = 'REFUNDING'; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }

function viewDetail(record) {
  currentOrder.value = record
  detailVisible.value = true
}

async function approveOrder(record) {
  try {
    await http.post(`/orders/${record.id}/refund/approve`)
    Message.success('退款已通过')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

async function rejectOrder(record) {
  try {
    await http.post(`/orders/${record.id}/refund/reject`)
    Message.success('退款已拒绝')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

function formatPrice(price) {
  const n = Number(price)
  return isNaN(n) ? '0.00' : n.toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => { await loadEnums(); loadData() })
</script>

<style lang="scss" scoped>
</style>
