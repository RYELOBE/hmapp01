<template>
  <div
    class="address-card"
    :class="{
      'address-card--default': isDefault,
      'address-card--editable': editable
    }"
  >
    <div v-if="isDefault" class="address-card__badge">
      <icon-check-circle-fill />
      默认地址
    </div>

    <div class="address-card__content">
      <div class="address-card__header">
        <div class="address-card__receiver">
          <span class="receiver-name">{{ address.receiverName }}</span>
          <span class="receiver-phone">{{ formatPhone(address.receiverPhone) }}</span>
        </div>
      </div>

      <div class="address-card__detail">
        {{ fullAddress }}
      </div>
    </div>

    <div v-if="editable" class="address-card__actions">
      <a-button type="text" size="small" @click="handleEdit">
        <template #icon><icon-edit /></template>
        编辑
      </a-button>
      <a-button
        v-if="!isDefault"
        type="text"
        size="small"
        @click="handleSetDefault"
      >
        <template #icon><icon-star /></template>
        设为默认
      </a-button>
      <a-popconfirm
        content="确定要删除此收货地址吗？"
        @ok="handleDelete"
      >
        <a-button type="text" size="small" status="danger" @click.stop>
          <template #icon><icon-delete /></template>
          删除
        </a-button>
      </a-popconfirm>
    </div>

    <slot name="extra"></slot>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import {
  IconCheckCircleFill,
  IconEdit,
  IconStar,
  IconDelete
} from '@arco-design/web-vue/es/icon';

const props = defineProps({
  address: {
    type: Object,
    required: true,
    default: () => ({})
  },
  isDefault: {
    type: Boolean,
    default: false
  },
  editable: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['edit', 'delete', 'set-default', 'click']);

const fullAddress = computed(() => {
  const parts = [
    props.address.province,
    props.address.city,
    props.address.district,
    props.address.detailAddress
  ].filter(Boolean);
  return parts.join(' ');
});

function formatPhone(phone) {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

function handleEdit() {
  emit('edit', props.address);
}

function handleDelete() {
  emit('delete', props.address);
}

function handleSetDefault() {
  emit('set-default', props.address);
}
</script>

<style lang="scss" scoped>
.address-card {
  position: relative;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;

  &:hover {
    border-color: #7c3aed;
    box-shadow: 0 4px 12px rgba(124, 58, 237, 0.1);
  }

  &--default {
    border-color: #7c3aed;
    background: linear-gradient(135deg, rgba(124, 58, 237, 0.03) 0%, white 100%);

    &:hover {
      border-color: #7c3aed;
    }
  }

  &--editable {
    cursor: pointer;
  }

  &__badge {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    color: white;
    font-size: 12px;
    border-radius: 20px;

    :deep(.arco-icon) {
      font-size: 12px;
    }
  }

  &__content {
    margin-bottom: 16px;
  }

  &__header {
    margin-bottom: 8px;
  }

  &__receiver {
    display: flex;
    align-items: center;
    gap: 12px;

    .receiver-name {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }

    .receiver-phone {
      font-size: 14px;
      color: #6b7280;
    }
  }

  &__detail {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
  }

  &__actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
