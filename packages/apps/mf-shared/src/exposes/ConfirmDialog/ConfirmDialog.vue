<template>
  <a-modal
    v-model:visible="visible"
    v-bind="$attrs"
    title-align="start"
    :ok-loading="loading"
    @before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title>
      <span class="title">{{ title }}</span>
    </template>
    <div class="content">
      <span class="icon">{{ typeMap[type].icon }}</span>
      <div class="text">{{ content }}</div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: '二次确认',
  },
  type: {
    type: String,
    default: 'warning',
    validator: (val) => ['info', 'success', 'warning', 'error'].includes(val),
  },
  content: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['ok', 'cancel']);

const visible = ref(false);

const typeMap = {
  info: { icon: 'ℹ️' },
  success: { icon: '✅' },
  warning: { icon: '⚠️' },
  error: { icon: '❌' },
};

// 暴露 visible 供父组件调用
defineExpose({
  visible,
});

// 确认操作
const handleBeforeOk = (done) => {
  emit('ok', done);
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.title {
  font-size: 16px;
  font-weight: bold;
  line-height: 24px;
  color: var(--color-text-1, #1d2129);
}

.content {
  font-size: 16px;
  display: flex;
  align-items: flex-start;
}

.icon {
  font-size: 20px;
  margin-right: 8px;
  flex-shrink: 0;
}

.text {
  font-size: 14px;
  font-weight: normal;
  line-height: 20px;
  color: var(--color-text-2, #4e5969);
  white-space: normal;
}
</style>
