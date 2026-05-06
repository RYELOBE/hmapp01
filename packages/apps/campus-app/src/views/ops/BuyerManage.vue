<template>
  <div class="buyer-manage-page">
    <div class="page-header">
      <h2 class="page-title">需方（买家）管理</h2>
      <span class="page-desc">管理平台所有买方用户及其购买统计</span>
    </div>

    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="8" v-for="(stat, index) in statsCards" :key="index">
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

    <FilterBar
      :filters="filterConfig"
      v-model="filterParams"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #extra-buttons>
        <a-button @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </template>
    </FilterBar>

    <a-card :bordered="false" class="table-card">
      <template #title>
        <div class="card-title">
          <span>买家列表</span>
          <a-tag color="arcoblue" size="small" v-if="pagination.total > 0">
            共 {{ pagination.total }} 个买家
          </a-tag>
        </div>
      </template>

      <a-table
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        :stripe="true"
        :scroll="{ x: 1200 }"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="买家" data-index="username" :width="180" fixed="left">
            <template #cell="{ record }">
              <a-space>
                <a-avatar :size="32" :style="{ backgroundColor: '#722ed1' }">
                  {{ (record.username || '需')[0]?.toUpperCase() }}
                </a-avatar>
                <div>
                  <div style="font-weight: 500;">{{ record.username }}</div>
                  <div style="font-size: 12px; color: #86909c;">ID: {{ record.id }}</div>
                </div>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="累计订单数" data-index="totalOrders" :width="110" align="center">
            <template #cell="{ record }">
              <a-badge :text="record.totalOrders || 0" :color="record.totalOrders > 0 ? 'arcoblue' : 'gray'" />
            </template>
          </a-table-column>

          <a-table-column title="累计消费金额" data-index="totalSpent" :width="120" align="right">
            <template #cell="{ record }">
              <a-typography-text type="danger" strong style="font-size: 14px;">
                ¥{{ formatPrice(record.totalSpent) }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="已完成订单" data-index="completedOrders" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag color="green" size="small">{{ record.completedOrders || 0 }}</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="已取消订单" data-index="cancelledOrders" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag color="gray" size="small">{{ record.cancelledOrders || 0 }}</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="平均订单金额" data-index="avgOrderValue" :width="110" align="right">
            <template #cell="{ record }">
              ¥{{ formatPrice(record.avgOrderValue) }}
            </template>
          </a-table-column>

          <a-table-column title="注册时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="140" fixed="right" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
                <a-button type="text" size="small" status="danger" @click="disableBuyer(record)" v-if="record.status === 'ACTIVE'">禁用</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:visible="detailVisible"
      :title="`买家详情 - ${currentBuyer?.username || ''}`"
      :width="560"
      placement="right"
      unmount-on-close
    >
      <a-descriptions v-if="currentBuyer" :column="2" bordered size="medium">
        <a-descriptions-item label="用户ID" :span="2">{{ currentBuyer.id || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ currentBuyer.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentBuyer.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="订单总数">
          <a-badge :text="currentBuyer.totalOrders || 0" />
        </a-descriptions-item>
        <a-descriptions-item label="已完成">
          <a-tag color="green" size="small">{{ currentBuyer.completedOrders || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="已取消">{{ currentBuyer.cancelledOrders || 0 }}</a-descriptions-item>
        <a-descriptions-item label="总消费">
          <a-typography-text type="danger" strong style="font-size: 16px;">¥{{ formatPrice(currentBuyer.totalSpent) }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="平均订单金额">¥{{ formatPrice(currentBuyer.avgOrderValue) }}</a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">{{ formatDate(currentBuyer.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="最后购买" :span="2">{{ formatDate(currentBuyer.lastOrderAt) }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconList,
  IconFire,
  IconBarChartCircle,
  IconCalendar,
  IconRefresh,
} from "@arco-design/web-vue/es/icon";
import FilterBar from "../../ops-components/FilterBar.vue";
import { getBuyers, getBuyerDetail, updateUserStatus } from "../../services/api";

const tableData = ref([]);
const loading = ref(false);
const detailVisible = ref(false);
const currentBuyer = ref(null);
const stats = ref({});

const filterParams = reactive({
  keyword: "",
  dateRange: [],
});

const filterConfig = [
  {
    field: "keyword",
    label: "关键词",
    type: "input",
    placeholder: "搜索买家用户名/手机号",
    span: 10,
  },
  {
    field: "dateRange",
    label: "注册时间",
    type: "daterange",
    span: 14,
  },
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
  { key: "totalBuyers", title: "买家总数", icon: IconList, type: "primary" },
  { key: "todayNew", title: "今日新增", icon: IconFire, type: "success" },
  { key: "activeBuyers", title: "活跃买家（近30天）", icon: IconBarChartCircle, type: "warning" },
];

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadStats() {
  try {
    const res = await getBuyers({ stats: true });
    stats.value = res?.stats || {};
  } catch (e) {
    console.error("[BuyerManage] load stats error:", e);
  }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      keyword: filterParams.keyword || undefined,
    };

    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    const res = await getBuyers(params);
    tableData.value = res?.buyers || res?.rows || [];
    pagination.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[BuyerManage] load error:", e);
    Message.error("加载买家列表失败");
  } finally {
    loading.value = false;
  }
}

async function viewDetail(record) {
  try {
    const res = await getBuyerDetail(record.id);
    currentBuyer.value = res?.buyer || res || record;
    detailVisible.value = true;
  } catch (e) {
    console.error("[BuyerManage] view detail error:", e);
    Message.error("获取买家详情失败");
  }
}

function disableBuyer(record) {
  Modal.confirm({
    title: '禁用买家',
    content: `确定要禁用买家「${record.username}」吗？`,
    onOk: async () => {
      try {
        await updateUserStatus(record.id, 'DISABLED');
        Message.success('已禁用该买家');
        loadData();
      } catch (e) {
        Message.error('操作失败');
      }
    }
  });
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}

function handleReset() {
  Object.keys(filterParams).forEach((key) => {
    if (Array.isArray(filterParams[key])) {
      filterParams[key] = [];
    } else {
      filterParams[key] = "";
    }
  });
  handleSearch();
}

function handlePageChange(page) {
  pagination.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  pagination.pageSize = size;
  pagination.current = 1;
  loadData();
}

onMounted(() => {
  loadStats();
  loadData();
});
</script>

<style lang="scss" scoped>
.buyer-manage-page {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      margin: 0 0 4px 0;
      font-size: 20px;
      font-weight: 700;
      color: #1d2129;
    }

    .page-desc {
      font-size: 13px;
      color: #86909c;
    }
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  }

  &--primary {
    background: linear-gradient(135deg, #e6f1ff 0%, #ffffff 100%);
    border-left: 4px solid #165dff;
  }

  &--success {
    background: linear-gradient(135deg, #e8ffef 0%, #ffffff 100%);
    border-left: 4px solid #00b42a;
  }

  &--warning {
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    border-left: 4px solid #ff7d00;
  }

  :deep(.arco-card-body) {
    padding: 18px 20px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 8px;
    }

    .arco-statistic-value {
      font-size: 26px;
      font-weight: 700;
    }
  }

  .stat-icon {
    font-size: 22px;
    margin-right: 10px;

    .stat-card--primary & { color: #165dff; }
    .stat-card--success & { color: #00b42a; }
    .stat-card--warning & { color: #ff7d00; }
  }
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-head) {
    border-bottom: 1px solid #e5e6eb;
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 0;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}
</style>
