<template>
  <div class="ops-enhanced-dashboard">
    <!-- 统计卡片区域 -->
    <a-row :gutter="[16, 16]" class="stats-section">
      <a-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in statsCards" :key="index">
        <div class="enhanced-stat-card" :style="{ '--accent-color': stat.color }">
          <div class="stat-icon">
            <component :is="stat.icon" />
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ formatNumber(stats[stat.key] || 0) }}
              <span v-if="stat.suffix" class="stat-suffix">{{ stat.suffix }}</span>
            </div>
            <div class="stat-label">{{ stat.label }}</div>
            <div v-if="stats[stat.trendKey] !== undefined" class="stat-trend" :class="{ 'trend-up': stats[stat.trendKey] > 0, 'trend-down': stats[stat.trendKey] < 0 }">
              {{ stats[stat.trendKey] > 0 ? '↑' : '↓' }} 
              {{ Math.abs(stats[stat.trendKey] || 0) }}
              <span class="trend-label">今日</span>
            </div>
          </div>
          <!-- 迷你趋势图 -->
          <div class="mini-chart">
            <svg viewBox="0 0 60 24" class="sparkline">
              <polyline 
                :points="generateSparkline(stat.key)" 
                fill="none" 
                stroke="currentColor"
                stroke-width="2"
              />
            </svg>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 快捷操作 + 待审核 -->
    <a-row :gutter="[16, 16]" class="middle-section">
      <a-col :xs="24" :lg="16">
        <a-card title="快捷操作" :bordered="false" class="quick-actions-card">
          <a-row :gutter="[12, 12]">
            <a-col :xs="8" :sm="6" v-for="action in quickActions" :key="action.label">
              <div class="action-item" @click="handleAction(action)">
                <div class="action-icon" :style="{ background: action.bgColor }">
                  <component :is="action.icon" :style="{ color: action.color }" />
                </div>
                <span class="action-label">{{ action.label }}</span>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-card title="实时动态" :bordered="false" class="activity-card">
          <template #extra>
            <a-button type="text" size="small" @click="refreshActivities">
              <template #icon><icon-refresh /></template>
              刷新
            </a-button>
          </template>
          <a-timeline v-if="activities.length > 0" pending>
            <a-timeline-item
              v-for="activity in activities.slice(0, 5)"
              :key="activity.id"
              :dot-color="activity.color || '#165DFF'"
            >
              <div class="activity-item">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无动态" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card title="待审核概览" :bordered="false" class="pending-card">
          <template #extra>
            <a-badge :count="totalPending" :max-count="99">
              <a-button type="text" size="small">查看全部</a-button>
            </a-badge>
          </template>
          
          <div class="pending-list">
            <div 
              v-for="item in pendingItems.slice(0, 4)" 
              :key="item.id"
              class="pending-item"
              @click="$router.push('/ops/review')"
            >
              <div class="pending-type" :class="'type-' + item.type">
                {{ getTypeIcon(item.type) }}
              </div>
              <div class="pending-info">
                <div class="pending-title">{{ truncate(item.title, 20) }}</div>
                <div class="pending-meta">{{ item.author }} · {{ formatTime(item.time) }}</div>
              </div>
              <div class="pending-action">
                <a-button type="primary" size="small" status="success">通过</a-button>
              </div>
            </div>
            
            <a-empty v-if="pendingItems.length === 0" description="暂无待审核项" />
          </div>
        </a-card>

        <a-card title="分类占比" :bordered="false" class="category-card">
          <OpsBarChart :data="categoryData" height="240" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="[16, 16]" class="charts-section">
      <a-col :xs="24" :lg="14">
        <a-card title="近7天订单趋势" :bordered="false" class="chart-card">
          <OpsLineChart :data="chartData" type="bar" height="300" title="订单数量" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="状态分布" :bordered="false" class="status-card">
          <OpsDonutChart 
            :data="statusLegend" 
            height="280" 
            center-text="总订单"
            :show-legend="true"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconUserGroup,
  IconStorage,
  IconFile,
  IconTag,
  IconCheckCircle,
  IconEye,
  IconRefresh,
} from "@arco-design/web-vue/es/icon";
import { http } from "../../../services/core/http";
import { OpsBarChart, OpsLineChart, OpsDonutChart } from "../../../components/charts";

const router = useRouter();
const loading = ref(false);
const stats = ref({});
const activities = ref([]);
const pendingItems = ref([]);
const chartData = ref([]);
const categoryData = ref([]);

let refreshTimer = null;

const statsCards = [
  {
    key: "totalUsers",
    label: "总用户数",
    icon: IconUserGroup,
    color: "#165DFF",
    suffix: "",
    trendKey: "newUsersToday",
  },
  {
    key: "totalItems",
    label: "总商品数",
    icon: IconStorage,
    color: "#00B42A",
    trendKey: "newItemsToday",
  },
  {
    key: "totalOrders",
    label: "总订单数",
    icon: IconFile,
    color: "#FF7D00",
    trendKey: "todayOrders",
  },
  {
    key: "todayRevenue",
    label: "今日成交额",
    icon: IconTag,
    color: "#F53F3F",
    suffix: "¥",
    trendKey: "revenueGrowth",
  },
];

const quickActions = [
  { label: "审批工作台", icon: IconCheckCircle, path: "/ops/review", bgColor: "#E6F1FF", color: "#165DFF" },
  { label: "最新订单", icon: IconEye, path: "/ops/orders", bgColor: "#E8FFEA", color: "#00B42A" },
  { label: "用户管理", icon: IconUserGroup, path: "/ops/user-manage", bgColor: "#F5E8FF", color: "#722ED1" },
  { label: "商品审核", icon: IconStorage, path: "/ops/reviews", bgColor: "#FFF7E8", color: "#FF7D00" },
];

function formatNumber(num) {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
  return num.toString();
}

function formatTime(time) {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前';
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function truncate(text, len) {
  if (!text) return '';
  return text.length > len ? text.substring(0, len) + '...' : text;
}

function getTypeIcon(type) {
  const icons = { item: '📦', review: '⭐', circle: '💬' };
  return icons[type] || '📋';
}

// 简单的 sparkline 数据生成
function generateSparkline(key) {
  const baseValue = Math.max(stats.value[key] || 0, 10);
  const points = [];
  for (let i = 0; i < 7; i++) {
    const value = baseValue * (0.85 + Math.random() * 0.3);
    const x = (i / 6) * 60;
    const y = 24 - (value / (baseValue * 1.3)) * 22;
    points.push(`${x},${y}`);
  }
  return points.join(' ');
}

const totalPending = computed(() => {
  return (stats.value.pendingItemsCount || 0) + 
         (stats.value.pendingReviewsCount || 0) + 
         (stats.value.pendingPosts || 0);
});

const totalOrders = computed(() => stats.value.totalOrders || 0);

const yAxisValues = computed(() => {
  const maxVal = Math.max(...chartData.value.map(d => d.value), 10);
  return [Math.ceil(maxVal), Math.ceil(maxVal * 0.66), Math.ceil(maxVal * 0.33), 0];
});

function getBarHeight(value) {
  const maxVal = Math.max(...chartData.value.map(d => d.value), 1);
  return (value / maxVal) * 80 + 10;
}

const donutSegments = computed(() => {
  const data = [
    { name: '待支付', count: stats.value.pendingPayment || 0, color: '#165DFF' },
    { name: '进行中', count: stats.value.processing || 0, color: '#00B42A' },
    { name: '已完成', count: stats.value.completed || 0, color: '#FF7D00' },
    { name: '已取消', count: stats.value.cancelled || 0, color: '#86909C' },
  ];
  
  const total = data.reduce((sum, d) => sum + d.count, 0) || 1;
  let offset = 0;
  
  return data.map(item => {
    const percentage = (item.count / total) * 100;
    const length = (percentage / 100) * 314;
    const segment = { ...item, length, offset };
    offset += length;
    return segment;
  });
});

const statusLegend = computed(() => donutSegments.value.map(s => ({
  name: s.name,
  count: s.count,
  color: s.color,
})));

async function loadDashboardData() {
  loading.value = true;
  
  try {
    // 加载统计数据
    const [statsRes, pendingRes] = await Promise.allSettled([
      http.get('/ops/stats/brief'),
      http.get('/ops/pending-counts'),
    ]);
    
    if (statsRes.status === 'fulfilled') {
      stats.value = statsRes.value?.data || statsRes.value || {};
    }
    
    if (pendingRes.status === 'fulfilled') {
      Object.assign(stats.value, pendingRes.value?.data || {});
      
      // 构建待审核列表
      const counts = pendingRes.value?.data || {};
      pendingItems.value = [
        ...(counts.items ? Array(Math.min(counts.items, 3)).fill({ id: 1, type: 'item', title: '新商品待审核', author: '卖家', time: new Date() }) : []),
        ...(counts.reviews ? Array(Math.min(counts.reviews, 2)).fill({ id: 2, type: 'review', title: '新评价待审核', author: '买家', time: new Date() }) : []),
        ...(counts.circle ? [{ id: 3, type: 'circle', title: '新帖子待审核', author: '用户', time: new Date() }] : []),
      ];
    }
    
    // 模拟图表数据（实际应从API获取）
    generateChartData();
    generateCategoryData();
    
    // 模拟活动数据
    generateActivities();
    
  } catch (e) {
    console.error('[Dashboard] 加载失败:', e);
    Message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
}

function generateChartData() {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  chartData.value = days.map((label, i) => ({
    label,
    value: Math.floor(Math.random() * 50) + 10 + (i === new Date().getDay() - 1 ? 30 : 0),
  }));
}

function generateCategoryData() {
  const categories = [
    { name: '数码产品', color: '#165DFF' },
    { name: '图书教材', color: '#00B42A' },
    { name: '服饰鞋包', color: '#FF7D00' },
    { name: '生活用品', color: '#722ED1' },
    { name: '运动户外', color: '#F53F3F' },
  ];
  
  const totalCount = categories.length * 20 + Math.floor(Math.random() * 50);
  
  categoryData.value = categories.map(cat => ({
    ...cat,
    count: Math.floor(totalCount / categories.length) + Math.floor(Math.random() * 15),
  })).map(cat => ({
    ...cat,
    percentage: Math.round((cat.count / totalCount) * 100),
  }));
}

function generateActivities() {
  const templates = [
    { title: '新用户注册：小明同学', color: '#165DFF' },
    { title: '发布新商品：MacBook Pro', color: '#00B42A' },
    { title: '完成订单：#10086 ¥299', color: '#FF7D00' },
    { title: '提交评价：雅诗兰黛眼霜 ⭐5分', color: '#722ED1' },
    { title: '发布圈子帖子：校园生活分享', color: '#14C9C9' },
  ];
  
  activities.value = templates.map((tpl, i) => ({
    id: i + 1,
    ...tpl,
    time: new Date(Date.now() - i * 1800000),
  }));
}

function handleAction(action) {
  if (action.path) {
    router.push(action.path);
  } else if (action.handler) {
    Message.info(`${action.label} 功能开发中`);
  }
}

async function refreshActivities() {
  Message.loading('刷新中...');
  await new Promise(resolve => setTimeout(resolve, 500));
  generateActivities();
  Message.success('已刷新');
}

onMounted(() => {
  loadDashboardData();
  
  // 每5分钟自动刷新
  refreshTimer = setInterval(loadDashboardData, 300000);
});

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});
</script>

<style lang="scss" scoped>
.ops-enhanced-dashboard {
  padding: 0;

  .stats-section {
    margin-bottom: 16px;
  }

  .enhanced-stat-card {
    position: relative;
    padding: 20px;
    background: var(--color-bg-white, #fff);
    border-radius: 12px;
    border: 1px solid var(--color-border-2, #e5e6eb);
    display: flex;
    align-items: center;
    gap: 16px;
    overflow: hidden;
    transition: all 200ms ease-out;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);

      .mini-chart {
        opacity: 1;
      }
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: var(--accent-color);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: var(--accent-color);
      background: linear-gradient(135deg, rgba(var(--accent-color), 0.1) 0%, rgba(var(--accent-color), 0.05) 100%);
      flex-shrink: 0;
    }

    .stat-content {
      flex: 1;
      min-width: 0;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--color-text-1, #1d2129);
        line-height: 1.2;

        .stat-suffix {
          font-size: 14px;
          font-weight: 400;
          color: var(--color-text-3, #86909c);
          margin-left: 2px;
        }
      }

      .stat-label {
        font-size: 13px;
        color: var(--color-text-3, #86909c);
        margin-top: 2px;
      }

      .stat-trend {
        display: inline-flex;
        align-items: center;
        gap: 2px;
        font-size: 12px;
        margin-top: 4px;
        padding: 2px 6px;
        border-radius: 4px;

        &.trend-up {
          color: #00b42a;
          background: #e8ffea;
        }

        &.trend-down {
          color: #f53f3f;
          background: #ffece8;
        }

        .trend-label {
          opacity: 0.8;
        }
      }
    }

    .mini-chart {
      position: absolute;
      bottom: 8px;
      right: 12px;
      width: 60px;
      height: 24px;
      opacity: 0;
      transition: opacity 200ms ease-out;
      color: var(--accent-color);

      .sparkline {
        width: 100%;
        height: 100%;
      }
    }
  }

  .middle-section {
    margin-bottom: 16px;
  }

  .quick-actions-card {
    margin-bottom: 16px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 16px 8px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 150ms ease-out;

      &:hover {
        background: var(--color-fill-1, #f7f8fa);
        transform: translateY(-2px);
      }

      .action-icon {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
      }

      .action-label {
        font-size: 13px;
        color: var(--color-text-2, #4e5969);
        text-align: center;
      }
    }
  }

  .activity-card {
    .activity-item {
      .activity-title {
        font-size: 14px;
        color: var(--color-text-1, #1d2129);
        margin-bottom: 2px;
      }

      .activity-time {
        font-size: 12px;
        color: var(--color-text-4, #c9cdd4);
      }
    }
  }

  .pending-card {
    margin-bottom: 16px;
    height: fit-content;

    .pending-list {
      .pending-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: background 150ms ease-out;

        &:hover {
          background: var(--color-fill-1, #f7f8fa);
        }

        &:not(:last-child) {
          margin-bottom: 8px;
        }

        .pending-type {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          background: #f2f3f5;
          flex-shrink: 0;
        }

        .pending-info {
          flex: 1;
          min-width: 0;

          .pending-title {
            font-size: 13px;
            color: var(--color-text-1, #1d2129);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .pending-meta {
            font-size: 11px;
            color: var(--color-text-4, #c9cdd4);
            margin-top: 2px;
          }
        }

        .pending-action {
          flex-shrink: 0;
        }
      }
    }
  }

  .category-card {
    .category-bars {
      .category-bar-item {
        &:not(:last-child) {
          margin-bottom: 12px;
        }

        .bar-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 4px;

          .cat-name {
            font-size: 13px;
            color: var(--color-text-2, #4e5969);
          }

          .cat-count {
            font-size: 13px;
            font-weight: 600;
            color: var(--color-text-1, #1d2129);
          }
        }

        .bar-track {
          height: 8px;
          background: #f2f3f5;
          border-radius: 4px;
          overflow: hidden;

          .bar-fill {
            height: 100%;
            border-radius: 4px;
            transition: width 500ms ease-out;
          }
        }
      }
    }
  }

  .charts-section {
    .chart-card {
      .simple-bar-chart {
        display: flex;
        gap: 8px;
        padding: 16px 0;

        .chart-y-axis {
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          font-size: 11px;
          color: var(--color-text-4, #c9cdd4);
          padding-right: 8px;
          min-height: 160px;
        }

        .chart-area {
          flex: 1;
          display: flex;
          align-items: flex-end;
          gap: 8px;
          height: 160px;
          padding-bottom: 24px;

          .chart-bar-wrapper {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            height: 100%;

            .chart-bar {
              width: 100%;
              max-width: 40px;
              background: linear-gradient(180deg, #165DFF 0%, #69b1ff 100%);
              border-radius: 4px 4px 0 0;
              position: relative;
              transition: height 300ms ease-out;
              cursor: pointer;

              &:hover {
                .bar-tooltip {
                  opacity: 1;
                  visibility: visible;
                }
              }

              .bar-tooltip {
                position: absolute;
                top: -28px;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(0, 0, 0, 0.75);
                color: #fff;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 11px;
                white-space: nowrap;
                opacity: 0;
                visibility: hidden;
                transition: all 150ms ease-out;
              }
            }

            .chart-label {
              font-size: 11px;
              color: var(--color-text-4, #c9cdd4);
              margin-top: 6px;
            }
          }
        }
      }
    }

    .status-card {
      .donut-chart {
        position: relative;
        width: 140px;
        height: 140px;
        margin: 0 auto 16px;

        .donut-svg {
          width: 100%;
          height: 100%;
        }

        .donut-segment {
          transition: stroke-dasharray 500ms ease-out;
          cursor: pointer;

          &:hover {
            opacity: 0.8;
          }
        }

        .donut-center {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          text-align: center;

          .donut-total {
            font-size: 24px;
            font-weight: 700;
            color: var(--color-text-1, #1d2129);
          }

          .donut-label {
            font-size: 12px;
            color: var(--color-text-4, #c9cdd4);
          }
        }
      }

      .legend-list {
        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 6px 0;

          &:not(:last-child) {
            border-bottom: 1px solid var(--color-border-1, #f2f3f5);
          }

          .legend-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            flex-shrink: 0;
          }

          .legend-name {
            flex: 1;
            font-size: 13px;
            color: var(--color-text-2, #4e5969);
          }

          .legend-count {
            font-size: 13px;
            font-weight: 600;
            color: var(--color-text-1, #1d2129);
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .ops-enhanced-dashboard {
    .enhanced-stat-card {
      padding: 14px;

      .stat-icon {
        width: 40px;
        height: 40px;
        font-size: 20px;
      }

      .stat-content .stat-value {
        font-size: 20px;
      }

      .mini-chart {
        display: none;
      }
    }

    .charts-section .chart-card .simple-bar-chart {
      .chart-area {
        gap: 4px;

        .chart-bar-wrapper .chart-bar {
          max-width: 28px;
        }
      }
    }
  }
}
</style>
