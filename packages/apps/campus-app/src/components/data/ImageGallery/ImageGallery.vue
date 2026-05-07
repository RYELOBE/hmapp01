<template>
  <div class="image-gallery">
    <a-spin :loading="loading" tip="图片加载中...">
      <!-- 主图 -->
      <div class="image-gallery__main">
        <template v-if="!loading && currentUrl">
          <a-image
            :src="currentUrl"
            :alt="'图片 ' + (currentIndex + 1)"
            class="image-gallery__img"
            fit="cover"
            :preview="false"
            @click="openPreview"
          >
            <template #fallback>
              <div class="image-gallery__empty">
                <icon-image />
                <span>图片加载失败</span>
              </div>
            </template>
          </a-image>
        </template>
        <div v-else-if="loading || !images.length" class="image-gallery__skeleton">
          <a-skeleton :animation="true">
            <a-skeleton-line :rows="1" style="width: 100%; height: 100%;" />
          </a-skeleton>
        </div>
        <div v-else-if="hasError" class="image-gallery__empty">
          <icon-image />
          <span>图片加载失败</span>
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
          @click="switchImage(idx)"
        >
          <a-image
            :src="img"
            :alt="'缩略图 ' + (idx + 1)"
            fit="cover"
            loading="lazy"
            @error="handleThumbError($event, idx)"
          />
        </div>
      </div>
    </a-spin>

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
import { ref, computed, watch, onMounted } from "vue";
import { IconImage, IconLeft, IconRight } from "@arco-design/web-vue/es/icon";

const props = defineProps({
  images: { type: [Array, String], default: () => [] },
  initialIndex: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
});

const emit = defineEmits(["change", "preview-open", "preview-close"]);

const currentIndex = ref(props.initialIndex);
const previewVisible = ref(false);
const hasError = ref(false);

const parsedImages = computed(() => {
  if (typeof props.images === "string") {
    try { return JSON.parse(props.images); } catch { return [props.images]; }
  }
  return Array.isArray(props.images) ? props.images : [];
});

const images = parsedImages;

const currentUrl = computed(() => images.value[currentIndex.value] || "");

watch(() => props.initialIndex, (v) => { currentIndex.value = v; });

onMounted(() => {
  if (props.loading) {
    setTimeout(() => {
      hasError.value = false;
    }, 500);
  }
});

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    hasError.value = false;
    emit("change", currentIndex.value);
  }
}

function next() {
  if (currentIndex.value < images.value.length - 1) {
    currentIndex.value++;
    hasError.value = false;
    emit("change", currentIndex.value);
  }
}

function switchImage(idx) {
  currentIndex.value = idx;
  hasError.value = false;
  emit("change", idx);
}

function openPreview() {
  previewVisible.value = true;
  emit("preview-open");
}

function handleImageLoad() {
  hasError.value = false;
}

function handleImageError() {
  hasError.value = true;
}

function handleThumbError(event, idx) {
  event.target.style.display = 'none';
}
</script>

<style lang="scss" scoped>
.image-gallery {
  &__main {
    position: relative;
    width: 100%;
    border-radius: var(--border-radius-medium, 8px);
    overflow: hidden;
    background: var(--color-fill-1, #f7f8fa);
    aspect-ratio: 1 / 1;
  }

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: zoom-in;
    transition: transform 250ms ease-out;

    &:hover {
      transform: scale(1.02);
    }
  }

  &__skeleton,
  &__empty {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--color-text-4, #c9cdd4);
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
    background: rgba(255, 255, 255, 0.9);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: var(--color-text-2, #4e5969);
    transition: all 150ms ease-out;
    backdrop-filter: blur(4px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

    &:hover {
      background: rgba(255, 255, 255, 1);
      transform: translateY(-50%) scale(1.05);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
    }

    &--left { left: 12px; }
    &--right { right: 12px; }
  }

  &__counter {
    position: absolute;
    bottom: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    -webkit-backdrop-filter: blur(4px);
    backdrop-filter: blur(4px);
  }

  &__thumbs {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    overflow-x: auto;
    padding-bottom: 4px;

    &::-webkit-scrollbar {
      height: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-border-3, #d9d9d9);
      border-radius: 3px;
    }
  }

  &__thumb {
    width: 60px;
    height: 60px;
    border-radius: var(--border-radius-small, 6px);
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    flex-shrink: 0;
    transition: all 150ms ease-out;
    opacity: 0.7;

    &:hover {
      opacity: 1;
      transform: scale(1.05);
    }

    &--active {
      border-color: var(--color-primary, #165DFF);
      opacity: 1;
      box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}
</style>
