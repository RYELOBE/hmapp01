<template>
  <div class="order-goods">
    <div class="order-goods__item">
      <a-image
        :src="imageUrl"
        :alt="itemTitle"
        width="60"
        height="60"
        fit="cover"
        loading="lazy"
        :referrer-policy="'no-referrer'"
        class="order-goods__image"
      />
      <div class="order-goods__info">
        <a-typography-paragraph
          class="order-goods__title"
          :ellipsis="{ rows: 2, expandable: false }"
        >
          {{ itemTitle }}
        </a-typography-paragraph>
        <div class="order-goods__meta">
          <span class="meta-seller" v-if="sellerName">
            <icon-apps /> {{ sellerName }}
          </span>
          <span class="meta-quantity" v-if="quantity > 1">
            x{{ quantity }}
          </span>
        </div>
      </div>
      <div class="order-goods__price">
        ¥{{ itemPrice }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { IconApps } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
});

const imageUrl = computed(() => {
  const urls = props.order.imageUrls || props.order.itemImageUrl || [];
  if (typeof urls === 'string') {
    try {
      return JSON.parse(urls)[0] || '';
    } catch {
      return urls || '';
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : '';
});

const itemTitle = computed(() => {
  return props.order.itemTitle || props.order.title || '商品';
});

const itemPrice = computed(() => {
  const price = props.order.price || props.order.itemPrice || 0;
  return typeof price === 'number' ? price.toFixed(2) : price;
});

const quantity = computed(() => {
  return props.order.quantity || 1;
});

const sellerName = computed(() => {
  return props.order.sellerName || '';
});
</script>

<style lang="scss" scoped>
.order-goods {
  &__item {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__image {
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    margin: 0;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
    font-size: 12px;
    color: #6b7280;

    .meta-seller {
      display: flex;
      align-items: center;
      gap: 4px;

      :deep(.arco-icon) {
        font-size: 12px;
      }
    }

    .meta-quantity {
      color: #9ca3af;
    }
  }

  &__price {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    flex-shrink: 0;
  }
}
</style>
