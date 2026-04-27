<template>
  <div class="home-panel">
    <a-card title="系统概览">
      <a-space direction="vertical" fill>
        <a-typography-paragraph>
          当前为主应用壳层，负责统一登录、角色路由、子应用挂载和 AI 小助手入口。
        </a-typography-paragraph>
        <a-space>
          <a-button type="primary" @click="$router.push('/portal')">进入门户（买家/卖家）</a-button>
          <a-button @click="$router.push('/ops')">进入运营中心</a-button>
        </a-space>
      </a-space>
    </a-card>
  </div>
</template>

<style scoped>
.home-panel {
  padding: 16px;
}
</style>
