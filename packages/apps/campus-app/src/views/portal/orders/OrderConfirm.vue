<template>
  <div class="order-confirm-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">确认订单</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="item" class="confirm-content">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :lg="16">
            <a-card title="商品信息" :bordered="false" class="section-card">
              <div class="item-info-card">
                <div class="item-image-wrapper">
                  <img
                    v-if="getItemImage(item)"
                    :src="getItemImage(item)"
                    class="item-image"
                  />
                  <div v-else class="item-image item-image--empty">📷</div>
                </div>
                <div class="item-detail">
                  <h3 class="item-title">{{ item.title }}</h3>
                  <div class="item-meta">
                    <ConditionTag v-if="item.conditionLevel" :condition="item.conditionLevel" />
                    <span v-if="item.category" class="item-category">{{ getCategoryLabel(item.category) }}</span>
                  </div>
                </div>
              </div>

              <div class="price-info">
                <span class="price-label">单价</span>
                <span class="price-value">¥{{ (item.price || 0).toFixed(2) }}</span>
                <span class="quantity-info">× {{ quantity }}</span>
                <span class="subtotal">= ¥{{ ((item.price || 0) * quantity).toFixed(2) }}</span>
              </div>
            </a-card>

            <a-card title="收货地址" :bordered="false" class="section-card address-card">
              <template #extra>
                <a-button type="text" size="small" @click="showAddressModal = true">
                  <template #icon><icon-edit /></template>
                  {{ defaultAddress ? '更换地址' : '使用新地址' }}
                </a-button>
              </template>

              <div v-if="defaultAddress" class="default-address">
                <AddressCard
                  :address="defaultAddress"
                  :editable="false"
                />
              </div>
              <div v-else class="no-address">
                <icon-location />
                <p>暂无收货地址</p>
                <a-button type="primary" size="small" @click="showAddressModal = true">
                  添加收货地址
                </a-button>
              </div>
            </a-card>
          </a-col>

          <a-col :xs="24" :lg="8">
            <a-card title="订单明细" :bordered="false" class="summary-card">
              <div class="summary-list">
                <div class="summary-item">
                  <span>商品金额</span>
                  <span>¥{{ ((item.price || 0) * quantity).toFixed(2) }}</span>
                </div>
                <div class="summary-item">
                  <span>运费</span>
                  <span class="free-shipping">免运费</span>
                </div>
                <div class="summary-divider"></div>
                <div class="summary-item summary-total">
                  <span>应付总额</span>
                  <span class="total-amount">¥{{ ((item.price || 0) * quantity).toFixed(2) }}</span>
                </div>
              </div>

              <a-button
                type="primary"
                size="large"
                long
                :loading="submitting"
                :disabled="!defaultAddress"
                @click="submitOrder"
                class="submit-btn"
              >
                提交订单
              </a-button>
              <p v-if="!defaultAddress" class="address-tip">
                请先选择或添加收货地址
              </p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>

    <EditAddressModal
      v-model:visible="showAddressModal"
      @success="handleAddressSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconLocation,
  IconEdit,
} from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "../../../utils/image-utils";
import AddressCard from "../../../components/AddressCard.vue";
import ConditionTag from "../../../components/sub/ConditionTag.vue";
import EditAddressModal from "../../../components/sub/EditAddressModal.vue";
import { getItemDetail, createOrder, getDefaultAddress, getAddressList } from "../../../services/api";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const item = ref(null);
const quantity = ref(1);
const defaultAddress = ref(null);
const showAddressModal = ref(false);

const CATEGORY_MAP = {
  digital: "数码", book: "教材", clothing: "服饰",
  daily: "生活", sport: "运动", instrument: "乐器", other: "其他",
  electronics: "数码", textbooks: "教材",
};

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

function getItemImage(itemData) {
  return parseFirstImageUrl(itemData?.imageUrls) || getImageUrl(itemData);
}

function getCategoryLabel(category) {
  return CATEGORY_MAP[category] || category || "";
}

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

async function loadDefaultAddress() {
  try {
    const res = await getDefaultAddress();
    if (res) {
      defaultAddress.value = res;
    }
  } catch (e) {
    try {
      const list = await getAddressList();
      if (Array.isArray(list) && list.length > 0) {
        const found = list.find((addr) => addr.isDefault) || list[0];
        defaultAddress.value = found;
      }
    } catch (err) {
      console.warn("[OrderConfirm] 加载地址失败:", err);
    }
  }
}

function handleAddressSuccess(address) {
  defaultAddress.value = address;
}

async function submitOrder() {
  if (!defaultAddress.value) {
    Message.warning("请先选择收货地址");
    return;
  }

  submitting.value = true;
  try {
    const address = defaultAddress.value;
    const orderData = {
      itemId: item.value.id,
      quantity: quantity.value,
      receiverName: address.receiverName,
      receiverPhone: address.receiverPhone,
      receiverAddress: `${address.province}${address.city}${address.district || ''}${address.detailAddress}`
    };
    const res = await createOrder(orderData);
    Message.success("订单创建成功");
    router.push("/portal/orders");
  } catch (e) {
    Message.error(e.message || "创建订单失败");
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadItem();
  loadDefaultAddress();
});
</script>

<style lang="scss" scoped>
.order-confirm-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }
}

.confirm-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-card {
  margin-bottom: 16px;
  border-radius: 12px;

  :deep(.arco-card-header) {
    font-weight: 600;
  }
}

.item-info-card {
  display: flex;
  gap: 16px;
  padding: 8px 0;

  .item-image-wrapper {
    flex-shrink: 0;
  }

  .item-image {
    width: 100px;
    height: 100px;
    border-radius: 12px;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f2f3f5;
      font-size: 40px;
    }
  }

  .item-detail {
    flex: 1;
    min-width: 0;

    .item-title {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 500;
      color: #1d2129;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 8px;

      .item-category {
        font-size: 12px;
        color: #86909c;
        background: #f2f3f5;
        padding: 2px 8px;
        border-radius: 4px;
      }
    }
  }
}

.price-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 12px;
  font-size: 14px;
  gap: 12px;

  .price-label {
    color: #86909c;
  }

  .price-value {
    font-weight: 600;
    color: #1d2129;
  }

  .quantity-info {
    color: #86909c;
  }

  .subtotal {
    font-weight: 700;
    color: #f53f3f;
    font-size: 16px;
  }
}

.address-card {
  .default-address {
    padding: 4px 0;
  }

  .no-address {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 32px 0;
    color: #86909c;

    :deep(.arco-icon) {
      font-size: 40px;
    }

    p {
      margin: 0;
    }
  }
}

.summary-card {
  :deep(.arco-card-header) {
    font-weight: 600;
  }
}

.summary-list {
  .summary-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    font-size: 14px;
    color: #4e5969;

    &.summary-total {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;

      .total-amount {
        color: #f53f3f;
        font-size: 22px;
      }
    }
  }

  .free-shipping {
    color: #00b42a;
    font-weight: 500;
  }

  .summary-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 8px 0;
  }
}

.submit-btn {
  margin-top: 24px;
  font-size: 16px;
  font-weight: 500;
}

.address-tip {
  text-align: center;
  margin-top: 12px;
  color: #86909c;
  font-size: 12px;
}
</style>
