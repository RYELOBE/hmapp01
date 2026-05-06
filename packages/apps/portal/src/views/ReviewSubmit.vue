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
          <h2 class="page-title">订单评价</h2>
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

        <!-- 评价表单 -->
        <a-form
          ref="formRef"
          :model="form"
          layout="vertical"
          class="review-form"
        >
          <!-- 总体评分 -->
          <a-card title="总体评分" class="form-section">
            <div class="rating-wrapper">
              <a-rate
                v-model="form.rating"
                :count="5"
                allow-half
                :stroke-width="28"
              />
              <span class="rating-text" :class="`rating-text--${getRatingLevel(form.rating)}`">
                {{ getRatingText(form.rating) }}
              </span>
            </div>
          </a-card>

          <!-- 分类评分（可选） -->
          <a-card title="详细评价（可选）" class="form-section">
            <div class="category-ratings">
              <div class="category-item">
                <label>商品质量</label>
                <a-rate v-model="form.qualityRating" :count="5" allow-half size="small" />
              </div>
              <div class="category-item">
                <label>描述相符</label>
                <a-rate v-model="form.descriptionRating" :count="5" allow-half size="small" />
              </div>
              <div class="category-item">
                <label>物流服务</label>
                <a-rate v-model="form.logisticsRating" :count="5" allow-half size="small" />
              </div>
            </div>
          </a-card>

          <!-- 评价内容 -->
          <a-card title="评价内容" class="form-section">
            <a-form-item field="content" hide-label>
              <a-textarea
                v-model="form.content"
                placeholder="分享您的使用体验..."
                :max-length="500"
                show-word-limit
                :rows="6"
                allow-clear
              />
            </a-form-item>
          </a-card>

          <!-- 匿名评价 & 图片上传 -->
          <a-card title="其他选项" class="form-section">
            <div class="options-row">
              <div class="option-item">
                <a-switch v-model="form.anonymous" size="small" />
                <span class="option-label">匿名评价</span>
              </div>
            </div>

            <div class="upload-section">
              <label class="upload-label">晒图（最多3张）</label>
              <a-upload
                action="/api/upload"
                list-type="picture-card"
                :file-list="form.images"
                :limit="3"
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
        </a-form>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <a-button size="large" @click="$router.back()">
            返回
          </a-button>
          <a-button type="primary" size="large" :loading="submitting" @click="submitReview">
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
import { getOrderDetail, createReview } from '../../services/api';

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const formRef = ref(null);
const order = ref(null);

const form = ref({
  rating: 5,
  qualityRating: 0,
  descriptionRating: 0,
  logisticsRating: 0,
  content: '',
  anonymous: false,
  images: [],
});

function getRatingText(rating) {
  const texts = ['非常差', '差', '一般', '好', '非常好'];
  const index = Math.floor(rating) - 1;
  return texts[index] || '';
}

function getRatingLevel(rating) {
  if (rating <= 1) return 'bad';
  if (rating <= 2) return 'poor';
  if (rating <= 3) return 'normal';
  if (rating <= 4) return 'good';
  return 'excellent';
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
  const orderId = route.params.orderId;
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
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    Message.error('只能上传图片文件');
    return false;
  }
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB');
    return false;
  }
  return true;
}

async function submitReview() {
  if (form.value.rating === 0) {
    Message.warning('请选择商品评分');
    return;
  }

  submitting.value = true;
  try {
    await createReview({
      orderId: order.value.id,
      itemId: order.value.itemId,
      rating: Math.floor(form.value.rating),
      qualityRating: form.value.qualityRating || undefined,
      descriptionRating: form.value.descriptionRating || undefined,
      logisticsRating: form.value.logisticsRating || undefined,
      content: form.value.content,
      anonymous: form.value.anonymous,
      images: Array.isArray(form.value.images)
        ? form.value.images.map(img => img.url || img.response?.url).filter(Boolean)
        : [],
    });
    Message.success('评价提交成功');
    router.push('/orders');
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
  max-width: 600px;
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
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--color-fill-2, #E5E6EB);

  &--empty {
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text-4, #C9CDD4);
    font-size: 28px;
  }
}

.item-details {
  flex: 1;
  min-width: 0;

  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--color-text-1, #1D2129);
    margin: 0 0 10px 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
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

.review-form {
  padding: 0;
}

.form-section {
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
  gap: 20px;
}

.rating-text {
  font-size: 15px;
  font-weight: 500;

  &--bad { color: #F53F3F; }
  &--poor { color: #FF7D00; }
  &--normal { color: #FF7D00; }
  &--good { color: #00B42A; }
  &--excellent { color: #00B42A; }
}

.category-ratings {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed var(--color-border-2, #E5E6EB);

  &:last-child {
    border-bottom: none;
  }

  label {
    font-size: 14px;
    color: var(--color-text-2, #4E5969);
    font-weight: 500;
  }
}

.options-row {
  margin-bottom: 20px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-label {
  font-size: 14px;
  color: var(--color-text-2, #4E5969);
}

.upload-section {
  .upload-label {
    display: block;
    font-size: 14px;
    color: var(--color-text-2, #4E5969);
    margin-bottom: 12px;
    font-weight: 500;
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
