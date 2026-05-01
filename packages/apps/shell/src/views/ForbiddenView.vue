<template>
  <div class="forbidden">
    <a-result :status="status" :title="title" :subtitle="subtitle">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
          <a-button v-if="isMicroAppError" @click="retry">重试</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const reason = computed(() => typeof route.query.reason === "string" ? route.query.reason : "");
const isMicroAppError = computed(() => route.query.type === "micro-app" || Boolean(reason.value));
const status = computed(() => isMicroAppError.value ? "500" : "403");
const title = computed(() => isMicroAppError.value ? "子应用加载失败" : "没有访问权限");
const subtitle = computed(() => isMicroAppError.value
  ? `请检查本地子应用端口、commonprovide remoteEntry 与后端注册配置。${reason.value ? `错误信息：${reason.value}` : ""}`
  : "当前角色无法访问该页面");

function retry() {
  window.location.reload();
}
</script>

<style scoped>
.forbidden {
  min-height: 80vh;
  display: grid;
  place-items: center;
  padding: 24px;
}
</style>
