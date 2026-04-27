<template>
  <a-card title="我的商品">
    <a-table :data="rows" :loading="loading" :pagination="false">
      <template #columns>
        <a-table-column title="商品名" data-index="title" />
        <a-table-column title="价格" data-index="price" />
        <a-table-column title="审核状态">
          <template #cell="{ record }">
            <StatusTag :status="record.reviewStatus" />
          </template>
        </a-table-column>
        <a-table-column title="驳回原因" data-index="rejectReason" />
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { StatusTag } from "commonprovide/status-tag";
import { getItems } from "../services/api";

const rows = ref([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const result = await getItems({ mine: true });
    rows.value = result.items || [];
  } catch (error) {
    Message.error(error.message || "加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
