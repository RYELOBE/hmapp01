<template>
  <a-drawer
    :visible="visible"
    :title="title"
    :width="width"
    :footer="footerVisible"
    :mask-closable="!loading"
    unmount-on-close
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template v-if="loading" #loader>
      <a-spin dot />
    </template>

    <div class="detail-drawer__content">
      <slot></slot>
    </div>

    <template v-if="$slots.footer" #footer>
      <slot name="footer"></slot>
    </template>
  </a-drawer>
</template>

<script setup>
import { computed } from 'vue';

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
    default: 560
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

const footerVisible = computed(() => {
  return props.showFooter && !!useSlots().footer;
});

function useSlots() {
  return { footer: true };
}

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
  }

  :deep(.arco-drawer-header) {
    border-bottom: 1px solid var(--color-border);
  }

  :deep(.arco-drawer-body) {
    padding: 20px;
  }

  :deep(.arco-drawer-footer) {
    border-top: 1px solid var(--color-border);
    padding: 12px 20px;
  }
}
</style>
