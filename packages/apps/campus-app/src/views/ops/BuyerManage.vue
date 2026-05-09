<template>
  <div class="buyer-page">
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="8" v-for="card in statsCards" :key="card.key">
        <a-card :bordered="false" class="stat-card" :class="`stat-card--${card.type}`">
          <a-statistic :title="card.title" :value="stats[card.key] ?? 0" :value-from="0" :duration="600">
            <template #prefix><component :is="card.icon" class="stat-icon" /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 统一表格 -->
    <OpsUnifiedTable
      title="买家列表"
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
          placeholder="搜索买家用户名"
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
      <template #username="{ record }">
        <a-space>
          <a-avatar :size="32" :style="{ backgroundColor: '#722ed1' }">
            {{ (record.username || '需')[0]?.toUpperCase() }}
          </a-avatar>
          <div>
            <div style="font-weight:500">{{ record.username }}</div>
            <div style="font-size:12px;color:#86909c">ID: {{ record.id }}</div>
          </div>
        </a-space>
      </template>

      <template #totalOrders="{ record }">
        <a-badge :text="toNum(record.totalOrders)" :color="toNum(record.totalOrders) > 0 ? 'arcoblue' : 'gray'" />
      </template>

      <template #totalSpent="{ record }">
        <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(record.totalSpent) }}</span>
      </template>

      <template #completedOrders="{ record }">
        <a-tag color="green" size="small">{{ toNum(record.completedOrders) }}</a-tag>
      </template>

      <template #cancelledOrders="{ record }">
        <a-tag color="gray" size="small">{{ toNum(record.cancelledOrders) }}</a-tag>
      </template>

      <template #avgOrderValue="{ record }">
        ¥{{ formatPrice(record.avgOrderValue) }}
      </template>

      <template #createdAt="{ record }">
        {{ formatDate(record.createdAt) }}
      </template>

      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status, userStatuses)" size="small">
          {{ getStatusLabel(record.status, userStatuses) }}
        </a-tag>
      </template>

      <template #operations="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
          <a-button
            type="text"
            size="small"
            :status="record.status === 'DISABLED' ? 'success' : 'danger'"
            @click="toggleStatus(record)"
          >
            {{ record.status === 'DISABLED' ? '启用' : '禁用' }}
          </a-button>
        </a-space>
      </template>
    </OpsUnifiedTable>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:visible="detailVisible"
      :title="`买家详情 - ${currentBuyer?.username || ''}`"
      :width="520"
      placement="right"
      unmount-on-close
    >
      <a-descriptions v-if="currentBuyer" :column="2" bordered size="medium">
        <a-descriptions-item label="用户ID">{{ currentBuyer.id }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ currentBuyer.username }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentBuyer.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="注册时间">{{ formatDate(currentBuyer.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="订单总数">
          <a-badge :text="toNum(currentBuyer.totalOrders)" />
        </a-descriptions-item>
        <a-descriptions-item label="已完成">
          <a-tag color="green" size="small">{{ toNum(currentBuyer.completedOrders) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="已取消">{{ toNum(currentBuyer.cancelledOrders) }}</a-descriptions-item>
        <a-descriptions-item label="总消费">
          <span style="color:#f53f3f;font-weight:600;font-size:16px">¥{{ formatPrice(currentBuyer.totalSpent) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="平均订单金额">¥{{ formatPrice(currentBuyer.avgOrderValue) }}</a-descriptions-item>
        <a-descriptions-item label="最后购买">{{ formatDate(currentBuyer.lastOrderAt) }}</a-descriptions-item>
      </a-descriptions>
      <template #footer>
        <a-button @click="detailVisible = false">关闭</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconList, IconFire, IconBarChart } from '@arco-design/web-vue/es/icon'
import OpsUnifiedTable from '../../components/ops/OpsUnifiedTable.vue'
import { getBuyers, getBuyerDetail, updateUserStatus, getBriefStats } from '../../services/ops/index'
import { getUserStatuses, getStatusLabel, getStatusColor } from '../../services/enums'

const loading = ref(false)
const keyword = ref('')
const tableData = ref([])
const stats = ref({})
const pagination = reactive({ current: 1, pageSize: 15, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })

// 枚举数据
const userStatuses = ref([])

const detailVisible = ref(false)
const currentBuyer = ref(null)

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '买家', dataIndex: 'username', width: 180, fixed: 'left', slotName: 'username' },
  { title: '累计订单数', dataIndex: 'totalOrders', width: 110, align: 'center', slotName: 'totalOrders' },
  { title: '累计消费', dataIndex: 'totalSpent', width: 120, align: 'right', slotName: 'totalSpent' },
  { title: '已完成', dataIndex: 'completedOrders', width: 90, align: 'center', slotName: 'completedOrders' },
  { title: '已取消', dataIndex: 'cancelledOrders', width: 90, align: 'center', slotName: 'cancelledOrders' },
  { title: '平均订单金额', dataIndex: 'avgOrderValue', width: 120, align: 'right', slotName: 'avgOrderValue' },
  { title: '注册时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '状态', dataIndex: 'status', width: 90, align: 'center', slotName: 'status' },
  { title: '操作', width: 160, fixed: 'right', align: 'center', slotName: 'operations' }
])

const statsCards = [
  { key: 'totalBuyers', title: '买家总数', icon: IconList, type: 'primary' },
  { key: 'todayNewUsers', title: '今日新增', icon: IconFire, type: 'success' },
  { key: 'activeUsers', title: '活跃用户（近30天）', icon: IconBarChart, type: 'warning' },
]

async function loadEnums() {
  try {
    userStatuses.value = await getUserStatuses()
  } catch (error) {
    console.error('[BuyerManage] Failed to load enums:', error)
  }
}

async function loadStats() {
  try {
    const data = await getBriefStats()
    const inner = data?.statistics || data
    const buyerRes = await getBuyers({ pageNo: 1, pageSize: 1 })
    stats.value = {
      totalBuyers: buyerRes?.totalCount ?? 0,
      todayNewUsers: inner?.todayNewUsers ?? 0,
      activeUsers: inner?.activeUsers ?? 0,
    }
  } catch (e) {
    console.error('[BuyerManage] loadStats error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getBuyers({ keyword: keyword.value || undefined, pageNo: pagination.current, pageSize: pagination.pageSize })
    tableData.value = res?.buyers || res?.rows || []
    pagination.total = res?.totalCount ?? res?.total ?? 0
  } catch (e) {
    console.error('[BuyerManage] loadData error:', e)
    Message.error('加载买家列表失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(record) {
  try {
    const res = await getBuyerDetail(record.id)
    currentBuyer.value = res?.user || res || record
    if (!currentBuyer.value.totalOrders) {
      Object.assign(currentBuyer.value, {
        totalOrders: record.totalOrders,
        completedOrders: record.completedOrders,
        cancelledOrders: record.cancelledOrders,
        totalSpent: record.totalSpent,
        avgOrderValue: record.avgOrderValue,
      })
    }
    detailVisible.value = true
  } catch (e) {
    console.error('[BuyerManage] viewDetail error:', e)
    currentBuyer.value = record
    detailVisible.value = true
  }
}

function toggleStatus(record) {
  const isDisabled = record.status === 'DISABLED'
  const action = isDisabled ? '启用' : '禁用'
  const newStatus = isDisabled ? 'ACTIVE' : 'DISABLED'
  Modal.confirm({
    title: `${action}买家`,
    content: isDisabled
      ? `确定要启用买家「${record.username}」吗？`
      : `确定要禁用买家「${record.username}」吗？`,
    onOk: async () => {
      try {
        await updateUserStatus(record.id, newStatus)
        Message.success(`已${action}该买家`)
        loadData()
      } catch (e) {
        Message.error('操作失败')
      }
    }
  })
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }

function toNum(val) {
  const n = Number(val)
  return isNaN(n) ? 0 : n
}

function formatPrice(price) {
  const n = Number(price)
  if (isNaN(n)) return '0.00'
  return n.toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await loadEnums()
  await loadStats()
  loadData()
})
</script>

<style lang="scss" scoped>
.buyer-page { background: #fff; min-height: 100%; padding: 16px 20px; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  border-radius: 8px;
  transition: transform .2s, box-shadow .2s;

  &:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,.08); }

  &--primary { background: linear-gradient(135deg,#e6f1ff 0%,#fff 100%); border-left: 4px solid #165dff; }
  &--success { background: linear-gradient(135deg,#e8ffef 0%,#fff 100%); border-left: 4px solid #00b42a; }
  &--warning { background: linear-gradient(135deg,#fff7e6 0%,#fff 100%); border-left: 4px solid #ff7d00; }

  :deep(.arco-card-body) { padding: 18px 20px; }
  :deep(.arco-statistic-title) { font-size: 13px; color: #86909c; margin-bottom: 8px; }
  :deep(.arco-statistic-value) { font-size: 26px; font-weight: 700; }
  .stat-icon { font-size: 22px; margin-right: 10px; }
}
</style>
