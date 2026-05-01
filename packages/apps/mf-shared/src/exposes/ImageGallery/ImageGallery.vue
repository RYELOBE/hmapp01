<template>
  <div class="image-gallery">
    <!-- 主图 -->
    <div class="image-gallery__main">
      <img
        v-if="currentUrl"
        :src="currentUrl"
        :alt="'图片 ' + (currentIndex + 1)"
        class="image-gallery__img"
        @click="openPreview"
      />
      <div v-else class="image-gallery__empty">
        <icon-image />
        <span>暂无图片</span>
      </div>
      <!-- 切换箭头 -->
      <button
        v-if="images.length > 1 && currentIndex > 0"
        class="image-gallery__arrow image-gallery__arrow--left"
        @click.stop="prev"
      >
        <icon-left />
      </button>
      <button
        v-if="images.length > 1 && currentIndex < images.length - 1"
        class="image-gallery__arrow image-gallery__arrow--right"
        @click.stop="next"
      >
        <icon-right />
      </button>
      <!-- 计数器 -->
      <span v-if="images.length > 1" class="image-gallery__counter">
        {{ currentIndex + 1 }} / {{ images.length }}
      </span>
    </div>
    <!-- 缩略图 -->
    <div v-if="images.length > 1" class="image-gallery__thumbs">
      <div
        v-for="(img, idx) in images"
        :key="idx"
        class="image-gallery__thumb"
        :class="{ 'image-gallery__thumb--active': idx === currentIndex }"
        @click="currentIndex = idx"
      >
        <img :src="img" :alt="'缩略图 ' + (idx + 1)" />
      </div>
    </div>
    <!-- 图片预览 -->
    <a-image-preview-group v-model:visible="previewVisible" :current="currentIndex">
      <a-image
        v-for="(img, idx) in images"
        :key="idx"
        :src="img"
        style="display: none"
      />
    </a-image-preview-group>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";

const props = defineProps({
  images: { type: [Array, String], default: () => [] },
  initialIndex: { type: Number, default: 0 },
});

const currentIndex = ref(props.initialIndex);
const previewVisible = ref(false);

const parsedImages = computed(() => {
  if (typeof props.images === "string") {
    try { return JSON.parse(props.images); } catch { return [props.images]; }
  }
  return Array.isArray(props.images) ? props.images : [];
});

// 用 parsedImages 作为实际数据源
const images = parsedImages;

const currentUrl = computed(() => images.value[currentIndex.value] || "");

watch(() => props.initialIndex, (v) => { currentIndex.value = v; });

function prev() { if (currentIndex.value > 0) currentIndex.value--; }
function next() { if (currentIndex.value < images.value.length - 1) currentIndex.value++; }
function openPreview() { previewVisible.value = true; }
</script>

<style lang="scss" scoped>
.image-gallery {
  &__main {
    position: relative;
    width: 100%;
    border-radius: 12px;
    overflow: hidden;
    background: #f7f8fa;
    aspect-ratio: 1 / 1;
  }

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: zoom-in;
  }

  &__empty {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #c9cdd4;
    gap: 8px;
    font-size: 14px;

    .arco-icon {
      font-size: 48px;
    }
  }

  &__arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.85);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #4e5969;
    transition: background 0.2s;

    &:hover { background: rgba(255, 255, 255, 1); }

    &--left { left: 12px; }
    &--right { right: 12px; }
  }

  &__counter {
    position: absolute;
    bottom: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    padding: 2px 10px;
    border-radius: 12px;
    font-size: 12px;
  }

  &__thumbs {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    overflow-x: auto;
  }

  &__thumb {
    width: 60px;
    height: 60px;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    flex-shrink: 0;
    transition: border-color 0.2s;

    &--active { border-color: #0fc6c2; }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}
</style>
