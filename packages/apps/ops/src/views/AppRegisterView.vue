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
      <div class="app-grid">
        <div
          v-for="app in apps"
          :key="app.appCode"
          class="app-card"
        >
          <div class="app-card__header">
            <div class="app-card__icon">{{ app.title[0] }}</div>
            <div class="app-card__info">
              <div class="app-card__name">{{ app.title }}</div>
              <div class="app-card__code">{{ app.appCode }}</div>
            </div>
            <div class="app-card__actions">
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
              <span class="app-info-label">挂载路径</span>
              <a-tag color="arcoblue" class="app-info-value">{{ app.pathPrefix }}</a-tag>
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
            <div v-if="app.hideShellMenu" class="app-info-row">
              <span class="app-info-label">显示模式</span>
              <a-tag color="purple" size="small">全屏（隐藏主菜单）</a-tag>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && apps.length === 0" class="empty-state">
          <icon-apps class="empty-icon" />
          <p>暂无子应用，点击右上角新增</p>
        </div>
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
        <a-form-item
          label="子应用标识（AppCode）"
          field="appCode"
          :rules="[{ required: true, message: '请输入子应用标识' }]"
          extra="唯一标识符，例如：portal、ops、order-center"
        >
          <a-input
            v-model="form.appCode"
            placeholder="例如：portal"
            :disabled="isEdit"
            allow-clear
          />
        </a-form-item>

        <a-form-item
          label="显示名称"
          field="title"
          :rules="[{ required: true, message: '请输入显示名称' }]"
        >
          <a-input v-model="form.title" placeholder="例如：门户（买家/卖家）" allow-clear />
        </a-form-item>

        <a-form-item
          label="服务入口地址（Entry）"
          field="entry"
          :rules="[{ required: true, message: '请输入服务地址' }]"
          extra="子应用服务地址，例如：http://localhost:7200"
        >
          <a-input v-model="form.entry" placeholder="例如：http://localhost:7200" allow-clear />
        </a-form-item>

        <a-form-item
          label="路由挂载前缀（PathPrefix）"
          field="pathPrefix"
          :rules="[{ required: true, message: '请输入路由前缀' }]"
          extra="主应用中该子应用的路由前缀，例如：/portal"
        >
          <a-input v-model="form.pathPrefix" placeholder="例如：/portal" allow-clear />
        </a-form-item>

        <a-form-item label="可访问角色（留空表示不限制）" field="roles">
          <a-checkbox-group v-model="form.roles">
            <a-checkbox value="BUYER">买家（BUYER）</a-checkbox>
            <a-checkbox value="SELLER">卖家（SELLER）</a-checkbox>
            <a-checkbox value="OPS">运营（OPS）</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item field="hideShellMenu">
          <a-switch v-model="form.hideShellMenu" />
          <span style="margin-left: 8px; color: #86909c; font-size: 13px;">
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
import { getAppRegisters, saveAppRegister, deleteAppRegister } from '../services/api';

// ── 状态 ──────────────────────────────────────────────
const loading = ref(false);
const saving = ref(false);
const apps = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

const defaultForm = () => ({
  appCode: '',
  title: '',
  entry: '',
  pathPrefix: '',
  roles: [],
  hideShellMenu: false,
});
const form = ref(defaultForm());

// ── 工具 ──────────────────────────────────────────────
const ROLE_MAP = { BUYER: '买家', SELLER: '卖家', OPS: '运营' };
const ROLE_COLORS = { BUYER: 'green', SELLER: 'blue', OPS: 'orange' };
function roleLabel(role) { return ROLE_MAP[role] || role; }
function roleColor(role) { return ROLE_COLORS[role] || 'gray'; }

// ── 加载 ──────────────────────────────────────────────
async function loadApps() {
  loading.value = true;
  try {
    const res = await getAppRegisters();
    apps.value = res.apps || [];
  } catch (e) {
    Message.error('加载子应用列表失败：' + e.message);
  } finally {
    loading.value = false;
  }
}

// ── Modal 控制 ─────────────────────────────────────────
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

// ── 保存 ──────────────────────────────────────────────
async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid) return;  // 有校验错误则阻止

  saving.value = true;
  try {
    await saveAppRegister({ ...form.value });
    Message.success(isEdit.value ? '子应用配置已更新' : '子应用配置已添加');
    resetForm();
    await loadApps();
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}

// ── 删除 ──────────────────────────────────────────────
async function handleDelete(appCode) {
  try {
    await deleteAppRegister(appCode);
    Message.success('子应用已删除');
    await loadApps();
  } catch (e) {
    Message.error('删除失败：' + e.message);
  }
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
  color: #1d2129;
}

.page-subtitle {
  font-size: 13px;
  color: #86909c;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.app-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  transition: box-shadow 0.2s, transform 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 16px 12px;
    border-bottom: 1px solid #f7f8fa;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #d88c1f 0%, #f0a838 100%);
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
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__code {
    font-size: 12px;
    color: #86909c;
    font-family: monospace;
  }

  &__actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
  }

  &__body {
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

.app-info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.app-info-label {
  font-size: 12px;
  color: #86909c;
  white-space: nowrap;
  padding-top: 2px;
  min-width: 56px;
}

.app-info-value {
  font-size: 12px;
  word-break: break-all;
}

.entry-tag {
  font-family: monospace;
  font-size: 12px;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #86909c;
  font-size: 14px;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  color: #c9cdd4;
}
</style>
