<template>
  <OpsUnifiedTable
    title="商品管理"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
  >
    <!-- 操作栏 -->
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索商品名称/卖家"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="statusFilter" placeholder="商品状态" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="s in itemStatuses" :key="s.value" :value="s.value">{{ s.label }}</a-option>
      </a-select>
      <a-select v-model="categoryFilter" placeholder="商品分类" style="width: 140px" allow-clear @change="handleSearch">
        <a-option v-for="cat in categories" :key="cat.value" :value="cat.value">{{ cat.label }}</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 列插槽 -->
    <template #image="{ record }">
      <a-image :src="getFirstImage(record)" width="60" height="60" fit="cover" style="border-radius:6px" />
    </template>

    <template #title="{ record }">
      <a-tooltip :content="record.title" position="top">
        <span class="ellipsis-text">{{ record.title }}</span>
      </a-tooltip>
    </template>

    <template #price="{ record }">
      <span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(record.price) }}</span>
    </template>

    <template #category="{ record }">
      <a-tag size="small" :color="getCategoryColor(record.category)">{{ getCategoryLabel(record.category) }}</a-tag>
    </template>

    <template #reviewStatus="{ record }">
      <a-tag size="small" :color="getStatusColor(record.reviewStatus, itemStatuses)">{{ getStatusLabel(record.reviewStatus, itemStatuses) }}</a-tag>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看</a-button>
        <a-button v-if="record.reviewStatus === 'APPROVED'" type="text" size="small" status="warning" @click="doOffline(record)">下架</a-button>
        <a-popconfirm content="确定删除该商品吗？" @ok="doDelete(record)">
          <a-button type="text" size="small" status="danger">删除</a-button>
        </a-popconfirm>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <!-- 商品详情抽屉 -->
  <a-drawer v-model:visible="detailVisible" :width="600" title="商品详情" placement="right" unmount-on-close>
    <div v-if="currentItem">
      <div style="margin-bottom:16px">
        <a-image-preview-group>
          <a-space wrap>
            <a-image v-for="(img, i) in getImages(currentItem)" :key="i" :src="img" width="90" height="90" fit="cover" style="border-radius:6px" />
          </a-space>
        </a-image-preview-group>
      </div>
      <a-descriptions :column="2" bordered size="medium">
        <a-descriptions-item label="商品名称" :span="2"><span style="font-weight:600">{{ currentItem.title }}</span></a-descriptions-item>
        <a-descriptions-item label="价格"><span style="color:#f53f3f;font-weight:600">¥{{ formatPrice(currentItem.price) }}</span></a-descriptions-item>
        <a-descriptions-item label="分类">
          <a-tag size="small" :color="getCategoryColor(currentItem.category)">{{ getCategoryLabel(currentItem.category) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="卖家">{{ currentItem.sellerName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag size="small" :color="getStatusColor(currentItem.reviewStatus, itemStatuses)">{{ getStatusLabel(currentItem.reviewStatus, itemStatuses) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="发布时间">{{ formatDate(currentItem.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="商品描述" :span="2"><span style="white-space:pre-wrap">{{ currentItem.description || '-' }}</span></a-descriptions-item>
      </a-descriptions>
    </div>
    <template #footer><a-button @click="detailVisible = false">关闭</a-button></template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'
import { getOpsItems, offlineItem, deleteItem } from '../../../services/ops/index'
import { getItemStatuses, getStatusLabel as getEnumStatusLabel, getStatusColor as getEnumStatusColor, loadEnums as loadDictEnums } from '../../../services/enums'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const tableData = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true, showPageSize: true, pageSizeOptions: [10, 15, 20, 50] })
const categories = ref([])
const itemStatuses = ref([])
const detailVisible = ref(false)
const currentItem = ref(null)

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = computed(() => [
  { title: '商品图片', dataIndex: 'image', width: 100, slotName: 'image' },
  { title: '商品名称', dataIndex: 'title', width: 200, slotName: 'title', ellipsis: true },
  { title: '卖家', dataIndex: 'sellerName', width: 120 },
  { title: '价格', dataIndex: 'price', width: 100, slotName: 'price' },
  { title: '分类', dataIndex: 'category', width: 120, slotName: 'category' },
  { title: '状态', dataIndex: 'reviewStatus', width: 100, slotName: 'reviewStatus' },
  { title: '发布时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 180, fixed: 'right', slotName: 'operations' }
])

async function loadEnums() {
  try {
    itemStatuses.value = await getItemStatuses()
  } catch (e) {
    console.error('[ItemManage] loadEnums error:', e)
  }
}

async function loadDict() {
  try {
    const dict = await loadDictEnums()
    categories.value = dict?.categories || []
  } catch (e) {
    console.error('[ItemManage] loadDict error:', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      category: categoryFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    }
    console.log('[ItemManage] 请求参数:', params)
    const res = await getOpsItems(params)
    console.log('[ItemManage] API返回:', res)
    tableData.value = res?.items || res?.rows || []
    pagination.total = res?.totalCount ?? res?.total ?? 0
    console.log('[ItemManage] 数据加载完成:', { 
      数据量: tableData.value.length, 
      总数: pagination.total 
    })
  } catch (e) {
    console.error('[ItemManage] load error:', e)
    Message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.current = 1; loadData() }
function handleReset() { keyword.value = ''; statusFilter.value = ''; categoryFilter.value = ''; handleSearch() }
function handlePageChange(page) { pagination.current = page; loadData() }

function viewDetail(record) { currentItem.value = record; detailVisible.value = true }

async function doOffline(record) {
  try {
    console.log('[ItemManage] 下架商品:', record.id)
    const res = await offlineItem(record.id)
    console.log('[ItemManage] 下架成功:', res)
    Message.success('商品已下架')
    loadData()
  } catch (e) {
    console.error('[ItemManage] 下架失败:', e)
    Message.error('下架失败: ' + (e.message || '请检查权限或网络连接'))
  }
}

async function doDelete(record) {
  try {
    console.log('[ItemManage] 删除商品:', record.id)
    const res = await deleteItem(record.id)
    console.log('[ItemManage] 删除成功:', res)
    Message.success('商品已删除')
    loadData()
  } catch (e) {
    console.error('[ItemManage] 删除失败:', e)
    Message.error('删除失败: ' + (e.message || '请检查权限或网络连接'))
  }
}

function getFirstImage(record) {
  const urls = record.imageUrls || record.images || []
  if (typeof urls === 'string') { try { const p = JSON.parse(urls); return p[0] || '' } catch { return urls || '' } }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : ''
}

function getImages(record) {
  const urls = record.imageUrls || record.images || []
  if (typeof urls === 'string') { try { return JSON.parse(urls) } catch { return [urls] } }
  return Array.isArray(urls) ? urls : []
}

function formatPrice(price) { const n = Number(price); return isNaN(n) ? '0.00' : n.toFixed(2) }
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function getCategoryColor(category) {
  return { ELECTRONICS: 'blue', BOOKS: 'green', CLOTHING: 'purple', DAILY: 'cyan', SPORTS: 'orange', FOOD: 'lime', BEAUTY: 'pink', OTHER: 'gray' }[category] || 'gray'
}

function getCategoryLabel(category) {
  const cat = categories.value.find(c => c.value === category)
  return cat?.label || category
}

function getStatusColor(status) {
  return getEnumStatusColor(status, itemStatuses.value)
}

function getStatusLabel(status) {
  return getEnumStatusLabel(status, itemStatuses.value)
}

onMounted(async () => { await loadEnums(); await loadDict(); loadData() })
</script>

<style lang="scss" scoped>
.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  
  &:hover {
    color: #165DFF;
  }
}
</style>
