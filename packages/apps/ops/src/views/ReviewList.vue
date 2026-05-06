<template>
  <div class="review-list-page">
    <div class="page-header">
      <h2 class="page-title">商品审核</h2>
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item>运营中心</a-breadcrumb-item>
        <a-breadcrumb-item>商品审核</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

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
          <span>待审核商品列表</span>
          <a-tag color="arcoblue" size="small" v-if="paginationConfig.total > 0">
            共 {{ paginationConfig.total }} 条
          </a-tag>
        </div>
      </template>

      <a-table
        :data="rows"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="(record) => record.id"
        :stripe="true"
        :scroll="{ x: 1200 }"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
        :row-selection="{ type: 'checkbox', showCheckedAll: true, onlyCurrent: false }"
      >
        <template #columns>
          <a-table-column title="缩略图" data-index="thumbnail" :width="80" align="center">
            <template #cell="{ record }">
              <a-image
                :src="getImageUrl(record)"
                width="50"
                height="50"
                fit="cover"
                style="border-radius: 4px;"
              />
            </template>
          </a-table-column>

          <a-table-column title="商品标题" data-index="title" :width="200" :ellipsis="true" :tooltip="true">
            <template #cell="{ record }">
              <a-typography-text style="font-weight: 500; color: #1d2129;">
                {{ record.title }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="价格" data-index="price" :width="100" align="right">
            <template #cell="{ record }">
              <a-typography-text type="danger" strong>
                ¥{{ formatPrice(record.price) }}
              </a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="卖家" data-index="sellerName" :width="120" />

          <a-table-column title="分类" data-index="category" :width="100">
            <template #cell="{ record }">
              <a-tag size="small">{{ record.category || '未分类' }}</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="成色" data-index="conditionLevel" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag size="small" :color="getConditionColor(record.conditionLevel)">
                {{ getConditionLabel(record.conditionLevel) }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="发布时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>

          <a-table-column title="状态" data-index="reviewStatus" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.reviewStatus)" size="small">
                {{ getStatusLabel(record.reviewStatus) }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="140" fixed="right" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  v-if="record.reviewStatus === 'PENDING_REVIEW'"
                  type="primary"
                  size="small"
                  @click="openReviewDrawer(record)"
                >
                  审核
                </a-button>
                <a-button
                  v-else
                  type="text"
                  size="small"
                  @click="openReviewDrawer(record)"
                >
                  查看
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <div v-if="selectedKeys.length > 0" class="batch-actions-bar">
        <a-space>
          <span class="batch-info">已选择 {{ selectedKeys.length }} 项</span>
          <a-button type="primary" status="success" size="small" @click="batchApprove" :loading="batchLoading">
            <template #icon><icon-check-circle /></template>
            批量通过
          </a-button>
          <a-button size="small" @click="clearSelection">
            取消选择
          </a-button>
        </a-space>
      </div>
    </a-card>

    <a-drawer
      v-model:visible="drawerVisible"
      :width="720"
      title="商品审核详情"
      placement="right"
      :footer="drawerFooter"
      unmount-on-close
    >
      <ReviewDetail
        v-if="currentItem"
        :item="currentItem"
        @approve="handleApproveSuccess"
        @reject="handleRejectSuccess"
      />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconRefresh,
  IconCheckCircle,
} from "@arco-design/web-vue/es/icon";
import FilterBar from "../components/FilterBar.vue";
import ReviewDetail from "./ReviewDetail.vue";
import { getReviewQueue, approveItem as apiApproveItem } from "../services/api";

const rows = ref([]);
const loading = ref(false);
const batchLoading = ref(false);
const drawerVisible = ref(false);
const currentItem = ref(null);
const selectedKeys = ref([]);

const filterParams = reactive({
  keyword: "",
  category: "",
  dateRange: [],
  status: "",
});

const filterConfig = [
  {
    field: "keyword",
    label: "关键词",
    type: "input",
    placeholder: "搜索商品标题/卖家名称",
    span: 6,
  },
  {
    field: "category",
    label: "分类",
    type: "select",
    placeholder: "全部分类",
    span: 5,
    options: [
      { value: "", label: "全部分类" },
      { value: "electronics", label: "电子产品" },
      { value: "books", label: "书籍教材" },
      { value: "clothing", label: "服装鞋帽" },
      { value: "furniture", label: "家具家电" },
      { value: "sports", label: "运动户外" },
      { value: "other", label: "其他" },
    ],
  },
  {
    field: "dateRange",
    label: "时间范围",
    type: "daterange",
    span: 8,
  },
  {
    field: "status",
    label: "审核状态",
    type: "select",
    placeholder: "全部状态",
    span: 5,
    options: [
      { value: "", label: "全部状态" },
      { value: "PENDING_REVIEW", label: "待审核" },
      { value: "APPROVED", label: "已通过" },
      { value: "REJECTED", label: "已拒绝" },
    ],
  },
];

const paginationConfig = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50],
});

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      const parsed = JSON.parse(urls);
      return parsed[0] || "";
    } catch {
      return urls || "";
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
}

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

function getStatusColor(status) {
  const colors = {
    PENDING_REVIEW: "orangered",
    APPROVED: "green",
    REJECTED: "red",
  };
  return colors[status] || "gray";
}

function getStatusLabel(status) {
  const labels = {
    PENDING_REVIEW: "待审核",
    APPROVED: "已通过",
    REJECTED: "已拒绝",
  };
  return labels[status] || status;
}

function getConditionColor(condition) {
  const colors = {
    NEW: "green",
    LIKE_NEW: "cyan",
    GOOD: "blue",
    FAIR: "orange",
    POOR: "red",
  };
  return colors[condition] || "gray";
}

function getConditionLabel(condition) {
  const labels = {
    NEW: "全新",
    LIKE_NEW: "几乎全新",
    GOOD: "良好",
    FAIR: "一般",
    POOR: "较差",
  };
  return labels[condition] || condition;
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
      keyword: filterParams.keyword || undefined,
      category: filterParams.category || undefined,
      status: filterParams.status || undefined,
    };

    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    const result = await getReviewQueue(params);
    rows.value = result.items || result.rows || [];
    paginationConfig.total = result.total || result.totalCount || 0;
  } catch (error) {
    Message.error(error.message || "加载审核列表失败");
    rows.value = [];
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  paginationConfig.current = 1;
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
  paginationConfig.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  paginationConfig.pageSize = size;
  paginationConfig.current = 1;
  loadData();
}

function handleSelectionChange(keys) {
  selectedKeys.value = keys;
}

function clearSelection() {
  selectedKeys.value = [];
}

function openReviewDrawer(record) {
  currentItem.value = record;
  drawerVisible.value = true;
}

async function batchApprove() {
  if (selectedKeys.value.length === 0) {
    Message.warning("请选择要审核的商品");
    return;
  }

  batchLoading.value = true;
  try {
    await Promise.all(selectedKeys.value.map((id) => apiApproveItem(id)));
    Message.success(`已批量通过 ${selectedKeys.value.length} 件商品`);
    selectedKeys.value = [];
    loadData();
  } catch (error) {
    Message.error(error.message || "批量操作失败");
  } finally {
    batchLoading.value = false;
  }
}

function handleApproveSuccess() {
  drawerVisible.value = false;
  loadData();
}

function handleRejectSuccess() {
  drawerVisible.value = false;
  loadData();
}

const drawerFooter = computed(() => ({
  bottom: "0",
  display: "flex",
  justifyContent: "flex-end",
  gap: "12px",
  padding: "16px 24px",
  borderTop: "1px solid #e5e6eb",
}));

onMounted(loadData);
</script>

<style lang="scss" scoped>
.review-list-page {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;

  .page-title {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 700;
    color: #1d2129;
  }

  .breadcrumb {
    font-size: 13px;
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

.batch-actions-bar {
  margin-top: 16px;
  padding: 12px 20px;
  background: #e6f1ff;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .batch-info {
    font-size: 14px;
    color: #165dff;
    font-weight: 500;
  }
}
</style>
