<template>
  <a-drawer
    :visible="visible"
    :title="title"
    :width="width"
    :footer="hasFooter"
    :mask-closable="!loading"
    :unmount-on-close="true"
    :esc-to-close="!loading"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <!-- 加载状态 -->
    <template v-if="loading" #loader>
      <div class="drawer-loading">
        <a-spin dot />
        <span>加载中...</span>
      </div>
    </template>

    <!-- 内容区域 -->
    <div class="detail-drawer__content" v-if="!loading">
      <slot></slot>
    </div>

    <!-- 底部操作区 -->
    <template v-if="$slots.footer && showFooter" #footer>
      <slot name="footer"></slot>
    </template>
  </a-drawer>
</template>

<script setup>
import { computed, useSlots } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '详情'
  },
  width: {
    type: [Number, String],
    default: 720
  },
  loading: {
    type: Boolean,
    default: false
  },
  showFooter: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:visible', 'cancel', 'close']);

const slots = useSlots();
const hasFooter = computed(() => {
  return props.showFooter && !!slots.footer;
});

function handleCancel() {
  emit('update:visible', false);
  emit('cancel');
}

function handleClose() {
  emit('close');
}
</script>

<style lang="scss" scoped>
.detail-drawer {
  &__content {
    min-height: 200px;
    padding: 4px 0;
  }

  .drawer-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
    gap: 16px;
    color: var(--color-text-3, #86909C);
    font-size: 14px;
  }

  :deep(.arco-drawer) {
    .arco-drawer-header {
      border-bottom: 1px solid var(--color-border-1, #E5E6EB);
      padding: 20px 24px;

      .arco-drawer-header-title {
        font-size: 17px;
        font-weight: 600;
        color: var(--color-text-1, #1D2129);
      }
    }

    .arco-drawer-body {
      padding: 0 24px;
      background: var(--color-bg-2, #F7F8FA);
      overflow-y: auto;
    }

    .arco-drawer-footer {
      border-top: 1px solid var(--color-border-1, #E5E6EB);
      padding: 12px 24px;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      background: var(--color-bg-white, #FFF);
    }
  }
}
</style>
