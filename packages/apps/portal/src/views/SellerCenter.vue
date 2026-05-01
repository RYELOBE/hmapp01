<template>
  <div class="seller-center-page">
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :bordered="false">
          <a-skeleton :loading="loading" :animation="true">
            <a-statistic
              title="我的商品"
              :value="stats.totalItems || 0"
              :value-from="0"
              :duration="600"
            >
              <template #prefix>
                <icon-app class="stat-icon stat-icon--items" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :bordered="false">
          <a-skeleton :loading="loading" :animation="true">
            <a-statistic
              title="已上架"
              :value="stats.approvedItems || 0"
              :value-from="0"
              :duration="600"
            >
              <template #prefix>
                <icon-check-circle class="stat-icon stat-icon--approved" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :bordered="false">
          <a-skeleton :loading="loading" :animation="true">
            <a-statistic
              title="待审核"
              :value="stats.pendingItems || 0"
              :value-from="0"
              :duration="600"
            >
              <template #prefix>
                <icon-clock-circle class="stat-icon stat-icon--pending" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :bordered="false">
          <a-skeleton :loading="loading" :animation="true">
            <a-statistic
              title="我的订单"
              :value="stats.totalOrders || 0"
              :value-from="0"
              :duration="600"
            >
              <template #prefix>
                <icon-shopping-cart class="stat-icon stat-icon--orders" />
              </template>
            </a-statistic>
          </a-skeleton>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="content-card">
      <template #title>
        <a-space>
          <icon-store />
          <span>卖家中心</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="$router.push('/publish')">
            <template #icon><icon-plus /></template>
            发布商品
          </a-button>
        </a-space>
      </template>

      <a-tabs default-active-key="items" @change="handleTabChange">
        <a-tab-pane key="items" title="我的商品">
          <a-table
            :data="items"
            :loading="loading"
            :pagination="paginationConfig"
            :row-key="(record) => record.id"
            @page-change="handlePageChange"
            @page-size-change="handlePageSizeChange"
          >
            <template #columns>
              <a-table-column title="商品信息" :width="300">
                <template #cell="{ record }">
                  <div class="item-cell">
                    <a-image
                      :src="getImageUrl(record)"
                      width="60"
                      height="60"
                      fit="cover"
                      style="border-radius: 6px;"
                    />
                    <div class="item-info">
                      <a-typography-text class="item-title" ellipsis>
                        {{ record.title }}
                      </a-typography-text>
                      <a-typography-text type="danger" class="item-price">
                        ¥{{ record.price }}
                      </a-typography-text>
                    </div>
                  </div>
                </template>
              </a-table-column>
              <a-table-column title="分类" data-index="category" :width="100" />
              <a-table-column title="审核状态" :width="100" align="center">
                <template #cell="{ record }">
                  <StatusTag :status="record.reviewStatus" />
                </template>
              </a-table-column>
              <a-table-column title="发布时间" data-index="createdAt" :width="160">
                <template #cell="{ record }">
                  {{ formatDate(record.createdAt) }}
                </template>
              </a-table-column>
              <a-table-column title="操作" :width="160" align="center">
                <template #cell="{ record }">
                  <a-space>
                    <a-button type="text" size="small" @click="viewDetail(record)">
                      <template #icon><icon-eye /></template>
                      查看
                    </a-button>
                    <a-button type="text" size="small" @click="editItem(record)">
                      <template #icon><icon-edit /></template>
                      编辑
                    </a-button>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="orders" title="我的订单">
          <a-table
            :data="orders"
            :loading="ordersLoading"
            :pagination="ordersPaginationConfig"
            :row-key="(record) => record.id"
            @page-change="handleOrderPageChange"
            @page-size-change="handleOrderPageSizeChange"
          >
            <template #columns>
              <a-table-column title="订单号" data-index="orderNo" :width="160">
                <template #cell="{ record }">
                  <a-typography-text code>{{ record.orderNo || record.id }}</a-typography-text>
                </template>
              </a-table-column>
              <a-table-column title="商品" :width="240">
                <template #cell="{ record }">
                  <div class="order-item">
                    <span>{{ record.itemTitle || record.title }}</span>
                    <span class="order-price">¥{{ record.price }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column title="买家" data-index="buyerName" :width="120" />
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
                  <a-space>
                    <a-button
                      v-if="record.status === 'PAID'"
                      type="text"
                      size="small"
                      status="success"
                      @click="shipOrder(record)"
                    >
                      <template #icon><icon-send /></template>
                      发货
                    </a-button>
                    <a-button type="text" size="small" @click="viewOrder(record)">
                      <template #icon><icon-eye /></template>
                      详情
                    </a-button>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-drawer
      v-model:visible="drawerVisible"
      :width="500"
      title="订单详情"
      unmount-on-close
    >
      <a-descriptions v-if="currentOrder" :column="2" bordered size="small">
        <a-descriptions-item label="订单号" :span="2">
          {{ currentOrder.orderNo || currentOrder.id }}
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
        <a-descriptions-item label="订单状态">
          <StatusTag :status="currentOrder.status || currentOrder.orderStatus" type="order" />
        </a-descriptions-item>
        <a-descriptions-item label="收货地址" :span="2">
          {{ currentOrder.shippingAddress || currentOrder.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="下单时间" :span="2">
          {{ formatDate(currentOrder.createdAt) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconStore,
  IconPlus,
  IconApp,
  IconCheckCircle,
  IconClockCircle,
  IconShoppingCart,
  IconEye,
  IconEdit,
  IconSend,
} from "@arco-design/web-vue/es/icon";
import StatusTag from "commonprovide/status-tag";
import { getMyItems, getMyOrders, shipOrder as apiShipOrder } from "../services/api";

const router = useRouter();

const loading = ref(false);
const ordersLoading = ref(false);
const stats = ref({});
const items = ref([]);
const orders = ref([]);
const drawerVisible = ref(false);
const currentOrder = ref(null);

const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
});

const ordersPaginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
});

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      return JSON.parse(urls)[0] || "";
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
  stats.value = {
    totalItems: items.value.length,
    approvedItems: items.value.filter(i => i.reviewStatus === 'APPROVED').length,
    pendingItems: items.value.filter(i => i.reviewStatus === 'PENDING_REVIEW').length,
    totalOrders: orders.value.length,
  };
}

async function loadItems() {
  loading.value = true;
  try {
    const result = await getMyItems({
      pageNo: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
    });
    items.value = result.items || result.rows || [];
    paginationConfig.total = result.total || result.totalCount || 0;
    loadStats();
  } catch (error) {
    Message.error(error.message || "加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadOrders() {
  ordersLoading.value = true;
  try {
    const result = await getMyOrders({
      pageNo: ordersPaginationConfig.current,
      pageSize: ordersPaginationConfig.pageSize,
    });
    orders.value = result.items || result.rows || result.data || [];
    ordersPaginationConfig.total = result.total || result.totalCount || 0;
    loadStats();
  } catch (error) {
    Message.error(error.message || "加载订单失败");
  } finally {
    ordersLoading.value = false;
  }
}

function handleTabChange(key) {
  if (key === "orders" && orders.value.length === 0) {
    loadOrders();
  }
}

function handlePageChange(page) {
  paginationConfig.current = page;
  loadItems();
}

function handlePageSizeChange(size) {
  paginationConfig.pageSize = size;
  paginationConfig.current = 1;
  loadItems();
}

function handleOrderPageChange(page) {
  ordersPaginationConfig.current = page;
  loadOrders();
}

function handleOrderPageSizeChange(size) {
  ordersPaginationConfig.pageSize = size;
  ordersPaginationConfig.current = 1;
  loadOrders();
}

function viewDetail(record) {
  router.push(`/item/${record.id}`);
}

function editItem(record) {
  router.push(`/publish?id=${record.id}`);
}

function viewOrder(record) {
  currentOrder.value = record;
  drawerVisible.value = true;
}

async function shipOrder(record) {
  try {
    await apiShipOrder(record.id, { trackingCompany: '未填写', trackingNumber: '' });
    Message.success("发货成功");
    loadOrders();
  } catch (error) {
    Message.error(error.message || "发货失败");
  }
}

onMounted(loadItems);
</script>

<style lang="scss" scoped>
.seller-center-page {
  padding: 0;
}

.stats-row {
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
}

.stat-icon {
  font-size: 20px;
  margin-right: 8px;

  &--items { color: #165dff; }
  &--approved { color: #52c41a; }
  &--pending { color: #fa8c16; }
  &--orders { color: #722ed1; }
}

.content-card {
  border-radius: 8px;

  :deep(.arco-card-head) {
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.arco-tabs-nav) {
    padding: 0 16px;
  }
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;

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
    font-size: 14px;
    font-weight: 600;
  }
}

.order-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .order-price {
    font-size: 13px;
    color: #ff4d4f;
    font-weight: 500;
  }
}
</style>
