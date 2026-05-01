<template>
  <div class="item-detail-page">
    <div class="item-detail-container">
      <!-- 商品信息 -->
      <div v-if="detail" class="item-detail">
        <!-- 图片轮播 -->
        <div class="item-gallery">
          <div class="item-main-image">
            <img :src="detail.imageUrls?.[0] || 'https://via.placeholder.com/400x400?text=商品图片'" :alt="detail.title" />
          </div>
          <div class="item-thumbs" v-if="detail.imageUrls?.length > 1">
            <div
              v-for="(img, index) in detail.imageUrls"
              :key="index"
              class="item-thumb"
              :class="{ active: activeImageIndex === index }"
              @click="activeImageIndex = index"
            >
              <img :src="img" :alt="`图片${index + 1}`" />
            </div>
          </div>
        </div>

        <!-- 商品详情 -->
        <div class="item-info">
          <div class="item-status-badge" :class="detail.reviewStatus">
            {{ getStatusText(detail.reviewStatus) }}
          </div>
          
          <h1 class="item-title">{{ detail.title }}</h1>
          
          <div class="item-price">
            <span class="price-current">¥{{ detail.price }}</span>
            <span v-if="detail.originalPrice" class="price-original">¥{{ detail.originalPrice }}</span>
          </div>

          <div class="item-meta">
            <div class="meta-item">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>发布于 {{ formatDate(detail.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
              <span>{{ detail.campus || '未知校区' }}</span>
            </div>
            <div v-if="detail.category" class="meta-item">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
              </svg>
              <span>{{ detail.category }}</span>
            </div>
            <div v-if="detail.conditionLevel" class="meta-item">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
              <span>{{ detail.conditionLevel }}</span>
            </div>
          </div>

          <div class="item-description">
            <h3>商品描述</h3>
            <p>{{ detail.description }}</p>
          </div>

          <!-- 卖家信息 -->
          <div class="seller-info">
            <div class="seller-avatar">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="seller-details">
              <div class="seller-name">{{ detail.sellerName }}</div>
              <div class="seller-label">卖家</div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="item-actions">
            <button class="btn btn-outline" @click="goBack">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 12H5M12 19l-7-7 7-7"></path>
              </svg>
              返回
            </button>
            <button class="btn btn-primary" @click="handleBuy" :disabled="detail.reviewStatus !== 'APPROVED'">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
              </svg>
              立即购买
            </button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-else-if="loading" class="item-detail-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else class="item-detail-empty">
        <svg viewBox="0 0 24 24" width="80" height="80" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
          <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
          <line x1="12" y1="22.08" x2="12" y2="12"></line>
        </svg>
        <h3>商品不存在</h3>
        <p>该商品可能已被删除或不存在</p>
        <button class="btn btn-primary" @click="goBack">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getItemDetail } from '../services/api';

const route = useRoute();
const router = useRouter();
const detail = ref(null);
const loading = ref(true);
const activeImageIndex = ref(0);

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'APPROVED': '已上架',
    'REJECTED': '已驳回'
  };
  return statusMap[status] || status;
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

// 返回上一页
function goBack() {
  router.back();
}

// 购买
function handleBuy() {
  Message.info('购买功能开发中...');
}

// 加载详情
async function loadDetail() {
  loading.value = true;
  try {
    const data = await getItemDetail(route.params.id);
    // 处理图片数据
    if (data.imageUrls && typeof data.imageUrls === 'string') {
      try {
        data.imageUrls = JSON.parse(data.imageUrls);
      } catch {
        data.imageUrls = [data.imageUrls];
      }
    }
    detail.value = data;
  } catch (error) {
    console.error('加载详情失败:', error);
    Message.error(error.message || '加载详情失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped>
.item-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8ec 100%);
  padding: 40px 20px;
}

.item-detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.item-detail {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 40px;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

/* 图片区域 */
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
}

.item-main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-thumbs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.item-thumb {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.item-thumb:hover {
  border-color: #667eea;
}

.item-thumb.active {
  border-color: #667eea;
}

.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息 */
.item-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.item-status-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  width: fit-content;
}

.item-status-badge.PENDING_REVIEW {
  background: #fff7e6;
  color: #fa8c16;
}

.item-status-badge.APPROVED {
  background: #f6ffed;
  color: #52c41a;
}

.item-status-badge.REJECTED {
  background: #fff1f0;
  color: #ff4d4f;
}

.item-title {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
  margin: 0;
}

.item-price {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-current {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
}

.price-original {
  font-size: 18px;
  color: #adb5bd;
  text-decoration: line-through;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 0;
  border-top: 1px solid #e9ecef;
  border-bottom: 1px solid #e9ecef;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 14px;
}

/* 商品描述 */
.item-description h3 {
  font-size: 16px;
  font-weight: 600;
  color: #212529;
  margin: 0 0 12px;
}

.item-description p {
  color: #495057;
  line-height: 1.7;
  margin: 0;
}

/* 卖家信息 */
.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
}

.seller-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seller-details {
  flex: 1;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: #212529;
}

.seller-label {
  font-size: 12px;
  color: #adb5bd;
  margin-top: 2px;
}

/* 操作按钮 */
.item-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.btn {
  flex: 1;
  padding: 14px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  border: none;
}

.btn-outline {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #f8f9ff;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 加载和空状态 */
.item-detail-loading,
.item-detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e9ecef;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.item-detail-loading p,
.item-detail-empty p {
  color: #6c757d;
  margin-top: 16px;
}

.item-detail-empty h3 {
  color: #212529;
  margin: 20px 0 8px;
  font-size: 20px;
}

.item-detail-empty svg {
  color: #adb5bd;
}

@media (max-width: 900px) {
  .item-detail {
    grid-template-columns: 1fr;
  }
  
  .item-gallery {
    max-width: 400px;
    margin: 0 auto;
  }
}
</style>
