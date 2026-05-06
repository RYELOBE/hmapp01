<template>
  <a-card
    class="order-card"
    :class="{ 'order-card--clickable': clickable }"
    :bordered="false"
    :hoverable="true"
  >
    <!-- 订单头部 -->
    <div class="order-card__header">
      <div class="order-info">
        <span class="order-no">
          <a-typography-text copyable :content="order.orderNo || order.id" />
        </span>
        <span class="order-time">{{ formatTime(order.createdAt) }}</span>
      </div>
      <StatusTag :status="order.status || order.orderStatus" type="order" />
    </div>

    <!-- 商品信息区 -->
    <div class="order-card__body">
      <slot name="goods">
        <div class="goods-item" v-for="(item, index) in orderItems" :key="index">
          <img
            v-if="item.image"
            :src="item.image"
            class="goods-image"
            alt="商品图片"
          />
          <div v-else class="goods-image goods-image--empty">📦</div>
          <div class="goods-details">
            <h4 class="goods-title">{{ item.title || order.itemTitle }}</h4>
            <ConditionTag v-if="item.conditionLevel" :condition="item.conditionLevel" size="small" />
            <div class="goods-price-row">
              <span class="unit-price">¥{{ item.price?.toFixed(2) }}</span>
              <span class="quantity">× {{ item.quantity || 1 }}</span>
              <span class="subtotal">= ¥{{ (item.price * (item.quantity || 1))?.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </slot>
    </div>

    <!-- 底部区域 -->
    <div class="order-card__footer">
      <div class="amount-section">
        <span class="amount-label">总金额：</span>
        <span class="amount-value">¥{{ displayAmount }}</span>
      </div>

      <!-- 操作按钮组 -->
      <div v-if="showActions" class="action-buttons">
        <template v-if="currentStatus === 'PENDING_PAYMENT'">
          <a-button type="primary" size="small" @click="$emit('pay', order.id)">
            去付款
          </a-button>
          <a-button size="small" @click="$emit('cancel', order.id)">
            取消订单
          </a-button>
        </template>

        <template v-else-if="currentStatus === 'PENDING_SHIPMENT'">
          <a-button type="text" status="danger" size="small" @click="$emit('refund', order.id)">
            申请退款
          </a-button>
        </template>

        <template v-else-if="currentStatus === 'SHIPPED' || currentStatus === 'PENDING_RECEIVE'">
          <a-button type="primary" size="small" @click="$emit('confirm', order.id)">
            确认收货
          </a-button>
          <a-button size="small" @click="$emit('refund', order.id)">
            申请退款
          </a-button>
        </template>

        <template v-else-if="currentStatus === 'COMPLETED'">
          <a-button type="dashed" size="small" @click="$emit('review', order.id)">
            评价
          </a-button>
          <a-button type="text" size="small" @click="$emit('rebuy', order.id)">
            再次购买
          </a-button>
        </template>

        <template v-else-if="currentStatus === 'CANCELLED'">
          <a-button type="text" status="danger" size="small" @click="$emit('delete', order.id)">
            删除
          </a-button>
        </template>

        <template v-else-if="currentStatus === 'REFUNDING'">
          <a-tag color="red">退款中</a-tag>
        </template>
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { computed } from 'vue';
import StatusTag from '../shared-components/StatusTag/StatusTag.vue';
import ConditionTag from './sub/ConditionTag.vue';

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

const emit = defineEmits([
  'pay',
  'cancel',
  'confirm',
  'refund',
  'review',
  'rebuy',
  'delete',
  'click'
]);

const currentStatus = computed(() => {
  return props.order.status || props.order.orderStatus;
});

const displayAmount = computed(() => {
  const amount = props.order.totalAmount || props.order.amount || props.order.price || 0;
  return typeof amount === 'number' ? amount.toFixed(2) : amount;
});

const orderItems = computed(() => {
  if (props.order.items && Array.isArray(props.order.items)) {
    return props.order.items;
  }
  if (props.order.itemTitle) {
    return [{
      title: props.order.itemTitle,
      image: props.order.itemImage,
      price: props.order.price || props.order.unitPrice,
      quantity: props.order.quantity || 1,
      conditionLevel: props.order.conditionLevel
    }];
  }
  return [];
});

function formatTime(dateStr) {
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
</script>

<style lang="scss" scoped>
.order-card {
  background: var(--color-bg-white, #FFF);
  border-radius: 8px;
  transition: box-shadow 0.25s ease;

  &--clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
  }

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--color-border-1, #E5E6EB);
    margin-bottom: 16px;
  }

  .order-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .order-no {
      font-size: 13px;
      font-weight: 600;
      color: var(--color-text-1, #1D2129);
      font-family: monospace;

      :deep(.arco-typography) {
        font-family: monospace !important;
      }
    }

    .order-time {
      font-size: 12px;
      color: var(--color-text-3, #86909C);
    }
  }

  &__body {
    margin-bottom: 16px;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid var(--color-border-1, #E5E6EB);

    @media (max-width: 480px) {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
    }
  }
}

.goods-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.goods-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--color-fill-2, #E5E6EB);

  &--empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }
}

.goods-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;

  .goods-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-1, #1D2129);
    margin: 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .goods-price-row {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;

    .unit-price {
      color: var(--color-text-2, #4E5969);
    }

    .quantity {
      color: var(--color-text-3, #86909C);
    }

    .subtotal {
      color: var(--color-text-1, #1D2129);
      font-weight: 500;
      margin-left: auto;
    }
  }
}

.amount-section {
  display: flex;
  align-items: baseline;
  gap: 4px;

  .amount-label {
    font-size: 14px;
    color: var(--color-text-2, #4E5969);
  }

  .amount-value {
    font-size: 18px;
    font-weight: 700;
    color: #F53F3F;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
