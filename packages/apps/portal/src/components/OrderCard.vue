<template>
  <div class="order-card" :class="{ 'order-card--clickable': clickable }">
    <div class="order-card__header">
      <div class="order-card__info">
        <span class="order-no">订单号: {{ order.orderNo || order.id }}</span>
        <span class="order-time">{{ formatDate(order.createdAt) }}</span>
      </div>
      <div class="order-card__status">
        <StatusTag :status="order.status || order.orderStatus" type="order" />
      </div>
    </div>

    <div class="order-card__body">
      <div class="order-card__goods">
        <slot name="goods">
          <OrderGoods :order="order" />
        </slot>
      </div>
    </div>

    <div class="order-card__footer">
      <div class="order-card__amount">
        <span class="amount-label">实付款:</span>
        <span class="amount-value">¥{{ displayAmount }}</span>
      </div>
      <div v-if="showActions" class="order-card__actions">
        <slot name="actions">
          <OrderActions :order="order" @action="handleAction" />
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import StatusTag from 'commonprovide/status-tag';
import OrderGoods from './sub/OrderGoods.vue';
import OrderActions from './sub/OrderActions.vue';

const props = defineProps({
  order: {
    type: Object,
    required: true,
    default: () => ({})
  },
  showActions: {
    type: Boolean,
    default: true
  },
  clickable: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click', 'action']);

const displayAmount = computed(() => {
  const amount = props.order.totalAmount || props.order.amount || props.order.price || 0;
  return typeof amount === 'number' ? amount.toFixed(2) : amount;
});

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function handleAction(action) {
  emit('action', action, props.order);
}
</script>

<style lang="scss" scoped>
.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s;

  &--clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 12px;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .order-no {
      font-size: 13px;
      font-weight: 600;
      color: #1f2937;
      font-family: monospace;
    }

    .order-time {
      font-size: 12px;
      color: #6b7280;
    }
  }

  &__body {
    margin-bottom: 12px;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }

  &__amount {
    display: flex;
    align-items: baseline;
    gap: 4px;

    .amount-label {
      font-size: 13px;
      color: #6b7280;
    }

    .amount-value {
      font-size: 18px;
      font-weight: 700;
      color: #ff4d4f;
    }
  }

  &__actions {
    display: flex;
    gap: 8px;
  }
}
</style>
