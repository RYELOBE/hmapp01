<template>
  <div class="item-list-page">
    <!-- 顶部筛选工具栏 -->
    <SearchFilter
      v-model="filterForm"
      :filter-items="filterItems"
      :loading="loading"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 排序工具条 -->
    <div class="sort-bar">
      <div class="sort-bar__left">
        <span class="total-count">共 <strong>{{ total }}</strong> 件商品</span>
      </div>
      <div class="sort-bar__right">
        <a-radio-group v-model="sortField" type="button" size="small" @change="handleSortChange">
          <a-radio value="latest">最新发布</a-radio>
          <a-radio value="price_asc">价格升序</a-radio>
          <a-radio value="price_desc">价格降序</a-radio>
        </a-radio-group>
      </div>
    </div>

    <!-- 商品网格 -->
    <a-spin :loading="loading" class="list-spin">
      <template v-if="itemList.length > 0">
        <a-row :gutter="[16, 16]" class="item-grid">
          <a-col
            v-for="item in itemList"
            :key="item.id"
            :xs="12"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
          >
            <ItemCard :item="item" @click="handleItemClick(item)" />
          </a-col>
        </a-row>
      </template>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="暂无符合条件的商品" />
    </a-spin>

    <!-- 分页器 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <a-pagination
        v-model:current="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="total"
        show-page-size
        show-total
        :page-size-options="['12', '24', '36', '48']"
        size="large"
        @change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import SearchFilter from "commonprovide/SearchFilter";
import ItemCard from "commonprovide/ItemCard";
import { getItems } from '../services/api';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const itemList = ref([]);
const total = ref(0);

const filterForm = reactive({
  keyword: '',
  category: '',
  conditionLevel: '',
  priceRange: [],
});

const filterItems = [
  {
    field: 'category',
    label: '商品分类',
    type: 'select',
    placeholder: '全部分类',
    options: [
      { label: '电子产品', value: 'electronics' },
      { label: '图书教材', value: 'books' },
      { label: '生活用品', value: 'daily' },
      { label: '服饰鞋包', value: 'clothing' },
      { label: '运动户外', value: 'sports' },
      { label: '美妆护肤', value: 'beauty' },
      { label: '其他', value: 'other' },
    ],
  },
  {
    field: 'conditionLevel',
    label: '商品成色',
    type: 'select',
    placeholder: '全部成色',
    options: [
      { label: '全新', value: 'new' },
      { label: '99新', value: '99' },
      { label: '95新', value: '95' },
      { label: '9成新', value: '9' },
      { label: '8成新', value: '8' },
      { label: '7成新及以下', value: '7' },
    ],
  },
  {
    field: 'priceRange',
    label: '价格区间',
    type: 'priceRange',
  },
];

const sortField = ref('latest');

const pagination = reactive({
  current: 1,
  pageSize: 12,
});

function buildQueryParams() {
  const params = {
    page: pagination.current,
    pageSize: pagination.pageSize,
    approvedOnly: true,
  };

  if (filterForm.keyword) {
    params.keyword = filterForm.keyword;
  }

  if (filterForm.category) {
    params.category = filterForm.category;
  }

  if (filterForm.conditionLevel) {
    params.conditionLevel = filterForm.conditionLevel;
  }

  if (filterForm.priceRange && filterForm.priceRange.length === 2) {
    params.minPrice = filterForm.priceRange[0];
    params.maxPrice = filterForm.priceRange[1];
  }

  switch (sortField.value) {
    case 'latest':
      params.sortBy = 'createdAt';
      params.order = 'desc';
      break;
    case 'price_asc':
      params.sortBy = 'price';
      params.order = 'asc';
      break;
    case 'price_desc':
      params.sortBy = 'price';
      params.order = 'desc';
      break;
  }

  return params;
}

function syncQueryToURL() {
  const query = {};

  if (filterForm.keyword) {
    query.keyword = filterForm.keyword;
  }

  if (filterForm.category) {
    query.category = filterForm.category;
  }

  if (filterForm.conditionLevel) {
    query.condition = filterForm.conditionLevel;
  }

  if (sortField.value !== 'latest') {
    query.sort = sortField.value;
  }

  if (pagination.current > 1) {
    query.page = pagination.current;
  }

  if (pagination.pageSize !== 12) {
    query.pageSize = pagination.pageSize;
  }

  router.replace({ query });
}

function initFromURL() {
  const query = route.query;

  if (query.keyword) {
    filterForm.keyword = query.keyword;
  }

  if (query.category) {
    filterForm.category = query.category;
  }

  if (query.condition) {
    filterForm.conditionLevel = query.condition;
  }

  if (query.sort) {
    sortField.value = query.sort;
  }

  if (query.page) {
    pagination.current = parseInt(query.page, 10);
  }

  if (query.pageSize) {
    pagination.pageSize = parseInt(query.pageSize, 10);
  }
}

async function loadItemList() {
  loading.value = true;
  try {
    const params = buildQueryParams();
    const result = await getItems(params);
    itemList.value = result.items || [];
    total.value = result.total || 0;
  } catch (error) {
    Message.error(error.message || '加载商品列表失败');
    itemList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  syncQueryToURL();
  loadItemList();
}

function handleReset() {
  pagination.current = 1;
  sortField.value = 'latest';
  syncQueryToURL();
  loadItemList();
}

function handleSortChange() {
  pagination.current = 1;
  syncQueryToURL();
  loadItemList();
}

function handlePageChange(page) {
  pagination.current = page;
  syncQueryToURL();
  loadItemList();

  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  });
}

function handlePageSizeChange(pageSize) {
  pagination.pageSize = pageSize;
  pagination.current = 1;
  syncQueryToURL();
  loadItemList();
}

function handleItemClick(item) {
  router.push(`/item/${item.id}`);
}

onMounted(() => {
  initFromURL();
  loadItemList();
});
</script>

<style lang="scss" scoped>
.item-list-page {
  min-height: 100vh;
  background: #F5F6F7;
  padding: 20px 24px;
}

.list-spin {
  min-height: 400px;
}

.item-grid {
  margin-bottom: 24px;

  :deep(.arco-col) {
    margin-bottom: 0;
  }
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #FFFFFF;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &__left {
    .total-count {
      font-size: 14px;
      color: #86909C;

      strong {
        color: #165DFF;
        font-weight: 600;
        font-size: 16px;
        margin: 0 2px;
      }
    }
  }

  &__right {
    :deep(.arco-radio-group-button) {
      background: transparent;
      border-color: #E5E6EB;

      .arco-radio-button {
        padding: 6px 12px;
        font-size: 13px;
        color: #4E5969;
        border-color: #E5E6EB;
        transition: all 0.2s;

        &:hover {
          color: #165DFF;
          border-color: #165DFF;
        }

        &.arco-radio-button-checked {
          background: #165DFF;
          border-color: #165DFF;
          color: #fff;
        }
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  :deep(.arco-pagination) {
    .arco-pagination-item,
    .arco-pagination-jumper-text-input,
    .arco-pagination-option-select-view {
      border-color: #E5E6EB;
    }

    .arco-pagination-item-active {
      background: #165DFF;
      border-color: #165DFF;
    }

    .arco-pagination-total {
      color: #86909C;
      font-size: 13px;
    }
  }
}
</style>
