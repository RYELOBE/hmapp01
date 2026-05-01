<template>
  <div class="user-manage-page">
    <a-layout class="page-layout">
      <!-- 左侧菜单 -->
      <a-layout-sider :width="240" class="menu-sider">
        <PermissionMenuTree @select="handleMenuSelect" />
      </a-layout-sider>

      <!-- 右侧内容 -->
      <a-layout-content class="main-content">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="page-header__left">
            <h2 class="page-title">组织用户管理</h2>
            <span class="page-subtitle">管理组织架构和用户关系</span>
          </div>
          <div class="page-header__right">
            <a-button type="primary" @click="showAddUserModal = true">
              <template #icon><icon-plus /></template>
              新增用户
            </a-button>
          </div>
        </div>

        <a-spin :loading="loading">
          <a-row :gutter="16">
            <!-- 左侧组织树 -->
            <a-col :span="7">
              <a-card :bordered="false" class="org-card">
                <OrgTree @select="handleSelectOrg" />
              </a-card>
            </a-col>

            <!-- 右侧用户列表 -->
            <a-col :span="17">
              <a-card title="用户列表" :bordered="false" class="user-card">
                <template #extra>
                  <a-input-search
                    v-model="searchKeyword"
                    placeholder="搜索用户名或邮箱"
                    style="width: 260px"
                    search-button
                    @search="handleSearch"
                  />
                </template>
                <a-table
                  :columns="columns"
                  :data="filteredUsers"
                  :loading="loading"
                  :pagination="{ pageSize: 10, showTotal: true }"
                  :row-key="'id'"
                >
                  <template #avatar="{ record }">
                    <a-avatar :size="40" :style="{ backgroundColor: '#165dff' }">
                      {{ record.name ? record.name.charAt(0).toUpperCase() : 'U' }}
                    </a-avatar>
                  </template>
                  <template #status="{ record }">
                    <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
                      {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
                    </a-tag>
                  </template>
                  <template #actions="{ record }">
                    <a-space :size="8">
                      <a-button size="small" @click="handleEditUser(record)">编辑</a-button>
                      <a-button
                        size="small"
                        :status="record.status === 'ACTIVE' ? 'warning' : 'success'"
                        @click="handleToggleStatus(record)"
                      >
                        {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
                      </a-button>
                      <a-button size="small" status="danger" @click="handleDeleteUser(record)">删除</a-button>
                    </a-space>
                  </template>
                </a-table>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>

        <!-- 新增/编辑用户弹窗 -->
        <a-modal
          v-model:visible="showAddUserModal"
          :title="isEditUser ? '编辑用户' : '新增用户'"
          @ok="handleSaveUser"
          @cancel="handleCancelUser"
        >
          <a-form :model="userForm" label-align="left" label-width="100px">
            <a-form-item label="用户名" required>
              <a-input v-model="userForm.username" placeholder="请输入用户名" />
            </a-form-item>
            <a-form-item label="姓名" required>
              <a-input v-model="userForm.name" placeholder="请输入姓名" />
            </a-form-item>
            <a-form-item label="邮箱">
              <a-input v-model="userForm.email" placeholder="请输入邮箱" />
            </a-form-item>
            <a-form-item label="所属组织">
              <a-select v-model="userForm.orgId" placeholder="请选择所属组织">
                <a-option value="org-1">校园二手交易平台</a-option>
                <a-option value="dept-1">运营部</a-option>
                <a-option value="dept-2">技术部</a-option>
                <a-option value="dept-3">市场部</a-option>
              </a-select>
            </a-form-item>
            <a-form-item label="状态">
              <a-switch
                v-model="userForm.status"
                :checked-value="'ACTIVE'"
                :unchecked-value="'INACTIVE'"
              />
            </a-form-item>
          </a-form>
        </a-modal>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import PermissionMenuTree from '../components/PermissionMenuTree.vue';
import OrgTree from '../components/OrgTree.vue';
import { getUsers, updateUserStatus } from '../services/api';

const router = useRouter();
const loading = ref(false);
const users = ref([]);
const searchKeyword = ref('');
const showAddUserModal = ref(false);
const isEditUser = ref(false);
const selectedOrgId = ref(null);

const userForm = reactive({
  id: null,
  username: '',
  name: '',
  email: '',
  orgId: null,
  status: 'ACTIVE'
});

const mockUsers = [
  { id: 'user-1', username: 'zhangsan', name: '张三', email: 'zhangsan@example.com', orgId: 'dept-1', status: 'ACTIVE', createTime: '2024-01-15' },
  { id: 'user-2', username: 'lisi', name: '李四', email: 'lisi@example.com', orgId: 'dept-1', status: 'ACTIVE', createTime: '2024-01-20' },
  { id: 'user-3', username: 'wangwu', name: '王五', email: 'wangwu@example.com', orgId: 'dept-2', status: 'ACTIVE', createTime: '2024-02-01' },
  { id: 'user-4', username: 'zhaoliu', name: '赵六', email: 'zhaoliu@example.com', orgId: 'dept-2', status: 'INACTIVE', createTime: '2024-02-10' },
  { id: 'user-5', username: 'qianqi', name: '钱七', email: 'qianqi@example.com', orgId: 'dept-3', status: 'ACTIVE', createTime: '2024-03-01' }
];

const columns = [
  { title: '头像', slot: 'avatar', width: 80 },
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '姓名', dataIndex: 'name', width: 120 },
  { title: '邮箱', dataIndex: 'email', width: 200 },
  { title: '状态', slot: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 150 },
  { title: '操作', slot: 'actions', width: 260, fixed: 'right' }
];

const filteredUsers = computed(() => {
  let result = users.value;
  if (searchKeyword.value) {
    result = result.filter(user =>
      (user.username && user.username.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
      (user.name && user.name.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
      (user.email && user.email.toLowerCase().includes(searchKeyword.value.toLowerCase()))
    );
  }
  if (selectedOrgId.value) {
    result = result.filter(user => user.orgId === selectedOrgId.value || selectedOrgId.value === 'org-1');
  }
  return result;
});

async function loadData() {
  loading.value = true;
  try {
    users.value = mockUsers;
  } catch (e) {
    Message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
}

function handleMenuSelect(key) {
  if (key === 'org-user') {
    router.push('/ops/user-manage-2');
  } else if (key === 'resource-manage') {
    router.push('/ops/resource-manage');
  } else if (key === 'role-manage') {
    router.push('/ops/role-manage');
  }
}

function handleSelectOrg(selectedKeys) {
  if (selectedKeys.length > 0) {
    selectedOrgId.value = selectedKeys[0];
  } else {
    selectedOrgId.value = null;
  }
}

function handleSearch() {
  // 搜索已通过 computed 属性实现
}

function handleEditUser(user) {
  isEditUser.value = true;
  Object.assign(userForm, user);
  showAddUserModal.value = true;
}

function handleDeleteUser(user) {
  Modal.confirm({
    title: '删除用户',
    content: `确定要删除用户「${user.name}」吗？`,
    onOk: () => {
      users.value = users.value.filter(u => u.id !== user.id);
      Message.success('删除成功');
    }
  });
}

async function handleToggleStatus(user) {
  const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  try {
    user.status = newStatus;
    Message.success(`用户已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`);
  } catch (e) {
    Message.error('操作失败');
  }
}

function handleSaveUser() {
  if (!userForm.username || !userForm.name) {
    Message.warning('请填写必填项');
    return;
  }

  if (isEditUser.value) {
    const index = users.value.findIndex(u => u.id === userForm.id);
    if (index !== -1) {
      users.value[index] = { ...userForm };
    }
    Message.success('编辑成功');
  } else {
    const newUser = {
      ...userForm,
      id: `user-${Date.now()}`,
      createTime: new Date().toISOString().split('T')[0]
    };
    users.value.unshift(newUser);
    Message.success('创建成功');
  }

  showAddUserModal.value = false;
  handleCancelUser();
}

function handleCancelUser() {
  Object.keys(userForm).forEach(key => {
    userForm[key] = key === 'status' ? 'ACTIVE' : null;
  });
  isEditUser.value = false;
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.user-manage-page {
  height: calc(100vh - 64px);

  .page-layout {
    height: 100%;
  }

  .menu-sider {
    background: #fff;
    height: 100%;
    overflow: auto;
  }

  .main-content {
    padding: 20px;
    overflow: auto;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    &__left {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    &__right {
      display: flex;
      gap: 12px;
    }
  }

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: #1d2129;
  }

  .page-subtitle {
    font-size: 13px;
    color: #86909c;
  }

  .org-card {
    height: calc(100vh - 180px);
    overflow: hidden;
  }

  .user-card {
    height: calc(100vh - 180px);
    overflow: hidden;
    display: flex;
    flex-direction: column;

    :deep(.arco-card-body) {
      flex: 1;
      overflow: auto;
    }
  }
}
</style>
