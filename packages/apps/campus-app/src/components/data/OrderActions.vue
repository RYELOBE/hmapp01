<template>
  <div class="order-actions">
    <template v-if="orderStatus === 'PENDING_PAYMENT'">
      <a-button type="primary" size="small" @click="handleAction('pay')">
        去支付
      </a-button>
      <a-popconfirm content="确定要取消此订单吗？" @ok="handleAction('cancel')">
        <a-button size="small" @click.stop>取消订单</a-button>
      </a-popconfirm>
    </template>

    <template v-else-if="orderStatus === 'PAID'">
      <a-popconfirm content="确认交易完成吗？" @ok="handleAction('confirm')">
        <a-button type="primary" size="small" @click.stop>
          确认完成
        </a-button>
      </a-popconfirm>
      <a-popconfirm content="确定要退款吗？" @ok="handleAction('refund')">
        <a-button type="text" size="small" status="warning" @click.stop>
          退款
        </a-button>
      </a-popconfirm>
    </template>

    <template v-else-if="orderStatus === 'COMPLETED'">
      <a-button
        v-if="showReview"
        type="primary"
        size="small"
        @click="handleAction('review')"
      >
        评价
      </a-button>
      <a-button size="small" @click="handleAction('rebuy')">
        再次购买
      </a-button>
    </template>

    <template v-else-if="orderStatus === 'REFUNDING'">
      <template v-if="isSeller">
        <a-popconfirm content="确定同意退款吗？" @ok="handleAction('approve-refund')">
          <a-button type="primary" size="small" @click.stop>同意退款</a-button>
        </a-popconfirm>
        <a-popconfirm content="确定拒绝退款吗？" @ok="handleAction('reject-refund')">
          <a-button type="text" size="small" status="danger" @click.stop>拒绝退款</a-button>
        </a-popconfirm>
      </template>
      <a-button v-else size="small" disabled>
        退款处理中
      </a-button>
    </template>

    <template v-else-if="orderStatus === 'REFUNDED'">
      <a-button size="small" disabled>
        已退款
      </a-button>
    </template>

    <template v-else-if="orderStatus === 'CANCELLED'">
      <a-button size="small" disabled>
        已取消
      </a-button>
    </template>

    <template v-else>
      <a-button type="text" size="small" @click="handleAction('view')">
        查看详情
      </a-button>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  showReview: {
    type: Boolean,
    default: true
  },
  isSeller: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['action']);

const orderStatus = computed(() => {
  return props.order.status || props.order.orderStatus || '';
});

function handleAction(action) {
  emit('action', action);
}
</script>

<style lang="scss" scoped>
.order-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
