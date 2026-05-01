<template>
  <div class="item-detail">
    <a-spin :loading="loading" style="width: 100%">
      <div v-if="item" class="item-detail__layout">
        <!-- 左：图片 -->
        <div class="item-detail__gallery">
          <ImageGallery :images="item.imageUrls || []" />
        </div>
        <!-- 右：信息 -->
        <div class="item-detail__info">
          <h1 class="item-detail__title">{{ item.title }}</h1>
          <div class="item-detail__price-row">
            <span class="item-detail__price">¥{{ item.price }}</span>
            <a-tag v-if="item.conditionLevel" color="cyan" size="small">
              {{ conditionLabel(item.conditionLevel) }}
            </a-tag>
            <a-tag v-if="item.category" color="blue" size="small">{{ item.category }}</a-tag>
          </div>
          <div class="item-detail__desc">
            <h3>商品描述</h3>
            <p>{{ item.description || '暂无描述' }}</p>
          </div>
          <div class="item-detail__seller">
            <h3>卖家信息</h3>
            <div class="item-detail__seller-card">
              <a-avatar :size="40">{{ item.sellerName?.[0] || '卖' }}</a-avatar>
              <div>
                <div class="item-detail__seller-name">{{ item.sellerName || '匿名卖家' }}</div>
                <div v-if="item.campus" class="item-detail__seller-campus">
                  📍 {{ item.campus }}
                </div>
              </div>
            </div>
          </div>
          <!-- 操作按钮 -->
          <div class="item-detail__actions">
            <a-button
              v-if="isBuyer && item.reviewStatus === 'APPROVED'"
              type="primary"
              size="large"
              long
              @click="handleBuy"
            >
              <template #icon><span>🛒</span></template>
              我想要
            </a-button>
            <a-button v-else-if="item.reviewStatus !== 'APPROVED'" type="primary" size="large" long disabled>
              商品审核中，暂不可购买
            </a-button>
            <a-button size="large" long @click="$router.back()">
              返回
            </a-button>
          </div>
        </div>
      </div>
      <!-- 不存在 -->
      <a-result v-else-if="!loading" status="404" subtitle="商品不存在或已下架">
        <template #extra>
          <a-button type="primary" @click="$router.push('/home')">返回首页</a-button>
        </template>
      </a-result>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import { getItemDetail, createOrder } from "../../services/api";
import { getCurrentUser } from "commonprovide/auth-sdk";
import ImageGallery from "commonprovide/ImageGallery";

const route = useRoute();
const router = useRouter();

const item = ref(null);
const loading = ref(false);

const currentUser = getCurrentUser();
const isBuyer = computed(() => (currentUser?.roles || []).includes("BUYER"));

const CONDITION_MAP = {
  new: "全新", "99": "99新", "95": "95新",
  "9": "9成新", "8": "8成新", "7": "7成新",
};
function conditionLabel(level) {
  return CONDITION_MAP[level] || level || "";
}

async function loadItem() {
  loading.value = true;
  try {
    const id = route.params.id;
    item.value = await getItemDetail(id);
  } catch (e) {
    console.error("[ItemDetail] load error:", e);
  } finally {
    loading.value = false;
  }
}

function handleBuy() {
  router.push(`/orders/confirm/${item.value.id}`);
}

onMounted(loadItem);
</script>

<style lang="scss" scoped>
.item-detail {
  &__layout {
    display: flex;
    gap: 32px;
    background: #fff;
    border-radius: 16px;
    padding: 24px;
  }

  &__gallery {
    width: 480px;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    margin: 0 0 12px;
    font-size: 22px;
    font-weight: 700;
    color: #1d2129;
    line-height: 1.4;
  }

  &__price-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
  }

  &__price {
    font-size: 28px;
    font-weight: 800;
    color: #f53f3f;
  }

  &__desc, &__seller {
    margin-bottom: 20px;

    h3 {
      margin: 0 0 8px;
      font-size: 15px;
      font-weight: 600;
      color: #4e5969;
    }
  }

  &__desc p {
    margin: 0;
    color: #1d2129;
    line-height: 1.7;
    white-space: pre-wrap;
  }

  &__seller-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 10px;
  }

  &__seller-name {
    font-weight: 600;
    color: #1d2129;
  }

  &__seller-campus {
    font-size: 12px;
    color: #86909c;
    margin-top: 2px;
    display: flex;
    align-items: center;
    gap: 3px;
  }

  &__actions {
    display: flex;
    gap: 12px;
    margin-top: 24px;
  }
}

@media (max-width: 900px) {
  .item-detail__layout { flex-direction: column; }
  .item-detail__gallery { width: 100%; }
}
</style>
