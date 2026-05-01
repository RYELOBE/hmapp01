<template>
  <div class="buyer-page">
    <div class="buyer-page__header">
      <h2>需方管理</h2>
      <span class="buyer-page__desc">管理所有买方用户及其订单统计</span>
    </div>

    <!-- 搜索 -->
    <div class="buyer-page__filter">
      <a-input-search
        v-model="keyword"
        placeholder="搜索买家用户名"
        style="width: 280px"
        @search="handleSearch"
      />
    </div>

    <!-- 数据表格 -->
    <a-table
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :columns="BUYER_COLUMNS"
      row-key="username"
      @page-change="handlePageChange"
    >
      <template #username="{ record }">
        <a-space>
          <a-avatar :size="28">{{ record.username?.[0] || '需' }}</a-avatar>
          <span style="font-weight: 500;">{{ record.username }}</span>
        </a-space>
      </template>
      <template #totalSpent="{ record }">
        <span style="color: #f53f3f; font-weight: 600;">¥{{ record.totalSpent || 0 }}</span>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { http } from "commonprovide/http";
import { BUYER_COLUMNS } from "./const";

const tableData = ref([]);
const loading = ref(false);
const keyword = ref("");
const pagination = ref({ current: 1, pageSize: 10, total: 0, showTotal: true });

async function loadData() {
  loading.value = true;
  try {
    const params = {
      keyword: keyword.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    const res = await http.get("/ops/buyers", { params });
    tableData.value = res?.buyers || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[Buyer] load error:", e);
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.buyer-page {
  background: var(--ops-bg-white);
  border-radius: var(--ops-radius-lg);
  padding: 24px;

  &__header {
    margin-bottom: 16px;
    h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; color: var(--ops-text-1); }
  }

  &__desc { font-size: 13px; color: var(--ops-text-3); }

  &__filter {
    margin-bottom: 16px;
  }
}
</style>
