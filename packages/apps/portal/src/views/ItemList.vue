<template>
  <a-card title="商品浏览">
    <a-table :data="rows" :loading="loading" :pagination="false">
      <template #columns>
        <a-table-column title="商品ID" data-index="id" />
        <a-table-column title="商品名" data-index="title" />
        <a-table-column title="价格" data-index="price" />
        <a-table-column title="卖家" data-index="sellerName" />
        <a-table-column title="操作">
          <template #cell="{ record }">
            <a-button type="text" @click="$router.push(`/buyer/items/${record.id}`)">查看</a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { getItems } from "../services/api";

const rows = ref([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const result = await getItems({ approvedOnly: true });
    rows.value = result.items || [];
  } catch (error) {
    Message.error(error.message || "加载商品失败");
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
