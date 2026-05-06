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
      <template v-if="slots.breadcrumb || breadcrumbs.length > 0" #breadcrumb>
        <slot name="breadcrumb">
          <a-breadcrumb v-if="breadcrumbs.length > 0">
            <a-breadcrumb-item
              v-for="(crumb, index) in breadcrumbs"
              :key="index"
              :href="index < breadcrumbs.length - 1 ? crumb.path : undefined"
            >
              {{ crumb.label }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </slot>
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
  breadcrumbs: {
    type: Array,
    default: () => [],
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
  max-width: var(--container-max-width, 1280px);
  margin: 0 auto;
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
  max-width: none;
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
  padding: var(--spacing-lg, 24px) var(--spacing-md, 16px);
}

:deep(.arco-page-header-wrapper) {
  padding: 0 var(--spacing-md, 16px);
}

:deep(.arco-page-header) {
  padding: var(--spacing-sm, 12px) var(--spacing-md, 16px);
}
</style>
