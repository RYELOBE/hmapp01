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

    <div class="page-body">
      <!-- 左侧：门户分组树 -->
      <div class="sidebar">
        <PortalGroupTree
          v-model="selectedGroup"
          :groups="portalGroups"
          @select="handleGroupSelect"
        />
      </div>

      <!-- 中间：门户列表 -->
      <div class="main-content">
        <!-- 搜索和筛选栏 -->
        <div class="filter-bar">
          <a-space>
            <a-input
              v-model="searchKeyword"
              placeholder="搜索门户名称或编码"
              allow-clear
              @keyup.enter="handleSearch"
            >
              <template #prefix-icon><icon-search /></template>
            </a-input>
            <a-select
              v-model="templateType"
              placeholder="模板类型"
              allow-clear
            >
              <a-option value="">全部</a-option>
              <a-option value="backstage">后台门户</a-option>
              <a-option value="open">开放门户</a-option>
            </a-select>
            <a-button @click="handleSearch">
              <template #icon><icon-search /></template>
              搜索
            </a-button>
            <a-button @click="resetFilter">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
          </a-space>
        </div>

        <a-spin :loading="loading">
          <PortalList
            :portals="filteredPortals"
            :loading="loading"
            :pagination="pagination"
            @design="openDesigner"
            @preview="openPreview"
            @edit="openEditModal"
            @delete="handleDelete"
            @page-change="handlePageChange"
          />
        </a-spin>
      </div>

      <!-- 右侧：操作按钮面板 -->
      <div class="action-panel">
        <div class="action-panel-header">
          <h4>快捷操作</h4>
        </div>
        <div class="action-list">
          <a-button type="primary" long @click="openAddModal">
            <template #icon><icon-plus /></template>
            新建门户
          </a-button>
          <a-button long @click="exportConfig">
            <template #icon><icon-download /></template>
            导出配置
          </a-button>
          <a-button long @click="importConfig">
            <template #icon><icon-upload /></template>
            导入配置
          </a-button>
          <a-button long @click="syncPortal">
            <template #icon><icon-refresh-cw /></template>
            同步门户
          </a-button>
        </div>
        <div class="info-card">
          <div class="info-title">统计信息</div>
          <div class="info-item">
            <span class="info-value">{{ portals.length }}</span>
            <span class="info-label">门户总数</span>
          </div>
          <div class="info-item">
            <span class="info-value">{{ backstageCount }}</span>
            <span class="info-label">后台门户</span>
          </div>
          <div class="info-item">
            <span class="info-value">{{ openCount }}</span>
            <span class="info-label">开放门户</span>
          </div>
        </div>
      </div>
    </div>

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
        <a-form-item label="所属分组" field="groupCode">
          <a-select v-model="form.groupCode" placeholder="选择分组（可选）" allow-clear>
            <a-option v-for="group in portalGroups" :key="group.code" :value="group.code">
              {{ group.name }}
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览 Modal -->
    <a-modal
      v-model:visible="previewVisible"
      title="门户预览"
      :width="800"
      :footer="false"
    >
      <div class="preview-container" v-if="previewPortal">
        <PortalPreview :portal="previewPortal" />
      </div>
    </a-modal>

    <!-- 门户设计器抽屉 -->
    <a-drawer
      v-model:visible="drawerVisible"
      :title="'设计 - ' + currentPortal?.portalName"
      :width="720"
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
import { ref, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconUpload,
  IconRefreshCw,
} from '@arco-design/web-vue/es/icon';
import {
  getPortalConfigs,
  savePortalConfig,
  deletePortalConfig,
  getPortalConfig,
} from '../services/api';
import PortalGroupTree from '../components/PortalGroupTree.vue';
import PortalList from '../components/PortalList.vue';
import PortalDesigner from './PortalDesigner.vue';

const loading = ref(false);
const saving = ref(false);
const portals = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const drawerVisible = ref(false);
const currentPortal = ref(null);
const previewVisible = ref(false);
const previewPortal = ref(null);
const selectedGroup = ref('');
const searchKeyword = ref('');
const templateType = ref('');

const pagination = ref({
  pageSize: 10,
  current: 1,
  total: 0,
});

const portalGroups = ref([
  { code: 'all', name: '全部门户', count: 0 },
  { code: 'system', name: '系统门户', count: 2, children: [
    { code: 'system-ops', name: '运营端', count: 1 },
    { code: 'system-admin', name: '管理端', count: 1 },
  ]},
  { code: 'business', name: '业务门户', count: 3, children: [
    { code: 'business-buyer', name: '买家端', count: 1 },
    { code: 'business-vendor', name: '卖家端', count: 1 },
    { code: 'business-public', name: '公共端', count: 1 },
  ]},
]);

const defaultForm = () => ({
  portalCode: '',
  portalName: '',
  templateType: 'backstage',
  groupCode: '',
  configJson: '',
  updatedBy: 'ops',
});
const form = ref(defaultForm());

const filteredPortals = computed(() => {
  let result = portals.value;
  if (selectedGroup.value && selectedGroup.value !== 'all') {
    result = result.filter(p => p.groupCode === selectedGroup.value || p.groupCode?.startsWith(selectedGroup.value));
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(p =>
      p.portalName.toLowerCase().includes(keyword) ||
      p.portalCode.toLowerCase().includes(keyword)
    );
  }
  if (templateType.value) {
    result = result.filter(p => p.templateType === templateType.value);
  }
  pagination.value.total = result.length;
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  return result.slice(start, start + pagination.value.pageSize);
});

const backstageCount = computed(() => portals.value.filter(p => p.templateType === 'backstage').length);
const openCount = computed(() => portals.value.filter(p => p.templateType === 'open').length);

async function loadPortals() {
  loading.value = true;
  try {
    const res = await getPortalConfigs();
    portals.value = (res.portals || []).map(p => ({
      ...p,
      themeColor: p.configJson ? JSON.parse(p.configJson)?.theme?.background || '#336ad8' : '#336ad8',
    }));
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
    groupCode: portal.groupCode || '',
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
  try {
    const res = await getPortalConfig(portal.portalCode);
    currentPortal.value = res.portal;
    drawerVisible.value = true;
  } catch (e) {
    Message.error('加载配置失败：' + e.message);
  }
}

function openPreview(portal) {
  previewPortal.value = portal;
  previewVisible.value = true;
}

function handleGroupSelect(groupCode) {
  selectedGroup.value = groupCode;
  pagination.value.current = 1;
}

function handleSearch() {
  pagination.value.current = 1;
}

function resetFilter() {
  searchKeyword.value = '';
  templateType.value = '';
  selectedGroup.value = '';
  pagination.value.current = 1;
}

function handlePageChange(page) {
  pagination.value.current = page;
}

function exportConfig() {
  const data = JSON.stringify(portals.value, null, 2);
  const blob = new Blob([data], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'portal-configs.json';
  a.click();
  URL.revokeObjectURL(url);
  Message.success('配置已导出');
}

function importConfig() {
  Message.info('导入功能开发中');
}

function syncPortal() {
  Message.info('同步功能开发中');
}

onMounted(loadPortals);
</script>

<style lang="scss" scoped>
.portal-design-page {
  padding: 0;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;

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

.page-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px 24px;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  height: calc(100vh - 160px);
  overflow-y: auto;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  overflow: hidden;
}

.filter-bar {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e6eb;
  background: #f7f8fa;
}

.action-panel {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-panel-header {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e6eb;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.info-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.info-title {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e5e6eb;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.info-value {
  font-size: 20px;
  font-weight: 700;
  color: #336ad8;
}

.info-label {
  font-size: 13px;
  color: #86909c;
}

.preview-container {
  padding: 16px;
}
</style>