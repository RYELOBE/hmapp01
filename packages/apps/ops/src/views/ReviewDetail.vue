<template>
  <a-card title="审核详情">
    <a-descriptions v-if="item" :column="1" bordered>
      <a-descriptions-item label="商品名称">{{
        item.title
      }}</a-descriptions-item>
      <a-descriptions-item label="卖家">{{
        item.sellerName
      }}</a-descriptions-item>
      <a-descriptions-item label="价格">{{ item.price }}</a-descriptions-item>
      <a-descriptions-item label="描述">{{
        item.description
      }}</a-descriptions-item>
      <a-descriptions-item label="状态">
        <StatusTag :status="item.reviewStatus" />
      </a-descriptions-item>
    </a-descriptions>
    <a-space style="margin-top: 16px">
      <a-input v-model="reason" placeholder="驳回原因（可选）" />
      <a-button type="primary" @click="approve">审核通过</a-button>
      <a-button status="danger" @click="reject">驳回</a-button>
    </a-space>
  </a-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { StatusTag } from "commonprovide/status-tag";
import { approveItem, getItemDetail, rejectItem } from "../services/api";

const route = useRoute();
const router = useRouter();
const item = ref(null);
const reason = ref("");

async function loadData() {
  try {
    item.value = await getItemDetail(route.params.id);
  } catch (error) {
    Message.error(error.message || "加载详情失败");
  }
}

async function approve() {
  try {
    await approveItem(route.params.id);
    Message.success("已审核通过");
    router.push("/reviews");
  } catch (error) {
    Message.error(error.message || "操作失败");
  }
}

async function reject() {
  try {
    await rejectItem(route.params.id, reason.value || "不符合平台规范");
    Message.success("已驳回");
    router.push("/reviews");
  } catch (error) {
    Message.error(error.message || "操作失败");
  }
}

onMounted(loadData);
</script>
