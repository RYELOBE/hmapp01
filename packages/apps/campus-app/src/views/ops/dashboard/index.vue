<template>
  <div class="ops-dashboard">
    <a-spin :loading="loading" style="width: 100%">
      <!-- 第一行：KPI统计卡片 -->
      <a-row :gutter="[16, 16]" class="kpi-row">
        <a-col :xs="12" :sm="12" :md="6" v-for="(card, index) in kpiCards" :key="index">
          <div class="kpi-card" :style="{ '--accent-color': card.color }">
            <div class="kpi-icon">
              <component :is="card.icon" />
            </div>
            <div class="kpi-content">
              <div class="kpi-value">
                {{ formatNumber(stats[card.key] || 0) }}
                <span v-if="card.suffix" class="kpi-suffix">{{ card.suffix }}</span>
              </div>
              <div class="kpi-label">{{ card.label }}</div>
              <div 
                v-if="card.trendKey && stats[card.trendKey] !== undefined" 
                class="kpi-trend"
                :class="{ 'trend-up': stats[card.trendKey] > 0, 'trend-down': stats[card.trendKey] < 0 }"
              >
                {{ stats[card.trendKey] > 0 ? '↑' : '↓' }} 
                {{ Math.abs(stats[card.trendKey] || 0) }}
                <span class="trend-label">今日</span>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- 第二行：左右分栏 -->
      <a-row :gutter="[16, 16]" class="main-section">
        <!-- 左侧：待办事项 (55%) -->
        <a-col :xs="24" :lg="13">
          <a-card title="📋 待办事项" :bordered="false" class="pending-card">
            <template #extra>
              <a-link @click="$router.push('/ops/review')">查看全部 →</a-link>
            </template>

            <!-- 商品审核 -->
            <div v-if="counts.items > 0" class="pending-group">
              <div class="group-header">
                <span class="group-title">🔴 商品审核</span>
                <a-badge :count="counts.items" :max-count="99" />
              </div>
              <div 
                v-for="item in pendingItemsByType.item.slice(0, 2)" 
                :key="item.id"
                class="pending-item"
              >
                <div class="item-info">
                  <div class="item-title">{{ item.title || '未命名商品' }}</div>
                  <div class="item-meta">{{ item.author || '未知卖家' }} · {{ formatTimeAgo(item.time) }}</div>
                </div>
                <a-button type="primary" size="small" @click="goToReview(item)">
                  审核
                </a-button>
              </div>
              <a-link v-if="counts.items > 2" class="more-link" @click="$router.push('/ops/items')">
                还有 {{ counts.items - 2 }} 条 →
              </a-link>
            </div>

            <!-- 评价审核 -->
            <div v-if="counts.reviews > 0" class="pending-group">
              <div class="group-header">
                <span class="group-title">⭐ 评价审核</span>
                <a-badge :count="counts.reviews" :max-count="99" />
              </div>
              <div 
                v-for="item in pendingItemsByType.review.slice(0, 2)" 
                :key="item.id"
                class="pending-item"
              >
                <div class="item-info">
                  <div class="item-title">{{ truncate(item.content || item.title, 30) }}</div>
                  <div class="item-meta">{{ item.author || '未知买家' }} · {{ formatTimeAgo(item.time) }}</div>
                </div>
                <a-space>
                  <a-button type="primary" size="small" status="success" @click="approveItem(item)">
                    通过
                  </a-button>
                </a-space>
              </div>
              <a-link v-if="counts.reviews > 2" class="more-link" @click="$router.push('/ops/review-manage')">
                还有 {{ counts.reviews - 2 }} 条 →
              </a-link>
            </div>

            <!-- 帖子审核 -->
            <div v-if="counts.posts > 0" class="pending-group">
              <div class="group-header">
                <span class="group-title">💬 帖子审核</span>
                <a-badge :count="counts.posts" :max-count="99" />
              </div>
              <div 
                v-for="item in pendingItemsByType.post.slice(0, 2)" 
                :key="item.id"
                class="pending-item"
              >
                <div class="item-info">
                  <div class="item-title">{{ item.title || '未命名帖子' }}</div>
                  <div class="item-meta">{{ item.author || '未知用户' }} · {{ formatTimeAgo(item.time) }}</div>
                </div>
                <a-button type="primary" size="small" @click="goToReview(item)">
                  审核
                </a-button>
              </div>
              <a-link v-if="counts.posts > 2" class="more-link" @click="$router.push('/ops/circle')">
                还有 {{ counts.posts - 2 }} 条 →
              </a-link>
            </div>

            <a-empty v-if="counts.total === 0" description="暂无待办事项 🎉" />
          </a-card>
        </a-col>

        <!-- 右侧：最新动态 (45%) -->
        <a-col :xs="24" :lg="11">
          <a-card title="📈 最新动态" :bordered="false" class="activity-card">
            <template #extra>
              <a-button type="text" size="small" @click="refreshActivities">
                <template #icon><icon-refresh /></template>
                刷新
              </a-button>
            </template>

            <a-timeline v-if="activities.length > 0" pending>
              <a-timeline-item
                v-for="activity in activities.slice(0, 8)"
                :key="activity.id"
                :dot-color="activity.dotColor || '#86909C'"
              >
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title || '系统活动' }}</div>
                  <div v-if="activity.description" class="activity-desc">
                    {{ activity.description }}
                  </div>
                  <div class="activity-time">{{ formatTimeAgo(activity.time) }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>

            <a-empty v-else description="暂无动态" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconUserGroup,
  IconStorage,
  IconTag,
  IconClockCircle,
  IconRefresh,
} from '@arco-design/web-vue/es/icon'
import { opsHttp as http } from '../../../services/http'

const router = useRouter()
const loading = ref(false)
const stats = ref({})
const pendingItems = ref([])
const activities = ref([])
const counts = reactive({
  items: 0,
  reviews: 0,
  posts: 0,
  total: 0
})

let refreshTimer = null

// KPI卡片配置
const kpiCards = [
  {
    key: 'totalUsers',
    label: '总用户数',
    icon: IconUserGroup,
    color: '#165DFF',
    trendKey: 'newUsersToday'
  },
  {
    key: 'totalItems',
    label: '总商品数',
    icon: IconStorage,
    color: '#00B42A',
    trendKey: 'newItemsToday'
  },
  {
    key: 'todayOrders',
    label: '今日订单',
    icon: IconTag,
    color: '#FF7D00',
    suffix: '',
    trendKey: 'orderGrowth'
  },
  {
    key: 'pendingTotal',
    label: '待审核数',
    icon: IconClockCircle,
    color: '#F53F3F'
  }
]

// 按类型分组的待办项
const pendingItemsByType = computed(() => {
  return {
    item: pendingItems.value.filter(item => item.type === 'item'),
    review: pendingItems.value.filter(item => item.type === 'review'),
    post: pendingItems.value.filter(item => item.type === 'post')
  }
})

// 格式化数字（超过1万显示为x.x万）
function formatNumber(num) {
  if (!num && num !== 0) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 截断文本
function truncate(text, len = 20) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

// 格式化时间为相对时间
function formatTimeAgo(time) {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  
  // 超过7天显示具体日期
  return date.toLocaleDateString('zh-CN', { 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载所有数据
async function loadDashboardData() {
  loading.value = true
  
  try {
    const [statsRes, pendingRes, activitiesRes] = await Promise.allSettled([
      http.get('/ops/stats/brief'),
      http.get('/ops/pending-items?limit=6'),
      http.get('/ops/recent-activities?limit=10')
    ])
    
    console.log('[Dashboard] API响应:', { statsRes, pendingRes, activitiesRes })
    
    // 处理统计数据
    if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
      const data = statsRes.value.data
      stats.value = {
        totalUsers: data.totalUsers || 0,
        newUsersToday: data.newUsersToday || 0,
        totalItems: data.totalItems || 0,
        newItemsToday: data.newItemsToday || 0,
        todayOrders: data.todayOrders || data.todayOrderCount || 0,
        todayRevenue: data.todayRevenue || 0,
        orderGrowth: data.orderGrowth || 0,
        pendingTotal: data.pendingTotal || 0 ||
          (data.pendingItems || 0) + (data.pendingReviews || 0) + (data.pendingPosts || 0),
        pendingItems: data.pendingItems || 0,
        pendingReviews: data.pendingReviews || 0,
        pendingPosts: data.pendingPosts || 0
      }
      
      // 更新待办数量
      counts.items = data.pendingItems || 0
      counts.reviews = data.pendingReviews || 0
      counts.posts = data.pendingPosts || 0
      counts.total = (data.pendingItems || 0) + (data.pendingReviews || 0) + (data.pendingPosts || 0)
      
      console.log('[Dashboard] 统计数据:', stats.value)
    } else {
      console.warn('[Dashboard] 统计API失败或无数据')
      // 设置默认值避免页面空白
      stats.value = {
        totalUsers: 0,
        totalItems: 0,
        todayOrders: 0,
        pendingTotal: 0
      }
    }
    
    // 处理待办事项
    if (pendingRes.status === 'fulfilled' && pendingRes.value?.data) {
      const data = pendingRes.value.data
      
      // 支持多种数据格式
      pendingItems.value = data.items || data.list || data.reviews || []
      
      // 如果返回的是counts，更新计数
      if (data.counts) {
        counts.items = data.counts.items || counts.items
        counts.reviews = data.counts.reviews || counts.reviews
        counts.posts = data.counts.posts || counts.posts
        counts.total = data.counts.total || counts.total
      }
      
      console.log('[Dashboard] 待办事项:', pendingItems.value.length, '条')
    } else {
      console.warn('[Dashboard] 待办API失败，使用空数组')
      pendingItems.value = []
      
      // 如果没有真实数据，使用统计数据的count生成占位符
      if (stats.value.pendingItems > 0) {
        for (let i = 0; i < Math.min(stats.value.pendingItems, 2); i++) {
          pendingItems.value.push({
            id: `mock_item_${i}`,
            type: 'item',
            title: `商品待审核 #${i + 1}`,
            author: '卖家',
            time: new Date().toISOString(),
            status: 'PENDING'
          })
        }
      }
      
      if (stats.value.pendingReviews > 0) {
        for (let i = 0; i < Math.min(stats.value.pendingReviews, 2); i++) {
          pendingItems.value.push({
            id: `mock_review_${i}`,
            type: 'review',
            content: '用户评价内容...',
            author: '买家',
            time: new Date(Date.now() - 3600000 * (i + 1)),
            status: 'PENDING'
          })
        }
      }
    }
    
    // 处理最新动态
    if (activitiesRes.status === 'fulfilled' && activitiesRes.value?.data) {
      const data = activitiesRes.value.data
      
      activities.value = (data.activities || data.list || []).map(item => ({
        ...item,
        dotColor: getDotColor(item.type)
      }))
      
      console.log('[Dashboard] 动态数据:', activities.value.length, '条')
    } else {
      console.warn('[Dashboard] 动态API失败')
      activities.value = []
    }
    
  } catch (error) {
    console.error('[Dashboard] 加载失败:', error)
    Message.error('数据加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

// 根据类型获取圆点颜色
function getDotColor(type) {
  const colors = {
    order: '#00B42A',       // 新订单 - 绿色
    user: '#165DFF',        // 用户注册 - 蓝色
    completed: '#FF7D00',   // 订单完成 - 橙色
    item: '#722ED1',        // 新商品 - 紫色
    review: '#FF7D00',      // 评价提交 - 金色
    post: '#14C9C9'         // 帖子发布 - 青色
  }
  return colors[type] || '#86909C'
}

// 刷新动态
async function refreshActivities() {
  Message.loading({ content: '刷新中...', duration: 500 })
  
  try {
    const res = await http.get('/ops/recent-activities?limit=10')
    
    if (res?.data?.activities) {
      activities.value = res.data.activities.map(item => ({
        ...item,
        dotColor: getDotColor(item.type)
      }))
      Message.success('已刷新')
    }
  } catch (e) {
    console.error('[Dashboard] 刷新失败:', e)
    Message.error('刷新失败')
  }
}

// 跳转到审核页面
function goToReview(item) {
  if (item.type === 'item' || item.type === 'post') {
    router.push('/ops/items')
  } else if (item.type === 'review') {
    router.push('/ops/review-manage')
  } else {
    router.push('/ops/review')
  }
}

// 快速通过评价
async function approveItem(item) {
  try {
    await http.post(`/ops/reviews/${item.id}/approve`)
    Message.success('已通过')
    loadDashboardData() // 刷新数据
  } catch (e) {
    console.error('[Dashboard] 审核失败:', e)
    Message.error('操作失败')
  }
}

onMounted(() => {
  loadDashboardData()
  
  // 每5分钟自动刷新
  refreshTimer = setInterval(loadDashboardData, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.ops-dashboard {
  padding: 0;
  min-height: calc(100vh - 64px);
  background: #F7F8FA;
}

// ========== KPI卡片区域 ==========
.kpi-row {
  margin-bottom: 16px;
}

.kpi-card {
  position: relative;
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid var(--accent-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
  cursor: default;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  .kpi-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: var(--accent-color);
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--accent-color) 10%, transparent) 0%,
      color-mix(in srgb, var(--accent-color) 5%, transparent) 100%
    );
    flex-shrink: 0;
  }

  .kpi-content {
    flex: 1;
    min-width: 0;

    .kpi-value {
      font-size: 28px;
      font-weight: 700;
      color: #1D2129;
      line-height: 1.2;

      .kpi-suffix {
        font-size: 14px;
        font-weight: 400;
        color: #86909C;
        margin-left: 4px;
      }
    }

    .kpi-label {
      font-size: 14px;
      color: #86909C;
      margin-top: 2px;
      font-weight: 400;
    }

    .kpi-trend {
      display: inline-flex;
      align-items: center;
      gap: 2px;
      font-size: 12px;
      margin-top: 4px;
      padding: 2px 8px;
      border-radius: 4px;

      &.trend-up {
        color: #00B42A;
        background: #E8FFEA;
      }

      &.trend-down {
        color: #F53F3F;
        background: #FFECE8;
      }

      .trend-label {
        opacity: 0.8;
        margin-left: 2px;
      }
    }
  }
}

// ========== 主内容区域 ==========
.main-section {
  margin-bottom: 16px;
}

// ========== 待办事项卡片 ==========
.pending-card {
  border-radius: 8px;
  height: 100%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-header) {
    border-bottom: 1px solid #E5E6EB;
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 16px 20px;
  }

  .pending-group {
    &:not(:last-child) {
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px dashed #E5E6EB;
    }

    .group-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .group-title {
        font-size: 14px;
        font-weight: 600;
        color: #1D2129;
      }
    }

    .pending-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 10px 12px;
      border-radius: 6px;
      background: #FAFBFC;
      transition: all 0.15s ease;

      &:hover {
        background: #F2F3F5;
      }

      &:not(:last-child) {
        margin-bottom: 8px;
      }

      .item-info {
        flex: 1;
        min-width: 0;

        .item-title {
          font-size: 13px;
          font-weight: 500;
          color: #1D2129;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-bottom: 2px;
        }

        .item-meta {
          font-size: 12px;
          color: #86909C;
        }
      }
    }

    .more-link {
      display: block;
      text-align: center;
      font-size: 12px;
      color: #165DFF;
      margin-top: 8px;
      padding: 4px 0;
      
      &:hover {
        color: #4080FF;
      }
    }
  }
}

// ========== 最新动态卡片 ==========
.activity-card {
  border-radius: 8px;
  height: 100%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-header) {
    border-bottom: 1px solid #E5E6EB;
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 16px 20px;
    max-height: 500px;
    overflow-y: auto;
  }

  .activity-content {
    .activity-title {
      font-size: 14px;
      font-weight: 500;
      color: #1D2129;
      margin-bottom: 2px;
    }

    .activity-desc {
      font-size: 13px;
      color: #4E5969;
      margin-bottom: 4px;
      line-height: 1.4;
    }

    .activity-time {
      font-size: 12px;
      color: #C9CDD4;
    }
  }
}

// ========== 响应式适配 ==========
@media screen and (max-width: 991px) {
  .kpi-row {
    .a-col {
      width: 50% !important;
      margin-bottom: 12px;
    }

    .kpi-card {
      padding: 16px;

      .kpi-icon {
        width: 40px;
        height: 40px;
        font-size: 20px;
      }

      .kpi-content .kpi-value {
        font-size: 24px;
      }
    }
  }
}

@media screen and (max-width: 767px) {
  .ops-dashboard {
    .kpi-row {
      margin-bottom: 12px;
    }

    .main-section {
      .a-col {
        width: 100% !important;
        margin-bottom: 16px;
      }
    }

    .pending-card,
    .activity-card {
      :deep(.arco-card-body) {
        max-height: none;
      }
    }
  }
}
</style>