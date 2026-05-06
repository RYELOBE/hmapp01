<template>
  <div class="portal-design-page">
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-title">门户设计 / 页面配置</h2>
        <span class="page-subtitle">配置各子应用的门户主题、导航和布局</span>
      </div>
      <a-space>
        <a-button @click="handlePreviewAll">
          <template #icon><icon-eye /></template>
          预览全部
        </a-button>
        <a-button type="primary" @click="openAddModal">
          <template #icon><icon-plus /></template>
          新建路由
        </a-button>
      </a-space>
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

      <!-- 中间：路由配置表格 -->
      <div class="main-content">
        <!-- 搜索和筛选栏 -->
        <div class="filter-bar">
          <a-input
            v-model="searchKeyword"
            placeholder="搜索路由路径或页面名称"
            allow-clear
            style="width: 260px;"
            @keyup.enter="handleSearch"
          >
            <template #prefix-icon><icon-search /></template>
          </a-input>
          <a-select
            v-model="statusFilter"
            placeholder="状态筛选"
            allow-clear
            style="width: 120px;"
          >
            <a-option value="">全部</a-option>
            <a-option value="active">启用</a-option>
            <a-option value="inactive">禁用</a-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">
            <template #icon><icon-search /></template>
            搜索
          </a-button>
          <a-button @click="resetFilter">
            <template #icon><icon-refresh /></template>
            重置
          </a-button>
        </div>

        <a-spin :loading="loading">
          <a-table
            :data="filteredRoutes"
            :columns="routeColumns"
            :pagination="pagination"
            row-key="id"
            :bordered="{ wrapper: true, cell: true }"
            :stripe="true"
            @page-change="handlePageChange"
          >
            <!-- 路由路径 -->
            <template #path="{ record }">
              <code class="route-path">{{ record.path }}</code>
            </template>

            <!-- 页面名称 -->
            <template #name="{ record }">
              <span class="route-name">{{ record.name }}</span>
            </template>

            <!-- 组件名称 -->
            <template #component="{ record }">
              <a-tag color="arcoblue" size="small">{{ record.component }}</a-tag>
            </template>

            <!-- 显示在导航 -->
            <template #showInNav="{ record }">
              <a-switch
                v-model="record.showInNav"
                size="small"
                @change="(val) => handleToggleNav(record, val)"
              />
            </template>

            <!-- 排序序号 -->
            <template #sortOrder="{ record }">
              <a-input-number
                v-model="record.sortOrder"
                :min="0"
                :max="999"
                size="small"
                style="width: 80px;"
                @change="(val) => handleSortChange(record, val)"
              />
            </template>

            <!-- 状态 -->
            <template #status="{ record }">
              <a-tag :color="record.status === 'active' ? 'green' : 'gray'" size="small">
                {{ record.status === 'active' ? '启用' : '禁用' }}
              </a-tag>
            </template>

            <!-- 操作 -->
            <template #actions="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="openEditModal(record)">
                  <template #icon><icon-edit /></template>
                  编辑
                </a-button>
                <a-button type="text" size="small" @click="openPreviewModal(record)">
                  <template #icon><icon-eye /></template>
                  预览
                </a-button>
                <a-popconfirm
                  content="确定删除该路由配置？"
                  @ok="handleDelete(record.id)"
                >
                  <a-button type="text" status="danger" size="small">
                    <template #icon><icon-delete /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-spin>
      </div>

      <!-- 右侧：操作面板 & 预览区域 -->
      <div class="action-panel">
        <div class="panel-section">
          <h4 class="section-title">快捷操作</h4>
          <div class="action-list">
            <a-button type="primary" long @click="openAddModal">
              <template #icon><icon-plus /></template>
              新建路由
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
        </div>

        <div class="panel-section info-card">
          <h4 class="section-title">统计信息</h4>
          <div class="info-item">
            <span class="info-value">{{ routes.length }}</span>
            <span class="info-label">路由总数</span>
          </div>
          <div class="info-item">
            <span class="info-value text-primary">{{ activeCount }}</span>
            <span class="info-label">已启用</span>
          </div>
          <div class="info-item">
            <span class="info-value text-gray">{{ inactiveCount }}</span>
            <span class="info-label">已禁用</span>
          </div>
        </div>

        <!-- 实时预览导航菜单效果 -->
        <div class="panel-section preview-section">
          <h4 class="section-title">导航预览</h4>
          <div class="nav-preview">
            <div
              v-for="route in navPreviewRoutes"
              :key="route.id"
              class="nav-item"
              :class="{ 'nav-item--disabled': route.status !== 'active' }"
            >
              <component :is="getIconComponent(route.icon)" v-if="route.icon" class="nav-icon" />
              <span class="nav-text">{{ route.name }}</span>
            </div>
            <div v-if="navPreviewRoutes.length === 0" class="nav-empty">
              暂无启用的导航项
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑 Modal -->
    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑路由配置' : '新建路由配置'"
      :width="560"
      :ok-loading="saving"
      @ok="handleSave"
      @cancel="resetForm"
    >
      <a-form :model="form" layout="vertical" ref="formRef">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="路由路径" field="path" :rules="[{ required: true, message: '请输入路由路径' }]"
              extra="例如：/portal/home">
              <a-input v-model="form.path" placeholder="/portal/home" :disabled="isEdit" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="页面名称" field="name" :rules="[{ required: true, message: '请输入页面名称' }]">
              <a-input v-model="form.name" placeholder="首页" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="组件名称" field="component" :rules="[{ required: true, message: '请选择组件' }]">
              <a-select v-model="form.component" placeholder="选择组件" allow-clear>
                <a-option v-for="comp in componentOptions" :key="comp.value" :value="comp.value">
                  {{ comp.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图标（可选）" field="icon">
              <a-input v-model="form.icon" placeholder="icon-name" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="显示在导航" field="showInNav">
              <a-switch v-model="form.showInNav" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序序号" field="sortOrder">
              <a-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%;" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态" field="status">
              <a-switch
                v-model="form.status"
                :checked-value="'active'"
                :unchecked-value="'inactive'"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="描述（可选）" field="description">
          <a-textarea v-model="form.description" placeholder="路由描述..." :rows="3" show-word-limit :max-length="200" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览 Modal -->
    <a-modal
      v-model:visible="previewVisible"
      title="路由预览"
      :width="800"
      :footer="false"
    >
      <div class="preview-container" v-if="previewRoute">
        <a-descriptions :column="1" bordered size="medium">
          <a-descriptions-item label="路由路径">
            <code>{{ previewRoute.path }}</code>
          </a-descriptions-item>
          <a-descriptions-item label="页面名称">{{ previewRoute.name }}</a-descriptions-item>
          <a-descriptions-item label="组件名称">{{ previewRoute.component }}</a-descriptions-item>
          <a-descriptions-item label="是否显示在导航">
            <a-tag :color="previewRoute.showInNav ? 'green' : 'gray'" size="small">
              {{ previewRoute.showInNav ? '是' : '否' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="排序序号">{{ previewRoute.sortOrder }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="previewRoute.status === 'active' ? 'green' : 'gray'" size="small">
              {{ previewRoute.status === 'active' ? '启用' : '禁用' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item v-if="previewRoute.description" label="描述">
            {{ previewRoute.description }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
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
  IconEye,
  IconEdit,
  IconDelete,
} from '@arco-design/web-vue/es/icon';
import {
  getPortalRoutes,
  savePortalRoute,
  deletePortalRoute,
  updatePortalRoute,
} from '../services/api';
import PortalGroupTree from '../components/PortalGroupTree.vue';

const loading = ref(false);
const saving = ref(false);
const routes = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const previewVisible = ref(false);
const previewRoute = ref(null);
const selectedGroup = ref('');
const searchKeyword = ref('');
const statusFilter = ref('');

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
    { code: 'business-seller', name: '卖家端', count: 1 },
    { code: 'business-public', name: '公共端', count: 1 },
  ]},
]);

const componentOptions = [
  { label: 'HomeView - 首页', value: 'HomeView' },
  { label: 'ProductList - 商品列表', value: 'ProductList' },
  { label: 'ProductDetail - 商品详情', value: 'ProductDetail' },
  { label: 'Cart - 购物车', value: 'Cart' },
  { label: 'OrderList - 订单列表', value: 'OrderList' },
  { label: 'UserCenter - 用户中心', value: 'UserCenter' },
  { label: 'Login - 登录页', value: 'Login' },
  { label: 'Register - 注册页', value: 'Register' },
];

const routeColumns = [
  { title: '路由路径', dataIndex: 'path', slotName: 'path', width: 180 },
  { title: '页面名称', dataIndex: 'name', slotName: 'name', width: 140 },
  { title: '组件名称', dataIndex: 'component', slotName: 'component', width: 160 },
  { title: '显示在导航', dataIndex: 'showInNav', slotName: 'showInNav', width: 100, align: 'center' },
  { title: '排序序号', dataIndex: 'sortOrder', slotName: 'sortOrder', width: 100, align: 'center' },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 80, align: 'center' },
  { title: '操作', slotName: 'actions', width: 220, fixed: 'right' },
];

const defaultForm = () => ({
  path: '',
  name: '',
  component: '',
  icon: '',
  showInNav: true,
  sortOrder: 0,
  status: 'active',
  description: '',
});
const form = ref(defaultForm());

const filteredRoutes = computed(() => {
  let result = routes.value;
  if (selectedGroup.value && selectedGroup.value !== 'all') {
    result = result.filter(r => r.groupCode === selectedGroup.value || r.groupCode?.startsWith(selectedGroup.value));
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(r =>
      r.path.toLowerCase().includes(keyword) ||
      r.name.toLowerCase().includes(keyword)
    );
  }
  if (statusFilter.value) {
    result = result.filter(r => r.status === statusFilter.value);
  }
  pagination.value.total = result.length;
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  return result.slice(start, start + pagination.value.pageSize);
});

const activeCount = computed(() => routes.value.filter(r => r.status === 'active').length);
const inactiveCount = computed(() => routes.value.filter(r => r.status !== 'active').length);

const navPreviewRoutes = computed(() => {
  return routes.value
    .filter(r => r.showInNav && r.status === 'active')
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .slice(0, 10);
});

async function loadRoutes() {
  loading.value = true;
  try {
    const res = await getPortalRoutes();
    routes.value = res.routes || [];
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

function openEditModal(route) {
  isEdit.value = true;
  form.value = {
    id: route.id,
    path: route.path,
    name: route.name,
    component: route.component,
    icon: route.icon || '',
    showInNav: route.showInNav,
    sortOrder: route.sortOrder,
    status: route.status,
    description: route.description || '',
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
    if (isEdit.value) {
      await updatePortalRoute(form.value.id, { ...form.value });
      Message.success('更新成功');
    } else {
      await savePortalRoute({ ...form.value });
      Message.success('创建成功');
    }
    resetForm();
    await loadRoutes();
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id) {
  try {
    await deletePortalRoute(id);
    Message.success('删除成功');
    await loadRoutes();
  } catch (e) {
    Message.error('删除失败：' + e.message);
  }
}

function openPreviewModal(route) {
  previewRoute.value = route;
  previewVisible.value = true;
}

function handlePreviewAll() {
  Message.info('全屏预览功能开发中');
}

async function handleToggleNav(route, val) {
  try {
    await updatePortalRoute(route.id, { ...route, showInNav: val });
    Message.success(val ? '已添加到导航' : '已从导航移除');
  } catch (e) {
    route.showInNav = !val;
    Message.error('操作失败');
  }
}

async function handleSortChange(route, val) {
  try {
    await updatePortalRoute(route.id, { ...route, sortOrder: val });
  } catch (e) {
    Message.error('保存排序失败');
  }
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
  statusFilter.value = '';
  selectedGroup.value = '';
  pagination.value.current = 1;
}

function handlePageChange(page) {
  pagination.value.current = page;
}

function exportConfig() {
  const data = JSON.stringify(routes.value, null, 2);
  const blob = new Blob([data], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'portal-routes.json';
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

function getIconComponent(iconName) {
  return null;
}

onMounted(loadRoutes);
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
  background: var(--color-bg-white, #fff);
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

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
  color: var(--color-text-1, #1D2129);
}

.page-subtitle {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
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
  background: var(--color-bg-white, #fff);
  border-radius: 8px;
  border: 1px solid var(--color-border-1, #E5E6EB);
  overflow: hidden;
}

.filter-bar {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);
  background: var(--color-fill-1, #F7F8FA);
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.panel-section {
  background: var(--color-bg-white, #fff);
  border-radius: 8px;
  border: 1px solid var(--color-border-1, #E5E6EB);
  padding: 16px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--color-border-1, #E5E6EB);
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }

  .info-value {
    font-size: 22px;
    font-weight: 700;
    color: var(--color-primary-6, #165DFF);
  }

  .info-label {
    font-size: 13px;
    color: var(--color-text-3, #86909C);
  }

  .text-gray {
    color: var(--color-text-4, #C9CDD4);
  }
}

.preview-section {
  max-height: 300px;
}

.nav-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--color-fill-1, #F7F8FA);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text-1, #1D2129);
  transition: all 0.2s;

  &:hover {
    background: var(--color-primary-light-1, #E8F3FF);
  }

  &--disabled {
    opacity: 0.45;
    text-decoration: line-through;
  }

  .nav-icon {
    font-size: 16px;
    color: var(--color-primary-6, #165DFF);
  }

  .nav-text {
    font-weight: 500;
  }
}

.nav-empty {
  text-align: center;
  padding: 20px;
  color: var(--color-text-4, #C9CDD4);
  font-size: 13px;
}

.route-path {
  font-family: monospace;
  font-size: 13px;
  background: var(--color-fill-2, #F5F6F7);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--color-primary-6, #165DFF);
}

.route-name {
  font-weight: 500;
}

.preview-container {
  padding: 16px;
}
</style>
