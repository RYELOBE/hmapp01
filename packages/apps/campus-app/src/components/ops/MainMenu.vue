<template>
  <a-menu 
    v-bind="$attrs" 
    :selected-keys="selectedKeys" 
    v-model:open-keys="openKeys"
    class="ops-menu"
  >
    <MenuItem
      v-for="menu in menuListOptions"
      :show-icon="$attrs.showIcon"
      :key="menu.path"
      :menu-data="menu"
      @click-menu="handleClickMenu"
    />
    <template #expand-icon-down>
      <icon-down />
    </template>
  </a-menu>
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { IconDown } from '@arco-design/web-vue/es/icon'
import MenuItem from './MenuItem.vue'

const $route = useRoute()
const router = useRouter()

const menuListOptions = computed(() => {
  return getDevMenuList()
})

const openKeys = ref([])
const selectedKeys = ref([])

// 查找父节点和目标节点
const findParentAndKeys = (list, target, type, prefix) => {
  for (let i in list) {
    const itemPath = list[i].path ? `${prefix ? '/' + prefix : ''}${list[i].path}` : list[i].path
    if (type === 'selected') {
      if (itemPath === target || itemPath + '/' === target) {
        if (list[i].meta?.type === 'button') {
          return []
        } else {
          return [itemPath]
        }
      }
    } else {
      if (list[i].path) {
        if (list[i].meta?.type === 'button') {
          return []
        } else {
          return [itemPath]
        }
      }
    }

    if (list[i].children) {
      let node = findParentAndKeys(list[i].children, target, type, prefix)
      if (node !== undefined) {
        return node.concat(itemPath)
      }
    }
  }
}

// 获取选中的keys
function getTargetKey(type = 'selected', currentRoute = '') {
  let keys = []
  const prefix = ''
  if (currentRoute) {
    const arr = findParentAndKeys(menuListOptions.value, currentRoute, type, prefix) || []
    const set = new Set([...openKeys.value, ...arr])
    openKeys.value = [...set]
    keys = [arr.shift() || '']
  }
  return keys
}

// 开发环境调试使用
function getDevMenuList() {
  const menuFunc = (list, fPath) => {
    list.forEach((item) => {
      if (item.path && item.path.startsWith('/')) {
        item.path = item.path
      } else {
        item.path = fPath ? `${fPath}${fPath.endsWith('/') ? '' : '/'}${item.path}` : item.path
      }
      item.title = item.meta?.title || item.name
      item.children = item.children
      if (item.children && item.children.length > 0) {
        menuFunc(item.children, item.path)
      }
    })
  }
  menuFunc(router.options.routes)
  return router.options.routes.filter(route => route.path?.startsWith('/ops'))
}

function handleClickMenu(menu) {
  router.push(menu.path)
}

watch(
  () => $route.path,
  (newValue, oldValue) => {
    selectedKeys.value = getTargetKey('selected', newValue)
  },
  { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
.ops-menu {
  background: transparent;
  border: none;
  
  :deep(.arco-menu-inner) {
    padding: 8px 0;
    
    &::-webkit-scrollbar-thumb {
      border-color: #001529;
      &:hover {
        border-color: #001529;
      }
    }
    
    .arco-menu-overflow-wrap {
      font-size: 14px;
      
      .arco-menu-overflow-sub-menu {
        > span:first-child {
          display: none;
        }
      }
    }
  }
  
  :deep(.arco-menu-item) {
    color: #4e5969;
    height: 40px;
    line-height: 40px;
    margin: 2px 8px;
    border-radius: 4px;
    
    &:hover {
      background: #f2f3f5;
      color: #1d2129;
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
    color: #4e5969;
    margin: 2px 8px;
    border-radius: 4px;
    
    .arco-sub-menu-title {
      height: 40px;
      line-height: 40px;
      
      &:hover {
        background: #f2f3f5;
        color: #1d2129;
      }
    }
    
    &.arco-sub-menu-selected {
      .arco-sub-menu-title {
        background: #e8f3ff;
        color: #165dff;
      }
    }
    
    .arco-menu-item {
      padding-left: 44px;
    }
  }
}
</style>
