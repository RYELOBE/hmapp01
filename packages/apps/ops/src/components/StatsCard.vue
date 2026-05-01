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
    <div class="stats-card__icon">
      <component :is="iconComponent" v-if="iconComponent" />
      <span v-else class="stats-card__emoji">{{ icon }}</span>
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
          :prefix="prefixComponent"
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
    default: '#336ad8'
  },
  bgColor: {
    type: String,
    default: '#e8f0ff'
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
  if (!props.icon) return null;
  return null;
});

const prefixComponent = computed(() => {
  if (props.prefix) return null;
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
  padding: 20px;
  background: var(--color-bg-1);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
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
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  &__icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    background: var(--card-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: var(--card-color);
    flex-shrink: 0;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.05);
    }

    :deep(svg) {
      font-size: 24px;
      color: var(--card-color);
    }
  }

  &__emoji {
    font-size: 24px;
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
        color: var(--color-text-1);
        line-height: 1.2;
      }

      .arco-statistic-title {
        font-size: 13px;
        color: var(--color-text-3);
        margin-top: 4px;
      }

      .arco-statistic-suffix {
        font-size: 14px;
        margin-left: 2px;
      }
    }
  }

  &__label {
    font-size: 13px;
    color: var(--color-text-3);
    margin-top: 2px;
  }

  &__prefix {
    margin-right: 4px;
  }

  &__trend {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 12px;

    &--up {
      color: #00b42a;
      background: rgba(0, 180, 42, 0.1);
    }

    &--down {
      color: #f53f3f;
      background: rgba(245, 63, 63, 0.1);
    }
  }
}
</style>
