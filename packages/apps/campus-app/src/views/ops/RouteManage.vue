<template>
  <div class="route-manage-page">
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-title">路由配置</h2>
        <span class="page-subtitle">配置前端页面路由规则，支持拖拽排序</span>
      </div>
      <a-space>
        <a-button @click="loadMenus">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
        <a-button type="primary" @click="openForm(null)">
          <template #icon><icon-plus /></template>
          添加路由
        </a-button>
      </a-space>
    </div>

    <a-spin :loading="loading">
      <a-card :bordered="false" class="route-card">
        <template #title>
          <a-space>
            <span>路由列表</span>
            <a-divider direction="vertical" />
            <span style="color: var(--color-text-3); font-size: 13px;">
              共 {{ flatMenus.length }} 条路由规则
            </span>
          </a-space>
        </template>
        <template #extra>
          <a-popconfirm content="确认要保存所有排序变更吗？" @ok="saveAllOrders">
            <a-button type="primary" size="small" :disabled="!hasOrderChanged">
              <template #icon><icon-save /></template>
              保存排序
            </a-button>
          </a-popconfirm>
        </template>

        <a-table
          :data="flatMenus"
          :loading="loading"
          :pagination="false"
          :row-key="(record) => record.id"
          :draggable="{ type: 'row', callback: onDragEnd }"
          :columns="columns"
          @selection-change="handleSelectionChange"
        >
          <template #drag-handle>
            <icon-more-vertical style="cursor: move;" />
          </template>

          <template #path="{ record }">
            <a-tag color="arcoblue" style="font-family: monospace;">
              {{ record.path || '-' }}
            </a-tag>
          </template>

          <template #menuName="{ record }">
            <a-space direction="horizontal" :size="8">
              <span style="font-weight: 500;">{{ record.menuName }}</span>
              <a-tag v-if="record.menuType === 'MENU'" color="green" size="small">菜单</a-tag>
              <a-tag v-else-if="record.menuType === 'BUTTON'" color="orange" size="small">按钮</a-tag>
              <a-tag v-else-if="record.menuType === 'PAGE'" color="purple" size="small">页面</a-tag>
            </a-space>
          </template>

          <template #icon="{ record }">
            <span v-if="record.icon" class="menu-icon">{{ record.icon }}</span>
            <span v-else style="color: #c9cdd4;">-</span>
          </template>

          <template #visible="{ record }">
            <a-tag :color="record.visible !== false ? 'green' : 'gray'" size="small">
              {{ record.visible !== false ? '显示' : '隐藏' }}
            </a-tag>
          </template>

          <template #sortOrder="{ record }">
            <a-input-number
              v-model="record.sortOrder"
              :min="0"
              :max="9999"
              size="small"
              style="width: 80px;"
              @change="markOrderChanged"
            />
          </template>

          <template #actions="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="openForm(record)">
                <template #icon><icon-edit /></template>
                编辑
              </a-button>
              <a-popconfirm
                content="确定要删除此路由配置吗？"
                @ok="handleDelete(record)"
              >
                <a-button type="text" size="small" status="danger">
                  <template #icon><icon-delete /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table>
      </a-card>
    </a-spin>

    <a-modal
      v-model:visible="formVisible"
      :title="editingMenu ? '编辑路由' : '添加路由'"
      @before-ok="handleSubmit"
      @cancel="formVisible = false"
      :width="560"
    >
      <a-form :model="form" layout="vertical" ref="formRef">
        <a-form-item label="路由路径" field="path" required>
          <a-input
            v-model="form.path"
            placeholder="例如：/ops/dashboard"
            :max-length="100"
          />
        </a-form-item>

        <a-form-item label="页面名称" field="menuName" required>
          <a-input
            v-model="form.menuName"
            placeholder="例如：运营工作台"
            :max-length="50"
          />
        </a-form-item>

        <a-form-item label="组件名称" field="componentName">
          <a-input
            v-model="form.componentName"
            placeholder="例如：Dashboard"
            :max-length="100"
          />
        </a-form-item>

        <a-form-item label="路由类型" field="menuType">
          <a-radio-group v-model="form.menuType">
            <a-radio value="MENU">菜单</a-radio>
            <a-radio value="PAGE">页面</a-radio>
            <a-radio value="BUTTON">按钮</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="所属应用" field="appCode">
          <a-select v-model="form.appCode" placeholder="选择所属应用">
            <a-option value="OPS">运营端</a-option>
            <a-option value="PORTAL">门户端</a-option>
            <a-option value="">通用</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="上级菜单" field="parentId">
          <a-tree-select
            v-model="form.parentId"
            :data="menuTreeForSelect"
            placeholder="选择上级菜单（可选）"
            allow-clear
            :field-names="{ key: 'id', title: 'menuName', children: 'children' }"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="菜单图标" field="icon">
          <a-input
            v-model="form.icon"
            placeholder="输入图标名称或emoji"
            :max-length="50"
          />
        </a-form-item>

        <a-form-item label="排序序号" field="sortOrder">
          <a-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="9999"
            :step="1"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item field="visible" no-label>
          <a-checkbox v-model="form.visible">显示在菜单中</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconPlus,
  IconEdit,
  IconDelete,
  IconRefresh,
  IconSave,
  IconMoreVertical,
} from "@arco-design/web-vue/es/icon";
import {
  getResourceMenus,
  saveResourceMenu,
  deleteResourceMenu,
} from "../../services/api";

const loading = ref(false);
const saving = ref(false);
const menus = ref([]);
const formVisible = ref(false);
const editingMenu = ref(null);
const hasOrderChanged = ref(false);
const selectedKeys = ref([]);

const form = reactive({
  path: "",
  menuName: "",
  componentName: "",
  menuType: "MENU",
  appCode: "OPS",
  parentId: undefined,
  icon: "",
  sortOrder: 0,
  visible: true,
});

const columns = [
  { title: "", dataIndex: "drag", width: 40, align: "center" },
  { title: "路由路径", dataIndex: "path", width: 200 },
  { title: "页面名称", dataIndex: "menuName", width: 180 },
  { title: "组件", dataIndex: "componentName", width: 140 },
  { title: "类型", dataIndex: "menuType", width: 100, align: "center" },
  { title: "图标", dataIndex: "icon", width: 80, align: "center" },
  { title: "显示", dataIndex: "visible", width: 80, align: "center" },
  { title: "排序", dataIndex: "sortOrder", width: 100 },
  { title: "操作", dataIndex: "actions", width: 160, fixed: "right" },
];

const flatMenus = computed(() => {
  const result = [];
  function flatten(nodes, level = 0) {
    if (!nodes) return;
    for (const node of nodes) {
      result.push({ ...node, _level: level });
      if (node.children && node.children.length > 0) {
        flatten(node.children, level + 1);
      }
    }
  }
  flatten(menus.value);
  return result.sort((a, b) => a.sortOrder - b.sortOrder);
});

const menuTreeForSelect = computed(() => {
  function transform(nodes) {
    return nodes.map((node) => ({
      id: node.id,
      menuName: node.menuName,
      children: node.children ? transform(node.children) : [],
    }));
  }
  return transform(menus.value);
});

function buildTree(menus) {
  const map = {};
  const roots = [];
  menus.forEach((m) => {
    map[m.id] = { ...m, children: [] };
  });
  menus.forEach((m) => {
    if (m.parentId && map[m.parentId]) {
      map[m.parentId].children.push(map[m.id]);
    } else {
      roots.push(map[m.id]);
    }
  });
  function sortNodes(nodes) {
    nodes.sort((a, b) => a.sortOrder - b.sortOrder);
    nodes.forEach((n) => {
      if (n.children.length > 0) sortNodes(n.children);
    });
  }
  sortNodes(roots);
  return roots;
}

async function loadMenus() {
  loading.value = true;
  try {
    const res = await getResourceMenus();
    menus.value = buildTree(res.menus || []);
    hasOrderChanged.value = false;
  } catch (e) {
    Message.error("加载路由配置失败：" + e.message);
  } finally {
    loading.value = false;
  }
}

function openForm(menu = null) {
  editingMenu.value = menu;
  if (menu) {
    Object.assign(form, {
      path: menu.path || "",
      menuName: menu.menuName || "",
      componentName: menu.componentName || "",
      menuType: menu.menuType || "MENU",
      appCode: menu.appCode || "OPS",
      parentId: menu.parentId || undefined,
      icon: menu.icon || "",
      sortOrder: menu.sortOrder || 0,
      visible: menu.visible !== false,
    });
  } else {
    Object.assign(form, {
      path: "",
      menuName: "",
      componentName: "",
      menuType: "MENU",
      appCode: "OPS",
      parentId: undefined,
      icon: "",
      sortOrder: flatMenus.value.length,
      visible: true,
    });
  }
  formVisible.value = true;
}

async function handleSubmit(done) {
  if (!form.path) {
    Message.error("请输入路由路径");
    done(false);
    return;
  }
  if (!form.menuName) {
    Message.error("请输入页面名称");
    done(false);
    return;
  }

  saving.value = true;
  try {
    await saveResourceMenu({
      menuCode: editingMenu.value?.id ? `MENU_${editingMenu.value.id}` : `MENU_${Date.now()}`,
      menuName: form.menuName,
      menuType: form.menuType,
      appCode: form.appCode,
      parentId: form.parentId || 0,
      path: form.path,
      componentName: form.componentName,
      icon: form.icon,
      sortOrder: form.sortOrder,
      visible: form.visible,
    });
    Message.success(editingMenu.value ? "路由已更新" : "路由已添加");
    formVisible.value = false;
    await loadMenus();
    done(true);
  } catch (e) {
    Message.error("保存失败：" + e.message);
    done(false);
  } finally {
    saving.value = false;
  }
}

async function handleDelete(menu) {
  try {
    await deleteResourceMenu(menu.id);
    Message.success("路由已删除");
    await loadMenus();
  } catch (e) {
    Message.error("删除失败：" + e.message);
  }
}

function markOrderChanged() {
  hasOrderChanged.value = true;
}

function onDragEnd(newIndex, oldIndex) {
  markOrderChanged();
}

async function saveAllOrders() {
  saving.value = true;
  try {
    for (const menu of flatMenus.value) {
      await saveResourceMenu({
        id: menu.id,
        menuCode: `MENU_${menu.id}`,
        menuName: menu.menuName,
        menuType: menu.menuType,
        appCode: menu.appCode,
        parentId: menu.parentId || 0,
        path: menu.path,
        componentName: menu.componentName,
        icon: menu.icon,
        sortOrder: menu.sortOrder,
        visible: menu.visible,
      });
    }
    Message.success("排序已保存");
    hasOrderChanged.value = false;
  } catch (e) {
    Message.error("保存失败：" + e.message);
  } finally {
    saving.value = false;
  }
}

function handleSelectionChange(keys) {
  selectedKeys.value = keys;
}

onMounted(loadMenus);
</script>

<style lang="scss" scoped>
.route-manage-page {
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

.route-card {
  :deep(.arco-table-drag-handle) {
    cursor: move;
  }

  :deep(.arco-table-tr) {
    transition: background 0.2s;

    &:hover {
      background: #f7f8fa;
    }
  }
}

.menu-icon {
  font-size: 16px;
}
</style>
