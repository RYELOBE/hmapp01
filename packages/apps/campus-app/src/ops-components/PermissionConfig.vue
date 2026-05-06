<template>
  <div class="permission-config">
    <div class="config-header">
      <h3 class="config-title">{{ roleName }} — 资源授权</h3>
      <a-space :size="12">
        <a-button size="small" @click="toggleAll(true)">
          <template #icon><icon-check-circle /></template>
          全选
        </a-button>
        <a-button size="small" @click="toggleAll(false)">
          <template #icon><icon-circle /></template>
          全不选
        </a-button>
        <a-divider direction="vertical" :margin="4" />
        <a-button size="small" type="primary" :loading="saving" @click="savePermissions">
          <template #icon><icon-save /></template>
          保存权限
        </a-button>
      </a-space>
    </div>

    <div class="config-body">
      <a-spin :loading="treeLoading" style="width: 100%; height: 100%;">
        <a-tree
          ref="treeRef"
          :data="treeDataWithFunctions"
          :field-names="{ key: 'id', title: 'title', children: 'children', isLeaf: 'isLeaf' }"
          :checkable="true"
          :checked-keys="checkedKeys"
          :default-expand-all="false"
          :check-strictly="false"
          :load-more="loadMoreFunctions"
          show-line
          class="permission-tree"
          @check="handleCheck"
          @expand="handleExpand"
        >
          <template #icon="{ node }">
            <component
              :is="node.isFunction ? 'icon-apps' : (node.expanded ? 'icon-folder-open' : 'icon-folder')"
              :style="{ color: node.isFunction ? '#165DFF' : '#FF7D00', fontSize: '14px' }"
            />
          </template>
          <template #title="{ node }">
            <span :class="{ 'function-node': node.isFunction, 'menu-node': !node.isFunction }">
              {{ node.title }}
            </span>
            <a-tag v-if="node.isFunction" size="small" color="arcoblue" style="margin-left: 6px;">功能</a-tag>
          </template>
        </a-tree>

        <div v-if="!treeDataWithFunctions.length && !treeLoading" class="empty-tree">
          <icon-folder-open :size="48" style="color: #c0c4cc;" />
          <p>暂无资源菜单数据</p>
        </div>
      </a-spin>
    </div>

    <div class="config-footer">
      <a-row justify="space-between" align="center">
        <a-col>
          <a-space :size="16">
            <a-statistic title="已选菜单" :value="menuCheckedCount" :value-style="{ fontSize: '14px' }" />
            <a-statistic title="已选功能" :value="functionCheckedCount" :value-style="{ fontSize: '14px' }" />
          </a-space>
        </a-col>
        <a-col>
          <a-tag color="blue" size="large">共选择 {{ checkedKeys.length }} 项</a-tag>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  IconCheckCircle,
  IconCircle,
  IconSave,
  IconFolder,
  IconFolder,
  IconApps
} from '@arco-design/web-vue/es/icon';
import { getResourceFunctions } from '../services/api';
import { saveRoleResources } from '../services/api';

const props = defineProps({
  roleCode: {
    type: String,
    required: true
  },
  roleName: {
    type: String,
    default: ''
  },
  menuTree: {
    type: Array,
    default: () => []
  },
  initialCheckedKeys: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['save-success']);

const saving = ref(false);
const treeLoading = ref(false);
const checkedKeys = ref([]);
const treeRef = ref(null);
const loadedFunctionMap = ref({});
const expandedKeysSet = ref(new Set());

watch(() => props.initialCheckedKeys, (newVal) => {
  checkedKeys.value = [...newVal];
}, { immediate: true });

watch(() => props.menuTree, () => {
  resetTree();
}, { deep: false });

const treeDataWithFunctions = computed(() => {
  return transformMenuTree(props.menuTree);
});

const menuCheckedCount = computed(() => {
  return checkedKeys.value.filter(key => !key.toString().startsWith('func_')).length;
});

const functionCheckedCount = computed(() => {
  return checkedKeys.value.filter(key => key.toString().startsWith('func_')).length;
});

function transformMenuTree(nodes) {
  if (!nodes || !nodes.length) return [];
  
  return nodes.map(node => {
    const transformedNode = {
      id: node.id,
      title: node.menuName || node.title || '未命名',
      isFunction: false,
      isLeaf: false,
      originalData: node,
      children: undefined
    };
    
    if (node.children && node.children.length > 0) {
      transformedNode.children = transformMenuTree(node.children);
    }
    
    if (loadedFunctionMap.value[node.id]) {
      const functions = loadedFunctionMap.value[node.id];
      const functionNodes = functions.map(func => ({
        id: `func_${func.id}`,
        title: func.functionName || func.functionCode || '未命名功能',
        isFunction: true,
        isLeaf: true,
        originalData: func,
        realId: func.id
      }));
      
      if (transformedNode.children && transformedNode.children.length > 0) {
        transformedNode.children = [...transformedNode.children, ...functionNodes];
      } else {
        transformedNode.children = functionNodes;
      }
    }
    
    return transformedNode;
  });
}

async function loadMoreFunctions(nodeData) {
  if (nodeData.isFunction || loadedFunctionMap.value[nodeData.id]) return [];
  
  try {
    const res = await getResourceFunctions(nodeData.id);
    const functions = res.functions || [];
    loadedFunctionMap.value[nodeData.id] = functions;
    return functions.map(func => ({
      id: `func_${func.id}`,
      title: func.functionName || func.functionCode || '未命名功能',
      isFunction: true,
      isLeaf: true,
      originalData: func,
      realId: func.id
    }));
  } catch (e) {
    console.error('加载功能失败:', e);
    return [];
  }
}

async function handleExpand(keys, info) {
  const node = info.node;
  if (!node || node.isFunction) return;
  
  const nodeId = node.id;
  if (!expandedKeysSet.value.has(nodeId)) {
    expandedKeysSet.value.add(nodeId);
    
    if (!loadedFunctionMap.value[nodeId]) {
      await loadFunctionsForNode(nodeId);
    }
  }
}

async function loadFunctionsForNode(menuId) {
  try {
    treeLoading.value = true;
    const res = await getResourceFunctions(menuId);
    const functions = res.functions || [];
    
    if (functions.length > 0) {
      loadedFunctionMap.value[menuId] = functions;
      
      const newFuncKeys = functions
        .filter(f => f.status === 'ACTIVE')
        .map(f => `func_${f.id}`);
      
      const initialFuncKeys = props.initialCheckedKeys.filter(k => 
        k.toString().startsWith('func_')
      );
      
      checkedKeys.value = [...new Set([...checkedKeys.value, ...initialFuncKeys])];
    }
  } catch (e) {
    console.error('加载功能按钮失败:', e);
    Message.warning('加载功能按钮失败');
  } finally {
    treeLoading.value = false;
  }
}

function handleCheck(keys, { node, checked }) {
  checkedKeys.value = keys;
}

function toggleAll(checked) {
  if (checked) {
    const allKeys = collectAllKeys(treeDataWithFunctions.value);
    checkedKeys.value = allKeys;
  } else {
    checkedKeys.value = [];
  }
}

function collectAllKeys(nodes) {
  const keys = [];
  function collect(items) {
    items.forEach(item => {
      keys.push(item.id);
      if (item.children && item.children.length > 0) {
        collect(item.children);
      }
    });
  }
  collect(nodes);
  return keys;
}

async function savePermissions() {
  saving.value = true;
  try {
    const resourceIds = checkedKeys.value.map(key => {
      if (key.toString().startsWith('func_')) {
        return parseInt(key.toString().replace('func_', ''));
      }
      return key;
    }).filter(id => id !== null && id !== undefined);

    await saveRoleResources(props.roleCode, resourceIds);
    Message.success('权限保存成功');
    emit('save-success', { roleCode: props.roleCode, count: resourceIds.length });
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

function resetTree() {
  loadedFunctionMap.value = {};
  expandedKeysSet.value = new Set();
  checkedKeys.value = [...props.initialCheckedKeys];
}

onMounted(async () => {
  if (props.menuTree && props.menuTree.length > 0) {
    await preloadFirstLevelFunctions();
  }
});

async function preloadFirstLevelFunctions() {
  const firstLevelMenus = props.menuTree.filter(m => !m.parentId || m.parentId === '0' || m.parentId === null);
  for (const menu of firstLevelMenus.slice(0, 5)) {
    if (!loadedFunctionMap.value[menu.id]) {
      await loadFunctionsForNode(menu.id);
    }
  }
}

defineExpose({
  getCheckedKeys: () => checkedKeys.value,
  resetTree
});
</script>

<style lang="scss" scoped>
.permission-config {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .config-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e6eb;
    background: linear-gradient(135deg, #f8f9fb 0%, #f0f2f5 100%);
    flex-shrink: 0;
  }

  .config-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
  }

  .config-body {
    flex: 1;
    padding: 16px 20px;
    overflow-y: auto;

    .permission-tree {
      :deep(.arco-tree-node) {
        padding: 4px 0;

        &:hover {
          .arco-tree-node-title {
            background-color: #f2f3f5;
          }
        }

        &.arco-tree-node-selected .arco-tree-node-title {
          background-color: #e8f3ff;
        }
      }

      :deep(.arco-tree-node-title) {
        border-radius: 4px;
        padding: 2px 8px;
        transition: all 0.2s;

        &:hover {
          background-color: rgba(22, 93, 255, 0.06);
        }
      }

      .function-node {
        color: #165DFF;
        font-size: 13px;
        font-weight: 500;
      }

      .menu-node {
        color: #1d2129;
        font-size: 13px;
        font-weight: 500;
      }
    }

    .empty-tree {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 200px;
      gap: 12px;
      color: #86909c;
      font-size: 14px;
    }
  }

  .config-footer {
    padding: 12px 20px;
    border-top: 1px solid #e5e6eb;
    background: #fafafa;
    flex-shrink: 0;

    :deep(.arco-statistic-label) {
      font-size: 12px;
      color: #86909c;
    }

    :deep(.arco-statistic-value) {
      font-size: 16px;
      font-weight: 600;
      color: #165DFF;
    }
  }
}
</style>