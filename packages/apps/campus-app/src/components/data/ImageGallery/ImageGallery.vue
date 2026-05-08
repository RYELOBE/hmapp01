<template>
  <div class="image-gallery">
    <a-spin :loading="loading" tip="图片加载中...">
      <!-- 主图 -->
      <div class="image-gallery__main">
        <template v-if="!loading && currentUrl && !hasError && images.length > 0">
          <a-image
            :src="currentUrl"
            :alt="'图片 ' + (currentIndex + 1)"
            class="image-gallery__img"
            fit="contain"
            :preview="false"
            @click="openPreview"
            @error="handleImageError"
          >
            <template #fallback>
              <div class="image-gallery__placeholder">
                <img :src="placeholderLogo" alt="图片加载失败" class="placeholder-logo" />
              </div>
            </template>
          </a-image>
        </template>
        <div v-else class="image-gallery__placeholder">
          <img :src="placeholderLogo" alt="暂无商品图片" class="placeholder-logo" />
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
import placeholderLogo from "@/assets/logo.png";

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
    background: #fafafa;
    min-height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &::before {
      content: '';
      display: block;
      padding-top: 75%; // 4:3 比例的最小高度
    }

    > * {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }
  }

  &__img {
    width: 100%;
    height: 100%;
    object-fit: contain; // 改为 contain，确保图片完整显示
    cursor: zoom-in;
    transition: transform 250ms ease-out;
    padding: 8px;

    &:hover {
      transform: scale(1.02);
    }
  }

  &__skeleton,
  &__empty,
  &__placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--color-text-4, #c9cdd4);
    gap: 12px;
    font-size: 14px;

    .placeholder-logo {
      object-fit: contain;
      max-width: 120px;
      max-height: 120px;
    }

    .placeholder-text {
      color: var(--color-text-3, #86909C);
      font-size: 14px;
      font-weight: 500;
    }
  }

  &__arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.95);
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
    z-index: 10;

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
    bottom: 16px;
    right: 16px;
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    padding: 5px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    -webkit-backdrop-filter: blur(4px);
    backdrop-filter: blur(4px);
    z-index: 10;
  }

  &__thumbs {
    display: flex;
    gap: 10px;
    margin-top: 12px;
    overflow-x: auto;
    padding-bottom: 6px;

    &::-webkit-scrollbar {
      height: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-border-3, #d9d9d9);
      border-radius: 3px;
      
      &:hover {
        background: var(--color-border-2, #c9cdd4);
      }
    }
  }

  &__thumb {
    width: 64px;
    height: 64px;
    border-radius: var(--border-radius-small, 6px);
    overflow: hidden;
    cursor: pointer;
    border: 2.5px solid transparent;
    flex-shrink: 0;
    transition: all 150ms ease-out;
    opacity: 0.7;
    background: #f5f5f5;

    &:hover {
      opacity: 1;
      transform: scale(1.08);
      border-color: rgba(22, 93, 255, 0.3);
    }

    &--active {
      border-color: var(--color-primary, #165DFF);
      opacity: 1;
      box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.15);
      transform: scale(1.05);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .image-gallery {
    &__main {
      min-height: 240px;
      
      &::before {
        padding-top: 80%; // 移动端稍微高一点
      }
    }

    &__arrow {
      width: 32px;
      height: 32px;
      font-size: 14px;
      
      &--left { left: 8px; }
      &--right { right: 8px; }
    }

    &__thumb {
      width: 56px;
      height: 56px;
    }

    &__counter {
      bottom: 12px;
      right: 12px;
      font-size: 12px;
      padding: 4px 10px;
    }

    &__img {
      padding: 6px;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
