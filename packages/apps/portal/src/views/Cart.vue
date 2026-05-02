<template>
  <div class="cart-page">
    <div class="page-header">
      <a-space>
        <a-button type="text" @click="$router.back()">
          <template #icon><icon-left /></template>
          返回
        </a-button>
      </a-space>
      <h2 class="page-title">购物车</h2>
      <a-typography-text type="secondary">
        共 {{ cartItems.length }} 件商品
      </a-typography-text>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="cartItems.length > 0" class="cart-content">
        <a-card :bordered="false" class="cart-card">
          <div class="seller-group" v-for="(group, sellerId) in groupedCartItems" :key="sellerId">
            <div class="seller-header">
              <a-space>
                <icon-store />
                <span class="seller-name">{{ group.sellerName }}</span>
              </a-space>
            </div>

            <div class="item-list">
              <div
                v-for="cartItem in group.items"
                :key="cartItem.id"
                class="cart-item"
                :class="{ 'cart-item--unavailable': !isItemAvailable(cartItem.item) }"
              >
                <a-checkbox
                  :model-value="cartItem.selected"
                  :disabled="!isItemAvailable(cartItem.item)"
                  @change="toggleSelect(cartItem)"
                />

                <div class="item-image">
                  <a-image
                    v-if="getImageUrl(cartItem.item)"
                    :src="getImageUrl(cartItem.item)"
                    width="80"
                    height="80"
                    fit="cover"
                  />
                  <div v-else class="item-image--empty">
                    <icon-image />
                  </div>
                </div>

                <div class="item-info">
                  <a-typography-text
                    class="item-title"
                    ellipsis
                    @click="goToItem(cartItem.item?.id)"
                  >
                    {{ cartItem.item?.title || '商品已下架' }}
                  </a-typography-text>
                  <a-typography-text type="danger" class="item-price">
                    ¥{{ cartItem.item?.price || 0 }}
                  </a-typography-text>
                </div>

                <div class="item-actions">
                  <a-input-number
                    :model-value="cartItem.quantity"
                    :min="1"
                    :max="99"
                    :step="1"
                    size="small"
                    :disabled="!isItemAvailable(cartItem.item)"
                    @change="(val) => updateQuantity(cartItem, val)"
                  />
                </div>

                <div class="item-subtotal">
                  <a-typography-text type="danger" strong>
                    ¥{{ getSubtotal(cartItem) }}
                  </a-typography-text>
                </div>

                <div class="item-operations">
                  <a-popconfirm
                    content="确定要删除此商品吗？"
                    @ok="removeItem(cartItem)"
                  >
                    <a-button type="text" status="danger" size="small">
                      <template #icon><icon-delete /></template>
                    </a-button>
                  </a-popconfirm>
                </div>
              </div>
            </div>
          </div>
        </a-card>

        <a-affix :offset-bottom="0" class="checkout-affix">
          <a-card :bordered="false" class="checkout-card">
            <div class="checkout-content">
              <div class="checkout-left">
                <a-checkbox
                  :model-value="isAllSelected"
                  :indeterminate="isIndeterminate"
                  @change="toggleSelectAll"
                >
                  全选
                </a-checkbox>
                <a-button type="text" status="danger" @click="clearSelected" :disabled="selectedCount === 0">
                  清空已选
                </a-button>
              </div>
              <div class="checkout-right">
                <div class="checkout-info">
                  <span class="total-label">合计：</span>
                  <span class="total-amount">¥{{ totalAmount }}</span>
                </div>
                <a-button
                  type="primary"
                  size="large"
                  :disabled="selectedCount === 0"
                  @click="checkout"
                  class="checkout-btn"
                >
                  去结算 ({{ selectedCount }})
                </a-button>
              </div>
            </div>
          </a-card>
        </a-affix>
      </div>

      <div v-else-if="!loading" class="cart-empty">
        <a-empty description="购物车是空的">
          <template #image>
            <icon-list size="64" />
          </template>
          <a-button type="primary" @click="$router.push('/home')">
            去逛逛
          </a-button>
        </a-empty>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconLeft, IconDelete, IconList, IconStore, IconImage } from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import {
  getCartList,
  updateCartQuantity,
  updateCartSelected,
  deleteCartItem,
} from "../../services/api";

const router = useRouter();
const loading = ref(false);
const cartItems = ref([]);

const groupedCartItems = computed(() => {
  const groups = {};
  cartItems.value.forEach((item) => {
    const sellerId = item.item?.sellerId || "unknown";
    const sellerName = item.item?.sellerName || "未知卖家";
    if (!groups[sellerId]) {
      groups[sellerId] = { sellerId, sellerName, items: [] };
    }
    groups[sellerId].items.push(item);
  });
  return groups;
});

const totalAmount = computed(() => {
  return cartItems.value
    .filter((item) => item.selected && isItemAvailable(item.item))
    .reduce((sum, item) => sum + (item.item?.price || 0) * item.quantity, 0)
    .toFixed(2);
});

const selectedCount = computed(() => {
  return cartItems.value.filter((item) => item.selected && isItemAvailable(item.item)).length;
});

const isAllSelected = computed(() => {
  const availableItems = cartItems.value.filter((item) => isItemAvailable(item.item));
  return availableItems.length > 0 && availableItems.every((item) => item.selected);
});

const isIndeterminate = computed(() => {
  const availableItems = cartItems.value.filter((item) => isItemAvailable(item.item));
  return selectedCount.value > 0 && selectedCount.value < availableItems.length;
});

function isItemAvailable(item) {
  return item && item.reviewStatus === "APPROVED";
}

function getSubtotal(cartItem) {
  return ((cartItem.item?.price || 0) * cartItem.quantity).toFixed(2);
}

function getImageUrl(item) {
  return parseFirstImageUrl(item?.imageUrls || item?.images);
}

function goToItem(itemId) {
  if (itemId) {
    router.push(`/item/${itemId}`);
  }
}

async function loadCart() {
  loading.value = true;
  try {
    const res = await getCartList();
    cartItems.value = res || [];
  } catch (e) {
    Message.error(e.message || "加载购物车失败");
  } finally {
    loading.value = false;
  }
}

async function toggleSelect(cartItem) {
  try {
    await updateCartSelected(cartItem.id, !cartItem.selected);
    cartItem.selected = !cartItem.selected;
  } catch (e) {
    Message.error(e.message || "更新失败");
  }
}

async function toggleSelectAll(checked) {
  try {
    for (const item of cartItems.value) {
      if (isItemAvailable(item.item) && item.selected !== checked) {
        await updateCartSelected(item.id, checked);
        item.selected = checked;
      }
    }
  } catch (e) {
    Message.error(e.message || "批量更新失败");
  }
}

async function updateQuantity(cartItem, quantity) {
  if (!quantity || quantity < 1) return;
  try {
    await updateCartQuantity(cartItem.id, quantity);
    cartItem.quantity = quantity;
  } catch (e) {
    Message.error(e.message || "更新失败");
  }
}

async function removeItem(cartItem) {
  try {
    await deleteCartItem(cartItem.id);
    Message.success("已从购物车删除");
    await loadCart();
  } catch (e) {
    Message.error(e.message || "删除失败");
  }
}

async function clearSelected() {
  const selectedItems = cartItems.value.filter((item) => item.selected);
  try {
    for (const item of selectedItems) {
      await deleteCartItem(item.id);
    }
    Message.success("已清空已选商品");
    await loadCart();
  } catch (e) {
    Message.error(e.message || "清空失败");
  }
}

function checkout() {
  const selectedItems = cartItems.value.filter((item) => item.selected && isItemAvailable(item.item));
  if (selectedItems.length === 0) {
    Message.warning("请选择要结算的商品");
    return;
  }
  const firstItem = selectedItems[0];
  router.push(`/orders/confirm/${firstItem.itemId}`);
}

onMounted(loadCart);
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
  padding-bottom: 120px;
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

.cart-card {
  border-radius: 12px;
  margin-bottom: 80px;

  :deep(.arco-card-body) {
    padding: 0;
  }
}

.seller-group {
  &:not(:last-child) {
    border-bottom: 1px solid #f0f0f0;
  }
}

.seller-header {
  padding: 12px 16px;
  background: #f7f8fa;
  border-bottom: 1px solid #f0f0f0;

  .seller-name {
    font-size: 14px;
    font-weight: 500;
    color: #4e5969;
  }

  :deep(.arco-icon) {
    color: #165dff;
  }
}

.item-list {
  padding: 8px 0;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  transition: background 0.2s;

  &:hover {
    background: #fafafa;
  }

  &--unavailable {
    opacity: 0.5;
    background: #f9f9f9;
  }
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;

  :deep(.arco-image) {
    border-radius: 8px;
  }

  &--empty {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f2f3f5;
    border-radius: 8px;
    color: #86909c;
    font-size: 32px;
  }
}

.item-info {
  flex: 1;
  min-width: 0;

  .item-title {
    display: block;
    font-size: 14px;
    color: #1d2129;
    margin-bottom: 8px;
    cursor: pointer;
    max-width: 200px;

    &:hover {
      color: #165dff;
    }
  }

  .item-price {
    font-size: 16px;
    font-weight: 600;
  }
}

.item-actions {
  width: 100px;
}

.item-subtotal {
  width: 80px;
  text-align: right;
  font-size: 15px;
  font-weight: 600;
}

.item-operations {
  width: 40px;
}

.checkout-affix {
  right: calc((100% - 1000px) / 2 + 24px);
  left: calc((100% - 1000px) / 2 + 24px);

  @media (max-width: 1048px) {
    right: 24px;
    left: 24px;
  }
}

.checkout-card {
  border-radius: 12px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.08);

  :deep(.arco-card-body) {
    padding: 16px 20px;
  }
}

.checkout-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkout-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.checkout-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.checkout-info {
  display: flex;
  align-items: baseline;
  gap: 8px;

  .total-label {
    font-size: 14px;
    color: #4e5969;
  }

  .total-amount {
    font-size: 24px;
    font-weight: 700;
    color: #ff4d4f;
  }
}

.checkout-btn {
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  border: none;
  padding: 0 32px;
  height: 44px;
  font-size: 16px;
}

.cart-empty {
  background: #fff;
  border-radius: 12px;
  padding: 80px 20px;

  :deep(.arco-empty-icon) {
    color: #d1d5db;
  }
}
</style>
