<template>
  <div class="vendor-page">
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="12" :sm="6" v-for="card in statsCards" :key="card.key">
        <a-card :bordered="false" class="stat-card" :class="`stat-card--${card.type}`">
          <a-statistic :title="card.title" :value="stats[card.key] ?? 0" :value-from="0" :duration="600">
            <template #prefix><component :is="card.icon" class="stat-icon" /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 统一表格 -->
    <OpsUnifiedTable
      title="卖家列表"
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
          placeholder="搜索卖家用户名"
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
          <a-avatar :size="32" :style="{ backgroundColor: '#00b42a' }">
            {{ (record.username || '供')[0]?.toUpperCase() }}
          </a-avatar>
          <div>
            <div style="font-weight:500">{{ record.username }}</div>
            <div style="font-size:12px;color:#86909c">ID: {{ record.id }}</div>
          </div>
        </a-space>
      </template>

      <template #totalItems="{ record }">
        <a-badge :text="record.totalItems || 0" :color="record.totalItems > 0 ? 'green' : 'gray'" />
      </template>

      <template #activeItems="{ record }">
        <a-tag color="green" size="small">{{ record.activeItems || 0 }}</a-tag>
      </template>

      <template #soldItems="{ record }">
        <a-tag color="arcoblue" size="small">{{ record.soldItems || 0 }}</a-tag>
      </template>

      <template #pendingItems="{ record }">
        <a-tag v-if="record.pendingItems > 0" color="orangered" size="small">{{ record.pendingItems }}</a-tag>
        <span v-else style="color:#86909c">0</span>
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
      :title="`卖家详情 - ${currentVendor?.username || ''}`"
      :width="600"
      placement="right"
      unmount-on-close
    >
      <a-descriptions v-if="currentVendor" :column="2" bordered size="medium">
        <a-descriptions-item label="用户ID">{{ currentVendor.id }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ currentVendor.username }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentVendor.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="注册时间">{{ formatDate(currentVendor.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="商品总数">{{ currentVendor.totalItems || 0 }}</a-descriptions-item>
        <a-descriptions-item label="在售数">
          <a-tag color="green" size="small">{{ currentVendor.activeItems || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="已售数">
          <a-tag color="arcoblue" size="small">{{ currentVendor.soldItems || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="待审核">
          <a-tag color="orangered" size="small">{{ currentVendor.pendingItems || 0 }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider>商品列表</a-divider>

      <a-table
        :data="vendorItems"
        :loading="itemsLoading"
        :pagination="itemsPagination"
        row-key="id"
        size="small"
        @page-change="handleItemsPageChange"
      >
        <template #columns>
          <a-table-column title="商品名" data-index="title" :ellipsis="true" :tooltip="true" />
          <a-table-column title="价格" data-index="price" :width="90">
            <template #cell="{ record }">
              <span style="color:#f53f3f">¥{{ formatPrice(record.price) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="reviewStatus" :width="90">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.reviewStatus, reviewStatuses)" size="small">
                {{ getStatusLabel(record.reviewStatus, reviewStatuses) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="发布时间" data-index="createdAt" :width="130">
            <template #cell="{ record }">{{ formatDate(record.createdAt) }}</template>
          </a-table-column>
        </template>
      </a-table>

      <template #footer>
        <a-button @click="detailVisible = false">关闭</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconUserGroup, IconFire, IconApps, IconClockCircle } from '@arco-design/web-vue/es/icon'
import OpsUnifiedTable from '../../components/ops/OpsUnifiedTable.vue'
import { getVendors, getVendorItems, updateUserStatus, getBriefStats } from '../../services/ops/index'
import { getUserStatuses, getReviewStatuses, getStatusLabel, getStatusColor } from '../../services/enums'

const loading = ref(false)
const keyword = ref('')
const tableData = ref([])
const stats = ref({})
const pagination = reactive({ current: 1, pageSize: 15, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })

// 枚举数据
const userStatuses = ref([])
const reviewStatuses = ref([])

// 详情
const detailVisible = ref(false)
const currentVendor = ref(null)
const vendorItems = ref([])
const itemsLoading = ref(false)
const itemsPagination = reactive({ current: 1, pageSize: 5, total: 0, showTotal: true })

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '卖家', dataIndex: 'username', width: 180, fixed: 'left', slotName: 'username' },
  { title: '商品总数', dataIndex: 'totalItems', width: 100, align: 'center', slotName: 'totalItems' },
  { title: '在售数', dataIndex: 'activeItems', width: 90, align: 'center', slotName: 'activeItems' },
  { title: '已售数', dataIndex: 'soldItems', width: 90, align: 'center', slotName: 'soldItems' },
  { title: '待审核', dataIndex: 'pendingItems', width: 90, align: 'center', slotName: 'pendingItems' },
  { title: '注册时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '状态', dataIndex: 'status', width: 90, align: 'center', slotName: 'status' },
  { title: '操作', width: 160, fixed: 'right', align: 'center', slotName: 'operations' }
])

const statsCards = [
  { key: 'totalVendors', title: '卖家总数', icon: IconUserGroup, type: 'primary' },
  { key: 'todayNewUsers', title: '今日新增', icon: IconFire, type: 'success' },
  { key: 'totalItems', title: '在售商品数', icon: IconApps, type: 'warning' },
  { key: 'pendingReviews', title: '待审核商品', icon: IconClockCircle, type: 'danger' },
]

async function loadEnums() {
  try {
    const [statuses, reviewS] = await Promise.all([
      getUserStatuses(),
      getReviewStatuses(),
    ])
    userStatuses.value = statuses || []
    reviewStatuses.value = reviewS || []
  } catch (e) {
    console.error('[VendorManage] loadEnums error:', e)
  }
}

async function loadStats() {
  try {
    const data = await getBriefStats()
    const inner = data?.statistics || data
    const vendorRes = await getVendors({ pageNo: 1, pageSize: 1 })
    stats.value = {
      totalVendors: vendorRes?.totalCount ?? 0,
      todayNewUsers: inner?.todayNewUsers ?? 0,
      totalItems: inner?.totalItems ?? 0,
      pendingReviews: inner?.pendingReviews ?? 0,
    }
  } catch (e) {
    console.error('[VendorManage] loadStats error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getVendors({ keyword: keyword.value || undefined, pageNo: pagination.current, pageSize: pagination.pageSize })
    tableData.value = res?.vendors || res?.rows || []
    pagination.total = res?.totalCount ?? res?.total ?? 0
  } catch (e) {
    console.error('[VendorManage] loadData error:', e)
    Message.error('加载卖家列表失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(record) {
  currentVendor.value = record
  detailVisible.value = true
  itemsPagination.current = 1
  await loadVendorItems(record.id)
}

async function loadVendorItems(vendorId) {
  itemsLoading.value = true
  try {
    const res = await getVendorItems(vendorId, { pageNo: itemsPagination.current, pageSize: itemsPagination.pageSize })
    vendorItems.value = res?.items || res?.rows || []
    itemsPagination.total = res?.totalCount ?? res?.total ?? 0
  } catch (e) {
    console.error('[VendorManage] loadVendorItems error:', e)
    vendorItems.value = []
  } finally {
    itemsLoading.value = false
  }
}

async function handleItemsPageChange(page) {
  itemsPagination.current = page
  await loadVendorItems(currentVendor.value.id)
}

function toggleStatus(record) {
  const isDisabled = record.status === 'DISABLED'
  const action = isDisabled ? '启用' : '禁用'
  const newStatus = isDisabled ? 'ACTIVE' : 'DISABLED'
  Modal.confirm({
    title: `${action}卖家`,
    content: isDisabled
      ? `确定要启用卖家「${record.username}」吗？`
      : `确定要禁用卖家「${record.username}」吗？禁用后其所有商品将下架。`,
    onOk: async () => {
      try {
        await updateUserStatus(record.id, newStatus)
        Message.success(`已${action}该卖家`)
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

function formatPrice(price) {
  if (!price && price !== 0) return '0.00'
  return Number(price).toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => { await loadEnums(); await loadStats(); loadData() })
</script>

<style lang="scss" scoped>
.vendor-page { background: #fff; min-height: 100%; padding: 16px 20px; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  border-radius: 8px;
  transition: transform .2s, box-shadow .2s;

  &:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,.08); }

  &--primary { background: linear-gradient(135deg,#e6f1ff 0%,#fff 100%); border-left: 4px solid #165dff; }
  &--success { background: linear-gradient(135deg,#e8ffef 0%,#fff 100%); border-left: 4px solid #00b42a; }
  &--warning { background: linear-gradient(135deg,#fff7e6 0%,#fff 100%); border-left: 4px solid #ff7d00; }
  &--danger  { background: linear-gradient(135deg,#ffece8 0%,#fff 100%); border-left: 4px solid #f53f3f; }

  :deep(.arco-card-body) { padding: 18px 20px; }
  :deep(.arco-statistic-title) { font-size: 13px; color: #86909c; margin-bottom: 8px; }
  :deep(.arco-statistic-value) { font-size: 26px; font-weight: 700; }
  .stat-icon { font-size: 22px; margin-right: 10px; }
}
</style>
