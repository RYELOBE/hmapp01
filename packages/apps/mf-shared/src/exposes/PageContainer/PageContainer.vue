<template>
  <div class="page-container" :class="{ 'full-content': fullContent }">
    <a-page-header
      :class="{ 'head-border': showHeadBorder }"
      @back="handleClickBack"
      v-bind="$attrs"
      :show-back="showBack"
    >
      <template v-if="slots.extra" #extra>
        <slot name="extra" />
      </template>
      <template v-if="slots.breadcrumb" #breadcrumb>
        <slot name="breadcrumb" />
      </template>
      <template v-if="slots['back-icon']" #back-icon>
        <slot name="back-icon" />
      </template>
      <template v-if="slots.title" #title>
        <span :class="{ 'large-header-title': largeHeaderTitle && !showBack }">
          <slot name="title" />
        </span>
      </template>
      <template v-if="slots.subtitle" #subtitle>
        <slot name="subtitle" />
      </template>
    </a-page-header>
    <div class="page-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { useSlots } from 'vue';

const props = defineProps({
  showBack: {
    type: Boolean,
    default: false,
  },
  fullContent: {
    type: Boolean,
    default: false,
  },
  showHeadBorder: {
    type: Boolean,
    default: false,
  },
  largeHeaderTitle: {
    type: Boolean,
    default: true,
  },
});

const slots = useSlots();
const emits = defineEmits(['back']);

function handleClickBack() {
  emits('back');
}
</script>

<style scoped>
.page-container {
  min-height: 100%;
  background: var(--color-bg-white, #fff);
}

.head-border {
  border-bottom: 1px solid var(--color-border-2, #e5e6eb);
}

.large-header-title {
  font-size: var(--font-size-title-2, 20px);
}

.page-container.full-content {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: auto;
}

.page-container.full-content :deep(.arco-page-header) {
  height: 64px;
  box-sizing: border-box;
}

.page-container.full-content .page-content {
  padding: 0;
  height: calc(100% - 64px);
}

.page-content {
  padding: 0 24px 8px 24px;
}

:deep(.arco-page-header-wrapper) {
  padding: 0 24px;
}
</style>
