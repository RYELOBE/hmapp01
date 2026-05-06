<template>
  <div class="publish-page">
    <div class="header">
      <a-button type="text" @click="goBack">
        <icon-left /> 返回
      </a-button>
      <span>发布商品</span>
      <div style="width: 60px"></div>
    </div>
    <div class="content">
      <a-form :model="form" layout="vertical" style="max-width: 600px">
        <a-form-item label="商品标题">
          <a-input v-model="form.title" placeholder="请输入商品标题" />
        </a-form-item>
        <a-form-item label="商品描述">
          <a-textarea v-model="form.description" placeholder="请输入商品描述" :rows="4" />
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="form.price" :min="0" style="width: 200px" />
        </a-form-item>
        <a-form-item label="成色">
          <a-select v-model="form.condition" placeholder="选择成色">
            <a-option value="全新">全新</a-option>
            <a-option value="九成新">九成新</a-option>
            <a-option value="八成新">八成新</a-option>
            <a-option value="七成新">七成新</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model="form.category" placeholder="选择分类">
            <a-option value="book">图书教材</a-option>
            <a-option value="digital">数码电子</a-option>
            <a-option value="life">生活用品</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handlePublish">发布</a-button>
          <a-button @click="goBack" style="margin-left: 12px">取消</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
const router = useRouter()
const form = ref({
  title: '',
  description: '',
  price: 0,
  condition: '',
  category: ''
})
const goBack = () => router.back()
const handlePublish = () => {
  Message.success('发布成功')
  router.push('/product/list')
}
</script>

<style scoped>
.publish-page { min-height: 100vh; background: var(--color-fill-1); }
.header {
  background: #fff;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.content { max-width: 800px; margin: 40px auto; padding: 24px; background: #fff; border-radius: 8px; }
</style>
