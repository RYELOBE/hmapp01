<template>
  <div class="ops-login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle--1"></div>
      <div class="circle circle--2"></div>
      <div class="circle circle--3"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <!-- 品牌头部 -->
        <div class="brand-header">
          <div class="brand-icon">
            <icon-fire :size="36" />
          </div>
          <h1 class="brand-title">运营管理中心</h1>
          <p class="brand-subtitle">Operation Management Center</p>
        </div>

        <!-- 分隔线 -->
        <div class="divider"></div>

        <!-- 表单区域 -->
        <a-form
          :model="form"
          :rules="rules"
          ref="formRef"
          layout="vertical"
          @submit-success="handleLogin"
          class="login-form"
        >
          <a-form-item field="username" hide-label>
            <a-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-user :size="18" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="password" hide-label>
            <a-input-password
              v-model="form.password"
              placeholder="请输入密码"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock :size="18" />
              </template>
            </a-input-password>
          </a-form-item>

          <div class="form-options">
            <a-checkbox v-model="rememberMe"> 记住登录状态 </a-checkbox>
          </div>

          <a-form-item hide-label>
            <a-button
              type="primary"
              html-type="submit"
              long
              size="large"
              :loading="loading"
              class="login-btn"
            >
              {{ loading ? "登录中..." : "登 录" }}
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 底部链接 -->
        <div class="footer-links">
          <a-link href="#" @click.prevent>忘记密码？联系管理员重置</a-link>
          <span class="divider-text">|</span>
          <router-link to="/portal/login" class="back-to-portal">
            返回用户端登录
          </router-link>
        </div>
      </div>

      <!-- 版权信息 -->
      <div class="copyright">© 2026 Campus Trading Platform · OPS Console</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconFire, IconUser, IconLock } from "@arco-design/web-vue/es/icon";
import { opsLogin } from "commonprovide/auth-sdk";

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const rememberMe = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名" }],
  password: [
    { required: true, message: "请输入密码" },
    { minLength: 6, message: "密码至少6位" },
  ],
};

async function handleLogin() {
  loading.value = true;
  try {
    const result = await opsLogin({
      username: form.username,
      password: form.password,
    });

    if (rememberMe.value) {
      localStorage.setItem("ops_remember", "true");
      localStorage.setItem("ops_username", form.username);
    }

    Message.success("登录成功，正在跳转...");

    setTimeout(() => {
      if (!window.__POWERED_BY_QIANKUN__ || import.meta.env.DEV) {
        router.push("/ops/dashboard");
      } else {
        router.push("/");
      }
    }, 500);
  } catch (error) {
    Message.error(error.message || "登录失败，请检查用户名和密码");
  } finally {
    loading.value = false;
  }
}

if (localStorage.getItem("ops_remember")) {
  rememberMe.value = true;
  form.username = localStorage.getItem("ops_username") || "";
}
</script>

<style scoped>
.ops-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f1d3a 0%, #1e3a5f 50%, #165dff 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.08;
    background: linear-gradient(135deg, #4080ff 0%, #86b1ff 100%);
  }

  .circle--1 {
    width: 500px;
    height: 500px;
    top: -150px;
    right: -100px;
    animation: float 20s ease-in-out infinite;
  }

  .circle--2 {
    width: 350px;
    height: 350px;
    bottom: -100px;
    left: -80px;
    animation: float 15s ease-in-out infinite reverse;
  }

  .circle--3 {
    width: 200px;
    height: 200px;
    top: 40%;
    left: 15%;
    animation: float 12s ease-in-out infinite;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(5deg);
  }
}

.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.login-card {
  background: rgba(255, 255, 255, 0.97);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 48px 40px;
  width: 100%;
  max-width: 400px;
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.1);
}

.brand-header {
  text-align: center;
  margin-bottom: 28px;
}

.brand-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35);
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 6px 0;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 13px;
  color: #86909c;
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e5e6eb, transparent);
  margin-bottom: 32px;
}

.login-form {
  margin-bottom: 4px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.login-btn {
  height: 46px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.35) !important;
  transition: all 0.25s ease !important;

  &:hover:not(:disabled) {
    transform: translateY(-2px) !important;
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.45) !important;
  }

  &:active {
    transform: translateY(0) !important;
  }
}

.footer-links {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #86909c;

  a {
    color: #86909c;
    transition: color 0.2s;

    &:hover {
      color: #165dff;
    }
  }

  .divider-text {
    margin: 0 8px;
    color: #e5e6eb;
  }

  .back-to-portal {
    color: #165dff;
    font-weight: 500;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.copyright {
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
  letter-spacing: 0.5px;
}
</style>
