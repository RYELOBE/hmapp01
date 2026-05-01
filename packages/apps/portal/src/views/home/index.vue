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
              <a-button type="primary" size="large" @click="$router.push('/home')">
                立即去看看
              </a-button>
            </div>
          </div>
        </a-carousel-item>
      </a-carousel>
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

    <section class="home-grid">
      <a-spin :loading="loading" style="width: 100%">
        <a-row :gutter="[16, 16]">
          <a-col
            v-for="item in items"
            :key="item.id"
            :xs="12"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
          >
            <ItemCard :item="item" @click="goDetail(item)" />
          </a-col>
        </a-row>

        <div v-if="!loading && items.length === 0" class="home-grid__empty">
          <a-empty description="暂无商品，换个关键词试试？">
            <template #image>
              <icon-subscribe-search size="48" />
            </template>
          </a-empty>
        </div>
      </a-spin>
    </section>

    <div v-if="hasMore && !loading" class="home-loadmore">
      <a-button type="outline" long @click="loadMore">查看更多好物</a-button>
    </div>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { IconSubscribeSearch } from "@arco-design/web-vue/es/icon";
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
  {
    title: "新学期，新装备",
    desc: "学长学姐的闲置好物，低价等你来淘",
    bg: "linear-gradient(135deg, #165dff 0%, #4080ff 100%)",
  },
  {
    title: "教材低价转",
    desc: "专业课教材 3 折起，知识不闲置",
    bg: "linear-gradient(135deg, #722ed1 0%, #9254de 100%)",
  },
  {
    title: "闲置换现金",
    desc: "一键发布，让闲置转起来",
    bg: "linear-gradient(135deg, #0fc6c2 0%, #2bd2b7 100%)",
  },
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
  router.push(`/item/${item.id}`);
}

watch(() => route.query.keyword, () => {
  currentPage.value = 1;
  loadData();
});

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
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

.home-category {
  background: #fff;
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
    transition: all 0.2s;
    border: 1px solid transparent;
    font-size: 13px;

    &:hover {
      background: #f2f3f5;
    }

    &--active {
      background: #e6f1ff !important;
      border-color: #165dff;
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
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__count {
    font-size: 14px;
    color: #4e5969;
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
</style>
