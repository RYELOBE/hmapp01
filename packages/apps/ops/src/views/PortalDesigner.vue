<template>
  <div class="portal-designer">
    <div class="designer-toolbar">
      <a-space>
        <a-button @click="openResourceSystemModal">
          <template #icon><icon-apps /></template>
          资源选择
        </a-button>
        <a-tag v-if="selectedAppCode" color="arcoblue" closable @close="clearResourceSystem">
          {{ selectedAppCode === 'ops' ? '运营系统(ops)' : '门户系统(portal)' }}
        </a-tag>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal('theme')"
        />
      </a-tab-pane>
      <a-tab-pane key="logo" title="Logo配置">
        <div class="logo-config">
          <h4 class="section-title">Logo配置</h4>
          <a-form :model="config.logoConfig" layout="vertical">
            <a-form-item label="系统名称">
              <a-input
                v-model="config.logoConfig.systemName"
                placeholder="输入系统名称"
                @change="handleConfigChange"
              />
            </a-form-item>
            <a-form-item label="Logo图片">
              <a-space direction="vertical" style="width: 100%">
                <a-input
                  v-model="config.logoConfig.logoUrl"
                  placeholder="输入Logo图片URL"
                  allow-clear
                  @change="handleConfigChange"
                />
                <a-button size="small" @click="openResourceModal('logo')">
                  <template #icon><icon-image /></template>
                  从资源库选择
                </a-button>
              </a-space>
            </a-form-item>
            <a-form-item>
              <a-switch
                v-model="config.logoConfig.showLogoTitle"
                @change="handleConfigChange"
              />
              <span style="margin-left: 8px; color: #86909c; font-size: 13px;">显示名称</span>
            </a-form-item>
            <a-form-item>
              <a-switch
                v-model="config.logoConfig.showLogoImage"
                @change="handleConfigChange"
              />
              <span style="margin-left: 8px; color: #86909c; font-size: 13px;">显示Logo图片</span>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>
      <a-tab-pane key="layout" title="布局配置">
        <LayoutConfig
          v-model="config.layout"
          @update:modelValue="handleConfigChange"
        />
      </a-tab-pane>
      <a-tab-pane key="login" title="登录配置">
        <div class="login-config">
          <h4 class="section-title">登录配置</h4>
          <a-form :model="config.loginConfig" layout="vertical">
            <a-form-item>
              <a-switch
                v-model="config.loginConfig.showRegister"
                @change="handleConfigChange"
              />
              <span style="margin-left: 8px; color: #86909c; font-size: 13px;">未登录时显示注册入口</span>
            </a-form-item>
            <a-form-item label="定制首页地址">
              <a-input
                v-model="config.loginConfig.customHomeUrl"
                placeholder="例如：/home"
                @change="handleConfigChange"
              />
            </a-form-item>
            <a-form-item>
              <a-switch
                v-model="config.loginConfig.showDataIQ"
                @change="handleConfigChange"
              />
              <span style="margin-left: 8px; color: #86909c; font-size: 13px;">显示DataIQ</span>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>
      <a-tab-pane key="preview" title="实时预览">
        <div class="preview-section">
          <h4 class="section-title">实时预览</h4>
          <div class="preview-frame">
            <div
              class="preview-header"
              :style="{ background: config.theme.background }"
            >
              <div class="preview-brand">
                <div
                  v-if="config.logoConfig.showLogoImage && config.logoConfig.logoUrl"
                  class="preview-logo-image"
                  :style="{ backgroundImage: `url(${config.logoConfig.logoUrl})` }"
                ></div>
                <div v-else class="preview-logo" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                  {{ config.logoConfig.systemName?.[0] || 'C' }}
                </div>
                <span
                  v-if="config.logoConfig.showLogoTitle"
                  class="preview-title"
                  :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  {{ config.logoConfig.systemName || 'CampusTrade' }}
                </span>
              </div>
              <div class="preview-actions">
                <icon-message
                  v-if="config.layout.showMessageCenter"
                  class="preview-icon"
                  :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                />
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : 'rgba(0,0,0,0.1)' }"
                >
                  <span :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">U</span>
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="preview-quick-item"
                  ></div>
                </div>
                <div class="preview-content-block"></div>
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText }}
            </div>
            <div v-if="config.layout.showFloatButton" class="preview-float-button">
              <icon-plus />
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="action-bar">
      <a-button :loading="saving" type="primary" @click="handleSave">
        <template #icon><icon-save /></template>
        保存配置
      </a-button>
    </div>

    <ResourceSelectModal
      v-model:visible="resourceModalVisible"
      :title="resourceModalTitle"
      @select="handleResourceSelect"
    />

    <a-modal
      v-model:visible="resourceSystemModalVisible"
      title="资源系统选择"
      :width="680"
      @ok="confirmResourceSystem"
      @cancel="resourceSystemModalVisible = false"
    >
      <div class="resource-system-content">
        <a-form layout="vertical">
          <a-form-item label="选择资源系统">
            <a-select
              v-model="tempAppCode"
              placeholder="请选择要关联的资源系统"
              @change="handleAppCodeChange"
            >
              <a-option value="ops">运营系统 (ops)</a-option>
              <a-option value="portal">门户系统 (portal)</a-option>
            </a-select>
          </a-form-item>
        </a-form>

        <div v-if="menuTree.length > 0" class="menu-tree-section">
          <h4 class="section-subtitle">菜单树预览</h4>
          <a-spin :loading="menuLoading">
            <a-tree
              :data="menuTree"
              :field-names="{ key: 'id', title: 'name', children: 'children' }"
              :default-expand-all="true"
              show-line
              :selected-keys="selectedMenuKeys"
              @select="handleMenuSelect"
            />
          </a-spin>
          <div class="menu-actions">
            <a-button size="small" type="outline" @click="goToResourceManage">
              <template #icon><icon-settings /></template>
              去配置
            </a-button>
          </div>
        </div>

        <a-empty v-else-if="tempAppCode && !menuLoading" description="该资源系统暂无菜单数据" />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconSave, IconPlus, IconMessage, IconApps, IconSettings } from '@arco-design/web-vue/es/icon';
import { useRouter } from 'vue-router';
import { savePortalConfig, getResourceMenus } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';
import ResourceSelectModal from '../components/ResourceSelectModal.vue';

const router = useRouter();
const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const activeTab = ref('theme');
const saving = ref(false);
const resourceModalVisible = ref(false);
const resourceModalTitle = ref('选择资源');
const currentResourceType = ref('');

const resourceSystemModalVisible = ref(false);
const tempAppCode = ref('');
const selectedAppCode = ref('');
const menuTree = ref([]);
const menuLoading = ref(false);
const selectedMenuKeys = ref([]);

const defaultConfig = {
  theme: {
    background: 'linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)',
    textTheme: 'light',
    customColor: '#336ad8',
    headerImage: '',
  },
  logoConfig: {
    systemName: 'CampusTrade',
    logoUrl: '',
    showLogoTitle: true,
    showLogoImage: false,
  },
  layout: {
    showLeftMenu: true,
    showQuickEntry: false,
    showFloatButton: false,
    showFooter: true,
    showMessageCenter: true,
    showUserAvatar: true,
    quickEntryCount: 6,
    footerText: 'CampusTrade - 校园二手交易平台',
  },
  loginConfig: {
    showRegister: true,
    customHomeUrl: '',
    showDataIQ: false,
  },
};

const config = reactive({ ...defaultConfig });

watch(() => props.portal, (portal) => {
  if (portal?.configJson) {
    try {
      const parsed = JSON.parse(portal.configJson);
      Object.assign(config, defaultConfig, parsed);
      if (parsed.resourceSystem) {
        selectedAppCode.value = parsed.resourceSystem;
      } else if (parsed.resourceAppCode) {
        selectedAppCode.value = parsed.resourceAppCode;
      }
    } catch {
      Object.assign(config, defaultConfig);
    }
  } else {
    Object.assign(config, defaultConfig);
  }
}, { immediate: true, deep: true });

function handleConfigChange() {
  // 配置变更时自动更新
}

function openResourceModal(type) {
  currentResourceType.value = type;
  resourceModalTitle.value = type === 'theme' ? '选择背景图' : '选择Logo';
  resourceModalVisible.value = true;
}

function handleResourceSelect(resource) {
  if (currentResourceType.value === 'theme' && resource.type === 'image') {
    config.theme.headerImage = resource.url;
  } else if (currentResourceType.value === 'logo' && resource.type === 'image') {
    config.logoConfig.logoUrl = resource.url;
  }
}

function openResourceSystemModal() {
  tempAppCode.value = selectedAppCode.value || '';
  menuTree.value = [];
  selectedMenuKeys.value = [];
  if (tempAppCode.value) {
    loadMenuTree(tempAppCode.value);
  }
  resourceSystemModalVisible.value = true;
}

async function handleAppCodeChange(appCode) {
  if (appCode) {
    await loadMenuTree(appCode);
  } else {
    menuTree.value = [];
  }
}

async function loadMenuTree(appCode) {
  menuLoading.value = true;
  try {
    const res = await getResourceMenus({ appCode });
    menuTree.value = res.menus || [];
    if (menuTree.value.length > 0) {
      Message.success(`已加载 ${appCode} 系统的菜单数据`);
    }
  } catch (e) {
    Message.error('加载菜单失败：' + e.message);
    menuTree.value = [];
  } finally {
    menuLoading.value = false;
  }
}

function handleMenuSelect(selectedKeys) {
  selectedMenuKeys.value = selectedKeys;
}

function confirmResourceSystem() {
  if (!tempAppCode.value) {
    Message.warning('请先选择资源系统');
    return;
  }
  selectedAppCode.value = tempAppCode.value;
  config.resourceAppCode = tempAppCode.value;
  resourceSystemModalVisible.value = false;
  Message.success(`已关联资源系统: ${tempAppCode.value}`);
  handleConfigChange();
}

function clearResourceSystem() {
  selectedAppCode.value = '';
  tempAppCode.value = '';
  menuTree.value = [];
  selectedMenuKeys.value = [];
  delete config.resourceAppCode;
  Message.info('已清除资源系统关联');
  handleConfigChange();
}

function goToResourceManage() {
  router.push('/ops/resource/manage');
}

async function handleSave() {
  saving.value = true;
  try {
    const configToSave = { ...config };
    if (!configToSave.resourceSystem && selectedAppCode.value) {
      configToSave.resourceSystem = selectedAppCode.value;
    }
    await savePortalConfig({
      portalCode: props.portal.portalCode,
      portalName: props.portal.portalName,
      templateType: props.portal.templateType,
      configJson: JSON.stringify(configToSave),
      updatedBy: 'ops',
    });
    Message.success('门户配置已保存');
    emit('saved');
  } catch (e) {
    Message.error('保存失败：' + e.message);
  } finally {
    saving.value = false;
  }
}
</script>

<style lang="scss" scoped>
.portal-designer {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 16px;
}

.logo-config,
.login-config {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.section-title {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e6eb;
}

.preview-section {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.preview-frame {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  height: 400px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.preview-header {
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.preview-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
}

.preview-logo-image {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-icon {
  font-size: 20px;
}

.preview-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.preview-body {
  flex: 1;
  display: flex;
}

.preview-sider {
  width: 60px;
  background: #f7f8fa;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-menu-item {
  height: 40px;
  border-radius: 6px;
  background: #e5e6eb;

  &.active {
    background: #c9cdd4;
  }
}

.preview-content {
  flex: 1;
  padding: 16px;
  background: #fff;
  overflow: auto;
}

.preview-quick-entry {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.preview-quick-item {
  height: 60px;
  background: #f7f8fa;
  border-radius: 8px;
}

.preview-content-block {
  width: 100%;
  height: 160px;
  background: #f2f3f5;
  border-radius: 8px;
}

.preview-footer {
  padding: 12px;
  background: #f7f8fa;
  text-align: center;
  font-size: 13px;
  color: #86909c;
  border-top: 1px solid #e5e6eb;
}

.preview-float-button {
  position: absolute;
  right: 24px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #336ad8;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(51, 106, 216, 0.4);
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #e5e6eb;
}

.designer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  margin-bottom: 16px;
}

.resource-system-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-tree-section {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  padding: 16px;
  background: #f7f8fa;
}

.section-subtitle {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

.menu-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e6eb;
  text-align: right;
}
</style>
