<template>
  <section class="subapp-shell">
    <!-- 加载状态 -->
    <div v-if="loadingState.isLoading" class="loading-container">
      <div class="loading-content">
        <a-spin :size="40" />
        <p class="loading-text">正在加载子应用...</p>
        <p v-if="loadingState.retryCount > 0" class="retry-info">
          第 {{ loadingState.retryCount }} 次重试中...
        </p>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="loadingState.error" class="error-container">
      <div class="error-content">
        <a-result status="error" title="加载失败">
          <template #sub-title>
            <p class="error-message">{{ loadingState.error }}</p>
          </template>
          <template #extra>
            <a-space>
              <a-button type="primary" @click="handleRetry" v-if="canRetry">
                <template #icon><icon-refresh /></template>
                重试加载
              </a-button>
              <a-button @click="handleRefresh">
                刷新页面
              </a-button>
              <a-button @click="goHome">
                返回首页
              </a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </div>

    <!-- 子应用容器 - 这是 qiankun 实际挂载的位置 -->
    <div
      id="FRAME_WINDOW"
      :class="{ 'hidden': loadingState.isLoading || loadingState.error }"
    ></div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { addLoadingListener, getLoadingState, retryLoadMicroApp } from "../minFrame";
import { useRouter } from "vue-router";

const router = useRouter();
const loadingState = ref(getLoadingState());
let removeListener = null;

const canRetry = ref(true);

onMounted(() => {
  removeListener = addLoadingListener((state) => {
    loadingState.value = state;
    canRetry.value = state.retryCount < 3;
  });
});

onUnmounted(() => {
  if (removeListener) {
    removeListener();
  }
});

function handleRetry() {
  retryLoadMicroApp();
}

function handleRefresh() {
  window.location.reload();
}

function goHome() {
  router.push("/");
}
</script>

<style scoped>
.subapp-shell {
  width: 100%;
  position: relative;
}

#FRAME_WINDOW {
  width: 100%;
}

#FRAME_WINDOW.hidden {
  display: none;
}

.loading-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
}

.loading-content {
  text-align: center;
}

.loading-text {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
}

.retry-info {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.error-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
}

.error-content {
  width: 100%;
  max-width: 600px;
  padding: 40px 20px;
}

.error-message {
  color: #666;
  margin: 0;
  word-break: break-all;
}
</style>
