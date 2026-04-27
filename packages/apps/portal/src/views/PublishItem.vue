<template>
  <a-card title="发布商品">
    <a-form :model="form" layout="vertical">
      <a-form-item field="title" label="商品名称">
        <a-input v-model="form.title" placeholder="如：九成新机械键盘" />
      </a-form-item>
      <a-form-item field="price" label="价格">
        <a-input-number v-model="form.price" :min="1" />
      </a-form-item>
      <a-form-item field="description" label="描述">
        <a-textarea v-model="form.description" :auto-size="{ minRows: 3 }" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="submit">提交审核</a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { publishItem } from "../services/api";

const loading = ref(false);
const form = reactive({
  title: "",
  price: 100,
  description: ""
});

async function submit() {
  if (!form.title.trim()) {
    Message.warning("请输入商品名称");
    return;
  }
  loading.value = true;
  try {
    await publishItem(form);
    Message.success("发布成功，已进入待审核队列");
    form.title = "";
    form.price = 100;
    form.description = "";
  } catch (error) {
    Message.error(error.message || "提交失败");
  } finally {
    loading.value = false;
  }
}
</script>
