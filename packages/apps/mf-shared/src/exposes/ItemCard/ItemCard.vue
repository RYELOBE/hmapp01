<template>
  <div class="item-card" @click="$emit('click', item)">
    <div class="item-card__image">
      <img
        v-if="coverUrl"
        :src="coverUrl"
        :alt="item.title"
        loading="lazy"
      />
      <div v-else class="item-card__image--empty">
        <icon-image />
      </div>
      <!-- 成色标签 -->
      <span v-if="item.conditionLevel" class="item-card__condition">
        {{ conditionLabel(item.conditionLevel) }}
      </span>
    </div>
    <div class="item-card__info">
      <h3 class="item-card__title">{{ item.title }}</h3>
      <div class="item-card__meta">
        <span class="item-card__price">¥{{ item.price }}</span>
        <span v-if="item.category" class="item-card__category">{{ item.category }}</span>
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
  </div>
</template>

<script setup>
import { computed } from "vue";

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
};

function conditionLabel(level) {
  return CONDITION_MAP[level] || level || "";
}
</script>

<style lang="scss" scoped>
.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid #f0f0f0;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  &__image {
    position: relative;
    width: 100%;
    padding-top: 100%;
    background: #f7f8fa;
    overflow: hidden;

    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    &:hover img {
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
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(4px);
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    color: #0fc6c2;
  }

  &__info {
    padding: 12px;
  }

  &__title {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__meta {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 6px;
  }

  &__price {
    font-size: 18px;
    font-weight: 700;
    color: #f53f3f;
  }

  &__category {
    font-size: 11px;
    color: #86909c;
    background: #f2f3f5;
    padding: 1px 6px;
    border-radius: 3px;
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
