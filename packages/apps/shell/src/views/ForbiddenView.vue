<template>
  <div class="forbidden">
    <a-result status="403" title="没有访问权限" subtitle="当前角色无法访问该页面">
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<style scoped>
.forbidden {
  min-height: 80vh;
  display: grid;
  place-items: center;
}
</style>
