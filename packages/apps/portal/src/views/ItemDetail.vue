<template>
  <div class="item-detail-page">
    <a-spin :loading="loading" class="detail-spin">
      <template v-if="detail">
        <div class="detail-layout">
          <!-- 左侧区域 (70%) -->
          <div class="detail-left">
            <!-- 商品图片画廊 -->
            <div class="gallery-section">
              <ImageGallery :images="detail.imageUrls || []" />
            </div>

            <!-- 商品基本信息卡片 -->
            <a-card :bordered="false" class="info-card">
              <div class="info-header">
                <StatusTag :status="detail.reviewStatus" />
                <a-tag v-if="detail.category" color="arcoblue">{{ detail.category }}</a-tag>
              </div>

              <h1 class="item-title" :title="detail.title">{{ truncateTitle(detail.title) }}</h1>

              <div class="price-section">
                <div class="current-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ formatPrice(detail.price) }}</span>
                </div>
                <span v-if="detail.originalPrice && detail.originalPrice > detail.price" class="original-price">
                  ¥{{ formatPrice(detail.originalPrice) }}
                </span>
              </div>

              <div class="condition-section">
                <ConditionTag :condition="detail.conditionLevel || 'NEW'" />
              </div>

              <a-divider />

              <div class="meta-info">
                <div class="meta-item">
                  <icon-clock-circle />
                  <span>发布于 {{ formatDate(detail.createdAt) }}</span>
                </div>
                <div class="meta-item">
                  <icon-eye />
                  <span>{{ detail.viewCount || 0 }} 次浏览</span>
                </div>
                <div class="meta-item clickable" @click="handleFavorite">
                  <icon-heart :class="{ 'is-favorite': isFavorited }" />
                  <span>{{ favoriteCount }} 次收藏</span>
                </div>
              </div>
            </a-card>

            <!-- 商品详细描述区 -->
            <a-card :bordered="false" class="description-card">
              <a-typography-title :heading="5">商品详细描述</a-typography-title>
              <div class="description-content" v-html="detail.description || '<p style=\'color: #86909c\'>暂无描述</p>'"></div>
            </a-card>
          </div>

          <!-- 右侧区域 (30%, sticky) -->
          <div class="detail-right">
            <div class="sidebar-sticky">
              <!-- 卖家信息卡片 -->
              <a-card title="卖家信息" class="seller-card">
                <div class="seller-content">
                  <a-avatar :size="48" class="seller-avatar">
                    {{ (detail.sellerName || '卖')[0] }}
                  </a-avatar>
                  <div class="seller-details">
                    <div class="seller-name">{{ detail.sellerName || '未知卖家' }}</div>
                    <div class="seller-rate">
                      <a-rate :model-value="detail.sellerRating || 5" readonly :count="5" allow-half />
                      <span class="rate-text">{{ detail.sellerRating?.toFixed(1) || '5.0' }} 分</span>
                    </div>
                  </div>
                </div>

                <a-divider />

                <div class="seller-stats">
                  <div class="stat-item">
                    <span class="stat-label">注册时间</span>
                    <span class="stat-value">{{ formatDate(detail.sellerCreatedAt) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">累计售出</span>
                    <span class="stat-value highlight">{{ detail.sellerSoldCount || 0 }} 件</span>
                  </div>
                </div>

                <a-button type="outline" long class="view-shop-btn" @click="handleViewShop">
                  <template #icon><icon-store /></template>
                  查看店铺
                </a-button>
              </a-card>

              <!-- 操作按钮组 -->
              <div class="action-group">
                <a-button
                  type="primary"
                  size="large"
                  long
                  class="buy-btn"
                  :disabled="detail.reviewStatus !== 'APPROVED'"
                  @click="handleBuy"
                >
                  <template #icon><icon-shopping-cart /></template>
                  立即购买
                </a-button>

                <a-button size="large" long class="cart-btn" @click="handleAddToCart">
                  <template #icon><icon-plus /></template>
                  加入购物车
                </a-button>

                <div class="action-row">
                  <a-button
                    shape="circle"
                    :class="['fav-btn', { 'is-favorited': isFavorited }]"
                    @click="handleFavorite"
                  >
                    <template #icon><icon-heart-fill /></template>
                  </a-button>
                  <a-button shape="circle" class="contact-btn" @click="handleContactSeller">
                    <template #icon><icon-message /></template>
                  </a-button>
                </div>
              </div>

              <!-- AI问答入口 -->
              <div class="ai-entry" @click="handleAIChat">
                <icon-robot class="ai-icon" />
                <span>有疑问？问问 AI 助手</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <a-result
        v-else-if="!loading"
        status="warning"
        title="商品不存在或已被删除"
        subtitle="该商品可能已下架或被删除"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/home')">返回首页</a-button>
        </template>
      </a-result>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconHeart,
  IconEye,
  IconClockCircle,
  IconShoppingCart,
  IconPlus,
  IconStore,
  IconMessage,
  IconRobot,
  IconHeartFill,
} from '@arco-design/web-vue/es/icon';
import StatusTag from "commonprovide/status-tag";
import ImageGallery from "commonprovide/ImageGallery";
import ConditionTag from "../components/sub/ConditionTag.vue";
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

function truncateTitle(title) {
  if (!title) return '';
  return title.length > 30 ? title.substring(0, 30) + '...' : title;
}

function formatPrice(price) {
  if (!price && price !== 0) return '0.00';
  return Number(price).toFixed(2);
}

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

function handleViewShop() {
  Message.info('店铺功能开发中');
}

function handleContactSeller() {
  Message.info('联系卖家功能开发中');
}

function handleAIChat() {
  Message.info('AI 助手功能开发中');
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
  background: #F5F6F7;
  padding: 24px;
}

.detail-spin {
  width: 100%;
  min-height: 60vh;
}

.detail-layout {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 70% 30%;
  gap: 20px;
  align-items: start;

  @media (max-width: 992px) {
    grid-template-columns: 1fr;
  }
}

.detail-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.gallery-section {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-body) {
    padding: 24px;
  }
}

.info-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.item-title {
  font-size: 22px;
  font-weight: 600;
  color: #1D2129;
  line-height: 1.4;
  margin: 0 0 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;

  .current-price {
    .price-symbol {
      font-size: 18px;
      font-weight: 600;
      color: #F53F3F;
    }

    .price-value {
      font-size: 28px;
      font-weight: 700;
      color: #F53F3F;
    }
  }

  .original-price {
    font-size: 14px;
    color: #86909C;
    text-decoration: line-through;
  }
}

.condition-section {
  margin-bottom: 12px;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #86909C;

    &.clickable {
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: #165DFF;
      }

      .is-favorite {
        color: #F53F3F;
      }
    }
  }
}

.description-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.description-content {
  color: #4E5969;
  line-height: 1.8;
  word-wrap: break-word;

  img {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
  }
}

.detail-right {
  position: relative;
}

.sidebar-sticky {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.seller-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-head-title) {
    font-weight: 600;
  }

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.seller-content {
  display: flex;
  align-items: center;
  gap: 12px;

  .seller-avatar {
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .seller-details {
    flex: 1;
    min-width: 0;

    .seller-name {
      font-size: 15px;
      font-weight: 600;
      color: #1D2129;
      margin-bottom: 4px;
    }

    .seller-rate {
      display: flex;
      align-items: center;
      gap: 6px;

      .rate-text {
        font-size: 12px;
        color: #86909C;
      }
    }
  }
}

.seller-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-label {
      font-size: 13px;
      color: #86909C;
    }

    .stat-value {
      font-size: 13px;
      color: #4E5969;
      font-weight: 500;

      &.highlight {
        color: #165DFF;
        font-weight: 600;
      }
    }
  }
}

.view-shop-btn {
  margin-top: 12px;
}

.action-group {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .buy-btn {
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    border: none;

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .cart-btn {
    height: 44px;
    font-size: 15px;
    border-radius: 8px;
    border-color: #165DFF;
    color: #165DFF;
    margin-bottom: 12px;

    &:hover {
      background: rgba(22, 93, 255, 0.05);
    }
  }

  .action-row {
    display: flex;
    justify-content: center;
    gap: 16px;

    .fav-btn,
    .contact-btn {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      transition: all 0.2s;

      &.is-favorited {
        color: #F53F3F;
        border-color: #F53F3F;
        background: rgba(245, 63, 63, 0.05);
      }

      &:hover {
        transform: scale(1.05);
      }
    }
  }
}

.ai-entry {
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f3ff 100%);
  border: 1px solid #b8d2ff;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.25s ease-out;
  color: #165DFF;
  font-size: 14px;
  font-weight: 500;

  .ai-icon {
    font-size: 18px;
  }

  &:hover {
    background: linear-gradient(135deg, #e8f3ff 0%, #d6ebff 100%);
    border-color: #165DFF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
  }
}
</style>
