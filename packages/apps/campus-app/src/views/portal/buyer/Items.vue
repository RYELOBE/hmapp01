<template>
  <div class="items-page">
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">🎓 校园二手交易平台</div>
        <h1 class="hero-title">全部商品</h1>
        <p class="hero-subtitle">发现更多优质二手好物 · 让闲置物品找到新主人</p>
      </div>

      <div class="search-wrapper">
        <div class="search-container">
          <a-input-search
            v-model="searchQuery"
            placeholder="输入关键词搜索..."
            allow-clear
            @search="(val) => handleSearch(val)"
            @pressEnter="() => handleSearch(searchQuery.value)"
            @clear="() => handleSearch('')"
          />
        </div>
      </div>
    </section>

    <div class="main-content">
      <div class="content-box">
        <a-row :gutter="[24, 0]">
          <a-col :span="5" class="left-col">
            <aside class="sidebar">
              <div class="sidebar-header">
                <h3 class="sidebar-title">商品分类</h3>
                <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
                  <icon-menu-fold v-if="!sidebarCollapsed" />
                  <icon-menu-unfold v-else />
                </button>
              </div>
              <nav class="category-nav" :class="{ 'nav-collapsed': sidebarCollapsed }">
                <a
                  v-for="cat in categories"
                  :key="cat.value"
                  class="category-item"
                  :class="{ active: selectedCategory === cat.value }"
                  @click="selectCategory(cat.value)"
                >
                  <span class="category-icon">{{ cat.icon }}</span>
                  <span class="category-label" v-show="!sidebarCollapsed">{{ cat.label }}</span>
                </a>
              </nav>
            </aside>
          </a-col>

          <a-col :span="19" class="right-col">
            <div class="right-content">
              <div class="filter-section">
                <div class="filter-group">
                  <div class="filter-row">
                    <span class="filter-label">成色</span>
                    <div class="filter-options">
                      <a-tag
                        v-for="cond in conditions"
                        :key="cond.value"
                        :class="{ active: selectedCondition === cond.value }"
                        class="filter-tag"
                        @click="
                          selectedCondition = selectedCondition === cond.value ? '' : cond.value;
                          currentPage = 1;
                          loadData()
                        "
                      >
                        {{ cond.label }}
                      </a-tag>
                    </div>
                  </div>
                  <div class="filter-row">
                    <span class="filter-label">校区</span>
                    <div class="filter-options">
                      <a-tag
                        v-for="camp in campuses"
                        :key="camp.value"
                        :class="{ active: selectedCampus === camp.value }"
                        class="filter-tag"
                        @click="
                          selectedCampus = selectedCampus === camp.value ? '' : camp.value;
                          currentPage = 1;
                          loadData()
                        "
                      >
                        {{ camp.label }}
                      </a-tag>
                    </div>
                  </div>
                </div>

                <div class="sort-bar">
                  <div class="sort-options">
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'latest' }"
                      @click="sortBy = 'latest'; currentPage = 1; loadData()"
                    >
                      最新发布
                    </a-button>
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'price_asc' }"
                      @click="sortBy = 'price_asc'; currentPage = 1; loadData()"
                    >
                      价格升序
                    </a-button>
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'price_desc' }"
                      @click="sortBy = 'price_desc'; currentPage = 1; loadData()"
                    >
                      价格降序
                    </a-button>
                  </div>
                  <a-button class="reset-btn" type="text" @click="resetFilters">
                    全部重置 <icon-refresh />
                  </a-button>
                </div>
              </div>

              <a-spin :loading="loading" style="width: 100%">
                <div class="card-grid">
                  <a-row :gutter="[20, 20]">
                    <a-col v-for="item in items" :key="item.id" :span="8">
                      <ItemCard
                        :item="formatItem(item)"
                        @click="handleItemClick(item)"
                      />
                    </a-col>
                  </a-row>

                  <div v-if="!loading && items.length === 0" class="empty-state">
                    <a-empty description="暂无商品，换个条件试试？">
                      <template #image>
                        <icon-search size="64" />
                      </template>
                    </a-empty>
                  </div>
                </div>
              </a-spin>
            </div>
          </a-col>
        </a-row>
      </div>

      <CircleStylePagination
        v-if="total > 0"
        v-model:current="currentPage"
        :total="total"
        :page-size="pageSize"
        @change="handlePageChange"
      />
    </div>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  IconSearch,
  IconFilter,
  IconHeart,
  IconStar,
  IconRefresh,
} from "@arco-design/web-vue/es/icon";
import { getItems } from "../../../services/items";
import { getDictOptions, trackClick } from "../../../services/api";
import ItemCard from "../../../components/data/ItemCard/ItemCard.vue";
import CircleStylePagination from "../../../components/common/CircleStylePagination.vue";

const router = useRouter();
const route = useRoute();

const searchQuery = ref("");
const selectedCategory = ref("");
const selectedCondition = ref("");
const selectedCampus = ref("");
const sortBy = ref("latest");
const items = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(8);
const sidebarCollapsed = ref(false);

const categories = ref([]);
const conditions = ref([]);
const campuses = ref([]);

async function loadData() {
  loading.value = true;
  try {
    const keyword = route.query.keyword || searchQuery.value;

    const params = {
      category: selectedCategory.value || undefined,
      keyword: keyword || undefined,
      conditionLevel: selectedCondition.value || undefined,
      campus: selectedCampus.value || undefined,
      sort: sortBy.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      approvedOnly: true
    };

    console.log('[Items] 请求参数:', JSON.stringify(params));

    const res = await getItems(params);

    let data = res;
    if (res && typeof res === "object") {
      if (res.data) data = res.data;
    }

    const rows = data?.items || res?.items || res?.rows || [];

    if (Array.isArray(rows)) {
      items.value = [...rows];
    } else {
      items.value = [];
    }

    total.value =
      data?.totalCount || data?.total || res?.total || res?.totalCount || 0;
  } catch (e) {
    console.error("[Items] 加载商品失败:", e);
    items.value = [];
  } finally {
    loading.value = false;
  }
}

function selectCategory(value) {
  selectedCategory.value = selectedCategory.value === value ? "" : value;
  currentPage.value = 1;

  if (selectedCategory.value) {
    router.push({
      path: "/portal/buyer/items",
      query: { category: selectedCategory.value },
    });
  } else {
    router.push({ path: "/portal/buyer/items", query: {} });
  }
}

function resetFilters() {
  selectedCategory.value = "";
  selectedCondition.value = "";
  selectedCampus.value = "";
  sortBy.value = "latest";
  searchQuery.value = "";
  currentPage.value = 1;
  router.push({ path: "/portal/buyer/items", query: {} });
}

function handlePageChange(page) {
  currentPage.value = page;
  loadData();
  window.scrollTo({ top: 400, behavior: "smooth" });
}

function handleSearch(value) {
  const keyword = value ?? searchQuery.value;
  searchQuery.value = keyword;
  currentPage.value = 1;
  router.push({
    path: "/portal/buyer/items",
    query: keyword ? { keyword } : {},
  });
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
    viewCount: item.viewCount || 0,
    favoriteCount: item.favoriteCount || 0,
  };
}

function handleItemClick(item) {
  trackClick(item.id);
  router.push(`/portal/item/${item.id}`);
}

watch(
  () => route.query,
  () => {
    currentPage.value = 1;
    selectedCategory.value = route.query.category || "";
    searchQuery.value = route.query.keyword || "";
    loadData();
  },
  { immediate: true }
);

async function loadDictOptions() {
  try {
    const res = await getDictOptions();
    const data = res?.data || res;
    categories.value = [{ value: "", label: "全部分类", icon: "📦" }, ...(data?.categories || [])];
    conditions.value = [{ value: "", label: "不限" }, ...(data?.conditions || [])];
    campuses.value = data?.campuses || [];
  } catch (e) {
    console.error('[Items] 加载字典选项失败:', e);
  }
}

onMounted(async () => {
  await loadDictOptions();
  selectedCategory.value = route.query.category || "";
  searchQuery.value = route.query.keyword || "";
  loadData();
});
</script>

<style lang="scss" scoped>
$primary-blue: #165DFF;
$primary-blue-light: #4080FF;
$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;

.items-page {
  background-color: $bg-gray;
  min-height: 100vh;
}

.hero-section {
  position: relative;
  width: 100vw;
  height: 300px;
  margin-left: calc(-50vw + 50%);
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
  background: linear-gradient(
    135deg,
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
  opacity: 0.06;
  background-image:
    radial-gradient(circle at 25% 25%, white 2px, transparent 2px),
    radial-gradient(circle at 75% 75%, white 2px, transparent 2px);
  background-size: 60px 60px;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 24px 56px;
}

.hero-badge {
  display: inline-block;
  padding: 6px 18px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 16px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.hero-title {
  margin: 0 0 10px;
  font-size: 38px;
  font-weight: 800;
  color: $bg-white;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
}

.search-wrapper {
  position: absolute;
  bottom: -28px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 680px;
  z-index: 10;
}

.search-container {
  width: 100%;
  height: 52px;
  display: flex;
  justify-content: center;
  background: $bg-white;
  box-shadow: 0 6px 24px rgba(71, 92, 134, 0.18);
  border-radius: 26px;

  :deep(.arco-input-wrapper) {
    width: 100%;
    height: 100%;
    border-radius: 26px;
    padding: 0 28px;
    box-sizing: border-box;
    border: none;
    background: transparent;
    box-shadow: none;

    .arco-input {
      font-size: 15px;
      color: $text-secondary;

      &::placeholder {
        color: $text-tertiary;
      }
    }

    .arco-input-suffix {
      font-size: 18px;
      color: $primary-blue;
    }
  }
}

.main-content {
  position: relative;
  max-width: 1400px;
  margin: 48px auto 32px;
  padding: 0 32px 40px;
}

.content-box {
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  padding: 24px;
  min-height: 500px;
}

.content-row {
  align-items: stretch;
}

.left-col {
  flex: 0 0 auto !important;
  max-width: 200px !important;
}

.right-col {
  flex: 1 !important;
  min-width: 0;
}

.sidebar {
  background: transparent;
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid #f0f1f3;
  padding-right: 8px;
  margin-right: 8px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 14px;
  border-bottom: 1px solid #f2f3f5;
  flex-shrink: 0;
}

.sidebar-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  transition: all 0.2s ease;

  &:hover {
    background: $bg-gray;
    color: $primary-blue;
  }
}

.category-nav {
  padding: 8px 4px;
  flex: 1;
  overflow-y: auto;

  &.nav-collapsed {
    .category-item {
      justify-content: center;
      padding: 11px 6px;
    }

    .category-icon {
      font-size: 20px;
    }
  }
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: $text-secondary;
  font-size: 14px;
  margin-bottom: 2px;

  &:hover {
    background: #f7f8fa;
    color: $text-primary;
  }

  &.active {
    background: linear-gradient(
      135deg,
      rgba(22, 93, 255, 0.08) 0%,
      rgba(64, 128, 255, 0.06) 100%
    );
    color: $primary-blue;
    font-weight: 500;
  }
}

.category-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 22px;
  text-align: center;
}

.category-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.right-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.filter-group {
  background: #fafbfc;
  border-radius: 10px;
  padding: 14px 18px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 14px;

  &:not(:last-child) {
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eeeef0;
  }
}

.filter-label {
  font-size: 13px;
  color: $text-tertiary;
  font-weight: 500;
  white-space: nowrap;
  min-width: 40px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 500;
  color: $text-secondary;
  background: $bg-white;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;

  &:hover {
    background: #e8f3ff;
    color: $primary-blue;
    border-color: rgba(22, 93, 255, 0.15);
  }

  &.active {
    background: $primary-blue;
    color: $bg-white;
    border-color: $primary-blue;

    &:hover {
      background: #0e42d2;
      color: $bg-white;
    }
  }
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 4px;

  .sort-btn {
    font-size: 13px;
    font-weight: 400;
    color: $text-secondary;
    transition: all 0.2s ease;
    padding: 5px 12px;
    border-radius: 6px;

    &.active {
      color: $primary-blue;
      font-weight: 500;
      background: rgba(22, 93, 255, 0.06);
    }
  }
}

.reset-btn {
  color: $text-secondary;
  font-size: 13px;
  transition: color 0.2s;

  &:hover {
    color: $primary-blue;
  }
}

.card-grid {
  flex: 1;
  min-height: 320px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}

@media (max-width: 1023px) {
  .main-content {
    padding: 36px 20px 32px;
  }

  .left-col {
    display: none;
  }

  .right-col {
    width: 100% !important;
    flex: none !important;
  }

  .search-wrapper {
    width: 95%;
  }
}

@media (max-width: 767px) {
  .hero-section {
    height: 260px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .hero-content {
    padding: 30px 16px 50px;
  }

  .search-wrapper {
    bottom: -24px;
  }

  .search-container {
    height: 46px;
    border-radius: 23px;

    :deep(.arco-input-wrapper) {
      padding: 0 20px;

      .arco-input {
        font-size: 14px;
      }
    }
  }

  .main-content {
    margin-top: 40px;
    padding: 20px 16px 28px;
  }

  .content-box {
    padding: 16px;
  }

  .filter-group {
    padding: 12px 14px;
  }

  .filter-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .filter-label {
    width: 100%;
    margin-bottom: -2px;
  }

  .sort-bar {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
