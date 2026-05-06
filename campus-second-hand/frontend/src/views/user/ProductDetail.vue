<template>
  <div class="product-detail">
    <a-affix :offset-top="0">
      <div class="header">
        <a-button type="text" @click="goBack">
          <icon-left /> 返回
        </a-button>
        <span>商品详情</span>
        <div style="width: 60px"></div>
      </div>
    </a-affix>
    <div class="content">
      <div class="product-image">
        <img src="https://picsum.photos/400/400?random=100" alt="商品图片" />
      </div>
      <div class="product-info">
        <h2>{{ product.title }}</h2>
        <div class="price">¥{{ product.price }}</div>
        <div class="meta">
          <span>卖家：{{ product.seller }}</span>
          <span>成色：{{ product.condition }}</span>
        </div>
        <div class="description">{{ product.description }}</div>
        <div class="actions">
          <a-button @click="addToCart">加入购物车</a-button>
          <a-button type="primary">立即购买</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const product = ref({
  id: 1,
  title: '二手商品示例',
  price: 99,
  seller: '校园用户',
  condition: '九成新',
  description: '这是一款优质的二手商品，欢迎选购。'
})

const goBack = () => router.back()
const addToCart = () => {
  Message.success('已加入购物车')
}
</script>

<style scoped>
.product-detail { min-height: 100vh; background: var(--color-fill-1); }
.header {
  background: #fff;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.content { max-width: 1000px; margin: 24px auto; padding: 0 24px; display: flex; gap: 40px; }
.product-image { width: 400px; }
.product-image img { width: 100%; border-radius: 8px; }
.product-info { flex: 1; }
.price { font-size: 28px; color: #ff7000; font-weight: bold; margin: 16px 0; }
.meta { color: #86909c; margin-bottom: 16px; }
.meta span { margin-right: 16px; }
.description { color: #646a73; line-height: 1.6; margin-bottom: 24px; }
.actions { display: flex; gap: 12px; }
</style>
