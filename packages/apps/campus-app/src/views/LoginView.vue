<template>
  <div class="auth-page">
    <!-- 左侧装饰区域 -->
    <div class="auth-decoration">
      <div class="deco-content">
        <div class="deco-illustration">
          <!-- 3D等距插图效果 -->
          <svg viewBox="0 0 400 300" class="isometric-art">
            <!-- 平台底座 -->
            <rect
              x="50"
              y="200"
              width="300"
              height="20"
              rx="4"
              fill="#E8F3FF"
              opacity="0.8"
            />
            <rect
              x="60"
              y="180"
              width="280"
              height="25"
              rx="4"
              fill="#D6E9FF"
              opacity="0.6"
            />

            <!-- 笔记本电脑 -->
            <g transform="translate(140, 120)">
              <rect
                x="0"
                y="30"
                width="120"
                height="80"
                rx="6"
                fill="#4080FF"
                opacity="0.15"
              />
              <rect
                x="5"
                y="35"
                width="110"
                height="65"
                rx="3"
                fill="#FFFFFF"
                stroke="#165DFF"
                stroke-width="2"
              />
              <rect
                x="10"
                y="42"
                width="100"
                height="55"
                rx="2"
                fill="#F5F7FA"
              />
              <!-- 屏幕内容 -->
              <rect
                x="18"
                y="50"
                width="35"
                height="28"
                rx="2"
                fill="#165DFF"
                opacity="0.1"
              />
              <rect
                x="58"
                y="50"
                width="45"
                height="12"
                rx="2"
                fill="#165DFF"
                opacity="0.08"
              />
              <rect
                x="58"
                y="66"
                width="38"
                height="8"
                rx="2"
                fill="#165DFF"
                opacity="0.06"
              />
              <rect
                x="58"
                y="78"
                width="42"
                height="8"
                rx="2"
                fill="#165DFF"
                opacity="0.06"
              />
              <!-- 键盘 -->
              <rect
                x="-10"
                y="115"
                width="140"
                height="10"
                rx="5"
                fill="#86909C"
                opacity="0.3"
              />
            </g>

            <!-- 人物 - 买家 -->
            <g transform="translate(90, 160)">
              <circle cx="15" cy="8" r="8" fill="#FFB366" />
              <path d="M 7 22 Q 15 16 23 22 L 23 38 L 7 38 Z" fill="#165DFF" />
            </g>

            <!-- 人物 - 卖家 -->
            <g transform="translate(270, 155)">
              <circle cx="15" cy="8" r="8" fill="#FFC78E" />
              <path d="M 7 22 Q 15 16 23 22 L 23 38 L 7 38 Z" fill="#4080FF" />
            </g>

            <!-- 装饰元素 -->
            <circle cx="320" cy="80" r="12" fill="#FFD666" opacity="0.6">
              <animate
                attributeName="cy"
                values="80;75;80"
                dur="3s"
                repeatCount="indefinite"
              />
            </circle>
            <circle cx="70" cy="100" r="8" fill="#69C0FF" opacity="0.5">
              <animate
                attributeName="cy"
                values="100;95;100"
                dur="2.5s"
                repeatCount="indefinite"
              />
            </circle>
            <rect
              x="330"
              y="150"
              width="24"
              height="24"
              rx="4"
              fill="#95DE64"
              opacity="0.4"
              transform="rotate(15 342 162)"
            />
          </svg>
        </div>
        <h2 class="deco-title">校园二手交易平台</h2>
        <p class="deco-subtitle">闲置不浪费，校园淘好物</p>
      </div>
    </div>

    <!-- 右侧表单区域 -->
    <div class="auth-form-section">
      <div class="auth-container">
        <!-- Logo -->
        <div class="auth-logo">
          <div class="logo-icon">🎓</div>
          <span class="logo-text">CampusTrade</span>
        </div>

        <!-- 登录表单 -->
        <div v-if="!showRegister" class="auth-card">
          <h1 class="auth-title">
            {{ showRegister ? "创建新账号" : "普通师生登陆" }}
          </h1>
          <p class="auth-subtitle">请输入您的账号信息</p>

          <a-form
            :model="loginForm"
            layout="vertical"
            @submit="handleLogin"
            class="auth-form"
          >
            <a-form-item
              field="username"
              :rules="[{ required: true, message: '请输入用户名' }]"
              hide-label
            >
              <a-input
                v-model="loginForm.username"
                placeholder="用户名 / 手机号 / 邮箱"
                size="large"
                allow-clear
              >
                <template #prefix>
                  <icon-user style="color: #86909c" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item
              field="password"
              :rules="[{ required: true, message: '请输入密码' }]"
              hide-label
            >
              <a-input-password
                v-model="loginForm.password"
                placeholder="密码"
                size="large"
                allow-clear
              >
                <template #prefix>
                  <icon-lock style="color: #86909c" />
                </template>
              </a-input-password>
            </a-form-item>

            <div class="form-options">
              <a-checkbox v-model="rememberMe">记住我</a-checkbox>
              <a-link @click="$message.info('请联系管理员重置密码')"
                >忘记密码？</a-link
              >
            </div>

            <a-button
              type="primary"
              html-type="submit"
              long
              size="large"
              :loading="loading"
              class="btn-submit"
            >
              登 录
            </a-button>
          </a-form>

          <div class="auth-switch">
            <span>还没有账号？</span>
            <a-link @click="switchToRegister">立即注册</a-link>
          </div>
        </div>

        <!-- 注册表单 -->
        <div v-else class="auth-card">
          <h1 class="auth-title">创建新账号</h1>
          <p class="auth-subtitle">加入校园二手交易平台</p>

          <a-form
            :model="registerForm"
            layout="vertical"
            @submit="handleRegister"
            class="auth-form"
          >
            <a-form-item
              field="username"
              :rules="[{ required: true, message: '请输入用户名' }]"
              hide-label
            >
              <a-input
                v-model="registerForm.username"
                placeholder="用户名（用于登录）"
                size="large"
                allow-clear
              />
            </a-form-item>

            <a-form-item
              field="phone"
              :rules="[{ required: true, message: '请输入手机号' }]"
              hide-label
            >
              <a-input
                v-model="registerForm.phone"
                placeholder="手机号码"
                size="large"
                allow-clear
              >
                <template #prefix>
                  <icon-phone style="color: #86909c" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item
              field="password"
              :rules="[
                { required: true, message: '请输入密码' },
                { minLength: 6, message: '密码至少6位' },
              ]"
              hide-label
            >
              <a-input-password
                v-model="registerForm.password"
                placeholder="设置密码（至少6位）"
                size="large"
                allow-clear
              />
            </a-form-item>

            <a-form-item
              field="confirmPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                {
                  validator: (value, callback) => {
                    if (value !== registerForm.password) {
                      callback('两次密码不一致');
                    } else {
                      callback();
                    }
                  },
                },
              ]"
              hide-label
            >
              <a-input-password
                v-model="registerForm.confirmPassword"
                placeholder="确认密码"
                size="large"
                allow-clear
              />
            </a-form-item>

            <!-- 角色选择 -->
            <a-form-item
              label="选择身份（可多选）"
              field="roles"
              :rules="[{ required: true, message: '请至少选择一个身份' }]"
            >
              <div class="role-selector">
                <div
                  class="role-option"
                  :class="{
                    'role-option--selected':
                      registerForm.roles.includes('BUYER'),
                  }"
                  @click="toggleRole('BUYER')"
                >
                  <span class="role-icon">🛍️</span>
                  <span class="role-name">我是买家</span>
                  <span class="role-desc">淘好物 省钱省心</span>
                  <icon-check-circle-fill
                    v-if="registerForm.roles.includes('BUYER')"
                    class="role-check"
                  />
                </div>

                <div
                  class="role-option"
                  :class="{
                    'role-option--selected':
                      registerForm.roles.includes('SELLER'),
                  }"
                  @click="toggleRole('SELLER')"
                >
                  <span class="role-icon">💰</span>
                  <span class="role-name">我是卖家</span>
                  <span class="role-desc">卖闲置 回收资金</span>
                  <icon-check-circle-fill
                    v-if="registerForm.roles.includes('SELLER')"
                    class="role-check"
                  />
                </div>
              </div>
            </a-form-item>

            <a-checkbox v-model="agreeTerms" class="terms-checkbox">
              我已阅读并同意
              <a-link>《用户协议》</a-link>和<a-link>《隐私政策》</a-link>
            </a-checkbox>

            <a-button
              type="primary"
              html-type="submit"
              long
              size="large"
              :loading="loading"
              :disabled="!agreeTerms || registerForm.roles.length === 0"
              class="btn-submit btn-register"
            >
              注 册
            </a-button>
          </a-form>

          <div class="auth-switch">
            <span>已有账号？</span>
            <a-link @click="switchToLogin">返回登录</a-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconUser,
  IconLock,
  IconPhone,
  IconCheckCircleFill,
} from "@arco-design/web-vue/es/icon";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const showRegister = ref(false);
const loading = ref(false);
const rememberMe = ref(false);
const agreeTerms = ref(false);

// 登录表单
const loginForm = reactive({
  username: "",
  password: "",
});

// 注册表单
const registerForm = reactive({
  username: "",
  phone: "",
  password: "",
  confirmPassword: "",
  roles: [], // BUYER, SELLER 可多选
});

function switchToRegister() {
  showRegister.value = true;
}

function switchToLogin() {
  showRegister.value = false;
}

function toggleRole(role) {
  const index = registerForm.roles.indexOf(role);
  if (index > -1) {
    registerForm.roles.splice(index, 1);
  } else {
    registerForm.roles.push(role);
  }
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    Message.warning("请填写完整的登录信息");
    return;
  }

  loading.value = true;
  try {
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
    });
    Message.success("登录成功！");

    console.log("[Login] ===== 开始计算跳转目标 =====");
    // 智能路由跳转：角色优先策略
    const roles = authStore.roles || [];
    const redirect = route.query.redirect;

    console.log("[Login] 用户角色:", JSON.stringify(roles));
    console.log("[Login] URL中的redirect参数:", redirect);
    console.log("[Login] 当前路径:", route.path);

    let targetPath = "";

    // 角色优先判断（运营人员必须去运营端）
    if (roles.includes("OPS")) {
      targetPath = "/ops/dashboard";
      console.log("[Login] → 检测到OPS角色，跳转到运营后台");
    } else if (roles.includes("SELLER") && !roles.includes("BUYER")) {
      targetPath = "/portal/seller/items";
      console.log("[Login] → 纯SELLER角色，跳转到卖家中心");
    } else if (
      redirect &&
      redirect !== "/login" &&
      !redirect.startsWith("/ops")
    ) {
      // 非运营角色且有指定来源，使用redirect（但排除运营端路径）
      targetPath = redirect;
      console.log("[Login] → 使用redirect参数:", targetPath);
    } else {
      targetPath = "/portal/home";
      console.log("[Login] → 默认跳转到门户首页");
    }

    console.log("[Login] ===== 最终跳转目标:", targetPath, "=====");
    console.log("[Login] 即将执行 router.push...");

    try {
      await router.push(targetPath);
      console.log("[Login] ✅ router.push 成功！已跳转到:", targetPath);
    } catch (navError) {
      console.error("[Login] ❌ router.push 失败！错误:", navError);
      console.error("[Login] 错误详情:", navError.message, navError.stack);

      // 如果跳转失败，尝试强制导航
      console.log("[Login] 尝试使用 window.location 强制跳转...");
      window.location.href = targetPath;
    }
  } catch (error) {
    console.error("[Login] 登录失败:", error);
    const errorMsg =
      error.response?.data?.message ||
      error.message ||
      "登录失败，请检查账号密码";
    Message.error(errorMsg);
  } finally {
    loading.value = false;
  }
}

// 根据角色计算跳转路径
function getRedirectPath(roles) {
  if (!roles || roles.length === 0) return "/portal/home";

  if (roles.includes("OPS")) {
    return "/ops/dashboard"; // 运营人员 → 后台管理
  } else if (roles.includes("SELLER") && !roles.includes("BUYER")) {
    return "/portal/seller/items"; // 纯卖家 → 卖家中心
  } else {
    return "/portal/home"; // 买家或混合角色 → 首页
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.phone || !registerForm.password) {
    Message.warning("请填写完整的注册信息");
    return;
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    Message.error("两次输入的密码不一致");
    return;
  }

  if (registerForm.roles.length === 0) {
    Message.warning("请至少选择一个身份（买家/卖家）");
    return;
  }

  if (!agreeTerms.value) {
    Message.warning("请先阅读并同意用户协议");
    return;
  }

  loading.value = true;
  try {
    // 调用注册API
    await authStore.register({
      username: registerForm.username,
      phone: registerForm.phone,
      password: registerForm.password,
      roles: registerForm.roles,
    });

    Message.success("注册成功！正在为您自动登录...");

    // 注册成功后自动登录
    setTimeout(async () => {
      try {
        await authStore.login({
          username: registerForm.username,
          password: registerForm.password,
        });

        Message.success("登录成功！");
        await router.push("/portal/home");
      } catch (loginError) {
        console.error("[Register] 自动登录失败:", loginError);
        Message.info("注册成功，请手动登录");
        switchToLogin();
      }
    }, 1500);
  } catch (error) {
    console.error("[Register] 注册失败:", error);
    const errorMsg =
      error.response?.data?.message || error.message || "注册失败，请稍后重试";
    Message.error(errorMsg);
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-white, #ffffff);
  overflow: hidden;
}

// ========== 左侧装饰区域 ==========
.auth-decoration {
  flex: 1;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 50%, #69c0ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: -50%;
    right: -20%;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.1) 0%,
      transparent 70%
    );
  }

  &::after {
    content: "";
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.08) 0%,
      transparent 70%
    );
  }
}

.deco-content {
  text-align: center;
  z-index: 1;
  max-width: 480px;
}

.deco-illustration {
  margin-bottom: 48px;

  .isometric-art {
    width: 100%;
    max-width: 380px;
    height: auto;
    filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.15));
  }
}

.deco-title {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.deco-subtitle {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

// ========== 右侧表单区域 ==========
.auth-form-section {
  flex: 0 0 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
  background: var(--bg-white, #ffffff);
  overflow-y: auto;
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
  cursor: pointer;

  &:hover {
    transform: scale(1.02);
  }

  .logo-icon {
    font-size: 36px;
  }

  .logo-text {
    font-size: 26px;
    font-weight: 800;
    color: var(--text-primary, #1d2129);
    letter-spacing: -0.03em;
  }
}

.auth-card {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1d2129);
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.auth-subtitle {
  font-size: 15px;
  color: var(--text-secondary, #4e5969);
  margin: 0 0 32px 0;
}

.auth-form {
  :deep(.arco-form-item) {
    margin-bottom: 20px;
  }

  :deep(.arco-input-wrapper),
  :deep(.arco-input-password-wrapper) {
    height: 46px !important;
    border-radius: 10px !important;
    transition: all 0.3s ease !important;

    &:hover {
      border-color: var(--primary-blue, #165dff) !important;
    }

    &:focus-within {
      box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1) !important;
    }
  }

  :deep(.arco-input) {
    font-size: 15px;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.btn-submit {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  background: linear-gradient(
    135deg,
    var(--primary-blue, #165dff) 0%,
    var(--primary-blue-light, #4080ff) 100%
  ) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.25) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  margin-top: 8px;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35) !important;
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-register {
  background: linear-gradient(135deg, #ff7d00 0%, #ff9500 100%) !important;
  box-shadow: 0 4px 16px rgba(255, 125, 0, 0.3) !important;

  &:hover:not(:disabled) {
    box-shadow: 0 8px 24px rgba(255, 125, 0, 0.4) !important;
  }
}

.auth-switch {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: var(--text-secondary, #4e5969);

  a {
    font-weight: 600;
    margin-left: 4px;
  }
}

// ========== 角色选择器 ==========
.role-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.role-option {
  position: relative;
  padding: 16px;
  border: 2px solid var(--border-color, #e5e6eb);
  border-radius: 12px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--bg-gray-light, #fafbfc);

  &:hover {
    border-color: var(--primary-blue-light, #4080ff);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.1);
  }

  &--selected {
    border-color: var(--primary-blue, #165dff);
    background: linear-gradient(135deg, #e8f3ff 0%, #f0f5ff 100%);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.15);
  }

  .role-icon {
    font-size: 32px;
    display: block;
    margin-bottom: 8px;
  }

  .role-name {
    display: block;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary, #1d2129);
    margin-bottom: 4px;
  }

  .role-desc {
    display: block;
    font-size: 12px;
    color: var(--text-tertiary, #86909c);
  }

  .role-check {
    position: absolute;
    top: 8px;
    right: 8px;
    font-size: 20px;
    color: var(--primary-blue, #165dff);
  }
}

.terms-checkbox {
  margin: 16px 0 24px 0;
  font-size: 13px;
  color: var(--text-secondary, #4e5969);

  :deep(.arco-checkbox-label) {
    color: inherit;
  }

  a {
    color: var(--primary-blue, #165dff);
  }
}

// ========== 响应式设计 ==========
@media (max-width: 1024px) {
  .auth-decoration {
    display: none; /* 移动端隐藏左侧装饰 */
  }

  .auth-form-section {
    flex: 1;
    padding: 40px 24px;
  }

  .auth-container {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .auth-form-section {
    padding: 32px 20px;
  }

  .auth-title {
    font-size: 24px;
  }

  .role-selector {
    grid-template-columns: 1fr; /* 移动端单列 */
  }
}
</style>
