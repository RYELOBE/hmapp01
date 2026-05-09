<template>
  <div class="orders-page">
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6" v-for="(stat, index) in statsCards" :key="index">
        <a-card :bordered="false" class="stat-card" :class="`stat-card--${stat.type}`">
          <a-statistic
            :title="stat.title"
            :value="stats[stat.key] ?? 0"
            :value-from="0"
            :duration="600"
            :precision="stat.precision || 0"
          >
            <template #prefix>
              <component :is="stat.icon" class="stat-icon" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 统一表格 -->
    <OpsUnifiedTable
      title="订单列表"
      :data="tableData"
      :columns="tableColumns"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      :stripe="true"
      @page-change="handlePageChange"
    >
      <template #actions>
        <a-input-search
          v-model="filterParams.keyword"
          placeholder="搜索订单号/买家/卖家/商品名"
          style="width: 260px"
          search-button
          @search="handleSearch"
          allow-clear
        />
        <a-select v-model="filterParams.status" placeholder="订单状态" style="width: 140px" allow-clear @change="handleSearch">
          <a-option v-for="opt in ORDER_STATUS_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</a-option>
        </a-select>
      </template>

      <template #extra>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-tag color="arcoblue" size="small" v-if="pagination.total > 0">共 {{ pagination.total }} 条</a-tag>
      </template>

      <!-- 列插槽 -->
      <template #orderNo="{ record }">
        <a-typography-text code copyable style="font-size:13px">{{ record.orderNo || record.id }}</a-typography-text>
      </template>

      <template #itemInfo="{ record }">
        <div class="order-item-cell">
          <a-image v-if="getFirstImage(record)" :src="getFirstImage(record)" width="48" height="48" fit="cover" style="border-radius:4px" />
          <div v-else class="item-placeholder"><icon-image /></div>
          <div class="item-info">
            <a-typography-text ellipsis :style="{ fontWeight:500, fontSize:'13px' }">{{ record.itemTitle || record.title || '商品' }}</a-typography-text>
            <a-typography-text type="secondary" style="font-size:12px">¥{{ formatPrice(record.price) }}</a-typography-text>
          </div>
        </div>
      </template>

      <template #amount="{ record }">
        <a-typography-text type="danger" strong style="font-size:14px">¥{{ formatPrice(record.totalAmount || record.amount || record.itemPrice || record.price) }}</a-typography-text>
      </template>

      <template #status="{ record }">
        <a-tag :color="ORDER_STATUS_MAP[record.status]?.color || 'gray'" size="small">{{ ORDER_STATUS_MAP[record.status]?.label || record.status }}</a-tag>
      </template>

      <template #createdAt="{ record }">
        {{ formatDate(record.createdAt) }}
      </template>

      <template #operations="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
          <a-popconfirm content="确定要删除该订单吗？" @ok="deleteOrder(record)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </OpsUnifiedTable>

    <!-- 详情抽屉 -->
    <a-drawer v-model:visible="drawerVisible" :width="640" title="订单详情" placement="right" unmount-on-close>
      <a-descriptions v-if="currentOrder" :column="2" bordered size="medium">
        <a-descriptions-item label="订单号" :span="2">
          <a-typography-text code>{{ currentOrder.orderNo || currentOrder.id }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="商品名称" :span="2">{{ currentOrder.itemTitle || currentOrder.title }}</a-descriptions-item>
        <a-descriptions-item label="商品价格">¥{{ formatPrice(currentOrder.price || currentOrder.itemPrice) }}</a-descriptions-item>
        <a-descriptions-item label="订单金额">
          <a-typography-text type="danger" strong>¥{{ formatPrice(currentOrder.totalAmount || currentOrder.amount) }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="买家">{{ currentOrder.buyerName || currentOrder.userName }}</a-descriptions-item>
        <a-descriptions-item label="卖家">{{ currentOrder.sellerName }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="ORDER_STATUS_MAP[currentOrder.status]?.color || 'gray'" size="small">{{ ORDER_STATUS_MAP[currentOrder.status]?.label || currentOrder.status }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="下单时间">{{ formatDate(currentOrder.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress || currentOrder.shippingAddress || currentOrder.address || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconSafe,
  IconCheckCircle,
  IconBarChart,
  IconClockCircle,
  IconImage,
} from "@arco-design/web-vue/es/icon";
import OpsUnifiedTable from "../../../components/ops/OpsUnifiedTable.vue";
import { opsHttp as http } from "../../../services/http";
import { ORDER_STATUS_OPTIONS, ORDER_STATUS_MAP } from "./const";

const tableData = ref([]);
const loading = ref(false);
const drawerVisible = ref(false);
const currentOrder = ref(null);
const stats = ref({});

const filterParams = reactive({
  keyword: "",
  status: "",
});

// 表格列定义 (对齐 operation-portal 格式)
const tableColumns = [
  { title: '订单号', dataIndex: 'orderNo', width: 160, slotName: 'orderNo' },
  { title: '商品信息', dataIndex: 'itemInfo', width: 250, slotName: 'itemInfo' },
  { title: '买家', dataIndex: 'buyerName', width: 120 },
  { title: '卖家', dataIndex: 'sellerName', width: 120 },
  { title: '金额', dataIndex: 'amount', width: 130, slotName: 'amount' },
  { title: '状态', dataIndex: 'status', width: 110, slotName: 'status' },
  { title: '下单时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '操作', width: 150, fixed: 'right', align: 'center', slotName: 'operations' }
];

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
});

const statsCards = [
  { key: "totalOrders", title: "总订单数", icon: IconSafe, type: "primary" },
  { key: "todayNew", title: "今日新增", icon: IconCheckCircle, type: "success" },
  { key: "totalAmount", title: "交易总额", icon: IconBarChart, type: "warning", precision: 2 },
  { key: "refundCount", title: "退款订单数", icon: IconClockCircle, type: "danger" },
];

function getFirstImage(record) {
  const urls = record.itemImage || record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try { const parsed = JSON.parse(urls); return parsed[0] || ""; }
    catch { return urls || ""; }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
}

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleString("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" });
}

async function loadStats() {
  try {
    const res = await http.get("/ops/statistics");
    const data = res?.data?.statistics || res?.data || res || {};
    stats.value = {
      totalOrders: data.totalOrders || 0,
      todayNew: data.todayNewOrders || data.todayOrders || 0,
      totalAmount: data.totalAmount || data.completedOrderAmount || 0,
      refundCount: data.refundCount || 0,
    };
  } catch (e) { console.error("[Orders] load stats error:", e); }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      status: filterParams.status || undefined,
      keyword: filterParams.keyword || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await http.post("/ops/orders", params);
    const data = res?.data || res;
    tableData.value = data?.orders || data?.rows || [];
    pagination.total = data?.totalCount ?? data?.total ?? 0;
  } catch (e) {
    console.error("[Orders] load error:", e);
    Message.error(e.message || "加载订单列表失败");
  } finally {
    loading.value = false;
  }
}

async function deleteOrder(record) {
  Message.info('暂不支持删除订单')
}

function handleSearch() { pagination.current = 1; loadData(); }

function handleReset() {
  filterParams.keyword = "";
  filterParams.status = "";
  handleSearch();
}

function handlePageChange(page) { pagination.current = page; loadData(); }

function viewDetail(record) { currentOrder.value = record; drawerVisible.value = true; }

onMounted(() => { loadStats(); loadData(); });
</script>

<style lang="scss" scoped>
.orders-page {
  background: #fff;
  min-height: 100%;
  padding: 16px 20px;

  .stats-row { margin-bottom: 20px; }
}

.stat-card {
  border-radius: 8px;
  transition: transform .2s, box-shadow .2s;

  &:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,.08); }

  &--primary { background: linear-gradient(135deg,#e6f1ff 0%,#fff 100%); border-left: 4px solid #165dff; }
  &--success { background: linear-gradient(135deg,#e8ffef 0%,#fff 100%); border-left: 4px solid #00b42a; }
  &--warning { background: linear-gradient(135deg,#fff7e6 0%,#fff 100%); border-left: 4px solid #ff7d00; }
  &--danger  { background: linear-gradient(135deg,#ffece8 0%,#fff 100%); border-left: 4px solid #f53f3f; }

  :deep(.arco-card-body) { padding: 18px 20px; }
  :deep(.arco-statistic-title) { font-size: 13px; color: #86909c; margin-bottom: 8px; }
  :deep(.arco-statistic-value) { font-size: 26px; font-weight: 700; }
  .stat-icon { font-size: 22px; margin-right: 10px; }
}

.order-item-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  .item-placeholder {
    width: 48px; height: 48px;
    display: flex; align-items: center; justify-content: center;
    background: #f7f8fa; border-radius: 4px; color: #c9cdd4;
  }

  .item-info {
    display: flex; flex-direction: column; gap: 4px;
    max-width: 180px;
  }
}
</style>
