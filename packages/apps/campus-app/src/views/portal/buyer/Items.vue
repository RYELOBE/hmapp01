<template>
  <div class="items-page">
    <div class="page-header">
      <h1 class="page-title">全部商品</h1>
      <p class="page-subtitle">发现更多优质二手好物</p>
    </div>

    <div class="search-bar">
      <a-input-search
        v-model="searchQuery"
        :style="{ width: '100%' }"
        :search-button="true"
        placeholder="搜索商品..."
        size="large"
        allow-clear
        @search="handleSearch"
        @pressEnter="handleSearch"
        class="search-box"
      >
        <template #prefix>
          <icon-search style="color: #86909C; font-size: 18px;" />
        </template>
        <template #search-button>
          <span>搜索</span>
        </template>
      </a-input-search>
    </div>

    <div class="filter-section">
      <div class="filter-categories">
        <a-tag
          v-for="cat in categories"
          :key="cat.value"
          :class="{ 'filter-tag--active': selectedCategory === cat.value }"
          class="filter-tag"
          @click="selectCategory(cat.value)"
        >
          {{ cat.label }}
        </a-tag>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <a-typography-text class="result-count">
          共 {{ total }} 件商品
        </a-typography-text>
      </div>
      <div class="toolbar-right">
        <a-radio-group v-model="sortBy" type="button" size="small" @change="handleSort">
          <a-radio value="latest">最新发布</a-radio>
          <a-radio value="price_asc">价格升序</a-radio>
          <a-radio value="price_desc">价格降序</a-radio>
        </a-radio-group>
      </div>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div class="items-grid">
        <ItemCard
          v-for="item in items"
          :key="item.id"
          :item="formatItem(item)"
          @click="$router.push(`/portal/item/${item.id}`)"
        />
      </div>

      <div v-if="!loading && items.length === 0" class="empty-state">
        <a-empty description="暂无商品，换个关键词试试？">
          <template #image>
            <icon-search size="48" />
          </template>
        </a-empty>
      </div>
    </a-spin>

    <div class="load-more-section">
      <a-button v-if="hasMore && !loading" type="primary" shape="round" size="large" class="load-more-btn" @click="loadMore">
        加载更多
      </a-button>
    </div>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
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
const pageSize = 20;
const hasMore = ref(false);

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

async function loadData(append = false) {
  loading.value = true;
  try {
    const keyword = route.query.keyword || "";
    const category = route.query.category || selectedCategory.value;
    
    const params = {
      category: category || undefined,
      keyword: keyword || undefined,
      sort: sortBy.value,
      pageNo: currentPage.value,
      pageSize,
      approvedOnly: true,
    };

    const res = await getItems(params);
    const rows = res?.items || res?.rows || [];
    
    if (append) {
      items.value = [...items.value, ...rows];
    } else {
      items.value = rows;
    }
    
    total.value = res?.total || res?.totalCount || 0;
    hasMore.value = items.value.length < total.value;
  } catch (e) {
    console.error('[Items] 加载商品失败:', e);
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

function handleSort() {
  currentPage.value = 1;
  loadData();
}

function loadMore() {
  currentPage.value++;
  loadData(true);
}

function handleSearch(value) {
  const keyword = value || searchQuery.value;
  router.push({ path: '/portal/buyer/items', query: { keyword } });
}

function formatItem(item) {
  return {
    id: item.id,
    title: item.title,
    price: item.price,
    imageUrls: item.imageUrls || (item.image ? [item.image] : []),
    sellerName: item.sellerName,
    category: item.category,
    conditionLevel: item.conditionLevel || item.condition,
    campus: item.campus,
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
$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;

.items-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 40px;
}

.page-header {
  text-align: center;
  padding: 20px 0;
}

.page-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
  color: $text-primary;
}

.page-subtitle {
  margin: 0;
  font-size: 16px;
  color: $text-tertiary;
}

.search-bar {
  max-width: 500px;
  margin: 0 auto;
}

.search-box {
  :deep(.arco-input-wrapper) {
    border-radius: 24px !important;
    height: 48px !important;
    background: $bg-white !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 1px solid #E5E6EB;
    transition: all 0.3s ease;
    
    &:hover, &:focus-within {
      border-color: $primary-blue !important;
      box-shadow: 0 4px 16px rgba(22, 93, 255, 0.15);
    }
  }

  :deep(.arco-btn) {
    border-radius: 20px !important;
    padding: 0 24px !important;
    font-size: 14px !important;
    font-weight: 600 !important;
    background: $primary-blue !important;
    border: none !important;
  }
}

.filter-section {
  background: $bg-white;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tag {
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: $text-secondary;
  background: $bg-gray;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  
  &:hover {
    background: #E8F3FF;
    color: $primary-blue;
  }
  
  &--active {
    background: $primary-blue;
    color: $bg-white;
    
    &:hover {
      background: #0E42D2;
      color: $bg-white;
    }
  }
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.toolbar-left {
  .result-count {
    font-size: 14px;
    font-weight: 500;
    color: $text-secondary;
  }
}

.toolbar-right {
  :deep(.arco-radio-group) {
    background: $bg-gray;
    border-radius: 8px;
    padding: 4px;
    
    .arco-radio-button {
      border-radius: 6px !important;
      margin: 0 !important;
      
      &.arco-radio-button--checked {
        background: $primary-blue !important;
        color: $bg-white !important;
      }
    }
  }
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  padding: 8px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}

.load-more-section {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.load-more-btn {
  min-width: 200px;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, $primary-blue 0%, #4080FF 100%);
  border: none;
  border-radius: 9999px;
  color: $bg-white;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.2);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.3);
  }
}

@media (min-width: 1440px) {
  .items-grid { 
    grid-template-columns: repeat(5, 1fr); 
  }
}

@media (min-width: 1280px) and (max-width: 1439px) {
  .items-grid { 
    grid-template-columns: repeat(4, 1fr); 
  }
}

@media (min-width: 1024px) and (max-width: 1279px) {
  .items-grid { 
    grid-template-columns: repeat(3, 1fr); 
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .items-grid { 
    grid-template-columns: repeat(3, 1fr); 
  }
}

@media (max-width: 767px) {
  .page-title {
    font-size: 26px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .filter-section {
    padding: 14px 16px;
  }
  
  .filter-tag {
    padding: 6px 14px;
    font-size: 13px;
  }
  
  .items-grid { 
    grid-template-columns: repeat(2, 1fr); 
    gap: 16px;
  }
}
</style>