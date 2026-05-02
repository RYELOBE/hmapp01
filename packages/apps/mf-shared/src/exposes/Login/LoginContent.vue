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
          <div class="role-icon">{{ role.icon || '👤' }}</div>
          <div class="role-info">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-desc">{{ role.description || '' }}</div>
          </div>
          <icon-check v-if="selectedRole === role.code" class="role-check" />
        </div>
      </div>
      <a-button type="primary" long :loading="loading" :disabled="!selectedRole" @click="confirmRole">
        确认并登录
      </a-button>
    </div>

    <a-form v-else :model="form" layout="vertical">
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

    <a-typography-text v-if="!showRoleSelect" type="secondary">
      内置演示账号：buyer、seller、ops，密码均为 123456
    </a-typography-text>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconCheck } from "@arco-design/web-vue/es/icon";
import { login, opsLogin } from "../auth-sdk.js";

const props = defineProps({
  loginConfig: {
    type: Object,
    default: () => ({})
  },
  appType: {
    type: String,
    default: ""
  }
});

const emits = defineEmits(["login-success"]);

const loading = ref(false);
const form = reactive({
  username: "buyer",
  password: "123456"
});

// 多角色选择相关状态
const showRoleSelect = ref(false);
const availableRoles = ref([]);
const selectedRole = ref('');
const pendingLoginResult = ref(null);

// 模拟角色数据（实际应从登录接口返回）
const roleMap = {
  buyer: { code: 'buyer', name: '买家', icon: '🛒', description: '浏览商品、下单购买' },
  seller: { code: 'seller', name: '卖家', icon: '🏪', description: '发布商品、管理订单' },
  ops: { code: 'ops', name: '运营管理员', icon: '⚙️', description: '系统管理、审核运营' },
};

async function submit() {
  loading.value = true;
  try {
    let result;
    if (props.appType === "ops") {
      result = await opsLogin(form);
    } else {
      result = await login(form);
    }

    // 检查用户是否拥有多个角色
    const userRoles = result?.user?.roles || [form.username];
    
    if (userRoles.length > 1) {
      // 多角色：显示角色选择界面
      availableRoles.value = userRoles.map(roleCode => ({
        ...roleMap[roleCode],
        code: roleCode,
        name: roleMap[roleCode]?.name || roleCode,
      }));
      pendingLoginResult.value = { ...result, selectedRole: null };
      showRoleSelect.value = true;
      Message.info('请选择要使用的角色');
    } else {
      // 单角色：直接完成登录
      Message.success("登录成功");
      emits("login-success");
    }
  } catch (error) {
    Message.error(error.message || "登录失败");
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
    // 将选中的角色附加到登录结果中
    if (pendingLoginResult.value) {
      pendingLoginResult.value.selectedRole = selectedRole.value;
      pendingLoginResult.value.user.currentRole = selectedRole.value;
    }
    Message.success(`已切换到 ${roleMap[selectedRole.value]?.name || selectedRole.value} 角色`);
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

.role-select-section {
  text-align: center;
}

.role-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.role-subtitle {
  margin: 0 0 20px;
  font-size: 13px;
  color: #86909c;
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
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;

  &:hover {
    border-color: #86909c;
    transform: translateX(4px);
  }

  &.active {
    border-color: #336ad8;
    background: #f0f5fe;
    box-shadow: 0 2px 8px rgba(51, 106, 216, 0.15);
  }
}

.role-icon {
  width: 48px;
  height: 48px;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  border-radius: 12px;
  flex-shrink: 0;
}

.role-info {
  flex: 1;
  text-align: left;
}

.role-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.role-desc {
  font-size: 12px;
  color: #86909c;
}

.role-check {
  font-size: 20px;
  color: #336ad8;
  flex-shrink: 0;
}
</style>
