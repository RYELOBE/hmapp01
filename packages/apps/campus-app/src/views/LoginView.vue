<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>CampusTrade</h1>
        <p>校园二手交易平台</p>
      </div>
      <a-card class="login-card">
        <template #title>欢迎登录</template>
        <a-form :model="form" @submit="handleSubmit">
          <a-form-item label="用户名" :required="true">
            <a-input
              v-model="form.username"
              placeholder="请输入用户名"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="密码" :required="true">
            <a-input-password
              v-model="form.password"
              placeholder="请输入密码"
              allow-clear
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" block :loading="loading">
              登录
            </a-button>
          </a-form-item>
        </a-form>
        <div class="login-footer">
          <a href="/register">注册账号</a>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const form = reactive({
  username: "",
  password: "",
});

async function handleSubmit() {
  if (!form.username || !form.password) {
    return;
  }
  loading.value = true;
  try {
    await authStore.login({
      username: form.username,
      password: form.password,
    });
    const redirect = router.currentRoute.value.query.redirect || "/portal/home";
    await router.push(redirect);
  } catch (error) {
    console.error("登录失败:", error);
    alert(error.response?.data?.message || "登录失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 32px;
  color: #fff;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.login-header p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>