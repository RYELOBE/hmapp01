<template>
  <div class="login-content-wrapper">
    <div v-if="showRoleSelect" class="role-select-section">
      <h4 class="role-title">请选择您的角色</h4>
      <p class="role-subtitle">检测到您拥有多个角色，请选择要使用的角色登录</p>
      <div class="role-list">
        <div
          v-for="role in availableRoles"
          :key="role.code"
          class="role-item"
          :class="{ active: selectedRole === role.code }"
          @click="selectRole(role.code)"
        >
          <div class="role-icon">{{ role.icon || "👤" }}</div>
          <div class="role-info">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-desc">{{ role.description || "" }}</div>
          </div>
          <icon-check v-if="selectedRole === role.code" class="role-check" />
        </div>
      </div>
      <a-button
        type="primary"
        long
        :loading="loading"
        :disabled="!selectedRole"
        @click="confirmRole"
      >
        确认并登录
      </a-button>
    </div>

    <a-form
      v-else
      :model="form"
      layout="vertical"
      ref="formRef"
      :rules="rules"
      @submit-success="submit"
    >
      <div class="form-header">
        <h3 class="form-title">欢迎回来</h3>
        <p class="form-subtitle">请输入您的账号信息</p>
      </div>

      <a-form-item
        field="username"
        label="账号"
        :validate-trigger="['blur', 'change']"
      >
        <a-input
          v-model="form.username"
          placeholder="请输入账号"
          allow-clear
          size="large"
        >
          <template #prefix>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item
        field="password"
        label="密码"
        :validate-trigger="['blur', 'change']"
      >
        <a-input-password
          v-model="form.password"
          placeholder="请输入密码"
          allow-clear
          size="large"
        >
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>

      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          long
          :loading="loading"
          size="large"
          class="login-button"
        >
          {{ loading ? "登录中..." : "登 录" }}
        </a-button>
      </a-form-item>

      <div class="form-footer">
        <a-typography-text type="secondary">
          内置演示账号：buyer、seller、ops，密码均为 123456
        </a-typography-text>
      </div>
    </a-form>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconCheck, IconUser, IconLock } from "@arco-design/web-vue/es/icon";
import { login, opsLogin } from "../auth-sdk.js";

const props = defineProps({
  loginConfig: {
    type: Object,
    default: () => ({}),
  },
  appType: {
    type: String,
    default: "",
  },
});

const emits = defineEmits(["login-success"]);

const loading = ref(false);
const formRef = ref(null);

const form = reactive({
  username: "buyer",
  password: "123456",
});

const rules = {
  username: [
    { required: true, message: "请输入账号" },
    { minLength: 2, message: "账号长度不能小于2个字符" },
  ],
  password: [
    { required: true, message: "请输入密码" },
    { minLength: 6, message: "密码长度不能小于6个字符" },
  ],
};

const showRoleSelect = ref(false);
const availableRoles = ref([]);
const selectedRole = ref("");
const pendingLoginResult = ref(null);

const roleMap = {
  buyer: {
    code: "buyer",
    name: "买家",
    icon: "🛒",
    description: "浏览商品、下单购买",
  },
  seller: {
    code: "seller",
    name: "卖家",
    icon: "🏪",
    description: "发布商品、管理订单",
  },
  ops: {
    code: "ops",
    name: "运营管理员",
    icon: "⚙️",
    description: "系统管理、审核运营",
  },
};

async function submit() {
  loading.value = true;
  try {
    let result;
    const loginData = { username: form.username, password: form.password };
    if (props.appType === "ops") {
      result = await opsLogin(loginData);
    } else {
      result = await login(loginData);
    }

    const userRoles = result?.user?.roles || [form.username];

    if (userRoles.length > 1) {
      availableRoles.value = userRoles.map((roleCode) => ({
        ...roleMap[roleCode],
        code: roleCode,
        name: roleMap[roleCode]?.name || roleCode,
      }));
      pendingLoginResult.value = { ...result, selectedRole: null };
      showRoleSelect.value = true;
      Message.info("请选择要使用的角色");
    } else {
      Message.success({
        content: "登录成功",
        showIcon: true,
        duration: 2000,
      });
      emits("login-success");
    }
  } catch (error) {
    Message.error({
      content: error.message || "登录失败",
      showIcon: true,
    });
  } finally {
    loading.value = false;
  }
}

function selectRole(roleCode) {
  selectedRole.value = roleCode;
}

async function confirmRole() {
  if (!selectedRole.value) return;

  loading.value = true;
  try {
    if (pendingLoginResult.value) {
      pendingLoginResult.value.selectedRole = selectedRole.value;
      pendingLoginResult.value.user.currentRole = selectedRole.value;
    }
    Message.success({
      content: `已切换到 ${roleMap[selectedRole.value]?.name || selectedRole.value} 角色`,
      showIcon: true,
    });
    emits("login-success");
  } catch (error) {
    Message.error(error.message || "角色切换失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.login-content-wrapper {
  width: 100%;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-1, #1d2129);
}

.form-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-3, #86909c);
}

.login-button {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--border-radius-medium, 8px);
  transition: all 150ms ease-out;

  &:hover:not(:disabled):not(.arco-btn-loading) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.35);
  }
}

.form-footer {
  text-align: center;
  margin-top: 16px;
}

.role-select-section {
  text-align: center;
}

.role-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-1, #1d2129);
}

.role-subtitle {
  margin: 0 0 20px;
  font-size: 13px;
  color: var(--color-text-3, #86909c);
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid var(--color-border-2, #e5e6eb);
  border-radius: var(--border-radius-medium, 8px);
  cursor: pointer;
  transition: all 150ms ease-out;
  background: var(--color-bg-white, #fff);

  &:hover {
    border-color: var(--color-text-3, #86909c);
    transform: translateX(4px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  &.active {
    border-color: var(--color-primary, #165dff);
    background: rgba(22, 93, 255, 0.04);
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
  }
}

.role-icon {
  width: 48px;
  height: 48px;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-fill-1, #f7f8fa);
  border-radius: var(--border-radius-medium, 8px);
  flex-shrink: 0;
}

.role-info {
  flex: 1;
  text-align: left;
}

.role-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1, #1d2129);
  margin-bottom: 4px;
}

.role-desc {
  font-size: 12px;
  color: var(--color-text-3, #86909c);
}

.role-check {
  font-size: 20px;
  color: var(--color-primary, #165dff);
  flex-shrink: 0;
}
</style>
