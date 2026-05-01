<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <h2>用户注册</h2>
        <p class="subtitle">创建账号，开始校园交易之旅</p>
      </div>

      <a-form
        :model="form"
        :rules="rules"
        @submit="handleSubmit"
        layout="vertical"
        class="register-form"
      >
        <a-form-item label="用户名" field="username">
          <a-input
            v-model="form.username"
            placeholder="请输入用户名（3-20个字符）"
            :max-length="20"
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" field="password">
          <a-input-password
            v-model="form.password"
            placeholder="请输入密码（至少6位）"
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="确认密码" field="confirmPassword">
          <a-input-password
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="昵称" field="nickname">
          <a-input
            v-model="form.nickname"
            placeholder="请输入昵称（选填）"
            :max-length="20"
          >
            <template #prefix>
              <icon-edit />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="选择角色" field="roles">
          <div class="role-selector">
            <a-checkbox-group v-model="form.roles">
              <a-checkbox value="BUYER">
                <div class="role-option">
                  <icon-shopping-cart :size="24" />
                  <span>买家</span>
                  <span class="role-desc">浏览商品、购买下单</span>
                </div>
              </a-checkbox>
              <a-checkbox value="SELLER">
                <div class="role-option">
                  <icon-sell :size="24" />
                  <span>卖家</span>
                  <span class="role-desc">发布商品、管理销售</span>
                </div>
              </a-checkbox>
            </a-checkbox-group>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            long
            :loading="loading"
            :disabled="loading"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </a-button>
        </a-form-item>
      </a-form>

      <div class="card-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { register } from 'commonprovide/auth-sdk';

const router = useRouter();
const loading = ref(false);

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  roles: ['BUYER'],
});

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 3, message: '用户名至少3个字符' },
    { maxLength: 20, message: '用户名最多20个字符' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少6位' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (value, callback) => {
        if (value !== form.password) {
          callback('两次输入的密码不一致');
        } else {
          callback();
        }
      },
    },
  ],
  roles: [
    {
      validator: (value, callback) => {
        if (!value || value.length === 0) {
          callback('请至少选择一个角色');
        } else {
          callback();
        }
      },
    },
  ],
};

async function handleSubmit({ values, errors }) {
  if (errors) return;

  loading.value = true;
  try {
    const result = await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username,
      roles: form.roles,
    });

    Message.success('注册成功！');
    
    if (result.user.roles.includes('SELLER')) {
      router.push('/seller/items');
    } else {
      router.push('/buyer/home');
    }
  } catch (error) {
    Message.error(error.message || '注册失败，请重试');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.card-header .subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-form {
  margin-bottom: 24px;
}

.role-selector {
  width: 100%;
}

.role-selector :deep(.arco-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.role-option:hover {
  border-color: #165dff;
  background: #f0f5ff;
}

.role-option span:first-of-type {
  font-weight: 500;
  color: #333;
}

.role-desc {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.card-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.card-footer a {
  color: #165dff;
  margin-left: 4px;
  text-decoration: none;
}

.card-footer a:hover {
  text-decoration: underline;
}
</style>
