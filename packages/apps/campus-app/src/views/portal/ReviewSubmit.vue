<template>
  <div class="review-submit-page">
    <a-spin :loading="loading" class="review-container">
      <div v-if="order" class="review-card">
        <!-- 页面头部 -->
        <div class="page-header">
          <a-button @click="$router.back()" type="text" size="small" class="back-btn">
            <template #icon><icon-arrow-left /></template>
            返回
          </a-button>
          <h2 class="page-title">发表评价</h2>
        </div>

        <!-- 商品信息展示区 -->
        <a-card title="商品信息" class="item-info-card">
          <div class="item-info-content">
            <img
              v-if="order.itemImage"
              :src="order.itemImage"
              class="item-image"
              alt="商品图片"
            />
            <div v-else class="item-image item-image--empty">
              <icon-camera />
            </div>
            <div class="item-details">
              <h3 class="item-title">{{ order.itemTitle }}</h3>
              <div class="price-row">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ formatPrice(order.itemPrice) }}</span>
              </div>
              <div class="order-meta">
                <span class="meta-item">
                  <icon-calendar /> 订单号：{{ order.orderNo || order.id }}
                </span>
                <span class="meta-item">
                  <icon-clock-circle /> {{ formatTime(order.createdAt) }}
                </span>
              </div>
            </div>
          </div>
        </a-card>

        <!-- 评分选择组件 -->
        <a-card title="总体评分" class="rating-card">
          <div class="rating-wrapper">
            <a-rate
              v-model="form.rating"
              :count="5"
              :stroke-width="32"
              @change="handleRatingChange"
            />
            <div class="rating-feedback">
              <span class="rating-score">{{ form.rating }} 分</span>
              <span class="rating-text" :class="`rating-text--${getRatingLevel(form.rating)}`">
                {{ getRatingText(form.rating) }}
              </span>
            </div>
          </div>
          <div class="rating-tips">
            {{ getRatingTip(form.rating) }}
          </div>
        </a-card>

        <!-- 富文本编辑器 - 评价内容 -->
        <a-card title="评价内容" class="content-card">
          <RichEditor
            v-model="form.content"
            placeholder="分享您的购买体验..."
            :height="300"
          />
        </a-card>

        <!-- 图片上传 -->
        <a-card title="晒图评价" class="image-card">
          <div class="upload-section">
            <p class="upload-tip">支持上传最多5张图片，单张不超过5MB</p>
            <a-upload
              action="/api/upload"
              list-type="picture-card"
              :file-list="form.images"
              :limit="5"
              image-preview
              @change="handleImageChange"
              @before-upload="beforeUpload"
            >
              <template #upload-button>
                <div class="upload-trigger">
                  <icon-plus :size="24" />
                  <div style="margin-top: 8px; font-size: 12px;">上传图片</div>
                </div>
              </template>
            </a-upload>
          </div>
        </a-card>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <a-button size="large" @click="$router.back()">
            返回订单
          </a-button>
          <a-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="!canSubmit"
            @click="submitReview"
          >
            提交评价
          </a-button>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-if="!loading && !order" description="未找到该订单信息" />
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconArrowLeft,
  IconCamera,
  IconCalendar,
  IconClockCircle,
  IconPlus,
} from '@arco-design/web-vue/es/icon';
import RichEditor from '../../components/form/RichEditor/RichEditor.vue';
import { getOrderDetail, createReview } from '../../services/api';

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const order = ref(null);

const form = ref({
  rating: 0,
  content: '',
  images: [],
});

const canSubmit = computed(() => {
  return form.value.rating > 0 && form.value.content.trim().length > 0;
});

function getRatingText(rating) {
  const texts = {
    1: '很差',
    2: '较差',
    3: '一般',
    4: '很好',
    5: '非常好',
  };
  return texts[rating] || '';
}

function getRatingLevel(rating) {
  if (rating <= 1) return 'bad';
  if (rating <= 2) return 'poor';
  if (rating <= 3) return 'normal';
  if (rating <= 4) return 'good';
  return 'excellent';
}

function getRatingTip(rating) {
  const tips = {
    1: '非常不满意，商品存在严重问题',
    2: '不太满意，与预期有较大差距',
    3: '一般般，符合基本要求',
    4: '比较满意，超出预期',
    5: '非常满意，强烈推荐！',
  };
  return tips[rating] || '';
}

function handleRatingChange(value) {
  form.value.rating = value;
}

function formatPrice(price) {
  if (!price && price !== 0) return '0.00';
  return Number(price).toFixed(2);
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

async function loadOrder() {
  const orderId = route.query.orderId;
  if (!orderId) {
    Message.error('订单ID不存在');
    router.back();
    return;
  }

  loading.value = true;
  try {
    const res = await getOrderDetail(orderId);
    order.value = res;
  } catch (e) {
    Message.error(e.message || '加载订单失败');
  } finally {
    loading.value = false;
  }
}

function handleImageChange(fileList) {
  form.value.images = fileList;
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  if (!isLt5M) {
    Message.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
}

async function submitReview() {
  if (!canSubmit.value) {
    Message.warning('请完成评分并填写评价内容');
    return;
  }

  submitting.value = true;
  try {
    const images = Array.isArray(form.value.images)
      ? form.value.images.map(img => img.url || img.response?.url).filter(Boolean)
      : [];
    await createReview(order.value.id, {
      itemId: order.value.itemId,
      rating: form.value.rating,
      content: form.value.content,
      images: images.length > 0 ? images : [],
    });
    Message.success('评价提交成功！');
    router.push('/portal/orders');
  } catch (e) {
    Message.error(e.message || '提交失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadOrder);
</script>

<style lang="scss" scoped>
.review-submit-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
  padding: 20px;
}

.review-container {
  max-width: 700px;
  margin: 0 auto;
}

.review-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

  .back-btn {
    color: var(--color-text-2, #4E5969);
  }

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }
}

.item-info-card {
  border-radius: 0;
  box-shadow: none;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);
}

.item-info-content {
  display: flex;
  gap: 16px;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--color-fill-2, #E5E6EB);

  &--empty {
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text-4, #C9CDD4);
    font-size: 32px;
  }
}

.item-details {
  flex: 1;
  min-width: 0;

  .item-title {
    font-size: 16px;
    font-weight: 500;
    color: var(--color-text-1, #1D2129);
    margin: 0 0 8px 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .price-row {
    margin-bottom: 10px;

    .price-symbol {
      font-size: 16px;
      font-weight: 600;
      color: #F53F3F;
    }

    .price-value {
      font-size: 22px;
      font-weight: 700;
      color: #F53F3F;
    }
  }

  .order-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--color-text-3, #86909C);
  }
}

.rating-card,
.content-card,
.image-card {
  border-radius: 0;
  box-shadow: none;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

  :deep(.arco-card-header-title) {
    font-size: 15px;
    font-weight: 600;
  }
}

.rating-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 0;

  :deep(.arco-rate) {
    font-size: 28px;
  }
}

.rating-feedback {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-score {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
}

.rating-text {
  font-size: 14px;
  font-weight: 500;

  &--bad { color: #F53F3F; }
  &--poor { color: #FF7D00; }
  &--normal { color: #FF7D00; }
  &--good { color: #00B42A; }
  &--excellent { color: #00B42A; }
}

.rating-tips {
  margin-top: 12px;
  padding: 10px 14px;
  background: var(--color-fill-1, #F7F8FA);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text-3, #86909C);
}

.upload-section {
  .upload-tip {
    font-size: 13px;
    color: var(--color-text-3, #86909C);
    margin-bottom: 12px;
  }
}

.upload-trigger {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-3, #86909C);
}

.action-bar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px;
  background: var(--color-bg-2, #F7F8FA);
  border-top: 1px solid var(--color-border-1, #E5E6EB);

  button {
    flex: 1;
  }
}
</style>
