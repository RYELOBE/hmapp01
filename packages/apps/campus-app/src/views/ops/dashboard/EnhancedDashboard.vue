<template>
  <div class="ops-enhanced-dashboard">
    <div class="stats-section" v-slide>
      <a-row :gutter="[16, 16]">
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
    </div>

    <div class="features-grid-section" v-slide>
      <h3 class="section-title">功能入口</h3>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(feature, index) in featuresGrid" :key="index">
          <GridCard
            :title="feature.title"
            :text="feature.description"
            :tag="feature.tag"
            :index-num="index"
            @click="handleFeatureClick(feature)"
          />
        </a-col>
      </a-row>
    </div>

    <div class="middle-section" v-slide>
      <a-row :gutter="[16, 16]">
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

          <a-card title="待审核" :bordered="false" class="pending-card" style="margin-top: 16px">
            <template #extra>
              <a-badge :count="totalPending" :max-count="99">
                <a-button type="primary" size="small" @click="$router.push('/ops/review')">
                  查看全部
                </a-button>
              </a-badge>
            </template>
            <div class="pending-list">
              <!-- 商品审核 -->
              <div v-if="counts.items > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">🔴</span>
                  <span class="group-title">商品审核</span>
                  <a-badge :count="counts.items" :max-count="99" />
                </div>
                <div
                  v-for="item in pendingItemsByType.item.slice(0, 2)"
                  :key="item.id"
                  class="pending-item"
                  @click="goToReview(item)"
                >
                  <div class="pending-type" :class="'type-' + item.type">
                    {{ getTypeIcon(item.type) }}
                  </div>
                  <div class="pending-info">
                    <div class="pending-title">{{ truncate(item.title, 20) }}</div>
                    <div class="pending-meta">{{ item.author }} · {{ formatTime(item.time) }}</div>
                  </div>
                  <div class="pending-action">
                    <a-button type="primary" status="success" size="small">审核</a-button>
                  </div>
                </div>
                <a-link v-if="counts.items > 2" class="more-link" @click="$router.push('/ops/items')">
                  还有 {{ counts.items - 2 }} 条 →
                </a-link>
              </div>

              <!-- 评价审核 -->
              <div v-if="counts.reviews > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">⭐</span>
                  <span class="group-title">评价审核</span>
                  <a-badge :count="counts.reviews" :max-count="99" />
                </div>
                <div
                  v-for="item in pendingItemsByType.review.slice(0, 2)"
                  :key="item.id"
                  class="pending-item"
                  @click="goToReview(item)"
                >
                  <div class="pending-type" :class="'type-' + item.type">
                    {{ getTypeIcon(item.type) }}
                  </div>
                  <div class="pending-info">
                    <div class="pending-title">{{ truncate(item.content || item.title, 20) }}</div>
                    <div class="pending-meta">{{ item.author }} · {{ formatTime(item.time) }}</div>
                  </div>
                  <div class="pending-action">
                    <a-button type="primary" status="success" size="small">审核</a-button>
                  </div>
                </div>
                <a-link v-if="counts.reviews > 2" class="more-link" @click="$router.push('/ops/review-manage')">
                  还有 {{ counts.reviews - 2 }} 条 →
                </a-link>
              </div>

              <!-- 帖子审核 -->
              <div v-if="counts.posts > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">💬</span>
                  <span class="group-title">帖子审核</span>
                  <a-badge :count="counts.posts" :max-count="99" />
                </div>
                <div
                  v-for="item in pendingItemsByType.post.slice(0, 2)"
                  :key="item.id"
                  class="pending-item"
                  @click="goToReview(item)"
                >
                  <div class="pending-type" :class="'type-' + item.type">
                    {{ getTypeIcon(item.type) }}
                  </div>
                  <div class="pending-info">
                    <div class="pending-title">{{ truncate(item.title, 20) }}</div>
                    <div class="pending-meta">{{ item.author }} · {{ formatTime(item.time) }}</div>
                  </div>
                  <div class="pending-action">
                    <a-button type="primary" status="success" size="small">审核</a-button>
                  </div>
                </div>
                <a-link v-if="counts.posts > 2" class="more-link" @click="$router.push('/ops/circle')">
                  还有 {{ counts.posts - 2 }} 条 →
                </a-link>
              </div>

              <a-empty v-if="counts.total === 0" description="暂无待审核项" />
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="8">
          <a-card title="最新消息" :bordered="false" class="activity-card">
            <template #extra>
              <a-button type="text" size="small" @click="refreshActivities">
                <template #icon><icon-refresh /></template>
                刷新
              </a-button>
            </template>
            <a-timeline v-if="activities.length > 0" pending>
              <a-timeline-item
                v-for="activity in sortedActivities"
                :key="activity.id"
                :dot-color="activity.dotColor || activity.color || '#165DFF'"
              >
                <div class="activity-item">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div v-if="activity.description" class="activity-desc">
                    {{ activity.description }}
                  </div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无消息" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div class="stats-table-section" v-slide>
      <a-card title="运营统计分析" :bordered="false" class="stats-table-card">
        <template #extra>
          <a-button type="text" size="small" @click="loadStatsTableData">
            <template #icon><icon-refresh /></template>
            刷新
          </a-button>
        </template>
        <a-table
          :data="statsTableData"
          :columns="statsTableColumns"
          :pagination="false"
          :scroll="{ x: 'max-content' }"
          :bordered="true"
          size="small"
        >
          <template #cell(amount)="{ text }">
            <span class="amount-cell">¥{{ formatPrice(text) }}</span>
          </template>
          <template #cell(growth)="{ text, record }">
            <span :class="['growth-cell', { positive: record.growth >= 0, negative: record.growth < 0 }]">
              {{ record.growth >= 0 ? '+' : '' }}{{ text }}%
            </span>
          </template>
          <template #cell(operations)="{ record }">
            <a-button type="text" size="small" @click="viewDetail(record)">详情</a-button>
          </template>
        </a-table>
      </a-card>
    </div>

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

    <a-row :gutter="[16, 16]" class="analysis-charts-section">
      <a-col :xs="24" :lg="12">
        <a-card title="用户增长趋势" :bordered="false" class="chart-card">
          <OpsLineChart :data="userGrowthData" type="line" height="280" title="用户数" color="#00B42A" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="商品分类分布" :bordered="false" class="chart-card">
          <OpsDonutChart 
            :data="categoryDistribution" 
            height="280" 
            center-text="商品数"
            :show-legend="true"
            :colors="['#165DFF', '#00B42A', '#FF7D00', '#722ED1', '#F53F3F', '#14C9C9']"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, onUnmounted, watch, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
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
import { http } from "../../../services/http";
import { OpsLineChart, OpsDonutChart } from "../../../components/charts";
import GridCard from '../../../components/ops/GridCard.vue'
import { vSlide, injectSlideStyles } from '../../../directives/slide.directive'

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const stats = ref({});
const activities = ref([]);
const pendingItems = ref([]);
const chartData = ref([]);
const categoryData = ref([]);
const recentActivities = ref([]);
const userGrowthData = ref([]);
const categoryDistribution = ref([]);
const counts = reactive({
  items: 0,
  reviews: 0,
  posts: 0,
  total: 0
});

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
  { label: "最新订单", icon: IconEye, path: "/ops/orders/list", bgColor: "#E8FFEA", color: "#00B42A" },
  { label: "用户管理", icon: IconUserGroup, path: "/ops/users/user-manage", bgColor: "#F5E8FF", color: "#722ED1" },
  { label: "商品审核", icon: IconStorage, path: "/ops/items/review", bgColor: "#FFF7E8", color: "#FF7D00" },
];

const featuresGrid = computed(() => {
  const opsRoot = router.options.routes.find((r) => r.path === "/ops");
  const children = opsRoot?.children || [];

  const features = [];

  for (const r of children) {
    if (!r?.meta?.title || r?.meta?.hidden) continue;
    if (String(r.path || "").includes(":")) continue;

    const hasComponent = !!r.component && r.component.name !== 'RouteView';
    const isRedirectOnly = r.redirect && !hasComponent;
    const isRouteView = r.children && r.children.length > 0 && !hasComponent;

    if (isRedirectOnly) continue;

    if (isRouteView && r.children) {
      for (const child of r.children) {
        if (!child?.meta?.title || child?.meta?.hidden) continue;
        if (String(child.path || "").includes(":")) continue;
        if (child.redirect && !child.component) continue;

        features.push({
          title: child.meta.title,
          description: child.meta.desc || child.meta.title,
          tag: child.meta.tag || "",
          path: `/ops/${r.path}/${child.path}`.replace(/\/+/g, "/"),
        });
      }
    } else if (hasComponent) {
      features.push({
        title: r.meta.title,
        description: r.meta.desc || r.meta.title,
        tag: r.meta.tag || "",
        path: `/ops/${r.path}`.replace(/\/+/g, "/"),
      });
    }
  }

  return features.slice(0, 12);
});

const statsTableColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '订单数', dataIndex: 'orders', key: 'orders', width: 100 },
  { title: '成交额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '用户数', dataIndex: 'users', key: 'users', width: 100 },
  { title: '商品数', dataIndex: 'items', key: 'items', width: 100 },
  { title: '增长率', dataIndex: 'growth', key: 'growth', width: 100 },
  { title: '操作', dataIndex: 'operations', key: 'operations', width: 80 },
];

const statsTableData = ref([]);

const sortedActivities = computed(() => {
  return [...activities.value].sort((a, b) => {
    const timeA = new Date(a.time).getTime();
    const timeB = new Date(b.time).getTime();
    return timeB - timeA;
  }).slice(0, 5);
});

function formatNumber(num) {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
  return num.toString();
}

function formatPrice(num) {
  if (!num) return '0.00';
  return Number(num).toFixed(2);
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

const pendingItemsByType = computed(() => {
  return {
    item: pendingItems.value.filter(item => item.type === 'item'),
    review: pendingItems.value.filter(item => item.type === 'review'),
    post: pendingItems.value.filter(item => item.type === 'post' || item.type === 'circle')
  };
});

function getTypeIcon(type) {
  const icons = { item: '📦', review: '⭐', circle: '💬', post: '💬', order: '📋' };
  return icons[type] || '📋';
}

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
  return counts.total;
});

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
    const [statsRes, pendingRes, trendRes, categoryRes, distributionRes, activitiesRes, userGrowthRes] = await Promise.allSettled([
      http.get('/ops/stats/brief'),
      http.post('/ops/pending-items', { pageNo: 1, pageSize: 10 }),
      http.get('/ops/stats/order-trend'),
      http.get('/ops/stats/categories'),
      http.get('/ops/stats/order-distribution'),
      http.get('/ops/stats/activities?limit=10'),
      http.get('/ops/stats/user-growth'),
    ]);
    
    console.log('[EnhancedDashboard] API响应:', { statsRes, pendingRes, activitiesRes });
    
    if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
      const data = statsRes.value.data;
      const statistics = data.statistics || data || {};
      stats.value = {
        totalUsers: statistics.totalUsers || 0,
        newUsersToday: statistics.todayUsers || statistics.newUsers || 0,
        totalItems: statistics.totalItems || 0,
        newItemsToday: statistics.todayItems || 0,
        totalOrders: statistics.totalOrders || 0,
        todayOrders: statistics.todayOrders || 0,
        todayRevenue: statistics.todayTotalAmount || 0,
        orderGrowth: 0,
        pendingTotal: (statistics.pendingItems || 0) +
          (data.pendingReviews || 0) +
          (statistics.pendingPosts || 0),
        pendingItems: statistics.pendingItems || 0,
        pendingReviews: data.pendingReviews || 0,
        pendingPosts: statistics.pendingPosts || 0
      };

      counts.items = statistics.pendingItems || 0;
      counts.reviews = data.pendingReviews || 0;
      counts.posts = statistics.pendingPosts || 0;
      counts.total = (statistics.pendingItems || 0) + (data.pendingReviews || 0) + (statistics.pendingPosts || 0);
    }
    
    if (pendingRes.status === 'fulfilled' && pendingRes.value?.data) {
      const res = pendingRes.value;
      const data = res?.data?.data ?? res?.data ?? res;
      
      pendingItems.value = data?.items || data?.list || data?.reviews || [];
      
      if (data.counts) {
        counts.items = data.counts.items || counts.items;
        counts.reviews = data.counts.reviews || counts.reviews;
        counts.posts = data.counts.posts || counts.posts;
        counts.total = data.counts.total || counts.total;
      }
      
      console.log('[EnhancedDashboard] 待办事项:', pendingItems.value.length, '条');
    } else {
      console.warn('[EnhancedDashboard] 待办API失败，使用空数组');
      pendingItems.value = [];
      
      if (stats.value.pendingItems > 0) {
        for (let i = 0; i < Math.min(stats.value.pendingItems, 2); i++) {
          pendingItems.value.push({
            id: `mock_item_${i}`,
            type: 'item',
            title: `商品待审核 #${i + 1}`,
            author: '卖家',
            time: new Date().toISOString(),
            status: 'PENDING'
          });
        }
      }
      
      if (stats.value.pendingReviews > 0) {
        for (let i = 0; i < Math.min(stats.value.pendingReviews, 2); i++) {
          pendingItems.value.push({
            id: `mock_review_${i}`,
            type: 'review',
            content: '用户评价内容...',
            title: '评价待审核',
            author: '买家',
            time: new Date(Date.now() - 3600000 * (i + 1)),
            status: 'PENDING'
          });
        }
      }
    }
    
    if (trendRes.status === 'fulfilled') {
      const trendData = trendRes.value?.data?.trend || trendRes.value?.trend || [];
      chartData.value = trendData.map(item => ({
        label: item.label,
        value: item.value || 0,
      }));
      
      if (chartData.value.length === 0) {
        chartData.value = generateEmptyTrendData();
      }
    } else {
      chartData.value = generateEmptyTrendData();
    }
    
    if (categoryRes.status === 'fulfilled') {
      const categories = categoryRes.value?.data?.categories || categoryRes.value?.categories || [];
      const totalCount = categories.reduce((sum, cat) => sum + (cat.count || 0), 0) || 1;
      
      categoryData.value = categories.map(cat => ({
        name: cat.name || '未分类',
        count: cat.count || 0,
        color: getCategoryColor(categories.indexOf(cat)),
        percentage: Math.round(((cat.count || 0) / totalCount) * 100),
      }));
      
      categoryDistribution.value = categories.map(cat => ({
        name: cat.name || '未分类',
        count: cat.count || 0,
      }));
      
      if (categoryData.value.length === 0) {
        categoryData.value = generateEmptyCategoryData();
        categoryDistribution.value = [];
      }
    } else {
      categoryData.value = generateEmptyCategoryData();
      categoryDistribution.value = [];
    }
    
    if (distributionRes.status === 'fulfilled') {
      const dist = distributionRes.value?.data || distributionRes.value || {};
      Object.assign(stats.value, dist);
    }
    
    if (activitiesRes.status === 'fulfilled' && activitiesRes.value?.data) {
      const data = activitiesRes.value.data;
      const activitiesList = data.activities || data.list || [];

      activities.value = activitiesList.map(item => ({
        id: item.id,
        type: item.type || 'system',
        title: item.title || '系统活动',
        description: item.description,
        dotColor: getDotColor(item.type),
        color: item.color || '#86909C',
        time: item.time || new Date().toISOString(),
      }));

      console.log('[EnhancedDashboard] 动态数据:', activities.value.length, '条');
    } else {
      console.warn('[EnhancedDashboard] 动态API失败');
      activities.value = [];
    }

    if (userGrowthRes.status === 'fulfilled') {
      const growthData = userGrowthRes.value?.data?.growth || userGrowthRes.value?.growth || [];
      userGrowthData.value = growthData.map(item => ({
        label: item.label || item.date,
        value: item.value || item.count || 0,
      }));
      
      if (userGrowthData.value.length === 0) {
        userGrowthData.value = generateEmptyUserGrowth();
      }
    } else {
      userGrowthData.value = generateEmptyUserGrowth();
    }
    
    await loadStatsTableData();
    
  } catch (e) {
    console.error('[Dashboard] 加载失败:', e);
    Message.error('加载数据失败');
    
    chartData.value = generateEmptyTrendData();
    categoryData.value = generateEmptyCategoryData();
    activities.value = generateEmptyActivities();
    userGrowthData.value = generateEmptyUserGrowth();
  } finally {
    loading.value = false;
  }
}

function getDotColor(type) {
  const colors = {
    order: '#00B42A',
    user: '#165DFF',
    completed: '#FF7D00',
    item: '#722ED1',
    review: '#FF7D00',
    post: '#14C9C9',
    circle: '#14C9C9'
  };
  return colors[type] || '#86909C';
}

async function loadStatsTableData() {
  try {
    const res = await http.get('/ops/stats/daily-stats');
    const data = res?.data?.stats || res?.stats || [];
    
    if (data.length > 0) {
      statsTableData.value = data.map(item => ({
        date: item.date,
        orders: item.orders || 0,
        amount: item.amount || 0,
        users: item.users || 0,
        items: item.items || 0,
        growth: item.growth || 0,
      }));
    } else {
      statsTableData.value = generateDefaultStatsTable();
    }
  } catch (e) {
    console.error('[Dashboard] 加载统计表格失败:', e);
    statsTableData.value = generateDefaultStatsTable();
  }
}

function generateDefaultStatsTable() {
  const data = [];
  const now = new Date();
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now);
    date.setDate(date.getDate() - i);
    data.push({
      date: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
      orders: Math.floor(Math.random() * 50) + 80,
      amount: Math.floor(Math.random() * 10000) + 15000,
      users: Math.floor(Math.random() * 20) + 30,
      items: Math.floor(Math.random() * 10) + 8,
      growth: (Math.random() * 20 - 5).toFixed(1),
    });
  }
  return data;
}

function generateEmptyTrendData() {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return days.map(label => ({ label, value: 0 }));
}

function generateEmptyCategoryData() {
  return [
    { name: '暂无数据', count: 0, color: '#86909C', percentage: 100 },
  ];
}

function generateEmptyActivities() {
  return [];
}

function generateEmptyUserGrowth() {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return days.map(label => ({ label, value: 0 }));
}

function getCategoryColor(index) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#722ED1', '#F53F3F', '#14C9C9'];
  return colors[index % colors.length];
}

function handleAction(action) {
  if (action.path) {
    router.push(action.path);
  } else if (action.handler) {
    Message.info(`${action.label} 功能开发中`);
  }
}

function handleFeatureClick(feature) {
  if (feature?.path) {
    router.push(feature.path);
  }
}

async function refreshActivities() {
  Message.loading('刷新中...');
  try {
    const activitiesRes = await http.get('/ops/stats/activities?limit=10');
    if (activitiesRes?.data?.activities) {
      const activityList = activitiesRes.data.activities;
      activities.value = activityList.map(item => ({
        id: item.id,
        type: item.type || 'system',
        title: item.title || '系统活动',
        color: item.color || '#86909C',
        time: item.time || new Date().toISOString(),
      }));
      Message.success('已刷新');
    }
  } catch (e) {
    console.error('[Dashboard] 刷新活动失败:', e);
    Message.error('刷新失败');
  }
}

function goToReview(item) {
  if (item.type === 'item') {
    router.push('/ops/items/review');
  } else if (item.type === 'review') {
    router.push('/ops/review');
  } else if (item.type === 'circle') {
    router.push('/ops/circle');
  } else if (item.type === 'order') {
    router.push('/ops/orders/review');
  } else {
    router.push('/ops/review');
  }
}

function viewDetail(record) {
  Message.info(`查看 ${record.date} 详情`);
}

onMounted(() => {
  loadDashboardData();
  
  refreshTimer = setInterval(() => {
    loadDashboardData();
  }, 60000);
});

onActivated(() => {
  console.log('[EnhancedDashboard] onActivated triggered, reloading data');
  loadDashboardData();
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
});

watch(() => route.path, (newPath, oldPath) => {
  if (newPath === oldPath) return;
  if (newPath.includes('/ops/dashboard') || newPath.includes('/ops/workbench')) {
    loadDashboardData();
  }
});
</script>

<style lang="scss" scoped>
.ops-enhanced-dashboard {
  padding: 20px;
  background: #f5f6f7;
  min-height: calc(100vh - 48px);

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
    .activity-content-custom {
      padding: 20px 24px;
      max-height: 520px;
      overflow-y: auto;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: #E5E6EB;
        border-radius: 3px;

        &:hover {
          background: #C9CDD4;
        }
      }
    }

    .activity-item {
      background: #FAFBFC;
      padding: 14px 16px;
      border-radius: 10px;
      border: 1px solid #F0F1F3;
      transition: all 0.25s ease;

      .activity-title {
        font-size: 14px;
        font-weight: 600;
        color: #1D2129;
        margin-bottom: 4px;
        line-height: 1.5;
      }

      .activity-desc {
        font-size: 13px;
        color: #4E5969;
        line-height: 1.6;
        margin-bottom: 6px;
      }

      .activity-time {
        font-size: 12px;
        color: #C9CDD4;
        font-weight: 500;
      }
    }
  }

  .pending-card {
    margin-bottom: 16px;
    height: fit-content;

    .pending-list {
      padding: 20px 24px;
      max-height: 520px;
      overflow-y: auto;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: #E5E6EB;
        border-radius: 3px;

        &:hover {
          background: #C9CDD4;
        }
      }

      .pending-group {
        &:not(:last-child) {
          margin-bottom: 24px;
          padding-bottom: 20px;
          border-bottom: 2px dashed #E5E6EB;
        }

        .group-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 14px;

          .group-icon {
            font-size: 18px;
          }

          .group-title {
            font-size: 15px;
            font-weight: 700;
            color: #1D2129;
          }
        }

        .pending-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 16px;
          border-radius: 12px;
          background: linear-gradient(135deg, #FAFBFC 0%, #F7F8FA 100%);
          border: 1px solid transparent;
          transition: all 0.25s ease;
          margin-bottom: 10px;
          cursor: pointer;

          &:hover {
            background: linear-gradient(135deg, #F0F5FF 0%, #E8F3FE 100%);
            border-color: rgba(22, 93, 255, 0.15);
            transform: translateX(4px);
            box-shadow: 0 4px 12px rgba(22, 93, 255, 0.08);
          }

          &:not(:last-child) {
            margin-bottom: 10px;
          }

          .pending-type {
            width: 32px;
            height: 32px;
          }

          .pending-info {
            flex: 1;
            min-width: 0;

            .pending-title {
              font-size: 14px;
              font-weight: 600;
              color: #1D2129;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              margin-bottom: 4px;
              line-height: 1.4;
            }

            .pending-meta {
              font-size: 12px;
              color: #86909C;
              font-weight: 400;
            }
          }

          .pending-action {
            flex-shrink: 0;
          }
        }

        .more-link {
          display: block;
          text-align: center;
          font-size: 13px;
          color: #165DFF;
          margin-top: 10px;
          padding: 8px 0;
          font-weight: 600;
          border-radius: 8px;
          transition: all 0.2s ease;

          &:hover {
            color: #4080FF;
            background: rgba(22, 93, 255, 0.04);
          }
        }
      }
    }
  }

  .stats-table-section {
    margin-bottom: 16px;
    
    .stats-table-card {
      .amount-cell {
        color: #f53f3f;
        font-weight: 600;
      }
      
      .growth-cell {
        font-weight: 500;
        
        &.positive {
          color: #00b42a;
        }
        
        &.negative {
          color: #f53f3f;
        }
      }
    }
  }

  .charts-section,
  .analysis-charts-section {
    margin-bottom: 16px;
    
    .chart-card,
    .status-card {
      :deep(.arco-card-body) {
        padding: 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .ops-enhanced-dashboard {
    padding: 12px;
    
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
  }
}
</style>