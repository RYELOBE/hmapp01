<template>
  <div class="product-list">
    <div class="header">
      <a-button type="text" @click="goBack">
        <icon-left /> 返回
      </a-button>
      <span>商品列表</span>
      <a-button type="primary" size="small" @click="goTo('/product/publish')">发布商品</a-button>
    </div>
    <div class="content">
      <div class="filters">
        <a-select v-model="category" placeholder="选择分类" style="width: 200px">
          <a-option value="">全部分类</a-option>
          <a-option value="book">图书教材</a-option>
          <a-option value="digital">数码电子</a-option>
          <a-option value="life">生活用品</a-option>
        </a-select>
      </div>
      <a-row :gutter="[16, 16]">
        <a-col :span="6" v-for="i in 12" :key="i">
          <a-card hoverable @click="goTo('/product/detail/' + i)">
            <img :src="`https://picsum.photos/200/200?random=${i+20}`" />
            <div class="card-title">二手商品 {{ i }}</div>
            <div class="card-meta">九成新 · 卖家{{ i }}</div>
            <div class="card-price">¥{{ i * 15 + 5 }}</div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const category = ref('')
const goBack = () => router.back()
const goTo = (path) => router.push(path)
</script>

<style scoped>
.product-list { min-height: 100vh; background: var(--color-fill-1); }
.header {
  background: #fff;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.content { max-width: 1200px; margin: 24px auto; padding: 0 24px; }
.filters { margin-bottom: 24px; }
.card-title { margin-top: 12px; font-size: 14px; font-weight: 500; }
.card-meta { font-size: 12px; color: #86909c; margin-top: 4px; }
.card-price { color: #ff7000; font-weight: bold; margin-top: 8px; font-size: 16px; }
</style>
