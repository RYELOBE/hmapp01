<template>
  <ErrorBoundary @retry="handleRetry">
    <div class="app-main-container">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="app-loading-overlay">
        <div class="app-loading-content">
          <a-spin :size="40" />
          <p class="app-loading-text">正在加载 {{ currentAppName }}...</p>
          <p v-if="loadTimeout" class="app-loading-timeout">加载较慢，请稍候...</p>
        </div>
        <div class="app-loading-bg"></div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="hasError" class="app-error-state">
        <div class="app-error-content">
          <div class="app-error-icon">
            <icon-close-circle-fill />
          </div>
          <h3 class="app-error-title">应用加载失败</h3>
          <p class="app-error-message">{{ errorMessage }}</p>
          <div class="app-error-actions">
            <a-button type="primary" @click="retryLoad">
              <template #icon><icon-refresh /></template>
              重试
            </a-button>
            <a-button @click="$router.push('/')">
              <template #icon><icon-home /></template>
              返回首页
            </a-button>
          </div>
        </div>
      </div>

      <!-- 子应用内容（带过渡动画） -->
      <Transition name="app-fade">
        <div v-show="!isLoading && !hasError" class="subapp-container" ref="containerRef">
          <slot />
        </div>
      </Transition>
    </div>
  </ErrorBoundary>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  IconCloseCircleFill,
  IconRefresh,
  IconHome,
} from '@arco-design/web-vue/es/icon';
import ErrorBoundary from '../../components/ErrorBoundary.vue';

const emit = defineEmits(['retry']);

const router = useRouter();
const route = useRoute();
const containerRef = ref(null);

const isLoading = ref(false);
const hasError = ref(false);
const errorMessage = ref('');
const currentAppName = ref('应用');
const loadTimeout = ref(false);
let timeoutTimer = null;
let loadingStartTime = 0;

function startLoading(appName) {
  isLoading.value = true;
  hasError.value = false;
  errorMessage.value = '';
  currentAppName.value = appName || '应用';
  loadTimeout.value = false;
  loadingStartTime = Date.now();

  // 超过5秒显示超时提示
  timeoutTimer = setTimeout(() => {
    if (isLoading.value) {
      loadTimeout.value = true;
    }
  }, 5000);
}

function stopLoading() {
  isLoading.value = false;
  if (timeoutTimer) {
    clearTimeout(timeoutTimer);
    timeoutTimer = null;
  }
}

function showError(message) {
  stopLoading();
  hasError.value = true;
  errorMessage.value = message || '未知错误，请稍后重试';
}

function retryLoad() {
  hasError.value = false;
  errorMessage.value = '';
  emit('retry');
  // 刷新当前路由
  router.go(0);
}

function handleRetry() {
  retryLoad();
}

// 监听路由变化，模拟子应用加载状态
onMounted(() => {
  // 可以在这里集成实际的 Qiankun 子应用生命周期监听
});

onUnmounted(() => {
  if (timeoutTimer) {
    clearTimeout(timeoutTimer);
  }
});

// 暴露方法供父组件调用
defineExpose({
  startLoading,
  stopLoading,
  showError,
});
</script>

<style scoped>
.app-main-container {
  position: relative;
  width: 100%;
}

/* ═══ 加载遮罩层 ═══ */
.app-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: var(--z-modal-backdrop, 400);
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-loading-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(22, 93, 255, 0.03) 0%,
    rgba(64, 128, 255, 0.05) 50%,
    rgba(22, 93, 255, 0.03) 100%
  );
  animation: loadingBgPulse 2s ease-in-out infinite;
}

@keyframes loadingBgPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.app-loading-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-large, 12px);
  box-shadow: var(--shadow-modal, 0 8px 32px rgba(29, 33, 41, 0.14));
}

.app-loading-text {
  font-size: 14px;
  color: var(--text-color-2, #4E5969);
  margin: 0;
}

.app-loading-timeout {
  font-size: 12px;
  color: var(--color-warning, #FF7D00);
  margin: 0;
  animation: fadeInOut 1.5s ease-in-out infinite;
}

@keyframes fadeInOut {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

/* ═══ 错误状态 ═══ */
.app-error-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px;
  background: var(--bg-color-page, #F5F6F7);
}

.app-error-content {
  text-align: center;
  max-width: 400px;
}

.app-error-icon {
  font-size: 48px;
  color: var(--color-danger, #F53F3F);
  margin-bottom: 16px;
}

.app-error-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-1, #1D2129);
  margin: 0 0 12px;
}

.app-error-message {
  font-size: 14px;
  color: var(--text-color-3, #86909C);
  margin: 0 0 24px;
  line-height: 1.6;
}

.app-error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>