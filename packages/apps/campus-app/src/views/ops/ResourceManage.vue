<template>
  <div class="resource-manage-page">
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
            <h2 class="page-title">资源管理</h2>
            <span class="page-subtitle">管理系统菜单资源和功能按钮</span>
          </div>
          <div class="page-header__right">
            <a-button type="primary" @click="showAddModal = true">
              <template #icon><icon-plus /></template>
              新建菜单
            </a-button>
          </div>
        </div>

        <a-spin :loading="loading">
          <div class="content-wrapper">
            <!-- 左侧资源树 -->
            <div class="tree-panel">
              <a-card title="资源菜单" :bordered="false" class="tree-card">
                <template #extra>
                  <a-input-search
                    v-model="treeSearchKeyword"
                    placeholder="搜索菜单"
                    size="small"
                    style="width: 140px;"
                    allow-clear
                  />
                </template>
                <ResourceTree
                  ref="resourceTreeRef"
                  :tree-data="filteredMenuTree"
                  :checkable="false"
                  :default-expand-all="!treeSearchKeyword"
                  @select="handleSelectResource"
                />
              </a-card>
            </div>

            <!-- 右侧详情 -->
            <div class="detail-panel">
              <a-card
                v-if="selectedResource"
                :title="selectedResource.menuName"
                :bordered="false"
                class="detail-card"
              >
                <template #extra>
                  <a-space :size="8">
                    <a-button size="small" @click="handleEditResource">编辑</a-button>
                    <a-button size="small" status="danger" @click="handleDeleteResource">删除</a-button>
                  </a-space>
                </template>
                <ResourceDetail
                  :resource="selectedResource"
                  :functions="resourceFunctions"
                  :available-roles="availableRoles"
                  @add-function="handleAddFunction"
                  @edit-function="handleEditFunction"
                  @delete-function="handleDeleteFunction"
                  @toggle-function-status="handleToggleFunctionStatus"
                />
              </a-card>
              <a-card v-else :bordered="false" class="empty-card">
                <div class="empty-content">
                  <icon-folder-open class="empty-icon" />
                  <p>请从左侧选择一个菜单查看详情</p>
                </div>
              </a-card>
            </div>
          </div>
        </a-spin>
      </a-layout-content>
    </a-layout>

    <!-- 新建/编辑菜单弹窗 -->
    <a-modal
      v-model:visible="showAddModal"
      :title="isEditResource ? '编辑菜单' : '新建菜单'"
      @ok="handleSaveResource"
      @cancel="handleCancelResource"
    >
      <a-form :model="resourceForm" label-align="left" label-width="100px">
        <a-form-item label="菜单名称" required>
          <a-input v-model="resourceForm.menuName" placeholder="请输入菜单名称" />
        </a-form-item>
        <a-form-item label="菜单代码" :required="!isEditResource">
          <a-input
            v-model="resourceForm.menuCode"
            :disabled="isEditResource"
            placeholder="请输入菜单代码"
          />
        </a-form-item>
        <a-form-item label="自定义资源代码">
          <a-input v-model="resourceForm.resourceCode" placeholder="请输入资源代码" />
        </a-form-item>
        <a-form-item label="上级菜单">
          <a-select
            v-model="resourceForm.parentId"
            placeholder="请选择上级菜单"
            allow-clear
          >
            <a-option :value="null">无（作为一级菜单）</a-option>
            <template v-for="menu in parentMenuOptions" :key="menu.id">
              <a-option :value="menu.id">{{ menu.menuName }}</a-option>
            </template>
          </a-select>
        </a-form-item>
        <a-form-item label="所属应用">
          <a-select v-model="resourceForm.appCode" placeholder="请选择所属应用">
            <a-option value="ops">运营管理</a-option>
            <a-option value="portal">门户系统</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="显示序号">
          <a-input-number v-model="resourceForm.sortOrder" :min="0" />
        </a-form-item>
        <a-form-item label="URL路径">
          <a-input v-model="resourceForm.urlPath" placeholder="请输入URL路径" />
        </a-form-item>
        <a-form-item label="组件路径">
          <a-input v-model="resourceForm.componentPath" placeholder="请输入组件路径" />
        </a-form-item>
        <a-form-item label="图标">
          <a-input v-model="resourceForm.icon" placeholder="请输入图标名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch
            v-model="resourceForm.status"
            :checked-value="'ACTIVE'"
            :unchecked-value="'INACTIVE'"
          />
        </a-form-item>
        <a-form-item label="是否展示">
          <a-switch v-model="resourceForm.visible" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { IconPlus, IconFolder } from '@arco-design/web-vue/es/icon';
import ResourceTree from '../../ops-components/ResourceTree.vue';
import ResourceDetail from '../../ops-components/ResourceDetail.vue';
import PermissionMenuTree from '../../ops-components/PermissionMenuTree.vue';
import {
  getResourceMenuTree,
  getResourceDetail,
  getResourceFunctions,
  getAllRoles,
  saveResourceMenu,
  updateResourceMenu,
  deleteResourceMenu,
  saveResourceFunction,
  deleteResourceFunction,
  updateResourceFunction
} from '../../services/api';

const router = useRouter();
const loading = ref(false);
const menuTree = ref([]);
const selectedResource = ref(null);
const resourceFunctions = ref([]);
const availableRoles = ref([]);
const showAddModal = ref(false);
const isEditResource = ref(false);
const treeSearchKeyword = ref('');
const resourceTreeRef = ref(null);

const resourceForm = reactive({
  menuName: '',
  menuCode: '',
  resourceCode: '',
  parentId: null,
  appCode: 'ops',
  sortOrder: 0,
  urlPath: '',
  componentPath: '',
  icon: '',
  status: 'ACTIVE',
  visible: true
});

function filterTree(nodes, keyword) {
  if (!keyword) return nodes;
  return nodes
    .map(node => {
      const filteredChildren = node.children ? filterTree(node.children, keyword) : [];
      if (
        (node.menuName && node.menuName.toLowerCase().includes(keyword.toLowerCase())) ||
        (node.menuCode && node.menuCode.toLowerCase().includes(keyword.toLowerCase())) ||
        (node.resourceCode && node.resourceCode.toLowerCase().includes(keyword.toLowerCase())) ||
        filteredChildren.length > 0
      ) {
        return { ...node, children: filteredChildren.length > 0 ? filteredChildren : undefined };
      }
      return null;
    })
    .filter(Boolean);
}

const filteredMenuTree = computed(() => {
  return filterTree(menuTree.value, treeSearchKeyword.value);
});

function handleMenuSelect(key) {
  if (key === 'org-user') {
    router.push('/ops/user-manage');
  } else if (key === 'resource-manage') {
    router.push('/ops/resource-manage');
  } else if (key === 'role-manage') {
    router.push('/ops/role-manage');
  }
}

const parentMenuOptions = computed(() => {
  const options = [];
  function collect(nodes) {
    nodes.forEach(node => {
      if (selectedResource.value && node.id === selectedResource.value.id) return;
      options.push({ id: node.id, menuName: node.menuName });
      if (node.children) collect(node.children);
    });
  }
  collect(menuTree.value);
  return options;
});

async function loadData() {
  loading.value = true;
  try {
    const [treeRes, rolesRes] = await Promise.all([
      getResourceMenuTree(),
      getAllRoles()
    ]);
    menuTree.value = treeRes.tree || [];
    availableRoles.value = rolesRes.roles || [];
  } catch (e) {
    Message.error('加载失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

async function handleSelectResource(selectedKeys) {
  if (!selectedKeys || !selectedKeys.length) {
    selectedResource.value = null;
    resourceFunctions.value = [];
    return;
  }

  const resourceId = selectedKeys[0];
  try {
    const [resourceRes, functionsRes] = await Promise.all([
      getResourceDetail(resourceId),
      getResourceFunctions(resourceId)
    ]);
    selectedResource.value = resourceRes;
    resourceFunctions.value = functionsRes.functions || [];
  } catch (e) {
    Message.error('加载资源详情失败：' + e.message);
  }
}

function handleEditResource() {
  if (!selectedResource.value) return;
  isEditResource.value = true;
  Object.assign(resourceForm, selectedResource.value);
  showAddModal.value = true;
}

function handleDeleteResource() {
  if (!selectedResource.value) return;
  Modal.confirm({
    title: '删除资源',
    content: `确定要删除菜单「${selectedResource.value.menuName}」吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        await deleteResourceMenu(selectedResource.value.id);
        Message.success('删除成功');
        selectedResource.value = null;
        resourceFunctions.value = [];
        loadData();
      } catch (e) {
        Message.error('删除失败：' + e.message);
      }
    }
  });
}

function handleSaveResource() {
  if (!resourceForm.menuName) {
    Message.warning('请输入菜单名称');
    return;
  }
  if (!isEditResource.value && !resourceForm.menuCode) {
    Message.warning('请输入菜单代码');
    return;
  }

  const data = { ...resourceForm };
  const action = isEditResource.value
    ? updateResourceMenu(resourceForm.id, data)
    : saveResourceMenu(data);

  action.then(() => {
    Message.success(isEditResource.value ? '编辑成功' : '创建成功');
    showAddModal.value = false;
    loadData();
    selectedResource.value = null;
  }).catch(e => {
    Message.error('保存失败：' + e.message);
  });
}

function handleCancelResource() {
  showAddModal.value = false;
  Object.keys(resourceForm).forEach(key => {
    resourceForm[key] = key === 'sortOrder' ? 0 : (key === 'status' ? 'ACTIVE' : (key === 'visible' ? true : ''));
  });
  isEditResource.value = false;
}

function handleAddFunction(data) {
  if (!selectedResource.value) return;
  saveResourceFunction({
    ...data,
    menuId: selectedResource.value.id
  }).then(() => {
    Message.success('创建成功');
    return getResourceFunctions(selectedResource.value.id);
  }).then(res => {
    resourceFunctions.value = res.functions || [];
  }).catch(e => {
    Message.error('创建失败：' + e.message);
  });
}

function handleEditFunction(data) {
  if (!selectedResource.value) return;
  updateResourceFunction(data.id, data).then(() => {
    Message.success('编辑成功');
    return getResourceFunctions(selectedResource.value.id);
  }).then(res => {
    resourceFunctions.value = res.functions || [];
  }).catch(e => {
    Message.error('编辑失败：' + e.message);
  });
}

function handleDeleteFunction(record) {
  Modal.confirm({
    title: '删除功能',
    content: `确定要删除功能「${record.functionName}」吗？`,
    onOk: async () => {
      try {
        await deleteResourceFunction(record.id);
        resourceFunctions.value = resourceFunctions.value.filter(f => f.id !== record.id);
        Message.success('删除成功');
      } catch (e) {
        Message.error('删除失败：' + e.message);
      }
    }
  });
}

function handleToggleFunctionStatus(record) {
  const newStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  updateResourceFunction(record.id, { ...record, status: newStatus }).then(() => {
    record.status = newStatus;
    Message.success(`功能已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`);
  }).catch(e => {
    Message.error('操作失败：' + e.message);
  });
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.resource-manage-page {
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

  .content-wrapper {
    display: flex;
    gap: 16px;
    height: calc(100vh - 160px);
  }

  .tree-panel {
    width: 280px;
    flex-shrink: 0;

    .tree-card {
      height: 100%;

      :deep(.arco-card-body) {
        height: calc(100% - 56px);
        overflow: auto;
        padding: 12px;
      }
    }
  }

  .detail-panel {
    flex: 1;
    min-width: 0;

    .detail-card {
      height: 100%;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      :deep(.arco-card-body) {
        flex: 1;
        overflow: auto;
        padding: 16px 20px;
      }
    }

    .empty-card {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .empty-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        color: #86909c;
      }

      .empty-icon {
        font-size: 48px;
        color: #c0c4cc;
      }
    }
  }
}
</style>