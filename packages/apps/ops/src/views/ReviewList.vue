<template>
  <a-card title="商品审核队列">
    <a-table :data="rows" :loading="loading" :pagination="false">
      <template #columns>
        <a-table-column title="商品ID" data-index="id" />
        <a-table-column title="商品名" data-index="title" />
        <a-table-column title="卖家" data-index="sellerName" />
        <a-table-column title="状态">
          <template #cell="{ record }">
            <StatusTag :status="record.reviewStatus" />
          </template>
        </a-table-column>
        <a-table-column title="操作">
          <template #cell="{ record }">
            <a-space>
              <a-button size="small" @click="$router.push(`/reviews/${record.id}`)">详情</a-button>
              <a-button size="small" type="primary" @click="approve(record.id)">通过</a-button>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { StatusTag } from "commonprovide/status-tag";
import { approveItem, getReviewQueue } from "../services/api";

const rows = ref([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const result = await getReviewQueue();
    rows.value = result.items || [];
  } catch (error) {
    Message.error(error.message || "加载审核队列失败");
  } finally {
    loading.value = false;
  }
}

async function approve(itemId) {
  try {
    await approveItem(itemId);
    Message.success("审核已通过");
    loadData();
  } catch (error) {
    Message.error(error.message || "操作失败");
  }
}

onMounted(loadData);
</script>
