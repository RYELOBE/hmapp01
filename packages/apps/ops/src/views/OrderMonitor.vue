<template>
  <div class="order-monitor-page">
    <a-row :gutter="[16, 16]" class="monitor-stats">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card stat-card--pending" :bordered="false">
          <a-skeleton :loading="statsLoading" :animation="true">
            <a-statistic
              title="待支付"
              :value="stats.pendingPayment || 0"
              :value-from="0"
              :duration="600"
              :precision="0"
            >
              <template #prefix>
                <icon-clock-circle class="stat-icon stat-icon--pending" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card stat-card--paid" :bordered="false">
          <a-skeleton :loading="statsLoading" :animation="true">
            <a-statistic
              title="已支付"
              :value="stats.paid || 0"
              :value-from="0"
              :duration="600"
              :precision="0"
            >
              <template #prefix>
                <icon-check-circle class="stat-icon stat-icon--paid" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card stat-card--shipped" :bordered="false">
          <a-skeleton :loading="statsLoading" :animation="true">
            <a-statistic
              title="已发货"
              :value="stats.shipped || 0"
              :value-from="0"
              :duration="600"
              :precision="0"
            >
              <template #prefix>
                <icon-truck class="stat-icon stat-icon--shipped" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card stat-card--refund" :bordered="false">
          <a-skeleton :loading="statsLoading" :animation="true">
            <a-statistic
              title="退款中/已退款"
              :value="stats.refunding || 0"
              :value-from="0"
              :duration="600"
              :precision="0"
            >
              <template #prefix>
                <icon-safe class="stat-icon stat-icon--refund" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="list-card">
      <template #title>
        <div class="card-title">
          <icon-list class="title-icon" />
          <span>订单列表</span>
        </div>
      </template>

      <SearchFilter
        v-model="filterParams"
        :filter-items="filterItems"
        @search="handleSearch"
        @reset="handleReset"
      />

      <a-table
        :data="rows"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="(record) => record.id"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="订单号" data-index="orderNo" :width="160">
            <template #cell="{ record }">
              <a-typography-text code>{{ record.orderNo || record.id }}</a-typography-text>
            </template>
          </a-table-column>
          <a-table-column title="商品信息" :width="280">
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
                  <a-typography-text ellipsis class="item-title">
                    {{ record.itemTitle || record.title || '商品' }}
                  </a-typography-text>
                  <a-typography-text type="secondary" class="item-price">
                    ¥{{ record.price || record.totalAmount }}
                  </a-typography-text>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="买家" data-index="buyerName" :width="120">
            <template #cell="{ record }">
              <a-typography-text>{{ record.buyerName || record.userName || '-' }}</a-typography-text>
            </template>
          </a-table-column>
          <a-table-column title="卖家" data-index="sellerName" :width="120">
            <template #cell="{ record }">
              <a-typography-text>{{ record.sellerName || '-' }}</a-typography-text>
            </template>
          </a-table-column>
          <a-table-column title="订单金额" data-index="totalAmount" :width="120" align="right">
            <template #cell="{ record }">
              <a-typography-text type="danger" strong>
                ¥{{ record.totalAmount || record.price }}
              </a-typography-text>
            </template>
          </a-table-column>
          <a-table-column title="订单状态" :width="100" align="center">
            <template #cell="{ record }">
              <StatusTag :status="record.status || record.orderStatus" type="order" />
            </template>
          </a-table-column>
          <a-table-column title="下单时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="120" align="center">
            <template #cell="{ record }">
              <a-space direction="vertical" size="mini">
                <a-button type="text" size="small" @click="viewDetail(record)">
                  <template #icon><icon-eye /></template>
                  详情
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:visible="drawerVisible"
      :width="560"
      title="订单详情"
      unmount-on-close
    >
      <a-descriptions v-if="currentOrder" :column="2" bordered size="small">
        <a-descriptions-item label="订单号" :span="2">
          <a-typography-text code>{{ currentOrder.orderNo || currentOrder.id }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="商品名称">
          {{ currentOrder.itemTitle || currentOrder.title }}
        </a-descriptions-item>
        <a-descriptions-item label="商品价格">
          ¥{{ currentOrder.price }}
        </a-descriptions-item>
        <a-descriptions-item label="买家">
          {{ currentOrder.buyerName || currentOrder.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="卖家">
          {{ currentOrder.sellerName }}
        </a-descriptions-item>
        <a-descriptions-item label="订单金额">
          <a-typography-text type="danger" strong>
            ¥{{ currentOrder.totalAmount || currentOrder.price }}
          </a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <StatusTag :status="currentOrder.status || currentOrder.orderStatus" type="order" />
        </a-descriptions-item>
        <a-descriptions-item label="下单时间" :span="2">
          {{ formatDate(currentOrder.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="收货地址" :span="2">
          {{ currentOrder.shippingAddress || currentOrder.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="买家留言" :span="2" v-if="currentOrder.remark">
          {{ currentOrder.remark }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconClockCircle, IconCheckCircle, IconTruck, IconSafe, IconEye, IconImage, IconList } from "@arco-design/web-vue/es/icon";
import StatusTag from "commonprovide/status-tag";
import SearchFilter from "commonprovide/SearchFilter";
import { getOpsOrders, getStatistics } from "../services/api";

const rows = ref([]);
const loading = ref(false);
const statsLoading = ref(false);
const stats = ref({});
const drawerVisible = ref(false);
const currentOrder = ref(null);

const filterParams = reactive({
  keyword: "",
  status: "",
});

const filterItems = [
  {
    type: "input",
    field: "keyword",
    placeholder: "搜索订单号/商品名称",
  },
  {
    type: "select",
    field: "status",
    label: "订单状态",
    placeholder: "全部状态",
    options: [
      { value: "", label: "全部状态" },
      { value: "PENDING_PAYMENT", label: "待支付" },
      { value: "PAID", label: "已支付" },
      { value: "SHIPPED", label: "已发货" },
      { value: "COMPLETED", label: "已完成" },
      { value: "CANCELLED", label: "已取消" },
      { value: "REFUNDING", label: "退款中" },
      { value: "REFUNDED", label: "已退款" },
    ],
  },
];

const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50],
});

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
  statsLoading.value = true;
  try {
    const result = await getStatistics();
    const statsData = result?.statistics || result || {};
    stats.value = {
      pendingPayment: statsData.todayPendingPaymentOrders || 0,
      paid: statsData.todayPaidOrders || 0,
      shipped: statsData.todayShippedOrders || 0,
      refunding: statsData.refundingOrders || 0
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
    const result = await getOpsOrders(params);
    const data = result || {};
    rows.value = data.rows || data.items || [];
    paginationConfig.total = data.totalCount || data.total || 0;
  } catch (error) {
    Message.error(error.message || "加载订单列表失败");
    rows.value = [];
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  paginationConfig.current = 1;
  loadData();
}

function handleReset() {
  filterParams.keyword = "";
  filterParams.status = "";
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
  padding: 0;
}

.monitor-stats {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  :deep(.arco-card-body) {
    padding: 16px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 6px;
    }

    .arco-statistic-value {
      font-size: 24px;
      font-weight: 600;
    }
  }

  &--pending {
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    border-left: 3px solid #fa8c16;
  }

  &--paid {
    background: linear-gradient(135deg, #f9f0ff 0%, #ffffff 100%);
    border-left: 3px solid #722ed1;
  }

  &--shipped {
    background: linear-gradient(135deg, #e6f7ff 0%, #ffffff 100%);
    border-left: 3px solid #1890ff;
  }

  &--refund {
    background: linear-gradient(135deg, #fff1f0 0%, #ffffff 100%);
    border-left: 3px solid #ff4d4f;
  }
}

.stat-icon {
  font-size: 20px;
  margin-right: 8px;

  &--pending { color: #fa8c16; }
  &--paid { color: #722ed1; }
  &--shipped { color: #1890ff; }
  &--refund { color: #ff4d4f; }
}

.list-card {
  :deep(.arco-card-head) {
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;

  .title-icon {
    font-size: 20px;
    color: #165dff;
  }
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

  .item-title {
    font-size: 13px;
    color: #1d2129;
    max-width: 160px;
  }

  .item-price {
    font-size: 13px;
    font-weight: 500;
  }
}
</style>
