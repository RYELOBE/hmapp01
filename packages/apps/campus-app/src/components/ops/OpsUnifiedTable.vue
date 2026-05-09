<template>
  <div class="scene-review-page">
    <div v-if="title || $slots.tabs" class="tab-bar">
      <div v-if="title" class="tab-bar-title" @click="$emit('title-click')">{{ title }}</div>
      <slot name="tabs"></slot>
    </div>

    <div class="scene-review-table">
      <div v-if="$slots.actions || $slots.extra || showSearch" class="action-bar">
        <div class="search-group" style="display: flex; align-items: center; gap: 8px">
          <slot name="actions">
            <div v-if="showSearch" class="search-group">
              <a-input
                v-model="searchKeyword"
                :placeholder="searchPlaceholder"
                style="width: 200px"
                @press-enter="handleSearch"
                allow-clear
              />
            </div>
          </slot>
        </div>
        <div v-if="$slots.extra" style="display: flex; align-items: center; gap: 8px">
          <slot name="extra"></slot>
        </div>
      </div>

      <a-table
        :data="data"
        :columns="columns"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="rowKey"
        :scroll="scroll"
        :stripe="stripe"
        :bordered="bordered"
        :size="size"
        :row-selection="rowSelection"
        :expandable="expandable"
        :default-expand-all-rows="defaultExpandAllRows"
        class="mt-6"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <template v-for="col in slotColumns" #[col.slotName]="scope">
          <slot :name="col.slotName" v-bind="scope"></slot>
        </template>

        <template #empty>
          <slot name="empty">
            <a-empty description="暂无数据" />
          </slot>
        </template>
      </a-table>

      <div v-if="pagination !== false && showCustomPagination" class="custom-pagination">
        <a-pagination
          :current="internalPagination.current"
          :page-size="internalPagination.pageSize"
          :total="internalPagination.total"
          :show-total="true"
          :show-jumper="true"
          :show-page-size="true"
          :page-size-options="internalPagination.pageSizeOptions"
          size="small"
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </div>

    <div v-if="$slots.footer" class="table-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  title: { type: String, default: '' },
  rowKey: { type: [String, Function], default: 'id' },
  scroll: { type: Object, default: null },
  stripe: { type: Boolean, default: true },
  bordered: { type: [Boolean, Object], default: false },
  size: { type: String, default: 'medium', validator: (v) => ['small', 'medium', 'large'].includes(v) },
  rowSelection: { type: Object, default: null },
  expandable: { type: Object, default: null },
  defaultExpandAllRows: { type: Boolean, default: false },
  showSearch: { type: Boolean, default: false },
  searchPlaceholder: { type: String, default: '请输入搜索关键词' },
  pagination: { type: [Object, Boolean], default: true },
  showCustomPagination: { type: Boolean, default: true }
})

const emit = defineEmits([
  'search',
  'reset',
  'page-change',
  'page-size-change',
  'selection-change',
  'row-click',
  'title-click'
])

const searchKeyword = ref('')

const slotColumns = computed(() => {
  return props.columns.filter(col => col.slotName)
})

const internalPagination = computed(() => {
  if (props.pagination === false) return { current: 1, pageSize: 10, total: 0 }

  const defaults = {
    current: 1,
    pageSize: 10,
    total: 0,
    showTotal: true,
    showJumper: true,
    size: 'small',
    showPageSize: true,
    pageSizeOptions: [10, 20, 30, 50, 100]
  }

  if (typeof props.pagination === 'object') {
    return { ...defaults, ...props.pagination }
  }

  return defaults
})

const paginationConfig = computed(() => {
  if (props.pagination === false) return false
  if (props.showCustomPagination) return false

  return {
    current: internalPagination.value.current,
    pageSize: internalPagination.value.pageSize,
    total: internalPagination.value.total,
    showTotal: true,
    size: 'small'
  }
})

function handleSearch() {
  emit('search', searchKeyword.value)
}

function handlePageChange(page) {
  emit('page-change', page)
}

function handlePageSizeChange(size) {
  emit('page-size-change', size)
}

function handleSelectionChange(keys) {
  emit('selection-change', keys)
}

function handleRowClick(record) {
  emit('row-click', record)
}

defineExpose({
  getSearchKeyword: () => searchKeyword.value,
  clearSearch: () => { searchKeyword.value = '' }
})
</script>

<style lang="scss" scoped>
.scene-review-page {
  height: 100%;
  background-color: #fff;
}

.scene-review-table {
  margin: 0 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tab-bar {
  display: flex;
  align-items: center;
  gap: 30px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 16px;
  margin-left: -20px;
  margin-right: -20px;
  padding-left: 20px;
  padding-right: 20px;
  background-color: #fff;

  &-title {
    color: #1d2129;
    font-weight: bold;
    font-size: 18px;
    padding: 0px 40px;
    border-right: 2px solid #e8e8e8;
    line-height: 48px;
    white-space: nowrap;
    cursor: pointer;

    &:hover {
      color: #165DFF;
    }
  }

  :deep(.arco-tabs-nav::before) {
    display: none !important;
  }

  :deep(.arco-tabs-content) {
    display: none;
  }

  :deep(.arco-tabs-tab) {
    font-size: 16px;
    padding: 14px 10px;
    border: 0px;
  }

  :deep(.arco-tabs-tab-active) {
    font-size: 17px;
    font-weight: bold;
  }
}

.line {
  color: #1459fa;
}

:deep(.arco-table) {
  width: 100%;

  .arco-table-th-title {
    font-weight: 700;
  }
}

.mt-6 {
  margin-top: 24px;
}

.table-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
  margin-top: 16px;
}

.custom-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  padding-bottom: 20px;
}

:deep(.arco-pagination) {
  display: flex !important;
  justify-content: end !important;
  align-items: center;
  gap: 8px;
  background: transparent;
  width: 100%;
}

:deep(.arco-pagination-list) {
  display: flex;
}

:deep(.arco-pagination-item) {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #000000d9;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #1677ff;
    color: #1677ff;
    background-color: #f0f8ff;
  }

  &.arco-pagination-item-active {
    background: #1677ff;
    border-color: #1677ff;
    color: #ffffff;
    box-shadow: 0 2px 4px rgba(22, 119, 255, 0.3);
  }
}

:deep(.arco-pagination-jumper) {
  margin-left: 16px;
  color: #000000d9;
  display: flex;
  align-items: center;
  font-size: 14px;
  gap: 8px;

  .arco-pagination-jumper-input {
    width: 80px !important;
    height: 32px !important;
    text-align: center !important;
    border: 1px solid #d9d9d9 !important;
    border-radius: 4px !important;

    :deep(.arco-input-number-input) {
      text-align: center !important;
    }
  }
}

:deep(.arco-pagination-options) {
  margin-left: 16px;

  .arco-select {
    width: 100%;

    .arco-select-view {
      height: 32px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
    }
  }
}

:deep(.arco-pagination-total) {
  margin-right: 16px;
  color: #000000d9;
  font-size: 14px;
}

:deep(.arco-table-row-hover) {
  background-color: #f5f8ff !important;
}
</style>
