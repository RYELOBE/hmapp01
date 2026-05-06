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
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconLocation,
  IconEdit,
} from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import AddressCard from "../../components/AddressCard";
import ConditionTag from "../../components/sub/ConditionTag";
import EditAddressModal from "../../components/sub/EditAddressModal";
import { getItemDetail, createOrder, getDefaultAddress, getAddressList } from "../../services/api";

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
};

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try { return JSON.parse(urls)[0]; } catch { return urls; }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : null;
}

function getItemImage(item) {
  return parseFirstImageUrl(item?.imageUrls) || getImageUrl(item);
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
    const orderData = {
      itemId: item.value.id,
      quantity: quantity.value,
      addressId: defaultAddress.value.id,
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
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }
}

.section-card {
  border-radius: 12px;
  margin-bottom: 16px;

  :deep(.arco-card-head-title) {
    font-size: 15px;
    font-weight: 600;
  }

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.item-info-card {
  display: flex;
  gap: 16px;

  .item-image-wrapper {
    flex-shrink: 0;
  }

  .item-image {
    width: 80px;
    height: 80px;
    border-radius: 10px;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f2f3f5;
      font-size: 32px;
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
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .item-category {
      font-size: 12px;
      color: #86909c;
      background: #f2f3f5;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
}

.price-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
  font-size: 14px;

  .price-label {
    color: #86909c;
  }

  .price-value {
    font-weight: 600;
    color: #1d2129;
  }

  .quantity-info {
    color: #86909c;
    margin: 0 4px;
  }

  .subtotal {
    font-weight: 700;
    color: #f53f3f;
    font-size: 16px;
  }
}

.address-card {
  :deep(.arco-card-head-extra) {
    font-size: 13px;
  }
}

.default-address {
  :deep(.address-card) {
    cursor: pointer;
  }
}

.no-address {
  text-align: center;
  padding: 24px;
  color: #86909c;

  :deep(.arco-icon) {
    font-size: 40px;
    color: #c9cdd4;
    margin-bottom: 8px;
  }

  p {
    margin: 0 0 12px;
    font-size: 14px;
  }
}

.summary-card {
  position: sticky;
  top: 20px;

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.summary-list {
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
  color: #4e5969;

  .free-shipping {
    color: #00b42a;
    font-weight: 500;
  }
}

.summary-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 4px 0;
}

.summary-total {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  padding-top: 12px;

  .total-amount {
    font-size: 22px;
    font-weight: 700;
    color: #f53f3f;
  }
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  border: none;
  border-radius: 8px;
}

.address-tip {
  margin: 12px 0 0;
  text-align: center;
  font-size: 13px;
  color: #f53f3f;
}
</style>
