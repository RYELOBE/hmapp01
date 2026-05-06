<template>
  <div class="home-page">
    <a-layout class="layout-wrapper">
      <a-layout-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/home')">
            <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='45' fill='%23165DFF'/%3E%3Ctext x='50' y='65' font-size='40' fill='white' text-anchor='middle' font-weight='bold'%3E%E4%BA%8C%3C/text%3E%3C/svg%3E" alt="logo" class="logo-img" />
            <span class="logo-text">校园二手交易平台</span>
          </div>
          <div class="header-actions">
            <a-button type="text" @click="$router.push('/home')" status="primary">首页</a-button>
            <a-button type="text" @click="$router.push('/product/list')">商品列表</a-button>
            <a-button type="text" @click="$router.push('/cart')">购物车</a-button>
            <a-button type="text" @click="$router.push('/user/profile')">个人中心</a-button>
          </div>
        </div>
      </a-layout-header>

      <a-layout-content class="main-content">
        <div class="hero-section">
          <div class="hero-content">
            <h1 class="hero-title">校园闲置好物</h1>
            <p class="hero-subtitle">安全便捷的大学生二手交易平台</p>
            <div class="search-box">
              <a-input-search
                placeholder="搜索你想要的商品..."
                search-button
                size="large"
                style="width: 500px"
                @search="handleSearch"
              >
                <template #search-icon>
                  <icon-search />
                </template>
              </a-input-search>
            </div>
            <div class="hot-keywords">
              <span class="keyword-label">热门搜索：</span>
              <a-tag class="keyword-tag" @click="handleKeywordClick('iPhone')">iPhone</a-tag>
              <a-tag class="keyword-tag" @click="handleKeywordClick('笔记本电脑')">笔记本电脑</a-tag>
              <a-tag class="keyword-tag" @click="handleKeywordClick('教材')">教材</a-tag>
              <a-tag class="keyword-tag" @click="handleKeywordClick('Switch')">Switch</a-tag>
            </div>
          </div>
        </div>

        <div class="page-container">
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">最新上架</h2>
              <a-button type="text" @click="$router.push('/product/list')">查看更多 <icon-right /></a-button>
            </div>
            <div class="product-grid">
              <div
                v-for="product in latestProducts"
                :key="product.id"
                class="product-card"
                @click="goToDetail(product.id)"
              >
                <div class="product-image">
                  <img :src="product.image" :alt="product.title" />
                </div>
                <div class="product-info">
                  <div class="product-title">{{ product.title }}</div>
                  <div class="product-price">¥{{ product.price }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="section">
            <div class="section-header">
              <h2 class="section-title">热门商品</h2>
              <a-button type="text" @click="$router.push('/product/list')">查看更多 <icon-right /></a-button>
            </div>
            <div class="product-grid">
              <div
                v-for="product in hotProducts"
                :key="product.id"
                class="product-card"
                @click="goToDetail(product.id)"
              >
                <div class="product-image">
                  <img :src="product.image" :alt="product.title" />
                </div>
                <div class="product-info">
                  <div class="product-title">{{ product.title }}</div>
                  <div class="product-price">¥{{ product.price }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="categories-section">
            <div class="section-header">
              <h2 class="section-title">商品分类</h2>
            </div>
            <div class="category-grid">
              <div
                v-for="cat in categories"
                :key="cat.key"
                class="category-item"
                @click="handleCategoryClick(cat.key)"
              >
                <div class="category-icon">
                  <component :is="cat.icon" />
                </div>
                <div class="category-name">{{ cat.name }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const latestProducts = ref([
  {
    id: 1,
    title: 'iPhone 14 Pro Max',
    price: 6999,
    image: 'https://picsum.photos/seed/iphone14/400/400'
  },
  {
    id: 4,
    title: 'Switch OLED',
    price: 1899,
    image: 'https://picsum.photos/seed/switch/400/400'
  },
  {
    id: 5,
    title: '小米手环7 Pro',
    price: 299,
    image: 'https://picsum.photos/seed/miband/400/400'
  },
  {
    id: 15,
    title: 'MacBook Air M1',
    price: 4999,
    image: 'https://picsum.photos/seed/macbook/400/400'
  }
])

const hotProducts = ref([
  {
    id: 2,
    title: '高等数学教材全套',
    price: 89,
    image: 'https://picsum.photos/seed/books/400/400'
  },
  {
    id: 3,
    title: '戴森V12吸尘器',
    price: 2599,
    image: 'https://picsum.photos/seed/dyson/400/400'
  },
  {
    id: 7,
    title: '联想拯救者Y7000P',
    price: 5899,
    image: 'https://picsum.photos/seed/laptop/400/400'
  },
  {
    id: 9,
    title: 'AirPods Pro 2代',
    price: 1599,
    image: 'https://picsum.photos/seed/airpods/400/400'
  }
])

const categories = ref([
  { key: 'electronics', name: '电子产品', icon: 'IconPhone' },
  { key: 'books', name: '图书教材', icon: 'IconBook' },
  { key: 'daily', name: '生活用品', icon: 'IconHome' },
  { key: 'sports', name: '运动户外', icon: 'IconFire' },
  { key: 'clothing', name: '服饰箱包', icon: 'IconBag' },
  { key: 'furniture', name: '家居家具', icon: 'IconOffice' },
  { key: 'digital', name: '数码配件', icon: 'IconCustomerService' },
  { key: 'others', name: '其他分类', icon: 'IconMore' }
])

const handleSearch = (value) => {
  console.log('搜索:', value)
  router.push('/product/list')
}

const handleKeywordClick = (keyword) => {
  console.log('关键词:', keyword)
  router.push('/product/list')
}

const handleCategoryClick = (key) => {
  console.log('分类:', key)
  router.push('/product/list')
}

const goToDetail = (id) => {
  router.push(`/product/detail/${id}`)
}
</script>

<style scoped>
.home-page {
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

.hero-section {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  padding: 80px 24px;
  text-align: center;
}

.hero-content {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
}

.search-box {
  margin-bottom: 24px;
}

.hot-keywords {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.keyword-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.keyword-tag {
  cursor: pointer;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
}

.keyword-tag:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main-content {
  padding: 0;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
}

.section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
}

.product-image {
  width: 100%;
  padding-top: 100%;
  background: #F7F8FA;
  position: relative;
}

.product-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 15px;
  color: #1D2129;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #F53F3F;
}

.categories-section {
  margin-top: 48px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;
}

.category-item {
  background: white;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.category-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
}

.category-icon {
  font-size: 32px;
  color: #165DFF;
  margin-bottom: 12px;
}

.category-name {
  font-size: 14px;
  color: #1D2129;
  font-weight: 500;
}
</style>
