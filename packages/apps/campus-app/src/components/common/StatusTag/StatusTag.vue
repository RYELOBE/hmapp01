<template>
  <span class="status-tag" :class="`status-tag--${statusClass}`">
    {{ displayLabel }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: ''
  }
})

const statusConfig = {
  draft: {
    label: '草稿',
    bgColor: '#F2F3F5',
    textColor: '#86909C',
    borderColor: '#D9D9D9'
  },
  pending: {
    label: '待审核',
    bgColor: '#FFF7E8',
    textColor: '#FF7D00',
    borderColor: '#FFE0A3'
  },
  pending_payment: {
    label: '待付款',
    bgColor: '#FFF7E8',
    textColor: '#FF7D00',
    borderColor: '#FFE0A3'
  },
  paid: {
    label: '已付款',
    bgColor: '#E8F3FF',
    textColor: '#165DFF',
    borderColor: '#91D5FF'
  },
  approved: {
    label: '已通过',
    bgColor: '#E8FFEA',
    textColor: '#00B42A',
    borderColor: '#B7EB8F'
  },
  rejected: {
    label: '已拒绝',
    bgColor: '#FFECE8',
    textColor: '#F53F3F',
    borderColor: '#FECACA'
  },
  active: {
    label: '进行中',
    bgColor: '#E8F3FF',
    textColor: '#165DFF',
    borderColor: '#91D5FF'
  },
  inactive: {
    label: '已停用',
    bgColor: '#F2F3F5',
    textColor: '#86909C',
    borderColor: '#D9D9D9'
  },
  processing: {
    label: '处理中',
    bgColor: '#E8F3FF',
    textColor: '#165DFF',
    borderColor: '#91D5FF'
  },
  completed: {
    label: '已完成',
    bgColor: '#E8FFEA',
    textColor: '#00B42A',
    borderColor: '#B7EB8F'
  },
  cancelled: {
    label: '已取消',
    bgColor: '#F2F3F5',
    textColor: '#86909C',
    borderColor: '#D9D9D9'
  },
  refunding: {
    label: '退款中',
    bgColor: '#FFF7E8',
    textColor: '#FF7D00',
    borderColor: '#FFE0A3'
  },
  refunded: {
    label: '已退款',
    bgColor: '#F2F3F5',
    textColor: '#86909C',
    borderColor: '#D9D9D9'
  },
  off_shelf: {
    label: '已下架',
    bgColor: '#F2F3F5',
    textColor: '#86909C',
    borderColor: '#D9D9D9'
  }
}

const statusAliasMap = {
  DRAFT: 'draft',
  PENDING_REVIEW: 'pending',
  PENDING: 'pending',
  PENDING_PAYMENT: 'pending_payment',
  PAID: 'paid',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  OFF_SHELF: 'off_shelf',
  ACTIVE: 'active',
  INACTIVE: 'inactive',
  PROCESSING: 'processing',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
  REFUNDING: 'refunding',
  REFUNDED: 'refunded'
}

const statusKey = computed(() => {
  const rawStatus = String(props.status || '').trim()
  return statusAliasMap[rawStatus.toUpperCase()] || rawStatus.toLowerCase() || 'pending'
})

const statusClass = computed(() => statusKey.value)

const currentConfig = computed(() => {
  return statusConfig[statusKey.value] || statusConfig.pending
})

const displayLabel = computed(() => {
  if (props.label) return props.label
  return currentConfig.value.label || props.status
})

const tagStyle = computed(() => ({
  backgroundColor: currentConfig.value.bgColor,
  color: currentConfig.value.textColor,
  borderColor: currentConfig.value.borderColor
}))
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover {
    opacity: 0.85;
    transform: scale(1.02);
  }

  &--pending {
    background-color: #FFF7E8;
    color: #FF7D00;
    border-color: #FFE0A3;
  }

  &--approved {
    background-color: #E8FFEA;
    color: #00B42A;
    border-color: #B7EB8F;
  }

  &--rejected {
    background-color: #FFECE8;
    color: #F53F3F;
    border-color: #FECACA;
  }

  &--active {
    background-color: #E8F3FF;
    color: #165DFF;
    border-color: #91D5FF;
  }

  &--inactive {
    background-color: #F2F3F5;
    color: #86909C;
    border-color: #D9D9D9;
  }

  &--processing {
    background-color: #E8F3FF;
    color: #165DFF;
    border-color: #91D5FF;
  }

  &--completed {
    background-color: #E8FFEA;
    color: #00B42A;
    border-color: #B7EB8F;
  }

  &--draft,
  &--cancelled,
  &--off_shelf,
  &--refunded {
    background-color: #F2F3F5;
    color: #86909C;
    border-color: #D9D9D9;
  }

  &--pending_payment {
    background-color: #FFF7E8;
    color: #FF7D00;
    border-color: #FFE0A3;
  }

  &--paid {
    background-color: #E8F3FF;
    color: #165DFF;
    border-color: #91D5FF;
  }

  &--refunding {
    background-color: #FFF7E8;
    color: #FF7D00;
    border-color: #FFE0A3;
  }
}
</style>
