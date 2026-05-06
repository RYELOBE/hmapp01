<template>
  <div class="resource-detail">
    <a-descriptions :column="2" :data="resourceInfo" class="detail-info">
      <a-descriptions-item label="菜单代码">{{ resource.menuCode || '-' }}</a-descriptions-item>
      <a-descriptions-item label="自定义资源代码">{{ resource.resourceCode || '-' }}</a-descriptions-item>
      <a-descriptions-item label="菜单名称">{{ resource.menuName || '-' }}</a-descriptions-item>
      <a-descriptions-item label="上级菜单">{{ resource.parentMenuName || '（无）' }}</a-descriptions-item>
      <a-descriptions-item label="所属应用">{{ appNameMap[resource.appCode] || resource.appCode || '-' }}</a-descriptions-item>
      <a-descriptions-item label="显示序号">{{ resource.sortOrder ?? 0 }}</a-descriptions-item>
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
            <a-button type="primary" size="small" @click="openAddFunctionModal">
              <template #icon><icon-plus /></template>
              新建功能
            </a-button>
          </div>
          <a-table
            :columns="functionColumns"
            :data="functions"
            :row-key="'id'"
            :pagination="{ pageSize: 10, showTotal: true, showPageSize: true }"
            size="small"
          >
            <template #status="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="'ACTIVE'"
                :unchecked-value="'INACTIVE'"
                size="small"
                @change="(val) => handleToggleStatus(record, val)"
              />
            </template>
            <template #actions="{ record }">
              <a-space :size="8">
                <a-button type="text" size="mini" @click="openEditFunctionModal(record)">
                  <template #icon><icon-edit /></template>
                  编辑
                </a-button>
                <a-popconfirm content="确定要删除该功能吗？" @ok="handleDeleteFunction(record)">
                  <a-button type="text" size="mini" status="danger">
                    <template #icon><icon-delete /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
      <a-tab-pane key="meta" title="资源元模型">
        <div class="tab-content">
          <a-card title="元数据信息" :bordered="false" size="small">
            <a-descriptions :column="2" :data="metaInfo" />
          </a-card>
        </div>
      </a-tab-pane>
      <a-tab-pane key="roles" title="可操作角色">
        <div class="tab-content">
          <a-table
            :columns="roleColumns"
            :data="availableRoles"
            :row-key="'roleCode'"
            :pagination="{ pageSize: 5, showTotal: true }"
            size="small"
          >
            <template #status="{ record }">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'" size="small">
                {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
              </a-tag>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>

    <!-- 新建/编辑功能弹窗 -->
    <a-modal
      v-model:visible="showFunctionModal"
      :title="isEditMode ? '编辑功能' : '新建功能'"
      :ok-text="isEditMode ? '保存' : '创建'
"
      @ok="handleSaveFunction"
      @cancel="handleCancelFunction"
    >
      <a-form
        ref="functionFormRef"
        :model="functionForm"
        :rules="functionRules"
        label-align="left"
        label-width="100px"
      >
        <a-form-item field="functionName" label="功能名称" required>
          <a-input v-model="functionForm.functionName" placeholder="请输入功能名称" />
        </a-form-item>
        <a-form-item field="functionCode" label="功能编码" required>
          <a-input
            v-model="functionForm.functionCode"
            :disabled="isEditMode"
            placeholder="请输入功能编码"
          />
        </a-form-item>
        <a-form-item field="sortOrder" label="排序号">
          <a-input-number v-model="functionForm.sortOrder" :min="0" style="width: 100%;" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-switch
            v-model="functionForm.status"
            :checked-value="'ACTIVE'"
            :unchecked-value="'INACTIVE'"
          />
        </a-form-item>
        <a-form-item field="description" label="功能描述">
          <a-textarea v-model="functionForm.description" placeholder="请输入功能描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon';

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

const emit = defineEmits(['add-function', 'edit-function', 'delete-function', 'toggle-function-status']);

const activeTab = ref('functions');
const showFunctionModal = ref(false);
const isEditMode = ref(false);
const functionFormRef = ref(null);

const functionForm = reactive({
  id: null,
  functionName: '',
  functionCode: '',
  sortOrder: 0,
  status: 'ACTIVE',
  description: ''
});

const functionRules = {
  functionName: [{ required: true, message: '请输入功能名称' }],
  functionCode: [{ required: true, message: '请输入功能编码' }]
};

const appNameMap = {
  ops: '运营管理',
  portal: '门户系统'
};

const resourceInfo = computed(() => [
  { label: '菜单代码', value: props.resource.menuCode || '-' },
  { label: '自定义资源代码', value: props.resource.resourceCode || '-' },
  { label: '菜单名称', value: props.resource.menuName || '-' },
  { label: '上级菜单', value: props.resource.parentMenuName || '（无）' },
  { label: '所属应用', value: appNameMap[props.resource.appCode] || props.resource.appCode || '-' },
  { label: '显示序号', value: props.resource.sortOrder ?? 0 }
]);

const metaInfo = computed(() => [
  { label: '资源类型', value: props.resource.resourceType || 'MENU' },
  { label: 'URL路径', value: props.resource.urlPath || '-' },
  { label: '组件路径', value: props.resource.componentPath || '-' },
  { label: '图标', value: props.resource.icon || '-' }
]);

const functionColumns = [
  { title: '功能名称', dataIndex: 'functionName', width: 150, ellipsis: true },
  { title: '功能编码', dataIndex: 'functionCode', width: 150, ellipsis: true },
  { title: '排序号', dataIndex: 'sortOrder', width: 80, align: 'center' },
  { title: '状态', slot: 'status', width: 80, align: 'center' },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '操作', slot: 'actions', width: 140, fixed: 'right' }
];

const roleColumns = [
  { title: '角色名称', dataIndex: 'roleName', width: 150 },
  { title: '角色编码', dataIndex: 'roleCode', width: 150 },
  { title: '状态', slot: 'status', width: 100 }
];

watch(() => props.resource, () => {
  activeTab.value = 'functions';
});

function openAddFunctionModal() {
  isEditMode.value = false;
  resetFunctionForm();
  showFunctionModal.value = true;
}

function openEditFunctionModal(record) {
  isEditMode.value = true;
  Object.assign(functionForm, {
    id: record.id,
    functionName: record.functionName,
    functionCode: record.functionCode,
    sortOrder: record.sortOrder ?? 0,
    status: record.status,
    description: record.description || ''
  });
  showFunctionModal.value = true;
}

function resetFunctionForm() {
  functionForm.id = null;
  functionForm.functionName = '';
  functionForm.functionCode = '';
  functionForm.sortOrder = 0;
  functionForm.status = 'ACTIVE';
  functionForm.description = '';
}

async function handleSaveFunction() {
  try {
    await functionFormRef.value?.validate();
  } catch (e) {
    return;
  }

  const data = { ...functionForm };
  
  if (isEditMode.value) {
    emit('edit-function', data);
  } else {
    delete data.id;
    emit('add-function', data);
  }
  
  showFunctionModal.value = false;
  resetFunctionForm();
}

function handleCancelFunction() {
  showFunctionModal.value = false;
  resetFunctionForm();
}

function handleDeleteFunction(record) {
  emit('delete-function', record);
}

function handleToggleStatus(record, val) {
  emit('toggle-function-status', record);
}
</script>

<style lang="scss" scoped>
.resource-detail {
  .detail-info {
    margin-bottom: 16px;
    background: #fafafa;
    border-radius: 8px;
    padding: 12px 16px;
  }

  .detail-tabs {
    :deep(.arco-tabs-content) {
      padding: 0;
    }

    :deep(.arco-tabs-tab) {
      font-size: 14px;
    }
  }

  .tab-content {
    background: #fff;
    border-radius: 8px;
    padding: 12px 0;
  }

  .tab-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
    padding: 0 4px;
  }

  :deep(.arco-table-th) {
    background-color: #f7f8fa;
    font-weight: 600;
  }

  :deep(.arco-table-td) {
    padding: 8px 12px;
  }
}
</style>