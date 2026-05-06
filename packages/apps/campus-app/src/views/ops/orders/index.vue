<template>
  <div class="orders-page">
    <div class="page-header">
      <h2 class="page-title">订单监控</h2>
    </div>

    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6" v-for="(stat, index) in statsCards" :key="index">
        <a-card :bordered="false" class="stat-card" :class="`stat-card--${stat.type}`">
          <a-statistic
            :title="stat.title"
            :value="stats[stat.key] ?? 0"
            :value-from="0"
            :duration="600"
            :precision="stat.precision || 0"
          >
            <template #prefix>
              <component :is="stat.icon" class="stat-icon" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <FilterBar
      :filters="filterConfig"
      v-model="filterParams"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #extra-buttons>
        <a-button @click="handleExport" :loading="exporting">
          <template #icon><icon-download /></template>
          导出
        </a-button>
        <a-button @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </template>
    </FilterBar>

    <a-card :bordered="false" class="table-card">
      <template #title>
        <div class="card-title">
          <span>订单列表</span>
          <a-tag color="arcoblue" size="small" v-if="pagination.total > 0">
            共 {{ pagination.total }} 条
          </a-tag>
        </div>
      </template>

      <a-table
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :columns="ORDER_COLUMNS"
        row-key="id"
        :stripe="true"
        :scroll="{ x: 1300 }"
        @page-change="handlePageChange"
      >
        <template #orderNo="{ record }">
          <a-typography-text code copyable style="font-size: 13px;">
            {{ record.orderNo || record.id }}
          </a-typography-text>
        </template>

        <template #itemInfo="{ record }">
          <div class="order-item-cell">
            <a-image
              v-if="getFirstImage(record)"
              :src="getFirstImage(record)"
              width="48"
              height="48"
              fit="cover"
              style="border-radius: 4px;"
            />
            <div v-else class="item-placeholder">
              <icon-image />
            </div>
            <div class="item-info">
              <a-typography-text ellipsis :style="{ fontWeight: 500, fontSize: '13px' }">
                {{ record.itemTitle || record.title || '商品' }}
              </a-typography-text>
              <a-typography-text type="secondary" style="font-size: 12px;">
                ¥{{ formatPrice(record.price) }}
              </a-typography-text>
            </div>
          </div>
        </template>

        <template #buyerName="{ record }">
          {{ record.buyerName || record.userName || '-' }}
        </template>

        <template #sellerName="{ record }">
          {{ record.sellerName || '-' }}
        </template>

        <template #amount="{ record }">
          <a-typography-text type="danger" strong style="font-size: 14px;">
            ¥{{ formatPrice(record.amount || record.itemPrice) }}
          </a-typography-text>
        </template>

        <template #status="{ record }">
          <a-tag :color="ORDER_STATUS_MAP[record.status]?.color || 'gray'" size="small">
            {{ ORDER_STATUS_MAP[record.status]?.label || record.status }}
          </a-tag>
        </template>

        <template #createdAt="{ record }">
          {{ formatDate(record.createdAt) }}
        </template>

        <template #actions="{ record }">
          <a-button type="text" size="small" @click="viewDetail(record)">
            查看详情
          </a-button>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:visible="drawerVisible"
      :width="640"
      title="订单详情"
      placement="right"
      unmount-on-close
    >
      <a-descriptions v-if="currentOrder" :column="2" bordered size="medium">
        <a-descriptions-item label="订单号" :span="2">
          <a-typography-text code>{{ currentOrder.orderNo || currentOrder.id }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="商品名称" :span="2">
          {{ currentOrder.itemTitle || currentOrder.title }}
        </a-descriptions-item>
        <a-descriptions-item label="商品价格">
          ¥{{ formatPrice(currentOrder.itemPrice) }}
        </a-descriptions-item>
        <a-descriptions-item label="订单金额">
          <a-typography-text type="danger" strong>¥{{ formatPrice(currentOrder.amount) }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="买家">
          {{ currentOrder.buyerName || currentOrder.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="卖家">
          {{ currentOrder.sellerName }}
        </a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="ORDER_STATUS_MAP[currentOrder.status]?.color || 'gray'" size="small">
            {{ ORDER_STATUS_MAP[currentOrder.status]?.label || currentOrder.status }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="下单时间">
          {{ formatDate(currentOrder.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="收货地址" :span="2">
          {{ currentOrder.shippingAddress || currentOrder.address || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconSafe,
  IconCheckCircle,
  IconBarChart,
  IconClockCircle,
  IconDownload,
  IconRefresh,
  IconImage,
} from "@arco-design/web-vue/es/icon";
import FilterBar from "../../../ops-components/FilterBar.vue";
import { opsHttp as http } from "../../../services/http";
import { ORDER_STATUS_OPTIONS, ORDER_STATUS_MAP, ORDER_COLUMNS } from "./const";

const tableData = ref([]);
const loading = ref(false);
const exporting = ref(false);
const drawerVisible = ref(false);
const currentOrder = ref(null);
const stats = ref({});

const filterParams = reactive({
  keyword: "",
  status: "",
  dateRange: [],
});

const filterConfig = [
  {
    field: "keyword",
    label: "关键词",
    type: "input",
    placeholder: "搜索订单号/买家/卖家/商品名",
    span: 8,
  },
  {
    field: "status",
    label: "订单状态",
    type: "select",
    placeholder: "全部状态",
    span: 6,
    options: [
      ...ORDER_STATUS_OPTIONS,
      { value: "REFUNDING", label: "退款中" },
      { value: "REFUNDED", label: "已退款" },
    ],
  },
  {
    field: "dateRange",
    label: "时间范围",
    type: "daterange",
    span: 10,
  },
];

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
});

const statsCards = [
  { key: "totalOrders", title: "总订单数", icon: IconSafe, type: "primary" },
  { key: "todayNew", title: "今日新增", icon: IconCheckCircle, type: "success" },
  { key: "totalAmount", title: "交易总额", icon: IconBarChart, type: "warning", precision: 2 },
  { key: "refundCount", title: "退款订单数", icon: IconClockCircle, type: "danger" },
];

function getFirstImage(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      const parsed = JSON.parse(urls);
      return parsed[0] || "";
    } catch {
      return urls || "";
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
}

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

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

async function loadStats() {
  try {
    const res = await http.get("/ops/statistics");
    const data = res?.statistics || res || {};
    stats.value = {
      totalOrders: data.totalOrders || 0,
      todayNew: data.todayNewOrders || 0,
      totalAmount: data.totalAmount || 0,
      refundCount: data.refundCount || 0,
    };
  } catch (e) {
    console.error("[Orders] load stats error:", e);
  }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      status: filterParams.status || undefined,
      keyword: filterParams.keyword || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };

    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    const res = await http.get("/ops/orders", { params });
    tableData.value = res?.orders || res?.rows || [];
    pagination.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[Orders] load error:", e);
    Message.error(e.message || "加载订单列表失败");
  } finally {
    loading.value = false;
  }
}

async function handleExport() {
  exporting.value = true;
  try {
    Message.warning('导出功能开发中');
  } catch (e) {
    Message.error(e.message || '导出失败');
  } finally {
    exporting.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}

function handleReset() {
  Object.keys(filterParams).forEach((key) => {
    if (Array.isArray(filterParams[key])) {
      filterParams[key] = [];
    } else {
      filterParams[key] = "";
    }
  });
  handleSearch();
}

function handlePageChange(page) {
  pagination.current = page;
  loadData();
}

function viewDetail(record) {
  currentOrder.value = record;
  drawerVisible.value = true;
}

onMounted(() => {
  loadStats();
  loadData();
});
</script>

<style lang="scss" scoped>
.orders-page {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: #1d2129;
    }
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  }

  &--primary {
    background: linear-gradient(135deg, #e6f1ff 0%, #ffffff 100%);
    border-left: 4px solid #165dff;
  }

  &--success {
    background: linear-gradient(135deg, #e8ffef 0%, #ffffff 100%);
    border-left: 4px solid #00b42a;
  }

  &--warning {
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    border-left: 4px solid #ff7d00;
  }

  &--danger {
    background: linear-gradient(135deg, #ffece8 0%, #ffffff 100%);
    border-left: 4px solid #f53f3f;
  }

  :deep(.arco-card-body) {
    padding: 18px 20px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 8px;
    }

    .arco-statistic-value {
      font-size: 26px;
      font-weight: 700;
    }
  }

  .stat-icon {
    font-size: 22px;
    margin-right: 10px;

    .stat-card--primary & { color: #165dff; }
    .stat-card--success & { color: #00b42a; }
    .stat-card--warning & { color: #ff7d00; }
    .stat-card--danger & { color: #f53f3f; }
  }
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-head) {
    border-bottom: 1px solid #e5e6eb;
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 0;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.order-item-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  .item-placeholder {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f8fa;
    border-radius: 4px;
    color: #c9cdd4;
  }

  .item-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 180px;
  }
}
</style>
