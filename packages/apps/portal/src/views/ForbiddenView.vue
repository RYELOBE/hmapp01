<template>
  <div class="forbidden-container">
    <a-result status="403" title="无权限访问" sub-title="抱歉，您没有权限访问此页面">
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.forbidden-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f8ff;
}
</style>
