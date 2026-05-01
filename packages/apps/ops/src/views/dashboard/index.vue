<template>
  <div class="ops-dashboard">
    <div class="ops-dashboard__header">
      <h2>数据概览</h2>
    </div>
    <a-spin :loading="loading" style="width: 100%">
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
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "commonprovide/http";

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

async function loadStats() {
  loading.value = true;
  try {
    const res = await http.get("/ops/statistics");
    stats.value = res?.statistics || res || {};
  } catch (e) {
    console.error("[Dashboard] load error:", e);
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
    h2 { margin: 0; font-size: 20px; font-weight: 700; color: var(--ops-text-1); }
  }
}

.stat-card {
  background: var(--ops-bg-white);
  border-radius: var(--ops-radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--ops-shadow-sm);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--ops-shadow-md);
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
    color: var(--ops-text-1);
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    color: var(--ops-text-3);
    margin-top: 2px;
  }
}
</style>
