<template>
  <div
    class="stats-card"
    :class="{ 'stats-card--loading': loading }"
    :style="cardStyle"
    @click="handleClick"
  >
    <!-- 左侧图标区域 -->
    <div class="stats-card__icon" :style="iconStyle">
      <slot name="icon">
        <span v-if="icon" class="stats-card__emoji">{{ icon }}</span>
        <component v-else-if="iconComponent" :is="iconComponent" />
      </slot>
    </div>

    <!-- 右侧内容区域 -->
    <div class="stats-card__content">
      <div class="stats-card__value">
        <template v-if="prefix">{{ prefix }}</template>
        {{ displayValue }}
        <template v-if="suffix">{{ suffix }}</template>
      </div>
      <div class="stats-card__label">{{ label }}</div>
    </div>

    <!-- 趋势指示器 -->
    <div
      v-if="trend && trend.value !== undefined"
      class="stats-card__trend"
      :class="trend.isUp ? 'stats-card__trend--up' : 'stats-card__trend--down'"
    >
      <icon-arrow-up v-if="trend.isUp" />
      <icon-arrow-down v-else />
      <span>{{ trend.value }}</span>
    </div>

    <!-- 加载遮罩 -->
    <transition name="fade">
      <div v-if="loading" class="stats-card__loading">
        <a-spin dot />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { IconArrowUp, IconArrowDown } from '@arco-design/web-vue/es/icon'

const props = defineProps({
  icon: {
    type: String,
    default: ''
  },
  value: {
    type: [Number, String],
    default: 0
  },
  label: {
    type: String,
    default: ''
  },
  trend: {
    type: Object,
    default: () => null,
    validator: (val) => !val || (typeof val.value === 'string' || typeof val.value === 'number')
  },
  color: {
    type: String,
    default: '#165DFF'
  },
  prefix: {
    type: String,
    default: ''
  },
  suffix: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const cardStyle = computed(() => ({
  '--card-color': props.color,
  '--card-bg-light': `${props.color}0D`
}))

const iconStyle = computed(() => ({
  backgroundColor: `${props.color}14`,
  color: props.color
}))

function handleClick(event) {
  emit('click', event)
}
</script>

<style lang="scss" scoped>
.stats-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--bg-card, #FFFFFF);
  border-radius: 12px;
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
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);

    .stats-card__icon {
      transform: scale(1.05) rotate(3deg);
    }
  }

  &:active {
    transform: translateY(0);
  }

  &--loading {
    pointer-events: none;
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
    transition: all 0.25s ease;

    :deep(svg) {
      width: 24px;
      height: 24px;
    }
  }

  &__emoji {
    font-size: 24px;
    line-height: 1;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 28px;
    font-weight: 800;
    color: var(--gray-600, #1D2129);
    line-height: 1.2;
    margin-bottom: 4px;
  }

  &__label {
    font-size: 13px;
    color: var(--gray-400, #86909C);
    font-weight: 500;
  }

  &__trend {
    position: absolute;
    top: 12px;
    right: 16px;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 600;
    padding: 4px 10px;
    border-radius: 20px;

    :deep(.arco-icon) {
      font-size: 14px;
    }

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
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    backdrop-filter: blur(2px);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 767px) {
  .stats-card {
    padding: 16px 20px;
    gap: 12px;

    &__icon {
      width: 44px;
      height: 44px;
      font-size: 22px;
    }

    &__value {
      font-size: 24px;
    }

    &__label {
      font-size: 12px;
    }

    &__trend {
      position: static;
      margin-left: auto;
    }
  }
}
</style>
