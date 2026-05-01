<template>
  <div class="review-list-page">
    <a-card :bordered="false" class="list-card">
      <template #title>
        <div class="card-title">
          <icon-check-circle-rejected class="title-icon" />
          <span>商品审核队列</span>
        </div>
      </template>
      
      <SearchFilter
        v-model="filterParams"
        :filter-items="filterItems"
        @search="handleSearch"
        @reset="handleReset"
      />

      <a-table
        :data="rows"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="(record) => record.id"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
      >
        <template #columns>
          <a-table-column title="商品ID" data-index="id" :width="80" align="center" />
          <a-table-column title="商品信息" :width="280">
            <template #cell="{ record }">
              <div class="item-cell">
                <a-image
                  :src="getImageUrl(record)"
                  width="56"
                  height="56"
                  fit="cover"
                  style="border-radius: 6px;"
                />
                <div class="item-cell__info">
                  <a-typography-text class="item-cell__title" ellipsis>
                    {{ record.title }}
                  </a-typography-text>
                  <a-typography-text type="secondary" class="item-cell__price">
                    ¥{{ record.price }}
                  </a-typography-text>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="卖家" data-index="sellerName" :width="120" />
          <a-table-column title="分类" data-index="category" :width="100" />
          <a-table-column title="审核状态" :width="100" align="center">
            <template #cell="{ record }">
              <StatusTag :status="record.reviewStatus" />
            </template>
          </a-table-column>
          <a-table-column title="提交时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="200" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="viewDetail(record)">
                  <template #icon><icon-eye /></template>
                  查看
                </a-button>
                <a-button
                  type="primary"
                  status="success"
                  size="small"
                  :disabled="record.reviewStatus !== 'PENDING_REVIEW'"
                  @click="approveItem(record)"
                >
                  <template #icon><icon-check /></template>
                  通过
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <div v-if="selectedKeys.length > 0" class="batch-actions">
        <a-divider orientation="center">批量操作</a-divider>
        <a-space>
          <a-button type="primary" status="success" @click="batchApprove">
            <template #icon><icon-check-circle /></template>
            批量通过 ({{ selectedKeys.length }})
          </a-button>
        </a-space>
      </div>
    </a-card>

    <a-drawer
      v-model:visible="drawerVisible"
      :width="600"
      title="商品详情"
      :footer="itemDetail ? drawerFooter : null"
      unmount-on-close
    >
      <ReviewDetail
        v-if="itemDetail"
        :item="itemDetail"
        @approve="handleApproveSuccess"
        @reject="handleRejectSuccess"
      />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconEye, IconCheck, IconCheckCircle, IconCheckCircleRejected } from "@arco-design/web-vue/es/icon";
import { StatusTag } from "commonprovide/status-tag";
import { SearchFilter } from "commonprovide/SearchFilter";
import { approveItem as apiApproveItem, rejectItem, getReviewQueue } from "../services/api";
import ReviewDetail from "./ReviewDetail.vue";

const router = useRouter();

const rows = ref([]);
const loading = ref(false);
const drawerVisible = ref(false);
const itemDetail = ref(null);
const selectedKeys = ref([]);

const filterParams = reactive({
  keyword: "",
  status: "",
});

const filterItems = [
  {
    type: "input",
    field: "keyword",
    placeholder: "搜索商品名称",
  },
  {
    type: "select",
    field: "status",
    label: "审核状态",
    placeholder: "全部状态",
    options: [
      { value: "", label: "全部状态" },
      { value: "PENDING_REVIEW", label: "待审核" },
      { value: "APPROVED", label: "已通过" },
      { value: "REJECTED", label: "已驳回" },
    ],
  },
];

const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
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

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
      keyword: filterParams.keyword || undefined,
      status: filterParams.status || undefined,
    };
    const result = await getReviewQueue(params);
    rows.value = result.items || result.rows || [];
    paginationConfig.total = result.total || result.totalCount || 0;
  } catch (error) {
    Message.error(error.message || "加载审核队列失败");
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
  filterParams.keyword = "";
  filterParams.status = "";
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

function viewDetail(record) {
  itemDetail.value = record;
  drawerVisible.value = true;
}

const drawerFooter = computed(() => ({
  bottom: "0",
  display: "flex",
  justifyContent: "flex-end",
  gap: "12px",
}));

async function approveItem(record) {
  try {
    await apiApproveItem(record.id);
    Message.success("审核已通过");
    loadData();
  } catch (error) {
    Message.error(error.message || "操作失败");
  }
}

async function batchApprove() {
  if (selectedKeys.value.length === 0) {
    Message.warning("请选择要审核的商品");
    return;
  }
  try {
    await Promise.all(selectedKeys.value.map(id => apiApproveItem(id)));
    Message.success(`已通过 ${selectedKeys.value.length} 件商品`);
    selectedKeys.value = [];
    loadData();
  } catch (error) {
    Message.error(error.message || "批量操作失败");
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

onMounted(loadData);
</script>

<style lang="scss" scoped>
.review-list-page {
  padding: 0;
}

.list-card {
  :deep(.arco-card-head) {
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;

  .title-icon {
    font-size: 20px;
    color: #165dff;
  }
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 180px;
  }

  &__title {
    font-size: 13px;
    color: #1d2129;
  }

  &__price {
    font-size: 14px;
    font-weight: 600;
    color: #ff4d4f !important;
  }
}

.batch-actions {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
</style>
