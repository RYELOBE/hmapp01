<template>
  <div class="favorites-page">
    <!-- 蓝色渐变头部区域 -->
    <div class="favorites-header">
      <div class="header-content">
        <h1 class="header-title">我的收藏页</h1>
        <p class="header-subtitle">这里汇集了您心仪的所有宝贝，随时查看或管理</p>
        <a-button type="primary" shape="round" class="header-btn" @click="$router.push('/')">
          去逛逛
        </a-button>
      </div>
    </div>

    <!-- 快捷操作按钮组 -->
    <div class="quick-actions">
      <div class="action-card" @click="handleViewItems">
        <div class="action-icon">
          <icon-eye :size="32" />
        </div>
        <span class="action-text">查看商品</span>
      </div>

      <div class="action-card" @click="handleBatchAddCart">
        <div class="action-icon">
          <icon-shopping-cart :size="32" />
        </div>
        <span class="action-text">添加到购物车</span>
      </div>

      <div class="action-card" @click="handleContactSeller">
        <div class="action-icon">
          <icon-message :size="32" />
        </div>
        <span class="action-text">联系卖家</span>
      </div>

      <div class="action-card action-card--danger" @click="handleBatchUnfavorite">
        <div class="action-icon">
          <icon-heart-fill :size="32" />
        </div>
        <span class="action-text">取消收藏</span>
      </div>
    </div>

    <!-- 收藏分类筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <a-tabs v-model:activeKey="activeCategory" @change="handleCategoryChange">
          <a-tab-pane key="all" title="全部">
            <template #title>
              <span class="tab-title">全部</span>
            </template>
          </a-tab-pane>
          <a-tab-pane key="digital" title="数码产品">
            <template #title>
              <span class="tab-title">数码产品</span>
            </template>
          </a-tab-pane>
          <a-tab-pane key="books" title="图书教材">
            <template #title>
              <span class="tab-title">图书教材</span>
            </template>
          </a-tab-pane>
          <a-tab-pane key="home" title="家居日用">
            <template #title>
              <span class="tab-title">家居日用</span>
            </template>
          </a-tab-pane>
        </a-tabs>
      </div>

      <div class="search-box">
        <a-input-search
          v-model="searchKeyword"
          placeholder="搜索收藏的商品"
          style="width: 280px;"
          allow-clear
          @search="handleSearch"
        />
      </div>
    </div>

    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">我的收藏</h2>
      <span class="favorite-count">共 {{ favorites.length }} 件收藏</span>

      <template v-if="favorites.length > 0 && batchMode">
        <a-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleSelectAll">
          全选
        </a-checkbox>
        <a-button type="primary" status="danger" size="small" :disabled="selectedCount === 0" @click="batchUnfavorite">
          批量取消收藏 ({{ selectedCount }})
        </a-button>
        <a-button type="outline" size="small" :disabled="selectedCount === 0" @click="batchAddToCart">
          加入购物车
        </a-button>
        <a-button type="text" size="small" @click="batchMode = false">
          取消批量操作
        </a-button>
      </template>

      <a-button v-else-if="favorites.length > 0" type="text" size="small" @click="batchMode = true">
        批量管理
      </a-button>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="selectedItems.length > 0" class="batch-toolbar">
      <a-checkbox
        :checked="isAllSelected"
        @change="handleSelectAll"
      >
        全选
      </a-checkbox>
      <span class="batch-info">已选 {{ selectedItems.length }} 项</span>
      <div class="batch-actions">
        <a-button size="small" type="primary" @click="handleBatchAddCart">
          批量加购
        </a-button>
        <a-button size="small" status="danger" @click="handleBatchUnfavorite">
          批量取消收藏
        </a-button>
      </div>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="filteredFavorites.length > 0" class="favorites-list">
        <div
          v-for="item in filteredFavorites"
          :key="item.id"
          class="favorite-item"
        >
          <a-checkbox
            :checked="selectedItems.includes(item.id)"
            @change="(val) => handleItemSelect(item.id, val)"
            class="item-checkbox"
          />

          <div class="item-image">
            <img :src="getItemImage(item.item) || defaultImage" :alt="item.item?.title" />
          </div>

          <div class="item-info">
            <h3 class="item-title">{{ item.item?.title || '商品已下架' }}</h3>
            <p class="item-price">¥{{ item.item?.price || 0 }}</p>
          </div>

          <a-button
            type="outline"
            status="primary"
            size="small"
            class="unfav-btn"
            @click="handleUnfavorite(item)"
          >
            取消收藏
          </a-button>
        </div>
      </div>

      <a-empty v-else-if="!loading" description="暂无收藏">
        <template #image>
          <icon-heart size="64" />
        </template>
        <a-button type="primary" shape="round" @click="$router.push('/')">
          去逛逛
        </a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconHeart,
  IconHeartFill,
  IconEye,
  IconList,
  IconMessage,
} from "@arco-design/web-vue/es/icon";
import { parseFirstImageUrl } from "../../utils/image-utils";
import { getFavoriteList, removeFavorite, addToCart } from "../../services/api";

const router = useRouter();
const loading = ref(false);
const favorites = ref([]);
const batchMode = ref(false);

const activeCategory = ref('all');
const searchKeyword = ref('');
const selectedItems = ref([]);
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIGZpbGw9IiNGNUY3RjciLz48cGF0aCBkPSJNMzAgNDBIMjVWMzVIMzBWNDBaTTU1IDQwSDUwVjM1SDU1VjQwWiIgZmlsbD0iI0Q5RDlEOSIvPjxjaXJjbGUgY3g9IjQwIiBjeT0iNTUiIHI9IjMiIGZpbGw9IiNEOUQ5RDkiLz48L3N2Zz4=';

const selectedCount = computed(() => favorites.value.filter((f) => f.selected).length);
const isAllSelected = computed(() => selectedItems.value.length === filteredFavorites.value.length && filteredFavorites.value.length > 0);
const isIndeterminate = computed(() => selectedCount.value > 0 && !isAllSelected.value);

const filteredFavorites = computed(() => {
  let items = favorites.value || [];

  if (activeCategory.value !== 'all') {
    const categoryMap = {
      digital: ['手机', '电脑', '平板', '耳机'],
      books: ['教材', '书籍', '课本'],
      home: ['家具', '电器', '生活']
    };
    items = items.filter(item =>
      categoryMap[activeCategory.value]?.some(keyword =>
        item.item?.title?.includes(keyword)
      )
    );
  }

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    items = items.filter(item =>
      item.item?.title?.toLowerCase().includes(keyword)
    );
  }

  return items;
});

function handleCategoryChange(key) {
  console.log('切换分类:', key);
  selectedItems.value = [];
}

function handleSearch(value) {
  console.log('搜索:', value);
  selectedItems.value = [];
}

function handleSelectAll(checked) {
  if (checked) {
    selectedItems.value = filteredFavorites.value.map(item => item.id);
  } else {
    selectedItems.value = [];
  }
}

function handleItemSelect(itemId, checked) {
  if (checked) {
    if (!selectedItems.value.includes(itemId)) {
      selectedItems.value.push(itemId);
    }
  } else {
    selectedItems.value = selectedItems.value.filter(id => id !== itemId);
  }
}

function handleUnfavorite(item) {
  handleRemove(item);
}

function getItemImage(item) {
  return parseFirstImageUrl(item?.imageUrls);
}

function getStatusText(status) {
  const map = { PENDING_REVIEW: "待审核", APPROVED: "在售", REJECTED: "已下架", OFF_SHELF: "已下架" };
  return map[status] || status || "";
}

function getStatusClass(status) {
  return status === "APPROVED" ? "status-approved" : status === "PENDING_REVIEW" ? "status-pending" : "status-offline";
}

function canBuy(fav) {
  return fav.item?.reviewStatus === "APPROVED";
}

function formatTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.toLocaleDateString("zh-CN");
}

function goToItem(itemId) {
  if (itemId) router.push(`/item/${itemId}`);
}

async function handleBuy(item) {
  if (!canBuy({ item })) return;
  router.push(`/orders/confirm/${item.id}`);
}

async function handleRemove(fav) {
  try {
    await removeFavorite(fav.itemId || fav.id);
    Message.success("已取消收藏");
    await loadFavorites();
  } catch (e) {
    Message.error(e.message || "取消收藏失败");
  }
}

function toggleSelect(fav, val) {
  fav.selected = !!val;
}

function toggleSelectAll(val) {
  favorites.value.forEach((f) => { f.selected = !!val; });
}

async function batchUnfavorite() {
  const selected = favorites.value.filter((f) => f.selected);
  Modal.confirm({
    title: "批量取消收藏",
    content: `确定要取消 ${selected.length} 个商品的收藏吗？`,
    okText: "确定",
    okProps: { status: "danger" },
    onOk: async () => {
      try {
        for (const fav of selected) {
          await removeFavorite(fav.itemId || fav.id);
        }
        Message.success(`已取消 ${selected.length} 个收藏`);
        batchMode.value = false;
        await loadFavorites();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

async function batchAddToCart() {
  const selected = favorites.value.filter((f) => f.selected && canBuy(f));
  if (selected.length === 0) {
    Message.warning("选中的商品无法加入购物车");
    return;
  }
  try {
    for (const fav of selected) {
      await addToCart(fav.itemId, 1);
    }
    Message.success(`已将 ${selected.length} 件商品加入购物车`);
    router.push("/cart");
  } catch (e) {
    Message.error(e.message || "操作失败");
  }
}

async function loadFavorites() {
  loading.value = true;
  try {
    const res = await getFavoriteList();
    favorites.value = (res || []).map((f) => ({ ...f, selected: false }));
  } catch (e) {
    Message.error(e.message || "加载收藏失败");
  } finally {
    loading.value = false;
  }
}

onMounted(loadFavorites);

function handleViewItems() {
  console.log('查看商品');
}

function handleBatchAddCart() {
  console.log('批量加购');
}

function handleContactSeller() {
  console.log('联系卖家');
}

function handleBatchUnfavorite() {
  console.log('批量取消收藏');
}
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
}

.favorites-header {
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 50%, #0E42D2 100%);
  padding: 60px 40px;
  border-radius: 16px 16px 0 0;
  margin-bottom: -24px; /* 与下方内容衔接 */
}

.header-content {
  max-width: 800px;
}

.header-title {
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.header-subtitle {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

.header-btn {
  background-color: #FF7D00 !important;
  border-color: #FF7D00 !important;
  color: #ffffff !important;
  height: 40px;
  padding: 0 32px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 9999px !important; /* pill形状 */
  transition: all 0.25s ease-out;
}

.header-btn:hover {
  background-color: #FF9500 !important;
  border-color: #FF9500 !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 125, 0, 0.35);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid #E5E6EB;
  background: #ffffff;
  transition: all 0.25s cubic-bezier(0.215, 0.61, 0.355, 1);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  border-color: #165DFF;
}

.action-card--danger:hover {
  border-color: #F53F3F;
  box-shadow: 0 8px 24px rgba(245, 63, 63, 0.15);
}

.action-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #E8F3FF;
  color: #165DFF;
  margin-bottom: 12px;
  transition: all 0.25s ease-out;
}

.action-card--danger .action-icon {
  background: #FFF7E8;
  color: #FF7D00;
}

.action-card:hover .action-icon {
  background: #165DFF;
  color: #ffffff;
}

.action-card--danger:hover .action-icon {
  background: #F53F3F;
  color: #ffffff;
}

.action-text {
  font-size: 14px;
  color: #1D2129;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-tabs {
  flex: 1;
}

.filter-tabs :deep(.arco-tabs-nav) {
  margin-bottom: 0;
}

.filter-tabs :deep(.arco-tabs-tab) {
  font-size: 14px;
  color: #86909C;
  padding: 12px 20px;
  transition: all 0.25s ease-out;
}

.filter-tabs :deep(.arco-tabs-tab:hover) {
  color: #165DFF;
}

.filter-tabs :deep(.arco-tabs-tab-active) {
  color: #165DFF !important;
  font-weight: 600;
}

.filter-tabs :deep(.arco-tabs-tab-active .arco-tabs-tab-title::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: #165DFF;
  border-radius: 1px;
  animation: tabUnderline 0.25s ease-out;
}

@keyframes tabUnderline {
  from { width: 0; }
  to { width: 60%; }
}

.search-box {
  margin-left: auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }

  .favorite-count {
    font-size: 14px;
    color: #86909c;
  }
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #E8F3FF;
  border-radius: 8px;
  margin-bottom: 16px;
  position: sticky;
  top: 80px;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.08);
}

.batch-info {
  font-size: 13px;
  color: #4E5969;
  font-weight: 500;
}

.batch-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease-out;
}

.favorite-item:hover {
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  transform: translateY(-2px);
}

.item-checkbox {
  flex-shrink: 0;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  color: #1D2129;
  line-height: 1.5;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-price {
  font-size: 18px;
  color: #F53F3F;
  font-weight: 700;
  margin: 0;
}

.unfav-btn {
  flex-shrink: 0;
  border-color: #165DFF !important;
  color: #165DFF !important;
  transition: all 0.25s ease-out;
}

.unfav-btn:hover {
  background-color: #165DFF !important;
  color: #ffffff !important;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.item-card {
  position: relative;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
  }

  &--selected {
    border: 2px solid #165dff;
  }

  .item-checkbox {
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 2;
  }

  .item-image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    overflow: hidden;

    &:hover .unfavorite-btn {
      opacity: 1;
    }
  }

  .item-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #9ca3af;
      font-size: 48px;
    }
  }

  .item-status {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 500;
    color: white;

    &.status-approved { background: linear-gradient(135deg, #00b42a 0%, #36cfc9 100%); }
    &.status-pending { background: linear-gradient(135deg, #ff7d00 0%, #ffc53d 100%); }
    &.status-offline { background: linear-gradient(135deg, #86909c 0%, #c9cdd4 100%); }
  }

  .unfavorite-btn {
    position: absolute;
    bottom: 10px;
    right: 10px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 77, 79, 0.9);
    color: white;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
    z-index: 2;

    &:hover { opacity: 1; background: #f53f3f; }
  }

  .item-info {
    padding: 16px;

    .item-title {
      font-size: 14px;
      font-weight: 500;
      color: #1d2129;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 8px;
    }

    .item-price {
      font-size: 18px;
      font-weight: 700;
      color: #f53f3f;
      margin-bottom: 8px;
    }

    .item-meta {
      display: flex;
      gap: 8px;
      font-size: 12px;
      color: #86909c;
    }
  }

  .item-actions {
    padding: 0 16px 16px;
  }
}

:deep(.arco-empty) {
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
}
</style>
