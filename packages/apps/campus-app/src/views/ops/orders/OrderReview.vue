<template>
  <OpsUnifiedTable
    title="退款管理"
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
        <a-tab-pane key="REFUNDING" title="待退款"></a-tab-pane>
        <a-tab-pane key="REFUNDED" title="已退款"></a-tab-pane>
        <a-tab-pane key="" title="全部订单"></a-tab-pane>
      </a-tabs>
    </template>

    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索订单号/买家/商品"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
    </template>

    <template #extra>
      <a-space>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </template>

    <template #orderNo="{ record }">
      <a-typography-text code style="font-size:12px">{{ record.orderNo || record.id }}</a-typography-text>
    </template>

    <template #totalAmount="{ record }">
      <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(record.totalAmount) }}</span>
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
        <a-button v-if="record.status === 'REFUNDING'" type="text" size="small" status="success" @click="approveOrder(record)">通过</a-button>
        <a-button v-if="record.status === 'REFUNDING'" type="text" size="small" status="danger" @click="rejectOrder(record)">拒绝</a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

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
        <a-tag size="small" :color="getStatusColor(currentOrder.status)">{{ getStatusLabel(currentOrder.status) }}</a-tag>
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

  <a-modal
    v-model:visible="rejectModalVisible"
    title="拒绝退款原因"
    @ok="confirmReject"
    @cancel="rejectModalVisible = false"
    :ok-loading="submitLoading"
  >
    <a-form :model="rejectForm" layout="vertical">
      <a-form-item label="请输入拒绝原因（必填）" field="reason" required>
        <a-textarea
          v-model="rejectForm.reason"
          placeholder="请输入拒绝原因"
          :max-length="200"
          show-word-limit
          :auto-size="{ minRows: 3, maxRows: 6 }"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { opsHttp as http } from '../../../services/http'

const loading = ref(false)
const submitLoading = ref(false)
const keyword = ref('')
const activeTab = ref('REFUNDING')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 15, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })
const detailVisible = ref(false)
const rejectModalVisible = ref(false)
const currentOrder = ref(null)

const rejectForm = reactive({
  reason: ''
})

const statusMap = {
  PENDING_PAYMENT: { label: '待支付', color: 'orange' },
  PAID: { label: '已支付', color: 'blue' },
  COMPLETED: { label: '已完成', color: 'green' },
  REFUNDING: { label: '退款中', color: 'orangered' },
  REFUNDED: { label: '已退款', color: 'gray' },
  CANCELLED: { label: '已取消', color: 'gray' }
}

const tableColumns = [
  { title: '订单号', dataIndex: 'orderNo', width: 160, slotName: 'orderNo' },
  { title: '商品', dataIndex: 'itemTitle', width: 200, ellipsis: true },
  { title: '买家', dataIndex: 'buyerName', width: 100 },
  { title: '卖家', dataIndex: 'sellerName', width: 100 },
  { title: '金额', dataIndex: 'totalAmount', width: 100, slotName: 'totalAmount' },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status' },
  { title: '申请时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 160, fixed: 'right', slotName: 'operations' }
]

function getStatusLabel(status) {
  return statusMap[status]?.label || status || '-'
}

function getStatusColor(status) {
  return statusMap[status]?.color || 'gray'
}

async function loadData() {
  loading.value = true
  try {
    const res = await http.post('/ops/orders', {
      keyword: keyword.value || undefined,
      status: activeTab.value,
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

function handleTabChange() {
  tableData.value = []
  pagination.current = 1
  loadData()
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; activeTab.value = 'REFUNDING'; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }

function viewDetail(record) {
  currentOrder.value = record
  detailVisible.value = true
}

async function approveOrder(record) {
  try {
    await http.post(`/ops/orders/${record.id}/refund/approve`)
    Message.success('退款已通过')
    loadData()
  } catch (e) {
    Message.error('操作失败')
  }
}

async function rejectOrder(record) {
  currentOrder.value = record
  rejectForm.reason = ''
  rejectModalVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.reason.trim()) {
    Message.warning('请输入拒绝原因')
    return
  }
  
  submitLoading.value = true
  try {
    await http.post(`/ops/orders/${currentOrder.value.id}/refund/reject`, { reason: rejectForm.reason })
    Message.success('退款已拒绝')
    rejectModalVisible.value = false
    loadData()
  } catch (e) {
    Message.error('操作失败')
  } finally {
    submitLoading.value = false
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

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>
:deep(.scene-review-page) {
  overflow-x: hidden;
}

:deep(.scene-review-table) {
  overflow: hidden;
}
</style>