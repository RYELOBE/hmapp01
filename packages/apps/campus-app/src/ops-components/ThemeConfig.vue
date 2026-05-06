<template>
  <div class="theme-config">
    <h4 class="section-title">主题配置</h4>
    
    <div class="config-section">
      <label class="config-label">内置主题</label>
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
    </div>

    <div class="config-section">
      <label class="config-label">自定义主题色</label>
      <a-space>
        <a-color-picker
          v-model="localTheme.customColor"
          show-text
          @change="handleCustomColorChange"
        />
        <a-tag :color="localTheme.customColor" class="color-value">
          {{ localTheme.customColor }}
        </a-tag>
      </a-space>
    </div>

    <div class="config-section">
      <label class="config-label">顶部文字主题</label>
      <a-radio-group v-model="localTheme.textTheme">
        <a-radio value="light">浅色文字</a-radio>
        <a-radio value="dark">深色文字</a-radio>
      </a-radio-group>
    </div>

    <div class="config-section">
      <label class="config-label">顶部背景图</label>
      <a-space direction="vertical" style="width: 100%">
        <a-input
          v-model="localTheme.headerImage"
          placeholder="输入背景图片URL"
          allow-clear
        />
        <a-button size="small" @click="openResourceModal">
          <template #icon><icon-image /></template>
          从资源库选择
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue';
import { IconImage } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
});

const emit = defineEmits(['update:modelValue', 'select-resource']);

const themePresets = [
  { label: '科技蓝', background: 'linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)', textTheme: 'light' },
  { label: '暖阳橙', background: 'linear-gradient(135deg, #d88c1f 0%, #f0a838 100%)', textTheme: 'light' },
  { label: '森林绿', background: 'linear-gradient(135deg, #0fc6c2 0%, #2bd2b7 100%)', textTheme: 'light' },
  { label: '纯净白', background: '#ffffff', textTheme: 'dark' },
  { label: '深邃紫', background: 'linear-gradient(135deg, #722ed1 0%, #b37feb 100%)', textTheme: 'light' },
  { label: '玫瑰红', background: 'linear-gradient(135deg, #f53f3f 0%, #ff7d00 100%)', textTheme: 'light' },
  { label: '天空蓝', background: 'linear-gradient(135deg, #13c2c2 0%, #87e8de 100%)', textTheme: 'light' },
  { label: '暗夜黑', background: 'linear-gradient(135deg, #1f1f1f 0%, #434343 100%)', textTheme: 'light' },
];

const defaultTheme = {
  background: 'linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)',
  textTheme: 'light',
  customColor: '#336ad8',
  headerImage: '',
};

const localTheme = reactive({ ...defaultTheme, ...props.modelValue });

watch(() => props.modelValue, (val) => {
  Object.assign(localTheme, defaultTheme, val);
}, { immediate: true, deep: true });

function isThemeActive(theme) {
  return localTheme.background === theme.background;
}

function selectTheme(theme) {
  localTheme.background = theme.background;
  localTheme.textTheme = theme.textTheme;
  emit('update:modelValue', { ...localTheme });
}

function handleCustomColorChange(color) {
  localTheme.customColor = color;
  localTheme.background = `linear-gradient(135deg, ${color} 0%, ${lightenColor(color, 30)} 100%)`;
  emit('update:modelValue', { ...localTheme });
}

function lightenColor(color, percent) {
  const num = parseInt(color.replace('#', ''), 16);
  const amt = Math.round(2.55 * percent);
  const R = Math.min(255, (num >> 16) + amt);
  const G = Math.min(255, ((num >> 8) & 0x00ff) + amt);
  const B = Math.min(255, (num & 0x0000ff) + amt);
  return `#${(0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1)}`;
}

function openResourceModal() {
  emit('select-resource');
}
</script>

<style lang="scss" scoped>
.theme-config {
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

.config-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.config-label {
  display: block;
  font-size: 13px;
  color: #4e5969;
  margin-bottom: 10px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  height: 36px;
}

.color-label {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #4e5969;
  padding: 4px 0;
}

.color-value {
  font-family: monospace;
}
</style>