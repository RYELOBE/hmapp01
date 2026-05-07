<template>
  <div class="user-manage-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <span class="page-desc">管理平台所有用户账号及其权限</span>
    </div>

    <FilterBar
      :filters="filterConfig"
      v-model="filterParams"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #extra-buttons>
        <a-button @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </template>
    </FilterBar>

    <a-card :bordered="false" class="table-card">
      <template #title>
        <div class="card-title">
          <span>用户列表</span>
          <a-tag color="arcoblue" size="small" v-if="pagination.total > 0">
            共 {{ pagination.total }} 个用户
          </a-tag>
        </div>
      </template>

      <a-table
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        :stripe="true"
        :scroll="{ x: 1200 }"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="头像" data-index="avatar" :width="80" align="center" fixed="left">
            <template #cell="{ record }">
              <a-avatar :size="36" :style="{ backgroundColor: getAvatarColor(record.username) }">
                {{ (record.username || '用')[0]?.toUpperCase() }}
              </a-avatar>
            </template>
          </a-table-column>

          <a-table-column title="用户名" data-index="username" :width="130">
            <template #cell="{ record }">
              <a-space>
                <span style="font-weight: 500; color: #1d2129;">{{ record.username }}</span>
                <a-tag v-if="record.isVip" color="gold" size="small">VIP</a-tag>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="昵称" data-index="nickname" :width="120" />

          <a-table-column title="手机号" data-index="phone" :width="130" />

          <a-table-column title="角色" data-index="role" :width="140">
            <template #cell="{ record }">
              <a-space>
                <a-tag v-if="hasRole(record.roles, 'BUYER')" color="arcoblue" size="small">买家</a-tag>
                <a-tag v-if="hasRole(record.roles, 'SELLER')" color="orangered" size="small">卖家</a-tag>
                <a-tag v-if="!record.roles || (!hasRole(record.roles, 'BUYER') && !hasRole(record.roles, 'SELLER'))" color="gray" size="small">
                  {{ getRoleLabel(record.role) }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="注册时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>

          <a-table-column title="状态" data-index="status" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'" size="small">
                {{ record.status === 'ACTIVE' ? '正常' : '禁用' }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="200" fixed="right" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="viewDetail(record)">
                  查看详情
                </a-button>
                <a-button type="text" size="small" @click="openRoleModal(record)">
                  分配角色
                </a-button>
                <a-button
                  :type="record.status === 'ACTIVE' ? 'text' : 'primary'"
                  :status="record.status === 'ACTIVE' ? 'danger' : 'success'"
                  size="small"
                  @click="toggleUserStatus(record)"
                >
                  {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:visible="detailVisible"
      :title="`用户详情 - ${currentUser?.username || ''}`"
      :width="600"
      placement="right"
      unmount-on-close
    >
      <a-spin :loading="detailLoading">
        <a-descriptions v-if="currentUser" :column="1" bordered size="medium" class="detail-descriptions">
          <a-descriptions-item label="用户ID">{{ currentUser.id || '-' }}</a-descriptions-item>
          <a-descriptions-item label="用户名">{{ currentUser.username || '-' }}</a-descriptions-item>
          <a-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</a-descriptions-item>
          <a-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-space>
              <a-tag v-if="hasRole(currentUser.roles, 'BUYER')" color="arcoblue" size="small">买家</a-tag>
              <a-tag v-if="hasRole(currentUser.roles, 'SELLER')" color="orangered" size="small">卖家</a-tag>
            </a-space>
          </a-descriptions-item>
          <a-descriptions-item label="账号状态">
            <a-tag :color="currentUser?.status === 'ACTIVE' ? 'green' : 'red'" size="small">
              {{ currentUser?.status === 'ACTIVE' ? '正常' : '禁用' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="注册时间">{{ formatDate(currentUser?.createdAt) }}</a-descriptions-item>
          <a-descriptions-item label="最后登录">{{ formatDate(currentUser?.lastLoginAt) }}</a-descriptions-item>
        </a-descriptions>

        <a-divider>行为统计</a-divider>

        <a-row :gutter="[16, 16]" v-if="currentUser">
          <a-col :span="8">
            <a-statistic title="订单总数" :value="currentUser.totalOrders || 0" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="发布商品数" :value="currentUser.totalItems || 0" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="总消费金额" :value="currentUser.totalSpent || 0" prefix="¥" />
          </a-col>
        </a-row>
      </a-spin>
    </a-drawer>

    <a-modal
      v-model:visible="roleModalVisible"
      title="分配角色"
      @ok="handleRoleSubmit"
      @cancel="roleModalVisible = false"
      :ok-loading="roleSubmitting"
    >
      <a-form :model="roleForm" layout="vertical">
        <a-form-item label="选择角色" field="roles" required>
          <a-select v-model="roleForm.roles" placeholder="请选择用户角色" multiple allow-clear>
            <a-option value="BUYER">买家</a-option>
            <a-option value="SELLER">卖家</a-option>
            <a-option value="OPS_ADMIN">运营管理员</a-option>
            <a-option value="OPS_SUPER">超级管理员</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="disableModalVisible"
      :title="currentActionUser?.status === 'ACTIVE' ? '禁用用户' : '启用用户'"
      @ok="confirmToggleStatus"
      @cancel="disableModalVisible = false"
      :ok-loading="disableLoading"
    >
      <div v-if="currentActionUser?.status === 'ACTIVE'">
        <p>确定要禁用用户「{{ currentActionUser?.username }}」吗？</p>
        <a-form :model="disableForm" layout="vertical">
          <a-form-item label="禁用原因" field="reason" required>
            <a-textarea
              v-model="disableForm.reason"
              placeholder="请输入禁用原因"
              :max-length="200"
              show-word-limit
              :auto-size="{ minRows: 3 }"
            />
          </a-form-item>
        </a-form>
      </div>
      <div v-else>
        <p>确定要启用用户「{{ currentActionUser?.username }}」吗？</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import { IconRefresh } from "@arco-design/web-vue/es/icon";
import FilterBar from "../../components/data/FilterBar.vue";
import {
  getUsers,
  getUserDetail,
  updateUserStatus,
  updateUserRole,
} from "../../services/api";

const tableData = ref([]);
const loading = ref(false);
const detailLoading = ref(false);
const roleSubmitting = ref(false);
const disableLoading = ref(false);
const detailVisible = ref(false);
const roleModalVisible = ref(false);
const disableModalVisible = ref(false);
const currentUser = ref(null);
const currentActionUser = ref(null);

const filterParams = reactive({
  keyword: "",
  role: "",
  status: "",
  dateRange: [],
});

const filterConfig = [
  {
    field: "keyword",
    label: "关键词",
    type: "input",
    placeholder: "搜索用户名/昵称/手机号",
    span: 8,
  },
  {
    field: "role",
    label: "角色",
    type: "select",
    placeholder: "全部角色",
    span: 5,
    options: [
      { value: "", label: "全部角色" },
      { value: "BUYER", label: "买家" },
      { value: "SELLER", label: "卖家" },
      { value: "BOTH", label: "两者都是" },
    ],
  },
  {
    field: "status",
    label: "状态",
    type: "select",
    placeholder: "全部状态",
    span: 5,
    options: [
      { value: "", label: "全部状态" },
      { value: "ACTIVE", label: "正常" },
      { value: "DISABLED", label: "禁用" },
    ],
  },
  {
    field: "dateRange",
    label: "注册时间",
    type: "daterange",
    span: 6,
  },
];

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
});

const roleForm = reactive({
  userId: null,
  roles: [],
});

const disableForm = reactive({
  reason: "",
});

function getAvatarColor(username) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1', '#14C9C9'];
  const index = username ? username.charCodeAt(0) % colors.length : 0;
  return colors[index];
}

function hasRole(roles, role) {
  if (!roles) return false;
  if (Array.isArray(roles)) return roles.includes(role);
  if (typeof roles === 'string') return roles.includes(role);
  return false;
}

function getRoleLabel(role) {
  const labels = {
    BUYER: "买家",
    SELLER: "卖家",
    BOTH: "买卖家",
    OPS: "运营",
    OPS_ADMIN: "运营管理员",
    OPS_SUPER: "超级管理员",
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

async function loadData() {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      keyword: filterParams.keyword || undefined,
      role: filterParams.role || undefined,
      status: filterParams.status || undefined,
    };

    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      params.startDate = filterParams.dateRange[0];
      params.endDate = filterParams.dateRange[1];
    }

    const res = await getUsers(params);
    tableData.value = res?.users || res?.rows || [];
    pagination.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[UserManage] load error:", e);
    Message.error("加载用户列表失败");
  } finally {
    loading.value = false;
  }
}

async function viewDetail(record) {
  detailLoading.value = true;
  try {
    const res = await getUserDetail(record.id);
    currentUser.value = res?.user || res || record;
    detailVisible.value = true;
  } catch (e) {
    console.error("[UserManage] view detail error:", e);
    Message.error("获取用户详情失败");
  } finally {
    detailLoading.value = false;
  }
}

function openRoleModal(record) {
  currentUser.value = record;
  roleForm.userId = record.id;
  roleForm.roles = Array.isArray(record.roles) ? [...record.roles] : (record.role ? [record.role] : []);
  roleModalVisible.value = true;
}

async function handleRoleSubmit() {
  if (!roleForm.roles || roleForm.roles.length === 0) {
    Message.warning('请至少选择一个角色');
    return;
  }

  roleSubmitting.value = true;
  try {
    await updateUserRole(roleForm.userId, roleForm.roles[0]);
    Message.success("角色分配成功");
    roleModalVisible.value = false;
    loadData();
  } catch (e) {
    console.error("[UserManage] update role error:", e);
    Message.error("角色分配失败");
  } finally {
    roleSubmitting.value = false;
  }
}

function toggleUserStatus(record) {
  currentActionUser.value = record;
  disableForm.reason = "";
  disableModalVisible.value = true;
}

async function confirmToggleStatus() {
  if (!currentActionUser.value) return;

  const newStatus = currentActionUser.value.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
  const action = newStatus === 'ACTIVE' ? '启用' : '禁用';

  if (newStatus === 'DISABLED' && !disableForm.reason.trim()) {
    Message.warning('请输入禁用原因');
    return;
  }

  disableLoading.value = true;
  try {
    await updateUserStatus(currentActionUser.value.id, newStatus);
    Message.success(`用户已${action}`);
    disableModalVisible.value = false;
    loadData();
  } catch (e) {
    console.error("[UserManage] toggle status error:", e);
    Message.error(`${action}用户失败`);
  } finally {
    disableLoading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}

function handleReset() {
  Object.keys(filterParams).forEach((key) => {
    if (Array.isArray(filterParams[key])) {
      filterParams[key] = [];
    } else {
      filterParams[key] = "";
    }
  });
  handleSearch();
}

function handlePageChange(page) {
  pagination.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  pagination.pageSize = size;
  pagination.current = 1;
  loadData();
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.user-manage-page {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      margin: 0 0 4px 0;
      font-size: 20px;
      font-weight: 700;
      color: #1d2129;
    }

    .page-desc {
      font-size: 13px;
      color: #86909c;
    }
  }
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-head) {
    border-bottom: 1px solid #e5e6eb;
    padding: 16px 20px;
  }

  :deep(.arco-card-body) {
    padding: 0;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.detail-descriptions {
  :deep(.arco-descriptions-item-label) {
    width: 120px;
    font-weight: 500;
  }
}
</style>
