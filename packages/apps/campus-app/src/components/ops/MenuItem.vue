<template>
  <a-sub-menu
    v-if="menuData.children && menuData.children.length > 0 && !menuData.hidden"
    :key="`sub:${menuData.path}`"
  >
    <template #icon v-if="showIcon && menuData.meta?.icon">
      <component :is="menuData.meta.icon" />
    </template>
    <template #title>
      <span>{{ menuData.meta?.title || menuData.name }}</span>
    </template>
    <template #expand-icon-down>
      <icon-down />
    </template>
    <MenuItem
      v-for="item in menuData.children"
      :key="item.path"
      :menu-data="item"
      :show-icon="showIcon"
      @click-menu="handleClickMenu"
    />
  </a-sub-menu>
  <a-menu-item
    v-else-if="!menuData.hidden"
    :key="`item:${menuData.path}`"
    @click="handleClickMenu(menuData)"
  >
    <template #icon v-if="showIcon && menuData.meta?.icon">
      <component :is="menuData.meta.icon" />
    </template>
    <span>{{ menuData.meta?.title || menuData.name }}</span>
  </a-menu-item>
</template>

<script setup>
import { IconDown } from '@arco-design/web-vue/es/icon'

defineProps({
  showIcon: {
    type: Boolean,
    default: false
  },
  menuData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['click-menu'])

function handleClickMenu(menu) {
  emit('click-menu', menu)
}
</script>

<style lang="scss" scoped>
:deep(.arco-menu-item) {
  margin: 2px 8px;
  border-radius: 4px;
  
  &:hover {
    background: #f2f3f5;
  }
  
  &.arco-menu-selected {
    background: #e8f3ff;
    color: #165dff;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: #165dff;
      border-radius: 0 2px 2px 0;
    }
  }
}

:deep(.arco-sub-menu) {
  margin: 2px 8px;
  border-radius: 4px;
  
  .arco-sub-menu-title {
    &:hover {
      background: #f2f3f5;
    }
  }
  
  &.arco-sub-menu-selected {
    .arco-sub-menu-title {
      background: #e8f3ff;
      color: #165dff;
    }
  }
}
</style>
