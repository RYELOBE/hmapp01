<template>
  <div class="vendor-page">
    <div class="vendor-page__header">
      <h2>供方管理</h2>
      <span class="vendor-page__desc">管理所有卖方用户及其商品统计</span>
    </div>

    <!-- 搜索 -->
    <div class="vendor-page__filter">
      <a-input-search
        v-model="keyword"
        placeholder="搜索卖家用户名"
        style="width: 280px"
        @search="handleSearch"
      />
    </div>

    <!-- 数据表格 -->
    <a-table
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :columns="VENDOR_COLUMNS"
      row-key="username"
      @page-change="handlePageChange"
    >
      <template #username="{ record }">
        <a-space>
          <a-avatar :size="28">{{ record.username?.[0] || '供' }}</a-avatar>
          <span style="font-weight: 500;">{{ record.username }}</span>
        </a-space>
      </template>
      <template #activeItems="{ record }">
        <a-tag color="green" size="small">{{ record.activeItems }}</a-tag>
      </template>
      <template #pendingItems="{ record }">
        <a-tag v-if="record.pendingItems > 0" color="orange" size="small">{{ record.pendingItems }}</a-tag>
        <span v-else style="color: #c9cdd4;">0</span>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { opsHttp as http } from "../../../services/http";
import { VENDOR_COLUMNS } from "./const";

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
    const res = await http.get("/ops/vendors", { params });
    const data = res?.data || res;
    tableData.value = data?.vendors || data?.rows || [];
    pagination.value.total = data?.totalCount ?? data?.total ?? 0;
  } catch (e) {
    console.error("[Vendor] load error:", e);
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
.vendor-page {
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
