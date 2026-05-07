<template>
  <div class="masonry-layout" ref="containerRef">
    <div
      class="masonry-layout__grid"
      :style="gridStyle"
    >
      <div
        v-for="(item, index) in items"
        :key="index"
        class="masonry-layout__item"
        :style="{ gridRowEnd: `span ${getItemHeight(item)}` }"
        @click="handleItemClick(item)"
      >
        <slot :item="item" :index="index">
          <div class="masonry-layout__default-card">
            {{ item }}
          </div>
        </slot>
      </div>
    </div>

    <div v-if="hasMore" ref="sentinelRef" class="masonry-layout__sentinel"></div>

    <div v-if="loading" class="masonry-layout__loading">
      <a-spin />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  columnWidth: {
    type: Number,
    default: 250,
  },
  gap: {
    type: Number,
    default: 16,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  hasMore: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['load-more', 'item-click']);

const containerRef = ref(null);
const sentinelRef = ref(null);
let observer = null;

const gridStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
  gridAutoRows: '10px',
  gap: `${props.gap}px`,
}));

function getItemHeight(item) {
  if (item.height) {
    return Math.ceil(item.height / 10);
  }
  return 20;
}

function handleItemClick(item) {
  emit('item-click', item);
}

function setupIntersectionObserver() {
  if (!sentinelRef.value) return;

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && props.hasMore && !props.loading) {
          emit('load-more');
        }
      });
    },
    {
      rootMargin: '100px',
      threshold: 0,
    }
  );

  observer.observe(sentinelRef.value);
}

onMounted(() => {
  setupIntersectionObserver();
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});

watch(
  () => props.hasMore,
  () => {
    if (observer && sentinelRef.value) {
      observer.unobserve(sentinelRef.value);
      observer.observe(sentinelRef.value);
    }
  }
);
</script>

<style lang="scss" scoped>
.masonry-layout {
  width: 100%;

  &__grid {
    width: 100%;
  }

  &__item {
    overflow: hidden;
    border-radius: var(--border-radius-medium, 8px);
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
    }
  }

  &__default-card {
    background: var(--color-bg-1, #ffffff);
    border: 1px solid var(--color-border-2, #e5e6eb);
    padding: 16px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__sentinel {
    width: 100%;
    height: 1px;
  }

  &__loading {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 24px 0;
  }
}
</style>
