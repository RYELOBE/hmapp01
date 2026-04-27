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
