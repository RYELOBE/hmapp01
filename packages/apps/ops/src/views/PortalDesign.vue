<template>
  <div class="portal-design-page">
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-title">门户设计</h2>
        <span class="page-subtitle">配置各子应用的门户主题、导航和布局</span>
      </div>
      <a-button type="primary" @click="openAddModal">
        <template #icon><icon-plus /></template>
        新建门户
      </a-button>
    </div>

    <a-spin :loading="loading" tip="加载中...">
      <a-table
        :data="portals"
        :pagination="false"
        row-key="portalCode"
        :bordered="false"
        class="portal-table"
      >
        <template #columns>
          <a-table-column title="门户名称" data-index="portalName" :width="180">
            <template #cell="{ record }">
              <div class="portal-name-cell">
                <div class="portal-name-cell__icon">{{ record.portalName[0] }}</div>
                <span>{{ record.portalName }}</span>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="门户编码" data-index="portalCode" :width="120" />
          <a-table-column title="模板类型" data-index="templateType" :width="120">
            <template #cell="{ record }">
              <a-tag size="small" :color="record.templateType === 'backstage' ? 'blue' : 'green'">
                {{ record.templateType === 'backstage' ? '后台门户' : '开放门户' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="更新时间" data-index="updatedAt" :width="180" />
          <a-table-column title="操作" :width="240" align="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="primary" size="small" @click="openDesigner(record)">
                  <template #icon><icon-palette /></template>
                  设计
                </a-button>
                <a-button size="small" @click="openEditModal(record)">
                  <template #icon><icon-edit /></template>
                </a-button>
                <a-popconfirm content="确认删除？" @ok="handleDelete(record.portalCode)">
                  <a-button size="small" status="danger">
                    <template #icon><icon-delete /></template>
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-spin>

    <!-- 新增/编辑 Modal -->
    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑门户' : '新建门户'"
      :ok-loading="saving"
      @ok="handleSave"
      @cancel="resetForm"
    >
      <a-form :model="form" layout="vertical" ref="formRef">
        <a-form-item label="门户编码" field="portalCode" :rules="[{ required: true, message: '请输入门户编码' }]"
          extra="唯一标识，如 portal、ops">
          <a-input v-model="form.portalCode" placeholder="portal" :disabled="isEdit" allow-clear />
        </a-form-item>
        <a-form-item label="门户名称" field="portalName" :rules="[{ required: true, message: '请输入门户名称' }]">
          <a-input v-model="form.portalName" placeholder="门户（买家/卖家）" allow-clear />
        </a-form-item>
        <a-form-item label="模板类型" field="templateType">
          <a-select v-model="form.templateType">
            <a-option value="backstage">后台门户</a-option>
            <a-option value="open">开放门户</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 门户设计器抽屉 -->
    <a-drawer
      v-model:visible="drawerVisible"
      :title="'设计 - ' + currentPortal?.portalName"
      :width="640"
      :footer="false"
      unmount-on-close
    >
      <PortalDesigner
        v-if="drawerVisible && currentPortal"
        :portal="currentPortal"
        @saved="loadPortals"
      />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  getPortalConfigs, savePortalConfig, deletePortalConfig, getPortalConfig
} from '../services/api';
import PortalDesigner from './PortalDesigner.vue';

const loading = ref(false);
const saving = ref(false);
const portals = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const drawerVisible = ref(false);
const currentPortal = ref(null);

const defaultForm = () => ({
  portalCode: '',
  portalName: '',
  templateType: 'backstage',
  configJson: '',
  updatedBy: 'ops',
});
const form = ref(defaultForm());

async function loadPortals() {
  loading.value = true;
  try {
    const res = await getPortalConfigs();
    portals.value = res.portals || [];
  } catch (e) {
    Message.error('加载失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

function openAddModal() {
  isEdit.value = false;
  form.value = defaultForm();
  modalVisible.value = true;
}

function openEditModal(portal) {
  isEdit.value = true;
  form.value = {
    portalCode: portal.portalCode,
    portalName: portal.portalName,
    templateType: portal.templateType,
    configJson: portal.configJson,
    updatedBy: 'ops',
  };
  modalVisible.value = true;
}

function resetForm() {
  form.value = defaultForm();
  formRef.value?.resetFields();
  modalVisible.value = false;
}

async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid) return;
  saving.value = true;
  try {
    await savePortalConfig({ ...form.value });
    Message.success(isEdit.value ? '已更新' : '已创建');
    resetForm();
    await loadPortals();
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

async function handleDelete(portalCode) {
  try {
    await deletePortalConfig(portalCode);
    Message.success('已删除');
    await loadPortals();
  } catch (e) {
    Message.error('删除失败：' + e.message);
  }
}

async function openDesigner(portal) {
  // 获取完整配置
  try {
    const res = await getPortalConfig(portal.portalCode);
    currentPortal.value = res.portal;
    drawerVisible.value = true;
  } catch (e) {
    Message.error('加载配置失败：' + e.message);
  }
}

onMounted(loadPortals);
</script>

<style lang="scss" scoped>
.portal-design-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;

  &__left {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1d2129;
}

.page-subtitle {
  font-size: 13px;
  color: #86909c;
}

.portal-table {
  border-radius: 8px;
  overflow: hidden;
}

.portal-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #336ad8 0%, #6d9fff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;
  }
}
</style>
