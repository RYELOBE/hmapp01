<template>
  <a-tag
    :color="computedColor"
    :class="`status-tag status-tag--${status}`"
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
  }
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
  border-radius: 4px;
  font-size: 12px;
  padding: 0 8px;
  line-height: 20px;

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
    color: #ff4d4f;
    border-color: #ffa39e;
  }

  &--OFF_SHELF {
    background-color: #f5f5f5;
    color: #8c8c8c;
    border-color: #d9d9d9;
  }

  &--PENDING_PAYMENT {
    background-color: #e6f7ff;
    color: #1890ff;
    border-color: #91d5ff;
  }

  &--PAID {
    background-color: #f9f0ff;
    color: #722ed1;
    border-color: #d3adf7;
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
    background-color: #f5f5f5;
    color: #8c8c8c;
    border-color: #d9d9d9;
  }

  &--REFUNDING {
    background-color: #fff7e6;
    color: #fa8c16;
    border-color: #ffd591;
  }

  &--REFUNDED {
    background-color: #fff1f0;
    color: #ff4d4f;
    border-color: #ffa39e;
  }
}
</style>
