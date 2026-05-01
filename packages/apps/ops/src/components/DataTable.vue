<template>
  <div class="data-table">
    <div v-if="$slots.header || showHeader" class="data-table__header">
      <slot name="header">
        <div class="data-table__title">{{ title }}</div>
      </slot>
    </div>

    <div v-if="showSearch || filterItems.length > 0" class="data-table__filter">
      <slot name="filter">
        <div class="default-filter">
          <a-input-search
            v-if="showSearch"
            v-model="searchKeyword"
            :placeholder="searchPlaceholder"
            style="width: 260px"
            search-button
            @search="handleSearch"
            @press-enter="handleSearch"
          />
          <a-space v-if="filterItems.length > 0">
            <a-select
              v-for="filter in filterItems"
              :key="filter.field"
              v-model="filterValues[filter.field]"
              :placeholder="filter.placeholder || filter.label"
              :style="{ width: filter.width || '120px' }"
              allow-clear
              @change="handleFilterChange"
            >
              <a-option
                v-for="opt in filter.options"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-option>
            </a-select>
          </a-space>
          <a-space v-if="showSearch || filterItems.length > 0">
            <a-button type="primary" @click="handleSearch">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
          </a-space>
        </div>
      </slot>
      <slot name="extra-buttons"></slot>
    </div>

    <a-table
      :data="displayData"
      :loading="loading"
      :pagination="paginationConfig"
      :row-key="rowKey"
      :columns="localColumns"
      :scroll="scroll"
      :row-selection="rowSelection"
      :draggable="draggable"
      @page-change="handlePageChange"
      @page-size-change="handlePageSizeChange"
      @selection-change="handleSelectionChange"
    >
      <template v-for="slot in columnSlots" #[slot]="scopedSlot">
        <slot :name="slot" v-bind="scopedSlot"></slot>
      </template>
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
      <template v-if="$slots.loading" #loading>
        <slot name="loading"></slot>
      </template>
    </a-table>

    <div v-if="$slots.footer" class="data-table__footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  dataSource: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  searchPlaceholder: {
    type: String,
    default: '搜索关键词'
  },
  filterItems: {
    type: Array,
    default: () => []
  },
  pagination: {
    type: Object,
    default: null
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  scroll: {
    type: Object,
    default: null
  },
  rowSelection: {
    type: Object,
    default: null
  },
  draggable: {
    type: [Boolean, Object],
    default: false
  }
});

const emit = defineEmits([
  'page-change',
  'page-size-change',
  'search',
  'reset',
  'filter-change',
  'selection-change',
  'data-change'
]);

const searchKeyword = ref('');
const filterValues = reactive({});
const localData = ref([]);
const selectedKeys = ref([]);

const displayData = computed(() => {
  return props.dataSource.length > 0 ? props.dataSource : localData.value;
});

const localColumns = computed(() => {
  return props.columns.map(col => ({
    ...col,
    align: col.align || 'left'
  }));
});

const columnSlots = computed(() => {
  const slots = [];
  props.columns.forEach(col => {
    if (col.slot) {
      slots.push(col.slot);
    }
  });
  return slots;
});

const paginationConfig = computed(() => {
  if (props.pagination === false) return false;
  return {
    current: 1,
    pageSize: 10,
    total: 0,
    showTotal: true,
    showPageSize: true,
    pageSizeOptions: [10, 20, 50, 100],
    ...(typeof props.pagination === 'object' ? props.pagination : {})
  };
});

function handleSearch() {
  emit('search', { keyword: searchKeyword.value, ...filterValues });
}

function handleReset() {
  searchKeyword.value = '';
  Object.keys(filterValues).forEach(key => {
    filterValues[key] = undefined;
  });
  emit('reset');
  handleSearch();
}

function handleFilterChange() {
  emit('filter-change', filterValues);
}

function handlePageChange(page) {
  emit('page-change', page);
}

function handlePageSizeChange(size) {
  emit('page-size-change', size);
}

function handleSelectionChange(keys) {
  selectedKeys.value = keys;
  emit('selection-change', keys);
}

function setData(data) {
  localData.value = data;
  emit('data-change', data);
}

defineExpose({
  setData,
  getSelectedKeys: () => selectedKeys.value,
  clearSelection: () => { selectedKeys.value = []; }
});
</script>

<style lang="scss" scoped>
.data-table {
  background: var(--color-bg-1);
  border-radius: 8px;
  overflow: hidden;

  &__header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--color-border);
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-1);
  }

  &__filter {
    padding: 16px 20px;
    border-bottom: 1px solid var(--color-border);
    background: #fafafa;

    .default-filter {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }
  }

  &__footer {
    padding: 16px 20px;
    border-top: 1px solid var(--color-border);
    background: #fafafa;
  }

  :deep(.arco-table) {
    .arco-table-th {
      background: #fafafa;
      font-weight: 600;
    }
  }
}
</style>
