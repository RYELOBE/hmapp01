<template>
  <div class="role-list">
    <a-table
      :columns="columns"
      :data="roles"
      :loading="loading"
      :row-key="'roleCode'"
      :pagination="false"
      class="role-table"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
          {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space :size="8">
          <a-button size="small" @click="$emit('edit', record)">
            <template #icon><icon-edit /></template>
            编辑
          </a-button>
          <a-button
            size="small"
            :status="record.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="$emit('toggle-status', record)"
          >
            {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
          </a-button>
          <a-button
            size="small"
            status="danger"
            @click="$emit('delete', record)"
          >
            <template #icon><icon-delete /></template>
            删除
          </a-button>
          <a-button size="small" @click="$emit('permissions', record)">
            <template #icon><icon-settings /></template>
            权限配置
          </a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { IconEdit, IconDelete, IconSettings } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  roles: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});

defineEmits(['edit', 'delete', 'toggle-status', 'permissions']);

const columns = [
  {
    title: '角色名称',
    dataIndex: 'roleName',
    width: 150
  },
  {
    title: '角色编码',
    dataIndex: 'roleCode',
    width: 150,
    className: 'text-mono'
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    slot: 'status'
  },
  {
    title: '权限数量',
    dataIndex: 'resourceCount',
    width: 100,
    align: 'center'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '操作',
    slot: 'actions',
    width: 280,
    fixed: 'right'
  }
];
</script>

<style lang="scss" scoped>
.role-list {
  .role-table {
    :deep(.arco-table-th) {
      background: #fafafa;
      font-weight: 600;
    }
    
    .text-mono {
      font-family: monospace;
      color: #646a73;
    }
  }
}
</style>