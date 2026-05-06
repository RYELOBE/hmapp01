<template>
  <div class="login-page">
    <div class="login-background">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo" @click="router.push('/')">
            <icon-shopping-cart style="font-size: 40px; color: #165DFF;" />
            <span class="logo-text">校园闲置</span>
          </div>
          <h1 class="login-title">欢迎回来</h1>
          <p class="login-subtitle">登录您的校园闲置交易平台账号</p>
        </div>

        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          class="login-form"
          @submit-success="handleSubmit"
        >
          <a-form-item field="loginType" hide-label>
            <a-radio-group v-model="formData.loginType" type="button">
              <a-radio value="phone">手机号登录</a-radio>
              <a-radio value="email">邮箱登录</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item
            field="account"
            :label="formData.loginType === 'phone' ? '手机号' : '邮箱'"
            hide-label
          >
            <a-input
              v-model="formData.account"
              :placeholder="formData.loginType === 'phone' ? '请输入手机号' : '请输入邮箱'"
              size="large"
            >
              <template #prefix>
                <icon-phone v-if="formData.loginType === 'phone'" />
                <icon-email v-else />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="password" label="密码" hide-label>
            <a-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input>
          </a-form-item>

          <div class="form-options">
            <a-checkbox v-model="formData.remember">记住登录状态</a-checkbox>
            <a-link @click="handleForgotPassword">忘记密码？</a-link>
          </div>

          <a-form-item hide-label>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              long
              :loading="loading"
              class="submit-btn"
            >
              登 录
            </a-button>
          </a-form-item>

          <div class="oauth-login">
            <span class="oauth-label">其他登录方式</span>
            <div class="oauth-buttons">
              <a-tooltip content="微信登录">
                <div class="oauth-btn" @click="handleOAuthLogin('wechat')">
                  <icon-wechat />
                </div>
              </a-tooltip>
              <a-tooltip content="QQ登录">
                <div class="oauth-btn" @click="handleOAuthLogin('qq')">
                  <icon-qq />
                </div>
              </a-tooltip>
            </div>
          </div>

          <div class="register-link">
            还没有账号？<a-link @click="router.push('/register')">立即注册</a-link>
          </div>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconShoppingCart,
  IconPhone,
  IconEmail,
  IconLock,
  IconWechat,
  IconQq
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const formData = reactive({
  loginType: 'phone',
  account: '',
  password: '',
  remember: false
})

const rules = {
  account: [
    { required: true, message: '请输入账号' },
    {
      validator: (value, cb) => {
        if (formData.loginType === 'phone') {
          if (!/^1[3-9]\d{9}$/.test(value)) {
            cb('请输入正确的手机号')
          }
        } else {
          if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(value)) {
            cb('请输入正确的邮箱')
          }
        }
        cb()
      }
    }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少6位' }
  ]
}

const handleSubmit = async ({ values }) => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    Message.success('登录成功')
    router.push('/')
  } catch (error) {
    Message.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  Message.info('跳转到找回密码页面')
}

const handleOAuthLogin = (type) => {
  Message.info(`${type === 'wechat' ? '微信' : 'QQ'}登录`)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.bg-shape-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation-delay: 2s;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-bottom: 16px;
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #165DFF;
  letter-spacing: 1px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #86909C;
}

.login-form :deep(.arco-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.arco-radio-group-button) {
  width: 100%;
}

.login-form :deep(.arco-radio-button) {
  flex: 1;
  text-align: center;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 100%);
}

.oauth-login {
  margin-top: 24px;
  text-align: center;
}

.oauth-label {
  display: block;
  font-size: 12px;
  color: #86909C;
  margin-bottom: 16px;
  position: relative;
}

.oauth-label::before,
.oauth-label::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 80px;
  height: 1px;
  background: #E5E6EB;
}

.oauth-label::before {
  left: 0;
}

.oauth-label::after {
  right: 0;
}

.oauth-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.oauth-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 22px;
  color: #4E5969;
}

.oauth-btn:hover {
  background: #165DFF;
  color: #fff;
  transform: scale(1.1);
}

.register-link {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #86909C;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }
}
</style>
