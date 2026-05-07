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
        loading="lazy"
        :referrer-policy="'no-referrer'"
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
              <template #icon><icon-plus /></template>
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
import { IconImage, IconPlus } from '@arco-design/web-vue/es/icon';
import { parseFirstImageUrl } from '../utils/image-utils';

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
  return parseFirstImageUrl(props.item.imageUrls || props.item.images);
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
  background: var(--color-bg-white, #FFF);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border: 1px solid var(--color-border-2, #E5E6EB);

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
      transition: transform 0.3s ease;
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
    color: var(--color-text-1, #1D2129);
    line-height: 1.5;

    :deep(.arco-typography) {
      margin-bottom: 0 !important;
    }
  }

  &__meta {
    display: flex;
    gap: 6px;
    margin-bottom: 10px;
    flex-wrap: wrap;

    .meta-category,
    .meta-campus {
      font-size: 12px;
      color: var(--color-text-3, #86909C);
      background: var(--color-fill-2, #F5F6F7);
      padding: 2px 8px;
      border-radius: 4px;
      line-height: 1.4;
    }
  }

  &__actions {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid var(--color-border-1, #E5E6EB);
  }
}

.image-placeholder {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-fill-1, #F7F8FA);
  color: var(--color-text-4, #C9CDD4);
  gap: 8px;
}
</style>
