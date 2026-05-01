<template>
  <div class="orders-page">
    <div class="orders-page__header">
      <h2>{{ isSeller ? '已售订单' : '我的订单' }}</h2>
    </div>

    <!-- 状态 Tab -->
    <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
      <a-tab-pane v-for="s in ORDER_STATUS_TABS" :key="s.value" :title="s.label" />
    </a-tabs>

    <!-- 订单列表 -->
    <a-spin :loading="loading" style="width: 100%">
      <div class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-card__header">
            <span class="order-card__id">订单号：{{ order.orderNo || order.id }}</span>
            <a-tag :color="STATUS_MAP[order.status]?.color || 'gray'" size="small">
              {{ STATUS_MAP[order.status]?.label || order.status }}
            </a-tag>
          </div>
          <div class="order-card__body" @click="goItem(order)">
            <img v-if="order.itemImage" :src="order.itemImage" class="order-card__img" />
            <div v-else class="order-card__img order-card__img--empty">📷</div>
            <div class="order-card__info">
              <div class="order-card__title">{{ order.itemTitle }}</div>
              <div class="order-card__meta">
                <span class="order-card__price">¥{{ order.totalAmount || order.price }}</span>
                <span class="order-card__time">{{ order.createdAt }}</span>
              </div>
              <div v-if="order.expressCompany || order.expressNo" class="order-card__express">
                <span v-if="order.expressCompany">{{ order.expressCompany }}</span>
                <span v-if="order.expressNo"> {{ order.expressNo }}</span>
              </div>
            </div>
          </div>
          <div class="order-card__actions">
            <!-- 待支付：买家可支付或取消 -->
            <a-button v-if="order.status === 'PENDING_PAYMENT' && !isSeller" type="primary" size="small" @click="handlePay(order)">立即支付</a-button>
            <a-button v-if="order.status === 'PENDING_PAYMENT' && !isSeller" size="small" @click="handleCancel(order)">取消订单</a-button>
            
            <!-- 已支付：卖家可发货 -->
            <a-button v-if="order.status === 'PAID' && isSeller" type="primary" size="small" @click="handleShip(order)">发货</a-button>
            
            <!-- 已发货：买家可确认收货或申请退款 -->
            <a-button v-if="order.status === 'SHIPPED' && !isSeller" type="primary" size="small" @click="handleConfirm(order)">确认收货</a-button>
            <a-button v-if="order.status === 'SHIPPED' && !isSeller" size="small" @click="handleRequestRefund(order)">申请退款</a-button>
            
            <!-- 退款中：卖家可同意或拒绝退款 -->
            <a-button v-if="order.status === 'REFUNDING' && isSeller" type="primary" size="small" status="success" @click="handleApproveRefund(order)">同意退款</a-button>
            <a-button v-if="order.status === 'REFUNDING' && isSeller" size="small" status="danger" @click="handleRejectRefund(order)">拒绝退款</a-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && orders.length === 0" class="orders-list__empty">
          <span class="orders-list__empty-icon">📦</span>
          <p>暂无订单</p>
        </div>
      </div>
    </a-spin>

    <!-- 分页 -->
    <div v-if="pagination.total > pagination.pageSize" class="orders-page__pagination">
      <a-pagination
        v-model:current="pagination.current"
        :total="pagination.total"
        :page-size="pagination.pageSize"
        size="small"
        @change="handlePageChange"
      />
    </div>

    <!-- 发货弹窗 -->
    <a-modal v-model:visible="shipModalVisible" title="发货" @ok="submitShip">
      <a-form :model="shipForm" layout="vertical">
        <a-form-item label="快递公司">
          <a-input v-model="shipForm.expressCompany" placeholder="请输入快递公司" />
        </a-form-item>
        <a-form-item label="快递单号">
          <a-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import {
  getMyOrders,
  confirmOrder,
  payOrder,
  shipOrder,
  cancelOrder,
  requestRefund,
  approveRefund,
  rejectRefund,
} from "../../services/api";
import { getCurrentUser } from "commonprovide/auth-sdk";
import { ORDER_STATUS_TABS, STATUS_MAP } from "./const";

const router = useRouter();
const currentUser = getCurrentUser();
const isSeller = computed(() => (currentUser?.roles || []).includes("SELLER"));

const orders = ref([]);
const loading = ref(false);
const activeStatus = ref("");
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

// 发货弹窗相关
const shipModalVisible = ref(false);
const currentShipOrder = ref(null);
const shipForm = ref({
  expressCompany: "",
  expressNo: "",
});

async function loadData() {
  loading.value = true;
  try {
    const params = {
      status: activeStatus.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    const res = await getMyOrders(params);
    orders.value = res?.orders || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[Orders] load error:", e);
  } finally {
    loading.value = false;
  }
}

function handleTabChange() {
  pagination.value.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

function goItem(order) {
  if (order.itemId) router.push(`/item/${order.itemId}`);
}

function handlePay(order) {
  Modal.confirm({
    title: "模拟支付",
    content: "确认支付订单吗？",
    onOk: async () => {
      try {
        await payOrder(order.id);
        Message.success("支付成功");
        loadData();
      } catch (e) {
        Message.error(e.message || "支付失败");
      }
    },
  });
}

function handleConfirm(order) {
  Modal.confirm({
    title: "确认收货",
    content: "确认已收到商品吗？",
    onOk: async () => {
      try {
        await confirmOrder(order.id);
        Message.success("已确认收货");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

function handleShip(order) {
  currentShipOrder.value = order;
  shipForm.value = { expressCompany: "", expressNo: "" };
  shipModalVisible.value = true;
}

async function submitShip() {
  if (!shipForm.value.expressCompany || !shipForm.value.expressNo) {
    Message.error("请填写快递公司和快递单号");
    return;
  }
  try {
    await shipOrder(currentShipOrder.value.id, shipForm.value);
    Message.success("发货成功");
    shipModalVisible.value = false;
    loadData();
  } catch (e) {
    Message.error(e.message || "发货失败");
  }
}

function handleCancel(order) {
  Modal.confirm({
    title: "取消订单",
    content: "确认取消该订单吗？",
    onOk: async () => {
      try {
        await cancelOrder(order.id);
        Message.success("订单已取消");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

function handleRequestRefund(order) {
  Modal.confirm({
    title: "申请退款",
    content: "确认申请退款吗？",
    onOk: async () => {
      try {
        await requestRefund(order.id);
        Message.success("退款申请已提交");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

function handleApproveRefund(order) {
  Modal.confirm({
    title: "同意退款",
    content: "确认同意退款吗？",
    onOk: async () => {
      try {
        await approveRefund(order.id);
        Message.success("退款成功");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

function handleRejectRefund(order) {
  Modal.confirm({
    title: "拒绝退款",
    content: "确认拒绝退款吗？",
    onOk: async () => {
      try {
        await rejectRefund(order.id);
        Message.success("已拒绝退款申请");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.orders-page {
  background: #fff;
  border-radius: 16px;
  padding: 24px;

  &__header {
    margin-bottom: 16px;
    h2 { margin: 0; font-size: 20px; font-weight: 700; }
  }

  &__pagination {
    margin-top: 20px;
    text-align: center;
  }
}

.order-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06); }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__id {
    font-size: 12px;
    color: #86909c;
  }

  &__body {
    display: flex;
    gap: 12px;
    cursor: pointer;
  }

  &__img {
    width: 72px;
    height: 72px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f2f3f5;
      color: #c9cdd4;
      font-size: 28px;
    }
  }

  &__info { flex: 1; min-width: 0; }

  &__title {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__price { font-weight: 700; color: #f53f3f; }
  &__time { font-size: 12px; color: #86909c; }

  &__express {
    margin-top: 4px;
    font-size: 12px;
    color: #666;
  }

  &__actions {
    margin-top: 12px;
    text-align: right;
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
}

.orders-list__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #86909c;

  &-icon { font-size: 48px; color: #c9cdd4; margin-bottom: 12px; }
}
</style>
