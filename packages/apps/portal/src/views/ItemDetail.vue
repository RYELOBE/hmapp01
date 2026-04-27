<template>
  <a-card title="商品详情">
    <a-descriptions v-if="detail" :column="1" bordered>
      <a-descriptions-item label="商品名">{{ detail.title }}</a-descriptions-item>
      <a-descriptions-item label="价格">{{ detail.price }}</a-descriptions-item>
      <a-descriptions-item label="描述">{{ detail.description }}</a-descriptions-item>
      <a-descriptions-item label="卖家">{{ detail.sellerName }}</a-descriptions-item>
    </a-descriptions>
    <a-empty v-else description="暂无数据" />
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { getItemDetail } from "../services/api";

const route = useRoute();
const detail = ref(null);

async function loadDetail() {
  try {
    detail.value = await getItemDetail(route.params.id);
  } catch (error) {
    Message.error(error.message || "加载详情失败");
  }
}

onMounted(loadDetail);
</script>
