<template>
  <div class="filter-bar" :class="{ 'filter-bar--compact': compact, 'filter-bar--collapsed': collapsed }">
    <a-row :gutter="[16, 12]" align="center">
      <a-col
        v-for="(filter, index) in visibleFilters"
        :key="filter.field"
        :xs="24"
        :sm="filter.span || 12"
        :md="filter.span || 8"
        :lg="filter.span || 6"
      >
        <div class="filter-item">
          <span v-if="filter.label" class="filter-item__label">
            {{ filter.label }}
          </span>
          <component
            :is="getFilterComponent(filter.type)"
            v-model="filterValues[filter.field]"
            v-bind="getFilterProps(filter)"
            @change="handleChange"
            @search="handleChange"
          />
        </div>
      </a-col>

      <!-- 操作按钮 -->
      <a-col :xs="24" :sm="24" :md="actionsSpan || 24" class="filter-bar__actions">
        <a-space>
          <a-button type="primary" @click="handleSearch">
            <template #icon><icon-search /></template>
            查询
          </a-button>
          <a-button @click="handleReset">
            <template #icon><icon-refresh /></template>
            重置
          </a-button>
          <slot name="extra-buttons"></slot>
          
          <!-- 折叠/展开按钮 -->
          <a-button
            v-if="filters.length > maxVisible"
            type="text"
            size="small"
            @click="toggleCollapse"
          >
            {{ collapsed ? '展开' : '收起' }}
            <icon-up v-if="!collapsed" />
            <icon-down v-else />
          </a-button>
        </a-space>

        <!-- 条件数量badge -->
        <a-badge
          v-if="activeFilterCount > 0"
          :count="activeFilterCount"
          :max-count="99"
          class="filter-count-badge"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue';
import { IconSearch, IconRefresh, IconUp, IconDown } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  filters: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({})
  },
  compact: {
    type: Boolean,
    default: false
  },
  actionsSpan: {
    type: Number,
    default: null
  },
  maxVisible: {
    type: Number,
    default: 6
  }
});

const emit = defineEmits(['update:modelValue', 'search', 'reset', 'change']);

const filterValues = reactive({});
const collapsed = ref(false);

const filterComponents = {
  input: 'a-input',
  select: 'a-select',
  daterange: 'a-range-picker',
  date: 'a-date-picker',
  cascader: 'a-cascader',
};

const visibleFilters = computed(() => {
  if (collapsed.value) {
    return props.filters.slice(0, props.maxVisible);
  }
  return props.filters;
});

const activeFilterCount = computed(() => {
  let count = 0;
  Object.entries(filterValues).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      if (Array.isArray(value) ? value.length > 0 : true) {
        count++;
      }
    }
  });
  return count;
});

function getFilterComponent(type) {
  return filterComponents[type] || 'a-input';
}

function getFilterProps(filter) {
  const baseProps = {
    placeholder: filter.placeholder || `请选择${filter.label || ''}`,
    allowClear: true,
    style: { width: '100%' }
  };

  switch (filter.type) {
    case 'input':
      return {
        ...baseProps,
        placeholder: filter.placeholder || `请输入${filter.label || ''}`,
        allowClear: true,
        searchButton: false,
        maxLength: filter.maxLength
      };

    case 'select':
      return {
        ...baseProps,
        placeholder: filter.placeholder || `请选择${filter.label || ''}`,
        allowSearch: filter.allowSearch !== false,
        options: filter.options || [],
        multiple: filter.multiple || false
      };

    case 'daterange':
      return {
        ...baseProps,
        placeholder: filter.placeholder || ['开始日期', '结束日期'],
        style: { width: '100%' }
      };

    case 'cascader':
      return {
        ...baseProps,
        placeholder: filter.placeholder || `请选择${filter.label || ''}`,
        options: filter.options || [],
        allowSearch: true,
        multiple: filter.multiple || false
      };

    default:
      return baseProps;
  }
}

function handleChange() {
  emit('update:modelValue', { ...filterValues });
  emit('change', { ...filterValues });
}

function handleSearch() {
  emit('update:modelValue', { ...filterValues });
  emit('search', { ...filterValues });
}

function handleReset() {
  props.filters.forEach(filter => {
    const defaultVal = filter.default !== undefined ? filter.default : undefined;
    filterValues[filter.field] = defaultVal;
  });
  emit('update:modelValue', { ...filterValues });
  emit('reset');
}

function toggleCollapse() {
  collapsed.value = !collapsed.value;
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    Object.assign(filterValues, newVal);
  }
}, { immediate: true, deep: true });

onMounted(() => {
  props.filters.forEach(filter => {
    if (filter.default !== undefined) {
      filterValues[filter.field] = filter.default;
    }
  });
});

defineExpose({
  reset: handleReset,
  search: handleSearch,
  getValues: () => ({ ...filterValues }),
  getActiveCount: () => activeFilterCount.value
});
</script>

<style lang="scss" scoped>
.filter-bar {
  background: var(--color-bg-white, #FFF);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border-1, #E5E6EB);
  transition: all 0.25s ease;

  &--compact {
    padding: 16px;

    .filter-item {
      margin-bottom: 12px;
    }
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    position: relative;

    .filter-count-badge {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  &:hover {
    border-color: var(--color-primary-light-3, #86b1ff);
  }
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;

  &__label {
    font-size: 14px;
    color: var(--color-text-2, #4E5969);
    white-space: nowrap;
    min-width: 70px;
    font-weight: 500;
    
    &::after {
      content: ':';
      margin-left: 2px;
    }
  }
}
</style>
