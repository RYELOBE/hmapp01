<template>
  <a-tag
    :color="computedColor"
    :class="`status-tag status-tag--${status}`"
    :size="size"
  >
    {{ computedLabel }}
  </a-tag>
</template>

<script setup>
import { ITEM_STATUS, ORDER_STATUS, ITEM_CONDITION, PAYMENT_METHODS, USER_TYPES, TRADE_METHODS } from "@campus/common/constants";
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'auto',
  },
  color: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    default: '',
  },
  size: {
    type: String,
    default: 'small',
  },
});

const allMaps = {
  item: ITEM_STATUS,
  order: ORDER_STATUS,
  condition: ITEM_CONDITION,
  payment: PAYMENT_METHODS,
  user: USER_TYPES,
  trade: TRADE_METHODS,
};

const detectType = (status) => {
  for (const [type, map] of Object.entries(allMaps)) {
    if (map[status]) return type;
  }
  return 'item';
};

const currentMap = computed(() => {
  const type = props.type === 'auto' ? detectType(props.status) : props.type;
  return allMaps[type] || ITEM_STATUS;
});

const computedColor = computed(() => {
  if (props.color) return props.color;
  const statusMap = currentMap.value;
  const config = statusMap[props.status];
  return config?.color || 'default';
});

const computedLabel = computed(() => {
  if (props.label) return props.label;
  const statusMap = currentMap.value;
  const config = statusMap[props.status];
  return config?.label || config?.value || props.status;
});
</script>

<style lang="scss" scoped>
.status-tag {
  border-radius: var(--border-radius-small, 4px);
  font-size: 12px;
  padding: 0 8px;
  line-height: 20px;
  transition: all 150ms ease-out;

  &--PENDING,
  &--PENDING_REVIEW {
    background-color: #fff7e6;
    color: #fa8c16;
    border-color: #ffd591;
  }

  &--APPROVED {
    background-color: #f6ffed;
    color: #52c41a;
    border-color: #b7eb8f;
  }

  &--REJECTED {
    background-color: #fff1f0;
    color: #f53f3f;
    border-color: #fecaca;
  }

  &--OFF_SHELF {
    background-color: var(--color-fill-2, #f5f5f5);
    color: var(--color-text-3, #86909c);
    border-color: var(--color-border-3, #d9d9d9);
  }

  &--ON_SHELF {
    background-color: #e6f7ff;
    color: #165DFF;
    border-color: #91d5ff;
  }

  &--PENDING_PAYMENT {
    background-color: #e6f7ff;
    color: #165DFF;
    border-color: #91d5ff;
  }

  &--PAID {
    background-color: #f0f5ff;
    color: #597ef7;
    border-color: #adc6ff;
  }

  &--SHIPPED {
    background-color: #f0f5ff;
    color: #2f54eb;
    border-color: #adc6ff;
  }

  &--COMPLETED {
    background-color: #f6ffed;
    color: #52c41a;
    border-color: #b7eb8f;
  }

  &--CANCELLED {
    background-color: var(--color-fill-2, #f5f5f5);
    color: var(--color-text-3, #86909c);
    border-color: var(--color-border-3, #d9d9d9);
  }

  &--REFUNDING {
    background-color: #fff7e6;
    color: #fa8c16;
    border-color: #ffd591;
  }

  &--REFUNDED {
    background-color: #fff1f0;
    color: #f53f3f;
    border-color: #fecaca;
  }

  &--PROCESSING {
    background-color: #e6f7ff;
    color: #165DFF;
    border-color: #91d5ff;
  }

  &--FAILED {
    background-color: #fff1f0;
    color: #f53f3f;
    border-color: #fecaca;
  }

  &:hover {
    opacity: 0.85;
  }
}
</style>
