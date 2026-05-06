<template>
  <div class="ops-dashboard">
    <div class="ops-dashboard__header">
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item>运营中心</a-breadcrumb-item>
        <a-breadcrumb-item>工作台</a-breadcrumb-item>
      </a-breadcrumb>
      <h2 class="page-title">运营工作台</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <!-- 数据统计卡片区 -->
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
          <a-card class="stat-card" :bordered="false" hoverable>
            <div class="stat-card__body">
              <div class="stat-card__icon" :style="{ background: card.bgColor, color: card.color }">
                <component :is="card.iconComponent" />
              </div>
              <div class="stat-card__info">
                <div class="stat-card__label">{{ card.label }}</div>
                <div class="stat-card__value">{{ formatNumber(stats[card.key]) }}</div>
                <div class="stat-card__trend" v-if="card.trendKey && stats[card.trendKey] !== undefined">
                  <icon-arrow-rise v-if="stats[card.trendKey] > 0" class="trend-icon up" />
                  <icon-arrow-fall v-else-if="stats[card.trendKey] < 0" class="trend-icon down" />
                  <span :class="{ 'trend-up': stats[card.trendKey] > 0, 'trend-down': stats[card.trendKey] < 0 }">
                    {{ stats[card.trendKey] > 0 ? '+' : '' }}{{ stats[card.trendKey] }}
                  </span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 待办事项区 -->
      <a-row :gutter="16" class="todo-section">
        <!-- 左列：待审核商品 -->
        <a-col :xs="24" :lg="12">
          <a-card title="待审核商品" :bordered="false" class="todo-card">
            <template #extra>
              <a-link @click="$router.push('/ops/reviews')">查看全部 →</a-link>
            </template>
            <a-table
              :data="pendingReviews"
              :pagination="false"
              :columns="reviewColumns"
              size="small"
              :scroll="{ y: 400 }"
              row-key="id"
            >
              <template #thumbnail="{ record }">
                <a-avatar :size="40" shape="square">
                  <img v-if="record.thumbnail" :src="record.thumbnail" alt="" />
                  <icon-storage v-else />
                </a-avatar>
              </template>
              <template #price="{ record }">
                <span class="price">¥{{ record.price?.toFixed(2) || '—' }}</span>
              </template>
              <template #action="{ record }">
                <a-button type="text" size="small" @click="goToReview(record.id)">
                  去审核
                </a-button>
              </template>
            </a-table>
            <a-empty v-if="!loading && pendingReviews.length === 0" description="暂无待审核商品" />
          </a-card>
        </a-col>

        <!-- 右列：最新订单 -->
        <a-col :xs="24" :lg="12">
          <a-card title="最新订单" :bordered="false" class="todo-card">
            <template #extra>
              <a-link @click="$router.push('/ops/orders')">查看全部 →</a-link>
            </template>
            <a-table
              :data="recentOrders"
              :pagination="false"
              :columns="orderColumns"
              size="small"
              :scroll="{ y: 400 }"
              row-key="id"
            >
              <template #amount="{ record }">
                <span class="price">¥{{ record.amount?.toFixed(2) || '—' }}</span>
              </template>
              <template #status="{ record }">
                <a-tag :color="getOrderStatusColor(record.status)" size="small">
                  {{ getOrderStatusLabel(record.status) }}
                </a-tag>
              </template>
            </a-table>
            <a-empty v-if="!loading && recentOrders.length === 0" description="暂无订单数据" />
          </a-card>
        </a-col>
      </a-row>

      <!-- 快捷操作区 -->
      <a-row :gutter="16" class="quick-actions-section">
        <a-col :span="24">
          <a-card title="快捷操作" :bordered="false">
            <a-row :gutter="[16, 16]">
              <a-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.label">
                <div class="action-item" @click="handleQuickAction(action)">
                  <div class="action-icon" :style="{ background: action.bgColor, color: action.color }">
                    <component :is="action.iconComponent" />
                  </div>
                  <div class="action-label">{{ action.label }}</div>
                </div>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <!-- 最近活动时间线 -->
      <a-row :gutter="16" class="timeline-section">
        <a-col :span="24">
          <a-card title="最近活动" :bordered="false">
            <a-timeline v-if="recentActivities.length > 0">
              <a-timeline-item
                v-for="activity in recentActivities"
                :key="activity.id"
                :dot-color="activity.dotColor"
              >
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无活动记录" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconUserGroup,
  IconStorage,
  IconFile,
  IconCurrency,
  IconCheckCircle,
  IconEye,
  IconExport,
  IconNotification,
  IconArrowRise,
  IconArrowFall,
} from "@arco-design/web-vue/es/icon";
import {
  getStatistics,
  getReviewQueue,
  getOpsOrders,
} from "../../services/api";

const router = useRouter();
const loading = ref(false);
const stats = ref({});
const pendingReviews = ref([]);
const recentOrders = ref([]);
const recentActivities = ref([]);

const statCards = [
  {
    key: "totalUsers",
    label: "用户总数",
    iconComponent: IconUserGroup,
    color: "#165DFF",
    bgColor: "#E6F1FF",
    trendKey: "newUsersToday",
  },
  {
    key: "totalItems",
    label: "商品总数",
    iconComponent: IconStorage,
    color: "#00B42A",
    bgColor: "#E8FFEA",
    trendKey: "pendingReviews",
  },
  {
    key: "totalOrders",
    label: "订单总数",
    iconComponent: IconFile,
    color: "#FF7D00",
    bgColor: "#FFF7E8",
    trendKey: "newOrdersToday",
  },
  {
    key: "totalRevenue",
    label: "交易总额",
    iconComponent: IconCurrency,
    color: "#722ED1",
    bgColor: "#F5E8FF",
    trendKey: null,
  },
];

const reviewColumns = [
  { title: "缩略图", dataIndex: "thumbnail", width: 70, slotName: "thumbnail" },
  { title: "标题", dataIndex: "title", ellipsis: true, tooltip: true },
  { title: "价格", dataIndex: "price", width: 90, slotName: "price" },
  { title: "卖家", dataIndex: "seller", width: 100, ellipsis: true },
  { title: "发布时间", dataIndex: "createTime", width: 120 },
  { title: "操作", slotName: "action", width: 80, align: "center" },
];

const orderColumns = [
  { title: "订单号", dataIndex: "orderNo", ellipsis: true },
  { title: "买家", dataIndex: "buyer", width: 100, ellipsis: true },
  { title: "金额", dataIndex: "amount", width: 100, slotName: "amount" },
  { title: "状态", dataIndex: "status", width: 90, slotName: "status" },
  { title: "时间", dataIndex: "createTime", width: 140 },
];

const quickActions = [
  { label: "待审核商品", iconComponent: IconCheckCircle, path: "/ops/reviews", color: "#165DFF", bgColor: "#E6F1FF" },
  { label: "订单监控", iconComponent: IconEye, path: "/ops/orders", color: "#00B42A", bgColor: "#E8FFEA" },
  { label: "批量导出", iconComponent: IconExport, handler: "exportData", color: "#FF7D00", bgColor: "#FFF7E8" },
  { label: "系统公告", iconComponent: IconNotification, handler: "publishNotice", color: "#722ED1", bgColor: "#F5E8FF" },
];

async function loadDashboardData() {
  loading.value = true;
  try {
    const [statsRes, reviewsRes, ordersRes] = await Promise.allSettled([
      getStatistics(),
      getReviewQueue({ pageSize: 10 }),
      getOpsOrders({ pageSize: 10 }),
    ]);

    if (statsRes.status === "fulfilled") {
      stats.value = statsRes.value?.statistics || statsRes.value || {};
    }

    if (reviewsRes.status === "fulfilled") {
      pendingReviews.value = reviewsRes.value?.list || reviewsRes.value?.items || reviewsRes.value || [];
    }

    if (ordersRes.status === "fulfilled") {
      recentOrders.value = ordersRes.value?.list || ordersRes.value?.items || ordersRes.value || [];
    }

    generateMockActivities();
  } catch (e) {
    console.error("[Dashboard] load error:", e);
    Message.error("加载数据失败，请刷新重试");
  } finally {
    loading.value = false;
  }
}

function generateMockActivities() {
  const now = new Date();
  recentActivities.value = [
    { id: 1, title: "新用户注册：张三", time: formatTimeAgo(new Date(now - 5 * 60000)), dotColor: "#165DFF" },
    { id: 2, title: "商品审核通过：智能手表 Pro", time: formatTimeAgo(new Date(now - 10 * 60000)), dotColor: "#00B42A" },
    { id: 3, title: "新订单生成：ORD20260502001", time: formatTimeAgo(new Date(now - 15 * 60000)), dotColor: "#FF7D00" },
    { id: 4, title: "异常订单告警：支付超时", time: formatTimeAgo(new Date(now - 30 * 60000)), dotColor: "#F53F3F" },
    { id: 5, title: "新商品发布申请：无线耳机 X3", time: formatTimeAgo(new Date(now - 45 * 60000)), dotColor: "#722ED1" },
  ];
}

function formatNumber(num) {
  if (num === undefined || num === null) return "—";
  if (typeof num === "number") {
    return num.toLocaleString("zh-CN");
  }
  return num;
}

function formatTimeAgo(date) {
  const diff = Date.now() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  return `${days}天前`;
}

function getOrderStatusColor(status) {
  const colors = {
    PENDING: "orange",
    PAID: "blue",
    SHIPPED: "cyan",
    COMPLETED: "green",
    CANCELLED: "red",
    REFUNDED: "gray",
  };
  return colors[status] || "gray";
}

function getOrderStatusLabel(status) {
  const labels = {
    PENDING: "待付款",
    PAID: "已付款",
    SHIPPED: "已发货",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
    REFUNDED: "已退款",
  };
  return labels[status] || status;
}

function goToReview(id) {
  router.push(`/ops/reviews/${id}`);
}

function handleQuickAction(action) {
  if (action.path) {
    router.push(action.path);
  } else if (action.handler === "exportData") {
    Message.info("批量导出功能开发中");
  } else if (action.handler === "publishNotice") {
    Message.info("系统公告发布功能开发中");
  }
}

onMounted(loadDashboardData);
</script>

<style lang="scss" scoped>
.ops-dashboard {
  &__header {
    margin-bottom: 20px;

    .breadcrumb {
      margin-bottom: 8px;
      font-size: 13px;
    }

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text-1);
    }
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .todo-section {
    margin-bottom: 20px;
  }

  .quick-actions-section {
    margin-bottom: 20px;
  }

  .timeline-section {
    margin-bottom: 20px;
  }
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    :deep(.arco-card-body) {
      background: transparent;
    }
  }

  &__body {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__label {
    font-size: 13px;
    color: var(--color-text-3);
    margin-bottom: 4px;
  }

  &__value {
    font-size: 28px;
    font-weight: 800;
    color: var(--color-text-1);
    line-height: 1.2;
  }

  &__trend {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 4px;
    font-size: 12px;

    .trend-icon {
      font-size: 14px;

      &.up {
        color: #00b42a;
      }

      &.down {
        color: #f53f3f;
      }
    }

    .trend-up {
      color: #00b42a;
    }

    .trend-down {
      color: #f53f3f;
    }
  }
}

.todo-card {
  border-radius: 8px;
  min-height: 480px;

  :deep(.arco-card-header) {
    border-bottom: 1px solid var(--color-border-2);
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 16px 20px;
  }
}

.price {
  font-weight: 500;
  color: #f53f3f;
}

.action-item {
  text-align: center;
  cursor: pointer;
  padding: 16px 12px;
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light-1);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
  }

  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin-bottom: 10px;
  }

  .action-label {
    font-size: 13px;
    color: var(--color-text-2);
    font-weight: 500;
  }
}

.activity-content {
  .activity-title {
    font-size: 14px;
    color: var(--color-text-1);
    font-weight: 500;
    margin-bottom: 4px;
  }

  .activity-time {
    font-size: 12px;
    color: var(--color-text-3);
  }
}

@media screen and (max-width: 768px) {
  .stat-card {
    &__value {
      font-size: 24px;
    }
  }

  .todo-card {
    min-height: auto;
  }
}
</style>
