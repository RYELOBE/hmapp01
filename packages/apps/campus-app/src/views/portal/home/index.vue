<template>
  <div class="home-page">
    <section class="home-banner">
      <a-carousel
        auto-play
        :interval="4000"
        indicator-type="dot"
        show-arrow="hover"
        class="home-banner__carousel"
      >
        <a-carousel-item v-for="(banner, idx) in banners" :key="idx">
          <div class="home-banner__slide" :style="{ background: banner.bg }">
            <div class="home-banner__text">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.desc }}</p>
              <a-button type="primary" size="large" @click="$router.push('/portal/home')">
                立即去看看
              </a-button>
            </div>
          </div>
        </a-carousel-item>
      </a-carousel>
    </section>

    <section class="search-enhanced">
      <a-input-search
        v-model="searchQuery"
        :style="{ width: '60%' }"
        :search-button="true"
        placeholder="搜索你想要的宝贝..."
        size="large"
        allow-clear
        @search="handleSearch"
        @pressEnter="handleSearch"
        class="search-box-pill"
      >
        <template #prefix>
          <icon-search style="color: #86909C; font-size: 18px;" />
        </template>
        <template #search-button>
          <span>搜一搜</span>
        </template>
      </a-input-search>
    </section>

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

      <a-link class="clear-filters" @click="clearAllFilters">
        清空
      </a-link>
    </div>

    <section class="home-quick-actions">
      <div
        v-for="action in quickActions"
        :key="action.id"
        class="quick-action-card"
        @click="handleQuickAction(action)"
      >
        <span class="quick-action-card__icon">{{ action.icon }}</span>
        <span class="quick-action-card__label">{{ action.label }}</span>
      </div>
    </section>

    <section class="home-category">
      <div class="home-category__scroll">
        <a-tag
          v-for="cat in CATEGORIES"
          :key="cat.value"
          :color="selectedCategory === cat.value ? 'arcoblue' : 'default'"
          class="home-category__item"
          :class="{ 'home-category__item--active': selectedCategory === cat.value }"
          @click="selectCategory(cat.value)"
        >
          <span class="home-category__icon">{{ cat.icon }}</span>
          <span class="home-category__label">{{ cat.label }}</span>
        </a-tag>
      </div>
    </section>

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

    <section class="items-grid-enhanced">
      <a-spin :loading="loading" style="width: 100%">
        <div class="items-grid-container">
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
import ItemCard from "../../../shared-components/ItemCard/ItemCard.vue";
import { CATEGORIES, SORT_OPTIONS } from "./const";
import { debounce } from "@campus/common";

const router = useRouter();
const route = useRoute();

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

const banners = [
  {
    title: "新学期，新装备",
    desc: "学长学姐的闲置好物，低价等你来淘",
    bg: "linear-gradient(135deg, #165DFF 0%, #4080FF 100%)",
  },
  {
    title: "教材低价转",
    desc: "专业课教材 3 折起，知识不闲置",
    bg: "linear-gradient(135deg, #722ED1 0%, #9254DE 100%)",
  },
  {
    title: "闲置换现金",
    desc: "一键发布，让闲置转起来",
    bg: "linear-gradient(135deg, #0FC6C2 0%, #2BD2B7 100%)",
  },
];

const quickActions = [
  {
    id: "all",
    label: "全部商品",
    icon: "📦",
    action: () => {
      selectedCategory.value = "";
      currentPage.value = 1;
      loadData();
    },
  },
  {
    id: "latest",
    label: "最新上架",
    icon: "✨",
    action: () => {
      sortBy.value = "latest";
      currentPage.value = 1;
      loadData();
    },
  },
  {
    id: "hot",
    label: "热门推荐",
    icon: "🔥",
    action: () => {
      router.push({ path: "/portal/home", query: { keyword: "" } });
    },
  },
  {
    id: "sale",
    label: "优惠活动",
    icon: "🎉",
    action: () => {
      router.push("/portal/home");
    },
  },
];

function handleQuickAction(action) {
  if (action.action) {
    action.action();
  }
}

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
    console.error("[Home] loadData error:", e);
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

function goDetail(item) {
  router.push(`/portal/item/${item.id}`);
}

const debouncedSearch = debounce((keyword) => {
  router.push({ path: '/portal/home', query: { keyword } });
}, 300);

function handleSearch(value) {
  console.log('搜索:', value || searchQuery.value);
  debouncedSearch(value || searchQuery.value);
}

function removeFilter(index) {
  selectedFilters.value.splice(index, 1);
}

function clearAllFilters() {
  selectedFilters.value = [];
}

function handleBuy(item) {
  console.log('立即购买:', item);
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
$primary-color: #165DFF;
$bg-color: #F5F6F7;
$card-bg: #FFFFFF;

.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 40px;
}

.home-banner {
  &__carousel {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  &__slide {
    height: 220px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  &__text {
    text-align: center;

    h2 {
      margin: 0 0 12px;
      font-size: 32px;
      font-weight: 700;
    }

    p {
      margin: 0 0 20px;
      font-size: 16px;
      opacity: 0.9;
    }
  }
}

.home-quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-action-card {
  background: $card-bg;
  border-radius: 8px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    font-size: 36px;
  }

  &__label {
    font-size: 14px;
    font-weight: 500;
    color: #1D2129;
  }
}

.home-category {
  background: $card-bg;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__scroll {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    scrollbar-width: none;
    padding-bottom: 4px;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 20px;
    cursor: pointer;
    flex-shrink: 0;
    transition: all 0.2s ease;
    border: 1px solid transparent;
    font-size: 13px;

    &:hover {
      background: #F2F3F5;
    }

    &--active {
      background: #E6F1FF !important;
      border-color: $primary-color;
    }
  }

  &__icon {
    font-size: 16px;
  }

  &__label {
    white-space: nowrap;
  }
}

.home-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: $card-bg;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__count {
    font-size: 14px;
    color: #4E5969;
  }
}

.home-grid {
  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60px 0;
  }
}

.home-loadmore {
  padding: 20px 0;
}

.search-enhanced {
  display: flex;
  justify-content: center;
  padding: 32px 0 24px 0;
}

.search-box-pill {
  max-width: 680px;
}

.search-box-pill :deep(.arco-input-wrapper) {
  border-radius: 20px !important;
  height: 48px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease-out;
}

.search-box-pill :deep(.arco-input-wrapper:hover),
.search-box-pill :deep(.arco-input-wrapper:focus-within) {
  border-color: #165DFF !important;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.15);
}

.search-box-pill :deep(.arco-input) {
  font-size: 15px;
  color: #1D2129;
}

.search-box-pill :deep(.arco-btn) {
  border-radius: 18px !important;
  margin-right: 2px;
  font-weight: 600;
}

.selected-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #FAFBFC;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  height: 28px;
  line-height: 26px;
  padding: 0 12px;
  border-radius: 4px;
  font-size: 13px;
}

.clear-filters {
  color: #86909C;
  font-size: 14px;
  white-space: nowrap;
  margin-left: 16px;
}

.clear-filters:hover {
  color: #165DFF;
}

.items-grid-enhanced {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  padding: 0 0 40px 0;
}

.items-grid-container {
  display: contents;
}

@media (min-width: 1280px) {
  .items-grid-enhanced { grid-template-columns: repeat(5, 1fr); }
}
@media (min-width: 1024px) and (max-width: 1279px) {
  .items-grid-enhanced { grid-template-columns: repeat(4, 1fr); }
}
@media (min-width: 768px) and (max-width: 1023px) {
  .items-grid-enhanced { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 767px) {
  .items-grid-enhanced { grid-template-columns: repeat(2, 1fr); }
}

.price-number {
  font-size: 18px;
  color: #F53F3F;
  font-weight: 700;
}

.price-original {
  font-size: 12px;
  color: #C9CDD4;
  text-decoration: line-through;
  margin-left: 4px;
}

.buy-btn {
  width: 100%;
  height: 36px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.25s ease-out;
}

.buy-btn:hover {
  background-color: #4080FF !important;
  transform: translateY(-1px);
}

.load-more-section {
  display: flex;
  justify-content: center;
  padding: 32px 0 48px 0;
}

.load-more-btn {
  min-width: 200px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 50%, #0E42D2 100%);
  border: none;
  border-radius: 9999px;
  color: #ffffff;
  transition: all 0.3s ease-out;
}

.load-more-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(64, 128, 255, 0.35);
}
</style>
