<template>
  <div class="register-page">
    <div class="register-card">
      <!-- Step 1: 基础信息 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="card-header">
          <h2>创建账号</h2>
          <p class="subtitle">填写您的基本信息</p>
        </div>

        <a-steps :current="0" class="steps-indicator">
          <a-step title="基本信息" />
          <a-step title="选择角色" />
          <a-step title="完成注册" />
        </a-steps>

        <a-card title="基本信息" class="form-card">
          <a-form
            :model="form"
            :rules="rules"
            ref="formRef1"
            layout="vertical"
            @submit-success="goToStep2"
          >
            <a-form-item label="用户名" field="username">
              <a-input
                v-model="form.username"
                placeholder="请输入用户名"
                :max-length="20"
                allow-clear
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
                allow-clear
                @input="checkPasswordStrength"
              >
                <template #prefix>
                  <icon-lock />
                </template>
              </a-input-password>
              <div v-if="form.password" class="password-strength">
                <div class="strength-bar">
                  <div
                    class="strength-fill"
                    :class="`strength-${passwordStrength.level}`"
                    :style="{ width: passwordStrength.percent + '%' }"
                  ></div>
                </div>
                <span class="strength-text" :class="`strength-text--${passwordStrength.level}`">
                  {{ passwordStrength.text }}
                </span>
              </div>
            </a-form-item>

            <a-form-item label="确认密码" field="confirmPassword">
              <a-input-password
                v-model="form.confirmPassword"
                placeholder="请再次输入密码"
                allow-clear
              >
                <template #prefix>
                  <icon-lock />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item label="手机号" field="phone">
              <a-input
                v-model="form.phone"
                placeholder="请输入手机号（选填）"
                :max-length="11"
                allow-clear
              >
                <template #prefix>
                  <icon-phone />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="邮箱" field="email">
              <a-input
                v-model="form.email"
                placeholder="请输入邮箱（选填）"
                allow-clear
              >
                <template #prefix>
                  <icon-email />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item>
              <a-button type="primary" long size="large" html-type="submit">
                下一步
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </div>

      <!-- Step 2: 角色选择 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="card-header">
          <h2>选择您的角色</h2>
          <p class="subtitle">选择您想要使用的功能角色</p>
        </div>

        <a-steps :current="1" class="steps-indicator">
          <a-step title="基本信息" />
          <a-step title="选择角色" />
          <a-step title="完成注册" />
        </a-steps>

        <a-card title="选择您的角色" class="form-card">
          <div class="role-cards-grid">
            <div
              class="role-card"
              :class="{ 'role-card--selected': selectedRoles.includes('BUYER') }"
              @click="toggleRole('BUYER')"
            >
              <div class="role-icon role-icon--buyer">🛍️</div>
              <div class="role-title">我是买家</div>
              <div class="role-desc">浏览商品、下单购买、管理订单</div>
              <icon-check-circle-fill v-if="selectedRoles.includes('BUYER')" class="role-check" />
            </div>

            <div
              class="role-card"
              :class="{ 'role-card--selected': selectedRoles.includes('SELLER') }"
              @click="toggleRole('SELLER')"
            >
              <div class="role-icon role-icon--seller">📦</div>
              <div class="role-title">我是卖家</div>
              <div class="role-desc">发布商品、管理库存、处理订单</div>
              <icon-check-circle-fill v-if="selectedRoles.includes('SELLER')" class="role-check" />
            </div>
          </div>

          <div class="role-tip">
            <icon-info-circle /> 您可以选择多个角色，后续也可申请开通新角色
          </div>

          <div class="step-buttons">
            <a-button size="large" @click="currentStep = 1">
              上一步
            </a-button>
            <a-button type="primary" size="long" :loading="loading" :disabled="selectedRoles.length === 0" @click="handleRegister">
              {{ loading ? '注册中...' : '立即注册' }}
            </a-button>
          </div>
        </a-card>
      </div>

      <!-- Step 3: 注册成功 -->
      <div v-if="currentStep === 3" class="step-content step-success">
        <div class="success-icon">
          <icon-check-circle-fill :size="48" style="color: #00B42A;" />
        </div>
        <h2 class="success-title">🎉 注册成功！</h2>
        <p class="success-desc">您的账号已创建，即将跳转到首页...</p>
        <div class="countdown">{{ countdown }}秒后自动跳转</div>

        <div class="success-buttons">
          <a-button type="primary" size="large" @click="goToLogin">
            立即登录
          </a-button>
          <a-button size="large" @click="goToProfile">
            完善个人信息
          </a-button>
        </div>
      </div>

      <!-- 底部链接 (仅步骤1和2显示) -->
      <div v-if="currentStep < 3" class="card-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconUser,
  IconLock,
  IconMobile,
  IconEmail,
  IconCheckCircleFill,
  IconInfoCircle,
} from '@arco-design/web-vue/es/icon';
import { register, login } from '../services/api';

const router = useRouter();
const loading = ref(false);
const currentStep = ref(1);
const formRef1 = ref(null);
const countdown = ref(3);
let timer = null;

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
});

const selectedRoles = ref(['BUYER']);

const passwordStrength = computed(() => {
  const pwd = form.password;
  if (!pwd) return { level: '', text: '', percent: 0 };

  let score = 0;
  if (pwd.length >= 6) score++;
  if (pwd.length >= 10) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;

  if (score <= 2) return { level: 'weak', text: '弱', percent: 33 };
  if (score <= 3) return { level: 'medium', text: '中', percent: 66 };
  return { level: 'strong', text: '强', percent: 100 };
});

function checkPasswordStrength() {
}

function toggleRole(role) {
  const index = selectedRoles.value.indexOf(role);
  if (index > -1) {
    if (selectedRoles.value.length > 1) {
      selectedRoles.value.splice(index, 1);
    }
  } else {
    selectedRoles.value.push(role);
  }
}

async function goToStep2() {
  try {
    await formRef1.value?.validate();
    currentStep.value = 2;
  } catch (e) {
  }
}

async function handleRegister() {
  if (selectedRoles.value.length === 0) {
    Message.warning('请至少选择一个角色');
    return;
  }

  loading.value = true;
  try {
    const result = await register({
      username: form.username,
      password: form.password,
      nickname: form.username,
      phone: form.phone || undefined,
      email: form.email || undefined,
      roles: selectedRoles.value,
    });

    Message.success('注册成功！');

    try {
      const loginRes = await login({
        username: form.username,
        password: form.password,
      });
      Message.success('自动登录成功');
    } catch (loginErr) {
      console.warn('自动登录失败，请手动登录:', loginErr.message);
    }

    currentStep.value = 3;
    startCountdown();
  } catch (error) {
    Message.error(error.message || '注册失败，请重试');
  } finally {
    loading.value = false;
  }
}

function startCountdown() {
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      router.push('/home');
    }
  }, 1000);
}

function goToLogin() {
  clearInterval(timer);
  router.push('/home');
}

function goToProfile() {
  clearInterval(timer);
  Message.info('完善个人信息功能开发中');
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 3, message: '用户名至少3个字符' },
    { maxLength: 20, message: '用户名最多20个字符' },
    {
      validator: async (value, callback) => {
        if (value && value.length >= 3) {
          try {
            const isUnique = await checkUsernameUnique(value);
            if (!isUnique) {
              callback('该用户名已被占用');
            }
          } catch (e) {
          }
        }
        callback();
      },
    },
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
  phone: [
    {
      match: /^1[3-9]\d{9}$/,
      message: '请输入正确的11位手机号',
    },
  ],
  email: [
    {
      match: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
      message: '请输入正确的邮箱格式',
    },
  ],
};
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
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.card-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  margin: 0 0 8px 0;
}

.subtitle {
  color: var(--color-text-3, #86909C);
  font-size: 14px;
  margin: 0;
}

.steps-indicator {
  margin-bottom: 24px;
}

.form-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--color-fill-2, #E5E6EB);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-fill.strength-weak {
  background: #F53F3F;
}

.strength-fill.strength-medium {
  background: #FF7D00;
}

.strength-fill.strength-strong {
  background: #00B42A;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
  min-width: 24px;
}

.strength-text--weak {
  color: #F53F3F;
}

.strength-text--medium {
  color: #FF7D00;
}

.strength-text--strong {
  color: #00B42A;
}

.role-cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.role-card {
  position: relative;
  padding: 24px 16px;
  border: 2px solid var(--color-border-2, #E5E6EB);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: center;
  background: var(--color-bg-2, #F5F6F7);
}

.role-card:hover {
  border-color: var(--color-primary-light-3, #86b1ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
}

.role-card--selected {
  border-color: var(--color-primary-6, #165DFF);
  background: linear-gradient(135deg, #e6f1ff 0%, #f0f5ff 100%);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.2);
}

.role-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.role-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  margin-bottom: 8px;
}

.role-desc {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
  line-height: 1.5;
}

.role-check {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #00B42A;
  font-size: 18px;
}

.role-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px;
  background: var(--color-fill-1, #F7F8FA);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text-3, #86909C);
  margin-bottom: 20px;
}

.step-buttons {
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

.step-success {
  text-align: center;
  padding: 32px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-1, #1D2129);
  margin: 0 0 12px 0;
}

.success-desc {
  font-size: 15px;
  color: var(--color-text-2, #4E5969);
  margin: 0 0 16px 0;
}

.countdown {
  font-size: 13px;
  color: var(--color-primary-6, #165DFF);
  margin-bottom: 28px;
}

.success-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.card-footer {
  text-align: center;
  color: var(--color-text-3, #86909C);
  font-size: 14px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-1, #E5E6EB);
}

.card-footer a {
  color: var(--color-primary-6, #165DFF);
  margin-left: 4px;
  text-decoration: none;
  font-weight: 500;
}

.card-footer a:hover {
  text-decoration: underline;
}
</style>
