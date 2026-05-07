<template>
  <div
    class="stats-card"
    :style="{
      '--card-color': color,
      '--card-bg': bgColor,
      '--card-gradient': gradient
    }"
    @click="handleClick"
  >
    <div class="stats-card__icon" :class="{ 'stats-card__icon--gradient': !!gradient }">
      <slot name="icon">
        <component :is="iconComponent" v-if="iconComponent" />
        <span v-else class="stats-card__emoji">{{ icon }}</span>
      </slot>
    </div>

    <div class="stats-card__content">
      <div class="stats-card__value">
        <a-statistic
          :value="displayValue"
          :value-from="animation ? valueFrom : value"
          :animation="animation"
          :duration="600"
          :precision="precision"
          :suffix="suffix"
        >
          <template v-if="prefix" #prefix>
            <span class="stats-card__prefix">{{ prefix }}</span>
          </template>
        </a-statistic>
      </div>
      <div class="stats-card__label">{{ title }}</div>
    </div>

    <div v-if="trend" class="stats-card__trend" :class="`stats-card__trend--${trend}`">
      <icon-arrow-up v-if="trend === 'up'" />
      <icon-arrow-down v-else-if="trend === 'down'" />
      <span>{{ trendValue }}</span>
    </div>

    <!-- 加载遮罩 -->
    <div v-if="loading" class="stats-card__loading">
      <a-spin dot />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { IconArrowUp, IconArrowDown } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  value: {
    type: [Number, String],
    default: 0
  },
  suffix: {
    type: String,
    default: ''
  },
  prefix: {
    type: String,
    default: ''
  },
  trend: {
    type: String,
    default: '',
    validator: (v) => ['', 'up', 'down'].includes(v)
  },
  trendValue: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: '#165DFF'
  },
  bgColor: {
    type: String,
    default: '#E8F3FF'
  },
  gradient: {
    type: String,
    default: ''
  },
  precision: {
    type: Number,
    default: 0
  },
  animation: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click']);

const displayValue = computed(() => {
  if (typeof props.value === 'number') return props.value;
  return parseFloat(props.value) || 0;
});

const valueFrom = computed(() => 0);

const iconComponent = computed(() => {
  return null;
});

function handleClick(e) {
  emit('click', e);
}
</script>

<style lang="scss" scoped>
.stats-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--color-bg-white, #FFF);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
  cursor: pointer;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: var(--card-color);
    border-radius: 4px 0 0 4px;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);

    .stats-card__icon {
      transform: scale(1.08) rotate(5deg);
    }
  }

  &:active {
    transform: translateY(-2px);
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    background: var(--card-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    color: var(--card-color);
    flex-shrink: 0;
    transition: all 0.25s ease;

    &--gradient {
      background: var(--card-gradient);
      color: #FFF;
    }

    :deep(svg) {
      font-size: 26px;
      color: inherit;
    }
  }

  &__emoji {
    font-size: 26px;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__value {
    :deep(.arco-statistic) {
      .arco-statistic-content {
        font-size: 28px;
        font-weight: 700;
        color: var(--color-text-1, #1D2129);
        line-height: 1.2;
      }

      .arco-statistic-title {
        font-size: 13px;
        color: var(--color-text-3, #86909C);
        margin-top: 6px;
        font-weight: 400;
      }

      .arco-statistic-suffix {
        font-size: 14px;
        margin-left: 2px;
        color: var(--color-text-2, #4E5969);
      }
    }
  }

  &__label {
    font-size: 13px;
    color: var(--color-text-3, #86909C);
    margin-top: 4px;
    font-weight: 500;
  }

  &__prefix {
    margin-right: 4px;
    font-weight: 600;
    color: var(--color-text-2, #4E5969);
  }

  &__trend {
    position: absolute;
    top: 14px;
    right: 16px;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 600;
    padding: 4px 10px;
    border-radius: 20px;

    &--up {
      color: #00B42A;
      background: rgba(0, 180, 42, 0.08);
    }

    &--down {
      color: #F53F3F;
      background: rgba(245, 63, 63, 0.08);
    }
  }

  &__loading {
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.85);
    -webkit-backdrop-filter: blur(2px);
    backdrop-filter: blur(2px);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
  }
}
</style>
