<template>
  <div class="forbidden-page">
    <a-result status="forbidden" title="暂无权限" sub-title="您没有权限访问此页面">
      <template #extra>
        <a-space>
          <a-button @click="goHome">返回首页</a-button>
          <a-button type="primary" @click="goBack">返回上一页</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goHome() {
  router.push("/portal/home");
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
</style>