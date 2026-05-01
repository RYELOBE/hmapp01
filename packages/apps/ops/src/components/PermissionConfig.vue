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
        <a-button size="small" type="primary" :loading="saving" @click="savePermissions">
          <template #icon><icon-save /></template>
          保存权限
        </a-button>
      </a-space>
    </div>
    
    <div class="config-body">
      <ResourceTree
        ref="resourceTreeRef"
        :tree-data="menuTree"
        :checkable="true"
        :checked-keys="checkedKeys"
        :default-expand-all="true"
        :check-strictly="true"
        @check="handleCheck"
      />
    </div>
    
    <div class="config-footer">
      <a-tag color="blue">已选择 {{ checkedKeys.length }} 个资源</a-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconCheckCircle, IconCircle, IconSave } from '@arco-design/web-vue/es/icon';
import ResourceTree from './ResourceTree.vue';
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
const checkedKeys = ref([]);
const resourceTreeRef = ref(null);

watch(() => props.initialCheckedKeys, (newVal) => {
  checkedKeys.value = [...newVal];
}, { immediate: true });

function handleCheck(keys) {
  checkedKeys.value = keys;
}

function toggleAll(checked) {
  if (resourceTreeRef.value) {
    const allKeys = resourceTreeRef.value.getAllKeys();
    checkedKeys.value = checked ? allKeys : [];
  }
}

async function savePermissions() {
  saving.value = true;
  try {
    await saveRoleResources(props.roleCode, checkedKeys.value);
    Message.success('权限保存成功');
    emit('save-success', { roleCode: props.roleCode, count: checkedKeys.value.length });
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}
</script>

<style lang="scss" scoped>
.permission-config {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  overflow: hidden;

  .config-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e6eb;
    background: #fafafa;
  }

  .config-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
  }

  .config-body {
    flex: 1;
    padding: 20px;
    overflow: auto;
  }

  .config-footer {
    padding: 12px 20px;
    border-top: 1px solid #e5e6eb;
    background: #fafafa;
  }
}
</style>