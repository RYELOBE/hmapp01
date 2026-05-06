<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-boundary__content">
      <div class="error-boundary__icon">
        <icon-close-circle-fill />
      </div>
      <h3 class="error-boundary__title">应用加载出错</h3>
      <p class="error-boundary__message">{{ errorMessage }}</p>

      <details v-if="errorDetail" class="error-boundary__detail">
        <summary>技术详情</summary>
        <pre>{{ errorDetail }}</pre>
      </details>

      <div class="error-boundary__actions">
        <a-button type="primary" @click="retry">
          <template #icon><icon-refresh /></template>
          重试
        </a-button>
        <a-button @click="goHome">
          <template #icon><icon-home /></template>
          返回首页
        </a-button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue';
import { useRouter } from 'vue-router';
import {
  IconCloseCircleFill,
  IconRefresh,
  IconHome,
} from '@arco-design/web-vue/es/icon';

const emit = defineEmits(['retry']);

const router = useRouter();
const hasError = ref(false);
const errorMessage = ref('未知错误');
const errorDetail = ref('');

onErrorCaptured((err, instance, info) => {
  console.error('[Shell ErrorBoundary]', err, info);
  hasError.value = true;
  errorMessage.value = err.message || '未知错误';
  errorDetail.value = `${err.stack || err.message}\n\nComponent: ${instance?.$options?.name || 'Unknown'}\nInfo: ${info}`;

  // 可以上报错误监控系统
  if (window.__ERROR_REPORT__) {
    window.__ERROR_REPORT__({
      error: err,
      component: instance?.$options?.name,
      info,
      timestamp: Date.now(),
    });
  }

  return false; // 阻止错误继续向上传播
});

function retry() {
  hasError.value = false;
  errorMessage.value = '';
  errorDetail.value = '';
  emit('retry');
}

function goHome() {
  retry();
  router.push('/');
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px;
  background: var(--bg-color-page, #F5F6F7);
  border-radius: var(--radius-medium, 8px);
}

.error-boundary__content {
  max-width: 480px;
  text-align: center;
}

.error-boundary__icon {
  font-size: 48px;
  color: var(--color-danger, #F53F3F);
  margin-bottom: 16px;
}

.error-boundary__title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-1, #1D2129);
  margin: 0 0 12px;
}

.error-boundary__message {
  font-size: 14px;
  color: var(--text-color-3, #86909C);
  margin: 0 0 24px;
  line-height: 1.6;
}

.error-boundary__detail {
  margin-bottom: 24px;
  text-align: left;
  background: var(--fill-color-1, #F2F3F5);
  border-radius: var(--radius-small, 4px);
  overflow: hidden;
}

.error-boundary__detail summary {
  padding: 10px 16px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-color-3, #86909C);
  user-select: none;
}

.error-boundary__detail pre {
  padding: 12px 16px;
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-color-2, #4E5969);
  white-space: pre-wrap;
  word-break: break-all;
  border-top: 1px solid var(--border-color, #E5E6EB);
  max-height: 200px;
  overflow-y: auto;
}

.error-boundary__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>