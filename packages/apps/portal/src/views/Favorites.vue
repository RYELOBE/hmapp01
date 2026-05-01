<template>
  <div class="favorites-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">我的收藏</h2>
      <span class="favorite-count">共 {{ favorites.length }} 件收藏</span>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="favorites.length > 0" class="items-grid">
        <div
          v-for="fav in favorites"
          :key="fav.id"
          class="item-card"
          @click="goToItem(fav.item?.id)"
        >
          <div class="item-image">
            <img
              v-if="fav.item?.imageUrls"
              :src="getItemImage(fav.item)"
              :alt="fav.item?.title"
            />
            <div v-else class="item-image--empty">📷</div>
            <div class="item-status" :class="fav.item?.reviewStatus">
              {{ getStatusText(fav.item?.reviewStatus) }}
            </div>
          </div>
          <div class="item-info">
            <div class="item-title">{{ fav.item?.title || '商品已下架' }}</div>
            <div class="item-price">¥{{ fav.item?.price || 0 }}</div>
            <div class="item-meta">
              <span class="seller">{{ fav.item?.sellerName }}</span>
              <span class="campus">{{ fav.item?.campus }}</span>
            </div>
          </div>
          <div class="item-actions">
            <a-button
              type="primary"
              size="small"
              :disabled="fav.item?.reviewStatus !== 'APPROVED'"
              @click.stop="handleBuy(fav.item)"
            >
              立即购买
            </a-button>
            <a-popconfirm
              content="确定要取消收藏吗？"
              @ok="handleRemove(fav)"
            >
              <a-button type="text" status="danger" size="small" @click.stop>
                <template #icon><icon-close /></template>
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>

      <a-empty v-else-if="!loading" description="暂无收藏商品">
        <template #image>
          <icon-heart size="48" />
        </template>
        <a-button type="primary" @click="$router.push('/home')">去逛逛</a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconArrowLeft, IconHeart, IconClose } from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import { getFavoriteList, removeFavorite } from "../../services/api";

const router = useRouter();
const loading = ref(false);
const favorites = ref([]);

function getItemImage(item) {
  return parseFirstImageUrl(item?.imageUrls);
}

function getStatusText(status) {
  const statusMap = {
    "PENDING_REVIEW": "待审核",
    "APPROVED": "已上架",
    "REJECTED": "已下架",
  };
  return statusMap[status] || status;
}

function goToItem(itemId) {
  if (itemId) {
    router.push(`/item/${itemId}`);
  }
}

function handleBuy(item) {
  if (item?.reviewStatus === "APPROVED") {
    router.push(`/orders/confirm/${item.id}`);
  }
}

async function handleRemove(fav) {
  try {
    await removeFavorite(fav.itemId);
    Message.success("已取消收藏");
    await loadFavorites();
  } catch (e) {
    Message.error(e.message || "取消收藏失败");
  }
}

async function loadFavorites() {
  loading.value = true;
  try {
    const res = await getFavoriteList();
    favorites.value = res || [];
  } catch (e) {
    Message.error(e.message || "加载收藏失败");
  } finally {
    loading.value = false;
  }
}

onMounted(loadFavorites);
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.06);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }

  .favorite-count {
    font-size: 14px;
    color: #6b7280;
  }
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.item-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(124, 58, 237, 0.12);
  }
}

.item-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--empty {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f0f0;
    color: #9ca3af;
    font-size: 48px;
  }

  .item-status {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 500;
    color: white;

    &.APPROVED {
      background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
    }

    &.PENDING_REVIEW {
      background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
    }

    &.REJECTED {
      background: linear-gradient(135deg, #6b7280 0%, #9ca3af 100%);
    }
  }
}

.item-info {
  padding: 16px;

  .item-title {
    font-size: 14px;
    font-weight: 500;
    color: #1f2937;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .item-price {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 8px;
  }

  .item-meta {
    display: flex;
    gap: 8px;
    font-size: 12px;
    color: #9ca3af;

    .seller {
      &::before {
        content: "👤 ";
      }
    }

    .campus {
      &::before {
        content: "📍 ";
      }
    }
  }
}

.item-actions {
  display: flex;
  gap: 8px;
  padding: 0 16px 16px;
}

:deep(.arco-empty) {
  padding: 60px 20px;
  background: white;
  border-radius: 12px;

  .arco-empty-icon {
    color: #d1d5db;
  }
}
</style>
