<template>
  <div class="page-container" :class="{ 'page-container--full': fullContent }">
    <!-- 标题区域 -->
    <div v-if="showHeader" class="page-header" :class="{ 'page-header--bordered': showBorder }">
      <!-- 自定义标题插槽 -->
      <slot name="header">
        <div class="header-content">
          <div class="header-left">
            <h2 v-if="title" class="page-title">{{ title }}</h2>
            <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
          </div>

          <div v-if="$slots.extra || extraContent" class="header-right">
            <slot name="extra">{{ extraContent }}</slot>
          </div>
        </div>
      </slot>
    </div>

    <!-- 主内容区域 -->
    <div class="page-content" :class="{ 'page-content--no-padding': noPadding }">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { useSlots, computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  showBorder: {
    type: Boolean,
    default: false
  },
  fullContent: {
    type: Boolean,
    default: false
  },
  noPadding: {
    type: Boolean,
    default: false
  },
  extraContent: {
    type: [String, Object],
    default: null
  }
})

const slots = useSlots()

const showHeader = computed(() => {
  return !!props.title || !!slots.header || !!slots.extra || !!props.extraContent
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  background: var(--bg-card, #FFFFFF);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &--full {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.page-header {
  padding: 24px 24px 16px;

  &--bordered {
    border-bottom: 1px solid var(--gray-100, #F2F3F5);
  }
}

.header-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--gray-600, #1D2129);
  line-height: 1.4;
  margin: 0 0 6px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--gray-400, #86909C);
  line-height: 1.5;
  margin: 0;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-content {
  padding: 0 24px 24px;

  &--no-padding {
    padding: 0;
  }
}

@media (max-width: 767px) {
  .page-header {
    padding: 20px 16px 14px;
  }

  .page-content {
    padding: 0 16px 20px;
  }

  .header-content {
    flex-direction: column;
    gap: 12px;
  }

  .header-right {
    width: 100%;

    > * {
      flex: 1;
    }
  }

  .page-title {
    font-size: 18px;
  }
}
</style>
