<template>
  <div class="condition-tag" :class="`condition-tag--${conditionClass}`">
    <span class="condition-tag__icon">{{ conditionIcon }}</span>
    <span class="condition-tag__text">{{ conditionText }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  condition: {
    type: String,
    default: 'NEW'
  }
});

const conditionMap = {
  NEW: { text: '全新', icon: '✨', class: 'new' },
  LIKE_NEW: { text: '几乎全新', icon: '🌟', class: 'like-new' },
  GOOD: { text: '良好', icon: '👍', class: 'good' },
  FAIR: { text: '一般', icon: '👌', class: 'fair' },
  POOR: { text: '较差', icon: '📉', class: 'poor' }
};

const currentCondition = computed(() => {
  return conditionMap[props.condition?.toUpperCase()] || conditionMap.NEW;
});

const conditionText = computed(() => currentCondition.value.text);
const conditionIcon = computed(() => currentCondition.value.icon);
const conditionClass = computed(() => currentCondition.value.class);
</script>

<style lang="scss" scoped>
.condition-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  pointer-events: auto;

  &__icon {
    font-size: 12px;
  }

  &--new {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    color: #fff;
  }

  &--like-new {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: #fff;
  }

  &--good {
    background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
    color: #fff;
  }

  &--fair {
    background: rgba(255, 255, 255, 0.9);
    color: #fa8c16;
    border: 1px solid #fa8c16;
  }

  &--poor {
    background: rgba(255, 255, 255, 0.9);
    color: #8c8c8c;
    border: 1px solid #d9d9d9;
  }
}
</style>
