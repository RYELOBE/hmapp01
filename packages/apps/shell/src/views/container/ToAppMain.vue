<template>
  <div></div>
</template>

<script setup>
import { onMounted, watch, nextTick } from "vue";
import { startMinFrame } from "../../minFrame";
import framePinia from "../../minFrame/pinia/framePinia";

const frame = framePinia();

onMounted(() => {
  nextTick(() => {
    startMinFrame();
  });
});

// 如果 configs 在组件挂载后才加载完毕（异步场景），监听变化再次启动
watch(
  () => frame.configsLoaded,
  (loaded) => {
    if (loaded) {
      startMinFrame();
    }
  },
);
</script>
