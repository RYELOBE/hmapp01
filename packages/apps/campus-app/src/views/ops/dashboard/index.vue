<template>
  <div class="ops-dashboard">
    <PageContainer title="运营工作台" subtitle="数据概览与快捷操作">
      <a-spin :loading="loading" style="width: 100%">
        <a-row :gutter="[16, 16]" class="stats-row">
          <a-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
            <StatsCard
              :icon-component="card.iconComponent"
              :value="stats[card.key] ?? 0"
              :label="card.label"
              :color="card.color"
              :trend="card.trendKey ? { value: stats[card.trendKey], isUp: stats[card.trendKey] > 0 } : null"
            />
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="stats-row">
          <a-col :xs="12" :sm="6" v-for="card in secondaryStatCards" :key="card.key">
            <StatsCard
              :icon-component="card.iconComponent"
              :value="stats[card.key] ?? 0"
              :label="card.label"
              :color="card.color"
            />
          </a-col>
        </a-row>

        <a-card title="快捷操作" :bordered="false" class="quick-actions-card">
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

        <a-row :gutter="16" class="charts-section">
          <a-col :xs="24" :lg="14">
            <a-card title="近7天订单趋势" :bordered="false" class="chart-card">
              <div class="chart-placeholder">
                <icon-apps />
                <span>订单趋势图表（预留）</span>
              </div>
            </a-card>
          </a-col>

          <a-col :xs="24" :lg="10">
            <a-card title="商品分类占比" :bordered="false" class="chart-card">
              <div class="chart-placeholder">
                <icon-list />
                <span>分类占比图表（预留）</span>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" class="todo-section">
          <a-col :xs="24" :lg="12">
            <a-card title="待审核商品" :bordered="false" class="todo-card">
              <template #extra>
                <a-link @click="$router.push('/ops/review')">查看全部 →</a-link>
              </template>
              <a-table
                :data="pendingReviews"
                :pagination="false"
                :columns="reviewColumns"
                size="small"
                :scroll="{ y: 300 }"
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
                  <a-button type="text" size="small" @click="$router.push(`/ops/reviews/${record.id}`)">
                    去审核
                  </a-button>
                </template>
              </a-table>
              <a-empty v-if="!loading && pendingReviews.length === 0" description="暂无待审核商品" />
            </a-card>
          </a-col>

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
                :scroll="{ y: 300 }"
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
    </PageContainer>
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
  IconTag,
  IconPlusCircle,
  IconClockCircle,
  IconCheckCircle,
  IconEye,
  IconExport,
  IconNotification,
  IconApps,
  IconList,
} from "@arco-design/web-vue/es/icon";
import PageContainer from "../../../components/layout/PageContainer/PageContainer.vue";
import StatsCard from "../../../components/data/StatsCard/StatsCard.vue";
import {
  getStatistics,
  getReviewQueue,
  getOpsOrders,
} from "../../../services/api";

const router = useRouter();
const loading = ref(false);
const stats = ref({});
const pendingReviews = ref([]);
const recentOrders = ref([]);
const recentActivities = ref([]);

const statCards = [
  {
    key: "totalUsers",
    label: "总用户数",
    iconComponent: IconUserGroup,
    color: "#165DFF",
    trendKey: "newUsersToday",
  },
  {
    key: "totalItems",
    label: "总商品数",
    iconComponent: IconStorage,
    color: "#00B42A",
  },
  {
    key: "totalOrders",
    label: "总订单数",
    iconComponent: IconFile,
    color: "#FF7D00",
  },
  {
    key: "todayRevenue",
    label: "今日成交额",
    iconComponent: IconTag,
    color: "#F53F3F",
  },
];

const secondaryStatCards = [
  {
    key: "newUsersToday",
    label: "今日新增用户",
    iconComponent: IconPlusCircle,
    color: "#722ED1",
  },
  {
    key: "pendingItems",
    label: "待审核商品数",
    iconComponent: IconClockCircle,
    color: "#FF7D00",
  },
  {
    key: "pendingReviewsCount",
    label: "待审核评价数",
    iconComponent: IconClockCircle,
    color: "#F53F3F",
  },
  {
    key: "pendingPosts",
    label: "待审核帖子数",
    iconComponent: IconClockCircle,
    color: "#14C9C9",
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
  { label: "进入审批工作台", iconComponent: IconCheckCircle, path: "/ops/review", color: "#165DFF", bgColor: "#E6F1FF" },
  { label: "查看最新订单", iconComponent: IconEye, path: "/ops/orders", color: "#00B42A", bgColor: "#E8FFEA" },
  { label: "批量导出数据", iconComponent: IconExport, handler: "exportData", color: "#FF7D00", bgColor: "#FFF7E8" },
  { label: "发布系统公告", iconComponent: IconNotification, handler: "publishNotice", color: "#722ED1", bgColor: "#F5E8FF" },
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
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 0;
}

.stats-row {
  margin-bottom: 16px;
}

.quick-actions-card {
  border-radius: 8px;
  margin-bottom: 16px;

  :deep(.arco-card-body) {
    padding: 16px 20px;
  }
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

.charts-section {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 8px;
  min-height: 320px;

  .chart-placeholder {
    height: 280px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: #86909c;
    font-size: 14px;

    :deep(.arco-icon) {
      font-size: 48px;
      opacity: 0.4;
    }
  }
}

.todo-section {
  margin-bottom: 16px;
}

.todo-card {
  border-radius: 8px;
  min-height: 400px;

  :deep(.arco-card-header) {
    border-bottom: 1px solid var(--color-border-2);
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 16px 20px;
  }
}

.timeline-section {
  margin-bottom: 16px;
}

.price {
  font-weight: 500;
  color: #f53f3f;
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

@media screen and (max-width: 767px) {
  .quick-actions-card {
    :deep(.arco-card-body) {
      padding: 12px 16px;
    }
  }

  .chart-card {
    min-height: 260px;

    .chart-placeholder {
      height: 220px;
    }
  }

  .todo-card {
    min-height: auto;
  }
}
</style>
