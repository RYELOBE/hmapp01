<template>
  <div class="order-confirm-page">
    <div class="order-confirm-page__header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2>确认订单</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="item" class="order-confirm-content">
        <!-- 商品信息 -->
        <div class="item-section">
          <h3>商品信息</h3>
          <div class="item-card">
            <img v-if="item.imageUrls" :src="parseFirstImageUrl(item.imageUrls)" class="item-card__img" />
            <div v-else class="item-card__img item-card__img--empty">📷</div>
            <div class="item-card__info">
              <div class="item-card__title">{{ item.title }}</div>
              <div class="item-card__price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="address-section">
          <h3>收货地址</h3>
          <a-form :model="form" layout="vertical">
            <a-form-item label="收货人">
              <a-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
            </a-form-item>
            <a-form-item label="联系电话">
              <a-input v-model="form.receiverPhone" placeholder="请输入联系电话" />
            </a-form-item>
            <a-form-item label="收货地址">
              <a-textarea v-model="form.receiverAddress" placeholder="请输入收货地址" :rows="3" />
            </a-form-item>
          </a-form>
        </div>

        <!-- 订单摘要 -->
        <div class="summary-section">
          <h3>订单摘要</h3>
          <div class="summary-item">
            <span>商品价格</span>
            <span>¥{{ item.price }}</span>
          </div>
          <div class="summary-item summary-item--total">
            <span>合计</span>
            <span>¥{{ item.price }}</span>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <a-button type="primary" size="large" :loading="submitting" @click="submitOrder">提交订单</a-button>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconArrowLeft } from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import { getItemDetail, createOrder, getDefaultAddress } from "../../services/api";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const item = ref(null);

const form = ref({
  receiverName: "",
  receiverPhone: "",
  receiverAddress: "",
});

async function loadItem() {
  const itemId = route.params.id;
  if (!itemId) {
    Message.error("商品ID不存在");
    router.back();
    return;
  }

  loading.value = true;
  try {
    const res = await getItemDetail(itemId);
    item.value = res;
  } catch (e) {
    Message.error(e.message || "加载商品失败");
  } finally {
    loading.value = false;
  }
}

async function submitOrder() {
  if (!form.value.receiverName) {
    Message.error("请输入收货人姓名");
    return;
  }
  if (!form.value.receiverPhone) {
    Message.error("请输入联系电话");
    return;
  }
  if (!form.value.receiverAddress) {
    Message.error("请输入收货地址");
    return;
  }

  submitting.value = true;
  try {
    const orderData = {
      itemId: item.value.id,
      quantity: 1,
      receiverName: form.value.receiverName,
      receiverPhone: form.value.receiverPhone,
      receiverAddress: form.value.receiverAddress,
    };
    const res = await createOrder(orderData);
    Message.success("订单创建成功");
    router.push(`/orders`);
  } catch (e) {
    Message.error(e.message || "创建订单失败");
  } finally {
    submitting.value = false;
  }
}

async function loadDefaultAddress() {
  try {
    const res = await getDefaultAddress();
    if (res) {
      form.value.receiverName = res.receiverName || res.name || "";
      form.value.receiverPhone = res.receiverPhone || res.phone || "";
      form.value.receiverAddress = res.receiverAddress || res.address || res.detailAddress || "";
    }
  } catch (e) {
    console.warn("[OrderConfirm] 加载默认地址失败:", e);
  }
}

onMounted(() => {
  loadItem();
  loadDefaultAddress();
});
</script>

<style lang="scss" scoped>
.order-confirm-page {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;

  &__header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;

    h2 { margin: 0; font-size: 20px; font-weight: 700; }
  }
}

.item-section,
.address-section,
.summary-section {
  margin-bottom: 24px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
  }
}

.item-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 12px;

  &__img {
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

  &__info { flex: 1; }

  &__title {
    font-size: 16px;
    font-weight: 500;
    color: #1d2129;
    margin-bottom: 8px;
  }

  &__price {
    font-size: 20px;
    font-weight: 700;
    color: #f53f3f;
  }
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }

  &--total {
    font-size: 16px;
    font-weight: 600;
    padding-top: 16px;

    span:last-child {
      color: #f53f3f;
      font-size: 20px;
    }
  }
}

.submit-section {
  display: flex;
  justify-content: center;
  padding-top: 16px;

  :deep(.arco-btn) {
    width: 100%;
    max-width: 300px;
  }
}
</style>
