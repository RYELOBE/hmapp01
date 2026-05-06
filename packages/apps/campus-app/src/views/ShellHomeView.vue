<template>
  <div class="shell-home">
    <a-card title="欢迎使用 CampusTrade" class="welcome-card">
      <p class="welcome-text">校园二手交易平台，让闲置物品焕发新生</p>
      <a-space class="action-buttons">
        <a-button type="primary" @click="goToPortal">进入交易中心</a-button>
        <a-button @click="goToOps" v-if="hasOpsRole">进入运营中心</a-button>
      </a-space>
    </a-card>

    <a-row :gutter="16">
      <a-col :span="8">
        <a-card hoverable class="stat-card">
          <a-statistic title="在售商品" :value="stats.items" suffix="件" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card hoverable class="stat-card">
          <a-statistic title="订单总数" :value="stats.orders" suffix="单" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card hoverable class="stat-card">
          <a-statistic title="活跃用户" :value="stats.users" suffix="人" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="快捷操作" class="quick-actions">
      <a-space :wrap="true">
        <a-button type="outline" v-for="action in quickActions" :key="action.key" @click="handleAction(action)">
          <component :is="action.icon" />
          {{ action.label }}
        </a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import {
  IconList,
  IconUpload,
  IconFile,
  IconBarChart,
  IconUser,
} from "@arco-design/web-vue/es/icon";

const router = useRouter();
const authStore = useAuthStore();

const stats = ref({
  items: 1258,
  orders: 3256,
  users: 2341,
});

const hasOpsRole = computed(() => authStore.roles.includes("OPS"));

const quickActions = computed(() => {
  const actions = [];
  const roles = authStore.roles;

  actions.push({
    key: "browse",
    label: "浏览商品",
    icon: IconList,
    path: "/portal/home",
  });

  if (roles.includes("SELLER")) {
    actions.push({
      key: "publish",
      label: "发布商品",
      icon: IconUpload,
      path: "/portal/seller/publish",
    });
    actions.push({
      key: "my-items",
      label: "我的商品",
      icon: IconFile,
      path: "/portal/seller/items",
    });
  }

  if (roles.includes("BUYER")) {
    actions.push({
      key: "cart",
      label: "购物车",
      icon: IconList,
      path: "/portal/cart",
    });
    actions.push({
      key: "favorites",
      label: "收藏夹",
      icon: IconUser,
      path: "/portal/favorites",
    });
  }

  if (roles.includes("OPS")) {
    actions.push({
      key: "dashboard",
      label: "数据概览",
      icon: IconBarChart,
      path: "/ops/dashboard",
    });
  }

  return actions;
});

function goToPortal() {
  router.push("/portal/home");
}

function goToOps() {
  router.push("/ops/dashboard");
}

function handleAction(action) {
  router.push(action.path);
}
</script>

<style scoped>
.shell-home {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-text {
  font-size: 16px;
  color: #666;
  margin: 16px 0;
}

.action-buttons {
  margin-top: 16px;
}

.stat-card {
  height: 100%;
}

.quick-actions {
  margin-top: 20px;
}
</style>