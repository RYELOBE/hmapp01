<template>
  <div class="item-detail-page">
    <div class="item-detail-container">
      <a-spin :loading="loading" style="width: 100%">
        <div v-if="detail" class="item-detail">
          <div class="item-gallery">
            <ImageGallery :images="detail.imageUrls || []" />
          </div>

          <div class="item-info">
            <a-card :bordered="false" class="info-card">
              <div class="item-header">
                <StatusTag :status="detail.reviewStatus" />
                <a-tag v-if="detail.category" color="arcoblue">{{ detail.category }}</a-tag>
              </div>

              <h1 class="item-title">{{ detail.title }}</h1>

              <div class="item-price-box">
                <div class="item-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ detail.price }}</span>
                </div>
                <a-typography-text v-if="detail.originalPrice" delete type="secondary" class="price-original">
                  ¥{{ detail.originalPrice }}
                </a-typography-text>
              </div>

              <a-divider />

              <div class="item-meta">
                <div class="meta-item">
                  <icon-location />
                  <span>{{ detail.campus || '未知校区' }}</span>
                </div>
                <div class="meta-item">
                  <icon-eye />
                  <span>{{ detail.viewCount || 0 }} 浏览</span>
                </div>
                <div class="meta-item clickable" @click="handleFavorite">
                  <icon-heart :class="{ 'is-favorite': isFavorited }" />
                  <span>{{ favoriteCount }} 收藏</span>
                </div>
              </div>

              <a-divider />

              <div class="item-description">
                <a-typography-title :heading="5">商品描述</a-typography-title>
                <a-typography-paragraph class="description-text">
                  {{ detail.description || '暂无描述' }}
                </a-typography-paragraph>
              </div>

              <a-divider />

              <div class="seller-info">
                <a-avatar :size="48" class="seller-avatar">
                  <icon-user />
                </a-avatar>
                <div class="seller-details">
                  <div class="seller-name">{{ detail.sellerName || '未知卖家' }}</div>
                  <div class="seller-meta">卖家 · 发布于 {{ formatDate(detail.createdAt) }}</div>
                </div>
              </div>
            </a-card>

            <a-card :bordered="false" class="action-card">
              <a-space direction="vertical" :size="12" style="width: 100%">
                <a-button
                  class="action-btn"
                  :type="isFavorited ? 'primary' : 'outline'"
                  :status="isFavorited ? 'warning' : 'normal'"
                  long
                  @click="handleFavorite"
                >
                  <template #icon>
                    <icon-heart :class="{ 'is-favorite': isFavorited }" />
                  </template>
                  {{ isFavorited ? '已收藏' : '收藏' }}
                </a-button>
                <a-button class="action-btn action-btn--cart" long @click="handleAddToCart">
                  <template #icon><icon-plus /></template>
                  加入购物车
                </a-button>
                <a-button
                  type="primary"
                  class="action-btn action-btn--buy"
                  long
                  :disabled="detail.reviewStatus !== 'APPROVED'"
                  @click="handleBuy"
                >
                  <template #icon><icon-list /></template>
                  立即购买
                </a-button>
              </a-space>
            </a-card>
          </div>
        </div>

        <div v-else-if="!loading" class="item-detail-empty">
          <a-result status="warning" title="商品不存在或已被删除">
            <template #subtitle>
              该商品可能已下架或被删除
            </template>
            <template #extra>
              <a-button type="primary" @click="$router.push('/home')">返回首页</a-button>
            </template>
          </a-result>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconHeart,
  IconEye,
  IconLocation,
  IconUser,
  IconList,
  IconPlus,
} from '@arco-design/web-vue/es/icon';
import StatusTag from "commonprovide/status-tag";
import ImageGallery from "commonprovide/ImageGallery";
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
const isFavorited = ref(false);
const favoriteCount = ref(0);

function formatDate(dateStr) {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

async function checkIsFavorite() {
  try {
    const res = await checkFavorite(route.params.id);
    isFavorited.value = res?.isFavorited || false;
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
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<style lang="scss" scoped>
.item-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  padding: 24px;
}

.item-detail-container {
  max-width: 1100px;
  margin: 0 auto;
}

.item-detail {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 24px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.item-gallery {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  border-radius: 12px;

  :deep(.arco-card-body) {
    padding: 24px;
  }
}

.item-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.item-title {
  font-size: 22px;
  font-weight: 600;
  color: #1d2129;
  line-height: 1.4;
  margin: 0 0 16px;
}

.item-price-box {
  display: flex;
  align-items: baseline;
  gap: 12px;

  .item-price {
    display: flex;
    align-items: baseline;
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;

    .price-symbol {
      font-size: 18px;
      font-weight: 500;
    }

    .price-value {
      font-size: 36px;
      font-weight: 700;
    }
  }

  .price-original {
    font-size: 16px;
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
    color: #4e5969;
    cursor: pointer;
    transition: color 0.2s;

    &.clickable:hover {
      color: #165dff;
    }

    .is-favorite {
      color: #ff4d4f;
      fill: #ff4d4f;
    }
  }
}

.item-description {
  .description-text {
    color: #4e5969;
    line-height: 1.8;
    white-space: pre-wrap;
    margin: 0;
  }
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;

  .seller-avatar {
    background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
    color: #fff;
  }

  .seller-details {
    flex: 1;

    .seller-name {
      font-size: 15px;
      font-weight: 600;
      color: #1d2129;
    }

    .seller-meta {
      font-size: 12px;
      color: #86909c;
      margin-top: 4px;
    }
  }
}

.action-card {
  border-radius: 12px;

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.action-btn {
  height: 44px;
  font-size: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &--cart {
    border-color: #165dff;
    color: #165dff;

    &:hover {
      background: rgba(22, 93, 255, 0.05);
    }
  }

  &--buy {
    background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
    border: none;
    color: #fff;

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .is-favorite {
    color: #ff4d4f;
    fill: #ff4d4f;
  }
}

.item-detail-empty {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}
</style>
