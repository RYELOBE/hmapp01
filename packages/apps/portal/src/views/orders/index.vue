<template>
  <div class="orders-page">
    <div class="orders-page__header">
      <h2>{{ isSeller ? '已售订单' : '我的订单' }}</h2>
    </div>

    <!-- 状态 Tab -->
    <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
      <a-tab-pane v-for="s in ORDER_STATUS" :key="s.value" :title="s.label" />
    </a-tabs>

    <!-- 订单列表 -->
    <a-spin :loading="loading" style="width: 100%">
      <div class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-card__header">
            <span class="order-card__id">订单号：{{ order.id }}</span>
            <a-tag :color="STATUS_MAP[order.status]?.color || 'gray'" size="small">
              {{ STATUS_MAP[order.status]?.label || order.status }}
            </a-tag>
          </div>
          <div class="order-card__body" @click="goItem(order)">
            <img v-if="order.itemCoverUrl" :src="order.itemCoverUrl" class="order-card__img" />
            <div v-else class="order-card__img order-card__img--empty">📷</div>
            <div class="order-card__info">
              <div class="order-card__title">{{ order.itemTitle }}</div>
              <div class="order-card__meta">
                <span class="order-card__price">¥{{ order.itemPrice }}</span>
                <span class="order-card__time">{{ order.createdAt }}</span>
              </div>
            </div>
          </div>
          <div class="order-card__actions">
            <a-button
              v-if="order.status === 'SHIPPED' && !isSeller"
              type="primary"
              size="small"
              @click="handleConfirm(order)"
            >
              确认收货
            </a-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && orders.length === 0" class="orders-list__empty">
          <span class="orders-list__empty-icon">✅</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import { getMyOrders, confirmOrder } from "../../services/api";
import { getCurrentUser } from "commonprovide/auth-sdk";
import { ORDER_STATUS, STATUS_MAP } from "./const";

const router = useRouter();
const currentUser = getCurrentUser();
const isSeller = computed(() => (currentUser?.roles || []).includes("SELLER"));

const orders = ref([]);
const loading = ref(false);
const activeStatus = ref("");
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

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

  &__actions {
    margin-top: 12px;
    text-align: right;
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
