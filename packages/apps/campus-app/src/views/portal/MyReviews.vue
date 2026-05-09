<template>
  <div class="my-reviews-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">我的评价</h2>
      <span class="review-count">共 {{ total }} 条评价</span>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <!-- 筛选栏 -->
      <div v-if="reviews.length > 0" class="filter-bar">
        <a-radio-group v-model:value="activeStatus" type="button" @change="handleStatusChange">
          <a-radio value="">全部</a-radio>
          <a-radio value="PENDING">待审核</a-radio>
          <a-radio value="APPROVED">已通过</a-radio>
          <a-radio value="REJECTED">已拒绝</a-radio>
        </a-radio-group>
      </div>

      <!-- 评价列表 -->
      <div v-if="reviews.length > 0" class="review-list">
        <div
          v-for="(review, index) in reviews"
          :key="review.id"
          class="review-card"
          :class="{ 'review-card--pending': review.status === 'PENDING' }"
        >
          <div class="review-header">
            <div class="review-item-info">
              <img
                v-if="getItemImage(review)"
                :src="getItemImage(review)"
                class="item-image"
              />
              <div v-else class="item-image item-image--empty">📷</div>
              
              <div class="item-detail">
                <h4 class="item-title">{{ review.itemTitle || '未知商品' }}</h4>
                <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              </div>
            </div>

            <div class="review-status">
              <a-tag 
                :color="getStatusColor(review.status)"
                size="small"
              >
                {{ getStatusText(review.status) }}
              </a-tag>
            </div>
          </div>

          <div class="review-rating">
            <a-rate 
              :model-value="review.rating || 5" 
              disabled 
              allow-half
            />
            <span class="rating-text">{{ review.rating }}.0 分</span>
          </div>

          <div class="review-content">
            {{ review.content || '用户未填写评价内容' }}
          </div>

          <div v-if="review.images && parseImages(review.images).length > 0" class="review-images">
            <div
              v-for="(image, idx) in parseImages(review.images)"
              :key="idx"
              class="review-image-wrapper"
              @click="previewImage(image)"
            >
              <img :src="image" class="review-image" />
            </div>
          </div>

          <div v-if="review.replyContent" class="review-reply">
            <div class="reply-header">
              <icon-message />
              <span>卖家回复</span>
            </div>
            <p>{{ review.replyContent }}</p>
          </div>

          <div class="review-actions">
            <a-button 
              v-if="review.status === 'PENDING'"
              type="text" 
              size="small"
              status="warning"
              disabled
            >
              待审核中...
            </a-button>
            <a-button 
              v-if="review.status === 'REJECTED' && review.rejectReason"
              type="text" 
              size="small"
              status="danger"
            >
              拒绝原因：{{ review.rejectReason }}
            </a-button>
          </div>

          <a-divider v-if="index < reviews.length - 1" />
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="暂无评价记录">
        <template #image>
          <icon-star-fill size="64" color="#c9cdd4" />
        </template>
        <template #extra>
          <a-button type="primary" @click="$router.push('/portal/home')">
            去逛逛
          </a-button>
        </template>
      </a-empty>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-wrapper">
        <a-pagination
          :current="currentPage"
          :total="total"
          :page-size="pageSize"
          show-total
          show-jumper
          show-page-size
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </a-spin>

    <!-- 图片预览 -->
    <a-image-preview-group v-model:visible="previewVisible" :current="previewIndex">
      <a-image
        v-for="(url, idx) in previewImages"
        :key="idx"
        :src="url"
        style="display: none"
      />
    </a-image-preview-group>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconStarFill,
  IconMessage,
} from "@arco-design/web-vue/es/icon";
import { http } from "../../services/http";
import { parseFirstImageUrl } from "../../utils/image-utils";

const loading = ref(false);
const reviews = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const activeStatus = ref("");

const previewVisible = ref(false);
const previewImages = ref([]);
const previewIndex = ref(0);

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      const parsed = JSON.parse(urls);
      return Array.isArray(parsed) ? parsed[0] : urls;
    } catch {
      return urls;
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : null;
}

function getItemImage(review) {
  if (review.item?.imageUrls) {
    return parseFirstImageUrl(review.item.imageUrls);
  }
  if (review.imageUrl) {
    return review.imageUrl.startsWith('data:') ? null : review.imageUrl;
  }
  if (review.itemImage) {
    return review.itemImage;
  }
  return null;
}

function parseImages(imagesStr) {
  if (!imagesStr) return [];
  
  if (Array.isArray(imagesStr)) {
    return imagesStr.filter(url => url && !url.startsWith('data:'));
  }
  
  try {
    const parsed = JSON.parse(imagesStr);
    if (Array.isArray(parsed)) {
      return parsed.filter(url => url && !url.startsWith('data:'));
    }
    return [imagesStr].filter(url => url && !url.startsWith('data:'));
  } catch {
    return [];
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    
    // 小于1分钟
    if (diff < 60000) return '刚刚';
    
    // 小于1小时
    if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`;
    
    // 小于24小时
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`;
    
    // 小于7天
    if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`;
    
    // 其他情况显示完整日期
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}

function getStatusColor(status) {
  switch (status) {
    case 'PENDING': return 'orange';
    case 'APPROVED': return 'green';
    case 'REJECTED': return 'red';
    default: return 'gray';
  }
}

function getStatusText(status) {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已通过';
    case 'REJECTED': return '已拒绝';
    default: return '未知';
  }
}

async function loadReviews() {
  loading.value = true;
  
  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
    };
    
    if (activeStatus.value) {
      params.status = activeStatus.value;
    }
    
    const result = await http.get('/reviews/my', params);
    
    if (result.data) {
      reviews.value = Array.isArray(result.data.records) ? result.data.records : (Array.isArray(result.data) ? result.data : []);
      total.value = result.data.total || reviews.value.length;
    } else {
      reviews.value = Array.isArray(result) ? result : [];
      total.value = reviews.value.length;
    }
  } catch (e) {
    console.error('[MyReviews] 加载失败:', e);
    Message.error(e.message || '加载评价失败');
    reviews.value = [];
  } finally {
    loading.value = false;
  }
}

function handleStatusChange(value) {
  activeStatus.value = value;
  currentPage.value = 1;
  loadReviews();
}

function handlePageChange(page) {
  currentPage.value = page;
  loadReviews();
}

function handlePageSizeChange(size) {
  pageSize.value = size;
  currentPage.value = 1;
  loadReviews();
}

function previewImage(imageUrl) {
  previewImages.value = [imageUrl];
  previewIndex.value = 0;
  previewVisible.value = true;
}

onMounted(() => {
  loadReviews();
});
</script>

<style lang="scss" scoped>
.my-reviews-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  .page-title {
    font-size: 22px;
    font-weight: 700;
    color: var(--color-text-1, #1d2129);
    margin: 0;
    flex: 1;
  }

  .review-count {
    color: var(--color-text-3, #86909C);
    font-size: 14px;
  }
}

.filter-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  padding: 12px;
  background: var(--color-bg-2, #f7f8fa);
  border-radius: 8px;
}

.review-list {
  .review-card {
    padding: 20px;
    background: var(--color-bg-white, #fff);
    border-radius: 12px;
    border: 1px solid var(--color-border, #e5e6eb);
    transition: all 200ms ease-out;
    margin-bottom: 16px;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    &--pending {
      border-left: 4px solid #ff7d00;
      background: linear-gradient(135deg, #fff9f0 0%, #fff 100%);
    }
  }
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.review-item-info {
  display: flex;
  gap: 14px;
  flex: 1;

  .item-image {
    width: 72px;
    height: 72px;
    object-fit: cover;
    border-radius: 10px;
    border: 1px solid var(--color-border-2, #e5e6eb);
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--color-fill-1, #f7f8fa);
      font-size: 28px;
    }
  }

  .item-detail {
    flex: 1;
    min-width: 0;

    .item-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--color-text-1, #1d2129);
      margin: 0 0 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .review-date {
      font-size: 13px;
      color: var(--color-text-4, #c9cdd4);
    }
  }
}

.review-status {
  flex-shrink: 0;
  margin-left: 12px;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .rating-text {
    font-size: 13px;
    color: var(--color-text-3, #86909C);
    font-weight: 500;
  }
}

.review-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-2, #4e5969);
  margin-bottom: 14px;
  word-break: break-word;
}

.review-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;

  .review-image-wrapper {
    cursor: pointer;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid var(--color-border-2, #e5e6eb);
    transition: all 150ms ease-out;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    }
  }

  .review-image {
    width: 96px;
    height: 96px;
    object-fit: cover;
  }
}

.review-reply {
  background: var(--color-fill-1, #f7f8fa);
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 12px;

  .reply-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: var(--color-text-2, #4e5969);
    margin-bottom: 6px;
  }

  p {
    font-size: 13px;
    line-height: 1.6;
    color: var(--color-text-3, #86909C);
    margin: 0;
  }
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-1, #f2f3f5);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .my-reviews-page {
    padding: 12px;
  }

  .page-header {
    flex-wrap: wrap;
    gap: 8px;
  }

  .review-card {
    padding: 14px;
  }

  .review-item-info {
    .item-image {
      width: 56px;
      height: 56px;
    }

    .item-detail .item-title {
      font-size: 14px;
    }
  }

  .review-images .review-image {
    width: 76px;
    height: 76px;
  }
}
</style>
