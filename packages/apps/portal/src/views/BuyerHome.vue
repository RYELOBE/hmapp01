<template>
  <div class="buyer-home">
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="user-avatar">
          <icon-user size="48" />
        </div>
        <div class="user-info">
          <h1 class="welcome-text">欢迎回来，{{ userInfo.nickname || '用户' }}！</h1>
          <p class="welcome-subtitle">今天也是购物的好日子~</p>
        </div>
      </div>
    </div>

    <div class="quick-entry-grid">
      <div
        v-for="entry in quickEntries"
        :key="entry.key"
        class="quick-entry-card"
        @click="$router.push(entry.path)"
      >
        <div class="entry-icon" :style="{ background: entry.bgColor }">
          <component :is="entry.icon" size="28" />
        </div>
        <span class="entry-label">{{ entry.label }}</span>
      </div>
    </div>

    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="累计订单" :value="statistics.totalOrders" :value-from="0">
            <template #prefix><icon-list /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="累计消费" :value="statistics.totalSpent" prefix="¥" :precision="2" :value-from="0">
            <template #prefix><icon-dollar /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="收藏商品" :value="statistics.favoriteCount" :value-from="0">
            <template #prefix><icon-heart /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6">
        <a-card class="stat-card" :bordered="false">
          <a-statistic title="收货地址" :value="statistics.addressCount" :value-from="0">
            <template #prefix><icon-location /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="最近订单" :bordered="false" class="recent-orders-card">
      <template #extra>
        <a-button type="text" @click="$router.push('/orders')">
          查看全部
          <template #icon><icon-right /></template>
        </a-button>
      </template>

      <a-spin :loading="ordersLoading">
        <div v-if="recentOrders.length > 0" class="recent-orders-list">
          <div
            v-for="order in recentOrders"
            :key="order.id"
            class="recent-order-item"
            @click="$router.push(`/orders/${order.id}`)"
          >
            <div class="order-info">
              <img
                v-if="getOrderImage(order)"
                :src="getOrderImage(order)"
                class="order-image"
              />
              <div v-else class="order-image order-image--empty">📷</div>
              <div class="order-detail">
                <div class="order-title">{{ order.itemTitle || order.title || '商品' }}</div>
                <div class="order-time">{{ formatTime(order.createdAt) }}</div>
              </div>
            </div>
            <div class="order-right">
              <div class="order-amount">¥{{ (order.totalAmount || order.amount || 0).toFixed(2) }}</div>
              <StatusTag :status="order.status || order.orderStatus" type="order" size="small" />
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无订单记录">
          <template #image>
            <icon-list size="48" />
          </template>
          <a-button type="primary" @click="$router.push('/home')">去逛逛</a-button>
        </a-empty>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import {
  IconUser,
  IconList,
  IconHeart,
  IconShoppingCart,
  IconLocation,
  IconDollar,
  IconRight,
} from "@arco-design/web-vue/es/icon";
import StatusTag from "commonprovide/status-tag";
import { getMyOrders, getFavoriteCount, getAddressList, getCartCount } from "../services/api";

const userInfo = reactive({
  nickname: "",
  avatar: "",
});

const statistics = reactive({
  totalOrders: 0,
  totalSpent: 0,
  favoriteCount: 0,
  addressCount: 0,
});

const recentOrders = ref([]);
const ordersLoading = ref(false);

const quickEntries = [
  { key: "orders", label: "我的订单", icon: "IconList", path: "/orders", bgColor: "linear-gradient(135deg, #165DFF 0%, #4080FF 100%)" },
  { key: "favorites", label: "我的收藏", icon: "IconHeart", path: "/favorites", bgColor: "linear-gradient(135deg, #F53F3F 0%, #FF7875 100%)" },
  { key: "cart", label: "购物车", icon: "IconShoppingCart", path: "/cart", bgColor: "linear-gradient(135deg, #F7BA1E 0%, #FFC53D 100%)" },
  { key: "addresses", label: "收货地址", icon: "IconLocation", path: "/addresses", bgColor: "linear-gradient(135deg, #00B42A 0%, #36CFC9 100%)" },
];

function getOrderImage(order) {
  const item = order.item || order.items?.[0];
  if (!item) return null;
  const urls = item.imageUrls || item.images || [];
  if (typeof urls === "string") {
    try { return JSON.parse(urls)[0]; } catch { return urls; }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : null;
}

function formatTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadStatistics() {
  try {
    const [ordersRes, favoritesRes, addressesRes] = await Promise.all([
      getMyOrders({ pageSize: 100 }).catch(() => ({ items: [] })),
      getFavoriteCount().catch(() => ({ count: 0 })),
      getAddressList().catch(() => []),
    ]);

    statistics.totalOrders = ordersRes.total || ordersRes.orders?.length || 0;
    statistics.favoriteCount = favoritesRes.count || favoritesRes.total || 0;
    statistics.addressCount = Array.isArray(addressesRes) ? addressesRes.length : 0;

    const allOrders = ordersRes.orders || ordersRes.items || [];
    statistics.totalSpent = allOrders.reduce((sum, o) => sum + (o.totalAmount || o.amount || 0), 0);
  } catch (e) {
    console.error("加载统计数据失败", e);
  }
}

async function loadRecentOrders() {
  ordersLoading.value = true;
  try {
    const res = await getMyOrders({ pageSize: 3 });
    recentOrders.value = res.orders || res.items || res.data || [];
  } catch (e) {
    console.error("加载最近订单失败", e);
  } finally {
    ordersLoading.value = false;
  }
}

onMounted(() => {
  loadStatistics();
  loadRecentOrders();
});
</script>

<style lang="scss" scoped>
.buyer-home {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
}

.welcome-banner {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #69b1ff 100%);
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 4px 20px rgba(22, 93, 255, 0.25);

  .banner-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .user-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    -webkit-backdrop-filter: blur(10px);
    backdrop-filter: blur(10px);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .user-info {
    .welcome-text {
      margin: 0;
      font-size: 26px;
      font-weight: 700;
      letter-spacing: 1px;
    }

    .welcome-subtitle {
      margin: 8px 0 0;
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.quick-entry-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  }

  .entry-icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
  }

  .entry-label {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
  }
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.1);
  }

  :deep(.arco-card-body) {
    padding: 20px;
  }

  :deep(.arco-statistic) {
    .arco-statistic-title {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 8px;
    }

    .arco-statistic-value {
      font-size: 24px;
      font-weight: 600;
      color: #1d2129;
    }
  }
}

.recent-orders-card {
  border-radius: 12px;

  :deep(.arco-card-head-title) {
    font-size: 16px;
    font-weight: 600;
  }
}

.recent-orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f0f5ff;
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.08);
  }

  .order-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
  }

  .order-image {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      font-size: 24px;
    }
  }

  .order-detail {
    min-width: 0;

    .order-title {
      font-size: 14px;
      font-weight: 500;
      color: #1d2129;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 280px;
    }

    .order-time {
      font-size: 12px;
      color: #86909c;
      margin-top: 4px;
    }
  }

  .order-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
    flex-shrink: 0;
  }

  .order-amount {
    font-size: 18px;
    font-weight: 700;
    color: #f53f3f;
  }
}

:deep(.arco-empty) {
  padding: 60px 20px;
}
</style>
