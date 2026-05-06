<template>
  <div class="register-page">
    <div class="register-background">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="logo" @click="router.push('/')">
            <icon-shopping-cart style="font-size: 40px; color: #165DFF;" />
            <span class="logo-text">校园闲置</span>
          </div>
          <h1 class="register-title">创建账号</h1>
          <p class="register-subtitle">加入校园闲置交易平台，开始您的闲置交易之旅</p>
        </div>

        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          class="register-form"
          @submit-success="handleSubmit"
        >
          <a-form-item field="username" label="用户名">
            <a-input
              v-model="formData.username"
              placeholder="请输入用户名（2-20个字符）"
              size="large"
              max-length="20"
            >
              <template #prefix>
                <icon-user />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="phone" label="手机号">
            <a-input
              v-model="formData.phone"
              placeholder="请输入手机号"
              size="large"
            >
              <template #prefix>
                <icon-phone />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="email" label="邮箱">
            <a-input
              v-model="formData.email"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <icon-email />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="captcha" label="验证码">
            <a-input
              v-model="formData.captcha"
              placeholder="请输入验证码"
              size="large"
              style="width: 60%;"
            >
              <template #prefix>
                <icon-safe />
              </template>
            </a-input>
            <a-button
              type="secondary"
              size="large"
              style="width: 38%; margin-left: 2%;"
              :disabled="countdown > 0"
              @click="handleSendCaptcha"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
            </a-button>
          </a-form-item>

          <a-form-item field="password" label="密码">
            <a-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码（6-20个字符）"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="confirmPassword" label="确认密码">
            <a-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="campusVerified" hide-label>
            <a-checkbox v-model="formData.campusVerified">
              完成校园认证（可选）
            </a-checkbox>
            <a-popover>
              <icon-question-circle class="help-icon" />
              <template #content>
                <div class="help-content">
                  <p>校园认证可以提升您的账号信誉度，让交易更安全。</p>
                  <p>认证后可在商品页面展示"已认证学生"标识。</p>
                </div>
              </template>
            </a-popover>
          </a-form-item>

          <a-form-item field="agreeTerms" hide-label>
            <a-checkbox v-model="formData.agreeTerms">
              我已阅读并同意
              <a-link @click.stop="router.push('/terms')">《用户协议》</a-link>
              和
              <a-link @click.stop="router.push('/privacy')">《隐私政策》</a-link>
            </a-checkbox>
          </a-form-item>

          <a-form-item hide-label>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              long
              :loading="loading"
              :disabled="!formData.agreeTerms"
              class="submit-btn"
            >
              注 册
            </a-button>
          </a-form-item>

          <div class="login-link">
            已有账号？<a-link @click="router.push('/login')">立即登录</a-link>
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
  IconUser,
  IconPhone,
  IconEmail,
  IconSafe,
  IconLock,
  IconQuestionCircle
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const countdown = ref(0)

const formData = reactive({
  username: '',
  phone: '',
  email: '',
  captcha: '',
  password: '',
  confirmPassword: '',
  campusVerified: false,
  agreeTerms: false
})

let countdownTimer = null

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 2, message: '用户名至少2个字符' },
    { maxLength: 20, message: '用户名最多20个字符' }
  ],
  phone: [
    { required: true, message: '请输入手机号' },
    {
      validator: (value, cb) => {
        if (!/^1[3-9]\d{9}$/.test(value)) {
          cb('请输入正确的手机号')
        }
        cb()
      }
    }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    {
      validator: (value, cb) => {
        if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(value)) {
          cb('请输入正确的邮箱')
        }
        cb()
      }
    }
  ],
  captcha: [
    { required: true, message: '请输入验证码' },
    { minLength: 4, maxLength: 6, message: '验证码4-6位' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少6个字符' },
    { maxLength: 20, message: '密码最多20个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码' },
    {
      validator: (value, cb) => {
        if (value !== formData.password) {
          cb('两次输入的密码不一致')
        }
        cb()
      }
    }
  ]
}

const handleSendCaptcha = () => {
  if (!formData.phone) {
    Message.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    Message.warning('请输入正确的手机号')
    return
  }

  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)

  Message.success('验证码已发送')
}

const handleSubmit = async ({ values }) => {
  if (!formData.agreeTerms) {
    Message.warning('请阅读并同意用户协议和隐私政策')
    return
  }

  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    Message.success('注册成功，即将跳转到登录页面')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    Message.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 0;
}

.register-background {
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
  left: -100px;
  animation-delay: 0s;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  top: 30%;
  right: 15%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 520px;
  padding: 20px;
}

.register-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
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

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #86909C;
  line-height: 1.6;
}

.register-form :deep(.arco-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.arco-form-item-label) {
  font-weight: 500;
  color: #1D2129;
}

.help-icon {
  margin-left: 8px;
  color: #86909C;
  cursor: pointer;
  vertical-align: middle;
}

.help-content {
  font-size: 13px;
  line-height: 1.6;
  color: #4E5969;
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border: none;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 100%);
}

.submit-btn:disabled {
  background: #E5E6EB;
  color: #86909C;
}

.login-link {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #86909C;
}

@media (max-width: 540px) {
  .register-card {
    padding: 32px 24px;
  }

  .register-form :deep(.arco-input-wrapper) {
    width: 100% !important;
  }
}
</style>
