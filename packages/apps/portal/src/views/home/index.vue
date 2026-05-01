<template>
  <div class="home-page">
    <!-- Banner 轮播 -->
    <section class="home-banner">
      <a-carousel auto-play :interval="4000" indicator-type="dot" class="home-banner__carousel">
        <a-carousel-item v-for="(banner, idx) in banners" :key="idx">
          <div class="home-banner__slide" :style="{ background: banner.bg }">
            <div class="home-banner__text">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.desc }}</p>
            </div>
          </div>
        </a-carousel-item>
      </a-carousel>
    </section>

    <!-- 分类导航 -->
    <section class="home-category">
      <div class="home-category__scroll">
        <div
          v-for="cat in CATEGORIES"
          :key="cat.value"
          class="home-category__item"
          :class="{ 'home-category__item--active': selectedCategory === cat.value }"
          @click="selectCategory(cat.value)"
        >
          <span class="home-category__icon">{{ cat.icon }}</span>
          <span class="home-category__label">{{ cat.label }}</span>
        </div>
      </div>
    </section>

    <!-- 排序筛选栏 -->
    <section class="home-toolbar">
      <div class="home-toolbar__left">
        <span class="home-toolbar__count">共 {{ total }} 件好物</span>
      </div>
      <div class="home-toolbar__right">
        <a-select v-model="sortBy" size="small" :options="SORT_OPTIONS" style="width: 140px" @change="handleSort" />
      </div>
    </section>

    <!-- 商品卡片网格 -->
    <section class="home-grid">
      <a-spin :loading="loading" style="width: 100%">
        <div class="home-grid__inner">
          <ItemCard
            v-for="item in items"
            :key="item.id"
            :item="item"
            @click="goDetail(item)"
          />
        </div>
        <!-- 空状态 -->
        <div v-if="!loading && items.length === 0" class="home-grid__empty">
          <span class="home-grid__empty-icon">🔍</span>
          <p>暂无商品，换个关键词试试？</p>
        </div>
      </a-spin>
    </section>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="home-loadmore">
      <a-button type="outline" long @click="loadMore">查看更多好物</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getItems } from "../../services/api";
import ItemCard from "commonprovide/ItemCard";
import { CATEGORIES, SORT_OPTIONS } from "./const";

const router = useRouter();
const route = useRoute();

const items = ref([]);
const loading = ref(false);
const total = ref(0);
const selectedCategory = ref("");
const sortBy = ref("latest");
const currentPage = ref(1);
const pageSize = 20;
const hasMore = ref(false);

const banners = [
  { title: "新学期，新装备", desc: "学长学姐的闲置好物，低价等你来淘", bg: "linear-gradient(135deg, #0fc6c2 0%, #2bd2b7 100%)" },
  { title: "教材低价转", desc: "专业课教材 3 折起，知识不闲置", bg: "linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)" },
  { title: "闲置换现金", desc: "一键发布，让闲置转起来 ♻️", bg: "linear-gradient(135deg, #f0a838 0%, #fcc46d 100%)" },
];

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
  selectedCategory.value = val;
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
  router.push(`/item/${item.id}`);
}

// 监听路由搜索关键词变化
watch(() => route.query.keyword, () => {
  currentPage.value = 1;
  loadData();
});

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
$portal-primary: #0fc6c2;
$portal-primary-light: #e8faf9;

.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ── Banner ────────────────────────────
.home-banner {
  &__carousel {
    border-radius: 16px;
    overflow: hidden;
  }

  &__slide {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  &__text {
    text-align: center;

    h2 {
      margin: 0 0 8px;
      font-size: 28px;
      font-weight: 700;
    }
    p {
      margin: 0;
      font-size: 16px;
      opacity: 0.9;
    }
  }
}

// ── 分类导航 ────────────────────────────
.home-category {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;

  &__scroll {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    scrollbar-width: none;

    &::-webkit-scrollbar { display: none; }
  }

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 10px 16px;
    border-radius: 12px;
    cursor: pointer;
    flex-shrink: 0;
    transition: all 0.2s;

    &:hover { background: #f2f3f5; }

    &--active {
      background: $portal-primary-light;
      .home-category__label { color: $portal-primary; font-weight: 600; }
    }
  }

  &__icon { font-size: 24px; }
  &__label { font-size: 12px; color: #4e5969; white-space: nowrap; }
}

// ── 工具栏 ────────────────────────────
.home-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__count { font-size: 14px; color: #86909c; }
}

// ── 商品网格 ────────────────────────────
.home-grid {
  &__inner {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 80px 0;
    color: #86909c;

    &-icon { font-size: 48px; color: #c9cdd4; margin-bottom: 12px; }
  }
}

// ── 加载更多 ────────────────────────────
.home-loadmore {
  padding: 20px 0;
}
</style>
