<template>
  <div class="resource-tree">
    <a-tree
      ref="treeRef"
      :data="treeData"
      :field-names="fieldNames"
      :checkable="checkable"
      :checked-keys="checkedKeys"
      :default-expand-all="defaultExpandAll"
      :check-strictly="checkStrictly"
      :disabled="disabled"
      :selected-keys="selectedKeys"
      class="tree-container"
      @check="handleCheck"
      @select="handleSelect"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  treeData: {
    type: Array,
    default: () => []
  },
  checkable: {
    type: Boolean,
    default: false
  },
  checkedKeys: {
    type: Array,
    default: () => []
  },
  defaultExpandAll: {
    type: Boolean,
    default: true
  },
  checkStrictly: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  selectedKeys: {
    type: Array,
    default: () => []
  },
  fieldNames: {
    type: Object,
    default: () => ({ key: 'id', title: 'menuName', children: 'children' })
  }
});

const emit = defineEmits(['check', 'select']);

const treeRef = ref(null);

function handleCheck(checkedKeys, info) {
  emit('check', checkedKeys, info);
}

function handleSelect(selectedKeys, info) {
  emit('select', selectedKeys, info);
}

function getAllKeys() {
  const keys = [];
  function collect(nodes) {
    nodes.forEach(node => {
      keys.push(node.id);
      if (node.children) collect(node.children);
    });
  }
  collect(props.treeData);
  return keys;
}

function getSelectedNode() {
  if (treeRef.value && props.selectedKeys.length > 0) {
    const selectedKey = props.selectedKeys[0];
    function findNode(nodes) {
      for (const node of nodes) {
        if (node.id === selectedKey) return node;
        if (node.children) {
          const found = findNode(node.children);
          if (found) return found;
        }
      }
      return null;
    }
    return findNode(props.treeData);
  }
  return null;
}

defineExpose({
  getAllKeys,
  getSelectedNode,
  treeRef
});
</script>

<style lang="scss" scoped>
.resource-tree {
  .tree-container {
    max-height: calc(100vh - 280px);
    overflow-y: auto;

    :deep(.arco-tree-node) {
      padding: 4px 0;

      &:hover {
        background-color: #f2f3f5;
      }

      &.arco-tree-node-selected {
        background-color: #e8f3ff;
      }
    }

    :deep(.arco-tree-node-title) {
      font-size: 13px;
      line-height: 28px;
    }

    :deep(.arco-tree-node-icon) {
      margin-right: 4px;
    }
  }
}
</style>