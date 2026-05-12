<template>
  <div class="favorites-page">
    <!-- 统一页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" class="back-btn" @click="$router.back()">
          <template #icon><icon-arrow-left /></template>
          返回
        </a-button>
        <h2 class="page-title">我的收藏</h2>
      </div>
      <div class="header-right">
        <span class="item-count">共 {{ favorites.length }} 件收藏</span>
      </div>
    </div>

    <!-- 搜索框 -->
    <div v-if="favorites.length > 0 || loading" class="filter-toolbar">
      <a-input-search
        v-model="searchKeyword"
        placeholder="搜索收藏的商品"
        style="width: 100%; max-width: 400px; margin: 0 auto;"
        allow-clear
        @search="handleSearch"
      />
    </div>

    <!-- 快捷操作按钮组 -->
    <div v-if="filteredFavorites.length > 0" class="quick-actions">
      <div class="action-btn" @click="handleBatchAddCart">
        <icon-apps :size="20" />
        <span>加入购物车</span>
      </div>
      <div class="action-btn action-btn--danger" @click="handleBatchUnfavorite">
        <icon-delete :size="20" />
        <span>批量取消</span>
      </div>
      <div class="action-btn" @click="toggleBatchMode">
        <icon-check-square :size="20" />
        <span>{{ batchMode ? '取消选择' : '批量管理' }}</span>
      </div>
    </div>

    <!-- 主内容区 -->
    <a-spin :loading="loading" style="width: 100%; min-height: 400px;">
      <!-- 收藏列表 -->
      <div v-if="filteredFavorites.length > 0" class="content-list">
        <div
          v-for="item in filteredFavorites"
          :key="item.id"
          class="list-item"
          :class="{ 'list-item--selected': selectedItems.includes(item.id) }"
        >
          <!-- 选择框 -->
          <div v-if="batchMode" class="item-checkbox">
            <a-checkbox
              :model-value="selectedItems.includes(item.id)"
              @change="(val) => handleItemSelect(item.id, val)"
            />
          </div>

          <!-- 商品图片 -->
          <div class="item-image">
            <img :src="getItemImage(item.item) || defaultImage" :alt="item.item?.title" />
          </div>

          <!-- 商品信息 -->
          <div class="item-info">
            <h3 class="item-title">{{ item.item?.title || '商品已下架' }}</h3>
            <div class="item-meta">
              <span class="item-price">¥{{ item.item?.price || 0 }}</span>
              <span class="item-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="item-tags" v-if="item.item?.category">
              <a-tag size="small" color="arcoblue">{{ item.item.category }}</a-tag>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="item-actions">
            <a-button type="primary" size="small" @click="goToItem(item.itemId)">
              查看详情
            </a-button>
            <a-button type="outline" status="danger" size="small" @click="handleUnfavorite(item)">
              取消收藏
            </a-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" class="empty-state">
        <template #image>
          <icon-heart :size="80" style="color: #c9cdd4;" />
        </template>
        <template #description>
          <span style="font-size: 14px; color: #86909c;">暂无收藏记录</span>
        </template>
        <a-button type="primary" shape="round" @click="$router.push('/portal/home')">
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
  IconApps,
  IconDelete,
  IconCheckSquare,
} from "@arco-design/web-vue/es/icon";
import { getFavoriteList, removeFavorite, addToCart } from "../../services/api";

const router = useRouter();
const loading = ref(false);
const favorites = ref([]);
const batchMode = ref(false);
const selectedItems = ref([]);

const activeCategory = ref('all');
const searchKeyword = ref('');

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyMCIgaGVpZ2h0PSIxMjAiIGZpbGw9IiNGNUY3RjciLz48cGF0aCBkPSJNNDUgNDBINDBWMzVINDVWNDRaTTcwIDQwSDY1VjM1SDcwVjQwWiIgZmlsbD0iI0Q5RDlEOSIvPjxjaXJjbGUgY3g9IjYwIiBjeT0iNzAiIHI9IjQiIGZpbGw9IiNEOUQ5RDkiLz48L3N2Zz4=';

// 计算属性
const filteredFavorites = computed(() => {
  let items = favorites.value || [];

  // 分类筛选
  if (activeCategory.value !== 'all') {
    const categoryMap = {
      digital: ['手机', '电脑', '平板', '耳机', '数码'],
      books: ['教材', '书籍', '课本', '图书'],
      home: ['家具', '电器', '生活', '家居']
    };
    const keywords = categoryMap[activeCategory.value] || [];
    items = items.filter(item =>
      keywords.some(kw => item.item?.title?.includes(kw))
    );
  }

  // 搜索筛选
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    items = items.filter(item =>
      item.item?.title?.toLowerCase().includes(keyword)
    );
  }

  return items;
});

// 方法
function handleCategoryChange(key) {
  activeCategory.value = key;
  selectedItems.value = [];
}

function handleSearch(value) {
  searchKeyword.value = value;
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

function toggleBatchMode() {
  batchMode.value = !batchMode.value;
  if (!batchMode.value) {
    selectedItems.value = [];
  }
}

function getItemImage(item) {
  if (!item) return null;
  if (item.imageUrls) {
    try {
      const urls = typeof item.imageUrls === 'string' ? JSON.parse(item.imageUrls) : item.imageUrls;
      return Array.isArray(urls) && urls.length > 0 ? urls[0] : null;
    } catch (e) {
      return null;
    }
  }
  return null;
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
}

function goToItem(itemId) {
  if (itemId) router.push(`/portal/item/${itemId}`);
}

async function handleUnfavorite(item) {
  try {
    await removeFavorite(item.itemId || item.id);
    Message.success('已取消收藏');
    await loadFavorites();
  } catch (e) {
    Message.error(e.message || '操作失败');
  }
}

async function handleBatchUnfavorite() {
  if (selectedItems.value.length === 0) {
    Message.warning('请先选择要取消收藏的商品');
    return;
  }

  Modal.confirm({
    title: '批量取消收藏',
    content: `确定要取消 ${selectedItems.value.length} 个商品的收藏吗？`,
    okText: '确定',
    okProps: { status: 'danger' },
    onOk: async () => {
      try {
        for (const itemId of selectedItems.value) {
          await removeFavorite(itemId);
        }
        Message.success(`已取消 ${selectedItems.value.length} 个收藏`);
        batchMode.value = false;
        selectedItems.value = [];
        await loadFavorites();
      } catch (e) {
        Message.error(e.message || '操作失败');
      }
    },
  });
}

async function handleBatchAddCart() {
  const itemsToCart = batchMode.value
    ? filteredFavorites.value.filter(f => selectedItems.value.includes(f.id))
    : filteredFavorites.value;

  if (itemsToCart.length === 0) {
    Message.warning('请选择要加入购物车的商品');
    return;
  }

  try {
    let successCount = 0;
    for (const fav of itemsToCart) {
      // 优先使用 itemId（商品ID），其次尝试 item.id
      const itemId = fav.itemId || fav.item?.id || fav.id;
      
      if (!itemId) {
        console.warn('[Favorites] 商品ID缺失:', fav);
        continue;
      }

      try {
        await addToCart(itemId, 1);
        successCount++;
      } catch (cartError) {
        console.error('[Favorites] 添加到购物车失败:', cartError);
        Message.error(`商品"${fav.item?.title || '未知'}"添加失败`);
      }
    }
    
    if (successCount > 0) {
      Message.success(`已将 ${successCount} 件商品加入购物车`);
      router.push('/portal/cart');
    } else {
      Message.error('所有商品添加失败，请稍后重试');
    }
  } catch (e) {
    console.error('[Favorites] 批量添加购物车失败:', e);
    Message.error(e.message || '操作失败');
  }
}

async function loadFavorites() {
  loading.value = true;
  try {
    const res = await getFavoriteList();
    // 兼容多种数据格式：数组 或 { data: [] } 或 { rows: [] }
    let list = [];
    if (Array.isArray(res)) {
      list = res;
    } else if (res?.data && Array.isArray(res.data)) {
      list = res.data;
    } else if (res?.rows && Array.isArray(res.rows)) {
      list = res.rows;
    } else if (res?.list && Array.isArray(res.list)) {
      list = res.list;
    }
    favorites.value = list.map(f => ({ ...f, selected: false }));
  } catch (e) {
    console.error('[Favorites] 加载失败:', e);
    Message.error(e.message || '加载收藏失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadFavorites);
</script>

<style lang="scss" scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f7f8fa;
  min-height: 100vh;
}

/* ========== 统一页面头部 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 50%, #0E42D2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    color: rgba(255, 255, 255, 0.9);
    &:hover {
      color: white;
      background: rgba(255, 255, 255, 0.15);
    }
  }

  .page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
  }

  .header-right {
    .item-count {
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

/* ========== 筛选工具栏 ========== */
.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .filter-left {
    :deep(.arco-tabs-nav) {
      margin-bottom: 0;
    }
    :deep(.arco-tabs-tab) {
      padding: 8px 16px;
      font-size: 14px;
    }
  }
}

/* ========== 快捷操作 ========== */
.quick-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #165DFF;
  border: 1px solid #E5E6EB;
  transition: all 0.25s ease;

  &:hover {
    background: #E8F3FF;
    border-color: #165DFF;
  }

  &--danger {
    color: #F53F3F;
    border-color: #FDC9C9;

    &:hover {
      background: #FFECE8;
      border-color: #F53F3F;
    }
  }
}

/* ========== 内容列表 ========== */
.content-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
  }

  &--selected {
    border: 2px solid #165DFF;
    background: #F7FBFF;
  }

  .item-checkbox {
    flex-shrink: 0;
  }

  .item-image {
    width: 96px;
    height: 96px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f5f6f8;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .item-info {
    flex: 1;
    min-width: 0;

    .item-title {
      font-size: 15px;
      font-weight: 500;
      color: #1D2129;
      line-height: 1.5;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 8px;

      .item-price {
        font-size: 20px;
        font-weight: 700;
        color: #F53F3F;
      }

      .item-time {
        font-size: 13px;
        color: #86909C;
      }
    }

    .item-tags {
      display: flex;
      gap: 6px;
    }
  }

  .item-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex-shrink: 0;
  }
}

/* ========== 空状态 ========== */
.empty-state {
  padding: 80px 20px;
  background: white;
  border-radius: 10px;
  margin-top: 20px;
}
</style>
