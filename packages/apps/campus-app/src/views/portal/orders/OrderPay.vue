<template>
  <div class="order-pay-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">订单支付</h2>
    </div>

    <a-spin :loading="loading">
      <div v-if="order" class="pay-content">
        <!-- 左侧主内容 -->
        <div class="pay-main">
          <!-- 订单信息 -->
          <a-card title="订单信息" :bordered="false" class="info-card">
            <div class="order-info">
              <img v-if="order.itemImage" :src="order.itemImage" class="item-image" />
              <div v-else class="item-image item-image--empty">📷</div>
              <div class="item-detail">
                <h3 class="item-title">{{ order.itemTitle }}</h3>
                <div class="order-meta">
                  <span>订单号：{{ order.orderNo || order.id }}</span>
                  <span>数量：{{ order.quantity || 1 }}</span>
                </div>
              </div>
              <div class="order-amount">
                <span class="amount-label">订单金额</span>
                <span class="amount-value">¥{{ (order.price || 0).toFixed(2) }}</span>
              </div>
            </div>
          </a-card>

          <!-- 线下交易提示 -->
          <a-card :bordered="false" class="notice-card">
            <div class="notice-icon">
              <icon-info-circle-fill style="color: #165DFF; font-size: 48px;" />
            </div>
            <div class="notice-content">
              <h3>校园线下交易</h3>
              <p>本平台为校园闲置物品流转平台，采用线下当面交易方式。</p>
              <p>下单后请联系卖家，约定时间地点进行面对面交易和支付。</p>
            </div>
          </a-card>

          <!-- 操作按钮 -->
          <div class="action-bar">
            <a-button size="large" @click="cancelOrder">
              取消订单
            </a-button>
            <a-button type="primary" size="large" :loading="paying" @click="confirmPay">
              确认支付
            </a-button>
          </div>
        </div>

        <!-- 右侧侧边栏 -->
        <div class="pay-sidebar">
          <!-- 卖家信息 -->
          <a-card title="联系卖家" :bordered="false" class="seller-card">
            <div class="seller-info">
              <a-avatar :size="56" :style="{ backgroundColor: '#165DFF' }">
                {{ (order.sellerName || '卖')[0]?.toUpperCase() }}
              </a-avatar>
              <div class="seller-detail">
                <div class="seller-name">{{ order.sellerName || '卖家' }}</div>
                <div class="seller-contact" v-if="order.sellerPhone">
                  <icon-phone /> {{ order.sellerPhone }}
                </div>
              </div>
            </div>
            <a-button type="primary" long size="large" @click="contactSeller" style="margin-top: 16px;">
              <template #icon><icon-message /></template>
              联系卖家
            </a-button>
            <div class="contact-tips">
              <p>💡 温馨提示</p>
              <ul>
                <li>建议选择人多的公共场所进行交易</li>
                <li>交易前请确认商品与描述一致</li>
                <li>完成交易后请点击"确认支付"</li>
              </ul>
            </div>
          </a-card>
        </div>
      </div>

      <a-empty v-else-if="!loading" description="订单不存在">
        <a-button type="primary" @click="$router.push('/portal/orders')">
          返回订单列表
        </a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import { IconArrowLeft, IconInfoCircleFill, IconMessage, IconPhone } from "@arco-design/web-vue/es/icon";
import { getOrderDetail, payOrder, cancelOrder as cancelOrderApi } from "../../../services/api";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const paying = ref(false);
const order = ref(null);

async function loadOrder() {
  loading.value = true;
  try {
    const res = await getOrderDetail(route.params.id);
    order.value = res?.data || res;
    
    // 如果已支付，跳转到订单列表
    if (order.value?.status !== "PENDING_PAYMENT") {
      Message.info("该订单已处理");
      router.push("/portal/orders");
    }
  } catch (e) {
    Message.error(e.message || "加载订单失败");
  } finally {
    loading.value = false;
  }
}

function contactSeller() {
  Message.info("请在消息中心与卖家联系");
  // TODO: 跳转到消息页面或打开聊天窗口
}

async function confirmPay() {
  Modal.confirm({
    title: "确认支付",
    content: "请确认您已与卖家完成线下交易，确定要标记为已支付吗？",
    okText: "确认支付",
    cancelText: "取消",
    onOk: async () => {
      paying.value = true;
      try {
        await payOrder(order.value.id);
        Message.success("支付成功，请等待卖家确认交易");
        router.push("/portal/orders");
      } catch (e) {
        Message.error(e.message || "支付失败");
      } finally {
        paying.value = false;
      }
    },
  });
}

async function cancelOrder() {
  Modal.confirm({
    title: "取消订单",
    content: "确定要取消此订单吗？",
    okText: "确认取消",
    cancelText: "返回",
    onOk: async () => {
      try {
        await cancelOrderApi(order.value.id);
        Message.success("订单已取消");
        router.push("/portal/orders");
      } catch (e) {
        Message.error(e.message || "取消失败");
      }
    },
  });
}

onMounted(loadOrder);
</script>

<style lang="scss" scoped>
.order-pay-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f7fbff;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    color: #1d2129;
  }
}

.pay-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.pay-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pay-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;

  @media (max-width: 991px) {
    order: -1;
  }
}

.info-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-card-header) {
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;
  }

  .order-info {
    display: flex;
    gap: 20px;
    align-items: flex-start;
    padding: 8px 0;
  }

  .item-image {
    width: 140px;
    height: 140px;
    object-fit: cover;
    border-radius: 12px;
    border: 1px solid #f0f0f0;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f6f8;
      font-size: 48px;
    }
  }

  .item-detail {
    flex: 1;
    min-width: 0;

    .item-title {
      margin: 0 0 12px 0;
      font-size: 18px;
      font-weight: 600;
      line-height: 1.4;
      color: #1d2129;
    }

    .order-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      color: #86909c;
      font-size: 14px;
    }
  }

  .order-amount {
    text-align: right;
    flex-shrink: 0;
    padding: 16px 20px;
    background: linear-gradient(135deg, #fff7e6 0%, #fffbe8 100%);
    border-radius: 12px;
    min-width: 160px;

    .amount-label {
      display: block;
      color: #86909c;
      font-size: 13px;
      margin-bottom: 8px;
    }

    .amount-value {
      color: #f53f3f;
      font-size: 28px;
      font-weight: 700;
    }
  }
}

.notice-card {
  background: linear-gradient(135deg, #e8f3ff 0%, #f0f7ff 100%);
  border: 1px solid #d6e9ff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.08);

  :deep(.arco-card-body) {
    display: flex;
    gap: 20px;
    align-items: flex-start;
    padding: 24px;
  }

  .notice-icon {
    flex-shrink: 0;
    padding: 12px;
    background: white;
    border-radius: 50%;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
  }

  .notice-content {
    flex: 1;

    h3 {
      margin: 0 0 12px 0;
      color: #165dff;
      font-size: 20px;
      font-weight: 600;
    }

    p {
      margin: 0 0 8px 0;
      color: #4e5969;
      font-size: 15px;
      line-height: 1.7;
    }
  }
}

.seller-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 24px;

  :deep(.arco-card-header) {
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;
  }

  .seller-info {
    display: flex;
    gap: 16px;
    align-items: center;
    padding: 20px;
    background: linear-gradient(135deg, #f7fbff 0%, #f0f7ff 100%);
    border-radius: 12px;
    margin-bottom: 16px;
  }

  .seller-detail {
    flex: 1;

    .seller-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 6px;
      color: #1d2129;
    }

    .seller-contact {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #86909c;
      font-size: 14px;
    }
  }

  .contact-tips {
    padding: 20px;
    background: linear-gradient(135deg, #fff7e6 0%, #fffbe8 100%);
    border-radius: 12px;
    border-left: 4px solid #ff7d00;

    p {
      margin: 0 0 12px 0;
      font-weight: 600;
      color: #ff7d00;
      font-size: 15px;
    }

    ul {
      margin: 0;
      padding-left: 20px;
      color: #4e5969;
      font-size: 14px;
      line-height: 2;
    }
  }
}

.action-bar {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  button {
    flex: 1;
    height: 56px;
    font-size: 17px;
    font-weight: 500;
    border-radius: 12px;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .order-pay-page {
    padding: 16px;
  }

  .page-header {
    padding: 12px 16px;
    margin-bottom: 16px;

    .page-title {
      font-size: 18px;
    }
  }

  .info-card {
    .order-info {
      flex-direction: column;
    }

    .item-image {
      width: 100%;
      height: 200px;
    }

    .order-amount {
      width: 100%;
      text-align: center;
    }
  }

  .notice-card {
    :deep(.arco-card-body) {
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
  }

  .seller-card {
    position: static;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;

    button {
      width: 100%;
    }
  }
}

@media (max-width: 576px) {
  .pay-content {
    gap: 16px;
  }

  .info-card .item-image {
    height: 160px;
  }

  .seller-info {
    flex-direction: column;
    text-align: center;
  }
}
</style>
