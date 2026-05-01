<template>
  <div class="reviews-page">
    <div class="reviews-page__header">
      <h2>商品审核</h2>
    </div>

    <!-- 状态 Tab -->
    <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
      <a-tab-pane v-for="s in REVIEW_STATUS_OPTIONS" :key="s.value" :title="s.label" />
    </a-tabs>

    <!-- 数据表格 -->
    <a-table
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :columns="REVIEW_COLUMNS"
      row-key="id"
      @page-change="handlePageChange"
    >
      <template #reviewStatus="{ record }">
        <a-tag :color="REVIEW_STATUS_MAP[record.reviewStatus]?.color || 'gray'" size="small">
          {{ REVIEW_STATUS_MAP[record.reviewStatus]?.label || record.reviewStatus }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button size="small" @click="$router.push(`/ops/reviews/${record.id}`)">详情</a-button>
          <a-button v-if="record.reviewStatus === 'PENDING'" size="small" type="primary" @click="handleApprove(record)">
            通过
          </a-button>
          <a-button v-if="record.reviewStatus === 'PENDING'" size="small" status="danger" @click="openRejectModal(record)">
            驳回
          </a-button>
        </a-space>
      </template>
    </a-table>

    <!-- 驳回弹窗 -->
    <a-modal
      v-model:visible="rejectModalVisible"
      title="驳回商品"
      :ok-loading="rejecting"
      @ok="handleReject"
      @cancel="rejectModalVisible = false"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="驳回原因" field="reason" :rules="[{ required: true, message: '请输入驳回原因' }]">
          <a-textarea v-model="rejectForm.reason" :max-length="200" show-word-limit :auto-size="{ minRows: 3 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { http } from "commonprovide/http";
import { REVIEW_STATUS_OPTIONS, REVIEW_STATUS_MAP, REVIEW_COLUMNS } from "./const";

const tableData = ref([]);
const loading = ref(false);
const activeStatus = ref("");
const pagination = ref({ current: 1, pageSize: 10, total: 0, showTotal: true });

const rejectModalVisible = ref(false);
const rejecting = ref(false);
const rejectForm = ref({ reason: "" });
const currentRejectItem = ref(null);

async function loadData() {
  loading.value = true;
  try {
    const params = {
      status: activeStatus.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    const res = await http.get("/ops/reviews", { params });
    tableData.value = res?.items || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[Reviews] load error:", e);
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

async function handleApprove(record) {
  try {
    await http.post(`/ops/reviews/${record.id}/approve`);
    Message.success("审核已通过");
    loadData();
  } catch (e) {
    Message.error(e.message || "操作失败");
  }
}

function openRejectModal(record) {
  currentRejectItem.value = record;
  rejectForm.value.reason = "";
  rejectModalVisible.value = true;
}

async function handleReject() {
  if (!rejectForm.value.reason.trim()) {
    Message.warning("请输入驳回原因");
    return;
  }
  rejecting.value = true;
  try {
    await http.post(`/ops/reviews/${currentRejectItem.value.id}/reject`, {
      reason: rejectForm.value.reason,
    });
    Message.success("已驳回");
    rejectModalVisible.value = false;
    loadData();
  } catch (e) {
    Message.error(e.message || "操作失败");
  } finally {
    rejecting.value = false;
  }
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.reviews-page {
  background: var(--ops-bg-white);
  border-radius: var(--ops-radius-lg);
  padding: 24px;

  &__header {
    margin-bottom: 16px;
    h2 { margin: 0; font-size: 20px; font-weight: 700; color: var(--ops-text-1); }
  }
}
</style>
