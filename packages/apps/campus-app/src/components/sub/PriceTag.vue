<template>
  <div class="price-tag" :class="{ 'price-tag--original': hasOriginal }">
    <span class="price-tag__current">
      <span class="price-tag__currency">¥</span>
      <span class="price-tag__value">{{ displayPrice }}</span>
    </span>
    <span v-if="hasOriginal" class="price-tag__original">
      ¥{{ originalPrice }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  price: {
    type: [Number, String],
    default: 0
  },
  originalPrice: {
    type: [Number, String],
    default: null
  },
  size: {
    type: String,
    default: 'normal',
    validator: (v) => ['small', 'normal', 'large'].includes(v)
  }
});

const displayPrice = computed(() => {
  const p = typeof props.price === 'number' ? props.price : parseFloat(props.price);
  return p.toFixed(2);
});

const hasOriginal = computed(() => {
  return props.originalPrice && parseFloat(props.originalPrice) > parseFloat(props.price);
});

const originalPrice = computed(() => {
  return parseFloat(props.originalPrice).toFixed(2);
});
</script>

<style lang="scss" scoped>
.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 4px 8px;
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.9) 0%, rgba(255, 77, 79, 0.8) 100%);
  border-radius: 6px;
  pointer-events: auto;

  &__current {
    color: #fff;
    font-weight: 700;
  }

  &__currency {
    font-size: 12px;
    margin-right: 1px;
  }

  &__value {
    font-size: 16px;
    font-family: 'DIN Alternate', 'Helvetica Neue', sans-serif;
  }

  &--original {
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .price-tag__current {
      color: #ff4d4f;
    }
  }

  &__original {
    font-size: 11px;
    color: #9ca3af;
    text-decoration: line-through;
  }
}
</style>
