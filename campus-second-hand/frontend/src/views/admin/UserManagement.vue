<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <a-card class="search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="用户名">
          <a-input v-model="searchForm.username" placeholder="请输入用户名" allow-clear style="width: 200px;">
            <template #prefix><icon-search /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="searchForm.phone" placeholder="请输入手机号" allow-clear style="width: 200px;">
            <template #prefix><icon-phone /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="认证状态">
          <a-select v-model="searchForm.certStatus" placeholder="请选择" allow-clear style="width: 150px;">
            <a-option value="certified">已认证</a-option>
            <a-option value="pending">待审核</a-option>
            <a-option value="unverified">未认证</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="table-card">
      <template #title>
        <div class="table-title">
          <span>用户列表</span>
          <a-space>
            <a-button type="primary" @click="handleExport">
              <template #icon><icon-download /></template>
              导出数据
            </a-button>
          </a-space>
        </div>
      </template>
      <a-table
        :columns="columns"
        :data="filteredUsers"
        :pagination="pagination"
        :loading="loading"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #certStatus="{ record }">
          <a-tag :color="getCertStatusColor(record.certStatus)">
            {{ getCertStatusText(record.certStatus) }}
          </a-tag>
        </template>
        <template #creditScore="{ record }">
          <a-progress
            :percent="record.creditScore / 100"
            :color="getCreditScoreColor(record.creditScore)"
            size="small"
            :show-text="false"
            style="width: 80px; display: inline-block;"
          />
          <span style="margin-left: 8px; color: #1d2129;">{{ record.creditScore }}</span>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
            <a-dropdown trigger="click">
              <a-button type="text" size="small">
                更多
                <icon-down />
              </a-button>
              <template #content>
                <a-doption @click="toggleUserStatus(record)">
                  {{ record.status === 'active' ? '禁用' : '启用' }}
                </a-doption>
                <a-doption status="danger" @click="handleDelete(record)">删除</a-doption>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailModalVisible"
      :title="`用户详情 - ${currentUser?.username || ''}`"
      :width="800"
      :footer="null"
    >
      <div class="user-detail">
        <a-tabs default-active-key="info">
          <a-tab-pane key="info" title="基本信息">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="用户ID">{{ currentUser?.id }}</a-descriptions-item>
              <a-descriptions-item label="用户名">{{ currentUser?.username }}</a-descriptions-item>
              <a-descriptions-item label="手机号">{{ currentUser?.phone }}</a-descriptions-item>
              <a-descriptions-item label="邮箱">{{ currentUser?.email }}</a-descriptions-item>
              <a-descriptions-item label="校园认证">
                <a-tag :color="getCertStatusColor(currentUser?.certStatus)">
                  {{ getCertStatusText(currentUser?.certStatus) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="注册时间">{{ currentUser?.registerTime }}</a-descriptions-item>
              <a-descriptions-item label="账号状态">
                <a-tag :color="currentUser?.status === 'active' ? 'green' : 'red'">
                  {{ currentUser?.status === 'active' ? '正常' : '已禁用' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="最后登录">{{ currentUser?.lastLogin }}</a-descriptions-item>
            </a-descriptions>
          </a-tab-pane>

          <a-tab-pane key="stats" title="交易统计">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-card size="small" class="stat-item">
                  <div class="stat-label">累计交易</div>
                  <div class="stat-value">{{ currentUser?.stats?.totalTransactions || 0 }} 笔</div>
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card size="small" class="stat-item">
                  <div class="stat-label">累计消费</div>
                  <div class="stat-value">¥{{ currentUser?.stats?.totalSpent || 0 }}</div>
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card size="small" class="stat-item">
                  <div class="stat-label">在售商品</div>
                  <div class="stat-value">{{ currentUser?.stats?.sellingProducts || 0 }} 件</div>
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card size="small" class="stat-item">
                  <div class="stat-label">收藏数</div>
                  <div class="stat-value">{{ currentUser?.stats?.favorites || 0 }}</div>
                </a-card>
              </a-col>
            </a-row>
            <a-card size="small" style="margin-top: 16px;">
              <template #title>交易趋势</template>
              <div ref="transactionChartRef" style="width: 100%; height: 200px;"></div>
            </a-card>
          </a-tab-pane>

          <a-tab-pane key="credit" title="信用评分">
            <div class="credit-section">
              <div class="credit-score-display">
                <a-progress
                  type="circle"
                  :percent="Math.round((currentUser?.creditScore || 0) / 100 * 100)"
                  :color="getCreditScoreColor(currentUser?.creditScore || 0)"
                  :width="120"
                />
                <div class="credit-info">
                  <div class="credit-label">信用评分</div>
                  <div class="credit-value">{{ currentUser?.creditScore || 0 }}</div>
                  <div class="credit-level">{{ getCreditLevel(currentUser?.creditScore || 0) }}</div>
                </div>
              </div>
              <a-divider />
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="信用基础分">800</a-descriptions-item>
                <a-descriptions-item label="交易表现">+{{ currentUser?.creditScore - 800 || 0 }}</a-descriptions-item>
                <a-descriptions-item label="最近活跃">{{ currentUser?.lastActive || '7天内' }}</a-descriptions-item>
              </a-descriptions>
            </div>
          </a-tab-pane>

          <a-tab-pane key="violations" title="违规记录">
            <a-empty v-if="!currentUser?.violations || currentUser.violations.length === 0">
              <template #image>
                <icon-check-circle-fill style="font-size: 48px; color: #00d084;" />
              </template>
              暂无违规记录
            </a-empty>
            <a-list v-else :dataSource="currentUser.violations">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <a-space>
                        <a-tag :color="item.type === 'warning' ? 'orange' : 'red'">
                          {{ item.type === 'warning' ? '警告' : '违规' }}
                        </a-tag>
                        <span>{{ item.reason }}</span>
                      </a-space>
                    </template>
                    <template #description>
                      <span style="color: #86909c;">{{ item.time }}</span>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const loading = ref(false)
const detailModalVisible = ref(false)
const currentUser = ref(null)
const transactionChartRef = ref(null)
let transactionChart = null

const searchForm = reactive({
  username: '',
  phone: '',
  certStatus: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const users = ref([
  { id: 1001, username: '张三', phone: '13800138001', email: 'zhangsan@campus.edu', certStatus: 'certified', creditScore: 950, registerTime: '2023-09-01', status: 'active', lastLogin: '2024-01-15 10:30', lastActive: '今日', stats: { totalTransactions: 45, totalSpent: 12580, sellingProducts: 8, favorites: 23 }, violations: [] },
  { id: 1002, username: '李四', phone: '13800138002', email: 'lisi@campus.edu', certStatus: 'certified', creditScore: 920, registerTime: '2023-09-05', status: 'active', lastLogin: '2024-01-15 09:20', lastActive: '今日', stats: { totalTransactions: 38, totalSpent: 8920, sellingProducts: 5, favorites: 15 }, violations: [] },
  { id: 1003, username: '王五', phone: '13800138003', email: 'wangwu@campus.edu', certStatus: 'pending', creditScore: 850, registerTime: '2023-10-10', status: 'active', lastLogin: '2024-01-14 18:45', lastActive: '2天内', stats: { totalTransactions: 22, totalSpent: 5600, sellingProducts: 3, favorites: 8 }, violations: [] },
  { id: 1004, username: '赵六', phone: '13800138004', email: 'zhaoliu@campus.edu', certStatus: 'certified', creditScore: 980, registerTime: '2023-08-15', status: 'active', lastLogin: '2024-01-15 11:00', lastActive: '今日', stats: { totalTransactions: 67, totalSpent: 18500, sellingProducts: 12, favorites: 35 }, violations: [] },
  { id: 1005, username: '钱七', phone: '13800138005', email: 'qianqi@campus.edu', certStatus: 'unverified', creditScore: 780, registerTime: '2023-11-20', status: 'active', lastLogin: '2024-01-13 14:30', lastActive: '3天内', stats: { totalTransactions: 15, totalSpent: 3200, sellingProducts: 2, favorites: 6 }, violations: [{ type: 'warning', reason: '商品描述不实', time: '2024-01-10' }] },
  { id: 1006, username: '孙八', phone: '13800138006', email: 'sunba@campus.edu', certStatus: 'certified', creditScore: 910, registerTime: '2023-09-12', status: 'active', lastLogin: '2024-01-15 08:15', lastActive: '今日', stats: { totalTransactions: 41, totalSpent: 9800, sellingProducts: 6, favorites: 18 }, violations: [] },
  { id: 1007, username: '周九', phone: '13800138007', email: 'zhoujiu@campus.edu', certStatus: 'certified', creditScore: 935, registerTime: '2023-09-20', status: 'active', lastLogin: '2024-01-14 20:00', lastActive: '1天内', stats: { totalTransactions: 52, totalSpent: 14200, sellingProducts: 9, favorites: 28 }, violations: [] },
  { id: 1008, username: '吴十', phone: '13800138008', email: 'wushi@campus.edu', certStatus: 'pending', creditScore: 820, registerTime: '2023-11-05', status: 'disabled', lastLogin: '2024-01-10 16:30', lastActive: '5天内', stats: { totalTransactions: 8, totalSpent: 1800, sellingProducts: 1, favorites: 4 }, violations: [] },
  { id: 1009, username: '郑一', phone: '13800138009', email: 'zhengyi@campus.edu', certStatus: 'certified', creditScore: 960, registerTime: '2023-08-20', status: 'active', lastLogin: '2024-01-15 10:45', lastActive: '今日', stats: { totalTransactions: 73, totalSpent: 21000, sellingProducts: 15, favorites: 42 }, violations: [] },
  { id: 1010, username: '陈二', phone: '13800138010', email: 'chener@campus.edu', certStatus: 'unverified', creditScore: 800, registerTime: '2023-12-01', status: 'active', lastLogin: '2024-01-14 12:00', lastActive: '1天内', stats: { totalTransactions: 18, totalSpent: 4100, sellingProducts: 4, favorites: 10 }, violations: [] },
  { id: 1011, username: '刘三', phone: '13800138011', email: 'liusan@campus.edu', certStatus: 'certified', creditScore: 945, registerTime: '2023-09-08', status: 'active', lastLogin: '2024-01-15 09:30', lastActive: '今日', stats: { totalTransactions: 48, totalSpent: 13800, sellingProducts: 7, favorites: 25 }, violations: [] },
  { id: 1012, username: '杨四', phone: '13800138012', email: 'yangsicampus.edu', certStatus: 'certified', creditScore: 970, registerTime: '2023-08-25', status: 'active', lastLogin: '2024-01-15 11:15', lastActive: '今日', stats: { totalTransactions: 65, totalSpent: 19200, sellingProducts: 11, favorites: 38 }, violations: [] },
  { id: 1013, username: '黄五', phone: '13800138013', email: 'huangwu@campus.edu', certStatus: 'pending', creditScore: 840, registerTime: '2023-10-25', status: 'active', lastLogin: '2024-01-13 17:00', lastActive: '2天内', stats: { totalTransactions: 25, totalSpent: 6200, sellingProducts: 4, favorites: 12 }, violations: [] },
  { id: 1014, username: '林六', phone: '13800138014', email: 'linliu@campus.edu', certStatus: 'certified', creditScore: 925, registerTime: '2023-09-15', status: 'active', lastLogin: '2024-01-14 19:30', lastActive: '1天内', stats: { totalTransactions: 44, totalSpent: 11500, sellingProducts: 8, favorites: 20 }, violations: [] },
  { id: 1015, username: '许七', phone: '13800138015', email: 'xuqi@campus.edu', certStatus: 'unverified', creditScore: 790, registerTime: '2023-11-15', status: 'active', lastLogin: '2024-01-12 15:45', lastActive: '3天内', stats: { totalTransactions: 12, totalSpent: 2800, sellingProducts: 2, favorites: 5 }, violations: [] },
  { id: 1016, username: '何八', phone: '13800138016', email: 'heba@campus.edu', certStatus: 'certified', creditScore: 955, registerTime: '2023-09-02', status: 'active', lastLogin: '2024-01-15 10:00', lastActive: '今日', stats: { totalTransactions: 58, totalSpent: 16800, sellingProducts: 10, favorites: 32 }, violations: [] },
  { id: 1017, username: '吕九', phone: '13800138017', email: 'lvjiu@campus.edu', certStatus: 'certified', creditScore: 915, registerTime: '2023-09-18', status: 'active', lastLogin: '2024-01-14 21:00', lastActive: '1天内', stats: { totalTransactions: 39, totalSpent: 9200, sellingProducts: 6, favorites: 17 }, violations: [] },
  { id: 1018, username: '施十', phone: '13800138018', email: 'shishi@campus.edu', certStatus: 'pending', creditScore: 830, registerTime: '2023-10-30', status: 'active', lastLogin: '2024-01-13 13:20', lastActive: '2天内', stats: { totalTransactions: 20, totalSpent: 4800, sellingProducts: 3, favorites: 9 }, violations: [] },
  { id: 1019, username: '张伟', phone: '13800138019', email: 'zhangwei@campus.edu', certStatus: 'certified', creditScore: 940, registerTime: '2023-09-10', status: 'active', lastLogin: '2024-01-15 09:00', lastActive: '今日', stats: { totalTransactions: 51, totalSpent: 14500, sellingProducts: 9, favorites: 27 }, violations: [] },
  { id: 1020, username: '王芳', phone: '13800138020', email: 'wangfang@campus.edu', certStatus: 'certified', creditScore: 965, registerTime: '2023-08-28', status: 'active', lastLogin: '2024-01-15 11:30', lastActive: '今日', stats: { totalTransactions: 69, totalSpent: 20500, sellingProducts: 13, favorites: 40 }, violations: [] }
])

const columns = [
  { title: '用户ID', dataIndex: 'id', width: 100 },
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 130 },
  { title: '邮箱', dataIndex: 'email', ellipsis: true },
  { title: '认证状态', dataIndex: 'certStatus', slotName: 'certStatus', width: 120 },
  { title: '信用分', dataIndex: 'creditScore', slotName: 'creditScore', width: 180 },
  { title: '注册时间', dataIndex: 'registerTime', width: 130 },
  { title: '操作', slotName: 'actions', width: 180, fixed: 'right' }
]

const filteredUsers = computed(() => {
  let result = users.value

  if (searchForm.username) {
    result = result.filter(user =>
      user.username.toLowerCase().includes(searchForm.username.toLowerCase())
    )
  }

  if (searchForm.phone) {
    result = result.filter(user =>
      user.phone.includes(searchForm.phone)
    )
  }

  if (searchForm.certStatus) {
    result = result.filter(user =>
      user.certStatus === searchForm.certStatus
    )
  }

  pagination.total = result.length
  return result
})

const getCertStatusColor = (status) => {
  const colorMap = {
    'certified': 'green',
    'pending': 'orange',
    'unverified': 'gray'
  }
  return colorMap[status] || 'default'
}

const getCertStatusText = (status) => {
  const textMap = {
    'certified': '已认证',
    'pending': '待审核',
    'unverified': '未认证'
  }
  return textMap[status] || status
}

const getCreditScoreColor = (score) => {
  if (score >= 950) return '#00d084'
  if (score >= 900) return '#165dff'
  if (score >= 850) return '#ff7d00'
  return '#f53f3f'
}

const getCreditLevel = (score) => {
  if (score >= 950) return '优秀'
  if (score >= 900) return '良好'
  if (score >= 850) return '一般'
  return '较差'
}

const handleSearch = () => {
  pagination.current = 1
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.certStatus = ''
  pagination.current = 1
}

const handlePageChange = (page) => {
  pagination.current = page
}

const handlePageSizeChange = (size) => {
  pagination.pageSize = size
  pagination.current = 1
}

const handleExport = () => {
  console.log('Export users data')
}

const viewDetail = (user) => {
  currentUser.value = user
  detailModalVisible.value = true

  setTimeout(() => {
    initTransactionChart()
  }, 100)
}

const toggleUserStatus = (user) => {
  user.status = user.status === 'active' ? 'disabled' : 'active'
}

const handleDelete = (user) => {
  const index = users.value.findIndex(u => u.id === user.id)
  if (index > -1) {
    users.value.splice(index, 1)
  }
}

const initTransactionChart = () => {
  if (!transactionChartRef.value || !currentUser.value) return

  if (transactionChart) {
    transactionChart.dispose()
  }

  transactionChart = echarts.init(transactionChartRef.value)

  const months = ['9月', '10月', '11月', '12月', '1月']
  const data = [
    Math.floor(Math.random() * 10) + 5,
    Math.floor(Math.random() * 12) + 6,
    Math.floor(Math.random() * 15) + 8,
    Math.floor(Math.random() * 18) + 10,
    Math.floor(Math.random() * 12) + 8
  ]

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
      data: months,
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
      name: '交易笔数',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#165dff' },
          { offset: 1, color: '#4080ff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      data: data
    }]
  }

  transactionChart.setOption(option)
}

watch(detailModalVisible, (newVal) => {
  if (!newVal && transactionChart) {
    transactionChart.dispose()
    transactionChart = null
  }
})

onMounted(() => {
  pagination.total = users.value.length
})

onUnmounted(() => {
  if (transactionChart) {
    transactionChart.dispose()
  }
})
</script>

<style scoped>
.user-management {
  background-color: #f5f6f7;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1d2129;
  font-weight: 600;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}

.table-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title span {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.user-detail {
  padding: 8px 0;
}

:deep(.arco-tabs-nav) {
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  color: #86909c;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  color: #1d2129;
  font-size: 24px;
  font-weight: 600;
}

.credit-section {
  padding: 16px 0;
}

.credit-score-display {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px;
  background-color: #f7f8fa;
  border-radius: 8px;
}

.credit-info {
  flex: 1;
}

.credit-label {
  color: #86909c;
  font-size: 14px;
  margin-bottom: 8px;
}

.credit-value {
  color: #1d2129;
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 4px;
}

.credit-level {
  color: #165dff;
  font-size: 16px;
  font-weight: 500;
}
</style>
