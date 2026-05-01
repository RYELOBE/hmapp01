<template>
  <div class="role-manage-page">
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
            <h2 class="page-title">角色管理</h2>
            <span class="page-subtitle">管理角色与资源菜单的权限绑定</span>
          </div>
          <div class="page-header__right">
            <a-button type="primary" @click="showAddModal = true">
              <template #icon><icon-plus /></template>
              新建角色
            </a-button>
          </div>
        </div>

        <a-spin :loading="loading">
          <!-- 角色列表 -->
          <a-card title="角色列表" :bordered="false" class="role-card">
            <template #extra>
              <a-input-search
                v-model="searchKeyword"
                placeholder="搜索角色名称或编码"
                style="width: 260px"
                search-button
                @search="handleSearch"
              />
            </template>
            <RoleList
              :roles="filteredRoles"
              :loading="loading"
              @edit="handleEdit"
              @delete="handleDelete"
              @toggle-status="handleToggleStatus"
              @permissions="handlePermissions"
            />
          </a-card>

          <!-- 权限配置抽屉 -->
          <a-drawer
            v-model:visible="showPermissionDrawer"
            title="权限配置"
            :width="480"
            :height="800"
          >
            <PermissionConfig
              v-if="selectedRole"
              :role-code="selectedRole.roleCode"
              :role-name="selectedRole.roleName"
              :menu-tree="menuTree"
              :initial-checked-keys="rolePermissions"
              @save-success="handlePermissionSave"
            />
          </a-drawer>
        </a-spin>

        <!-- 新建/编辑角色弹窗 -->
        <a-modal
          v-model:visible="showAddModal"
          :title="isEdit ? '编辑角色' : '新建角色'"
          @ok="handleSaveRole"
          @cancel="handleCancel"
        >
          <a-form :model="roleForm" label-align="left" label-width="100px">
            <a-form-item label="角色名称" required>
              <a-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
            </a-form-item>
            <a-form-item label="角色编码" :required="!isEdit">
              <a-input
                v-model="roleForm.roleCode"
                :disabled="isEdit"
                placeholder="请输入角色编码（如：CUSTOMER）"
              />
            </a-form-item>
            <a-form-item label="角色描述">
              <a-textarea v-model="roleForm.description" placeholder="请输入角色描述" :rows="3" />
            </a-form-item>
            <a-form-item label="状态">
              <a-switch
                v-model="roleForm.status"
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
import RoleList from '../components/RoleList.vue';
import PermissionConfig from '../components/PermissionConfig.vue';
import PermissionMenuTree from '../components/PermissionMenuTree.vue';
import {
  getAllRoles,
  getResourceMenuTree,
  getRoleResources,
  createRole,
  updateRole,
  getRoleDetail,
  updateRoleStatus,
  deleteRole
} from '../services/api';

const router = useRouter();
const loading = ref(false);
const roles = ref([]);
const menuTree = ref([]);
const searchKeyword = ref('');
const showAddModal = ref(false);
const showPermissionDrawer = ref(false);
const isEdit = ref(false);
const selectedRole = ref(null);
const rolePermissions = ref([]);

function handleMenuSelect(key) {
  if (key === 'org-user') {
    router.push('/ops/user-manage-2');
  } else if (key === 'resource-manage') {
    router.push('/ops/resource-manage');
  } else if (key === 'role-manage') {
    router.push('/ops/role-manage');
  }
}

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  description: '',
  status: 'ACTIVE'
});

const filteredRoles = computed(() => {
  if (!searchKeyword.value) return roles.value;
  const keyword = searchKeyword.value.toLowerCase();
  return roles.value.filter(role =>
    role.roleName.toLowerCase().includes(keyword) ||
    role.roleCode.toLowerCase().includes(keyword)
  );
});

async function loadData() {
  loading.value = true;
  try {
    const [rolesRes, treeRes] = await Promise.all([
      getAllRoles(),
      getResourceMenuTree()
    ]);
    roles.value = rolesRes.roles || [];
    menuTree.value = treeRes.tree || [];
  } catch (e) {
    Message.error('加载失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
}

function handleEdit(role) {
  isEdit.value = true;
  Object.assign(roleForm, role);
  showAddModal.value = true;
}

function handleDelete(role) {
  Modal.confirm({
    title: '删除角色',
    content: `确定要删除角色「${role.roleName}」吗？`,
    onOk: async () => {
      try {
        await deleteRole(role.roleCode);
        roles.value = roles.value.filter(r => r.roleCode !== role.roleCode);
        Message.success('删除成功');
      } catch (e) {
        Message.error('删除失败：' + e.message);
      }
    }
  });
}

async function handleToggleStatus(role) {
  const newStatus = role.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  try {
    await updateRoleStatus(role.roleCode, newStatus);
    role.status = newStatus;
    Message.success(`角色已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`);
  } catch (e) {
    Message.error('操作失败：' + e.message);
  }
}

async function handlePermissions(role) {
  selectedRole.value = role;
  try {
    const res = await getRoleResources(role.roleCode);
    rolePermissions.value = res.resourceIds || [];
    showPermissionDrawer.value = true;
  } catch (e) {
    Message.error('加载权限失败：' + e.message);
  }
}

function handlePermissionSave({ roleCode, count }) {
  const role = roles.value.find(r => r.roleCode === roleCode);
  if (role) {
    role.resourceCount = count;
  }
  showPermissionDrawer.value = false;
}

function handleSaveRole() {
  if (!roleForm.roleName) {
    Message.warning('请输入角色名称');
    return;
  }
  if (!isEdit.value && !roleForm.roleCode) {
    Message.warning('请输入角色编码');
    return;
  }

  const action = isEdit.value ? updateRole(roleForm.roleCode, roleForm) : createRole(roleForm);
  
  action.then(() => {
    Message.success(isEdit.value ? '编辑成功' : '创建成功');
    showAddModal.value = false;
    loadData();
  }).catch(e => {
    Message.error('保存失败：' + e.message);
  });
}

function handleCancel() {
  showAddModal.value = false;
  Object.keys(roleForm).forEach(key => {
    roleForm[key] = key === 'status' ? 'ACTIVE' : '';
  });
  isEdit.value = false;
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.role-manage-page {
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

  .role-card {
    :deep(.arco-card-body) {
      padding: 0;
    }
  }
}
</style>