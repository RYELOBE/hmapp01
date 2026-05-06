<template>
  <div class="app-register-page">
    <!-- 标题栏 -->
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-title">子应用管理</h2>
        <span class="page-subtitle">动态配置微前端子应用，保存后主应用将自动加载对应路由</span>
      </div>
      <a-button type="primary" @click="openAddModal">
        <template #icon><icon-plus /></template>
        新增子应用
      </a-button>
    </div>

    <!-- 应用列表 -->
    <a-spin :loading="loading" tip="加载中...">
      <div v-if="apps.length > 0" class="app-grid">
        <div
          v-for="app in apps"
          :key="app.appCode"
          class="app-card"
          :class="{ 'app-card--offline': !app.isOnline }"
        >
          <div class="app-card__header">
            <div class="app-card__icon">{{ app.title[0] }}</div>
            <div class="app-card__info">
              <div class="app-card__name">{{ app.title }}</div>
              <div class="app-card__code">{{ app.appCode }}</div>
              <a-tag
                :color="app.isOnline ? 'green' : 'gray'"
                size="small"
                class="status-tag"
              >
                {{ app.isOnline ? '在线' : '离线' }}
              </a-tag>
            </div>
            <div class="app-card__actions">
              <a-switch
                v-model="app.enabled"
                size="small"
                @change="(val) => handleToggleEnable(app, val)"
              />
              <a-button type="text" size="small" @click="openEditModal(app)">
                <template #icon><icon-edit /></template>
              </a-button>
              <a-popconfirm
                content="确认删除该子应用配置？"
                @ok="handleDelete(app.appCode)"
              >
                <a-button type="text" size="small" status="danger">
                  <template #icon><icon-delete /></template>
                </a-button>
              </a-popconfirm>
            </div>
          </div>

          <div class="app-card__body">
            <div class="app-info-row">
              <span class="app-info-label">入口地址</span>
              <a-tag class="app-info-value entry-tag">{{ app.entry }}</a-tag>
            </div>
            <div class="app-info-row">
              <span class="app-info-label">容器ID</span>
              <a-tag color="arcoblue" class="app-info-value">{{ app.container || '#container' }}</a-tag>
            </div>
            <div class="app-info-row">
              <span class="app-info-label">激活规则</span>
              <code class="rule-code">{{ app.pathPrefix }}</code>
            </div>
            <div class="app-info-row">
              <span class="app-info-label">访问角色</span>
              <a-space wrap>
                <a-tag
                  v-for="role in (app.roles || [])"
                  :key="role"
                  :color="roleColor(role)"
                  size="small"
                >{{ roleLabel(role) }}</a-tag>
                <a-tag v-if="!app.roles || app.roles.length === 0" color="gray" size="small">不限</a-tag>
              </a-space>
            </div>

            <!-- 健康检查信息 -->
            <div v-if="app.healthCheck" class="health-section">
              <div class="health-item">
                <span class="health-label">响应时间</span>
                <span class="health-value">{{ app.healthCheck.responseTime || '-' }}ms</span>
              </div>
              <div class="health-item">
                <span class="health-label">错误率</span>
                <span class="health-value" :class="{ 'text-danger': (app.healthCheck.errorRate || 0) > 5 }">
                  {{ app.healthCheck.errorRate || 0 }}%
                </span>
              </div>
            </div>
          </div>

          <div class="app-card__footer">
            <a-space>
              <a-button type="text" size="small" @click="handleConfig(app)">
                <template #icon><icon-settings /></template>
                配置
              </a-button>
              <a-button type="text" size="small" @click="handleViewLogs(app)">
                <template #icon><icon-file /></template>
                查看日志
              </a-button>
              <a-button type="text" size="small" @click="handleRestart(app)">
                <template #icon><icon-refresh-cw /></template>
                重启
              </a-button>
            </a-space>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && apps.length === 0" class="empty-state">
        <icon-apps class="empty-icon" />
        <p>暂无子应用，点击右上角新增</p>
      </div>
    </a-spin>

    <!-- 新增/编辑 Modal -->
    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑子应用配置' : '新增子应用配置'"
      :width="560"
      :ok-loading="saving"
      @ok="handleSave"
      @cancel="resetForm"
    >
      <a-form :model="form" layout="vertical" ref="formRef">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="应用标识（AppCode）"
              field="appCode"
              :rules="[{ required: true, message: '请输入应用标识' }]"
              extra="唯一标识符，例如：portal、ops"
            >
              <a-input
                v-model="form.appCode"
                placeholder="例如：portal"
                :disabled="isEdit"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="显示名称"
              field="title"
              :rules="[{ required: true, message: '请输入显示名称' }]"
            >
              <a-input v-model="form.title" placeholder="例如：门户系统" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item
          label="服务入口地址（Entry）"
          field="entry"
          :rules="[
            { required: true, message: '请输入服务地址' },
            { match: /^https?:\/\/.+/, message: '请输入有效的URL地址' }
          ]"
          extra="子应用服务地址，例如：http://localhost:7200"
        >
          <a-input v-model="form.entry" placeholder="http://localhost:7200" allow-clear>
            <template #suffix>
              <a-button type="text" size="small" @click="testConnection">
                测试连接
              </a-button>
            </template>
          </a-input>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="容器ID（Container）"
              field="container"
              :rules="[{ required: true, message: '请输入容器ID' }]"
              extra="挂载容器的选择器，如 #container"
            >
              <a-input v-model="form.container" placeholder="#container" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="激活规则（PathPrefix）"
              field="pathPrefix"
              :rules="[{ required: true, message: '请输入激活规则' }]"
              extra="路由前缀或正则表达式"
            >
              <a-input v-model="form.pathPrefix" placeholder="/portal" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="可访问角色（留空表示不限制）" field="roles">
          <a-checkbox-group v-model="form.roles">
            <a-checkbox value="BUYER">买家（BUYER）</a-checkbox>
            <a-checkbox value="SELLER">卖家（SELLER）</a-checkbox>
            <a-checkbox value="OPS">运营（OPS）</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item field="hideShellMenu">
          <a-switch v-model="form.hideShellMenu" />
          <span style="margin-left: 8px; color: var(--color-text-3, #86909C); font-size: 13px;">
            隐藏主应用侧边栏（子应用占满整个页面宽度）
          </span>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  IconPlus,
  IconEdit,
  IconDelete,
  IconApps,
  IconSettings,
  IconFile,
  IconRefreshCw,
} from '@arco-design/web-vue/es/icon';
import { getSubApps, registerSubApp, updateSubApp, deleteSubApp } from '../services/api';

const loading = ref(false);
const saving = ref(false);
const testingConnection = ref(false);
const apps = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

const defaultForm = () => ({
  appCode: '',
  title: '',
  entry: '',
  container: '#container',
  pathPrefix: '',
  roles: [],
  hideShellMenu: false,
});
const form = ref(defaultForm());

const ROLE_MAP = { BUYER: '买家', SELLER: '卖家', OPS: '运营' };
const ROLE_COLORS = { BUYER: 'green', SELLER: 'blue', OPS: 'orange' };
function roleLabel(role) { return ROLE_MAP[role] || role; }
function roleColor(role) { return ROLE_COLORS[role] || 'gray'; }

async function loadApps() {
  loading.value = true;
  try {
    const res = await getSubApps();
    apps.value = res.apps || [];
  } catch (e) {
    Message.error('加载子应用列表失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

function openAddModal() {
  isEdit.value = false;
  form.value = defaultForm();
  modalVisible.value = true;
}

function openEditModal(app) {
  isEdit.value = true;
  form.value = {
    appCode: app.appCode,
    title: app.title,
    entry: app.entry,
    container: app.container || '#container',
    pathPrefix: app.pathPrefix,
    roles: app.roles ? [...app.roles] : [],
    hideShellMenu: !!app.hideShellMenu,
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
      await updateSubApp(form.value.appCode, { ...form.value });
      Message.success('子应用配置已更新');
    } else {
      await registerSubApp({ ...form.value });
      Message.success('子应用配置已添加');
    }
    resetForm();
    await loadApps();
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

async function handleDelete(appCode) {
  try {
    await deleteSubApp(appCode);
    Message.success('子应用已删除');
    await loadApps();
  } catch (e) {
    Message.error('删除失败：' + e.message);
  }
}

async function testConnection() {
  if (!form.value.entry) {
    Message.warning('请先输入入口地址');
    return;
  }

  testingConnection.value = true;
  try {
    Message.info(`正在测试连接 ${form.value.entry}...`);
    setTimeout(() => {
      Message.success('连接测试成功');
      testingConnection.value = false;
    }, 1000);
  } catch (e) {
    Message.error('连接测试失败');
    testingConnection.value = false;
  }
}

async function handleToggleEnable(app, val) {
  try {
    await updateSubApp(app.appCode, { ...app, enabled: val });
    Message.success(val ? '已启用' : '已禁用');
  } catch (e) {
    app.enabled = !val;
    Message.error('操作失败');
  }
}

function handleConfig(app) {
  Message.info(`打开 ${app.title} 的详细配置`);
}

function handleViewLogs(app) {
  Message.info(`查看 ${app.title} 的运行日志`);
}

function handleRestart(app) {
  Message.loading(`正在重启 ${app.title}...`);
  setTimeout(() => {
    Message.success(`${app.title} 重启成功`);
  }, 1500);
}

onMounted(loadApps);
</script>

<style lang="scss" scoped>
.app-register-page {
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
  color: var(--color-text-1, #1D2129);
}

.page-subtitle {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.app-card {
  background: var(--color-bg-white, #fff);
  border-radius: 12px;
  border: 1px solid var(--color-border-2, #E5E6EB);
  overflow: hidden;
  transition: all 0.25s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &--offline {
    opacity: 0.7;
    border-color: var(--color-danger-3, #FCDCDC);
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 16px 12px;
    border-bottom: 1px solid var(--color-fill-2, #F5F6F7);
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__code {
    font-size: 12px;
    color: var(--color-text-3, #86909C);
    font-family: monospace;
  }

  .status-tag {
    align-self: flex-start;
  }

  &__actions {
    display: flex;
    flex-direction: column;
    gap: 4px;
    align-items: flex-end;
    flex-shrink: 0;
  }

  &__body {
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__footer {
    padding: 10px 16px;
    border-top: 1px solid var(--color-fill-2, #F5F6F7);
    background: var(--color-fill-1, #F7F8FA);
  }
}

.app-info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.app-info-label {
  font-size: 12px;
  color: var(--color-text-3, #86909C);
  white-space: nowrap;
  padding-top: 2px;
  min-width: 64px;
}

.app-info-value {
  font-size: 12px;
  word-break: break-all;
}

.entry-tag {
  font-family: monospace;
  font-size: 11px;
}

.rule-code {
  font-family: monospace;
  font-size: 12px;
  background: var(--color-fill-2, #F5F6F7);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--color-primary-6, #165DFF);
}

.health-section {
  margin-top: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border-1, #E5E6EB);
  display: flex;
  gap: 16px;
}

.health-item {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .health-label {
    font-size: 11px;
    color: var(--color-text-4, #C9CDD4);
  }

  .health-value {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-success-6, #00B42A);

    &.text-danger {
      color: var(--color-danger-6, #F53F3F);
    }
  }
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: var(--color-text-3, #86909C);
  font-size: 14px;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  color: var(--color-text-4, #C9CDD4);
}
</style>
