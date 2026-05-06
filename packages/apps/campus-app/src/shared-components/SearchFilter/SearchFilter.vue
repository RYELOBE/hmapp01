<template>
  <div class="search-filter">
    <!-- 关键词搜索区 -->
    <div class="search-filter__header">
      <a-input
        v-model="form.keyword"
        placeholder="搜索商品名称、描述..."
        allow-clear
        size="large"
        @clear="handleSearch"
        @press-enter="handleSearch"
      >
        <template #prefix>
          <icon-search />
        </template>
        <template #suffix>
          <a-button type="primary" :loading="loading" @click="handleSearch">
            <template #icon><icon-search /></template>
            搜索
          </a-button>
        </template>
      </a-input>

      <button
        v-if="filterItems.length > 0"
        class="filter-toggle"
        :class="{ active: showFilters }"
        @click="showFilters = !showFilters"
      >
        <icon-filter />
        <span>筛选</span>
        <a-badge
          v-if="activeFilterCount > 0"
          :count="activeFilterCount"
          :max-count="99"
          class="filter-badge"
        />
      </button>
    </div>

    <!-- 筛选条件区域（可折叠） -->
    <transition name="slide">
      <div v-show="showFilters || filterItems.length === 0" class="search-filter__body">
        <a-row :gutter="[16, 16]" align="middle">
          <a-col v-for="item in filterItems" :key="item.field" :xs="24" :sm="12" :md="8" :lg="6">
            <div class="search-filter__field">
              <label v-if="item.label && item.type !== 'input'" class="search-filter__label">
                {{ item.label }}
              </label>

              <a-select
                v-if="item.type === 'select'"
                v-model="form[item.field]"
                :placeholder="item.placeholder || '请选择'"
                allow-clear
                allow-search
                :multiple="!!item.multiple"
                size="large"
                style="width: 100%"
                @change="handleChange"
              >
                <a-option v-for="opt in item.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </a-option>
              </a-select>

              <a-range-input-number
                v-else-if="item.type === 'priceRange'"
                v-model="form[item.field]"
                :placeholder="['最低价格', '最高价格']"
                allow-clear
                size="large"
                style="width: 100%"
                @change="handleChange"
              />

              <a-range-picker
                v-else-if="item.type === 'daterange'"
                v-model="form[item.field]"
                allow-clear
                size="large"
                style="width: 100%"
                @change="handleChange"
              />

              <a-input
                v-else-if="item.type === 'input'"
                v-model="form[item.field]"
                :placeholder="item.placeholder || '请输入'"
                allow-clear
                size="large"
                @clear="handleChange"
                @press-enter="handleSearch"
              />
            </div>
          </a-col>
        </a-row>

        <!-- 操作按钮 -->
        <div class="search-filter__actions">
          <a-space>
            <a-button type="primary" :loading="loading" @click="handleSearch">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
            <slot name="extra-buttons"></slot>
          </a-space>
        </div>
      </div>
    </transition>

    <!-- 已选条件标签展示 -->
    <div v-if="activeFilters.length > 0" class="search-filter__tags">
      <div class="tags-label">已选条件：</div>
      <div class="tags-list">
        <a-tag
          v-for="(filter, index) in activeFilters"
          :key="index"
          closable
          color="blue"
          @close="removeFilter(filter)"
        >
          {{ filter.label }}: {{ filter.valueLabel }}
        </a-tag>
        <a-button type="text" size="small" @click="handleReset">
          清空全部
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import {
  IconSearch,
  IconRefresh,
  IconFilter,
} from "@arco-design/web-vue/es/icon";

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  filterItems: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  collapsible: { type: Boolean, default: true },
});

const emit = defineEmits(["update:modelValue", "search", "reset"]);

const form = reactive({
  keyword: "",
  ...props.modelValue,
});
const showFilters = ref(!props.collapsible);

onMounted(() => {
  Object.assign(form, props.modelValue);
  if (props.modelValue.keyword !== undefined) {
    form.keyword = props.modelValue.keyword;
  }
});

const activeFilterCount = computed(() => {
  let count = 0;
  if (form.keyword) count++;
  props.filterItems.forEach((item) => {
    const value = form[item.field];
    if (Array.isArray(value) ? value.length > 0 : value !== undefined && value !== "" && value !== null) {
      count++;
    }
  });
  return count;
});

const activeFilters = computed(() => {
  const filters = [];

  if (form.keyword) {
    filters.push({
      field: "keyword",
      label: "关键词",
      value: form.keyword,
      valueLabel: form.keyword,
    });
  }

  props.filterItems.forEach((item) => {
    const value = form[item.field];
    if (Array.isArray(value)) {
      if (value.length > 0) {
        let valueLabel = "";
        if (item.type === "priceRange") {
          valueLabel = `${value[0] || 0} - ${value[1] || "∞"}`;
        } else if (item.options) {
          valueLabel = value
            .map((v) => {
              const opt = item.options.find((o) => o.value === v);
              return opt ? opt.label : v;
            })
            .join(", ");
        }
        filters.push({ field: item.field, label: item.label, value, valueLabel });
      }
    } else if (value !== undefined && value !== "" && value !== null) {
      let valueLabel = value;
      if (item.options) {
        const opt = item.options.find((o) => o.value === value);
        valueLabel = opt ? opt.label : value;
      }
      filters.push({ field: item.field, label: item.label, value, valueLabel });
    }
  });

  return filters;
});

function handleChange() {
  emit("update:modelValue", { ...form });
}

function handleSearch() {
  emit("update:modelValue", { ...form });
  emit("search");
}

function handleReset() {
  form.keyword = "";
  props.filterItems.forEach((item) => {
    if (item.type === "daterange" || item.type === "priceRange") {
      form[item.field] = [];
    } else if (item.multiple) {
      form[item.field] = [];
    } else {
      form[item.field] = item.default !== undefined ? item.default : undefined;
    }
  });
  emit("update:modelValue", { ...form });
  emit("reset");
}

function removeFilter(filter) {
  if (filter.field === "keyword") {
    form.keyword = "";
  } else {
    const item = props.filterItems.find((i) => i.field === filter.field);
    if (item?.type === "daterange" || item?.type === "priceRange") {
      form[filter.field] = [];
    } else if (item?.multiple) {
      form[filter.field] = [];
    } else {
      form[filter.field] = undefined;
    }
  }
  emit("update:modelValue", { ...form });
}
</script>

<style lang="scss" scoped>
.search-filter {
  background: var(--color-bg-white, #fff);
  padding: var(--spacing-lg, 24px);
  border-radius: var(--border-radius-medium, 8px);
  margin-bottom: var(--spacing-md, 16px);
  border: 1px solid var(--color-border-2, #e5e6eb);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  &__header {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: var(--spacing-md, 16px);

    .arco-input-wrapper {
      flex: 1;

      :deep(.arco-input-suffix) {
        margin-left: 8px;
      }
    }
  }

  &__body {
    padding-top: var(--spacing-md, 16px);
    border-top: 1px solid var(--color-border-2, #e5e6eb);
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__label {
    font-size: 13px;
    font-weight: 500;
    color: var(--color-text-2, #4e5969);
    white-space: nowrap;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: var(--spacing-md, 16px);
    padding-top: var(--spacing-sm, 12px);
    border-top: 1px dashed var(--color-border-2, #e5e6eb);
  }

  &__tags {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-top: var(--spacing-md, 16px);
    padding-top: var(--spacing-sm, 12px);
    border-top: 1px solid var(--color-border-2, #e5e6eb);

    .tags-label {
      font-size: 13px;
      font-weight: 500;
      color: var(--color-text-2, #4e5969);
      white-space: nowrap;
      line-height: 28px;
    }

    .tags-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      flex: 1;
    }
  }
}

.filter-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: var(--color-fill-1, #f7f8fa);
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: var(--border-radius-medium, 8px);
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text-2, #4e5969);
  transition: all 150ms ease-out;
  white-space: nowrap;

  &:hover {
    background: var(--color-fill-2, #f2f3f5);
    border-color: var(--color-primary, #165DFF);
    color: var(--color-primary, #165DFF);
  }

  &.active {
    background: rgba(22, 93, 255, 0.06);
    border-color: var(--color-primary, #165DFF);
    color: var(--color-primary, #165DFF);
  }

  .filter-badge {
    margin-left: -4px;
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 250ms ease-out;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  max-height: 600px;
  transform: translateY(0);
}
</style>
