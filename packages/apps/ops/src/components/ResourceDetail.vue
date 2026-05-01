<template>
  <div class="resource-detail">
    <a-descriptions :column="2" :data="resource" class="detail-info">
      <a-descriptions-item label="菜单代码" prop="menuCode" />
      <a-descriptions-item label="自定义资源代码" prop="resourceCode" />
      <a-descriptions-item label="菜单名称" prop="menuName" />
      <a-descriptions-item label="上级菜单" prop="parentMenuName" />
      <a-descriptions-item label="所属应用" prop="appName" />
      <a-descriptions-item label="显示序号" prop="sortOrder" />
      <a-descriptions-item label="状态">
        <a-tag :color="resource.status === 'ACTIVE' ? 'green' : 'red'">
          {{ resource.status === 'ACTIVE' ? '启用' : '禁用' }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="是否展示">
        <a-tag :color="resource.visible ? 'blue' : 'gray'">
          {{ resource.visible ? '是' : '否' }}
        </a-tag>
      </a-descriptions-item>
    </a-descriptions>

    <a-tabs v-model:active-key="activeTab" class="detail-tabs">
      <a-tab-pane key="functions" title="功能按钮">
        <div class="tab-content">
          <div class="tab-header">
            <a-button type="primary" size="small" @click="showAddFunctionModal = true">
              <template #icon><icon-plus /></template>
              新建外部功能
            </a-button>
          </div>
          <a-table
            :columns="functionColumns"
            :data="functions"
            :row-key="'id'"
            :pagination="false"
          >
            <template #status="{ record }">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
                {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
              </a-tag>
            </template>
            <template #actions="{ record }">
              <a-space :size="8">
                <a-button size="mini" @click="editFunction(record)">编辑</a-button>
                <a-button size="mini" status="danger" @click="deleteFunction(record)">删除</a-button>
              </a-space>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
      <a-tab-pane key="meta" title="资源元模型">
        <div class="tab-content">
          <a-card title="元数据信息" :bordered="false">
            <a-descriptions :column="2">
              <a-descriptions-item label="资源类型" prop="resourceType" />
              <a-descriptions-item label="URL路径" prop="urlPath" />
              <a-descriptions-item label="组件路径" prop="componentPath" />
              <a-descriptions-item label="图标" prop="icon" />
            </a-descriptions>
          </a-card>
        </div>
      </a-tab-pane>
      <a-tab-pane key="roles" title="可操作角色">
        <div class="tab-content">
          <a-table
            :columns="roleColumns"
            :data="availableRoles"
            :row-key="'roleCode'"
            :pagination="false"
          >
            <template #status="{ record }">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
                {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
              </a-tag>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:visible="showAddFunctionModal"
      title="新建外部功能"
      @ok="handleAddFunction"
      @cancel="showAddFunctionModal = false"
    >
      <a-form :model="functionForm" label-align="left" label-width="120px">
        <a-form-item label="功能名称" required>
          <a-input v-model="functionForm.functionName" placeholder="请输入功能名称" />
        </a-form-item>
        <a-form-item label="功能编码" required>
          <a-input v-model="functionForm.functionCode" placeholder="请输入功能编码" />
        </a-form-item>
        <a-form-item label="排序号">
          <a-input-number v-model="functionForm.sortOrder" :min="0" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model="functionForm.status" :checked-value="'ACTIVE'" :unchecked-value="'INACTIVE'" />
        </a-form-item>
        <a-form-item label="功能描述">
          <a-textarea v-model="functionForm.description" placeholder="请输入功能描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  resource: {
    type: Object,
    default: () => ({})
  },
  functions: {
    type: Array,
    default: () => []
  },
  availableRoles: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['add-function', 'edit-function', 'delete-function']);

const activeTab = ref('functions');
const showAddFunctionModal = ref(false);
const functionForm = reactive({
  functionName: '',
  functionCode: '',
  sortOrder: 0,
  status: 'ACTIVE',
  description: ''
});

const functionColumns = [
  { title: '功能名称', dataIndex: 'functionName', width: 150 },
  { title: '功能编码', dataIndex: 'functionCode', width: 150 },
  { title: '排序号', dataIndex: 'sortOrder', width: 100, align: 'center' },
  { title: '状态', slot: 'status', width: 100 },
  { title: '描述', dataIndex: 'description', width: 200 },
  { title: '操作', slot: 'actions', width: 120 }
];

const roleColumns = [
  { title: '角色名称', dataIndex: 'roleName', width: 150 },
  { title: '角色编码', dataIndex: 'roleCode', width: 150 },
  { title: '状态', slot: 'status', width: 100 }
];

watch(() => props.resource, () => {
  activeTab.value = 'functions';
});

function editFunction(record) {
  emit('edit-function', record);
}

function deleteFunction(record) {
  emit('delete-function', record);
}

function handleAddFunction() {
  emit('add-function', { ...functionForm });
  showAddFunctionModal.value = false;
  Object.keys(functionForm).forEach(key => {
    functionForm[key] = key === 'sortOrder' ? 0 : (key === 'status' ? 'ACTIVE' : '');
  });
}
</script>

<style lang="scss" scoped>
.resource-detail {
  .detail-info {
    margin-bottom: 16px;
  }

  .detail-tabs {
    :deep(.arco-tabs-content) {
      padding: 16px;
    }
  }

  .tab-content {
    background: #fafafa;
    border-radius: 8px;
    padding: 16px;
  }

  .tab-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 16px;
  }
}
</style>