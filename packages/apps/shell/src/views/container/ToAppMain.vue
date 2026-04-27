<template>
  <teleport :to="targetEl">
    <div id="FRAME_WINDOW" />
  </teleport>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from "vue";
import { startMinFrame } from "../../minFrame";
import framePinia from "../../minFrame/pinia/framePinia";

const frame = framePinia();
const targetEl = ref("#TO_FRAME_WINDOW");

onMounted(() => {
  // 缓存 DOM 引用，避免 teleport 动态查找失败
  nextTick(() => {
    const el = document.getElementById("TO_FRAME_WINDOW");
    if (el) {
      targetEl.value = el;
      frame.toEL = el;
    }
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
  }
);

</script>

<!-- 不使用 scoped，确保 #FRAME_WINDOW 样式对 qiankun 子应用可见 -->
<style>
#FRAME_WINDOW {
  width: 100%;
  height: 100%;
  min-height: inherit;
}
</style>
