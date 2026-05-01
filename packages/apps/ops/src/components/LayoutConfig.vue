<template>
  <div class="layout-config">
    <h4 class="section-title">布局配置</h4>

    <div class="switch-group">
      <div class="switch-row">
        <span class="switch-label">显示左侧菜单</span>
        <a-switch v-model="localConfig.showLeftMenu" @change="handleChange" />
      </div>
      <div class="switch-row">
        <span class="switch-label">显示快捷入口</span>
        <a-switch v-model="localConfig.showQuickEntry" @change="handleChange" />
      </div>
      <div class="switch-row">
        <span class="switch-label">显示悬浮操作按钮</span>
        <a-switch v-model="localConfig.showFloatButton" @change="handleChange" />
      </div>
      <div class="switch-row">
        <span class="switch-label">显示页面底部信息</span>
        <a-switch v-model="localConfig.showFooter" @change="handleChange" />
      </div>
      <div class="switch-row">
        <span class="switch-label">显示消息中心</span>
        <a-switch v-model="localConfig.showMessageCenter" @change="handleChange" />
      </div>
      <div class="switch-row">
        <span class="switch-label">显示用户头像</span>
        <a-switch v-model="localConfig.showUserAvatar" @change="handleChange" />
      </div>
    </div>

    <div class="config-section" v-if="localConfig.showQuickEntry">
      <label class="config-label">快捷入口数量</label>
      <a-input-number
        v-model="localConfig.quickEntryCount"
        :min="1"
        :max="12"
        :step="1"
        @change="handleChange"
      />
    </div>

    <div class="config-section">
      <label class="config-label">页脚信息</label>
      <a-textarea
        v-model="localConfig.footerText"
        :rows="3"
        placeholder="输入页脚显示的版权信息或其他内容"
        @change="handleChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue';

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
});

const emit = defineEmits(['update:modelValue']);

const defaultConfig = {
  showLeftMenu: true,
  showQuickEntry: false,
  showFloatButton: false,
  showFooter: true,
  showMessageCenter: true,
  showUserAvatar: true,
  quickEntryCount: 6,
  footerText: 'CampusTrade - 校园二手交易平台',
};

const localConfig = reactive({ ...defaultConfig, ...props.modelValue });

watch(() => props.modelValue, (val) => {
  Object.assign(localConfig, defaultConfig, val);
}, { immediate: true, deep: true });

function handleChange() {
  emit('update:modelValue', { ...localConfig });
}
</script>

<style lang="scss" scoped>
.layout-config {
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

.switch-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-label {
  font-size: 13px;
  color: #4e5969;
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
</style>