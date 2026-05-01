<template>
  <div class="filter-bar" :class="{ 'filter-bar--compact': compact }">
    <a-row :gutter="[16, 12]" align="center">
      <a-col
        v-for="(filter, index) in filters"
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
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue';
import { IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon';

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
  }
});

const emit = defineEmits(['update:modelValue', 'search', 'reset', 'change']);

const filterValues = reactive({});

const filterComponents = {
  input: 'a-input',
  select: 'a-select',
  daterange: 'a-range-picker',
  date: 'a-date-picker',
  cascader: 'a-cascader',
};

function getFilterComponent(type) {
  return filterComponents[type] || 'a-input';
}

function getFilterProps(filter) {
  const baseProps = {
    placeholder: filter.placeholder || `请选择${filter.label || ''}`,
    allowClear: true,
    style: { width: '100%' }
  };

  if (filter.type === 'input') {
    return {
      ...baseProps,
      placeholder: filter.placeholder || `请输入${filter.label || ''}`,
      allowClear: true,
      searchButton: false
    };
  }

  if (filter.type === 'select') {
    return {
      ...baseProps,
      placeholder: filter.placeholder || `请选择${filter.label || ''}`,
      allowSearch: filter.allowSearch !== false,
      options: filter.options || []
    };
  }

  if (filter.type === 'daterange') {
    return {
      ...baseProps,
      placeholder: filter.placeholder || ['开始日期', '结束日期'],
      style: { width: '100%' }
    };
  }

  if (filter.type === 'cascader') {
    return {
      ...baseProps,
      placeholder: filter.placeholder || `请选择${filter.label || ''}`,
      options: filter.options || [],
      allowSearch: true
    };
  }

  return baseProps;
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
  getValues: () => ({ ...filterValues })
});
</script>

<style lang="scss" scoped>
.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e5e6eb;

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
  }
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;

  &__label {
    font-size: 14px;
    color: #4e5969;
    white-space: nowrap;
    min-width: 70px;
  }
}
</style>
