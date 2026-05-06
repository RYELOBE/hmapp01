<template>
  <div class="vendor-manage-page">
    <div class="page-header">
      <h2 class="page-title">供方（卖家）管理</h2>
      <span class="page-desc">管理平台所有卖方用户及其商品销售统计</span>
    </div>

    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="12" :sm="6" v-for="(stat, index) in statsCards" :key="index">
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
          <span>卖家列表</span>
          <a-tag color="arcoblue" size="small" v-if="pagination.total > 0">
            共 {{ pagination.total }} 个卖家
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
          <a-table-column title="卖家" data-index="username" :width="180" fixed="left">
            <template #cell="{ record }">
              <a-space>
                <a-avatar :size="32" :style="{ backgroundColor: '#00b42a' }">
                  {{ (record.username || '供')[0]?.toUpperCase() }}
                </a-avatar>
                <div>
                  <div style="font-weight: 500;">{{ record.username }}</div>
                  <div style="font-size: 12px; color: #86909c;">ID: {{ record.id }}</div>
                </div>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="商品总数" data-index="totalItems" :width="100" align="center">
            <template #cell="{ record }">
              <a-badge :text="record.totalItems || 0" :color="record.totalItems > 0 ? 'green' : 'gray'" />
            </template>
          </a-table-column>

          <a-table-column title="在售数" data-index="activeItems" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag color="green" size="small">{{ record.activeItems || 0 }}</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="已售数" data-index="soldItems" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag color="arcoblue" size="small">{{ record.soldItems || 0 }}</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="待审核" data-index="pendingItems" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.pendingItems > 0" color="orangered" size="small">{{ record.pendingItems }}</a-tag>
              <span v-else style="color: #86909c;">0</span>
            </template>
          </a-table-column>

          <a-table-column title="总销售额" data-index="salesAmount" :width="120" align="right">
            <template #cell="{ record }">
              <a-typography-text type="danger" strong style="font-size: 14px;">
                ¥{{ formatPrice(record.salesAmount) }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="注册时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="180" fixed="right" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
                <a-button type="text" size="small" @click="viewShop(record)">查看店铺</a-button>
                <a-button type="text" size="small" status="danger" @click="disableVendor(record)" v-if="record.status === 'ACTIVE'">禁用</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:visible="detailVisible"
      :title="`卖家详情 - ${currentVendor?.username || ''}`"
      :width="560"
      placement="right"
      unmount-on-close
    >
      <a-descriptions v-if="currentVendor" :column="2" bordered size="medium">
        <a-descriptions-item label="用户ID" :span="2">{{ currentVendor.id || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ currentVendor.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentVendor.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="商品总数">{{ currentVendor.totalItems || 0 }}</a-descriptions-item>
        <a-descriptions-item label="在售数">
          <a-tag color="green" size="small">{{ currentVendor.activeItems || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="已售数">
          <a-tag color="arcoblue" size="small">{{ currentVendor.soldItems || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="待审核">
          <a-tag color="orangered" size="small">{{ currentVendor.pendingItems || 0 }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="总销售额" :span="2">
          <a-typography-text type="danger" strong style="font-size: 16px;">
            ¥{{ formatPrice(currentVendor.salesAmount) }}
          </a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">{{ formatDate(currentVendor.createdAt) }}</a-descriptions-item>
      </a-descriptions>

      <a-divider>商品列表</a-divider>

      <a-table
        v-if="vendorItems.length > 0"
        :data="vendorItems"
        :pagination="false"
        size="small"
        :scroll="{ x: 400 }"
      >
        <template #columns>
          <a-table-column title="商品名" data-index="title" :ellipsis="true" :tooltip="true" />
          <a-table-column title="价格" data-index="price" :width="80">
            <template #cell="{ record }">
              <span style="color: #f53f3f;">¥{{ formatPrice(record.price) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="reviewStatus" :width="80">
            <template #cell="{ record }">
              <a-tag
                :color="record.reviewStatus === 'APPROVED' ? 'green' : (record.reviewStatus === 'PENDING' ? 'orangered' : 'gray')"
                size="small"
              >
                {{ record.reviewStatus === 'APPROVED' ? '已通过' : (record.reviewStatus === 'PENDING' ? '待审核' : record.reviewStatus) }}
              </a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-else description="暂无商品" />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconUserGroup,
  IconFire,
  IconApps,
  IconClockCircle,
  IconRefresh,
} from "@arco-design/web-vue/es/icon";
import FilterBar from "../../ops-components/FilterBar.vue";
import { getVendors, getVendorDetail, updateUserStatus } from "../../services/api";

const tableData = ref([]);
const loading = ref(false);
const detailVisible = ref(false);
const currentVendor = ref(null);
const vendorItems = ref([]);
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
    placeholder: "搜索卖家用户名/手机号",
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
  { key: "totalVendors", title: "卖家总数", icon: IconUserGroup, type: "primary" },
  { key: "todayNew", title: "今日新增", icon: IconFire, type: "success" },
  { key: "totalActiveItems", title: "在售商品数", icon: IconApps, type: "warning" },
  { key: "pendingReviewItems", title: "待审核商品", icon: IconClockCircle, type: "danger" },
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
    const res = await getVendors({ stats: true });
    stats.value = res?.stats || {};
  } catch (e) {
    console.error("[VendorManage] load stats error:", e);
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

    const res = await getVendors(params);
    tableData.value = res?.vendors || res?.rows || [];
    pagination.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[VendorManage] load error:", e);
    Message.error("加载卖家列表失败");
  } finally {
    loading.value = false;
  }
}

async function viewDetail(record) {
  try {
    const res = await getVendorDetail(record.id);
    currentVendor.value = res?.vendor || res || record;
    vendorItems.value = res?.items || [];
    detailVisible.value = true;
  } catch (e) {
    console.error("[VendorManage] view detail error:", e);
    Message.error("获取卖家详情失败");
  }
}

function viewShop(record) {
  Message.info(`查看 ${record.username} 的店铺`);
}

function disableVendor(record) {
  Modal.confirm({
    title: '禁用卖家',
    content: `确定要禁用卖家「${record.username}」吗？禁用后其所有商品将下架。`,
    onOk: async () => {
      try {
        await updateUserStatus(record.id, 'DISABLED');
        Message.success('已禁用该卖家');
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
.vendor-manage-page {
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

  &--danger {
    background: linear-gradient(135deg, #ffece8 0%, #ffffff 100%);
    border-left: 4px solid #f53f3f;
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
    .stat-card--danger & { color: #f53f3f; }
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
