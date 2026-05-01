<template>
  <div class="portal-designer">
    <!-- 主题色选择 -->
    <section class="design-section">
      <h4 class="section-title">主题配置</h4>
      <div class="color-grid">
        <div
          v-for="(theme, idx) in themePresets"
          :key="idx"
          class="color-card"
          :class="{ active: isThemeActive(theme) }"
          @click="selectTheme(theme)"
        >
          <div class="color-preview" :style="{ background: theme.background }"></div>
          <span class="color-label">{{ theme.label }}</span>
        </div>
      </div>
    </section>

    <!-- Logo 配置 -->
    <section class="design-section">
      <h4 class="section-title">Logo 配置</h4>
      <a-form :model="localConfig" layout="vertical">
        <a-form-item label="系统名称">
          <a-input v-model="localConfig.logoConfig.systemName" placeholder="输入系统名称" />
        </a-form-item>
        <a-form-item>
          <a-switch v-model="localConfig.logoConfig.showLogoTitle" />
          <span style="margin-left: 8px; color: #86909c; font-size: 13px;">显示名称</span>
        </a-form-item>
      </a-form>
    </section>

    <!-- 布局配置 -->
    <section class="design-section">
      <h4 class="section-title">布局配置</h4>
      <div class="switch-list">
        <div class="switch-row">
          <span>显示左侧菜单</span>
          <a-switch v-model="localConfig.showLeftMenu" />
        </div>
        <div class="switch-row">
          <span>显示快捷入口</span>
          <a-switch v-model="localConfig.showQuickEntry" />
        </div>
      </div>
    </section>

    <!-- 预览区 -->
    <section class="design-section">
      <h4 class="section-title">效果预览</h4>
      <div class="preview-frame">
        <div class="preview-header" :style="{ background: localConfig.theme.background }">
          <div class="preview-brand">
            <div class="preview-logo">C</div>
            <span v-if="localConfig.logoConfig.showLogoTitle" class="preview-title">
              {{ localConfig.logoConfig.systemName || 'CampusTrade' }}
            </span>
          </div>
          <div class="preview-actions">
            <div class="preview-avatar">U</div>
          </div>
        </div>
        <div class="preview-body">
          <div v-if="localConfig.showLeftMenu" class="preview-sider">
            <div class="preview-menu-item active"></div>
            <div class="preview-menu-item"></div>
            <div class="preview-menu-item"></div>
          </div>
          <div class="preview-content">
            <div class="preview-content-block"></div>
          </div>
        </div>
      </div>
    </section>

    <a-button type="primary" long :loading="saving" @click="handleSave">
      保存配置
    </a-button>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { savePortalConfig } from '../services/api';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);
const saving = ref(false);

const themePresets = [
  { label: '科技蓝', background: 'linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)', textTheme: 'light' },
  { label: '暖阳橙', background: 'linear-gradient(135deg, #d88c1f 0%, #f0a838 100%)', textTheme: 'light' },
  { label: '森林绿', background: 'linear-gradient(135deg, #0fc6c2 0%, #2bd2b7 100%)', textTheme: 'light' },
  { label: '纯净白', background: '#ffffff', textTheme: 'dark' },
  { label: '深邃紫', background: 'linear-gradient(135deg, #722ed1 0%, #b37feb 100%)', textTheme: 'light' },
  { label: '玫瑰红', background: 'linear-gradient(135deg, #f53f3f 0%, #ff7d00 100%)', textTheme: 'light' },
];

const defaultConfig = {
  theme: { background: 'linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)', textTheme: 'light' },
  logoConfig: { systemName: '', showLogoTitle: true },
  showLeftMenu: true,
  showQuickEntry: false,
};

let localConfig = reactive({ ...defaultConfig });

// 初始化：解析后端 configJson
watch(() => props.portal, (portal) => {
  if (portal?.configJson) {
    try {
      const parsed = JSON.parse(portal.configJson);
      localConfig = reactive({ ...defaultConfig, ...parsed });
    } catch {
      localConfig = reactive({ ...defaultConfig });
    }
  } else {
    localConfig = reactive({ ...defaultConfig });
  }
}, { immediate: true });

function isThemeActive(theme) {
  return localConfig.theme.background === theme.background;
}

function selectTheme(theme) {
  localConfig.theme = { background: theme.background, textTheme: theme.textTheme };
}

async function handleSave() {
  saving.value = true;
  try {
    await savePortalConfig({
      portalCode: props.portal.portalCode,
      portalName: props.portal.portalName,
      templateType: props.portal.templateType,
      configJson: JSON.stringify(localConfig),
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
  gap: 24px;
  padding-bottom: 24px;
}

.design-section {
  .section-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
    padding-bottom: 8px;
    border-bottom: 1px solid #e5e6eb;
  }
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.color-card {
  cursor: pointer;
  border-radius: 8px;
  border: 2px solid transparent;
  overflow: hidden;
  transition: border-color 0.2s, transform 0.15s;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);

  &.active {
    border-color: #336ad8;
    transform: scale(1.02);
  }

  &:hover {
    transform: scale(1.02);
  }
}

.color-preview {
  height: 40px;
}

.color-label {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #4e5969;
  padding: 4px 0;
}

.switch-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #4e5969;
}

.preview-frame {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  height: 200px;
}

.preview-header {
  height: 36px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
}

.preview-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-logo {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: rgba(255,255,255,0.3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.preview-title {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.preview-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.preview-body {
  display: flex;
  height: calc(100% - 36px);
}

.preview-sider {
  width: 48px;
  background: #f7f8fa;
  padding: 8px 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-menu-item {
  height: 6px;
  border-radius: 2px;
  background: #e5e6eb;

  &.active {
    background: #c9cdd4;
  }
}

.preview-content {
  flex: 1;
  padding: 8px;
}

.preview-content-block {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  background: #f2f3f5;
}
</style>
<template><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
        />
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
        />
      </a-tab-pane>
      <a-tab-pane key="logo" title="Logo配置">
        <div class="logo-config"><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
        />
      </a-tab-pane>
      <a-tab-pane key="logo" title="Logo配置">
        <div class="logo-config">
          <h4 class="section-title">Logo配置</h4>
          <a-form :model="config.logoConfig" layout="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
        />
      </a-tab-pane>
      <a-tab-pane key="logo" title="Logo配置">
        <div class="logo-config">
          <h4 class="section-title">Logo配置</h4>
          <a-form :model="config.logoConfig" layout="vertical">
            <a-form-item label="系统名称">
              <a-input
                v-model="config.logoConfig.systemName"<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
                  <template<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
                  <template #icon><icon-image /></template>
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
                  <template #icon><icon-image /></template>
                  从资源库选择
                </a-button>
              </a-space>
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
                  <template #icon><icon-image /></template>
                  从资源库选择
                </a-button>
              </a-space>
            </a-form-item>
            <a-form-item>
              <a-switch
                v-model="config.logoConfig.showLogoTitle"
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
              <span style="margin-left:<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
            </a<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
              <span style="margin-left: 8px; color: #8690<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
        <Layout<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
      </a-tab-pane<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
      </a-tab-pane>
      <a-tab-pane key="preview" title="实时预览">
        <div class="preview-section">
          <h4<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
      </a-tab-pane>
      <a-tab-pane key="preview" title="实时预览">
        <div class="preview-section">
          <h4 class="section-title">实时预览</h4>
          <div class="preview-frame">
            <div
              class="preview-header"<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                  :<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <div v-<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <div v-else class="preview-logo" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d212<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                  {{ config.logoConfig.systemName?.[0] || 'C<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                  class="preview-title<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                  :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
              </div><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
              <div class="preview-actions"><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3"<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129'<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ?<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' :<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntry<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  ><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'Camp<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!--<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx)<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource ===<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button><template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import Theme<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20minimalist&image_size=square',
<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20minimalist&image_size=square',
  'https://neeko-copilot.by<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20minimalist&image_size=square',
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=modern%20tech%20company%<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20minimalist&image_size=square',
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=modern%20tech%20company%20logo%20blue&image_size<template>
  <div class="portal-designer">
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="theme" title="主题配置">
        <ThemeConfig
          v-model="config.theme"
          @select-resource="openResourceModal"
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
                <a-button size="small" @click="openResourceModal">
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
          @update:modelValue="handleLayoutChange"
        />
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
                <a-badge v-if="config.layout.showMessageCenter" :count="3" :dot="false">
                  <div class="preview-icon" :style="{ color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }">
                    <icon-bell />
                  </div>
                </a-badge>
                <div
                  v-if="config.layout.showUserAvatar"
                  class="preview-avatar"
                  :style="{ background: config.theme.textTheme === 'light' ? 'rgba(255,255,255,0.3)' : '#e5e6eb', color: config.theme.textTheme === 'light' ? '#fff' : '#1d2129' }"
                >
                  U
                </div>
              </div>
            </div>
            <div class="preview-body">
              <div v-if="config.layout.showLeftMenu" class="preview-sider">
                <div class="preview-menu-item active"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
                <div class="preview-menu-item"></div>
              </div>
              <div class="preview-content">
                <div v-if="config.layout.showQuickEntry" class="preview-quick-entry">
                  <div
                    v-for="i in config.layout.quickEntryCount"
                    :key="i"
                    class="quick-entry-item"
                  >
                    <div class="quick-entry-icon">{{ i }}</div>
                    <span class="quick-entry-label">入口{{ i }}</span>
                  </div>
                </div>
                <div class="preview-content-block"></div>
              </div>
              <div v-if="config.layout.showFloatButton" class="preview-float-button">
                <icon-plus />
              </div>
            </div>
            <div v-if="config.layout.showFooter" class="preview-footer">
              {{ config.layout.footerText || 'CampusTrade - 校园二手交易平台' }}
            </div>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>

    <div class="designer-footer">
      <a-space>
        <a-button @click="resetConfig">重置配置</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><icon-save /></template>
          保存配置
        </a-button>
      </a-space>
    </div>

    <!-- 资源选择弹窗 -->
    <a-modal
      v-model:visible="resourceModalVisible"
      title="选择资源"
      :width="600"
      :footer="false"
    >
      <div class="resource-modal">
        <div class="resource-tabs">
          <a-tabs v-model:active-key="resourceType" type="card" :style="{ marginBottom: '16px' }">
            <a-tab-pane key="image" title="图片">
              <div class="resource-grid">
                <div
                  v-for="(img, idx) in mockImages"
                  :key="idx"
                  class="resource-item"
                  :class="{ active: selectedResource === img }"
                  @click="selectResource(img)"
                >
                  <img :src="img" alt="" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="icon" title="图标">
              <div class="resource-grid">
                <div
                  v-for="(icon, idx) in mockIcons"
                  :key="idx"
                  class="resource-item icon-item"
                  :class="{ active: selectedResource === icon }"
                  @click="selectResource(icon)"
                >
                  <span>{{ icon }}</span>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="resource-footer">
          <a-button @click="resourceModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmResource">确认选择</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconImage, IconBell, IconPlus, IconSave } from '@arco-design/web-vue/es/icon';
import { savePortalConfig } from '../services/api';
import ThemeConfig from '../components/ThemeConfig.vue';
import LayoutConfig from '../components/LayoutConfig.vue';

const props = defineProps({
  portal: { type: Object, required: true },
});

const emit = defineEmits(['saved']);

const saving = ref(false);
const activeTab = ref('theme');
const resourceModalVisible = ref(false);
const resourceType = ref('image');
const selectedResource = ref('');

const mockImages = [
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=campus%20university%20logo%20design%20minimalist&image_size=square',
  'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=modern%20tech%20company%20logo%20blue&image_size=square',
  'https://neeko-copilot.bytedance.net/api/text_to