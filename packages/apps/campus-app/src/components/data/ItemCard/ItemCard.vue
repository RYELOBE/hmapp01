<template>
  <a-card
    class="item-card"
    :bordered="false"
    hoverable
    @click="$emit('click', item)"
  >
    <template #cover>
      <div class="item-card__image">
        <a-image
          v-if="coverUrl"
          :src="coverUrl"
          :alt="item.title"
          :preview-props="{ infinite: true }"
          fit="cover"
          loading="lazy"
          :referrer-policy="'no-referrer'"
        />
        <div v-else class="item-card__image--empty">
          <icon-image />
        </div>
        <a-tag
          v-if="item.conditionLevel"
          class="item-card__condition"
          :color="getConditionColor(item.conditionLevel)"
          size="small"
        >
          {{ conditionLabel(item.conditionLevel) }}
        </a-tag>
      </div>
    </template>
    <div class="item-card__info">
      <a-typography-text class="item-card__title" ellipsis>
        {{ item.title }}
      </a-typography-text>
      <div class="item-card__meta">
        <span class="item-card__price">
          <span class="item-card__price-symbol">¥</span>
          <span class="item-card__price-value">{{ formatPrice(item.price) }}</span>
        </span>
        <a-tag v-if="item.category" size="small" class="item-card__category">
          {{ item.category }}
        </a-tag>
      </div>
      <div class="item-card__footer">
        <span v-if="item.sellerName" class="item-card__seller">
          <icon-user />
          {{ item.sellerName }}
        </span>
        <span v-if="item.campus" class="item-card__campus">
          <icon-location />
          {{ item.campus }}
        </span>
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { computed } from "vue";
import { IconImage, IconUser, IconLocation } from "@arco-design/web-vue/es/icon";

const props = defineProps({
  item: { type: Object, required: true },
});

defineEmits(["click"]);

const coverUrl = computed(() => {
  const urls = props.item.imageUrls || props.item.images || [];
  if (typeof urls === "string") {
    try { return JSON.parse(urls)[0]; } catch { return urls; }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
});

const CONDITION_MAP = {
  NEW: "全新",
  "99": "99新",
  "95": "95新",
  "9": "9成新",
  "8": "8成新",
  "7": "7成新",
  BRAND_NEW: "全新",
  LIKE_NEW: "99新",
  EXCELLENT: "95新",
  GOOD: "8成新",
  FAIR: "7成新",
  POOR: "6成新",
  new: "全新",
  brand_new: "全新",
  like_new: "99新",
  excellent: "95新",
  good: "8成新",
  fair: "7成新",
  poor: "6成新",
};

const CONDITION_COLOR_MAP = {
  NEW: "#52c41a",
  BRAND_NEW: "#52c41a",
  "99": "#165DFF",
  LIKE_NEW: "#165DFF",
  EXCELLENT: "#0fc6c2",
  "95": "#0fc6c2",
  GOOD: "#fa8c16",
  "9": "#fa8c16",
  "8": "#ff7d00",
  FAIR: "#ff7d00",
  "7": "#86909c",
  POOR: "#86909c",
  new: "#52c41a",
  brand_new: "#52c41a",
  like_new: "#165DFF",
  excellent: "#0fc6c2",
  good: "#fa8c16",
  fair: "#ff7d00",
  poor: "#86909c",
};

function conditionLabel(level) {
  return CONDITION_MAP[level] || level || "";
}

function getConditionColor(level) {
  return CONDITION_COLOR_MAP[level] || "arcoblue";
}

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}
</script>

<style lang="scss" scoped>
.item-card {
  border-radius: var(--border-radius-medium, 8px);
  overflow: hidden;
  cursor: pointer;
  transition: transform 150ms ease-out, box-shadow 250ms ease-out;
  border: 1px solid var(--color-border-2, #e5e6eb);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
    border-color: transparent;
  }

  :deep(.arco-card-body) {
    padding: 12px;
  }

  &__image {
    position: relative;
    width: 100%;
    padding-top: 100%;
    background: var(--color-fill-1, #f7f8fa);
    overflow: hidden;

    :deep(.arco-image) {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;

      .arco-image-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 250ms ease-out;
      }
    }

    &:hover :deep(.arco-image-img) {
      transform: scale(1.05);
    }

    &--empty {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--color-text-4, #c9cdd4);
      font-size: 36px;
    }
  }

  &__condition {
    position: absolute;
    top: 8px;
    left: 8px;
    border: none;
    font-weight: 500;
    border-radius: 4px;
    -webkit-backdrop-filter: blur(4px);
    backdrop-filter: blur(4px);
  }

  &__info {
    padding-top: 8px;
  }

  &__title {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-1, #1d2129);
    line-height: 1.4;
    margin-bottom: 8px;
  }

  &__meta {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__price {
    font-weight: 700;
    color: var(--color-danger-6, #f53f3f);
    display: flex;
    align-items: baseline;
    gap: 1px;
  }

  &__price-symbol {
    font-size: 12px;
    font-weight: 600;
  }

  &__price-value {
    font-size: 18px;
    line-height: 1.2;
  }

  &__category {
    font-size: 11px;
    border: none;
    background: var(--color-fill-2, #f2f3f5);
    color: var(--color-text-3, #86909c);
    border-radius: 4px;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: var(--color-text-3, #86909c);
  }

  &__seller,
  &__campus {
    display: flex;
    align-items: center;
    gap: 3px;
  }
}
</style>
