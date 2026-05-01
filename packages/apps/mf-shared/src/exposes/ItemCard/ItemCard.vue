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
        />
        <div v-else class="item-card__image--empty">
          <icon-image />
        </div>
        <a-tag
          v-if="item.conditionLevel"
          class="item-card__condition"
          color="arcoblue"
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
        <span class="item-card__price">¥{{ item.price }}</span>
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
  new: "全新",
  "99": "99新",
  "95": "95新",
  "9": "9成新",
  "8": "8成新",
  "7": "7成新",
  brand_new: "全新",
  like_new: "99新",
  good: "8成新",
  fair: "7成新",
  poor: "6成新",
};

function conditionLabel(level) {
  return CONDITION_MAP[level] || level || "";
}
</script>

<style lang="scss" scoped>
.item-card {
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  }

  :deep(.arco-card-body) {
    padding: 12px;
  }

  &__image {
    position: relative;
    width: 100%;
    padding-top: 100%;
    background: #f7f8fa;
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
        transition: transform 0.3s ease;
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
      color: #c9cdd4;
      font-size: 36px;
    }
  }

  &__condition {
    position: absolute;
    top: 8px;
    left: 8px;
    border: none;
    font-weight: 500;
  }

  &__info {
    padding-top: 8px;
  }

  &__title {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
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
    font-size: 18px;
    font-weight: 700;
    color: #ff4d4f;
  }

  &__category {
    font-size: 11px;
    border: none;
    background: #f2f3f5;
    color: #86909c;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: #86909c;
  }

  &__seller,
  &__campus {
    display: flex;
    align-items: center;
    gap: 3px;
  }
}
</style>
