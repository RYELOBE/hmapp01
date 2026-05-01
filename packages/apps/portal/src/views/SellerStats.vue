<template>
  <div class="seller-stats-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">销售统计</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="stats" class="stats-content">
        <!-- 数据卡片 -->
        <div class="stats-cards">
          <div class="stat-card stat-card--today">
            <div class="stat-icon">
              <icon-today />
            </div>
            <div class="stat-info">
              <div class="stat-label">今日销售</div>
              <div class="stat-value">¥{{ stats.todaySales }}</div>
              <div class="stat-count">{{ stats.todayOrders }} 笔订单</div>
            </div>
          </div>

          <div class="stat-card stat-card--month">
            <div class="stat-icon">
              <icon-calendar />
            </div>
            <div class="stat-info">
              <div class="stat-label">本月销售</div>
              <div class="stat-value">¥{{ stats.monthSales }}</div>
              <div class="stat-count">{{ stats.monthOrders }} 笔订单</div>
            </div>
          </div>

          <div class="stat-card stat-card--total">
            <div class="stat-icon">
              <icon-chart />
            </div>
            <div class="stat-info">
              <div class="stat-label">累计销售</div>
              <div class="stat-value">¥{{ stats.totalSales }}</div>
              <div class="stat-count">{{ stats.totalOrders }} 笔订单</div>
            </div>
          </div>

          <div class="stat-card stat-card--items">
            <div class="stat-icon">
              <icon-box />
            </div>
            <div class="stat-info">
              <div class="stat-label">在售商品</div>
              <div class="stat-value">{{ stats.onSaleCount }}</div>
              <div class="stat-count">待审核 {{ stats.pendingCount }}</div>
            </div>
          </div>
        </div>

        <!-- 销售趋势 -->
        <div class="trend-section">
          <div class="section-header">
            <h3>销售趋势</h3>
            <a-radio-group v-model="trendDays" type="button" @change="loadTrend">
              <a-radio value="7">近7天</a-radio>
              <a-radio value="30">近30天</a-radio>
            </a-radio-group>
          </div>
          <div class="trend-chart">
            <div class="chart-bars">
              <div
                v-for="(day, index) in trendData"
                :key="index"
                class="chart-bar-wrapper"
              >
                <div
                  class="chart-bar"
                  :style="{ height: getBarHeight(day.sales) + 'px' }"
                >
                  <div class="bar-tooltip">
                    <span>{{ day.date }}</span>
                    <span>¥{{ day.sales }}</span>
                  </div>
                </div>
                <div class="chart-label">{{ day.date }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 商品排行 -->
        <div class="ranking-section">
          <div class="section-header">
            <h3>商品销量排行</h3>
          </div>
          <a-table
            :columns="rankingColumns"
            :data="rankingData"
            :pagination="false"
            :loading="rankingLoading"
            stripe
          >
            <template #itemTitle="{ record }">
              <div class="item-cell">
                <img
                  v-if="record.imageUrls"
                  :src="parseFirstImageUrl(record.imageUrls)"
                  class="item-thumb"
                />
                <div v-else class="item-thumb item-thumb--empty">📷</div>
                <span class="item-name">{{ record.title }}</span>
              </div>
            </template>
            <template #price="{ record }">
              <span class="price">¥{{ record.price }}</span>
            </template>
            <template #soldCount="{ record }">
              <a-tag color="purple">{{ record.soldCount || 0 }} 售出</a-tag>
            </template>
          </a-table>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconToday,
  IconCalendar,
  IconChart,
  IconBox,
} from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import {
  getSellerOverview,
  getSellerTrend,
  getSellerRanking,
} from "../../services/api";

const loading = ref(false);
const rankingLoading = ref(false);
const stats = ref(null);
const trendData = ref([]);
const rankingData = ref([]);
const trendDays = ref("7");

const rankingColumns = [
  {
    title: "商品",
    slotName: "itemTitle",
  },
  {
    title: "价格",
    slotName: "price",
    width: 100,
  },
  {
    title: "销量",
    slotName: "soldCount",
    width: 120,
  },
];

const maxTrendSales = () => {
  return Math.max(...trendData.value.map((d) => d.sales), 1);
};

function getBarHeight(sales) {
  const max = maxTrendSales();
  return Math.max((sales / max) * 100, 4);
}

async function loadOverview() {
  loading.value = true;
  try {
    const res = await getSellerOverview();
    stats.value = res;
  } catch (e) {
    Message.error(e.message || "加载统计数据失败");
  } finally {
    loading.value = false;
  }
}

async function loadTrend() {
  try {
    const res = await getSellerTrend(parseInt(trendDays.value));
    trendData.value = res || [];
  } catch (e) {
    console.error("加载趋势数据失败", e);
  }
}

async function loadRanking() {
  rankingLoading.value = true;
  try {
    const res = await getSellerRanking(10);
    rankingData.value = res || [];
  } catch (e) {
    console.error("加载排行数据失败", e);
  } finally {
    rankingLoading.value = false;
  }
}

onMounted(() => {
  loadOverview();
  loadTrend();
  loadRanking();
});
</script>

<style lang="scss" scoped>
.seller-stats-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.06);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  &--today .stat-icon {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
    color: white;
  }

  &--month .stat-icon {
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
    color: white;
  }

  &--total .stat-icon {
    background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
    color: white;
  }

  &--items .stat-icon {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
    color: white;
  }

  .stat-info {
    flex: 1;

    .stat-label {
      font-size: 13px;
      color: #6b7280;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 4px;
    }

    .stat-count {
      font-size: 12px;
      color: #9ca3af;
    }
  }
}

.trend-section,
.ranking-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }
}

.trend-chart {
  height: 200px;
  padding: 10px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 150px;
  gap: 8px;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;

  .chart-bar {
    width: 100%;
    max-width: 40px;
    background: linear-gradient(180deg, #7c3aed 0%, #a78bfa 100%);
    border-radius: 4px 4px 0 0;
    position: relative;
    transition: height 0.3s ease;
    cursor: pointer;

    &:hover .bar-tooltip {
      opacity: 1;
      transform: translateX(-50%) translateY(0);
    }

    .bar-tooltip {
      position: absolute;
      bottom: 100%;
      left: 50%;
      transform: translateX(-50%) translateY(10px);
      background: rgba(0, 0, 0, 0.8);
      color: white;
      padding: 6px 10px;
      border-radius: 6px;
      font-size: 12px;
      white-space: nowrap;
      opacity: 0;
      transition: all 0.2s;
      margin-bottom: 8px;

      span {
        display: block;
      }
    }
  }

  .chart-label {
    margin-top: 8px;
    font-size: 11px;
    color: #9ca3af;
    text-align: center;
  }
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  .item-thumb {
    width: 40px;
    height: 40px;
    border-radius: 6px;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #9ca3af;
      font-size: 16px;
    }
  }

  .item-name {
    font-size: 13px;
    color: #374151;
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.price {
  font-weight: 600;
  color: #f53f3f;
}
</style>
