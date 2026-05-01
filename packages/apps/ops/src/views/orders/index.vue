<template>
  <div class="orders-page">
    <div class="orders-page__header">
      <h2>订单监控</h2>
    </div>

    <!-- 状态 Tab -->
    <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
      <a-tab-pane v-for="s in ORDER_STATUS_OPTIONS" :key="s.value" :title="s.label" />
    </a-tabs>

    <!-- 数据表格 -->
    <a-table
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :columns="ORDER_COLUMNS"
      row-key="id"
      @page-change="handlePageChange"
    >
      <template #status="{ record }">
        <a-tag :color="ORDER_STATUS_MAP[record.status]?.color || 'gray'" size="small">
          {{ ORDER_STATUS_MAP[record.status]?.label || record.status }}
        </a-tag>
      </template>
      <template #amount="{ record }">
        <span style="color: #f53f3f; font-weight: 600;">¥{{ record.amount || record.itemPrice }}</span>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { opsHttp as http } from "commonprovide/http";
import { ORDER_STATUS_OPTIONS, ORDER_STATUS_MAP, ORDER_COLUMNS } from "./const";

const tableData = ref([]);
const loading = ref(false);
const activeStatus = ref("");
const pagination = ref({ current: 1, pageSize: 10, total: 0, showTotal: true });

async function loadData() {
  loading.value = true;
  try {
    const params = {
      status: activeStatus.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    const res = await http.get("/ops/orders", { params });
    tableData.value = res?.orders || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[Orders] load error:", e);
  } finally {
    loading.value = false;
  }
}

function handleTabChange() {
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
.orders-page {
  background: var(--ops-bg-white);
  border-radius: var(--ops-radius-lg);
  padding: 24px;

  &__header {
    margin-bottom: 16px;
    h2 { margin: 0; font-size: 20px; font-weight: 700; color: var(--ops-text-1); }
  }
}
</style>
