<template>
  <div class="item-detail-page">
    <!-- 页面头部 - 使用 PageHeader 组件 -->
    <PageHeader title="商品详情" @back="goBack">
      <template #extra>
        <a-breadcrumb>
          <a-breadcrumb-item>
            <a-link @click="$router.push('/portal/home')">首页</a-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>商品详情</a-breadcrumb-item>
        </a-breadcrumb>
      </template>
    </PageHeader>

    <a-spin :loading="loading" class="detail-spin">
      <template v-if="detail">
        <div class="detail-layout">
          <!-- 左侧区域 (70%) -->
          <div class="detail-left">
            <!-- 商品基本信息卡片 -->
            <a-card :bordered="false" class="info-card">
              <div class="gallery-section">
                <div class="gallery-wrapper">
                  <ImageGallery :images="detail.imageUrls || []" />
                </div>
                <div class="info-content">
                  <div class="tags-group">
                    <StatusTag :status="detail.reviewStatus" />
                    <a-tag
                      v-if="detail.category"
                      color="arcoblue"
                      class="category-tag"
                      >{{ detail.category }}</a-tag
                    >
                    <ConditionTag :condition="detail.conditionLevel || 'NEW'" />
                  </div>

                  <h1 class="item-title" :title="detail.title">
                    {{ truncateTitle(detail.title) }}
                  </h1>

                  <div class="price-section">
                    <div class="current-price">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{
                        formatPrice(detail.price)
                      }}</span>
                    </div>
                    <span
                      v-if="
                        detail.originalPrice && detail.originalPrice > detail.price
                      "
                      class="original-price"
                    >
                      ¥{{ formatPrice(detail.originalPrice) }}
                    </span>
                  </div>
                </div>
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
              <div
                class="description-content"
                v-html="
                  detail.description ||
                  '<p style=\'color: #86909c\'>暂无描述</p>'
                "
              ></div>
            </a-card>

            <!-- 用户评价区域 - 使用 a-comment 组件 -->
            <a-card :bordered="false" class="reviews-card">
              <div class="reviews-header">
                <a-typography-title :heading="5">用户评价</a-typography-title>
                <div class="reviews-header__actions">
                  <span class="reviews-count">{{ reviewsTotal }} 条评价</span>
                  <a-button type="primary" size="small" @click="handleShowReviewModal">
                    <template #icon><icon-edit /></template>
                    写评价
                  </a-button>
                </div>
              </div>

              <!-- 评价列表 - 使用 a-comment -->
              <div v-if="reviews.length > 0" class="reviews-list">
                <a-comment
                  v-for="review in reviews"
                  :key="review.id"
                  :author="review.userName || '匿名用户'"
                  :datetime="formatReviewTime(review.createdAt)"
                  align="right"
                >
                  <template #avatar>
                    <a-avatar :size="40" class="user-avatar">
                      {{ (review.userName || "用")[0] }}
                    </a-avatar>
                  </template>

                  <template #actions>
                    <a-rate
                      :model-value="review.rating"
                      readonly
                      size="small"
                      allow-half
                    />
                  </template>

                  <template #content>
                    <div class="review-content" v-html="review.content"></div>

                    <!-- 评价图片 -->
                    <div
                      v-if="review.images && review.images.length > 0"
                      class="review-images"
                    >
                      <img
                        v-for="(img, idx) in review.images"
                        :key="idx"
                        :src="img"
                        class="review-image"
                        alt="评价图片"
                      />
                    </div>

                    <!-- 卖家回复 - 嵌套评论 -->
                    <a-comment
                      v-if="review.reply"
                      author="卖家回复"
                      datetime=""
                    >
                      <template #content>
                        <div class="seller-reply">{{ review.reply }}</div>
                      </template>
                    </a-comment>
                  </template>
                </a-comment>

                <!-- 加载更多 -->
                <div v-if="hasMoreReviews" class="load-more-wrapper">
                  <a-button
                    type="outline"
                    long
                    @click="loadMoreReviews"
                    :loading="loadingReviews"
                  >
                    加载更多评价
                  </a-button>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-else class="empty-reviews">
                <a-empty description="暂无评价，快来发表第一条评价吧！" />
              </div>
            </a-card>
          </div>

          <!-- 右侧区域 (30%, sticky) -->
          <div class="detail-right">
            <div class="sidebar-sticky">
              <!-- 卖家信息卡片 -->
              <a-card title="卖家信息" class="seller-card">
                <div class="seller-profile">
                  <a-avatar :size="56" class="seller-avatar">
                    {{ (detail.sellerName || "卖")[0] }}
                  </a-avatar>
                  <div class="seller-basic">
                    <div class="seller-name">
                      {{ detail.sellerName || "未知卖家" }}
                    </div>
                    <div class="seller-rating">
                      <a-rate
                        :model-value="detail.sellerRating || 5"
                        readonly
                        :count="5"
                        allow-half
                        size="small"
                      />
                      <span class="rating-text"
                        >{{ formatRating(detail.sellerRating) }} 分</span
                      >
                    </div>
                  </div>
                </div>

                <a-divider style="margin: 16px 0" />

                <!-- 卖家详细信息 -->
                <div class="seller-details-grid">
                  <div class="detail-item" v-if="detail.sellerDescription">
                    <div class="detail-label">
                      <icon-info-circle />
                      <span>卖家描述</span>
                    </div>
                    <div class="detail-value">
                      {{ detail.sellerDescription }}
                    </div>
                  </div>

                  <div class="detail-item">
                    <div class="detail-label">
                      <icon-phone />
                      <span>联系电话</span>
                    </div>
                    <div class="detail-value phone">
                      {{ detail.sellerPhone || "未提供" }}
                    </div>
                  </div>

                  <div class="detail-item">
                    <div class="detail-label">
                      <icon-email />
                      <span>电子邮箱</span>
                    </div>
                    <div class="detail-value email">
                      {{ detail.sellerEmail || "未提供" }}
                    </div>
                  </div>

                  <div class="detail-item">
                    <div class="detail-label">
                      <icon-calendar />
                      <span>注册时间</span>
                    </div>
                    <div class="detail-value">
                      {{ formatDate(detail.sellerCreatedAt) }}
                    </div>
                  </div>

                  <div class="detail-item highlight">
                    <div class="detail-label">
                      <icon-apps />
                      <span>累计售出</span>
                    </div>
                    <div class="detail-value sold-count">
                      {{ detail.sellerSoldCount || 0 }} 件商品
                    </div>
                  </div>
                </div>
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
                  <template #icon><icon-apps /></template>
                  立即购买
                </a-button>

                <a-button
                  size="large"
                  long
                  :status="isInCart ? 'warning' : 'normal'"
                  :class="['cart-btn', { 'is-in-cart': isInCart }]"
                  :loading="cartLoading"
                  @click="handleAddToCart"
                >
                  <template #icon><icon-plus /></template>
                  {{ isInCart ? '取消加入购物车' : '加入购物车' }}
                </a-button>

                <div class="action-row">
                  <a-button
                    shape="circle"
                    :class="['fav-btn', { 'is-favorited': isFavorited }]"
                    @click="handleFavorite"
                  >
                    <template #icon><icon-heart-fill /></template>
                  </a-button>
                </div>
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
          <a-button type="primary" @click="$router.push('/portal/home')"
            >返回首页</a-button
          >
        </template>
      </a-result>
    </a-spin>

    <!-- 登录提示弹窗 - 复用 LoginPromptModal -->
    <LoginPromptModal
      v-model:visible="showLoginPrompt"
      title="购买需要登录"
      description="请先登录后再进行购买操作，享受更便捷的购物体验"
      @login="handleLoginRedirect"
    />

    <!-- 发表评价弹窗 - 使用 a-form + ImageUploader -->
    <a-modal
      v-model:visible="showReviewModal"
      title="发表评价"
      :footer="false"
      :width="560"
      :centered="true"
      class="review-modal"
    >
      <a-form
        :model="reviewForm"
        layout="vertical"
        @submit-success="submitReview"
      >
        <!-- 评分字段 -->
        <a-form-item
          field="rating"
          label="评分"
          :rules="[{ required: true, message: '请选择评分' }]"
        >
          <a-rate v-model="reviewForm.rating" :count="5" allow-half />
        </a-form-item>

        <!-- 评价内容字段 -->
        <a-form-item
          field="content"
          label="评价内容"
          :rules="[
            { required: true, message: '请输入评价内容' },
            { maxLength: 500, message: '评价内容不能超过500字' },
          ]"
        >
          <a-textarea
            v-model="reviewForm.content"
            placeholder="分享您的使用体验..."
            :max-length="500"
            :auto-size="{ minRows: 4, maxRows: 8 }"
            show-word-limit
          />
        </a-form-item>

        <!-- 上传图片字段 - 复用 ImageUploader -->
        <a-form-item field="images" label="上传图片（可选）">
          <ImageUploader
            v-model="reviewForm.images"
            :limit="5"
            upload-url="/api/upload"
          />
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item>
          <a-space direction="vertical" :style="{ width: '100%' }">
            <a-button
              type="primary"
              long
              size="large"
              html-type="submit"
              :loading="submittingReview"
            >
              提交评价
            </a-button>
            <a-button long size="large" @click="showReviewModal = false">
              取消
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconHeart,
  IconEye,
  IconClockCircle,
  IconPlus,
  IconHeartFill,
  IconApps,
  IconEdit,
  IconInfoCircle,
  IconPhone,
  IconEmail,
  IconCalendar,
} from "@arco-design/web-vue/es/icon";
import StatusTag from "../../components/common/StatusTag/StatusTag.vue";
import ImageGallery from "../../components/data/ImageGallery/ImageGallery.vue";
import ConditionTag from "../../components/data/ConditionTag.vue";
import PageHeader from "../../components/common/PageHeader/PageHeader.vue";
import LoginPromptModal from "../../components/common/LoginPromptModal/LoginPromptModal.vue";
import ImageUploader from "../../components/form/ImageUploader/ImageUploader.vue";
import {
  getItemDetail,
  checkFavorite,
  addFavorite,
  removeFavorite,
  addToCart,
  getCart,
  deleteCartItem,
} from "../../services/api";
import http from "../../services/core/http";
import { useAuthStore } from "../../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const detail = ref(null);
const loading = ref(true);
const isFavorited = ref(false);
const favoriteCount = ref(0);
const isInCart = ref(false);
const cartItemId = ref(null);
const cartLoading = ref(false);

const showLoginPrompt = ref(false);

const reviews = ref([]);
const reviewsTotal = ref(0);
const reviewsPage = ref(1);
const hasMoreReviews = ref(false);
const loadingReviews = ref(false);
const showReviewModal = ref(false);
const submittingReview = ref(false);

const reviewForm = reactive({
  rating: 5,
  content: "",
  images: [],
});

function goBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push("/portal/home");
  }
}

function truncateTitle(title) {
  if (!title) return "";
  return title.length > 30 ? title.substring(0, 30) + "..." : title;
}

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

function formatRating(rating) {
  const value = Number(rating || 5);
  return Number.isFinite(value) ? value.toFixed(1) : "5.0";
}

function formatDate(dateStr) {
  if (!dateStr) return "未知";
  const date = new Date(dateStr);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

async function checkIsFavorite() {
  try {
    const res = await checkFavorite(route.params.id);
    isFavorited.value = res?.data?.isFavorited || res?.isFavorited || false;
  } catch (e) {
    console.error("检查收藏状态失败", e);
  }
}

async function handleFavorite() {
  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id);
      isFavorited.value = false;
      favoriteCount.value = Math.max(0, favoriteCount.value - 1);
      Message.success("已取消收藏");
    } else {
      await addFavorite(route.params.id);
      isFavorited.value = true;
      favoriteCount.value += 1;
      Message.success("收藏成功");
    }
  } catch (e) {
    Message.error(e.message || "操作失败");
  }
}

async function syncCartState() {
  if (!authStore.isLoggedIn || !detail.value?.id) {
    isInCart.value = false;
    cartItemId.value = null;
    return;
  }

  try {
    const res = await getCart();
    const items = Array.isArray(res?.data) ? res.data : [];
    const cartItem = items.find((item) => String(item.itemId) === String(detail.value.id));
    isInCart.value = Boolean(cartItem);
    cartItemId.value = cartItem?.id || null;
  } catch (e) {
    console.error("同步购物车状态失败", e);
  }
}

async function handleAddToCart() {
  if (detail.value.reviewStatus !== "APPROVED") {
    Message.warning("该商品暂不可购买");
    return;
  }

  if (!authStore.isLoggedIn) {
    showLoginPrompt.value = true;
    return;
  }

  cartLoading.value = true;
  try {
    if (isInCart.value && cartItemId.value) {
      await deleteCartItem(cartItemId.value);
      isInCart.value = false;
      cartItemId.value = null;
      Message.success("已取消加入购物车");
      return;
    }

    const res = await addToCart(detail.value.id, 1);
    const cartItem = res?.data || null;
    isInCart.value = true;
    cartItemId.value = cartItem?.id || cartItemId.value;
    Message.success("已加入购物车");
  } catch (e) {
    Message.error(e.message || "购物车操作失败");
  } finally {
    cartLoading.value = false;
  }
}

function handleBuy() {
  if (detail.value.reviewStatus !== "APPROVED") {
    Message.warning("该商品暂不可购买");
    return;
  }

  if (!authStore.isLoggedIn) {
    showLoginPrompt.value = true;
    return;
  }

  router.push(`/portal/orders/confirm/${detail.value.id}`);
}

function handleLoginRedirect() {
  router.push("/login");
}

function handleShowReviewModal() {
  if (!authStore.isLoggedIn) {
    showLoginPrompt.value = true;
    return;
  }
  showReviewModal.value = true;
}

function normalizeReview(review) {
  let images = review.images || [];
  if (typeof images === "string") {
    try {
      images = JSON.parse(images || "[]");
    } catch {
      images = images ? [images] : [];
    }
  }

  return {
    ...review,
    userName: review.userName || review.buyerNickname || "匿名用户",
    images: Array.isArray(images) ? images : [],
    reply: review.reply || review.replyContent || "",
  };
}

async function loadReviews(reset = false) {
  if (reset) {
    reviewsPage.value = 1;
    reviews.value = [];
  }

  loadingReviews.value = true;
  try {
    const res = await http.get(`/items/${route.params.id}/reviews`, {
      params: {
        page: reviewsPage.value,
        pageSize: 5,
      },
    });

    const data = res?.data || res || {};
    const list = data.items || data.list || data.records || [];
    const normalizedList = list.map(normalizeReview);
    if (reset) {
      reviews.value = normalizedList;
    } else {
      reviews.value = [...reviews.value, ...normalizedList];
    }
    reviewsTotal.value = data.totalCount || data.total || 0;
    hasMoreReviews.value = reviews.value.length < reviewsTotal.value;
  } catch (e) {
    console.error("加载评价失败:", e);
  } finally {
    loadingReviews.value = false;
  }
}

function loadMoreReviews() {
  reviewsPage.value += 1;
  loadReviews();
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    Message.warning("请输入评价内容");
    return;
  }

  submittingReview.value = true;
  try {
    const imageUrls = Array.isArray(reviewForm.images)
      ? reviewForm.images
      : [reviewForm.images].filter(Boolean);

    const result = await http.post(`/items/${route.params.id}/reviews`, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim(),
      images: imageUrls,
    });

    Message.success(result?.message || "评价已提交，待审核后展示");
    showReviewModal.value = false;

    reviewForm.rating = 5;
    reviewForm.content = "";
    reviewForm.images = [];

    await loadReviews(true);
  } catch (e) {
    Message.error(e.message || "提交评价失败");
  } finally {
    submittingReview.value = false;
  }
}

function formatReviewTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "刚刚";
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

async function loadDetail() {
  loading.value = true;
  try {
    const data = await getItemDetail(route.params.id);
    detail.value = data;
    favoriteCount.value = data.favoriteCount || 0;
    await checkIsFavorite();
    await syncCartState();
    await loadReviews(true);
  } catch (error) {
    console.error("加载详情失败:", error);
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<style lang="scss" scoped>
.item-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
  padding-bottom: 40px;
}

.detail-spin {
  width: 100%;
  min-height: 60vh;
}

.detail-layout {
  max-width: 1280px;
  margin: 24px auto 0;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 70% 30%;
  gap: 24px;
  align-items: start;

  @media (max-width: 992px) {
    grid-template-columns: 1fr;
  }
}

.detail-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  animation: slideUp 0.6s ease-out 0.1s both;

  :deep(.arco-card-body) {
    padding: 28px;
  }
}
.gallery-section {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
}

.gallery-wrapper {
  flex-shrink: 0;
  width: 280px;
  max-width: 45%;
  
  @media (max-width: 768px) {
    width: 100%;
    max-width: 100%;
  }
}

.info-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.tags-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
  flex-wrap: wrap;

  .category-tag {
    border-radius: 6px;
    font-weight: 500;
  }
}

.item-title {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1.4;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
  
  @media (max-width: 768px) {
    font-size: 20px;
  }
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 0;

  .current-price {
    .price-symbol {
      font-size: 20px;
      font-weight: 700;
      color: #f53f3f;
    }

    .price-value {
      font-size: 32px;
      font-weight: 800;
      color: #f53f3f;
      background: linear-gradient(135deg, #f53f3f 0%, #ff7875 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .original-price {
    font-size: 15px;
    color: #86909c;
    text-decoration: line-through;
  }
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #86909c;

    &.clickable {
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        color: #165dff;
        transform: translateY(-1px);
      }

      .is-favorite {
        color: #f53f3f;
      }
    }
  }
}

.description-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  animation: slideUp 0.6s ease-out 0.2s both;

  :deep(.arco-card-body) {
    padding: 8px 24px 24px 24px;
  }
}

.description-content {
  color: #4e5969;
  line-height: 1.8;
  word-wrap: break-word;

  img {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
  }
}

.reviews-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  animation: slideUp 0.6s ease-out 0.3s both;

  :deep(.arco-card-body) {
    padding: 24px;
  }
}

.reviews-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .reviews-count {
    font-size: 14px;
    color: #86909c;
    background: #f2f3f5;
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 18px;

  :deep(.arco-comment) {
    background: linear-gradient(135deg, #fafbff 0%, #ffffff 100%);
    border: 1px solid #e8ecf2;
    border-radius: 10px;
    padding: 20px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
      border-color: #b8d2ff;
    }
  }

  :deep(.arco-comment-avatar) {
    .user-avatar {
      background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
      color: #fff;
      font-size: 15px;
      font-weight: 700;
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
    }
  }

  :deep(.arco-comment-author) {
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;
  }

  :deep(.arco-comment-datetime) {
    font-size: 13px;
    color: #86909c;
  }
}

.review-content {
  color: #4e5969;
  line-height: 1.7;
  font-size: 14px;
  word-wrap: break-word;
  margin-bottom: 14px;

  img {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
  }
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;

  .review-image {
    width: 88px;
    height: 88px;
    object-fit: cover;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}

.seller-reply {
  margin-top: 14px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f3fe 100%);
  border-radius: 8px;
  border-left: 3px solid #165dff;
  font-size: 14px;
  color: #4e5969;
  line-height: 1.6;
}

.empty-reviews {
  text-align: center;
  padding: 32px 0;

  .write-review-btn {
    margin-top: 20px;
    height: 44px;
    border-radius: 8px;
    font-weight: 600;
  }
}

.load-more-wrapper {
  margin-top: 20px;
}

.detail-right {
  position: relative;
}

.sidebar-sticky {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.seller-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  animation: slideUp 0.6s ease-out 0.15s both;

  :deep(.arco-card-head-title) {
    font-weight: 700;
    font-size: 17px;
  }

  :deep(.arco-card-body) {
    padding: 24px;
  }
}

.seller-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 4px;

  .seller-avatar {
    background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
    color: #fff;
    font-size: 20px;
    font-weight: 700;
    flex-shrink: 0;
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
  }

  .seller-basic {
    flex: 1;
    min-width: 0;

    .seller-name {
      font-size: 17px;
      font-weight: 700;
      color: #1d2129;
      margin-bottom: 6px;
    }

    .seller-rating {
      display: flex;
      align-items: center;
      gap: 8px;

      .rating-text {
        font-size: 13px;
        color: #86909c;
        font-weight: 500;
      }
    }
  }
}

.seller-details-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;

  .detail-item {
    padding: 12px 14px;
    background: #fafbff;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      background: #f0f5ff;
      transform: translateX(2px);
    }

    &.highlight {
      background: linear-gradient(135deg, #e8f3ff 0%, #f0f5ff 100%);

      .sold-count {
        color: #165dff;
        font-weight: 700;
        font-size: 15px;
      }
    }

    .detail-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #86909c;
      margin-bottom: 6px;
      font-weight: 500;
    }

    .detail-value {
      font-size: 14px;
      color: #4e5969;
      font-weight: 500;
      line-height: 1.5;

      &.phone,
      &.email {
        color: #165dff;
        font-family: monospace;
      }
    }
  }
}

.action-group {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  animation: slideUp 0.6s ease-out 0.25s both;

  .buy-btn {
    height: 52px;
    font-size: 17px;
    font-weight: 700;
    border-radius: 10px;
    margin-bottom: 14px;
    background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
    border: none;
    letter-spacing: 1px;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(22, 93, 255, 0.35);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .cart-btn {
    height: 48px;
    font-size: 15px;
    border-radius: 10px;
    border-color: #165dff;
    color: #165dff;
    font-weight: 600;
    margin-bottom: 14px;

    &:hover {
      background: rgba(22, 93, 255, 0.06);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
    }

    &.is-in-cart {
      border-color: #ff7d00;
      color: #ff7d00;
      background: #fff7e8;
    }
  }

  .action-row {
    display: flex;
    justify-content: center;
    gap: 16px;

    .fav-btn {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: scale(1.1) rotate(5deg);
      }

      &.is-favorited {
        color: #f53f3f;
        border-color: #f53f3f;
        background: linear-gradient(135deg, #fff1f0 0%, #ffecea 100%);
        box-shadow: 0 4px 12px rgba(245, 63, 63, 0.2);
      }
    }
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 992px) {
  .detail-layout {
    padding: 0 16px;
    gap: 16px;
  }

  .item-title {
    font-size: 20px;
  }

  .price-section .current-price .price-value {
    font-size: 26px;
  }
}

@media (max-width: 768px) {
  .item-detail-page {
    padding-bottom: 24px;
  }

  .gallery-section {
    flex-direction: column;
    gap: 16px;
    padding: 12px;

    .gallery-wrapper {
      width: 100%;
      max-width: 100%;
    }

    .info-content {
      gap: 10px;
    }
  }

  .item-title {
    font-size: 20px;
    -webkit-line-clamp: 2;
  }

  .price-section .current-price .price-value {
    font-size: 28px;
  }

  .info-card,
  .description-card,
  .reviews-card,
  .seller-card {
    :deep(.arco-card-body) {
      padding: 18px;
    }
  }

  .tags-group {
    gap: 8px;
  }

  .meta-info {
    gap: 16px;
  }
}
</style>
