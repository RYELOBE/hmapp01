<template>
  <div class="org-tree">
    <div class="tree-header">
      <h3 class="tree-title">组织架构</h3>
    </div>
    <a-tree
      ref="treeRef"
      :data="orgTreeData"
      :field-names="fieldNames"
      :default-expand-all="true"
      :checkable="false"
      :show-line="true"
      @select="handleSelect"
    >
      <template #title="{ record }">
        <span class="tree-node">
          <icon-user-group v-if="record.type === 'dept'" class="node-icon" />
          <icon-user v-else class="node-icon" />
          {{ record.title }}
        </span>
      </template>
    </a-tree>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { IconUserGroup, IconUser } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  orgData: {
    type: Array,
    default: () => []
  },
  fieldNames: {
    type: Object,
    default: () => ({ key: 'id', title: 'title', children: 'children' })
  }
});

const emit = defineEmits(['select']);

const treeRef = ref(null);

const mockOrgTree = [
  {
    id: 'org-1',
    title: '校园二手交易平台',
    type: 'dept',
    children: [
      {
        id: 'dept-1',
        title: '运营部',
        type: 'dept',
        children: [
          { id: 'user-1', title: '张三', type: 'user' },
          { id: 'user-2', title: '李四', type: 'user' }
        ]
      },
      {
        id: 'dept-2',
        title: '技术部',
        type: 'dept',
        children: [
          { id: 'user-3', title: '王五', type: 'user' },
          { id: 'user-4', title: '赵六', type: 'user' }
        ]
      },
      {
        id: 'dept-3',
        title: '市场部',
        type: 'dept',
        children: [
          { id: 'user-5', title: '钱七', type: 'user' }
        ]
      }
    ]
  }
];

const orgTreeData = computed(() => {
  return props.orgData.length > 0 ? props.orgData : mockOrgTree;
});

function handleSelect(selectedKeys, info) {
  emit('select', selectedKeys, info);
}
</script>

<style lang="scss" scoped>
.org-tree {
  .tree-header {
    padding: 12px 16px;
    border-bottom: 1px solid #e5e6eb;
  }

  .tree-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .node-icon {
    color: #86909c;
  }
}
</style>
