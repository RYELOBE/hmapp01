<template>
  <div class="permission-menu-tree">
    <a-menu
      v-model:selected-key="selectedKey"
      :default-open-keys="['basic', 'permission']"
      @menu-item-click="handleMenuItemClick"
      mode="vertical"
    >
      <a-sub-menu key="basic">
        <template #icon><icon-settings /></template>
        <template #title>基础配置</template>
        <a-sub-menu key="permission">
          <template #icon><icon-safe /></template>
          <template #title>权限管理</template>
          <a-menu-item key="org-user">
            <template #icon><icon-user-group /></template>
            组织用户
          </a-menu-item>
          <a-menu-item key="role-manage">
            <template #icon><icon-user-group-2 /></template>
            角色管理
          </a-menu-item>
          <a-menu-item key="resource-manage">
            <template #icon><icon-folder-open /></template>
            资源管理
          </a-menu-item>
        </a-sub-menu>
      </a-sub-menu>
    </a-menu>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  IconSettings,
  IconSafe,
  IconUserGroup,
  IconUserGroup2,
  IconFolder
} from '@arco-design/web-vue/es/icon';

const emit = defineEmits(['select']);

const selectedKey = ref('role-manage');

function handleMenuItemClick(key) {
  emit('select', key);
}
</script>

<style lang="scss" scoped>
.permission-menu-tree {
  height: 100%;
  border-right: 1px solid #e5e6eb;
  
  :deep(.arco-menu) {
    height: 100%;
    border-right: none;
  }
}
</style>
