<template>
  <div class="search-filter">
    <a-row :gutter="16" align="center">
      <a-col v-for="item in filterItems" :key="item.field" :xs="24" :sm="12" :md="8" :lg="6">
        <div class="search-filter__field">
          <span v-if="item.label && item.type !== 'input'" class="search-filter__label">
            {{ item.label }}
          </span>
          <a-select
            v-if="item.type === 'select'"
            v-model="form[item.field]"
            :placeholder="item.placeholder || '请选择'"
            allow-clear
            allow-search
            :multiple="!!item.multiple"
            style="width: 100%"
            @change="handleChange"
          >
            <a-option v-for="opt in item.options" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </a-option>
          </a-select>
          <a-input
            v-else-if="item.type === 'input'"
            v-model="form[item.field]"
            :placeholder="item.placeholder || '请输入'"
            allow-clear
            @clear="handleChange"
            @press-enter="handleSearch"
          >
            <template v-if="item.prefix" #prefix>
              <icon-search />
            </template>
          </a-input>
          <a-range-picker
            v-else-if="item.type === 'daterange'"
            v-model="form[item.field]"
            allow-clear
            style="width: 100%"
            @change="handleChange"
          />
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :md="24" :lg="24" class="search-filter__actions">
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
import { reactive, onMounted } from "vue";
import { IconSearch, IconRefresh } from "@arco-design/web-vue/es/icon";

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  filterItems: { type: Array, default: () => [] },
  buttonSpan: { type: Number, default: 6 },
});

const emit = defineEmits(["update:modelValue", "search", "reset"]);

const form = reactive({ ...props.modelValue });

onMounted(() => {
  Object.assign(form, props.modelValue);
});

function handleChange() {
  emit("update:modelValue", { ...form });
}

function handleSearch() {
  emit("update:modelValue", { ...form });
  emit("search");
}

function handleReset() {
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
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e5e6eb;

  &__field {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
  }

  &__label {
    font-size: 14px;
    color: #4e5969;
    white-space: nowrap;
    min-width: 70px;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
}
</style>
