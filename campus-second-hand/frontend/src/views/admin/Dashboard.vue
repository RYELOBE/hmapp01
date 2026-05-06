<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>控制台</h2>
      <span class="date">{{ currentDate }}</span>
    </div>

    <a-row :gutter="20" class="stat-cards">
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);">
            <icon-user />
          </div>
          <div class="stat-content">
            <div class="stat-label">用户总数</div>
            <div class="stat-value">{{ statistics.totalUsers }}</div>
            <div class="stat-trend">
              <icon-rise />
              <span class="trend-value positive">+{{ statistics.userGrowth }}%</span>
              <span class="trend-label">较上周</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #00d084 0%, #00b377 100%);">
            <icon-app />
          </div>
          <div class="stat-content">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ statistics.totalProducts }}</div>
            <div class="stat-trend">
              <icon-rise />
              <span class="trend-value positive">+{{ statistics.productGrowth }}%</span>
              <span class="trend-label">较上周</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ff7d00 0%, #ff9d2e 100%);">
            <icon-file />
          </div>
          <div class="stat-content">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ statistics.totalOrders }}</div>
            <div class="stat-trend">
              <icon-rise />
              <span class="trend-value positive">+{{ statistics.orderGrowth }}%</span>
              <span class="trend-label">较上周</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);">
            <icon-user-add />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日新增用户</div>
            <div class="stat-value">{{ statistics.todayNewUsers }}</div>
            <div class="stat-trend">
              <icon-rise />
              <span class="trend-value positive">+{{ statistics.todayNewUserGrowth }}%</span>
              <span class="trend-label">较昨日</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="20" class="charts-section">
      <a-col :span="14">
        <a-card class="chart-card">
          <div class="card-header">
            <h3>用户增长趋势</h3>
            <a-radio-group type="button" v-model="userChartPeriod" @change="updateUserChart">
              <a-radio value="week">近7天</a-radio>
              <a-radio value="month">近30天</a-radio>
            </a-radio-group>
          </div>
          <div ref="userChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :span="10">
        <a-card class="chart-card">
          <div class="card-header">
            <h3>商品分类占比</h3>
          </div>
          <div ref="categoryChartRef" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="20" class="tables-section">
      <a-col :span="12">
        <a-card class="table-card">
          <template #title>
            <div class="card-title">
              <span>待审核商品</span>
              <a-link @click="goToProducts">查看更多</a-link>
            </div>
          </template>
          <a-table :columns="pendingProductColumns" :data="pendingProducts" :pagination="false" size="small">
            <template #status="{ record }">
              <a-tag :color="record.status === '待审核' ? 'orange' : 'green'">{{ record.status }}</a-tag>
            </template>
            <template #actions="{ record }">
              <a-button type="text" size="small" @click="approveProduct(record)">通过</a-button>
              <a-button type="text" size="small" status="danger" @click="rejectProduct(record)">拒绝</a-button>
            </template>
          </a-table>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card class="table-card">
          <template #title>
            <div class="card-title">
              <span>最新订单</span>
              <a-link @click="goToOrders">查看更多</a-link>
            </div>
          </template>
          <a-table :columns="orderColumns" :data="latestOrders" :pagination="false" size="small">
            <template #status="{ record }">
              <a-tag :color="getOrderStatusColor(record.status)">{{ record.status }}</a-tag>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="20" class="quick-actions">
      <a-col :span="24">
        <a-card class="actions-card">
          <div class="card-header">
            <h3>快捷操作</h3>
          </div>
          <div class="actions-grid">
            <div class="action-item" @click="handleAction('publish')">
              <icon-app />
              <span>发布公告</span>
            </div>
            <div class="action-item" @click="handleAction('user')">
              <icon-user-add />
              <span>添加用户</span>
            </div>
            <div class="action-item" @click="handleAction('category')">
              <icon-menu />
              <span>添加分类</span>
            </div>
            <div class="action-item" @click="handleAction('export')">
              <icon-download />
              <span>导出数据</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()

const currentDate = ref('')
const userChartPeriod = ref('week')
const userChartRef = ref(null)
const categoryChartRef = ref(null)
let userChart = null
let categoryChart = null

const statistics = ref({
  totalUsers: 2847,
  userGrowth: 12.5,
  totalProducts: 5632,
  productGrowth: 8.3,
  totalOrders: 12045,
  orderGrowth: 15.7,
  todayNewUsers: 45,
  todayNewUserGrowth: 23.2
})

const pendingProducts = ref([
  { id: 1, name: 'iPhone 14 Pro Max 256G', price: 6999, seller: '张三', status: '待审核', time: '2024-01-15 10:30' },
  { id: 2, name: '联想拯救者游戏本', price: 5899, seller: '李四', status: '待审核', time: '2024-01-15 09:45' },
  { id: 3, name: '小米手环8 NFC版', price: 299, seller: '王五', status: '待审核', time: '2024-01-15 08:20' },
  { id: 4, name: 'AirPods Pro 2代', price: 1599, seller: '赵六', status: '待审核', time: '2024-01-14 22:15' },
  { id: 5, name: '索尼WH-1000XM5', price: 2199, seller: '钱七', status: '待审核', time: '2024-01-14 18:30' }
])

const latestOrders = ref([
  { id: 'ORD20240115001', product: 'iPad Air 5', amount: 4599, buyer: '孙八', status: '已完成', time: '2024-01-15 11:20' },
  { id: 'ORD20240115002', product: 'Switch OLED', amount: 2099, buyer: '周九', status: '配送中', time: '2024-01-15 10:15' },
  { id: 'ORD20240115003', product: '戴尔显示器27寸', amount: 1399, buyer: '吴十', status: '待发货', time: '2024-01-15 09:30' },
  { id: 'ORD20240115004', product: '机械键盘87键', amount: 399, buyer: '郑一', status: '已取消', time: '2024-01-15 08:45' },
  { id: 'ORD20240115005', product: '罗技GPW鼠标', amount: 499, buyer: '陈二', status: '已完成', time: '2024-01-14 22:30' }
])

const pendingProductColumns = [
  { title: '商品名称', dataIndex: 'name', ellipsis: true },
  { title: '价格', dataIndex: 'price' },
  { title: '卖家', dataIndex: 'seller' },
  { title: '状态', dataIndex: 'status', slotName: 'status' },
  { title: '操作', slotName: 'actions', width: 120 }
]

const orderColumns = [
  { title: '订单号', dataIndex: 'id', width: 140 },
  { title: '商品', dataIndex: 'product', ellipsis: true },
  { title: '金额', dataIndex: 'amount' },
  { title: '买家', dataIndex: 'buyer' },
  { title: '状态', dataIndex: 'status', slotName: 'status' }
]

const getOrderStatusColor = (status) => {
  const colorMap = {
    '已完成': 'green',
    '配送中': 'arcoblue',
    '待发货': 'orange',
    '已取消': 'gray'
  }
  return colorMap[status] || 'default'
}

const updateUserChart = () => {
  if (userChart) {
    userChart.dispose()
  }
  initUserChart()
}

const initUserChart = () => {
  if (!userChartRef.value) return

  userChart = echarts.init(userChartRef.value)

  const days = userChartPeriod.value === 'week' ? 7 : 30
  const dates = []
  const data = []

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)

    if (userChartPeriod.value === 'week') {
      data.push(Math.floor(Math.random() * 30) + 20 + (days - i) * 2)
    } else {
      data.push(Math.floor(Math.random() * 40) + 15 + (days - i))
    }
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      textStyle: { color: '#1d2129' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisLabel: { color: '#4e5969' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f2f3f5' } },
      axisLabel: { color: '#4e5969' }
    },
    series: [{
      name: '新增用户',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#165dff', width: 2 },
      itemStyle: { color: '#165dff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(22, 93, 255, 0.2)' },
          { offset: 1, color: 'rgba(22, 93, 255, 0.01)' }
        ])
      },
      data: data
    }]
  }

  userChart.setOption(option)
}

const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  categoryChart = echarts.init(categoryChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      textStyle: { color: '#1d2129' }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#4e5969', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: false }
      },
      labelLine: { show: false },
      data: [
        { value: 35, name: '电子产品', itemStyle: { color: '#165dff' } },
        { value: 25, name: '图书教材', itemStyle: { color: '#00d084' } },
        { value: 18, name: '生活用品', itemStyle: { color: '#ff7d00' } },
        { value: 12, name: '运动装备', itemStyle: { color: '#722ed1' } },
        { value: 10, name: '其他', itemStyle: { color: '#86909c' } }
      ]
    }]
  }

  categoryChart.setOption(option)
}

const approveProduct = (record) => {
  record.status = '已通过'
}

const rejectProduct = (record) => {
  record.status = '已拒绝'
}

const goToProducts = () => {
  router.push('/admin/products')
}

const goToOrders = () => {
  router.push('/admin/orders')
}

const handleAction = (type) => {
  console.log('Action:', type)
}

const updateDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  currentDate.value = `${year}年${month}月${day}日 ${weekday}`
}

onMounted(() => {
  updateDate()
  setTimeout(() => {
    initUserChart()
    initCategoryChart()
  }, 100)

  window.addEventListener('resize', () => {
    if (userChart) userChart.resize()
    if (categoryChart) categoryChart.resize()
  })
})

onUnmounted(() => {
  if (userChart) userChart.dispose()
  if (categoryChart) categoryChart.dispose()
})
</script>

<style scoped>
.dashboard {
  background-color: #f5f6f7;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1d2129;
  font-weight: 600;
}

.date {
  color: #86909c;
  font-size: 14px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.arco-card-body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  color: #86909c;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  color: #1d2129;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.stat-trend .arco-icon {
  color: #00d084;
}

.trend-value {
  color: #00d084;
  font-weight: 500;
}

.trend-value.negative {
  color: #f53f3f;
}

.trend-label {
  color: #86909c;
  margin-left: 4px;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1d2129;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.tables-section {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 8px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title span {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.quick-actions {
  margin-bottom: 20px;
}

.actions-card {
  border-radius: 8px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background-color: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-item:hover {
  background-color: #e5e6eb;
  transform: translateY(-2px);
}

.action-item .arco-icon {
  font-size: 32px;
  color: #165dff;
  margin-bottom: 12px;
}

.action-item span {
  color: #1d2129;
  font-size: 14px;
  font-weight: 500;
}
</style>
