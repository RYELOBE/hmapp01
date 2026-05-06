<template>
  <div class="order-monitor-page">
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
          <a-tag color="arcoblue" size="small" v-if="paginationConfig.total > 0">
            共 {{ paginationConfig.total }} 条
          </a-tag>
        </div>
      </template>

      <a-table
        :data="rows"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="(record) => record.id"
        :stripe="true"
        :scroll="{ x: 1300 }"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="订单号" data-index="orderNo" :width="170" fixed="left">
            <template #cell="{ record }">
              <a-typography-text code copyable style="font-size: 13px;">
                {{ record.orderNo || record.id }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="商品信息" :width="260">
            <template #cell="{ record }">
              <div class="order-item-cell">
                <a-image
                  v-if="getImageUrl(record)"
                  :src="getImageUrl(record)"
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
          </a-table-column>

          <a-table-column title="买家" data-index="buyerName" :width="110">
            <template #cell="{ record }">
              <a-typography-text>{{ record.buyerName || record.userName || '-' }}</a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="卖家" data-index="sellerName" :width="110">
            <template #cell="{ record }">
              <a-typography-text>{{ record.sellerName || '-' }}</a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="金额" data-index="totalAmount" :width="100" align="right">
            <template #cell="{ record }">
              <a-typography-text type="danger" strong style="font-size: 14px;">
                ¥{{ formatPrice(record.totalAmount || record.price) }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="状态" data-index="status" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag :color="getOrderStatusColor(record.status)" size="small">
                {{ getOrderStatusLabel(record.status) }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="下单时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="90" fixed="right" align="center">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="viewDetail(record)">
                查看详情
              </a-button>
            </template>
          </a-table-column>
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
      <a-spin :loading="detailLoading">
        <a-descriptions v-if="currentOrder" :column="2" bordered size="medium">
          <a-descriptions-item label="订单号" :span="2">
            <a-typography-text code>{{ currentOrder.orderNo || currentOrder.id }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="商品名称" :span="2">
            {{ currentOrder.itemTitle || currentOrder.title }}
          </a-descriptions-item>
          <a-descriptions-item label="商品价格">
            ¥{{ formatPrice(currentOrder.price) }}
          </a-descriptions-item>
          <a-descriptions-item label="订单金额">
            <a-typography-text type="danger" strong>¥{{ formatPrice(currentOrder.totalAmount || currentOrder.price) }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="买家">
            {{ currentOrder.buyerName || currentOrder.userName }}
          </a-descriptions-item>
          <a-descriptions-item label="卖家">
            {{ currentOrder.sellerName }}
          </a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getOrderStatusColor(currentOrder.status)" size="small">
              {{ getOrderStatusLabel(currentOrder.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="下单时间">
            {{ formatDate(currentOrder.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="收货地址" :span="2">
            {{ currentOrder.shippingAddress || currentOrder.address || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="买家留言" :span="2" v-if="currentOrder.remark">
            {{ currentOrder.remark }}
          </a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconClockCircle,
  IconCheckCircle,
  IconTruck,
  IconSafe,
  IconEye,
  IconImage,
  IconDownload,
  IconRefresh,
} from "@arco-design/web-vue/es/icon";
import FilterBar from "../components/FilterBar.vue";
import { getOpsOrders, getStatistics, exportOrders } from "../services/api";

const rows = ref([]);
const loading = ref(false);
const exporting = ref(false);
const detailLoading = ref(false);
const statsLoading = ref(false);
const stats = ref({});
const drawerVisible = ref(false);
const currentOrder = ref(null);

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
      { value: "", label: "全部状态" },
      { value: "PENDING_PAYMENT", label: "待付款" },
      { value: "PAID", label: "已付款" },
      { value: "SHIPPED", label: "已发货" },
      { value: "COMPLETED", label: "已完成" },
      { value: "CANCELLED", label: "已取消" },
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

const paginationConfig = reactive({
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
  { key: "totalAmount", title: "交易总额", icon: IconTruck, type: "warning", precision: 2 },
  { key: "refundCount", title: "退款订单数", icon: IconClockCircle, type: "danger" },
];

function getImageUrl(record) {
  const urls = record.imageUrls || record.itemImageUrl || record.images || [];
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

function getOrderStatusColor(status) {
  const colors = {
    PENDING_PAYMENT: "orangered",
    PAID: "arcoblue",
    SHIPPED: "cyan",
    COMPLETED: "green",
    CANCELLED: "gray",
    REFUNDING: "red",
    REFUNDED: "#ffccc7",
  };
  return colors[status] || "gray";
}

function getOrderStatusLabel(status) {
  const labels = {
    PENDING_PAYMENT: "待付款",
    PAID: "已付款",
    SHIPPED: "已发货",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
    REFUNDING: "退款中",
    REFUNDED: "已退款",
  };
  return labels[status] || status;
}

async function loadStats() {
  statsLoading.value = true;
  try {
    const result = await getStatistics();
    const statsData = result?.statistics || result || {};
    stats.value = {
      totalOrders: statsData.totalOrders || 0,
      todayNew: statsData.todayNewOrders || 0,
      totalAmount: statsData.totalAmount || 0,
      refundCount: statsData.refundCount || 0,
    };
  } catch (error) {
    console.error("加载订单统计失败", error);
  } finally {
    statsLoading.value = false;
  }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
      keyword: filterParams.keyword || undefined,
      status: filterParams.status || undefined,
    };

    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    const result = await getOpsOrders(params);
    rows.value = result.rows || result.items || [];
    paginationConfig.total = result.totalCount || result.total || 0;
  } catch (error) {
    Message.error(error.message || "加载订单列表失败");
    rows.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleExport() {
  exporting.value = true;
  try {
    const params = {
      keyword: filterParams.keyword || undefined,
      status: filterParams.status || undefined,
    };
    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    if (typeof exportOrders === 'function') {
      await exportOrders(params);
      Message.success('导出成功');
    } else {
      Message.warning('导出功能开发中');
    }
  } catch (error) {
    Message.error(error.message || '导出失败');
  } finally {
    exporting.value = false;
  }
}

function handleSearch() {
  paginationConfig.current = 1;
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
  paginationConfig.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  paginationConfig.pageSize = size;
  paginationConfig.current = 1;
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
.order-monitor-page {
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
