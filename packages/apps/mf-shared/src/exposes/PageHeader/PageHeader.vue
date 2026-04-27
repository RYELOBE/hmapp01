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
});

// 动态判断是否需要显示返回按钮
// 一级页面（如 /buyer/home）：路径段数 = 2，不显示返回
// 二级页面（如 /buyer/items/:id）：路径段数 > 2，显示返回
const isShowBack = computed(() => {
  const segments = route.path.split('/').filter(Boolean);
  return segments.length > 2;
});

// 返回上一层
const handleBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back();
  } else {
    router.push('/');
  }
};
</script>

<style scoped>
.project-page-header {
  display: flex;
  justify-content: space-between;
  height: 64px;
  line-height: 32px;
  color: var(--color-text-1, #1d2129);
  padding: 16px 24px;
}

.project-page-header-left {
  display: flex;
  font-size: 20px;
  font-weight: bold;
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
}

.project-page-header-back {
  position: relative;
  margin-right: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.project-page-header-back svg {
  position: relative;
}

.project-page-header-back::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 32px;
  height: 32px;
  background-color: transparent;
  border-radius: 100%;
  transition: background-color 0.1s cubic-bezier(0, 0, 1, 1);
}

.project-page-header-back:hover::before {
  background-color: var(--color-fill-2, #f2f3f5);
}

.show-bottom-border {
  border-bottom: 1px solid var(--color-border-2, #e5e6eb);
}
</style>
