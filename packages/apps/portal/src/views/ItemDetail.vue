<template>
  <div class="item-detail-page">
    <div class="item-detail-container">
      <div v-if="detail" class="item-detail">
        <div class="item-gallery">
          <div class="item-main-image">
            <img :src="getMainImage()" :alt="detail.title" />
          </div>
          <div class="item-thumbs" v-if="imageList.length > 1">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="item-thumb"
              :class="{ active: activeImageIndex === index }"
              @click="activeImageIndex = index"
            >
              <img :src="img" :alt="`图片${index + 1}`" />
            </div>
          </div>
        </div>

        <div class="item-info">
          <div class="item-status-badge" :class="detail.reviewStatus">
            {{ getStatusText(detail.reviewStatus) }}
          </div>

          <h1 class="item-title">{{ detail.title }}</h1>

          <div class="item-price">
            <span class="price-current">¥{{ detail.price }}</span>
            <span v-if="detail.originalPrice" class="price-original">¥{{ detail.originalPrice }}</span>
          </div>

          <div class="item-stats">
            <div class="stat-item">
              <icon-eye />
              <span>{{ detail.viewCount || 0 }} 浏览</span>
            </div>
            <div class="stat-item" @click="handleFavorite">
              <icon-heart :class="{ 'is-favorite': isFavorited }" />
              <span>{{ favoriteCount }} 收藏</span>
            </div>
          </div>

          <a-divider />

          <div class="item-meta">
            <div class="meta-item">
              <icon-environment />
              <span>{{ detail.campus || '未知校区' }}</span>
            </div>
            <div v-if="detail.category" class="meta-item">
              <icon-apps />
              <span>{{ detail.category }}</span>
            </div>
            <div v-if="detail.conditionLevel" class="meta-item">
              <icon-star />
              <span>{{ detail.conditionLevel }}</span>
            </div>
          </div>

          <div class="item-description">
            <h3>商品描述</h3>
            <p>{{ detail.description }}</p>
          </div>

          <div class="seller-info">
            <div class="seller-avatar">
              <icon-user />
            </div>
            <div class="seller-details">
              <div class="seller-name">{{ detail.sellerName }}</div>
              <div class="seller-label">卖家 · 发布于 {{ formatDate(detail.createdAt) }}</div>
            </div>
          </div>

          <div class="item-actions">
            <a-button class="action-btn action-btn--favorite" @click="handleFavorite">
              <template #icon>
                <icon-heart :class="{ 'is-favorite': isFavorited }" />
              </template>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </a-button>
            <a-button class="action-btn action-btn--cart" @click="handleAddToCart">
              <template #icon><icon-shopping-cart /></template>
              加入购物车
            </a-button>
            <a-button
              type="primary"
              class="action-btn action-btn--buy"
              @click="handleBuy"
              :disabled="detail.reviewStatus !== 'APPROVED'"
            >
              <template #icon><icon-shopping /></template>
              立即购买
            </a-button>
          </div>
        </div>
      </div>

      <div v-else-if="loading" class="item-detail-loading">
        <a-spin size="large" />
        <p>加载中...</p>
      </div>

      <div v-else class="item-detail-empty">
        <a-empty description="商品不存在或已被删除">
          <a-button type="primary" @click="$router.push('/home')">返回首页</a-button>
        </a-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconHeart,
  IconEye,
  IconEnvironment,
  IconApps,
  IconStar,
  IconUser,
  IconShoppingCart,
  IconShopping,
} from '@arco-design/web-vue/es/icon';
import {
  getItemDetail,
  checkFavorite,
  addFavorite,
  removeFavorite,
  addToCart,
} from '../services/api';

const route = useRoute();
const router = useRouter();
const detail = ref(null);
const loading = ref(true);
const activeImageIndex = ref(0);
const isFavorited = ref(false);
const favoriteCount = ref(0);

const imageList = computed(() => {
  if (!detail.value) return [];
  if (Array.isArray(detail.value.imageUrls)) {
    return detail.value.imageUrls;
  }
  if (typeof detail.value.imageUrls === 'string') {
    try {
      return JSON.parse(detail.value.imageUrls);
    } catch {
      return [detail.value.imageUrls];
    }
  }
  return [];
});

function getMainImage() {
  if (imageList.value.length > 0) {
    return imageList.value[activeImageIndex.value];
  }
  return 'https://via.placeholder.com/400x400?text=商品图片';
}

function getStatusText(status) {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'APPROVED': '已上架',
    'REJECTED': '已驳回'
  };
  return statusMap[status] || status;
}

function formatDate(dateStr) {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

async function checkIsFavorite() {
  try {
    const res = await checkFavorite(route.params.id);
    isFavorited.value = res?.data?.isFavorited || false;
  } catch (e) {
    console.error('检查收藏状态失败', e);
  }
}

async function handleFavorite() {
  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id);
      isFavorited.value = false;
      favoriteCount.value = Math.max(0, favoriteCount.value - 1);
      Message.success('已取消收藏');
    } else {
      await addFavorite(route.params.id);
      isFavorited.value = true;
      favoriteCount.value += 1;
      Message.success('收藏成功');
    }
  } catch (e) {
    Message.error(e.message || '操作失败');
  }
}

async function handleAddToCart() {
  if (detail.value.reviewStatus !== 'APPROVED') {
    Message.warning('该商品暂不可购买');
    return;
  }
  try {
    await addToCart(detail.value.id, 1);
    Message.success('已加入购物车');
  } catch (e) {
    Message.error(e.message || '加入购物车失败');
  }
}

function handleBuy() {
  if (detail.value.reviewStatus !== 'APPROVED') {
    Message.warning('该商品暂不可购买');
    return;
  }
  router.push(`/orders/confirm/${detail.value.id}`);
}

async function loadDetail() {
  loading.value = true;
  try {
    const data = await getItemDetail(route.params.id);
    detail.value = data;
    favoriteCount.value = data.favoriteCount || 0;
    await checkIsFavorite();
  } catch (error) {
    console.error('加载详情失败:', error);
    Message.error(error.message || '加载详情失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<style lang="scss" scoped>
.item-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%);
  padding: 24px;
}

.item-detail-container {
  max-width: 1100px;
  margin: 0 auto;
}

.item-detail {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 40px;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.06);
}

.item-gallery {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-thumbs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.item-thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    border-color: #7c3aed;
  }

  &.active {
    border-color: #7c3aed;
    box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.2);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-status-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  width: fit-content;

  &.PENDING_REVIEW {
    background: #fff7e6;
    color: #fa8c16;
  }

  &.APPROVED {
    background: #f6ffed;
    color: #52c41a;
  }

  &.REJECTED {
    background: #fff1f0;
    color: #ff4d4f;
  }
}

.item-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin: 0;
}

.item-price {
  display: flex;
  align-items: baseline;
  gap: 12px;

  .price-current {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .price-original {
    font-size: 18px;
    color: #9ca3af;
    text-decoration: line-through;
  }
}

.item-stats {
  display: flex;
  gap: 24px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;

    &:hover {
      color: #7c3aed;
    }

    .is-favorite {
      color: #ef4444;
      fill: #ef4444;
    }
  }
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6b7280;
    background: #f3f4f6;
    padding: 6px 12px;
    border-radius: 6px;
  }
}

.item-description {
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #374151;
    margin: 0 0 12px;
  }

  p {
    color: #6b7280;
    line-height: 1.7;
    margin: 0;
    font-size: 14px;
  }
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;

  .seller-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  .seller-details {
    flex: 1;

    .seller-name {
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;
    }

    .seller-label {
      font-size: 12px;
      color: #9ca3af;
      margin-top: 4px;
    }
  }
}

.item-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;

  .action-btn {
    flex: 1;
    height: 48px;
    font-size: 15px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.2s;

    &--favorite {
      border: 2px solid #e5e7eb;
      color: #6b7280;

      &:hover {
        border-color: #ef4444;
        color: #ef4444;
      }

      .is-favorite {
        color: #ef4444;
        fill: #ef4444;
      }
    }

    &--cart {
      border: 2px solid #7c3aed;
      color: #7c3aed;

      &:hover {
        background: rgba(124, 58, 237, 0.05);
      }
    }

    &--buy {
      background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
      border: none;
      color: white;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(124, 58, 237, 0.4);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}

.item-detail-loading,
.item-detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.06);

  p {
    color: #6b7280;
    margin-top: 16px;
  }
}

:deep(.arco-divider) {
  margin: 8px 0;
  border-color: #f0f0f0;
}

@media (max-width: 900px) {
  .item-detail {
    grid-template-columns: 1fr;
  }

  .item-gallery {
    max-width: 420px;
    margin: 0 auto;
  }

  .item-actions {
    flex-wrap: wrap;
  }

  .action-btn {
    min-width: calc(50% - 6px);
  }
}
</style>
