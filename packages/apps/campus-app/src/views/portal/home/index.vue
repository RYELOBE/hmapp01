<template>
  <div class="home-page">
    <!-- 全屏 Hero 区域 -->
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>
、

      
      <div class="hero-content">
        <h1 class="hero-title">校园二手交易平台</h1>
        <p class="hero-subtitle">闲置不浪费，校园淘好物</p>
        
        <!-- 搜索框 -->
        <div class="hero-search">
          <a-input-search
            v-model="searchQuery"
            :style="{ width: '100%' }"
            :search-button="true"
            placeholder="搜索你想要的宝贝..."
            size="large"
            allow-clear
            @search="handleSearch"
            @pressEnter="handleSearch"
            class="hero-search-box"
          >
            <template #prefix>
              <icon-search style="color: #86909C; font-size: 20px;" />
            </template>
            <template #search-button>
              <span>搜索</span>
            </template>
          </a-input-search>
        </div>

        <!-- CTA 按钮组 -->
        <div class="hero-actions">
          <a-button type="primary" size="large" class="btn-publish" @click="$router.push('/portal/seller/publish')">
            <template #icon>📦</template>
            立即发布
          </a-button>
          <a-button size="large" class="btn-browse" @click="scrollToItems">
            去逛逛 →
          </a-button>
        </div>
      </div>
    </section>

    <!-- 分类快捷入口 -->
    <section class="category-section">
      <div class="category-grid">
        <div
          v-for="cat in CATEGORIES.slice(1)"
          :key="cat.value"
          class="category-card"
          :class="{ 'category-card--active': selectedCategory === cat.value }"
          @click="selectCategory(cat.value)"
        >
          <span class="category-card__icon">{{ cat.icon }}</span>
          <span class="category-card__label">{{ cat.label }}</span>
        </div>
      </div>
    </section>

    <!-- 已选筛选器 -->
    <div v-if="selectedFilters.length > 0" class="selected-filters">
      <div class="filter-tags">
        <a-tag
          v-for="(filter, index) in selectedFilters"
          :key="index"
          closable
          color="arcoblue"
          class="filter-tag"
          @close="removeFilter(index)"
        >
          {{ filter.label }}
        </a-tag>
      </div>
      <a-link class="clear-filters" @click="clearAllFilters">清空</a-link>
    </div>

    <!-- 工具栏 -->
    <section class="home-toolbar">
      <div class="home-toolbar__left">
        <a-typography-text class="home-toolbar__count">
          共 {{ total }} 件好物
        </a-typography-text>
      </div>
      <div class="home-toolbar__right">
        <a-radio-group v-model="sortBy" type="button" size="small" @change="handleSort">
          <a-radio value="latest">最新</a-radio>
          <a-radio value="price_asc">价格升</a-radio>
          <a-radio value="price_desc">价格降</a-radio>
        </a-radio-group>
      </div>
    </section>

    <!-- 商品网格 -->
    <section ref="itemsSection" class="items-grid-section">
      <a-spin :loading="loading" style="width: 100%">
        <div class="items-grid-enhanced">
          <ItemCard
            v-for="item in items"
            :key="item.id"
            :item="formatItem(item)"
            @click="$router.push(`/portal/buyer/items`)"
          />
        </div>

        <div v-if="!loading && items.length === 0" class="home-grid__empty">
          <a-empty description="暂无商品，换个关键词试试？">
            <template #image>
              <icon-search size="48" />
            </template>
          </a-empty>
        </div>
      </a-spin>
    </section>

    <!-- 加载更多 -->
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
import { useRouter, useRoute } from "vue-router";
import { IconSearch } from "@arco-design/web-vue/es/icon";
import { getItems } from "../../../services/api";
import ItemCard from "../../../components/data/ItemCard/ItemCard.vue";
import { CATEGORIES, SORT_OPTIONS } from "./const";
import { debounce } from "@campus/common";

const router = useRouter();
const route = useRoute();
const itemsSection = ref(null);

const searchQuery = ref('');
const selectedFilters = ref([]);
const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQwIiBoZWlnaHQ9IjMyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxOCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuWbvueJh+WKoOi9veWksei0pTwvdGV4dD48L3N2Zz4=';
const items = ref([]);
const loading = ref(false);
const total = ref(0);
const selectedCategory = ref("");
const sortBy = ref("latest");
const currentPage = ref(1);
const pageSize = 20;
const hasMore = ref(false);

async function loadData(append = false) {
  loading.value = true;
  try {
    const keyword = route.query.keyword || "";
    const params = {
      category: selectedCategory.value || undefined,
      keyword: keyword || undefined,
      sort: sortBy.value,
      pageNo: currentPage.value,
      pageSize,
      approvedOnly: true,
    };

    console.log('[Home] ===== 开始加载商品数据 =====');
    console.log('[Home] 请求参数:', JSON.stringify(params, null, 2));
    console.log('[Home] 当前Token:', localStorage.getItem('campus_market_token')?.substring(0, 30) + '...');
    console.log('[Home] 即将调用 getItems API...');

    const res = await getItems(params);

    console.log('[Home] ✅ API调用成功！响应数据:', res);
    const rows = res?.items || res?.rows || [];
    if (append) {
      items.value = [...items.value, ...rows];
    } else {
      items.value = rows;
    }
    total.value = res?.total || res?.totalCount || 0;
    hasMore.value = items.value.length < total.value;

    console.log('[Home] ✅ 数据加载完成，共', rows.length, '条记录');
  } catch (e) {
    console.error('[Home] ❌ loadData 失败！');
    console.error('[Home] 错误类型:', e.constructor.name);
    console.error('[Home] 错误消息:', e.message);
    console.error('[Home] 错误状态码:', e.response?.status || 'N/A');
    console.error('[Home] 完整错误对象:', e);

    // 如果是401错误，显示友好提示而不是崩溃
    if (e.response?.status === 401) {
      console.warn('[Home] ⚠️ 检测到401未授权错误，可能是Token无效或API需要认证');
      // 不抛出错误，让页面继续显示（只是数据为空）
      items.value = [];
      total.value = 0;
    } else {
      throw e; // 其他错误继续抛出
    }
  } finally {
    loading.value = false;
  }
}

function selectCategory(val) {
  selectedCategory.value = selectedCategory.value === val ? "" : val;
  currentPage.value = 1;
  loadData();
}

function handleSort() {
  currentPage.value = 1;
  loadData();
}

function loadMore() {
  currentPage.value++;
  loadData(true);
}

function scrollToItems() {
  itemsSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

const debouncedSearch = debounce((keyword) => {
  router.push({ path: '/portal/home', query: { keyword } });
}, 300);

function handleSearch(value) {
  debouncedSearch(value || searchQuery.value);
}

function removeFilter(index) {
  selectedFilters.value.splice(index, 1);
}

function clearAllFilters() {
  selectedFilters.value = [];
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

watch(() => route.query.keyword, () => {
  currentPage.value = 1;
  loadData();
});

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
// 设计变量
$primary-blue: #165DFF;
$primary-blue-light: #4080FF;
$primary-blue-dark: #0E42D2;
$accent-orange: #FF7D00;
$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;

.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 40px;
  background: transparent;
}

// ========== Hero 区域 ==========
.hero-section {
  position: relative;
  width: 100%;
  min-height: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin: -24px -24px 0 -24px;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.hero-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, 
    $primary-blue 0%, 
    $primary-blue-light 35%,
    #4A90FF 65%, 
    #E8F3FF 100%
  );
}

.hero-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.08;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 30%, rgba(255,255,255,0.1) 0%, transparent 40%);
  background-size: 100% 100%;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 900px;
  padding: 60px 24px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  margin: 0 0 16px;
  font-size: 56px;
  font-weight: 800;
  color: $bg-white;
  letter-spacing: -0.02em;
  line-height: 1.2;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  margin: 0 0 40px;
  font-size: 22px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 400;
  letter-spacing: 0.01em;
}

.hero-search {
  max-width: 640px;
  margin: 0 auto 32px;
}

.hero-search-box {
  :deep(.arco-input-wrapper) {
    border-radius: 28px !important;
    height: 56px !important;
    background: $bg-white !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 2px solid transparent;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover, &:focus-within {
      border-color: $primary-blue !important;
      box-shadow: 0 12px 40px rgba(22, 93, 255, 0.25), 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }
  }

  :deep(.arco-input) {
    font-size: 16px;
    color: $text-primary;
    
    &::placeholder {
      color: $text-tertiary;
    }
  }

  :deep(.arco-btn) {
    border-radius: 24px !important;
    height: 48px !important;
    padding: 0 32px !important;
    font-size: 16px !important;
    font-weight: 600 !important;
    background: linear-gradient(135deg, $primary-blue 0%, $primary-blue-light 100%) !important;
    border: none !important;
    margin-right: 4px !important;
    
    &:hover {
      transform: scale(1.02);
    }
  }
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
}

.btn-publish {
  height: 52px !important;
  padding: 0 36px !important;
  font-size: 17px !important;
  font-weight: 700 !important;
  border-radius: 26px !important;
  background: linear-gradient(135deg, $accent-orange 0%, #FF9500 100%) !important;
  border: none !important;
  box-shadow: 0 6px 24px rgba(255, 125, 0, 0.35) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  
  &:hover {
    transform: translateY(-3px) scale(1.03) !important;
    box-shadow: 0 10px 32px rgba(255, 125, 0, 0.45) !important;
  }
}

.btn-browse {
  height: 52px !important;
  padding: 0 36px !important;
  font-size: 17px !important;
  font-weight: 600 !important;
  border-radius: 26px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  color: $primary-blue !important;
  border: 2px solid rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  
  &:hover {
    transform: translateY(-3px) !important;
    background: $bg-white !important;
    border-color: $bg-white !important;
    box-shadow: 0 8px 24px rgba(255, 255, 255, 0.3) !important;
  }
}

// ========== 分类快捷入口 ==========
.category-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  margin-top: -24px;
  position: relative;
  z-index: 2;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: $bg-gray;
  border: 2px solid transparent;
  
  &:hover {
    transform: translateY(-6px);
    background: #E8F3FF;
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
    border-color: rgba(22, 93, 255, 0.2);
  }
  
  &--active {
    background: linear-gradient(135deg, #E8F3FF 0%, #D6E9FF 100%);
    border-color: $primary-blue;
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.18);
    
    .category-card__label {
      color: $primary-blue;
      font-weight: 700;
    }
  }

  &__icon {
    font-size: 38px;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.08));
    transition: transform 0.3s ease;
  }

  &__label {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
    white-space: nowrap;
  }
  
  &:hover &__icon {
    transform: scale(1.15) rotate(-5deg);
  }
}

// ========== 筛选器 ==========
.selected-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  height: 30px;
  line-height: 28px;
  padding: 0 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.clear-filters {
  color: $text-tertiary;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  margin-left: 16px;
  
  &:hover {
    color: $primary-blue;
  }
}

// ========== 工具栏 ==========
.home-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__count {
    font-size: 15px;
    font-weight: 500;
    color: $text-secondary;
  }
  
  &__right {
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
}

// ========== 商品网格 ==========
.items-grid-section {
  animation: fadeIn 0.6s ease-out 0.2s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.items-grid-enhanced {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
  padding: 8px 0 40px 0;
}

.home-grid__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}

// ========== 加载更多 ==========
.load-more-section {
  display: flex;
  justify-content: center;
  padding: 32px 0 48px 0;
}

.load-more-btn {
  min-width: 220px;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, $primary-blue-light 0%, $primary-blue 50%, $primary-blue-dark 100%);
  border: none;
  border-radius: 9999px;
  color: $bg-white;
  letter-spacing: 0.02em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.25);

  &:hover {
    transform: translateY(-3px) scale(1.02);
    box-shadow: 0 10px 32px rgba(22, 93, 255, 0.35);
  }
}

// ========== 响应式设计 ==========
@media (min-width: 1440px) {
  .items-grid-enhanced { 
    grid-template-columns: repeat(5, 1fr); 
  }
  .category-grid {
    grid-template-columns: repeat(8, 1fr);
  }
}

@media (min-width: 1280px) and (max-width: 1439px) {
  .items-grid-enhanced { 
    grid-template-columns: repeat(4, 1fr); 
  }
  .category-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}

@media (min-width: 1024px) and (max-width: 1279px) {
  .items-grid-enhanced { 
    grid-template-columns: repeat(3, 1fr); 
  }
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .hero-section {
    min-height: 480px;
  }
  
  .hero-title {
    font-size: 44px;
  }
  
  .hero-subtitle {
    font-size: 19px;
  }
  
  .hero-search-box :deep(.arco-input-wrapper) {
    height: 50px !important;
  }
  
  .items-grid-enhanced { 
    grid-template-columns: repeat(3, 1fr); 
  }
  
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .category-card {
    padding: 16px 10px;
    
    &__icon {
      font-size: 32px;
    }
  }
}

@media (max-width: 767px) {
  .hero-section {
    min-height: 420px;
  }
  
  .hero-title {
    font-size: 34px;
    margin-bottom: 12px;
  }
  
  .hero-subtitle {
    font-size: 17px;
    margin-bottom: 28px;
  }
  
  .hero-content {
    padding: 40px 20px;
  }
  
  .hero-search-box :deep(.arco-input-wrapper) {
    height: 46px !important;
  }
  
  .hero-actions {
    flex-direction: column;
    gap: 12px;
    
    .btn-publish,
    .btn-browse {
      width: 100%;
      max-width: 280px;
    }
  }
  
  .category-section {
    padding: 20px 16px;
    margin-top: -16px;
    border-radius: 12px;
  }
  
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
  
  .category-card {
    padding: 14px 8px;
    border-radius: 10px;
    
    &__icon {
      font-size: 28px;
    }
    
    &__label {
      font-size: 12px;
    }
  }
  
  .items-grid-enhanced { 
    grid-template-columns: repeat(2, 1fr); 
    gap: 16px;
  }
  
  .home-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .selected-filters {
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
  }
}
</style>
