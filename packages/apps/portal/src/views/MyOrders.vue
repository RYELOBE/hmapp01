<template>
  <div class="my-orders-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">我的订单</h2>
    </div>

    <a-card :bordered="false" class="orders-card">
      <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
        <a-tab-pane
          v-for="tab in orderTabs"
          :key="tab.key"
          :title="tab.title"
        >
          <template #title>
            {{ tab.label }}
            <a-badge
              v-if="tab.count > 0"
              :count="tab.count"
              :max-count="99"
            />
          </template>
        </a-tab-pane>
      </a-tabs>

      <a-spin :loading="loading" style="width: 100%">
        <div v-if="orders.length > 0" class="orders-list">
          <OrderCard
            v-for="order in orders"
            :key="order.id"
            :order="order"
            clickable
            @click="viewOrderDetail(order)"
            @action="handleOrderAction"
          />
        </div>

        <a-empty v-else-if="!loading" description="暂无订单记录">
          <template #image>
            <icon-list size="64" />
          </template>
          <a-button type="primary" @click="$router.push('/home')">
            去逛逛
          </a-button>
        </a-empty>
      </a-spin>

      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <a-pagination
          :current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          show-total
          show-page-size
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import { IconArrowLeft, IconList } from "@arco-design/web-vue/es/icon";
import OrderCard from "../components/OrderCard";
import { getMyOrders, payOrder, cancelOrder, confirmOrder, shipOrder, requestRefund } from "../services/api";

const router = useRouter();
const loading = ref(false);
const orders = ref([]);
const activeTab = ref("all");

const orderTabs = ref([
  { key: "all", label: "全部", count: 0 },
  { key: "PENDING_PAYMENT", label: "待付款", count: 0 },
  { key: "PAID", label: "待发货", count: 0 },
  { key: "SHIPPED", label: "待收货", count: 0 },
  { key: "COMPLETED", label: "已完成", count: 0 },
  { key: "CANCELLED", label: "已取消", count: 0 },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

async function loadOrders() {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    if (activeTab.value && activeTab.value !== "all") {
      params.status = activeTab.value;
    }

    const result = await getMyOrders(params);
    orders.value = result.orders || result.items || result.data || [];
    pagination.total = result.total || result.totalCount || 0;

    updateTabCounts(result);
  } catch (error) {
    Message.error(error.message || "加载订单失败");
  } finally {
    loading.value = false;
  }
}

function updateTabCounts(data) {
  if (!data) return;

  const allOrders = data.allOrders || [];
  orderTabs.value[0].count = allOrders.length;

  const statusMap = {
    PENDING_PAYMENT: 1,
    PAID: 2,
    SHIPPED: 3,
    COMPLETED: 4,
    CANCELLED: 5,
  };

  Object.keys(statusMap).forEach((status) => {
    const index = statusMap[status];
    if (data.statusCounts && data.statusCounts[status] !== undefined) {
      orderTabs.value[index].count = data.statusCounts[status];
    }
  });
}

function handleTabChange(key) {
  activeTab.value = key;
  pagination.current = 1;
  loadOrders();
}

function handlePageChange(page) {
  pagination.current = page;
  loadOrders();
}

function handlePageSizeChange(pageSize) {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  loadOrders();
}

function viewOrderDetail(order) {
  router.push(`/orders/${order.id}`);
}

async function handleOrderAction(action, order) {
  switch (action) {
    case "pay":
      await handlePay(order);
      break;
    case "cancel":
      await handleCancel(order);
      break;
    case "refund":
      await handleRefund(order);
      break;
    case "confirm":
      await handleConfirm(order);
      break;
    case "ship":
      await handleShip(order);
      break;
    case "review":
      router.push(`/reviews/create/${order.id}`);
      break;
    case "rebuy":
      router.push(`/item/${order.itemId}`);
      break;
    case "delete":
      await handleDelete(order);
      break;
  }
}

async function handlePay(order) {
  Modal.confirm({
    title: '确认支付',
    content: `订单金额：¥${order.totalAmount || order.price || '0.00'}`,
    okText: '立即支付',
    onOk: async () => {
      try {
        await payOrder(order.id);
        Message.success("支付成功");
        loadOrders();
      } catch (error) {
        Message.error(error.message || "支付失败");
      }
    },
  });
}

async function handleCancel(order) {
  Modal.confirm({
    title: "取消订单",
    content: "确定要取消此订单吗？",
    onOk: async () => {
      try {
        await cancelOrder(order.id);
        Message.success("订单已取消");
        loadOrders();
      } catch (error) {
        Message.error(error.message || "取消失败");
      }
    },
  });
}

async function handleRefund(order) {
  Modal.confirm({
    title: "申请退款",
    content: "确定要申请退款吗？",
    okText: "申请退款",
    onOk: async () => {
      try {
        const refundData = {
          reason: '用户主动申请退款',
          description: `订单${order.id}申请退款`,
        };
        await requestRefund(order.id, refundData);
        Message.success("退款申请已提交");
        loadOrders();
      } catch (error) {
        Message.error(error.message || "申请退款失败");
      }
    },
  });
}

async function handleConfirm(order) {
  Modal.confirm({
    title: "确认收货",
    content: "确认已收到商品？确认后订单将完成。",
    okText: "确认收货",
    onOk: async () => {
      try {
        await confirmOrder(order.id);
        Message.success("已确认收货");
        loadOrders();
      } catch (error) {
        Message.error(error.message || "操作失败");
      }
    },
  });
}

async function handleShip(order) {
  Modal.confirm({
    title: '发货',
    content: '确定要发货吗？请确保已填写快递信息。',
    okText: '确认发货',
    onOk: async () => {
      try {
        const shipData = {
          expressCompany: order.expressCompany || '顺丰速运',
          expressNo: order.expressNo || '',
        };
        await shipOrder(order.id, shipData);
        Message.success("发货成功");
        loadOrders();
      } catch (error) {
        Message.error(error.message || "发货失败");
      }
    },
  });
}

async function handleDelete(order) {
  Modal.confirm({
    title: "删除订单",
    content: "确定要删除此订单记录吗？",
    okText: "删除",
    okProps: { status: "danger" },
    onOk: async () => {
      orders.value = orders.value.filter((o) => o.id !== order.id);
      Message.success("订单记录已删除");
    },
  });
}

onMounted(loadOrders);
</script>

<style lang="scss" scoped>
.my-orders-page {
  padding: 24px;
  max-width: 1000px;
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

.orders-card {
  border-radius: 12px;

  :deep(.arco-tabs-nav) {
    padding: 0 8px;
  }

  :deep(.arco-tab) {
    font-weight: 500;
  }

  :deep(.arco-tabs-content) {
    padding-top: 16px;
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
}

:deep(.arco-empty) {
  padding: 80px 20px;
}
</style>
