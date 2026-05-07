<template>
  <a-modal
    v-model:visible="visible"
    v-bind="$attrs"
    :title-align="'start'"
    :ok-loading="loading"
    :ok-text="okText"
    :cancel-text="cancelText"
    :width="width"
    :closable="true"
    :mask-closable="false"
    :unmount-on-close="true"
    @before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title>
      <div class="dialog-title">
        <component
          v-if="iconComponent && !customIcon"
          :is="iconComponent"
          class="title-icon"
          :class="`title-icon--${type}`"
        />
        <span v-else-if="customIcon" class="title-custom-icon" v-html="customIcon"></span>
        <span class="title-text">{{ title }}</span>
      </div>
    </template>

    <div class="confirm-dialog-content" :class="[`content--${type}`, { 'content--centered': centered }]">
      <div class="content-body">
        <component
          v-if="showContentIcon && iconComponent"
          :is="iconComponent"
          class="content-icon"
          :class="`content-icon--${type}`"
        />
        <span v-if="customContentIcon" class="content-custom-icon" v-html="customContentIcon"></span>

        <div class="text-wrapper">
          <div v-if="content" class="text">{{ content }}</div>
          <slot name="default">
            <slot></slot>
          </slot>
        </div>
      </div>

      <div v-if="$slots.footer" class="footer-extra">
        <slot name="footer"></slot>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed } from 'vue';
import {
  IconInfoCircleFill,
  IconCheckCircleFill,
  IconExclamationCircleFill,
  IconCloseCircleFill,
} from '@arco-design/web-vue/es/icon';

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
  okText: {
    type: String,
    default: '确定',
  },
  cancelText: {
    type: String,
    default: '取消',
  },
  width: {
    type: [String, Number],
    default: 480,
  },
  customIcon: {
    type: String,
    default: '',
  },
  customContentIcon: {
    type: String,
    default: '',
  },
  showContentIcon: {
    type: Boolean,
    default: true,
  },
  centered: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['ok', 'cancel']);

const visible = ref(false);

const iconComponent = computed(() => {
  const iconMap = {
    info: IconInfoCircleFill,
    success: IconCheckCircleFill,
    warning: IconExclamationCircleFill,
    error: IconCloseCircleFill,
  };
  return iconMap[props.type] || IconExclamationCircleFill;
});

defineExpose({
  visible,
  open: () => { visible.value = true; },
  close: () => { visible.value = false; },
});

function handleBeforeOk(done) {
  emit('ok', done);
}

function handleCancel() {
  visible.value = false;
  emit('cancel');
}
</script>

<style scoped>
.dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;

  &--info {
    color: var(--color-primary, #165DFF);
  }

  &--success {
    color: var(--color-success-6, #00b42a);
  }

  &--warning {
    color: var(--color-warning-6, #ff7d00);
  }

  &--error {
    color: var(--color-danger-6, #f53f3f);
  }
}

.title-custom-icon {
  font-size: 20px;
  line-height: 1;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  color: var(--color-text-1, #1d2129);
}

.confirm-dialog-content {
  padding: 16px 0;

  &--centered .content-body {
    flex-direction: column;
    text-align: center;
  }

  &--info {
    --type-color: var(--color-primary, #165DFF);
  }

  &--success {
    --type-color: var(--color-success-6, #00b42a);
  }

  &--warning {
    --type-color: var(--color-warning-6, #ff7d00);
  }

  &--error {
    --type-color: var(--color-danger-6, #f53f3f);
  }
}

.content-body {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.content-icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;

  &--info {
    color: var(--color-primary, #165DFF);
  }

  &--success {
    color: var(--color-success-6, #00b42a);
  }

  &--warning {
    color: var(--color-warning-6, #ff7d00);
  }

  &--error {
    color: var(--color-danger-6, #f53f3f);
  }
}

.content-custom-icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;
  line-height: 1;
}

.text-wrapper {
  flex: 1;
  min-width: 0;
}

.text {
  font-size: 14px;
  font-weight: normal;
  line-height: 22px;
  color: var(--color-text-2, #4e5969);
  white-space: pre-wrap;
  word-break: break-word;
}

.footer-extra {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-2, #e5e6eb);
}
</style>
