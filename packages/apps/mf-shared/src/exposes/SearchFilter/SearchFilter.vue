<template>
  <div class="search-filter">
    <a-row :gutter="12" align="center">
      <a-col v-for="item in filterItems" :key="item.field" :span="item.span || 6">
        <!-- 下拉选择 -->
        <a-select
          v-if="item.type === 'select'"
          v-model="form[item.field]"
          :placeholder="item.placeholder || item.label"
          allow-clear
          :multiple="!!item.multiple"
          @change="handleChange"
        >
          <template v-if="item.prefix" #prefix>{{ item.label }}</template>
          <a-option v-for="opt in item.options" :key="opt.value" :value="opt.value">{{ opt.label }}</a-option>
        </a-select>
        <!-- 输入框 -->
        <a-input
          v-else-if="item.type === 'input'"
          v-model="form[item.field]"
          :placeholder="item.placeholder || item.label"
          allow-clear
          @clear="handleChange"
          @press-enter="handleSearch"
        >
          <template v-if="item.prefix" #prefix>{{ item.label }}</template>
        </a-input>
        <!-- 日期范围 -->
        <a-range-picker
          v-else-if="item.type === 'daterange'"
          v-model="form[item.field]"
          allow-clear
          style="width: 100%"
          @change="handleChange"
        >
          <template v-if="item.prefix" #prefix>{{ item.label }}</template>
        </a-range-picker>
      </a-col>
      <a-col :span="buttonSpan" class="search-filter__actions">
        <a-space>
          <a-button type="primary" @click="handleSearch">
            <template #icon><icon-search /></template>查询
          </a-button>
          <a-button @click="handleReset">
            <template #icon><icon-refresh /></template>重置
          </a-button>
          <slot name="extra-buttons"></slot>
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { watch } from "vue";

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  filterItems: { type: Array, default: () => [] },
  buttonSpan: { type: Number, default: 6 },
});

const emit = defineEmits(["update:modelValue", "search", "reset"]);

const form = props.modelValue;

function handleChange() {
  emit("update:modelValue", { ...form });
}

function handleSearch() {
  emit("update:modelValue", { ...form });
  emit("search");
}

function handleReset() {
  // 清空所有筛选字段
  props.filterItems.forEach((item) => {
    if (item.type === "daterange") {
      form[item.field] = [];
    } else {
      form[item.field] = item.default !== undefined ? item.default : undefined;
    }
  });
  emit("update:modelValue", { ...form });
  emit("reset");
}
</script>

<style lang="scss" scoped>
.search-filter {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
</style>
