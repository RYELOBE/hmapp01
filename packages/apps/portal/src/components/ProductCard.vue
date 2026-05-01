<template>
  <div
    class="product-card"
    :class="{ 'product-card--clickable': clickable }"
    @click="handleClick"
  >
    <div class="product-card__image">
      <a-image
        :src="imageUrl"
        :alt="item.title"
        width="100%"
        height="180"
        fit="cover"
        class="product-image"
      >
        <template #fallback>
          <div class="image-placeholder">
            <icon-image />
            <span>暂无图片</span>
          </div>
        </template>
      </a-image>
      <div v-if="showStatus" class="product-card__status">
        <PriceTag v-if="showPrice" :price="item.price" :original-price="item.originalPrice" />
        <ConditionTag v-if="showCondition" :condition="item.conditionLevel" />
      </div>
    </div>

    <div class="product-card__content">
      <a-typography-paragraph
        class="product-card__title"
        :ellipsis="{ rows: 2, expandable: false }"
      >
        {{ item.title || '商品名称' }}
      </a-typography-paragraph>

      <div class="product-card__meta">
        <span class="meta-category" v-if="item.category">
          {{ item.category }}
        </span>
        <span class="meta-campus" v-if="item.campus">
          {{ item.campus }}
        </span>
      </div>

      <div v-if="showActions" class="product-card__actions">
        <slot name="actions">
          <a-space direction="vertical" size="small" style="width: 100%">
            <a-button
              v-if="showAddCart"
              type="primary"
              long
              size="small"
              @click.stop="handleAddCart"
            >
              <template #icon><icon-shopping-cart /></template>
              加入购物车
            </a-button>
            <a-button
              v-if="showBuyNow"
              type="outline"
              long
              size="small"
              @click.stop="handleBuyNow"
            >
              立即购买
            </a-button>
          </a-space>
        </slot>
      </div>
    </div>

    <slot name="extra"></slot>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { IconImage, IconShoppingCart } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  item: {
    type: Object,
    required: true,
    default: () => ({})
  },
  showActions: {
    type: Boolean,
    default: true
  },
  showPrice: {
    type: Boolean,
    default: true
  },
  showCondition: {
    type: Boolean,
    default: true
  },
  showStatus: {
    type: Boolean,
    default: true
  },
  showAddCart: {
    type: Boolean,
    default: true
  },
  showBuyNow: {
    type: Boolean,
    default: false
  },
  clickable: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['click', 'add-cart', 'buy-now']);

const imageUrl = computed(() => {
  const urls = props.item.imageUrls || props.item.images || [];
  if (typeof urls === 'string') {
    try {
      return JSON.parse(urls)[0] || '';
    } catch {
      return urls || '';
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : '';
});

function handleClick() {
  if (props.clickable) {
    emit('click', props.item);
  }
}

function handleAddCart() {
  emit('add-cart', props.item);
}

function handleBuyNow() {
  emit('buy-now', props.item);
}
</script>

<script>
import PriceTag from './sub/PriceTag.vue';
import ConditionTag from './sub/ConditionTag.vue';

export default {
  components: { PriceTag, ConditionTag }
};
</script>

<style lang="scss" scoped>
.product-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f0f0f0;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);

      .product-image {
        transform: scale(1.02);
      }
    }
  }

  &__image {
    position: relative;
    overflow: hidden;

    .product-image {
      transition: transform 0.3s;
    }
  }

  &__status {
    position: absolute;
    top: 8px;
    left: 8px;
    right: 8px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    pointer-events: none;
  }

  &__content {
    padding: 12px;
  }

  &__title {
    margin: 0 0 8px;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.5;
  }

  &__meta {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;

    .meta-category,
    .meta-campus {
      font-size: 12px;
      color: #6b7280;
      background: #f3f4f6;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }

  &__actions {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
  }
}

.image-placeholder {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  color: #9ca3af;
  gap: 8px;
}
</style>
