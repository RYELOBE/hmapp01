<template>
  <div class="items-page">
    <div class="hero-banner">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">校园二手市场</h1>
          <p class="hero-desc">发现优质二手好物，让闲置物品流转起来</p>
        </div>
        <div class="hero-search">
          <a-input-search
            v-model="searchQuery"
            :search-button="true"
            placeholder="输入关键词搜索商品..."
            size="large"
            allow-clear
            @search="handleSearch"
            @pressEnter="handleSearch"
            class="hero-search-box"
          >
            <template #prefix>
              <icon-search style="color: #86909C; font-size: 18px;" />
            </template>
            <template #search-button>
              <icon-search style="color: #fff;" />
            </template>
          </a-input-search>
        </div>
      </div>
    </div>

    <div class="main-container">
      <aside class="sidebar">
        <div class="sidebar-section">
          <div class="sidebar-title">
            <span class="sidebar-dot"></span>
            商品分类
          </div>
          <ul class="sidebar-list">
            <li
              v-for="cat in categories"
              :key="cat.value"
              :class="{ 'sidebar-item--active': selectedCategory === cat.value }"
              class="sidebar-item"
              @click="selectCategory(cat.value)"
            >
              {{ cat.label }}
            </li>
          </ul>
        </div>

        <div class="sidebar-section">
          <div class="sidebar-title">
            <span class="sidebar-dot"></span>
            快捷筛选
          </div>
          <ul class="sidebar-list sidebar-list--compact">
            <li class="sidebar-item" :class="{ 'sidebar-item--active': sortBy === 'latest' }" @click="handleSortChange('latest')">
              最新发布
            </li>
            <li class="sidebar-item" :class="{ 'sidebar-item--active': sortBy === 'price_asc' }" @click="handleSortChange('price_asc')">
              价格从低到高
            </li>
            <li class="sidebar-item" :class="{ 'sidebar-item--active': sortBy === 'price_desc' }" @click="handleSortChange('price_desc')">
              价格从高到低
            </li>
          </ul>
        </div>
      </aside>

      <main class="content-area">
        <div class="filter-bar">
          <div class="filter-row">
            <span class="filter-label">分类筛选：</span>
            <div class="filter-tags">
              <span
                v-for="cat in categories"
                :key="cat.value"
                :class="{ 'filter-tag--active': selectedCategory === cat.value }"
                class="filter-tag"
                @click="selectCategory(cat.value)"
              >
                {{ cat.label }}
              </span>
            </div>
          </div>
        </div>

        <div class="result-header">
          <div class="result-info">
            共 <strong>{{ total }}</strong> 件商品
          </div>
          <div class="result-sort">
            <span
              :class="{ 'sort-link--active': sortBy === 'latest' }"
              class="sort-link"
              @click="handleSortChange('latest')"
            >最新发布</span>
            <span class="sort-divider"></span>
            <span
              :class="{ 'sort-link--active': sortBy === 'price_asc' }"
              class="sort-link"
              @click="handleSortChange('price_asc')"
            >价格升序</span>
            <span class="sort-divider"></span>
            <span
              :class="{ 'sort-link--active': sortBy === 'price_desc' }"
              class="sort-link"
              @click="handleSortChange('price_desc')"
            >价格降序</span>
          </div>
        </div>

        <a-spin :loading="loading" class="spin-wrapper">
          <div v-if="!loading && items.length > 0" class="items-grid">
            <ItemCard
              v-for="item in items"
              :key="item.id"
              :item="formatItem(item)"
              @click="$router.push(`/portal/item/${item.id}`)"
            />
          </div>

          <div v-if="!loading && items.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="#C9CDD4" stroke-width="1.2">
                <circle cx="11" cy="11" r="8"/>
                <path d="M21 21l-4.35-4.35"/>
              </svg>
            </div>
            <p class="empty-text">暂无商品，换个关键词试试？</p>
          </div>
        </a-spin>

        <div v-if="totalPages > 1" class="pagination-wrap">
          <a-pagination
            :current="currentPage"
            :total="total"
            :page-size="pageSize"
            show-page-size
            :page-size-options="[12, 18, 24]"
            size="large"
            @change="onPageChange"
            @page-size-change="onPageSizeChange"
            class="custom-pagination"
          />
        </div>
      </main>
    </div>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { IconSearch } from "@arco-design/web-vue/es/icon";
import { getItems } from "../../../services/api";
import ItemCard from "../../../components/data/ItemCard/ItemCard.vue";

const router = useRouter();
const route = useRoute();

const searchQuery = ref('');
const selectedCategory = ref('');
const sortBy = ref('latest');
const items = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const categories = [
  { value: '', label: '全部' },
  { value: 'ELECTRONICS', label: '电子产品' },
  { value: 'BOOKS', label: '图书教材' },
  { value: 'CLOTHING', label: '服饰鞋包' },
  { value: 'DAILY', label: '生活用品' },
  { value: 'SPORTS', label: '运动器材' },
  { value: 'BEAUTY', label: '美妆护肤' },
  { value: 'FOOD', label: '食品零食' },
];

async function loadData() {
  loading.value = true;
  try {
    const keyword = route.query.keyword || "";
    const category = route.query.category || selectedCategory.value;

    const params = {
      category: category || undefined,
      keyword: keyword || undefined,
      sort: sortBy.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      approvedOnly: true,
    };

    const res = await getItems(params);
    const rows = res?.items || res?.rows || res?.list || res?.content || [];
    
    if (Array.isArray(rows)) {
      items.value = rows;
    } else if (res && typeof res === 'object') {
      items.value = [res];
    } else {
      items.value = [];
    }
    
    total.value = res?.total || res?.totalCount || res?.totalElements || (Array.isArray(rows) ? rows.length : 0);
  } catch (e) {
    console.error('[Items] 加载商品失败:', e);
    items.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function selectCategory(value) {
  selectedCategory.value = selectedCategory.value === value ? "" : value;
  currentPage.value = 1;

  if (selectedCategory.value) {
    router.push({ path: '/portal/buyer/items', query: { category: selectedCategory.value } });
  } else {
    router.push({ path: '/portal/buyer/items', query: {} });
  }
}

function handleSortChange(value) {
  sortBy.value = value;
  currentPage.value = 1;
  loadData();
}

function handleSearch(value) {
  const keyword = value || searchQuery.value;
  router.push({ path: '/portal/buyer/items', query: { keyword } });
}

function onPageChange(page) {
  currentPage.value = page;
  loadData();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function onPageSizeChange(size) {
  pageSize.value = size;
  currentPage.value = 1;
  loadData();
}

function formatItem(item) {
  return {
    id: item.id,
    title: item.title,
    price: item.price,
    imageUrls: item.imageUrls || (item.image ? [item.image] : []),
    sellerName: item.sellerName || item.seller?.nickname || item.sellerName,
    category: item.category,
    conditionLevel: item.conditionLevel || item.condition,
    campus: item.campus || item.location,
  };
}

watch(() => route.query, () => {
  currentPage.value = 1;
  selectedCategory.value = route.query.category || '';
  searchQuery.value = route.query.keyword || '';
  loadData();
}, { immediate: true });

onMounted(() => {
  selectedCategory.value = route.query.category || '';
  searchQuery.value = route.query.keyword || '';
  loadData();
});
</script>

<style lang="scss" scoped>
$primary-blue: #165DFF;
$primary-light: #E8F3FF;
$bg-white: #FFFFFF;
$bg-gray: #F7F8FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;
$border-color: #E5E6EB;
$radius-sm: 6px;
$radius-md: 10px;
$radius-lg: 14px;

.items-page {
  min-height: calc(100vh - 64px);
  background: $bg-gray;
}

.hero-banner {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a2a6c 0%, #2d4a8c 40%, #3d5fa3 70%, #4a6bb5 100%);
  padding: 56px 24px 48px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(255,255,255,0.06) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(22,93,255,0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 60% 80%, rgba(100,150,255,0.08) 0%, transparent 50%);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }
}

.hero-content {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

.hero-text {
  margin-bottom: 32px;
}

.hero-title {
  margin: 0 0 12px;
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.hero-desc {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 300;
}

.hero-search {
  max-width: 600px;
  margin: 0 auto;
}

.hero-search-box {
  :deep(.arco-input-wrapper) {
    border-radius: 28px !important;
    height: 52px !important;
    background: rgba(255, 255, 255, 0.95) !important;
    border: 2px solid transparent !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    padding-right: 8px !important;
    transition: all 0.3s ease;

    &:hover, &:focus-within {
      background: #fff !important;
      box-shadow: 0 8px 36px rgba(22, 93, 255, 0.25);
      transform: translateY(-1px);
    }

    .arco-input {
      font-size: 15px;
      color: $text-primary;

      &::placeholder {
        color: $text-tertiary;
      }
    }
  }

  :deep(.arco-input-append) {
    background: $primary-blue !important;
    border-radius: 24px !important;
    width: 48px !important;
    height: 42px !important;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background 0.2s ease;

    &:hover {
      background: #0E42D2 !important;
    }

    .arco-icon {
      font-size: 20px;
    }
  }
}

.main-container {
  display: flex;
  gap: 24px;
  max-width: 1360px;
  margin: -24px auto 0;
  padding: 0 24px 48px;
  position: relative;
  z-index: 1;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-section {
  background: $bg-white;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid $border-color;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px 12px;
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  border-bottom: 1px solid #F2F3F5;
}

.sidebar-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $primary-blue;
  flex-shrink: 0;
}

.sidebar-list {
  list-style: none;
  margin: 0;
  padding: 8px 12px 12px;
}

.sidebar-list--compact {
  padding: 6px 10px 10px;
}

.sidebar-item {
  padding: 9px 14px;
  font-size: 13.5px;
  color: $text-secondary;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all 0.2s ease;
  line-height: 1.4;

  &:hover {
    background: $primary-light;
    color: $primary-blue;
  }

  &--active {
    background: $primary-blue;
    color: #fff;
    font-weight: 500;

    &:hover {
      background: #0E42D2;
      color: #fff;
    }
  }
}

.content-area {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  background: $bg-white;
  border-radius: $radius-md;
  padding: 18px 22px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid $border-color;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
  padding-top: 5px;
  line-height: 1;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  padding: 5px 16px;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 500;
  color: $text-secondary;
  background: $bg-gray;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
  user-select: none;

  &:hover {
    background: $primary-light;
    color: $primary-blue;
    border-color: rgba(22, 93, 255, 0.2);
  }

  &--active {
    background: $primary-blue;
    color: #fff;
    border-color: $primary-blue;

    &:hover {
      background: #0E42D2;
      color: #fff;
    }
  }
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 4px;
}

.result-info {
  font-size: 14px;
  color: $text-secondary;

  strong {
    color: $primary-blue;
    font-weight: 700;
    font-size: 16px;
  }
}

.result-sort {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sort-link {
  font-size: 13px;
  color: $text-tertiary;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: $radius-sm;
  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    color: $primary-blue;
    background: $primary-light;
  }

  &--active {
    color: $primary-blue;
    font-weight: 600;
    background: $primary-light;
  }
}

.sort-divider {
  width: 1px;
  height: 14px;
  background: #E5E6EB;
}

.spin-wrapper {
  min-height: 200px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 4px 0 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  gap: 16px;
}

.empty-icon {
  opacity: 0.6;
}

.empty-text {
  margin: 0;
  font-size: 15px;
  color: $text-tertiary;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0 16px;
}

.custom-pagination {
  :deep(.arco-pagination-item) {
    border-radius: 8px !important;
    min-width: 38px;
    height: 38px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid $border-color;
    transition: all 0.2s ease;
    font-weight: 500;

    &:hover:not(.arco-pagination-item-active):not(:disabled) {
      border-color: $primary-blue;
      color: $primary-blue;
    }

    &.arco-pagination-item-active {
      background: $primary-blue;
      border-color: $primary-blue;

      a {
        color: #fff;
      }
    }
  }

  :deep(.arco-pagination-prev),
  :deep(.arco-pagination-next) {
    border-radius: 8px !important;
    border: 1px solid $border-color;
    transition: all 0.2s ease;

    &:hover:not(:disabled) {
      border-color: $primary-blue;
      color: $primary-blue;
    }
  }

  :deep(.arco-pagination-jumper) {
    .arco-input-wrapper {
      border-radius: 8px;
    }
  }
}

@media (max-width: 1100px) {
  .items-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .sidebar {
    width: 190px;
  }
}

@media (max-width: 900px) {
  .main-container {
    flex-direction: column;
    gap: 16px;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    gap: 12px;
    overflow-x: auto;

    .sidebar-section {
      flex-shrink: 0;
      min-width: 160px;
    }
  }

  .sidebar-list {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 8px 12px;
  }

  .sidebar-item {
    padding: 5px 12px;
    font-size: 13px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-desc {
    font-size: 14px;
  }

  .hero-banner {
    padding: 40px 20px 36px;
  }

  .items-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .hero-title {
    font-size: 24px;
  }

  .hero-search-box {
    :deep(.arco-input-wrapper) {
      height: 46px !important;
    }
  }

  .filter-row {
    flex-direction: column;
    gap: 10px;
  }

  .filter-label {
    padding-top: 0;
  }

  .result-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .items-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .main-container {
    padding: 0 16px 32px;
    margin-top: -16px;
  }

  .sidebar {
    flex-direction: column;
  }

  .sidebar-section {
    min-width: unset;
  }

  .sidebar-list {
    flex-direction: column;
  }
}
</style>
