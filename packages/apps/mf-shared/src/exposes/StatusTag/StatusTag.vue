<template>
  <a-tag :color="color || colorMap[status] || 'default'">
    {{ label || labelMap[status] || status }}
  </a-tag>
</template>

<script setup>
import { ITEM_STATUS, ORDER_STATUS, ITEM_CONDITION, PAYMENT_METHODS, USER_TYPES, TRADE_METHODS } from "@campus/common/constants";
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'auto', // 'auto' | 'item' | 'order' | 'condition' | 'payment' | 'user' | 'trade'
  },
  color: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    default: '',
  }
});

// 所有状态映射表
const allMaps = {
  item: ITEM_STATUS,
  order: ORDER_STATUS,
  condition: ITEM_CONDITION,
  payment: PAYMENT_METHODS,
  user: USER_TYPES,
  trade: TRADE_METHODS,
};

// 自动检测状态类型
const detectType = (status) => {
  for (const [type, map] of Object.entries(allMaps)) {
    if (map[status]) return type;
  }
  return 'item'; // 默认使用 item
};

// 根据 type 获取对应的映射
const currentMap = computed(() => {
  const type = props.type === 'auto' ? detectType(props.status) : props.type;
  return allMaps[type] || ITEM_STATUS;
});

// 动态生成 colorMap 和 labelMap
const colorMap = computed(() => {
  const map = {};
  for (const [key, val] of Object.entries(currentMap.value)) {
    // 适配不同常量的结构：有的有 color 属性，有的没有
    map[key] = val.color || 'default';
  }
  return map;
});

const labelMap = computed(() => {
  const map = {};
  for (const [key, val] of Object.entries(currentMap.value)) {
    // 适配不同常量的结构：有的有 label 属性，有的有 value 属性
    map[key] = val.label || val.value || key;
  }
  return map;
});
</script>
