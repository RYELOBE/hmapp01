<template>
  <div class="ops-dashboard">
    <a-spin :loading="loading" style="width: 100%">
      <div class="kpi-section">
        <div class="kpi-grid">
          <div v-for="(card, index) in kpiCards" :key="index" class="kpi-card" :style="{ '--accent-color': card.color, '--card-index': index }">
            <div class="kpi-background">
              <div class="bg-circle bg-circle-1"></div>
              <div class="bg-circle bg-circle-2"></div>
            </div>
            <div class="kpi-content">
              <div class="kpi-icon-wrapper">
                <component :is="card.icon" class="kpi-icon" />
              </div>
              <div class="kpi-info">
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
                  <icon-arrow-rise v-if="stats[card.trendKey] > 0" />
                  <icon-arrow-fall v-else />
                  {{ Math.abs(stats[card.trendKey] || 0) }}
                  <span class="trend-label">今日</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <a-row :gutter="[24, 24]" class="main-section">
        <a-col :xs="24" :lg="14">
          <div class="pending-card-wrapper">
            <div class="card-header-custom">
              <div class="header-left">
                <div class="header-icon"></div>
                <h3 class="header-title">待办事项</h3>
                <a-badge :count="counts.total" :max-count="99" class="header-badge" />
              </div>
              <a-link class="header-link" @click="$router.push('/ops/reviews/manage')">
                查看全部
                <icon-right />
              </a-link>
            </div>

            <div class="pending-content">
              <div v-if="counts.items > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">📦</span>
                  <span class="group-title">商品审核</span>
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
                  <a-button type="primary" size="small" class="action-btn" @click="goToReview(item)">
                    审核
                  </a-button>
                </div>
                <a-link v-if="counts.items > 2" class="more-link" @click="$router.push('/ops/items/review')">
                  还有 {{ counts.items - 2 }} 条 →
                </a-link>
              </div>

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
                >
                  <div class="item-info">
                    <div class="item-title">{{ truncate(item.content || item.title || item.itemTitle, 30) }}</div>
                    <div class="item-meta">{{ item.author || item.buyerName || '未知买家' }} · {{ formatTimeAgo(item.time) }}</div>
                  </div>
                  <a-space>
                    <a-button type="primary" size="small" status="success" class="action-btn" @click="approveItem(item)">
                      通过
                    </a-button>
                    <a-button type="primary" size="small" status="danger" class="action-btn" @click="rejectReview(item)">
                      拒绝
                    </a-button>
                  </a-space>
                </div>
                <a-link v-if="counts.reviews > 2" class="more-link" @click="$router.push('/ops/reviews/audit')">
                  还有 {{ counts.reviews - 2 }} 条 →
                </a-link>
              </div>

              <div v-if="counts.circle > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">💬</span>
                  <span class="group-title">圈子审核</span>
                  <a-badge :count="counts.circle" :max-count="99" />
                </div>
                <div
                  v-for="item in pendingItemsByType.circle.slice(0, 2)"
                  :key="item.id"
                  class="pending-item"
                >
                  <div class="item-info">
                    <div class="item-title">{{ item.title || '未命名帖子' }}</div>
                    <div class="item-meta">{{ item.userName || item.authorName || '未知用户' }} · {{ formatTimeAgo(item.time) }}</div>
                  </div>
                  <a-button type="primary" size="small" class="action-btn" @click="goToReview(item)">
                    审核
                  </a-button>
                </div>
                <a-link v-if="counts.circle > 2" class="more-link" @click="$router.push('/ops/circles/review')">
                  还有 {{ counts.circle - 2 }} 条 →
                </a-link>
              </div>

              <div v-if="counts.orders > 0" class="pending-group">
                <div class="group-header">
                  <span class="group-icon">🔄</span>
                  <span class="group-title">退款订单</span>
                  <a-badge :count="counts.orders" :max-count="99" />
                </div>
                <div
                  v-for="item in pendingItemsByType.order.slice(0, 2)"
                  :key="item.id"
                  class="pending-item"
                >
                  <div class="item-info">
                    <div class="item-title">{{ item.title || item.orderNo || '退款订单' }}</div>
                    <div class="item-meta">{{ item.buyerName || '买家' }} · ¥{{ formatPrice(item.totalAmount || item.amount) }} · {{ formatTimeAgo(item.time) }}</div>
                  </div>
                  <a-button type="primary" size="small" class="action-btn" @click="goToOrderReview(item)">
                    处理
                  </a-button>
                </div>
                <a-link v-if="counts.orders > 2" class="more-link" @click="$router.push('/ops/orders/review')">
                  还有 {{ counts.orders - 2 }} 条 →
                </a-link>
              </div>

              <div v-if="counts.total === 0" class="empty-state-custom">
                <div class="empty-icon"></div>
                <p class="empty-text">暂无待办事项</p>
              </div>
            </div>
          </div>
        </a-col>

        <a-col :xs="24" :lg="10">
          <div class="activity-card-wrapper">
            <div class="card-header-custom">
              <div class="header-left">
                <div class="header-icon"></div>
                <h3 class="header-title">最新消息</h3>
              </div>
              <a-button type="text" size="small" class="refresh-btn" @click="refreshActivities">
                <template #icon><icon-refresh /></template>
                刷新
              </a-button>
            </div>

            <div class="activity-content-custom">
              <a-timeline v-if="sortedActivities.length > 0" pending class="custom-timeline">
                <a-timeline-item
                  v-for="activity in sortedActivities.slice(0, 5)"
                  :key="activity.id"
                  :dot-color="activity.dotColor || activity.color || '#86909C'"
                  class="timeline-item-custom"
                >
                  <div class="activity-item">
                    <div class="activity-title">{{ activity.title || '系统活动' }}</div>
                    <div v-if="activity.description" class="activity-desc">
                      {{ activity.description }}
                    </div>
                    <div class="activity-time">{{ formatTimeAgo(activity.time) }}</div>
                  </div>
                </a-timeline-item>
              </a-timeline>

              <div v-else class="empty-state-custom">
                <div class="empty-icon">📭</div>
                <p class="empty-text">暂无消息</p>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>


    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconUserGroup,
  IconStorage,
  IconTag,
  IconClockCircle,
  IconRefresh,
  IconArrowRise,
  IconArrowFall,
  IconRight,
} from '@arco-design/web-vue/es/icon'
import { opsHttp as http } from '../../../services/http'
import { getNotifications } from '../../../services/notifications'

defineOptions({ name: 'OpsDashboard' })

const router = useRouter()
const loading = ref(false)
const stats = ref({})
const pendingItems = ref([])
const activities = ref([])
const counts = reactive({
  items: 0,
  reviews: 0,
  circle: 0,
  orders: 0,
  total: 0
})

let refreshTimer = null

const kpiCards = [
  { key: 'totalUsers', label: '总用户数', icon: IconUserGroup, color: '#165DFF', trendKey: 'newUsersToday' },
  { key: 'totalItems', label: '总商品数', icon: IconStorage, color: '#00B42A', trendKey: 'newItemsToday' },
  { key: 'todayOrders', label: '今日订单', icon: IconTag, color: '#FF7D00', suffix: '', trendKey: 'orderGrowth' },
  { key: 'pendingTotal', label: '待审核数', icon: IconClockCircle, color: '#F53F3F' },
]

const pendingItemsByType = computed(() => ({
  item: pendingItems.value.filter(item => item.type === 'item'),
  review: pendingItems.value.filter(item => item.type === 'review'),
  circle: pendingItems.value.filter(item => item.type === 'circle'),
  order: pendingItems.value.filter(item => item.type === 'order'),
}))

const sortedActivities = computed(() => {
  return [...activities.value].sort((a, b) => {
    const timeA = new Date(a.time || a.createTime || a.createdAt || 0).getTime()
    const timeB = new Date(b.time || b.createTime || b.createdAt || 0).getTime()
    return timeB - timeA
  })
})

function formatNumber(num) {
  if (!num && num !== 0) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

function formatPrice(num) {
  if (!num) return '0.00'
  return Number(num).toFixed(2)
}

function truncate(text, len = 20) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

function formatTimeAgo(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function unwrapApiRes(res) {
  return res?.data?.data ?? res?.data ?? res
}

async function loadDashboardData() {
  loading.value = true
  try {
    const [statsRes, countsRes] = await Promise.allSettled([
      http.get('/ops/stats/brief'),
      http.get('/ops/pending-counts'),
    ])

    if (statsRes.status === 'fulfilled' && statsRes.value) {
      const data = unwrapApiRes(statsRes.value)
      const statistics = data?.statistics || data || {}
      stats.value = {
        totalUsers: statistics.totalUsers || 0,
        newUsersToday: statistics.todayUsers || 0,
        totalItems: statistics.totalItems || 0,
        newItemsToday: statistics.todayItems || 0,
        todayOrders: statistics.todayOrders || 0,
        todayRevenue: statistics.todayTotalAmount || 0,
        orderGrowth: 0,
        pendingTotal: 0,
      }
      console.log('[Dashboard] 统计数据:', stats.value)
    }

    if (countsRes.status === 'fulfilled' && countsRes.value) {
      const data = unwrapApiRes(countsRes.value)
      counts.items = data?.items || 0
      counts.reviews = data?.reviews || 0
      counts.circle = data?.circle || 0
      counts.orders = data?.orders || 0
      counts.total = counts.items + counts.reviews + counts.circle + counts.orders
      console.log('[Dashboard] 待审核计数:', counts)
    }

    stats.value.pendingTotal = counts.total

    if (counts.total > 0) {
      await loadPendingItems()
    } else {
      pendingItems.value = []
    }

    await loadActivities()

  } catch (error) {
    console.error('[Dashboard] 加载失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadActivities() {
  try {
    console.log('[Dashboard] 开始加载消息...')
    const response = await getNotifications({ page: 1, size: 10 })
    console.log('[Dashboard] API响应:', JSON.stringify(response, null, 2))

    const data = response?.data?.data ?? response?.data ?? response
    console.log('[Dashboard] 解析data:', JSON.stringify(data, null, 2))
    const list = data?.records || data?.list || (Array.isArray(data) ? data : [])
    console.log('[Dashboard] 解析列表:', list.length, '条')

    activities.value = list.slice(0, 5).map(msg => ({
      id: msg.id,
      title: msg.title || msg.content || '系统消息',
      description: msg.content || '',
      dotColor: getNotificationDotColor(msg.type),
      time: msg.createdAt || msg.created_at || new Date().toISOString(),
    }))
    console.log('[Dashboard] 消息数据:', activities.value.length, '条')
  } catch (e) {
    console.error('[Dashboard] 加载消息失败:', e)
    activities.value = []
  }
}

async function loadPendingItems() {
  const items = []
  try {
    if (counts.items > 0) {
      const res = await http.post('/ops/pending-items', { pageNo: 1, pageSize: Math.min(counts.items, 5) })
      const data = unwrapApiRes(res)
      const list = data?.items || data?.rows || []
      list.forEach((item, idx) => {
        items.push({
          id: item.id || `item-${idx}`,
          type: 'item',
          title: item.title || '商品待审核',
          author: item.sellerName || '卖家',
          time: item.createdAt || new Date().toISOString(),
        })
      })
    }
    if (counts.reviews > 0) {
      const res = await http.post('/reviews/pending', { pageNo: 1, pageSize: Math.min(counts.reviews, 5) })
      const data = unwrapApiRes(res)
      const list = data?.items || data?.records || data?.list || []
      list.forEach((review, idx) => {
        items.push({
          id: review.id || `review-${idx}`,
          type: 'review',
          title: review.itemTitle || '评价待审核',
          content: review.content || '',
          author: review.buyerName || review.userName || '买家',
          time: review.createdAt || new Date().toISOString(),
        })
      })
    }
    if (counts.circle > 0) {
      const res = await http.post('/ops/circle/pending', { pageNo: 1, pageSize: Math.min(counts.circle, 5) })
      const data = unwrapApiRes(res)
      const list = data?.items || data?.posts || data?.list || []
      list.forEach((post, idx) => {
        items.push({
          id: post.id || `post-${idx}`,
          type: 'circle',
          title: post.title || '帖子待审核',
          author: post.authorName || post.userName || '用户',
          userName: post.userName || post.authorName || '',
          time: post.createdAt || new Date().toISOString(),
        })
      })
    }
    if (counts.orders > 0) {
      const res = await http.post('/ops/orders', { status: 'REFUNDING', pageNo: 1, pageSize: Math.min(counts.orders, 5) })
      const data = unwrapApiRes(res)
      const list = data?.orders || data?.rows || []
      list.forEach((order, idx) => {
        items.push({
          id: order.id || `order-${idx}`,
          type: 'order',
          title: order.orderNo || `退款订单`,
          orderNo: order.orderNo || '',
          author: order.buyerName || '买家',
          buyerName: order.buyerName || '',
          totalAmount: order.totalAmount || 0,
          time: order.createdAt || new Date().toISOString(),
        })
      })
    }
  } catch (e) {
    console.error('[Dashboard] 加载待审核项失败:', e)
  }
  items.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
  pendingItems.value = items
  console.log('[Dashboard] 待审核项总数:', pendingItems.value.length)
}

function getNotificationDotColor(type) {
  const colorMap = {
    ITEM: '#00B42A',
    REVIEW: '#FF7D00',
    ORDER: '#165DFF',
    USER: '#722ED1',
    SYSTEM: '#86909C',
    BUSINESS: '#14C9C9',
  }
  return colorMap[type] || '#86909C'
}

async function refreshActivities() {
  Message.loading({ content: '刷新中...', duration: 500 })
  try {
    const res = await getNotifications({ page: 1, size: 10 })
    const data = res?.data?.data ?? res?.data ?? res
    const list = data?.records || data?.list || data?.notifications || data?.items || (Array.isArray(data) ? data : [])
    activities.value = list.slice(0, 5).map(msg => ({
      id: msg.id,
      title: msg.title || msg.content || '系统消息',
      description: msg.content || '',
      dotColor: getNotificationDotColor(msg.type),
      time: msg.createdAt || msg.created_at || new Date().toISOString(),
    }))
    Message.success('已刷新')
  } catch (e) {
    Message.error('刷新失败')
  }
}

function goToReview(item) {
  if (item.type === 'item') router.push('/ops/items/review')
  else if (item.type === 'review') router.push('/ops/reviews/audit')
  else if (item.type === 'circle') router.push('/ops/circles/review')
  else router.push('/ops/reviews/manage')
}

function goToOrderReview() {
  router.push('/ops/orders/review')
}

async function approveItem(item) {
  try {
    await http.post(`/ops/reviews/${item.id}/approve`)
    Message.success('已通过')
    loadDashboardData()
  } catch (e) {
    Message.error('操作失败')
  }
}

async function rejectReview(item) {
  try {
    await http.post(`/ops/reviews/${item.id}/reject`, { reason: '不符合规范' })
    Message.success('已拒绝')
    loadDashboardData()
  } catch (e) {
    Message.error('操作失败')
  }
}

onMounted(() => {
  loadDashboardData()
  refreshTimer = setInterval(loadDashboardData, 5 * 60 * 1000)
})

onActivated(() => {
  loadDashboardData()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style lang="scss" scoped>
.ops-dashboard {
  padding: 0;
  min-height: calc(100vh - 48px);
  background: linear-gradient(135deg, #F5F7FA 0%, #E8ECF1 100%);
}

.kpi-section {
  margin-bottom: 24px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.kpi-card {
  position: relative;
  background: #FFFFFF;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
    .kpi-icon-wrapper { transform: scale(1.1) rotate(5deg); }
    .bg-circle-1 { transform: scale(1.2) translate(-10px, -10px); }
    .bg-circle-2 { transform: scale(1.3) translate(10px, 10px); }
  }

  .kpi-background {
    position: absolute;
    top: 0;
    right: 0;
    width: 150px;
    height: 150px;
    opacity: 0.08;
    pointer-events: none;
    .bg-circle {
      position: absolute;
      border-radius: 50%;
      background: var(--accent-color);
      transition: all 0.4s ease;
    }
    .bg-circle-1 { width: 120px; height: 120px; top: -40px; right: -30px; }
    .bg-circle-2 { width: 80px; height: 80px; bottom: -20px; right: 20px; }
  }

  .kpi-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 18px;
  }

  .kpi-icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #FFFFFF;
    background: linear-gradient(135deg, var(--accent-color) 0%, color-mix(in srgb, var(--accent-color) 85%, white) 100%);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    .kpi-icon { filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2)); }
  }

  .kpi-info {
    flex: 1;
    min-width: 0;
    .kpi-value {
      font-size: 32px;
      font-weight: 800;
      color: #1D2129;
      line-height: 1.2;
      letter-spacing: -0.02em;
      .kpi-suffix { font-size: 14px; font-weight: 500; color: #86909C; margin-left: 4px; }
    }
    .kpi-label { font-size: 14px; color: #86909C; margin-top: 4px; font-weight: 500; }
    .kpi-trend {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      margin-top: 8px;
      padding: 4px 10px;
      border-radius: 8px;
      font-weight: 600;
      &.trend-up { color: #00B42A; background: linear-gradient(135deg, #E8FFEA 0%, #F0FFF0 100%); border: 1px solid rgba(0, 180, 42, 0.2); }
      &.trend-down { color: #F53F3F; background: linear-gradient(135deg, #FFECE8 0%, #FFF0F0 100%); border: 1px solid rgba(245, 63, 63, 0.2); }
      .trend-label { opacity: 0.85; margin-left: 2px; font-weight: 500; }
    }
  }
}

.main-section {
  margin-bottom: 24px;
}

.pending-card-wrapper,
.activity-card-wrapper {
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  height: 100%;
  transition: all 0.3s ease;
  &:hover { box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1); }
}

.card-header-custom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 2px solid #F2F3F5;
  background: linear-gradient(135deg, #FAFBFC 0%, #FFFFFF 100%);
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    .header-icon { font-size: 24px; line-height: 1; }
    .header-title { margin: 0; font-size: 18px; font-weight: 700; color: #1D2129; }
    .header-badge :deep(.arco-badge-number) { background: #F53F3F; box-shadow: 0 2px 8px rgba(245, 63, 63, 0.3); }
  }
  .header-link {
    font-size: 14px; font-weight: 500; color: #165DFF;
    display: flex; align-items: center; gap: 4px;
    transition: all 0.2s ease;
    &:hover { color: #4080FF; gap: 6px; }
  }
  .refresh-btn { color: #86909C; transition: all 0.2s ease; &:hover { color: #165DFF; background: rgba(22, 93, 255, 0.06); } }
}

.pending-content {
  padding: 20px 24px;
  max-height: 520px;
  overflow-y: auto;
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #E5E6EB; border-radius: 3px; &:hover { background: #C9CDD4; } }
}

.pending-group {
  &:not(:last-child) { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 2px dashed #E5E6EB; }
  .group-header { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; .group-icon { font-size: 18px; } .group-title { font-size: 15px; font-weight: 700; color: #1D2129; } }
  .pending-item {
    display: flex; align-items: center; justify-content: space-between; gap: 14px;
    padding: 14px 16px; border-radius: 12px;
    background: linear-gradient(135deg, #FAFBFC 0%, #F7F8FA 100%);
    border: 1px solid transparent; transition: all 0.25s ease; margin-bottom: 10px;
    &:hover { background: linear-gradient(135deg, #F0F5FF 0%, #E8F3FE 100%); border-color: rgba(22, 93, 255, 0.15); transform: translateX(4px); box-shadow: 0 4px 12px rgba(22, 93, 255, 0.08); }
    .item-info { flex: 1; min-width: 0; .item-title { font-size: 14px; font-weight: 600; color: #1D2129; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; line-height: 1.4; } .item-meta { font-size: 13px; color: #86909C; font-weight: 400; } }
    .action-btn { font-weight: 600; border-radius: 8px; padding: 6px 18px; flex-shrink: 0; transition: all 0.25s ease; &:hover { transform: scale(1.05); } }
  }
  .more-link { display: block; text-align: center; font-size: 13px; color: #165DFF; margin-top: 10px; padding: 8px 0; font-weight: 600; border-radius: 8px; transition: all 0.2s ease; &:hover { color: #4080FF; background: rgba(22, 93, 255, 0.04); } }
}

.activity-content-custom {
  padding: 20px 24px; max-height: 520px; overflow-y: auto;
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #E5E6EB; border-radius: 3px; &:hover { background: #C9CDD4; } }
}

.custom-timeline {
  :deep(.arco-timeline-item-content) { padding-left: 8px; }
  .timeline-item-custom { transition: all 0.25s ease; &:hover { .activity-item { transform: translateX(4px); } } }
  .activity-item {
    background: #FAFBFC; padding: 14px 16px; border-radius: 10px; border: 1px solid #F0F1F3; transition: all 0.25s ease;
    .activity-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 4px; line-height: 1.5; }
    .activity-desc { font-size: 13px; color: #4E5969; line-height: 1.6; margin-bottom: 6px; }
    .activity-time { font-size: 12px; color: #C9CDD4; font-weight: 500; }
  }
}

.empty-state-custom { text-align: center; padding: 60px 20px; .empty-icon { font-size: 64px; margin-bottom: 16px; animation: float 3s ease-in-out infinite; } .empty-text { font-size: 15px; color: #86909C; font-weight: 500; margin: 0; } }

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-12px); } }

@media screen and (max-width: 1199px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .kpi-card { padding: 20px; .kpi-icon-wrapper { width: 52px; height: 52px; font-size: 24px; } .kpi-info .kpi-value { font-size: 28px; } }
}

@media screen and (max-width: 767px) {
  .kpi-grid { grid-template-columns: 1fr; gap: 14px; }
  .main-section .a-col { width: 100% !important; margin-bottom: 20px; }
  .pending-content, .activity-content-custom { max-height: none; padding: 16px; }
  .card-header-custom { padding: 16px 18px; .header-title { font-size: 16px; } }
  .kpi-card { padding: 18px; .kpi-background { display: none; } .kpi-info .kpi-value { font-size: 26px; } }
  .pending-group .pending-item { flex-direction: column; align-items: stretch; gap: 12px; .action-btn { width: 100%; } }
}
</style>