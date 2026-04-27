<template>
  <a-card title="我的订单">
    <a-table :data="rows" :loading="loading" :pagination="false">
      <template #columns>
        <a-table-column title="订单号" data-index="id" />
        <a-table-column title="商品" data-index="itemTitle" />
        <a-table-column title="金额" data-index="amount" />
        <a-table-column title="状态" data-index="status" />
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { getMyOrders } from "../services/api";

const rows = ref([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const result = await getMyOrders();
    rows.value = result.orders || [];
  } catch (error) {
    Message.error(error.message || "加载订单失败");
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
