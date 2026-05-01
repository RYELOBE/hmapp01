<template>
  <div class="cart-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">购物车</h2>
      <span class="cart-count">共 {{ cartItems.length }} 件商品</span>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="cartItems.length > 0" class="cart-content">
        <!-- 卖家分组 -->
        <div v-for="(group, sellerId) in groupedCartItems" :key="sellerId" class="seller-group">
          <div class="seller-header">
            <span class="seller-name">{{ group.sellerName }}</span>
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
                <img
                  v-if="cartItem.item?.imageUrls"
                  :src="cartItem.item.imageUrls"
                  :alt="cartItem.item?.title"
                />
                <div v-else class="item-image--empty">📷</div>
              </div>

              <div class="item-info">
                <div class="item-title" @click="goToItem(cartItem.item?.id)">
                  {{ cartItem.item?.title || '商品已下架' }}
                </div>
                <div class="item-price">¥{{ cartItem.item?.price || 0 }}</div>
              </div>

              <div class="item-actions">
                <a-input-number
                  :model-value="cartItem.quantity"
                  :min="1"
                  :max="99"
                  :step="1"
                  size="small"
                  @change="(val) => updateQuantity(cartItem, val)"
                />
                <div class="item-subtotal">小计: ¥{{ getSubtotal(cartItem) }}</div>
              </div>

              <div class="item-operations">
                <a-popconfirm content="确定要删除此商品吗？" @ok="removeItem(cartItem)">
                  <a-button type="text" status="danger" size="small">
                    <template #icon><icon-delete /></template>
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="checkout-bar">
          <div class="checkout-info">
            <span class="total-label">合计:</span>
            <span class="total-amount">¥{{ totalAmount }}</span>
          </div>
          <a-button
            type="primary"
            size="large"
            :disabled="selectedCount === 0"
            @click="checkout"
          >
            去结算 ({{ selectedCount }})
          </a-button>
        </div>
      </div>

      <a-empty v-else-if="!loading" description="购物车是空的">
        <template #image>
          <icon-shopping-cart size="64" />
        </template>
        <a-button type="primary" @click="$router.push('/home')">去逛逛</a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconArrowLeft, IconDelete, IconShoppingCart } from "@arco-design/web-vue/es/icon";
import {
  getCartList,
  updateCartQuantity,
  updateCartSelected,
  deleteCartItem,
  clearCart,
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
    .reduce((sum, item) => sum + (item.item?.price || 0) * item.quantity, 0);
});

const selectedCount = computed(() => {
  return cartItems.value.filter((item) => item.selected && isItemAvailable(item.item)).length;
});

function isItemAvailable(item) {
  return item && item.reviewStatus === "APPROVED";
}

function getSubtotal(cartItem) {
  return (cartItem.item?.price || 0) * cartItem.quantity;
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
    cartItems.value = res?.data || [];
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

function checkout() {
  const selectedItems = cartItems.value.filter((item) => item.selected && isItemAvailable(item.item));
  if (selectedItems.length === 0) {
    Message.warning("请选择要结算的商品");
    return;
  }
  const itemId = selectedItems[0].itemId;
  router.push(`/orders/confirm/${itemId}`);
}

onMounted(loadCart);
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%);
  min-height: 100vh;
  padding-bottom: 100px;
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

  .cart-count {
    font-size: 14px;
    color: #6b7280;
  }
}

.seller-group {
  margin-bottom: 20px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.seller-header {
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;

  .seller-name {
    font-size: 14px;
    font-weight: 500;
    color: #4b5563;
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
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

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

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--empty {
    width: 100%;
    height: 100%;
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
  min-width: 0;

  .item-title {
    font-size: 14px;
    color: #1f2937;
    margin-bottom: 8px;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      color: #7c3aed;
    }
  }

  .item-price {
    font-size: 16px;
    font-weight: 600;
    color: #f53f3f;
  }
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;

  .item-subtotal {
    font-size: 13px;
    color: #6b7280;
  }
}

.item-operations {
  display: flex;
  gap: 4px;
}

.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  z-index: 100;

  .checkout-info {
    display: flex;
    align-items: baseline;
    gap: 8px;

    .total-label {
      font-size: 14px;
      color: #6b7280;
    }

    .total-amount {
      font-size: 24px;
      font-weight: 700;
      color: #f53f3f;
    }
  }

  :deep(.arco-btn-primary) {
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    border: none;
    padding: 0 40px;
    height: 44px;
    font-size: 16px;
  }
}

:deep(.arco-empty) {
  padding: 60px 20px;
  background: white;
  border-radius: 12px;

  .arco-empty-icon {
    color: #d1d5db;
  }
}
</style>
