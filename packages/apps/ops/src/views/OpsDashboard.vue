<template>
  <div class="ops-dashboard">
    <div class="ops-dashboard__header">
      <h2>数据概览</h2>
      <span class="ops-dashboard__subtitle">实时监控平台运营数据</span>
    </div>

    <a-skeleton :loading="loading" :animation="true">
      <a-row :gutter="16" class="ops-dashboard__cards">
        <a-col v-for="card in statCards" :key="card.key" :span="6">
          <div class="stat-card" :style="{ '--card-color': card.color, '--card-bg': card.bgColor }">
            <div class="stat-card__icon">
              {{ card.iconEmoji }}
            </div>
            <div class="stat-card__info">
              <div class="stat-card__value">{{ stats[card.key] ?? '—' }}</div>
              <div class="stat-card__label">{{ card.label }}</div>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-skeleton>

    <a-row :gutter="16" style="margin-top: 24px">
      <a-col :span="16">
        <a-card title="快捷操作" :bordered="false">
          <template #extra>
            <a-link @click="$router.push('/ops/reviews')">查看全部</a-link>
          </template>
          <a-row :gutter="[16, 16]">
            <a-col :span="6" v-for="action in quickActions" :key="action.label">
              <div class="action-card" @click="$router.push(action.path)">
                <div class="action-card__icon">{{ action.icon }}</div>
                <div class="action-card__label">{{ action.label }}</div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <a-col :span="8">
        <a-card title="最近活动" :bordered="false">
          <template #extra>
            <a-link>查看全部</a-link>
          </template>
          <a-skeleton v-if="loading" :animation="true" />
          <a-list v-else :data="recentActivities" :bordered="false">
            <template #item="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.time"
                >
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: item.color }">
                      {{ item.icon }}
                    </a-avatar>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 24px">
      <a-col :span="12">
        <a-card title="待处理事项" :bordered="false">
          <template #extra>
            <a-tag color="orange">{{ pendingCount }} 项待处理</a-tag>
          </template>
          <a-skeleton v-if="loading" :animation="true" />
          <a-table
            v-else
            :data="pendingTasks"
            :pagination="false"
            :columns="taskColumns"
            size="small"
          >
            <template #type="{ record }">
              <a-tag :color="getTaskColor(record.type)" size="small">
                {{ getTaskLabel(record.type) }}
              </a-tag>
            </template>
            <template #action="{ record }">
              <a-button type="text" size="small" @click="handleTaskAction(record)">
                处理
              </a-button>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :span="12">
        <a-card title="平台概览" :bordered="false">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="商品总数">
              <a-statistic :value="stats.totalItems || 0" :animation="true" />
            </a-descriptions-item>
            <a-descriptions-item label="订单总数">
              <a-statistic :value="stats.totalOrders || 0" :animation="true" />
            </a-descriptions-item>
            <a-descriptions-item label="供方总数">
              <a-statistic :value="stats.totalVendors || 0" :animation="true" />
            </a-descriptions-item>
            <a-descriptions-item label="需方总数">
              <a-statistic :value="stats.totalBuyers || 0" :animation="true" />
            </a-descriptions-item>
            <a-descriptions-item label="今日新增用户">
              <a-statistic :value="stats.newUsersToday || 0" :animation="true" :value-from="0" />
            </a-descriptions-item>
            <a-descriptions-item label="今日发布商品">
              <a-statistic :value="stats.newItemsToday || 0" :animation="true" :value-from="0" />
            </a-descriptions-item>
            <a-descriptions-item label="今日新增订单">
              <a-statistic :value="stats.newOrdersToday || 0" :animation="true" :value-from="0" />
            </a-descriptions-item>
            <a-descriptions-item label="活跃用户">
              <a-statistic :value="stats.activeUsers || 0" :animation="true" />
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { opsHttp as http } from "commonprovide/http";
import { Message } from "@arco-design/web-vue";

const loading = ref(false);
const stats = ref({});

const statCards = [
  { key: "pendingReviews", label: "待审核商品", iconEmoji: "⏳", color: "#ff7d00", bgColor: "#fff7e8" },
  { key: "todayApproved", label: "今日通过", iconEmoji: "✅", color: "#00b42a", bgColor: "#e8ffea" },
  { key: "todayRejected", label: "今日驳回", iconEmoji: "❌", color: "#f53f3f", bgColor: "#ffece8" },
  { key: "totalItems", label: "商品总数", iconEmoji: "📦", color: "#336ad8", bgColor: "#e8f0ff" },
  { key: "totalOrders", label: "订单总数", iconEmoji: "🧾", color: "#0fc6c2", bgColor: "#e8faf9" },
  { key: "totalVendors", label: "供方总数", iconEmoji: "👥", color: "#d88c1f", bgColor: "#fff5e6" },
  { key: "totalBuyers", label: "需方总数", iconEmoji: "🛒", color: "#722ed1", bgColor: "#f5e8ff" },
  { key: "activeUsers", label: "活跃用户", iconEmoji: "🔥", color: "#f0a838", bgColor: "#fff9ed" },
];

const quickActions = [
  { label: "待审核商品", icon: "📋", path: "/ops/reviews" },
  { label: "订单监控", icon: "📦", path: "/ops/orders" },
  { label: "用户管理", icon: "👤", path: "/ops/user-manage" },
  { label: "供方管理", icon: "👥", path: "/ops/vendor-manage" },
];

const recentActivities = ref([
  { id: 1, title: "新用户注册", time: "5分钟前", icon: "👤", color: "#1650d2" },
  { id: 2, title: "商品审核通过", time: "10分钟前", icon: "✅", color: "#00b42a" },
  { id: 3, title: "新订单生成", time: "15分钟前", icon: "🧾", color: "#0fc6c2" },
  { id: 4, title: "商品发布申请", time: "20分钟前", icon: "📦", color: "#ff7d00" },
  { id: 5, title: "订单已完成", time: "30分钟前", icon: "🎉", color: "#722ed1" },
]);

const taskColumns = [
  { title: "类型", dataIndex: "type", width: 100 },
  { title: "描述", dataIndex: "description", width: 200 },
  { title: "时间", dataIndex: "time", width: 120 },
  { title: "操作", dataIndex: "action", width: 80 },
];

const pendingTasks = ref([
  { id: 1, type: "REVIEW", description: "待审核商品 #108", time: "5分钟前" },
  { id: 2, type: "REVIEW", description: "待审核商品 #107", time: "10分钟前" },
  { id: 3, type: "REVIEW", description: "待审核商品 #106", time: "15分钟前" },
  { id: 4, type: "ORDER", description: "待发货订单 #256", time: "20分钟前" },
  { id: 5, type: "ORDER", description: "超时未发货 #254", time: "30分钟前" },
]);

const pendingCount = computed(() => pendingTasks.value.length);

function getTaskColor(type) {
  const colors = { REVIEW: "orange", ORDER: "blue" };
  return colors[type] || "gray";
}

function getTaskLabel(type) {
  const labels = { REVIEW: "待审核", ORDER: "待处理" };
  return labels[type] || type;
}

function handleTaskAction(record) {
  if (record.type === "REVIEW") {
    window.location.href = `/ops/reviews/${record.id}`;
  } else if (record.type === "ORDER") {
    window.location.href = "/ops/orders";
  }
}

async function loadStats() {
  loading.value = true;
  try {
    const res = await http.get("/ops/statistics");
    stats.value = res?.statistics || res || {};
  } catch (e) {
    console.error("[Dashboard] load error:", e);
    Message.warning("加载统计数据失败，部分数据可能不完整");
  } finally {
    loading.value = false;
  }
}

onMounted(loadStats);
</script>

<style lang="scss" scoped>
.ops-dashboard {
  &__header {
    margin-bottom: 20px;
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text-1);
    }
  }

  &__subtitle {
    font-size: 13px;
    color: var(--color-text-3);
    margin-top: 4px;
    display: block;
  }
}

.stat-card {
  background: var(--color-bg-1);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: var(--card-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: var(--card-color);
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 28px;
    font-weight: 800;
    color: var(--color-text-1);
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    color: var(--color-text-3);
    margin-top: 2px;
  }
}

.action-card {
  background: var(--color-bg-1);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light-1);
  }

  &__icon {
    font-size: 24px;
    margin-bottom: 8px;
  }

  &__label {
    font-size: 13px;
    color: var(--color-text-2);
  }
}
</style>
