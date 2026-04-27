<template>
  <a-form :model="form" layout="vertical">
    <a-form-item field="username" label="账号">
      <a-input v-model="form.username" placeholder="buyer / seller / ops" />
    </a-form-item>
    <a-form-item field="password" label="密码">
      <a-input-password v-model="form.password" placeholder="123456" />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" long :loading="loading" @click="submit">登录</a-button>
    </a-form-item>
  </a-form>
  <a-typography-text type="secondary">
    内置演示账号：buyer、seller、ops，密码均为 123456
  </a-typography-text>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { login } from "../auth-sdk.js";

const props = defineProps({
  loginConfig: {
    type: Object,
    default: () => ({})
  }
});

const emits = defineEmits(["login-success"]);

const loading = ref(false);
const form = reactive({
  username: "buyer",
  password: "123456"
});

async function submit() {
  loading.value = true;
  try {
    await login(form);
    Message.success("登录成功");
    emits("login-success");
  } catch (error) {
    Message.error(error.message || "登录失败");
  } finally {
    loading.value = false;
  }
}
</script>
