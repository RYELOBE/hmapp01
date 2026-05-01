<template>
  <div class="review-submit-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">商品评价</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="order" class="review-content">
        <!-- 商品信息 -->
        <div class="item-card">
          <img v-if="order.itemImage" :src="order.itemImage" class="item-image" />
          <div v-else class="item-image item-image--empty">📷</div>
          <div class="item-info">
            <div class="item-title">{{ order.itemTitle }}</div>
            <div class="item-price">¥{{ order.totalAmount }}</div>
          </div>
        </div>

        <!-- 评价表单 -->
        <div class="review-form">
          <div class="form-item">
            <label class="form-label">商品评分</label>
            <div class="rating-stars">
              <a-rate
                v-model="form.rating"
                :count="5"
                :stroke-width="20"
                allow-half
              />
              <span class="rating-text">{{ ratingText }}</span>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">评价内容</label>
            <a-textarea
              v-model="form.content"
              placeholder="分享您的购物体验，帮助其他同学更好地了解这件商品..."
              :max-length="500"
              show-word-limit
              :rows="5"
            />
          </div>

          <div class="form-item">
            <label class="form-label">上传图片（可选）</label>
            <ImageUploader v-model="form.images" :limit="9" />
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <a-button type="primary" size="large" :loading="submitting" @click="submitReview">
            提交评价
          </a-button>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconArrowLeft } from "@arco-design/web-vue/es/icon";
import ImageUploader from "commonprovide/ImageUploader";
import { getOrderDetail, createReview } from "../../services/api";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const order = ref(null);

const form = ref({
  rating: 5,
  content: "",
  images: [],
});

const ratingText = computed(() => {
  const texts = ["很差", "较差", "一般", "满意", "非常满意"];
  return texts[Math.floor(form.value.rating) - 1] || "";
});

async function loadOrder() {
  const orderId = route.params.orderId;
  if (!orderId) {
    Message.error("订单ID不存在");
    router.back();
    return;
  }

  loading.value = true;
  try {
    const res = await getOrderDetail(orderId);
    order.value = res;
  } catch (e) {
    Message.error(e.message || "加载订单失败");
  } finally {
    loading.value = false;
  }
}

async function submitReview() {
  if (form.value.rating === 0) {
    Message.error("请选择商品评分");
    return;
  }

  submitting.value = true;
  try {
    await createReview({
      orderId: order.value.id,
      itemId: order.value.itemId,
      rating: form.value.rating,
      content: form.value.content,
      images: Array.isArray(form.value.images) ? form.value.images.join(",") : form.value.images,
    });
    Message.success("评价提交成功");
    router.push("/orders");
  } catch (e) {
    Message.error(e.message || "提交失败");
  } finally {
    submitting.value = false;
  }
}

onMounted(loadOrder);
</script>

<style lang="scss" scoped>
.review-submit-page {
  padding: 24px;
  max-width: 700px;
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
}

.review-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.item-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;

  .item-image {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e5e6eb;
      color: #86909c;
      font-size: 32px;
    }
  }

  .item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .item-title {
      font-size: 16px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 8px;
    }

    .item-price {
      font-size: 18px;
      font-weight: 700;
      color: #f53f3f;
    }
  }
}

.review-form {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;

  .form-label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: 10px;
  }
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 16px;

  .rating-text {
    font-size: 14px;
    color: #f59e0b;
  }
}

.submit-section {
  :deep(.arco-btn-primary) {
    width: 100%;
    height: 48px;
    font-size: 16px;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    border: none;
  }
}

:deep(.arco-rate) {
  font-size: 28px;
}
</style>
