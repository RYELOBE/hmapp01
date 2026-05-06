<template>
  <div class="order-page">
    <a-layout class="layout-wrapper">
      <a-layout-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/home')">
            <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='45' fill='%23165DFF'/%3E%3Ctext x='50' y='65' font-size='40' fill='white' text-anchor='middle' font-weight='bold'%3E%E4%BA%8C%3C/text%3E%3C/svg%3E" alt="logo" class="logo-img" />
            <span class="logo-text">校园二手交易平台</span>
          </div>
          <div class="header-actions">
            <a-button type="text" @click="$router.push('/home')">首页</a-button>
            <a-button type="text" @click="$router.push('/product/list')">商品列表</a-button>
            <a-button type="text" @click="$router.push('/cart')">购物车</a-button>
            <a-button type="text" @click="$router.push('/user/profile')">个人中心</a-button>
          </div>
        </div>
      </a-layout-header>

      <a-layout-content class="main-content">
        <div class="page-container">
          <a-result status="info" title="我的订单" sub-title="订单管理功能正在开发中...">
            <template #extra>
              <a-button type="primary" @click="$router.push('/home')">返回首页</a-button>
            </template>
          </a-result>
        </div>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background: #F2F3F5;
}

.layout-wrapper {
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
  height: 64px;
  line-height: 64px;
  padding: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-actions :deep(.arco-btn-text) {
  color: white;
  font-size: 15px;
}

.main-content {
  padding: 0;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
</style>
