<template>
  <div class="project-page-header" :class="{ 'show-bottom-border': showBottomBorder && isShowBack }">
    <div class="project-page-header-left">
      <slot name="title">
        <div v-if="isShowBack" class="project-page-header-back" @click="handleBack()">
          <IconLeft />
        </div>
        {{ title }}
        <span class="project-page-header-left-desc">{{ description }}</span>
      </slot>
    </div>
    <div class="project-page-header-right">
      <slot name="actions">
        <a-space v-if="actionButtons && actionButtons.length > 0" :size="8">
          <a-button
            v-for="(btn, index) in actionButtons"
            :key="index"
            :type="btn.type || 'outline'"
            :size="btn.size || 'small'"
            :loading="btn.loading"
            :disabled="btn.disabled"
            @click="handleActionClick(btn)"
          >
            <template #icon v-if="btn.icon">
              <component :is="btn.icon" />
            </template>
            {{ btn.label }}
          </a-button>
        </a-space>
      </slot>
      <slot name="titleBoxRight">
        {{ rightInfo }}
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { IconLeft } from '@arco-design/web-vue/es/icon';

const route = useRoute();
const router = useRouter();

const props = defineProps({
  title: String,
  description: String,
  rightInfo: String,
  showBottomBorder: {
    type: Boolean,
    default: true,
  },
  actionButtons: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['back', 'action-click']);

const isShowBack = computed(() => {
  const segments = route.path.split('/').filter(Boolean);
  return segments.length > 2;
});

const handleBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back();
  } else {
    router.push('/portal/home');
  }
};

const handleActionClick = (btn) => {
  if (btn.onClick) {
    btn.onClick();
  }
  emit('action-click', btn);
};
</script>

<style scoped>
.project-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  line-height: 32px;
  color: var(--color-text-1, #1d2129);
  padding: 16px 24px;
  background: var(--color-bg-white, #fff);
  transition: background-color 150ms ease-out;
}

.project-page-header-left {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  gap: 4px;
}

.project-page-header-left-desc {
  margin-left: 8px;
  font-size: 14px;
  font-weight: normal;
  color: var(--color-text-3, #86909c);
}

.project-page-header-right {
  display: flex;
  flex: 1;
  justify-content: flex-end;
  font-size: 14px;
  font-weight: normal;
  color: var(--color-text-2, #4e5969);
  gap: 12px;
}

.project-page-header-back {
  position: relative;
  margin-right: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--border-radius-medium, 8px);
  transition: background-color 150ms ease-out;
}

.project-page-header-back:hover {
  background-color: var(--color-fill-2, #f2f3f5);
}

.show-bottom-border {
  border-bottom: 1px solid var(--color-border-2, #e5e6eb);
}
</style>
