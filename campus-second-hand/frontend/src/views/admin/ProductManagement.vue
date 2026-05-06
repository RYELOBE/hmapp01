<template>
  <div class="management">
    <h2>商品管理</h2>

    <a-card class="search-card">
      <a-space wrap>
        <a-input
          v-model="searchName"
          placeholder="搜索商品名称"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><icon-search /></template>
        </a-input>

        <a-select
          v-model="searchCategory"
          placeholder="选择分类"
          allow-clear
          style="width: 140px"
          @change="handleSearch"
        >
          <a-option value="数码电子">数码电子</a-option>
          <a-option value="图书教材">图书教材</a-option>
          <a-option value="生活用品">生活用品</a-option>
          <a-option value="服饰鞋包">服饰鞋包</a-option>
          <a-option value="运动健身">运动健身</a-option>
          <a-option value="美妆护肤">美妆护肤</a-option>
        </a-select>

        <a-select
          v-model="searchStatus"
          placeholder="选择状态"
          allow-clear
          style="width: 140px"
          @change="handleSearch"
        >
          <a-option value="pending">待审核</a-option>
          <a-option value="approved">已通过</a-option>
          <a-option value="rejected">已拒绝</a-option>
        </a-select>

        <a-range-picker
          v-model="dateRange"
          style="width: 260px"
          @change="handleSearch"
        />

        <a-button type="primary" @click="handleSearch">
          <template #icon><icon-search /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data="filteredData"
        :pagination="pagination"
        :row-class="rowClass"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #image="{ record }">
          <img
            :src="record.image"
            class="product-image"
            :alt="record.name"
            @error="handleImageError"
          />
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #price="{ record }">
          <span class="price">¥{{ record.price.toFixed(2) }}</span>
        </template>

        <template #publishTime="{ record }">
          {{ formatDate(record.publishTime) }}
        </template>

        <template #operations="{ record }">
          <a-space>
            <a-link @click="viewDetail(record)">查看详情</a-link>
            <a-link
              v-if="record.status === 'pending'"
              status="success"
              @click="handleApprove(record)"
            >
              通过
            </a-link>
            <a-link
              v-if="record.status === 'pending'"
              status="danger"
              @click="handleReject(record)"
            >
              拒绝
            </a-link>
            <a-link
              v-if="record.status === 'approved'"
              status="warning"
              @click="handleOffline(record)"
            >
              下架
            </a-link>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailVisible"
      title="商品详情"
      :width="700"
      :footer="null"
    >
      <div v-if="currentProduct" class="product-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="商品ID">{{ currentProduct.id }}</a-descriptions-item>
          <a-descriptions-item label="商品名称">{{ currentProduct.name }}</a-descriptions-item>
          <a-descriptions-item label="分类">{{ currentProduct.category }}</a-descriptions-item>
          <a-descriptions-item label="价格">
            <span class="price">¥{{ currentProduct.price.toFixed(2) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="原价">
            <span class="original-price">¥{{ currentProduct.originalPrice.toFixed(2) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="新旧程度">{{ currentProduct.condition }}</a-descriptions-item>
          <a-descriptions-item label="库存">{{ currentProduct.stock }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentProduct.status)">
              {{ getStatusText(currentProduct.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="卖家" :span="2">{{ currentProduct.sellerName }}</a-descriptions-item>
          <a-descriptions-item label="卖家电话" :span="2">{{ currentProduct.sellerPhone }}</a-descriptions-item>
          <a-descriptions-item label="商品描述" :span="2">{{ currentProduct.description }}</a-descriptions-item>
          <a-descriptions-item label="发布时间" :span="2">{{ formatDate(currentProduct.publishTime) }}</a-descriptions-item>
        </a-descriptions>

        <div class="image-section">
          <h4>商品图片</h4>
          <div class="image-list">
            <img
              v-for="(img, index) in currentProduct.images"
              :key="index"
              :src="img"
              class="detail-image"
              @error="handleImageError"
            />
          </div>
        </div>

        <div v-if="currentProduct.auditHistory && currentProduct.auditHistory.length" class="audit-section">
          <h4>审核历史</h4>
          <a-timeline>
            <a-timeline-item
              v-for="(audit, index) in currentProduct.auditHistory"
              :key="index"
              :color="audit.action === '通过' ? 'green' : audit.action === '拒绝' ? 'red' : 'blue'"
            >
              <p><strong>{{ audit.action }}</strong> - {{ audit.operator }}</p>
              <p>{{ formatDate(audit.time) }}</p>
              <p v-if="audit.reason">原因：{{ audit.reason }}</p>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="rejectVisible"
      title="拒绝原因"
      @before-ok="submitReject"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="拒绝原因" required>
          <a-textarea
            v-model="rejectForm.reason"
            placeholder="请输入拒绝原因"
            :max-length="200"
            show-word-limit
            style="height: 100px"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Message } from '@arco-design/web-vue'

const searchName = ref('')
const searchCategory = ref('')
const searchStatus = ref('')
const dateRange = ref([])
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentProduct = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const rejectForm = reactive({
  reason: ''
})

const categories = ['数码电子', '图书教材', '生活用品', '服饰鞋包', '运动健身', '美妆护肤']
const sellers = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']
const conditions = ['全新', '几乎全新', '轻微使用痕迹', '正常使用痕迹', '明显使用痕迹']

const mockProducts = [
  { id: 1, name: 'iPhone 14 Pro Max 256G', category: '数码电子', price: 6999, originalPrice: 8999, condition: '几乎全新', stock: 1, status: 'pending', sellerName: '张三', sellerPhone: '13800138001', publishTime: '2024-03-15 10:30:00', description: '2023年12月购入，使用三个月，无划痕无磕碰，配件齐全。', images: ['https://picsum.photos/200?random=1', 'https://picsum.photos/200?random=2', 'https://picsum.photos/200?random=3'], auditHistory: [] },
  { id: 2, name: '高等数学同济第七版', category: '图书教材', price: 25, originalPrice: 49, condition: '轻微使用痕迹', stock: 1, status: 'approved', sellerName: '李四', sellerPhone: '13800138002', publishTime: '2024-03-14 14:20:00', description: '高数教材，略有笔记，不影响使用。', images: ['https://picsum.photos/200?random=4', 'https://picsum.photos/200?random=5'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-14 15:00:00' }] },
  { id: 3, name: '小米手环8 NFC版', category: '数码电子', price: 180, originalPrice: 299, condition: '全新', stock: 1, status: 'pending', sellerName: '王五', sellerPhone: '13800138003', publishTime: '2024-03-15 09:00:00', description: '单位年会奖品，全新未拆封。', images: ['https://picsum.photos/200?random=6', 'https://picsum.photos/200?random=7'], auditHistory: [] },
  { id: 4, name: '床上书桌折叠桌', category: '生活用品', price: 45, originalPrice: 89, condition: '正常使用痕迹', stock: 2, status: 'approved', sellerName: '赵六', sellerPhone: '13800138004', publishTime: '2024-03-13 16:45:00', description: '用了半年，桌面有些许磨损，功能完好。', images: ['https://picsum.photos/200?random=8'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-13 18:00:00' }] },
  { id: 5, name: 'Nike Air Force 1 白', category: '服饰鞋包', price: 380, originalPrice: 799, condition: '轻微使用痕迹', stock: 1, status: 'rejected', sellerName: '钱七', sellerPhone: '13800138005', publishTime: '2024-03-12 11:15:00', description: '穿了两次洗过，很干净。', images: ['https://picsum.photos/200?random=9', 'https://picsum.photos/200?random=10'], auditHistory: [{ action: '拒绝', operator: '管理员', time: '2024-03-12 14:00:00', reason: '图片不清晰，请重新上传' }] },
  { id: 6, name: '线性代数教材', category: '图书教材', price: 18, originalPrice: 35, condition: '正常使用痕迹', stock: 1, status: 'pending', sellerName: '孙八', sellerPhone: '13800138006', publishTime: '2024-03-15 08:30:00', description: '大二线代教材，有重点标记。', images: ['https://picsum.photos/200?random=11'], auditHistory: [] },
  { id: 7, name: '雅思真题剑15-18', category: '图书教材', price: 60, originalPrice: 180, condition: '几乎全新', stock: 4, status: 'approved', sellerName: '周九', sellerPhone: '13800138007', publishTime: '2024-03-11 20:00:00', description: '备考结束后闲置，做了不到十页。', images: ['https://picsum.photos/200?random=12', 'https://picsum.photos/200?random=13'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-11 21:00:00' }] },
  { id: 8, name: '电动牙刷', category: '生活用品', price: 90, originalPrice: 199, condition: '轻微使用痕迹', stock: 1, status: 'pending', sellerName: '吴十', sellerPhone: '13800138008', publishTime: '2024-03-15 11:00:00', description: '用了三个月，充一次电能用很久。', images: ['https://picsum.photos/200?random=14'], auditHistory: [] },
  { id: 9, name: 'MacBook Air M1', category: '数码电子', price: 5500, originalPrice: 7999, condition: '几乎全新', stock: 1, status: 'pending', sellerName: '张三', sellerPhone: '13800138001', publishTime: '2024-03-15 07:00:00', description: '买来写论文用了半年，现在换台式了。', images: ['https://picsum.photos/200?random=15', 'https://picsum.photos/200?random=16'], auditHistory: [] },
  { id: 10, name: ' Adidas运动外套', category: '服饰鞋包', price: 150, originalPrice: 399, condition: '轻微使用痕迹', stock: 1, status: 'approved', sellerName: '李四', sellerPhone: '13800138002', publishTime: '2024-03-10 15:30:00', description: '春秋季穿了几次，尺码不合适出了。', images: ['https://picsum.photos/200?random=17'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-10 17:00:00' }] },
  { id: 11, name: '漫步者蓝牙耳机', category: '数码电子', price: 120, originalPrice: 249, condition: '正常使用痕迹', stock: 1, status: 'pending', sellerName: '王五', sellerPhone: '13800138003', publishTime: '2024-03-15 06:30:00', description: '用了快一年，续航还可以。', images: ['https://picsum.photos/200?random=18'], auditHistory: [] },
  { id: 12, name: '心理学概论教材', category: '图书教材', price: 22, originalPrice: 45, condition: '正常使用痕迹', stock: 1, status: 'approved', sellerName: '赵六', sellerPhone: '13800138004', publishTime: '2024-03-09 13:00:00', description: '专业课教材，有荧光笔标记。', images: ['https://picsum.photos/200?random=19'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-09 14:30:00' }] },
  { id: 13, name: '小米台灯', category: '生活用品', price: 65, originalPrice: 169, condition: '轻微使用痕迹', stock: 1, status: 'rejected', sellerName: '钱七', sellerPhone: '13800138005', publishTime: '2024-03-08 10:00:00', description: '护眼台灯，支持多档调光。', images: ['https://picsum.photos/200?random=20'], auditHistory: [{ action: '拒绝', operator: '管理员', time: '2024-03-08 11:00:00', reason: '商品描述不完整' }] },
  { id: 14, name: 'Switch游戏卡带', category: '数码电子', price: 180, originalPrice: 298, condition: '几乎全新', stock: 3, status: 'pending', sellerName: '孙八', sellerPhone: '13800138006', publishTime: '2024-03-15 05:00:00', description: '买来通了关就没再玩过。', images: ['https://picsum.photos/200?random=21', 'https://picsum.photos/200?random=22'], auditHistory: [] },
  { id: 15, name: '卡西欧计算器', category: '数码电子', price: 55, originalPrice: 120, condition: '正常使用痕迹', stock: 1, status: 'approved', sellerName: '周九', sellerPhone: '13800138007', publishTime: '2024-03-07 09:00:00', description: '考试必备，函数计算器。', images: ['https://picsum.photos/200?random=23'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-07 10:00:00' }] },
  { id: 16, name: '考研政治资料全套', category: '图书教材', price: 80, originalPrice: 200, condition: '轻微使用痕迹', stock: 5, status: 'pending', sellerName: '吴十', sellerPhone: '13800138008', publishTime: '2024-03-15 04:30:00', description: '考研上岸后出，笔记详细。', images: ['https://picsum.photos/200?random=24', 'https://picsum.photos/200?random=25'], auditHistory: [] },
  { id: 17, name: '宿舍小风扇', category: '生活用品', price: 25, originalPrice: 59, condition: '正常使用痕迹', stock: 1, status: 'approved', sellerName: '张三', sellerPhone: '13800138001', publishTime: '2024-03-06 14:00:00', description: '夏天神器，可充电可插电。', images: ['https://picsum.photos/200?random=26'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-06 15:30:00' }] },
  { id: 18, name: 'Nike运动背包', category: '服饰鞋包', price: 120, originalPrice: 299, condition: '轻微使用痕迹', stock: 2, status: 'pending', sellerName: '李四', sellerPhone: '13800138002', publishTime: '2024-03-15 03:00:00', description: '健身房用了几次，容量大。', images: ['https://picsum.photos/200?random=27'], auditHistory: [] },
  { id: 19, name: 'iPad Air 5 64G', category: '数码电子', price: 3200, originalPrice: 4399, condition: '几乎全新', stock: 1, status: 'pending', sellerName: '王五', sellerPhone: '13800138003', publishTime: '2024-03-15 02:00:00', description: '配原装笔和键盘，考研用完了。', images: ['https://picsum.photos/200?random=28', 'https://picsum.photos/200?random=29'], auditHistory: [] },
  { id: 20, name: '大学物理教材上下册', category: '图书教材', price: 35, originalPrice: 78, condition: '正常使用痕迹', stock: 2, status: 'approved', sellerName: '赵六', sellerPhone: '13800138004', publishTime: '2024-03-05 11:00:00', description: '大物教材，有部分解题痕迹。', images: ['https://picsum.photos/200?random=30'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-05 12:00:00' }] },
  { id: 21, name: '置物架收纳架', category: '生活用品', price: 38, originalPrice: 89, condition: '正常使用痕迹', stock: 1, status: 'rejected', sellerName: '钱七', sellerPhone: '13800138005', publishTime: '2024-03-04 16:00:00', description: '桌面收纳架毕业带不走。', images: ['https://picsum.photos/200?random=31'], auditHistory: [{ action: '拒绝', operator: '管理员', time: '2024-03-04 17:00:00', reason: '价格过低，疑似虚假标价' }] },
  { id: 22, name: '华为FreeBuds耳机', category: '数码电子', price: 200, originalPrice: 399, condition: '轻微使用痕迹', stock: 1, status: 'pending', sellerName: '孙八', sellerPhone: '13800138006', publishTime: '2024-03-15 01:00:00', description: '降噪耳机，用了半年，音质好。', images: ['https://picsum.photos/200?random=32'], auditHistory: [] },
  { id: 23, name: '托福词汇红宝书', category: '图书教材', price: 28, originalPrice: 68, condition: '轻微使用痕迹', stock: 1, status: 'approved', sellerName: '周九', sellerPhone: '13800138007', publishTime: '2024-03-03 10:00:00', description: '托福词汇，词根词缀法整理。', images: ['https://picsum.photos/200?random=33'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-03 11:00:00' }] },
  { id: 24, name: '羽毛球拍套装', category: '运动健身', price: 85, originalPrice: 180, condition: '正常使用痕迹', stock: 1, status: 'pending', sellerName: '吴十', sellerPhone: '13800138008', publishTime: '2024-03-14 22:00:00', description: '含两支球拍和球，适合新手。', images: ['https://picsum.photos/200?random=34', 'https://picsum.photos/200?random=35'], auditHistory: [] },
  { id: 25, name: '宿舍电煮锅', category: '生活用品', price: 45, originalPrice: 99, condition: '轻微使用痕迹', stock: 1, status: 'approved', sellerName: '张三', sellerPhone: '13800138001', publishTime: '2024-03-02 15:00:00', description: '1.5L小锅，蒸煮炒都行。', images: ['https://picsum.photos/200?random=36'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-02 16:00:00' }] },
  { id: 26, name: '程序员穿搭T恤', category: '服饰鞋包', price: 30, originalPrice: 89, condition: '全新', stock: 3, status: 'pending', sellerName: '李四', sellerPhone: '13800138002', publishTime: '2024-03-14 20:00:00', description: '买大了几件，吊牌未拆。', images: ['https://picsum.photos/200?random=37'], auditHistory: [] },
  { id: 27, name: '索尼PS4游戏机', category: '数码电子', price: 1200, originalPrice: 2199, condition: '正常使用痕迹', stock: 1, status: 'pending', sellerName: '王五', sellerPhone: '13800138003', publishTime: '2024-03-14 18:00:00', description: '附带手柄和几款游戏，机能正常。', images: ['https://picsum.photos/200?random=38', 'https://picsum.photos/200?random=39'], auditHistory: [] },
  { id: 28, name: '四六级真题全套', category: '图书教材', price: 15, originalPrice: 56, condition: '正常使用痕迹', stock: 2, status: 'approved', sellerName: '赵六', sellerPhone: '13800138004', publishTime: '2024-03-01 09:00:00', description: '含真题和答案详解，备考用完了。', images: ['https://picsum.photos/200?random=40'], auditHistory: [{ action: '通过', operator: '管理员', time: '2024-03-01 10:00:00' }] },
  { id: 29, name: '瑜伽垫加厚', category: '运动健身', price: 35, originalPrice: 79, condition: '轻微使用痕迹', stock: 1, status: 'pending', sellerName: '钱七', sellerPhone: '13800138005', publishTime: '2024-03-14 16:00:00', description: '183×80cm，加厚防滑款。', images: ['https://picsum.photos/200?random=41'], auditHistory: [] },
  { id: 30, name: '水乳套装护肤品', category: '美妆护肤', price: 120, originalPrice: 350, condition: '轻微使用痕迹', stock: 1, status: 'pending', sellerName: '孙八', sellerPhone: '13800138006', publishTime: '2024-03-14 14:00:00', description: '适合油皮，用了三分之一。', images: ['https://picsum.photos/200?random=42', 'https://picsum.photos/200?random=43'], auditHistory: [] }
]

const columns = [
  { title: '商品ID', dataIndex: 'id', width: 80, align: 'center' },
  { title: '商品图片', dataIndex: 'image', width: 100, slotName: 'image', align: 'center' },
  { title: '商品名称', dataIndex: 'name', minWidth: 180 },
  { title: '分类', dataIndex: 'category', width: 100, align: 'center' },
  { title: '价格', dataIndex: 'price', width: 100, slotName: 'price', align: 'center' },
  { title: '卖家', dataIndex: 'sellerName', width: 80, align: 'center' },
  { title: '状态', dataIndex: 'status', width: 100, slotName: 'status', align: 'center' },
  { title: '发布时间', dataIndex: 'publishTime', width: 160, slotName: 'publishTime', align: 'center' },
  { title: '操作', dataIndex: 'operations', width: 220, slotName: 'operations', align: 'center', fixed: 'right' }
]

const pagination = reactive({
  total: computed(() => filteredData.value.length),
  current: currentPage,
  pageSize: pageSize,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50]
})

const filteredData = computed(() => {
  let result = [...mockProducts]

  if (searchName.value) {
    result = result.filter(item =>
      item.name.toLowerCase().includes(searchName.value.toLowerCase())
    )
  }

  if (searchCategory.value) {
    result = result.filter(item => item.category === searchCategory.value)
  }

  if (searchStatus.value) {
    result = result.filter(item => item.status === searchStatus.value)
  }

  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]).getTime()
    const endDate = new Date(dateRange.value[1]).getTime() + 86399999
    result = result.filter(item => {
      const itemDate = new Date(item.publishTime).getTime()
      return itemDate >= startDate && itemDate <= endDate
    })
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const getStatusColor = (status) => {
  const colors = {
    pending: 'arcoblue',
    approved: 'green',
    rejected: 'red'
  }
  return colors[status] || 'gray'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/200?random=placeholder'
}

const rowClass = ({ record }) => {
  return record.status === 'pending' ? 'pending-row' : ''
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchName.value = ''
  searchCategory.value = ''
  searchStatus.value = ''
  dateRange.value = []
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const viewDetail = (record) => {
  currentProduct.value = record
  detailVisible.value = true
}

const handleApprove = (record) => {
  record.status = 'approved'
  record.auditHistory = record.auditHistory || []
  record.auditHistory.unshift({
    action: '通过',
    operator: '管理员',
    time: new Date().toLocaleString('zh-CN')
  })
  Message.success(`商品「${record.name}」审核通过`)
}

const handleReject = (record) => {
  currentProduct.value = record
  rejectForm.reason = ''
  rejectVisible.value = true
}

const submitReject = (done) => {
  if (!rejectForm.reason.trim()) {
    Message.warning('请填写拒绝原因')
    done(false)
    return
  }

  const product = mockProducts.find(p => p.id === currentProduct.value.id)
  if (product) {
    product.status = 'rejected'
    product.auditHistory = product.auditHistory || []
    product.auditHistory.unshift({
      action: '拒绝',
      operator: '管理员',
      time: new Date().toLocaleString('zh-CN'),
      reason: rejectForm.reason
    })
  }

  Message.success(`商品「${currentProduct.value.name}」已拒绝`)
  rejectVisible.value = false
  done(true)
}

const handleOffline = (record) => {
  record.status = 'rejected'
  record.auditHistory = record.auditHistory || []
  record.auditHistory.unshift({
    action: '下架',
    operator: '管理员',
    time: new Date().toLocaleString('zh-CN')
  })
  Message.warning(`商品「${record.name}」已下架`)
}
</script>

<style scoped>
.management {
  padding: 20px;
}

.management h2 {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.price {
  color: #ff4d4f;
  font-weight: 600;
}

.original-price {
  color: #999;
  text-decoration: line-through;
}

.product-detail {
  padding: 10px 0;
}

.product-detail h4 {
  margin: 20px 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.image-section {
  margin-top: 20px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.audit-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

:deep(.pending-row) {
  background-color: #f0f7ff !important;
}

:deep(.arco-table .arco-table-tr:hover .arco-table-td) {
  background-color: #f7f8fa;
}

:deep(.arco-tag) {
  border-radius: 4px;
}
</style>
