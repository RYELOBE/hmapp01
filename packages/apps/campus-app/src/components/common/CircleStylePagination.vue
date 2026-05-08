<template>
  <div class="circle-pagination">
    <a-pagination
      :current="current"
      :total="total"
      :page-size="pageSize"
      :buffer-size="2"
      :show-total="showTotal"
      v-bind="$attrs"
      @change="handleChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  current: { type: Number, default: 1 },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 12 },
  showTotal: { type: Boolean, default: false }
});

const emit = defineEmits(["update:current", "change"]);

function handleChange(page) {
  emit("update:current", page);
  emit("change", page);
}
</script>

<style lang="scss" scoped>
.circle-pagination {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 32px;
  padding: 20px 0;

  :deep(.arco-pagination) {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  :deep(.arco-pagination-list) {
    display: flex;
    align-items: center;
    gap: 8px;

    .arco-pagination-item,
    .arco-pagination-item-next,
    .arco-pagination-item-prev,
    .arco-pagination-item-ellipsis {
      min-width: 40px;
      height: 40px;
      border-radius: 50%;
      border: none;
      background: #ffffff;
      color: #4e5969;
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      &:hover:not(.arco-pagination-item-active):not(:disabled) {
        background: #f2f3f5;
        color: #165dff;
        box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
        transform: translateY(-1px);
      }

      .arco-icon {
        font-size: 16px;
      }
    }

    .arco-pagination-item-active {
      background: #165dff !important;
      color: #ffffff !important;
      font-weight: 600;
      box-shadow: 0 4px 14px rgba(22, 93, 255, 0.28);
      border: none !important;

      &:hover {
        background: #0e42d2 !important;
        box-shadow: 0 6px 18px rgba(22, 93, 255, 0.35);
      }
    }

    .arco-pagination-item-next,
    .arco-pagination-item-prev {
      .arco-icon {
        color: #1d2129;
      }

      &:hover .arco-icon {
        color: #165dff;
      }
    }

    .arco-pagination-item-disabled {
      cursor: not-allowed;
      opacity: 0.45;

      &:hover {
        background: #ffffff;
        color: #4e5969;
        transform: none;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }
    }
  }

  :deep(.arco-pagination-total) {
    font-size: 14px;
    color: #86909c;
    margin-right: 8px;
  }

  :deep(.arco-pagination-jumper) {
    margin-left: 8px;
    font-size: 13px;
    color: #86909c;

    input {
      width: 48px;
      height: 32px;
      border-radius: 6px;
      text-align: center;
      border-color: #e5e6eb;
      
      &:focus {
        border-color: #165dff;
      }
    }
  }
}
</style>
