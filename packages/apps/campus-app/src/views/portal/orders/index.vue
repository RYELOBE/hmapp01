<template>
  <div class="orders-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">{{ isSeller ? '已售订单' : '我的订单' }}</h2>
    </div>

    <a-card :bordered="false" class="orders-card">
      <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
        <a-tab-pane v-for="s in ORDER_STATUS_TABS" :key="s.value" :title="s.label">
          <template #title>
            {{ s.label }}
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
            @click="goItem(order)"
            @action="handleAction"
          />
        </div>

        <a-empty v-else-if="!loading" description="暂无订单记录">
          <template #image>
            <icon-list size="64" />
          </template>
          <a-button type="primary" @click="$router.push('/portal/home')">
            去逛逛
          </a-button>
        </a-empty>
      </a-spin>

      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          show-total
          show-page-size
          size="small"
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>

    <a-modal v-model:visible="shipModalVisible" title="发货" @ok="submitShip">
      <a-form :model="shipForm" layout="vertical">
        <a-form-item label="快递公司" required>
          <a-input v-model="shipForm.expressCompany" placeholder="请输入快递公司" />
        </a-form-item>
        <a-form-item label="快递单号" required>
          <a-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import { IconArrowLeft, IconList } from "@arco-design/web-vue/es/icon";
import OrderCard from "../../../components/data/OrderCard.vue";
import {
  getMyOrders,
  confirmOrder,
  payOrder,
  shipOrder,
  cancelOrder,
  requestRefund,
  approveRefund,
  rejectRefund,
} from "../../../services/api";
import { getCurrentUser } from "../../../services/auth";
import { ORDER_STATUS_TABS } from "./const";

const router = useRouter();
const route = useRoute();
const currentUser = getCurrentUser();
const isSeller = computed(() => (currentUser?.roles || []).includes("SELLER"));

const orders = ref([]);
const loading = ref(false);
const activeStatus = ref(route.query.tab || "");
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

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
    Message.error(e.message || "加载失败");
  } finally {
    loading.value = false;
  }
}

function handleTabChange(key) {
  activeStatus.value = key;
  pagination.value.current = 1;
  router.replace({ query: { ...route.query, tab: key || undefined } });
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

function handlePageSizeChange(pageSize) {
  pagination.value.pageSize = pageSize;
  pagination.value.current = 1;
  loadData();
}

function goItem(order) {
  if (order.itemId) router.push(`/portal/item/${order.itemId}`);
}

async function handleAction(action, order) {
  switch (action) {
    case "pay":
      Modal.confirm({
        title: "确认支付",
        content: "确定要支付此订单吗？",
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
      break;

    case "cancel":
      Modal.confirm({
        title: "取消订单",
        content: "确定要取消此订单吗？",
        onOk: async () => {
          try {
            await cancelOrder(order.id);
            Message.success("订单已取消");
            loadData();
          } catch (e) {
            Message.error(e.message || "取消失败");
          }
        },
      });
      break;

    case "confirm":
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
      break;

    case "refund":
      Modal.confirm({
        title: "申请退款",
        content: "确定要申请退款吗？",
        onOk: async () => {
          try {
            await requestRefund(order.id);
            Message.success("退款申请已提交");
            loadData();
          } catch (e) {
            Message.error(e.message || "申请退款失败");
          }
        },
      });
      break;

    case "ship":
      currentShipOrder.value = order;
      shipForm.value = { expressCompany: "", expressNo: "" };
      shipModalVisible.value = true;
      break;

    case "approve-refund":
      Modal.confirm({
        title: "同意退款",
        content: "确定同意退款吗？",
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
      break;

    case "reject-refund":
      Modal.confirm({
        title: "拒绝退款",
        content: "确定拒绝退款吗？",
        onOk: async () => {
          try {
            await rejectRefund(order.id);
            Message.success("已拒绝退款");
            loadData();
          } catch (e) {
            Message.error(e.message || "操作失败");
          }
        },
      });
      break;
  }
}

async function submitShip() {
  if (!shipForm.value.expressCompany || !shipForm.value.expressNo) {
    Message.warning("请填写完整信息");
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

onMounted(loadData);
</script>

<style lang="scss" scoped>
.orders-page {
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
