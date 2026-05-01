<template>
  <div class="buyer-manage">
    <div class="buyer-manage__header">
      <h2>需方管理</h2>
      <span class="buyer-manage__desc">管理平台所有买方用户及其购买统计</span>
    </div>

    <a-space direction="vertical" :size="16" style="width: 100%">
      <a-row :gutter="16">
        <a-col :span="6" v-for="card in statCards" :key="card.key">
          <a-card size="small" :bordered="false" class="stat-card">
            <a-statistic
              :title="card.label"
              :value="stats[card.key] ?? 0"
              :value-from="0"
              :animation="true"
            >
              <template #prefix>
                <span class="stat-icon">{{ card.icon }}</span>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false">
        <template #title>
          <a-space>
            <span>买家列表</span>
            <a-divider direction="vertical" />
            <span style="color: var(--color-text-3); font-weight: normal;">
              共 {{ pagination.total }} 个买家
            </span>
          </a-space>
        </template>
        <template #extra>
          <a-input-search
            v-model="keyword"
            placeholder="搜索买家用户名"
            style="width: 240px"
            search-button
            @search="handleSearch"
            @press-enter="handleSearch"
          />
        </template>

        <a-table
          :data="tableData"
          :loading="loading"
          :pagination="paginationConfig"
          :columns="BUYER_COLUMNS"
          row-key="id"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
          :scroll="{ x: 1200 }"
        >
          <template #username="{ record }">
            <a-space>
              <a-avatar :size="32" :style="{ backgroundColor: '#722ed1' }">
                {{ record.username?.[0]?.toUpperCase() || '需' }}
              </a-avatar>
              <div>
                <div style="font-weight: 500;">{{ record.username }}</div>
                <div style="font-size: 12px; color: var(--color-text-3);">
                  ID: {{ record.id }}
                </div>
              </div>
            </a-space>
          </template>

          <template #totalOrders="{ record }">
            <a-badge
              :text="record.totalOrders || 0"
              :color="record.totalOrders > 0 ? 'arcoblue' : 'gray'"
            />
          </template>

          <template #paidOrders="{ record }">
            <a-tag color="orange" size="small">{{ record.paidOrders || 0 }}</a-tag>
          </template>

          <template #completedOrders="{ record }">
            <a-tag color="green" size="small">{{ record.completedOrders || 0 }}</a-tag>
          </template>

          <template #cancelledOrders="{ record }">
            <span style="color: var(--color-text-3);">{{ record.cancelledOrders || 0 }}</span>
          </template>

          <template #totalSpent="{ record }">
            <span style="color: #f53f3f; font-weight: 600;">
              ¥{{ record.totalSpent || 0 }}
            </span>
          </template>

          <template #avgOrderValue="{ record }">
            <span>¥{{ record.avgOrderValue || 0 }}</span>
          </template>

          <template #createdAt="{ record }">
            {{ formatDate(record.createdAt) }}
          </template>

          <template #actions="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="viewDetail(record)">
                详情
              </a-button>
              <a-button type="text" size="small" @click="viewOrders(record)">
                订单
              </a-button>
            </a-space>
          </template>
        </a-table>
      </a-card>
    </a-space>

    <a-drawer
      v-model:visible="detailVisible"
      :title="`买家详情 - ${currentBuyer?.username || ''}`"
      :width="520"
      :footer="false"
    >
      <a-descriptions :column="2" bordered size="large">
        <a-descriptions-item label="用户ID" :span="2">
          {{ currentBuyer?.id || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ currentBuyer?.username || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ currentBuyer?.phone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="订单总数">
          {{ currentBuyer?.totalOrders || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="已完成">
          <a-tag color="green" size="small">{{ currentBuyer?.completedOrders || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="已取消">
          {{ currentBuyer?.cancelledOrders || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="总消费">
          <span style="color: #f53f3f; font-weight: 600; font-size: 16px;">
            ¥{{ currentBuyer?.totalSpent || 0 }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="平均订单金额">
          <span>¥{{ currentBuyer?.avgOrderValue || 0 }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentBuyer?.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="最后购买" :span="2">
          {{ formatDate(currentBuyer?.lastOrderAt) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider>最近订单</a-divider>

      <a-table
        v-if="buyerOrders.length > 0"
        :data="buyerOrders"
        :pagination="false"
        :columns="ORDER_COLUMNS"
        size="small"
      >
        <template #status="{ record }">
          <a-tag
            :color="getOrderStatusColor(record.status)"
            size="small"
          >
            {{ getOrderStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template #amount="{ record }">
          <span style="color: #f53f3f;">¥{{ record.amount }}</span>
        </template>
      </a-table>
      <a-empty v-else description="暂无订单" />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { getBuyers, getBuyerDetail } from "../services/api";

const BUYER_COLUMNS = [
  { title: "买家", dataIndex: "username", width: 180 },
  { title: "订单总数", dataIndex: "totalOrders", width: 100 },
  { title: "已付款", dataIndex: "paidOrders", width: 100 },
  { title: "已完成", dataIndex: "completedOrders", width: 100 },
  { title: "已取消", dataIndex: "cancelledOrders", width: 100 },
  { title: "总消费", dataIndex: "totalSpent", width: 120 },
  { title: "平均金额", dataIndex: "avgOrderValue", width: 100 },
  { title: "注册时间", dataIndex: "createdAt", width: 160 },
  { title: "操作", dataIndex: "actions", width: 120, fixed: "right" },
];

const ORDER_COLUMNS = [
  { title: "订单号", dataIndex: "id", width: 120 },
  { title: "商品", dataIndex: "itemTitle", width: 150 },
  { title: "金额", dataIndex: "amount", width: 100 },
  { title: "状态", dataIndex: "status", width: 100 },
  { title: "下单时间", dataIndex: "createdAt", width: 160 },
];

const ORDER_STATUS_MAP = {
  PENDING_PAY: { label: "待付款", color: "orange" },
  PENDING_SHIP: { label: "待发货", color: "blue" },
  SHIPPED: { label: "待收货", color: "cyan" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "gray" },
};

const statCards = [
  { key: "totalBuyers", label: "买家总数", icon: "🛒" },
  { key: "activeBuyers", label: "活跃买家", icon: "🔥" },
  { key: "totalOrders", label: "总订单数", icon: "📦" },
  { key: "totalSpent", label: "总消费额", icon: "💰" },
];

const tableData = ref([]);
const loading = ref(false);
const stats = ref({});
const keyword = ref("");
const detailVisible = ref(false);
const currentBuyer = ref(null);
const buyerOrders = ref([]);

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50, 100],
});

const paginationConfig = computed(() => ({
  ...pagination.value,
  total: pagination.value.total,
}));

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getOrderStatusColor(status) {
  return ORDER_STATUS_MAP[status]?.color || "gray";
}

function getOrderStatusLabel(status) {
  return ORDER_STATUS_MAP[status]?.label || status || "未知";
}

async function loadStats() {
  try {
    const res = await getBuyers({ stats: true });
    stats.value = res?.stats || {};
  } catch (e) {
    console.error("[BuyerManage] load stats error:", e);
  }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      keyword: keyword.value || undefined,
    };
    const res = await getBuyers(params);
    tableData.value = res?.buyers || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[BuyerManage] load error:", e);
    Message.error("加载买家列表失败");
  } finally {
    loading.value = false;
  }
}

async function viewDetail(record) {
  try {
    const res = await getBuyerDetail(record.id);
    currentBuyer.value = res?.buyer || res || record;
    buyerOrders.value = res?.orders || [];
    detailVisible.value = true;
  } catch (e) {
    console.error("[BuyerManage] view detail error:", e);
    Message.error("获取买家详情失败");
  }
}

function viewOrders(record) {
  currentBuyer.value = record;
  buyerOrders.value = record.orders || [];
  detailVisible.value = true;
}

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

function handlePageSizeChange(pageSize) {
  pagination.value.pageSize = pageSize;
  pagination.value.current = 1;
  loadData();
}

onMounted(() => {
  loadStats();
  loadData();
});
</script>

<style lang="scss" scoped>
.buyer-manage {
  &__header {
    margin-bottom: 16px;
    h2 {
      margin: 0 0 4px;
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text-1);
    }
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-3);
  }
}

.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #f7f8fa 100%);
  border-radius: 8px;

  :deep(.arco-statistic-title) {
    color: var(--color-text-3);
    font-size: 13px;
  }

  :deep(.arco-statistic-content) {
    margin-top: 4px;
  }

  .stat-icon {
    margin-right: 4px;
  }
}
</style>
