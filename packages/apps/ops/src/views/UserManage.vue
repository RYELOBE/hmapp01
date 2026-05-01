<template>
  <div class="user-manage">
    <div class="user-manage__header">
      <h2>用户管理</h2>
      <span class="user-manage__desc">管理平台所有用户账号及其权限</span>
    </div>

    <a-space direction="vertical" :size="16" style="width: 100%">
      <a-row :gutter="16">
        <a-col :span="6" v-for="card in statCards" :key="card.key">
          <a-card size="small" :bordered="false" class="stat-card">
            <a-statistic
              :title="card.label"
              :value="stats[card.key] ?? 0"
              :value-from="0"
              :animation="true"
            >
              <template #prefix>
                <span class="stat-icon">{{ card.icon }}</span>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false">
        <template #title>
          <a-space>
            <span>用户列表</span>
            <a-divider direction="vertical" />
            <span style="color: var(--color-text-3); font-weight: normal;">
              共 {{ pagination.total }} 个用户
            </span>
          </a-space>
        </template>
        <template #extra>
          <a-space>
            <a-select
              v-model="filterStatus"
              placeholder="筛选状态"
              style="width: 120px"
              @change="handleFilterChange"
            >
              <a-option value="">全部状态</a-option>
              <a-option value="ACTIVE">正常</a-option>
              <a-option value="DISABLED">禁用</a-option>
            </a-select>
            <a-input-search
              v-model="keyword"
              placeholder="搜索用户名/手机号"
              style="width: 240px"
              search-button
              @search="handleSearch"
              @press-enter="handleSearch"
            />
          </a-space>
        </template>

        <a-table
          :data="tableData"
          :loading="loading"
          :pagination="paginationConfig"
          :columns="USER_COLUMNS"
          row-key="id"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
          :scroll="{ x: 1000 }"
        >
          <template #avatar="{ record }">
            <a-avatar :size="32" :style="{ backgroundColor: record.avatarColor || '#1650d2' }">
              {{ record.username?.[0]?.toUpperCase() || '用' }}
            </a-avatar>
          </template>

          <template #username="{ record }">
            <a-space direction="horizontal" :size="8">
              <span style="font-weight: 500;">{{ record.username }}</span>
              <a-tag v-if="record.isVip" color="gold" size="small">VIP</a-tag>
            </a-space>
          </template>

          <template #role="{ record }">
            <a-tag :color="getRoleColor(record.role)" size="small">
              {{ getRoleLabel(record.role) }}
            </a-tag>
          </template>

          <template #status="{ record }">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'" size="small">
              {{ record.status === 'ACTIVE' ? '正常' : '禁用' }}
            </a-tag>
          </template>

          <template #userStats="{ record }">
            <a-space direction="vertical" :size="2" style="font-size: 12px;">
              <span>订单: {{ record.totalOrders || 0 }}</span>
              <span>商品: {{ record.totalItems || 0 }}</span>
            </a-space>
          </template>

          <template #createdAt="{ record }">
            {{ formatDate(record.createdAt) }}
          </template>

          <template #actions="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="viewDetail(record)">
                详情
              </a-button>
              <a-dropdown trigger="click">
                <a-button type="text" size="small">
                  操作 <icon-down />
                </a-button>
                <template #content>
                  <a-doption @click="toggleUserStatus(record)">
                    <template #icon><icon-swap /></template>
                    {{ record.status === 'ACTIVE' ? '禁用账号' : '启用账号' }}
                  </a-doption>
                  <a-doption @click="changeUserRole(record)" v-if="canChangeRole">
                    <template #icon><icon-user-edit /></template>
                    修改角色
                  </a-doption>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </a-table>
      </a-card>
    </a-space>

    <a-drawer
      v-model:visible="detailVisible"
      :title="`用户详情 - ${currentUser?.username || ''}`"
      :width="480"
      :footer="false"
    >
      <a-descriptions :column="1" bordered size="large">
        <a-descriptions-item label="用户ID">
          {{ currentUser?.id || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ currentUser?.username || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ currentUser?.phone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="邮箱">
          {{ currentUser?.email || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户角色">
          <a-tag :color="getRoleColor(currentUser?.role)" size="small">
            {{ getRoleLabel(currentUser?.role) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="账号状态">
          <a-tag :color="currentUser?.status === 'ACTIVE' ? 'green' : 'red'" size="small">
            {{ currentUser?.status === 'ACTIVE' ? '正常' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间">
          {{ formatDate(currentUser?.createdAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="最后登录">
          {{ formatDate(currentUser?.lastLoginAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="订单总数">
          {{ currentUser?.totalOrders || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="发布商品">
          {{ currentUser?.totalItems || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="总消费">
          <span style="color: #f53f3f; font-weight: 600;">
            ¥{{ currentUser?.totalSpent || 0 }}
          </span>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>

    <a-modal
      v-model:visible="roleModalVisible"
      title="修改用户角色"
      @before-ok="handleRoleSubmit"
      @cancel="roleModalVisible = false"
    >
      <a-form :model="roleForm" layout="vertical">
        <a-form-item label="选择角色">
          <a-select v-model="roleForm.role" placeholder="请选择用户角色">
            <a-option value="BUYER">买家</a-option>
            <a-option value="SELLER">卖家</a-option>
            <a-option value="BOTH">买卖家</a-option>
            <a-option value="OPS">运营管理员</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  getUsers,
  getUserDetail,
  updateUserStatus,
  updateUserRole,
} from "../services/api";

const USER_COLUMNS = [
  { title: "头像", dataIndex: "avatar", width: 80, fixed: "left" },
  { title: "用户名", dataIndex: "username", width: 140 },
  { title: "手机号", dataIndex: "phone", width: 130 },
  { title: "角色", dataIndex: "role", width: 100 },
  { title: "状态", dataIndex: "status", width: 80 },
  { title: "用户统计", dataIndex: "userStats", width: 120 },
  { title: "注册时间", dataIndex: "createdAt", width: 160 },
  { title: "操作", dataIndex: "actions", width: 160, fixed: "right" },
];

const statCards = [
  { key: "totalUsers", label: "用户总数", icon: "👥" },
  { key: "activeUsers", label: "活跃用户", icon: "🔥" },
  { key: "newUsersToday", label: "今日新增", icon: "✨" },
  { key: "disabledUsers", label: "禁用账号", icon: "🚫" },
];

const tableData = ref([]);
const loading = ref(false);
const stats = ref({});
const keyword = ref("");
const filterStatus = ref("");
const detailVisible = ref(false);
const roleModalVisible = ref(false);
const currentUser = ref(null);
const canChangeRole = ref(true);

const roleForm = reactive({
  role: "",
  userId: null,
});

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50, 100],
});

const paginationConfig = computed(() => ({
  ...pagination.value,
  total: pagination.value.total,
}));

function getRoleColor(role) {
  const colors = {
    BUYER: "arcoblue",
    SELLER: "green",
    BOTH: "purple",
    OPS: "orange",
  };
  return colors[role] || "gray";
}

function getRoleLabel(role) {
  const labels = {
    BUYER: "买家",
    SELLER: "卖家",
    BOTH: "买卖家",
    OPS: "运营",
  };
  return labels[role] || role || "未知";
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadStats() {
  try {
    const res = await getUsers({ stats: true });
    stats.value = res?.stats || {};
  } catch (e) {
    console.error("[UserManage] load stats error:", e);
  }
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      status: filterStatus.value || undefined,
    };
    const res = await getUsers(params);
    tableData.value = res?.users || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[UserManage] load error:", e);
    Message.error("加载用户列表失败");
  } finally {
    loading.value = false;
  }
}

async function viewDetail(record) {
  try {
    const res = await getUserDetail(record.id);
    currentUser.value = res?.user || res || record;
    detailVisible.value = true;
  } catch (e) {
    console.error("[UserManage] view detail error:", e);
    Message.error("获取用户详情失败");
  }
}

async function toggleUserStatus(record) {
  const newStatus = record.status === "ACTIVE" ? "DISABLED" : "ACTIVE";
  const action = newStatus === "ACTIVE" ? "启用" : "禁用";

  try {
    await updateUserStatus(record.id, newStatus);
    Message.success(`用户已${action}`);
    loadData();
  } catch (e) {
    console.error("[UserManage] toggle status error:", e);
    Message.error(`${action}用户失败`);
  }
}

function changeUserRole(record) {
  currentUser.value = record;
  roleForm.userId = record.id;
  roleForm.role = record.role || "BUYER";
  roleModalVisible.value = true;
}

async function handleRoleSubmit(done) {
  try {
    await updateUserRole(roleForm.userId, roleForm.role);
    Message.success("角色修改成功");
    roleModalVisible.value = false;
    loadData();
    done(true);
  } catch (e) {
    console.error("[UserManage] update role error:", e);
    Message.error("角色修改失败");
    done(false);
  }
}

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handleFilterChange() {
  pagination.value.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

function handlePageSizeChange(pageSize) {
  pagination.value.pageSize = pageSize;
  pagination.value.current = 1;
  loadData();
}

onMounted(() => {
  loadStats();
  loadData();
});
</script>

<style lang="scss" scoped>
.user-manage {
  &__header {
    margin-bottom: 16px;
    h2 {
      margin: 0 0 4px;
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text-1);
    }
  }

  &__desc {
    font-size: 13px;
    color: var(--color-text-3);
  }
}

.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #f7f8fa 100%);
  border-radius: 8px;

  :deep(.arco-statistic-title) {
    color: var(--color-text-3);
    font-size: 13px;
  }

  :deep(.arco-statistic-content) {
    margin-top: 4px;
  }

  .stat-icon {
    margin-right: 4px;
  }
}
</style>
