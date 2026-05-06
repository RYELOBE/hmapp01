<template>
  <div class="role-manage-page">
    <div class="page-header">
      <h2 class="page-title">角色权限管理</h2>
      <span class="page-desc">超级管理员专属 - 管理系统角色与权限分配</span>
    </div>

    <a-card :bordered="false" class="main-card">
      <template #title>
        <div class="card-title">
          <span>角色列表</span>
          <a-button type="primary" size="small" @click="openCreateModal">
            <template #icon><icon-plus /></template>
            新建角色
          </a-button>
        </div>
      </template>
      <template #extra>
        <a-input-search
          v-model="searchKeyword"
          placeholder="搜索角色名称或编码"
          style="width: 260px"
          size="small"
          allow-clear
          @search="handleSearch"
        />
      </template>

      <a-table
        :data="filteredRoles"
        :loading="loading"
        row-key="roleCode"
        :stripe="true"
        :pagination="{ pageSize: 10, showTotal: true }"
      >
        <template #columns>
          <a-table-column title="角色名称" data-index="roleName" :width="160">
            <template #cell="{ record }">
              <a-space>
                <a-avatar :size="28" :style="{ backgroundColor: getRoleColor(record.roleCode) }">
                  {{ (record.roleName || '角')[0] }}
                </a-avatar>
                <span style="font-weight: 600; color: #1d2129;">{{ record.roleName }}</span>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="角色编码" data-index="roleCode" :width="140">
            <template #cell="{ record }">
              <a-typography-text code>{{ record.roleCode }}</a-typography-text>
            </template>
          </a-table-column>

          <a-table-column title="角色描述" data-index="description" :ellipsis="true" :tooltip="true" />

          <a-table-column title="关联用户数" data-index="userCount" :width="110" align="center">
            <template #cell="{ record }">
              <a-badge :text="record.userCount || 0" :color="record.userCount > 0 ? '#165dff' : '#c9cdd4'" />
            </template>
          </a-table-column>

          <a-table-column title="权限数量" data-index="resourceCount" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag color="arcoblue" size="small">{{ record.resourceCount || 0 }} 项</a-tag>
            </template>
          </a-table-column>

          <a-table-column title="类型" data-index="isSystem" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.isSystem ? 'purple' : 'cyan'" size="small">
                {{ record.isSystem ? '系统内置' : '自定义' }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="状态" data-index="status" :width="90" align="center">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="'ACTIVE'"
                :unchecked-value="'INACTIVE'"
                @change="(val) => handleToggleStatus(record, val)"
              />
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="180" fixed="right" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="handleEditPermissions(record)">
                  编辑权限
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="handleEdit(record)"
                >
                  编辑
                </a-button>
                <a-button
                  v-if="!record.isSystem"
                  type="text"
                  status="danger"
                  size="small"
                  @click="handleDelete(record)"
                >
                  删除
                </a-button>
                <a-tooltip v-else content="系统内置角色不可删除">
                  <a-button type="text" size="small" disabled>删除</a-button>
                </a-tooltip>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="showFormModal"
      :title="isEditing ? '编辑角色' : '新建角色'"
      :ok-loading="formSubmitting"
      @ok="handleSaveRole"
      @cancel="cancelForm"
      :unmount-on-close="true"
    >
      <a-form ref="roleFormRef" :model="roleForm" layout="vertical" :rules="formRules">
        <a-form-item label="角色名称" field="roleName" required>
          <a-input
            v-model="roleForm.roleName"
            placeholder="请输入角色名称"
            :disabled="isEditing && currentRole?.isSystem"
          />
        </a-form-item>
        <a-form-item label="角色编码" field="roleCode" required>
          <a-input
            v-model="roleForm.roleCode"
            placeholder="请输入角色编码（如：CUSTOM_ROLE）"
            :disabled="isEditing"
          />
        </a-form-item>
        <a-form-item label="角色描述" field="description">
          <a-textarea
            v-model="roleForm.description"
            placeholder="请输入角色描述"
            :auto-size="{ minRows: 3 }"
            :max-length="200"
            show-word-limit
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:visible="showPermissionDrawer"
      title="权限配置"
      :width="640"
      placement="right"
      unmount-on-close
    >
      <div v-if="currentPermissionRole" class="permission-content">
        <a-alert type="info" class="role-info-alert">
          正在为「<strong>{{ currentPermissionRole.roleName }}</strong>」({{ currentPermissionRole.roleCode }}) 配置权限
        </a-alert>

        <a-divider>权限分组配置</a-divider>

        <div class="permission-groups">
          <div v-for="group in permissionGroups" :key="group.key" class="permission-group">
            <h4 class="group-title">{{ group.label }}</h4>
            <a-checkbox-group v-model="selectedPermissions[group.key]" direction="vertical">
              <a-checkbox
                v-for="perm in group.permissions"
                :key="perm.value"
                :value="perm.value"
              >
                {{ perm.label }}
              </a-checkbox>
            </a-checkbox-group>
          </div>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import { IconPlus } from "@arco-design/web-vue/es/icon";
import {
  getAllRoles,
  createRole,
  updateRole,
  deleteRole,
  updateRoleStatus,
  saveRoleResources,
  getRoleResources,
} from "../../services/api";

const loading = ref(false);
const roles = ref([]);
const searchKeyword = ref("");
const showFormModal = ref(false);
const showPermissionDrawer = ref(false);
const formSubmitting = ref(false);
const isEditing = ref(false);
const currentRole = ref(null);
const currentPermissionRole = ref(null);
const roleFormRef = ref(null);

const roleForm = reactive({
  roleName: "",
  roleCode: "",
  description: "",
});

const formRules = {
  roleName: [{ required: true, message: "请输入角色名称" }],
  roleCode: [{ required: true, message: "请输入角色编码" }],
};

const selectedPermissions = reactive({
  item: [],
  order: [],
  user: [],
  ops: [],
  system: [],
});

const permissionGroups = [
  {
    key: "item",
    label: "📦 商品管理",
    permissions: [
      { value: "item:view", label: "浏览商品" },
      { value: "item:create", label: "发布商品" },
      { value: "item:edit", label: "编辑商品" },
      { value: "item:delete", label: "删除商品" },
      { value: "item:review", label: "审核商品" },
    ],
  },
  {
    key: "order",
    label: "🛒 订单管理",
    permissions: [
      { value: "order:create", label: "创建订单" },
      { value: "order:view", label: "查看订单" },
      { value: "order:cancel", label: "取消订单" },
      { value: "order:confirm", label: "确认订单" },
      { value: "order:refund", label: "退款操作" },
    ],
  },
  {
    key: "user",
    label: "👤 用户管理",
    permissions: [
      { value: "user:view", label: "查看用户" },
      { value: "user:edit", label: "编辑用户" },
      { value: "user:disable", label: "禁用用户" },
      { value: "user:assign_role", label: "角色分配" },
    ],
  },
  {
    key: "ops",
    label: "⚙️ 运营管理",
    permissions: [
      { value: "ops:review", label: "内容审核" },
      { value: "ops:config", label: "系统配置" },
      { value: "ops:stats", label: "数据统计" },
      { value: "ops:announce", label: "公告管理" },
    ],
  },
  {
    key: "system",
    label: "🔐 系统管理",
    permissions: [
      { value: "system:role_manage", label: "角色管理" },
      { value: "system:log_view", label: "日志查看" },
      { value: "system:menu_manage", label: "菜单管理" },
    ],
  },
];

const filteredRoles = computed(() => {
  if (!searchKeyword.value) return roles.value;
  const keyword = searchKeyword.value.toLowerCase();
  return roles.value.filter(
    (role) =>
      role.roleName?.toLowerCase().includes(keyword) ||
      role.roleCode?.toLowerCase().includes(keyword)
  );
});

function getRoleColor(roleCode) {
  const colors = {
    BUYER: "#165DFF",
    SELLER: "#00B42A",
    OPS_ADMIN: "#FF7D00",
    OPS_SUPER: "#F53F3F",
  };
  return colors[roleCode] || "#86909C";
}

async function loadData() {
  loading.value = true;
  try {
    const res = await getAllRoles();
    roles.value = res?.roles || [];
  } catch (e) {
    Message.error("加载角色列表失败：" + e.message);
  } finally {
    loading.value = false;
  }
}

function handleSearch() {}

function openCreateModal() {
  isEditing.value = false;
  currentRole.value = null;
  Object.assign(roleForm, { roleName: "", roleCode: "", description: "" });
  showFormModal.value = true;
}

function handleEdit(record) {
  isEditing.value = true;
  currentRole.value = record;
  Object.assign(roleForm, {
    roleName: record.roleName,
    roleCode: record.roleCode,
    description: record.description || "",
  });
  showFormModal.value = true;
}

async function handleSaveRole() {
  if (!roleFormRef.value) return;

  try {
    await roleFormRef.value.validate();
  } catch {
    return;
  }

  formSubmitting.value = true;
  try {
    if (isEditing.value) {
      await updateRole(roleForm.roleCode, roleForm);
      Message.success("角色更新成功");
    } else {
      await createRole(roleForm);
      Message.success("角色创建成功");
    }
    showFormModal.value = false;
    loadData();
  } catch (e) {
    Message.error("保存失败：" + e.message);
  } finally {
    formSubmitting.value = false;
  }
}

function cancelForm() {
  showFormModal.value = false;
  Object.assign(roleForm, { roleName: "", roleCode: "", description: "" });
  isEditing.value = false;
  currentRole.value = null;
}

function handleDelete(role) {
  if (role.isSystem) {
    Message.warning("系统内置角色不可删除");
    return;
  }

  const userCount = role.userCount || 0;
  Modal.confirm({
    title: "删除角色",
    content: userCount > 0
      ? `确定要删除角色「${role.roleName}」吗？该角色下有 ${userCount} 个用户，删除后这些用户将失去相关权限。`
      : `确定要删除角色「${role.roleName}」吗？`,
    okText: "确认删除",
    cancelText: "取消",
    status: "danger",
    onOk: async () => {
      try {
        await deleteRole(role.roleCode);
        Message.success("角色已删除");
        loadData();
      } catch (e) {
        Message.error("删除失败：" + e.message);
      }
    },
  });
}

async function handleToggleStatus(role, val) {
  try {
    await updateRoleStatus(role.roleCode, val);
    Message.success(`角色已${val === "ACTIVE" ? "启用" : "禁用"}`);
  } catch (e) {
    Message.error("操作失败：" + e.message);
    role.status = val === "ACTIVE" ? "INACTIVE" : "ACTIVE";
  }
}

async function handleEditPermissions(role) {
  currentPermissionRole.value = role;

  Object.keys(selectedPermissions).forEach((key) => {
    selectedPermissions[key] = [];
  });

  try {
    const res = await getRoleResources(role.roleCode);
    const resourceIds = res?.resourceIds || [];

    permissionGroups.forEach((group) => {
      const groupPerms = group.permissions.map((p) => p.value);
      selectedPermissions[group.key] = resourceIds.filter((id) =>
        groupPerms.includes(id)
      );
    });

    showPermissionDrawer.value = true;
  } catch (e) {
    Message.error("加载权限失败：" + e.message);
  }
}

async function savePermissions() {
  if (!currentPermissionRole.value) return;

  const allSelectedPermissions = Object.values(selectedPermissions).flat();
  formSubmitting.value = true;

  try {
    await saveRoleResources(currentPermissionRole.value.roleCode, allSelectedPermissions);
    Message.success("权限保存成功");
    showPermissionDrawer.value = false;
    loadData();
  } catch (e) {
    Message.error("保存失败：" + e.message);
  } finally {
    formSubmitting.value = false;
  }
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.role-manage-page {
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

.main-card {
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
  gap: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.permission-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.role-info-alert {
  margin-bottom: 12px;
}

.permission-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.permission-group {
  .group-title {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
    padding-bottom: 8px;
    border-bottom: 1px solid #e5e6eb;
  }

  :deep(.arco-checkbox-group) {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
  }
}
</style>
