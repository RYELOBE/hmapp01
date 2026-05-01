<template>
  <div class="portal-group-tree">
    <div class="tree-header">
      <h4 class="tree-title">门户分组</h4>
    </div>
    <a-tree
      :data="groupTree"
      :default-expand-all="true"
      :field-names="{ title: 'name', key: 'code', children: 'children' }"
      :selected-keys="selectedGroup"
      @select="handleSelect"
    >
      <template #title="{ node }">
        <a-space>
          <component :is="node.icon || IconFolder" class="tree-icon" />
          <span>{{ node.name }}</span>
          <a-badge v-if="node.count" :count="node.count" :dot="false" />
        </a-space>
      </template>
    </a-tree>
  </div>
</template>

<script setup>import { ref, computed } from 'vue';
import { IconFolder, IconFolderOpen } from '@arco-design/web-vue/es/icon';
const props = defineProps({
 modelValue: { type: String, default: '' },
 groups: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'select']);
const selectedGroup = ref([props.modelValue]);
const groupTree = computed(() => {
 return props.groups.map(g => ({
 code: g.code,
 name: g.name,
 count: g.count || 0,
 children: (g.children || []).map(c => ({
 code: c.code,
 name: c.name,
 count: c.count || 0,
 })),
 }));
});
function handleSelect(selectedKeys) {
 const key = selectedKeys[0] || '';
 selectedGroup.value = selectedKeys;
 emit('update:modelValue', key);
 emit('select', key);
}
</script>

<style lang="scss" scoped>
.portal-group-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.tree-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e6eb;
}

.tree-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

.tree-icon {
  font-size: 14px;
  color: #86909c;
}
</style>