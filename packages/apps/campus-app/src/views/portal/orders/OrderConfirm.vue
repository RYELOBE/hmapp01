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
      <div v-if="orderItems.length > 0" class="confirm-content">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :lg="16">
            <!-- 商品列表 -->
            <a-card title="商品信息" :bordered="false" class="section-card">
              <div 
                v-for="(orderItem, index) in orderItems" 
                :key="orderItem.cartId || index"
                class="order-item"
              >
                <div class="item-info-card">
                  <div class="item-image-wrapper">
                    <img
                      v-if="getItemImage(orderItem.item)"
                      :src="getItemImage(orderItem.item)"
                      class="item-image"
                    />
                    <div v-else class="item-image item-image--empty">📷</div>
                  </div>
                  <div class="item-detail">
                    <h3 class="item-title">{{ orderItem.item.title }}</h3>
                    <div class="item-meta">
                      <ConditionTag v-if="orderItem.item.conditionLevel" :condition="orderItem.item.conditionLevel" />
                      <span v-if="orderItem.item.category" class="item-category">{{ getCategoryLabel(orderItem.item.category) }}</span>
                    </div>
                  </div>
                </div>

                <div class="price-info">
                  <span class="price-label">单价</span>
                  <span class="price-value">¥{{ (orderItem.item.price || 0).toFixed(2) }}</span>
                  <span class="quantity-info">× {{ orderItem.quantity }}</span>
                  <span class="subtotal">= ¥{{ ((orderItem.item.price || 0) * orderItem.quantity).toFixed(2) }}</span>
                </div>

                <a-divider v-if="index < orderItems.length - 1" />
              </div>
            </a-card>

            <!-- 线下交易提示 -->
            <a-card :bordered="false" class="section-card trade-notice">
              <div class="notice-content">
                <icon-info-circle style="color: #165DFF; font-size: 20px;" />
                <div class="notice-text">
                  <h4>校园线下交易</h4>
                  <p>本平台为校园二手交易平台，下单后请与卖家联系，约定时间地点进行线下面对面交易。</p>
                  <p class="notice-tip">💡 建议选择人多的公共场所进行交易，注意安全！</p>
                </div>
              </div>
            </a-card>
          </a-col>

          <a-col :xs="24" :lg="8">
            <a-card title="订单明细" :bordered="false" class="summary-card">
              <div class="summary-list">
                <div class="summary-item">
                  <span>商品数量</span>
                  <span>{{ totalQuantity }} 件</span>
                </div>
                <div class="summary-item">
                  <span>商品金额</span>
                  <span>¥{{ totalAmount }}</span>
                </div>
                <div class="summary-item">
                  <span>运费</span>
                  <span class="free-shipping">免运费</span>
                </div>
                <div class="summary-divider"></div>
                <div class="summary-item summary-total">
                  <span>应付总额</span>
                  <span class="total-amount">¥{{ totalAmount }}</span>
                </div>
              </div>

              <a-button
                type="primary"
                size="large"
                long
                :loading="submitting"
                :disabled="!defaultAddress"
                @click="submitOrders"
                class="submit-btn"
              >
                提交订单 ({{ orderItems.length }}个商品)
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
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconLocation,
  IconEdit,
} from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "../../../utils/image-utils";
import { getErrorMessage } from "../../../utils/error-utils";
import AddressCard from "../../../components/data/AddressCard.vue";
import ConditionTag from "../../../components/data/ConditionTag.vue";
import EditAddressModal from "../../../components/data/EditAddressModal.vue";
import { getItemDetail, createOrder, getDefaultAddress, getAddressList, getCartList } from "../../../services/api";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const submitting = ref(false);
const orderItems = ref([]); // 改为数组，支持多商品
const defaultAddress = ref(null);
const showAddressModal = ref(false);

const CATEGORY_MAP = {
  digital: "数码", book: "教材", clothing: "服饰",
  daily: "生活", sport: "运动", instrument: "乐器", other: "其他",
  electronics: "数码", textbooks: "教材",
};

// 计算总金额
const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => {
    return sum + (item.item?.price || 0) * item.quantity;
  }, 0).toFixed(2);
});

// 计算总数量
const totalQuantity = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.quantity, 0);
});

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

async function loadOrderItems() {
  loading.value = true;
  
  try {
    const cartIdsStr = route.query.cartIds;
    
    if (!cartIdsStr) {
      // 兼容旧的单商品模式
      const itemId = route.params.id;
      if (itemId) {
        const res = await getItemDetail(itemId);
        orderItems.value = [{ cartId: null, item: res, quantity: 1 }];
      }
      return;
    }
    
    // 批量模式：从购物车加载多个商品
    const cartIds = cartIdsStr.split(',').map(id => parseInt(id)).filter(id => !isNaN(id));
    
    if (cartIds.length === 0) {
      Message.error("无效的购物车项");
      router.back();
      return;
    }
    
    // 获取购物车列表并筛选出选中的项
    const allCartItems = await getCartList();
    const selectedCartItems = allCartItems.filter(cartItem => 
      cartIds.includes(cartItem.id)
    );
    
    if (selectedCartItems.length === 0) {
      Message.error("未找到购物车项");
      router.back();
      return;
    }
    
    // 为每个购物车项加载完整的商品信息
    const itemsWithDetails = await Promise.all(
      selectedCartItems.map(async (cartItem) => {
        try {
          if (cartItem.item && cartItem.item.id) {
            // 如果已有完整信息，直接使用
            return { ...cartItem };
          }
          
          // 否则加载商品详情
          const itemDetail = await getItemDetail(cartItem.itemId);
          return { ...cartItem, item: itemDetail };
        } catch (e) {
          console.error(`加载商品 ${cartItem.itemId} 失败:`, e);
          return null;
        }
      })
    );
    
    // 过滤掉加载失败的项
    orderItems.value = itemsWithDetails.filter(item => item !== null);
    
    if (orderItems.value.length === 0) {
      Message.error("无法加载商品信息");
      router.back();
    }
    
  } catch (e) {
    console.error('[OrderConfirm] 加载失败:', e);
    Message.error(e.message || "加载订单信息失败");
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

async function submitOrders() {
  if (!defaultAddress.value) {
    Message.warning("请先选择收货地址");
    return;
  }

  submitting.value = true;
  try {
    const address = defaultAddress.value;
    let successCount = 0;
    
    // 批量创建订单（每个商品创建一个独立订单）
    for (const orderItem of orderItems.value) {
      try {
        // 验证必要字段
        if (!orderItem?.item?.id) {
          console.error('[OrderConfirm] 商品ID缺失:', orderItem);
          throw new Error('商品信息不完整');
        }

        // 校园线下交易：不需要收货地址，只需商品信息
        const orderData = {
          itemId: orderItem.item.id,
          quantity: orderItem.quantity || 1,
          note: '校园线下交易，请与卖家联系',
        };

        console.log('[OrderConfirm] 创建订单参数:', orderData);

        const result = await createOrder(orderData);
        console.log('[OrderConfirm] 订单创建成功:', result);
        
        successCount++;
      } catch (e) {
        console.error(`[OrderConfirm] 创建订单失败 (${orderItem?.item?.title || '未知商品'}):`, e);
        errors.push({
          item: orderItem?.item?.title || '未知商品',
          error: e.message || '创建失败'
        });
      }
    }
    
    if (successCount > 0) {
      Message.success(`成功创建 ${successCount} 个订单`);
      router.push("/portal/orders");
    } else {
      throw new Error("所有订单创建失败");
    }
  } catch (e) {
    const errorMsg = getErrorMessage(e);
    Message.error(errorMsg);
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadOrderItems();
  // 校园线下交易模式：无需加载收货地址
});
</script>

<style lang="scss" scoped>
.trade-notice {
  background: linear-gradient(135deg, #E8F3FF 0%, #F0F5FF 100%);
  border: 1px solid #D6E9FF;

  .notice-content {
    display: flex;
    gap: 16px;
    align-items: flex-start;
  }

  .notice-text {
    flex: 1;

    h4 {
      margin: 0 0 8px 0;
      color: #165DFF;
      font-size: 16px;
      font-weight: 600;
    }

    p {
      margin: 0 0 6px 0;
      color: #4E5969;
      font-size: 14px;
      line-height: 1.6;
    }

    .notice-tip {
      color: #86909C;
      font-size: 13px;
      padding-left: 12px;
      border-left: 3px solid #165DFF;
      background: white;
      padding: 8px 12px;
      border-radius: 4px;
      margin-top: 12px;
    }
  }
}
</style>
