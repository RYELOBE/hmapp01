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
      class="tree-container"
      @check="handleCheck"
      @select="handleSelect"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';

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

defineExpose({
  getAllKeys,
  treeRef
});
</script>

<style lang="scss" scoped>
.resource-tree {
  .tree-container {
    max-height: 500px;
    overflow-y: auto;
  }
}
</style>