<template>
  <div class="seller-center-page">
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="user-avatar">
          <icon-store size="40" />
        </div>
        <div class="user-info">
          <h1 class="welcome-text">卖家工作台</h1>
          <p class="welcome-subtitle">管理您的商品和订单，轻松赚钱~</p>
        </div>
      </div>
    </div>

    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card stat-card--items" :bordered="false" hoverable @click="$router.push('/seller/items')">
          <a-statistic title="在售商品数" :value="stats.onSaleCount || 0" :value-from="0">
            <template #prefix><icon-apps /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card stat-card--sold" :bordered="false" hoverable>
          <a-statistic title="已售出件数" :value="stats.soldCount || 0" :value-from="0">
            <template #prefix><icon-check-circle /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card stat-card--sales" :bordered="false" hoverable @click="$router.push('/seller/stats')">
          <a-statistic title="本月销售额" :value="stats.monthSales || 0" prefix="¥" :precision="2" :value-from="0">
            <template #prefix><icon-dollar /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card stat-card--pending" :bordered="false" hoverable @click="$router.push('/orders?tab=PAID')">
          <a-statistic title="待处理订单" :value="stats.pendingOrders || 0" :value-from="0">
            <template #prefix><icon-clock-circle /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="16">
        <a-card title="快捷操作" :bordered="false" class="quick-actions-card">
          <a-row :gutter="[16, 16]">
            <a-col :span="6">
              <div class="action-item" @click="$router.push('/seller/publish')">
                <div class="action-icon action-icon--publish"><icon-plus /></div>
                <span>发布新商品</span>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="action-item" @click="$router.push('/seller/items')">
                <div class="action-icon action-icon--items"><icon-list /></div>
                <span>我的商品</span>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="action-item" @click="$router.push('/seller/stats')">
                <div class="action-icon action-icon--stats"><icon-line-chart /></div>
                <span>销售统计</span>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="action-item" @click="$router.push('/orders?tab=PAID')">
                <div class="action-icon action-icon--shipping"><icon-car /></div>
                <span>待发货订单</span>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-card title="最近订单" :bordered="false" class="recent-orders-card">
          <template #extra>
            <a-button type="text" @click="$router.push('/orders')">
              查看全部
              <template #icon><icon-right /></template>
            </a-button>
          </template>

          <a-spin :loading="ordersLoading">
            <div v-if="recentOrders.length > 0" class="recent-list">
              <div
                v-for="order in recentOrders"
                :key="order.id"
                class="recent-order-item"
                @click="$router.push(`/orders/${order.id}`)"
              >
                <div class="order-left">
                  <StatusTag :status="order.status || order.orderStatus" type="order" />
                  <span class="order-buyer">{{ order.buyerName || '买家' }}</span>
                  <span class="order-title">{{ order.itemTitle || '商品' }}</span>
                </div>
                <div class="order-right">
                  <span class="order-amount">¥{{ (order.totalAmount || order.amount || 0).toFixed(2) }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无订单记录" />
          </a-spin>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card title="待处理事项" :bordered="false" class="pending-card">
          <div v-if="pendingItems.length > 0" class="pending-list">
            <div
              v-for="(item, index) in pendingItems"
              :key="index"
              class="pending-item"
              @click="handlePendingClick(item)"
            >
              <div class="pending-icon" :class="`pending-icon--${item.type}`">
                <component :is="item.icon" />
              </div>
              <div class="pending-info">
                <span class="pending-label">{{ item.label }}</span>
                <span class="pending-count">{{ item.count }} 项</span>
              </div>
              <icon-right class="pending-arrow" />
            </div>
          </div>
          <a-empty v-else description="暂无待处理事项" :image-size="48" />
        </a-card>

        <a-card title="商品概览" :bordered="false" class="overview-card">
          <div class="overview-list">
            <div class="overview-item">
              <span class="label">全部商品</span>
              <span class="value">{{ stats.totalItems || 0 }}</span>
            </div>
            <div class="overview-item">
              <span class="label">已上架</span>
              <span class="value value--success">{{ stats.approvedItems || 0 }}</span>
            </div>
            <div class="overview-item">
              <span class="label">待审核</span>
              <span class="value value--warning">{{ stats.pendingItems || 0 }}</span>
            </div>
            <div class="overview-item">
              <span class="label">已下架</span>
              <span class="value value--info">{{ stats.offlineItems || 0 }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconStore,
  IconApps,
  IconCheckCircle,
  IconDollar,
  IconClockCircle,
  IconPlus,
  IconList,
  IconLineChart,
  IconCar,
  IconRight,
} from "@arco-design/web-vue/es/icon";
import StatusTag from "commonprovide/status-tag";
import { getMyItems, getMyOrders, getSellerOverview } from "../services/api";

const router = useRouter();

const stats = reactive({
  totalItems: 0,
  approvedItems: 0,
  pendingItems: 0,
  offlineItems: 0,
  soldCount: 0,
  monthSales: 0,
  pendingOrders: 0,
  onSaleCount: 0,
});

const recentOrders = ref([]);
const ordersLoading = ref(false);

const pendingItems = ref([]);

function formatTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadStats() {
  try {
    const [itemsRes, ordersRes] = await Promise.all([
      getMyItems({ pageSize: 100 }).catch(() => ({ items: [] })),
      getMyOrders({ pageSize: 100 }).catch(() => ({ orders: [] })),
    ]);

    const items = itemsRes.items || itemsRes.rows || [];
    const orders = ordersRes.orders || ordersRes.items || [];

    stats.totalItems = items.length;
    stats.approvedItems = items.filter((i) => i.reviewStatus === "APPROVED").length;
    stats.pendingItems = items.filter((i) => i.reviewStatus === "PENDING_REVIEW").length;
    stats.offlineItems = items.filter((i) => i.reviewStatus === "OFF_SHELF").length;
    stats.onSaleCount = stats.approvedItems;

    stats.soldCount = orders.filter((o) => o.status === "COMPLETED").length;
    stats.monthSales = orders
      .filter((o) => {
        if (!o.createdAt) return false;
        const date = new Date(o.createdAt);
        const now = new Date();
        return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
      })
      .reduce((sum, o) => sum + (o.totalAmount || o.amount || 0), 0);

    stats.pendingOrders = orders.filter((o) => o.status === "PAID").length;

    pendingItems.value = [
      {
        type: "shipping",
        label: "待发货订单",
        count: stats.pendingOrders,
        icon: "IconCar",
        path: "/orders?tab=PAID",
      },
      {
        type: "review",
        label: "待审核商品",
        count: stats.pendingItems,
        icon: "IconClockCircle",
        path: "/seller/items?status=PENDING_REVIEW",
      },
    ].filter((i) => i.count > 0);
  } catch (e) {
    console.error("加载统计数据失败", e);
  }
}

async function loadRecentOrders() {
  ordersLoading.value = true;
  try {
    const res = await getMyOrders({ pageSize: 5 });
    recentOrders.value = res.orders || res.items || res.data || [];
  } catch (e) {
    console.error("加载最近订单失败", e);
  } finally {
    ordersLoading.value = false;
  }
}

function handlePendingClick(item) {
  router.push(item.path);
}

onMounted(() => {
  loadStats();
  loadRecentOrders();
});
</script>

<style lang="scss" scoped>
.seller-center-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
}

.welcome-banner {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 50%, #b37feb 100%);
  border-radius: 16px;
  padding: 28px 36px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 20px rgba(114, 46, 209, 0.25);

  .banner-content {
    display: flex;
    align-items: center;
    gap: 18px;
  }

  .user-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    -webkit-backdrop-filter: blur(10px);
    backdrop-filter: blur(10px);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .user-info {
    .welcome-text {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
    }

    .welcome-subtitle {
      margin: 6px 0 0;
      font-size: 13px;
      opacity: 0.9;
    }
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(114, 46, 209, 0.15);
  }

  &--items { border-top: 3px solid #165dff; }
  &--sold { border-top: 3px solid #00b42a; }
  &--sales { border-top: 3px solid #ff7d00; }
  &--pending { border-top: 3px solid #f53f3f; }

  :deep(.arco-card-body) {
    padding: 20px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 6px;
    }

    .arco-statistic-value {
      font-size: 22px;
      font-weight: 600;
    }
  }
}

.quick-actions-card,
.recent-orders-card,
.pending-card,
.overview-card {
  border-radius: 12px;
  margin-bottom: 16px;

  :deep(.arco-card-head-title) {
    font-size: 15px;
    font-weight: 600;
  }
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f0f5ff;
    transform: translateY(-2px);
  }

  span {
    font-size: 13px;
    font-weight: 500;
    color: #4e5969;
  }
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;

  &--publish { background: linear-gradient(135deg, #165dff 0%, #4080ff 100%); }
  &--items { background: linear-gradient(135deg, #00b42a 0%, #36cfc9 100%); }
  &--stats { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }
  &--shipping { background: linear-gradient(135deg, #ff7d00 0%, #ffc53d 100%); }
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f0f5ff;
  }

  .order-left {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
    min-width: 0;

    .order-buyer {
      font-size: 12px;
      color: #86909c;
    }

    .order-title {
      font-size: 13px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 200px;
    }
  }

  .order-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
    flex-shrink: 0;

    .order-amount {
      font-size: 15px;
      font-weight: 600;
      color: #f53f3f;
    }

    .order-time {
      font-size: 11px;
      color: #c9cdd4;
    }
  }
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #fff7e6;
  }

  .pending-icon {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: white;

    &--shipping { background: linear-gradient(135deg, #ff7d00 0%, #ffc53d 100%); }
    &--review { background: linear-gradient(135deg, #165dff 0%, #4080ff 100%); }
  }

  .pending-info {
    flex: 1;

    .pending-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #1d2129;
    }

    .pending-count {
      font-size: 12px;
      color: #86909c;
    }
  }

  .pending-arrow {
    color: #c9cdd4;
    font-size: 14px;
  }
}

.overview-list {
  .overview-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 14px;
      color: #4e5969;
    }

    .value {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;

      &--success { color: #00b42a; }
      &--warning { color: #ff7d00; }
      &--info { color: #86909c; }
    }
  }
}
</style>
