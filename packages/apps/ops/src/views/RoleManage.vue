<template>
  <div class="role-manage-page">
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-title">角色管理</h2>
        <span class="page-subtitle">管理角色与资源菜单的权限绑定</span>
      </div>
    </div>

    <a-spin :loading="loading">
      <!-- 角色列表 -->
      <div class="role-card-list">
        <a-card
          v-for="role in roles"
          :key="role"
          hoverable
          class="role-card"
          :class="{ 'role-card--active': selectedRole === role }"
          @click="selectRoleAction(role)"
        >
          <div class="role-card__header">
            <a-tag :color="roleColor(role)" size="large">{{ roleLabel(role) }}</a-tag>
            <span class="role-card__code">{{ role }}</span>
          </div>
          <div class="role-card__info">
            已授权 <strong>{{ roleResourceCount[role] || 0 }}</strong> 个资源
          </div>
        </a-card>
      </div>

      <!-- 资源树 -->
      <div v-if="selectedRole" class="resource-panel">
        <div class="resource-panel__header">
          <h3>{{ roleLabel(selectedRole) }} — 资源授权</h3>
          <a-space>
            <a-button @click="toggleAll(true)">全选</a-button>
            <a-button @click="toggleAll(false)">全不选</a-button>
            <a-button type="primary" :loading="saving" @click="savePermissions">保存</a-button>
          </a-space>
        </div>
        <a-tree
          ref="treeRef"
          :data="menuTree"
          :field-names="{ key: 'id', title: 'menuName', children: 'children' }"
          :checkable="true"
          :checked-keys="checkedKeys"
          :check-strictly="true"
          @check="onCheck"
          class="resource-tree"
        />
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { getAllRoles, getResourceMenuTree, getRoleResources, saveRoleResources } from '../services/api';

const loading = ref(false);
const saving = ref(false);
const roles = ref([]);
const menuTree = ref([]);
const selectedRole = ref(null);
const checkedKeys = ref([]);
const roleResourceCount = reactive({});
const treeRef = ref(null);

const ROLE_MAP = { BUYER: '买家', SELLER: '卖家', OPS: '运营' };
const ROLE_COLORS = { BUYER: 'green', SELLER: 'blue', OPS: 'orange' };

function roleLabel(r) { return ROLE_MAP[r] || r; }
function roleColor(r) { return ROLE_COLORS[r] || 'gray'; }

async function loadData() {
  loading.value = true;
  try {
    const [rolesRes, treeRes] = await Promise.all([
      getAllRoles(),
      getResourceMenuTree()
    ]);
    roles.value = rolesRes.roles || [];
    menuTree.value = buildTreeChecked(treeRes.tree || []);
    // 加载每个角色的资源数量
    for (const role of roles.value) {
      const res = await getRoleResources(role);
      roleResourceCount[role] = (res.resourceIds || []).length;
    }
  } catch (e) {
    Message.error('加载失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

function buildTreeChecked(tree) {
  return (tree || []).map(node => ({
    ...node,
    children: node.children ? buildTreeChecked(node.children) : [],
  }));
}

async function selectRoleAction(role) {
  selectedRole.value = role;
  try {
    const res = await getRoleResources(role);
    checkedKeys.value = res.resourceIds || [];
  } catch (e) {
    Message.error('加载权限失败：' + e.message);
  }
}

function onCheck(checked) {
  checkedKeys.value = checked;
}

function toggleAll(checked) {
  const allIds = [];
  function collectIds(nodes) {
    for (const node of nodes) {
      allIds.push(node.id);
      if (node.children) collectIds(node.children);
    }
  }
  collectIds(menuTree.value);
  checkedKeys.value = checked ? allIds : [];
}

async function savePermissions() {
  if (!selectedRole.value) return;
  saving.value = true;
  try {
    await saveRoleResources(selectedRole.value, checkedKeys.value);
    roleResourceCount[selectedRole.value] = checkedKeys.value.length;
    Message.success('权限已保存');
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.role-manage-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;

  &__left {
    display: flex;
    flex-direction: column;
    gap: 4px;
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

.role-card-list {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.role-card {
  width: 200px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;

  &--active {
    border-color: #336ad8;
    box-shadow: 0 0 0 1px #336ad8;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__code {
    font-size: 12px;
    color: #86909c;
    font-family: monospace;
  }

  &__info {
    font-size: 12px;
    color: #86909c;

    strong {
      color: #336ad8;
    }
  }
}

.resource-panel {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  padding: 20px;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
    }
  }
}

.resource-tree {
  max-height: 400px;
  overflow: auto;
}
</style>
