<template>
  <teleport to="body">
    <transition name="mini-cart-fade">
      <div v-if="visible" class="mini-cart-overlay" @click.self="visible = false">
        <div class="mini-cart-panel" :class="{ 'mini-cart-panel--expanded': expanded }">
          <div class="mini-cart-header" @click="toggleExpand">
            <div class="mini-cart-icon">
              <icon-list size="24" />
              <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
            </div>
            <div class="mini-cart-info">
              <span class="cart-label">购物车</span>
              <span class="cart-count-text">共{{ cartCount }}件</span>
            </div>
            <div class="expand-icon">
              <icon-down :class="{ 'rotate-180': expanded }" />
            </div>
          </div>

          <transition name="slide-down">
            <div v-if="expanded" class="mini-cart-content">
              <div v-if="cartItems.length > 0" class="cart-items">
                <div
                  v-for="item in cartItems.slice(0, 5)"
                  :key="item.id"
                  class="cart-item"
                  @click="goToCart"
                >
                  <img
                    v-if="item.item?.imageUrls"
                    :src="parseFirstImageUrl(item.item.imageUrls)"
                    class="item-thumb"
                  />
                  <div v-else class="item-thumb item-thumb--empty">📷</div>
                  <div class="item-info">
                    <div class="item-title">{{ item.item?.title }}</div>
                    <div class="item-price">¥{{ item.item?.price }}</div>
                  </div>
                </div>
                <div v-if="cartItems.length > 5" class="more-items">
                  还有 {{ cartItems.length - 5 }} 件商品
                </div>
              </div>

              <a-empty v-else description="购物车是空的" :image-size="48">
                <template #image>
                  <icon-list size="32" />
                </template>
              </a-empty>

              <div class="mini-cart-footer">
                <div class="total-info">
                  <span>合计:</span>
                  <span class="total-amount">¥{{ totalAmount }}</span>
                </div>
                <a-button type="primary" size="small" @click="goToCart">
                  去结算
                </a-button>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </transition>

    <transition name="mini-cart-badge">
      <div v-if="!visible && cartCount > 0" class="mini-cart-trigger" @click="visible = true">
        <icon-list size="28" />
        <span class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { IconList, IconDown } from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "commonprovide/image-utils";
import { getCartCount, getCartList } from "../services/api";

const router = useRouter();
const visible = ref(false);
const expanded = ref(false);
const cartCount = ref(0);
const cartItems = ref([]);

const totalAmount = computed(() => {
  return cartItems.value
    .filter((item) => item.selected)
    .reduce((sum, item) => sum + (item.item?.price || 0) * item.quantity, 0);
});

async function loadCartData() {
  try {
    const countRes = await getCartCount();
    cartCount.value = countRes?.total || 0;

    if (visible.value) {
      const listRes = await getCartList();
      cartItems.value = listRes || [];
    }
  } catch (e) {
    console.error("加载购物车数据失败", e);
  }
}

function toggleExpand() {
  expanded.value = !expanded.value;
  if (expanded.value) {
    loadCartData();
  }
}

function goToCart() {
  visible.value = false;
  expanded.value = false;
  router.push("/cart");
}

let refreshInterval = null;

onMounted(() => {
  loadCartData();
  refreshInterval = setInterval(loadCartData, 30000);
});

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});

defineExpose({ loadCartData });
</script>

<style lang="scss" scoped>
.mini-cart-trigger {
  position: fixed;
  bottom: 100px;
  right: 24px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.4);
  transition: transform 0.3s, box-shadow 0.3s;
  z-index: 999;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(124, 58, 237, 0.5);
  }

  .cart-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    background: #f53f3f;
    color: white;
    font-size: 11px;
    font-weight: 600;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.mini-cart-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.mini-cart-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-radius: 16px 16px 0 0;
  transition: height 0.3s ease;
  max-height: 70vh;
  overflow: hidden;

  &--expanded {
    height: auto;
  }
}

.mini-cart-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;

  .mini-cart-icon {
    position: relative;
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;

    .cart-badge {
      position: absolute;
      top: -6px;
      right: -6px;
      min-width: 18px;
      height: 18px;
      padding: 0 5px;
      background: #f53f3f;
      color: white;
      font-size: 10px;
      font-weight: 600;
      border-radius: 9px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .mini-cart-info {
    flex: 1;

    .cart-label {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;
    }

    .cart-count-text {
      font-size: 12px;
      color: #9ca3af;
    }
  }

  .expand-icon {
    color: #9ca3af;

    :deep(.arco-icon) {
      transition: transform 0.3s;
    }

    .rotate-180 {
      transform: rotate(180deg);
    }
  }
}

.mini-cart-content {
  max-height: 400px;
  overflow-y: auto;
  border-top: 1px solid #f0f0f0;
}

.cart-items {
  padding: 8px 0;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f9fafb;
  }

  .item-thumb {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #9ca3af;
      font-size: 20px;
    }
  }

  .item-info {
    flex: 1;
    min-width: 0;

    .item-title {
      font-size: 13px;
      color: #374151;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }

    .item-price {
      font-size: 14px;
      font-weight: 600;
      color: #f53f3f;
    }
  }
}

.more-items {
  text-align: center;
  padding: 12px;
  font-size: 12px;
  color: #9ca3af;
  cursor: pointer;

  &:hover {
    color: #7c3aed;
  }
}

.mini-cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f9fafb;
  border-top: 1px solid #f0f0f0;

  .total-info {
    font-size: 14px;
    color: #4b5563;

    .total-amount {
      margin-left: 8px;
      font-size: 18px;
      font-weight: 700;
      color: #f53f3f;
    }
  }

  :deep(.arco-btn-primary) {
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    border: none;
  }
}

.mini-cart-fade-enter-active,
.mini-cart-fade-leave-active {
  transition: opacity 0.3s;
}

.mini-cart-fade-enter-from,
.mini-cart-fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.mini-cart-badge-enter-active,
.mini-cart-badge-leave-active {
  transition: all 0.3s;
}

.mini-cart-badge-enter-from,
.mini-cart-badge-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

:deep(.arco-empty) {
  padding: 20px;
}
</style>
