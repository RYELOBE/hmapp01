<template>
  <a-page-header
    :title="title"
    :subtitle="subtitle"
    :show-back="showBack"
    @back="handleBack"
    class="app-page-header"
  >
    <template v-if="$slots.breadcrumb" #breadcrumb>
      <slot name="breadcrumb" />
    </template>

    <template v-if="$slots.extra" #extra>
      <slot name="extra" />
    </template>

    <template v-if="$slots.subtitle" #subtitle>
      <slot name="subtitle" />
    </template>

    <template v-if="$slots.default" #default>
      <slot />
    </template>
  </a-page-header>
</template>

<script setup>
import { useRouter } from 'vue-router';

defineProps({
  title: {
    type: String,
    default: '',
  },
  subtitle: {
    type: String,
    default: '',
  },
  showBack: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['back']);
const router = useRouter();

function handleBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/');
  }
  emit('back');
}
</script>

<style lang="scss" scoped>
.app-page-header {
  background: #fff;
  border-bottom: 1px solid var(--color-border, #E5E6EB);
  position: sticky;
  top: 0;
  z-index: 100;

  :deep(.arco-page-header-content) {
    max-width: 1280px;
    margin: 0 auto;
    padding: 12px 24px;
  }

  :deep(.arco-page-header-header) {
    display: flex;
    align-items: center;
  }

  :deep(.arco-page-header-back) {
    margin-right: 12px;

    .arco-page-header-back-btn {
      color: #4E5969;
      font-size: 14px;
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        background: #F2F3F5;
        color: #165DFF;
      }
    }
  }

  :deep(.arco-page-header-title) {
    font-size: 17px;
    font-weight: 600;
    color: #1D2129;
    margin: 0;
  }

  :deep(.arco-page-header-subtitle) {
    font-size: 13px;
    color: #86909C;
  }

  :deep(.arco-breadcrumb) {
    margin-bottom: 4px;
  }

  :deep(.arco-page-header-extra) {
    display: flex;
    align-items: center;
  }
}
</style>
