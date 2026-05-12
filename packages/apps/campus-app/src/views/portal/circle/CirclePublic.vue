<template>
  <div class="circle-page">
    <!-- 大蓝渐变Hero区域（与全部商品一致） -->
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">🎓 校园闲置物品流转平台</div>
        <h1 class="hero-title">校园圈子</h1>
        <p class="hero-subtitle">分享你的校园生活 · 发现有趣的人和事</p>
      </div>

      <!-- 居中搜索框 -->
      <div class="search-wrapper">
        <div class="search-container">
          <a-input-search
            v-model="searchQuery"
            placeholder="搜索话题、动态、用户..."
            allow-clear
            @search="(val) => handleSearch(val)"
          />
        </div>
      </div>
    </section>

    <!-- 主内容区（左右分栏） -->
    <div class="main-content">
      <div class="content-box">
        <a-row :gutter="[24, 0]">
          <!-- 左侧分类栏 -->
          <a-col :span="5" class="left-col">
            <aside class="sidebar">
              <div class="sidebar-header">
                <h3 class="sidebar-title">话题分类</h3>
                <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
                  <icon-menu-fold v-if="!sidebarCollapsed" />
                  <icon-menu-unfold v-else />
                </button>
              </div>
              <nav class="category-nav" :class="{ 'nav-collapsed': sidebarCollapsed }">
                <a
                  v-for="cat in categories"
                  :key="cat.value"
                  class="category-item"
                  :class="{ active: selectedCategory === cat.value }"
                  @click="selectCategory(cat.value)"
                >
                  <span class="category-icon">{{ cat.icon }}</span>
                  <span class="category-label" v-show="!sidebarCollapsed">{{ cat.label }}</span>
                </a>
              </nav>
            </aside>
          </a-col>

          <!-- 右侧内容区 -->
          <a-col :span="19" class="right-col">
            <div class="right-content">
              <!-- 热门标签（浏览量Top3） -->
              <div class="filter-section">
                <div class="filter-group">
                  <div class="filter-row">
                    <span class="filter-label">🔥 热门话题</span>
                    <div class="filter-options">
                      <a-tag
                        v-for="(tag, index) in hotTags.slice(0, 3)"
                        :key="tag.value"
                        :class="{ active: selectedTag === tag.value, 'top-tag': index < 3 }"
                        class="filter-tag"
                        @click="
                          selectedTag = selectedTag === tag.value ? '' : tag.value;
                          currentPage = 1;
                          loadPosts()
                        "
                      >
                        {{ tag.label }}
                      </a-tag>
                    </div>
                  </div>
                </div>

                <!-- 排序栏 -->
                <div class="sort-bar">
                  <div class="sort-options">
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'latest' }"
                      @click="sortBy = 'latest'; currentPage = 1; loadPosts()"
                    >
                      最新发布
                    </a-button>
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'hot' }"
                      @click="sortBy = 'hot'; currentPage = 1; loadPosts()"
                    >
                      最热讨论
                    </a-button>
                    <a-button
                      class="sort-btn"
                      type="text"
                      :class="{ active: sortBy === 'comments' }"
                      @click="sortBy = 'comments'; currentPage = 1; loadPosts()"
                    >
                      评论最多
                    </a-button>
                  </div>
                  <a-button class="reset-btn" type="text" @click="resetFilters">
                    全部重置 <icon-refresh />
                  </a-button>
                </div>
              </div>

              <!-- 动态卡片网格（3列） -->
              <a-spin :loading="loading" style="width: 100%">
                <div class="card-grid">
                  <a-row :gutter="[20, 20]">
                    <a-col v-for="post in posts" :key="post.id" :span="8">
                      <div class="post-card" @click="handlePostClick(post)">
                        <!-- 图片区域（1:1正方形，与商品卡片一致） -->
                        <div class="card-image-wrapper">
                          <div v-if="post.images && post.images.length > 0" class="card-image">
                            <img :src="post.images[0]" :alt="post.title" />
                            <div v-if="post.images.length > 1" class="image-count">
                              <icon-image /> {{ post.images.length }}
                            </div>
                          </div>
                          <!-- 无图占位 -->
                          <div v-else class="card-image card-no-image">
                            <icon-image style="font-size: 48px; color: #c9cdd4;" />
                          </div>
                        </div>

                        <!-- 内容区域 -->
                        <div class="card-body">
                          <h3 class="card-title">{{ post.title || '分享动态' }}</h3>

          <!-- 标签 -->
                          <div v-if="post.tags && post.tags.length > 0" class="card-tags">
                            <span v-for="tag in post.tags.slice(0, 2)" :key="tag" class="tag">#{{ tag }}</span>
                          </div>

          <!-- 底部信息 -->
                          <div class="card-footer">
                            <div class="author-info">
                              <a-avatar :size="24" class="author-avatar">
                                {{ (post.authorName || '用')[0] }}
                              </a-avatar>
                              <span class="author-name">{{ post.authorName || '匿名' }}</span>
                            </div>
                            <div class="post-stats">
                              <span class="stat-item">
                                <icon-eye /> {{ post.viewCount || 0 }}
                              </span>
                              <span class="stat-item">
                                <icon-heart-fill /> {{ post.likeCount || 0 }}
                              </span>
                              <span class="stat-item">
                                <icon-message /> {{ post.commentCount || 0 }}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </a-col>
                  </a-row>

                  <!-- 空状态 -->
                  <div v-if="!loading && posts.length === 0" class="empty-state">
                    <a-empty description="暂无动态，快来发布第一条吧！">
                      <template #image>
                        <icon-heart-fill size="64" style="color: #c9cdd4;" />
                      </template>
                    </a-empty>
                  </div>
                </div>
              </a-spin>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 分页（使用与全部商品一致的圆形分页组件） -->
      <CircleStylePagination
        v-if="total > 0"
        v-model:current="currentPage"
        :total="total"
        :page-size="pageSize"
        @change="handlePageChange"
      />
    </div>

    <a-back-top :visible-height="300" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconHeart,
  IconMessage,
  IconEye,
  IconImage,
  IconHeartFill,
  IconMenuFold,
  IconMenuUnfold,
  IconRefresh,
} from '@arco-design/web-vue/es/icon';
import CircleStylePagination from '../../../components/common/CircleStylePagination.vue';

const router = useRouter();
const route = useRoute();

// 状态管理
const searchQuery = ref('');
const selectedCategory = ref('');
const selectedTag = ref('');
const sortBy = ref('latest');
const posts = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(9); // 3x3网格
const sidebarCollapsed = ref(false);

// 分类数据（话题分类）
const categories = ref([
  { value: '', label: '全部分类', icon: '💬' },
  { value: 'study', label: '学习资料', icon: '📚' },
  { value: 'life', label: '生活好物', icon: '🏠' },
  { value: 'secondhand', label: '闲置转让', icon: '♻️' },
  { value: 'share', label: '经验分享', icon: '💡' },
  { value: 'help', label: '求助问答', icon: '❓' },
]);

// 热门话题标签（Top3）
const hotTags = ref([
  { value: '', label: '全部' },
  { value: 'study', label: '#学习资料' },
  { value: 'life', label: '#生活好物' },
  { value: 'secondhand', label: '#闲置转让' },
]);

// 方法
function selectCategory(value) {
  selectedCategory.value = selectedCategory.value === value ? '' : value;
  currentPage.value = 1;
  
  if (selectedCategory.value) {
    router.push({
      path: '/portal/circle',
      query: { category: selectedCategory.value },
    });
  } else {
    router.push({ path: '/portal/circle', query: {} });
  }
}

function resetFilters() {
  selectedCategory.value = '';
  selectedTag.value = '';
  sortBy.value = 'latest';
  searchQuery.value = '';
  currentPage.value = 1;
  router.push({ path: '/portal/circle', query: {} });
}

function handleSearch(value) {
  const keyword = value ?? searchQuery.value;
  searchQuery.value = keyword;
  currentPage.value = 1;
  router.push({
    path: '/portal/circle',
    query: keyword ? { keyword } : {},
  });
}

function handlePostClick(post) {
  router.push(`/portal/circle/${post.id}`);
}

function handlePageChange(page) {
  currentPage.value = page;
  loadPosts();
  window.scrollTo({ top: 400, behavior: 'smooth' });
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;

  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

async function loadPosts() {
  loading.value = true;
  try {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.value.toString(),
    });

    // 只传递后端支持的参数
    if (selectedTag.value) {
      params.append('tag', selectedTag.value);
    }

    console.log('[CirclePublic] 请求参数:', Object.fromEntries(params));

    const response = await fetch(`/api/circle/posts?${params}`);
    const data = await response.json();

    console.log('[CirclePublic] API返回数据:', data);

    // 后端返回格式: {code: 200, data: {posts: [...], total: N}}
    const rawData = data?.data?.posts || data?.posts || [];
    const apiTotal = data?.data?.total || data?.total;

    const list = (Array.isArray(rawData) ? rawData : []).map(item => ({
      id: item.id,
      title: item.title || '无标题',
      content: item.content || '',
      images: (() => {
        try {
          if (!item.images) return [];
          if (typeof item.images === 'string') return JSON.parse(item.images || '[]');
          return Array.isArray(item.images) ? item.images : [];
        } catch (e) {
          return [];
        }
      })(),
      authorName: item.userName || item.user_name || '匿名用户',
      likeCount: item.likeCount || item.likes || 0,
      commentCount: item.commentCount || item.comments || 0,
      viewCount: item.viewCount || 0,
      tags: (() => {
        try {
          if (!item.tags) return [];
          if (typeof item.tags === 'string') return item.tags.split(',').filter(t => t.trim());
          return Array.isArray(item.tags) ? item.tags : [];
        } catch (e) {
          return [];
        }
      })(),
      campus: item.campus || '',
      status: item.status || 'PUBLISHED',
      createdAt: item.createdAt || item.create_time || new Date().toISOString(),
    }));

    posts.value = list;
    
    // 使用后端返回的真实total值
    total.value = apiTotal || list.length;
    
    console.log('[CirclePublic] 加载完成:', { 
      当前页: currentPage.value, 
      每页条数: pageSize.value, 
      总数: total.value, 
      当前数据量: list.length 
    });

  } catch (error) {
    console.error('[CirclePublic] 加载失败:', error);
    Message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
}

// 监听路由变化
watch(
  () => route.query,
  () => {
    currentPage.value = 1;
    selectedCategory.value = route.query.category || '';
    searchQuery.value = route.query.keyword || '';
    loadPosts();
  },
  { immediate: true }
);

onMounted(() => {
  selectedCategory.value = route.query.category || '';
  searchQuery.value = route.query.keyword || '';
  loadPosts();
});
</script>

<style lang="scss" scoped>
$primary-blue: #165DFF;
$primary-blue-light: #4080FF;
$bg-white: #FFFFFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;

.circle-page {
  background-color: $bg-gray;
  min-height: 100vh;
}

/* ========== Hero区域（与全部商品100%一致） ========== */
.hero-section {
  position: relative;
  width: 100vw;
  height: 300px;
  margin-left: calc(-50vw + 50%);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.hero-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    $primary-blue 0%,
    $primary-blue-light 35%,
    #4A90FF 65%,
    #E8F3FF 100%
  );
}

.hero-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.06;
  background-image:
    radial-gradient(circle at 25% 25%, white 2px, transparent 2px),
    radial-gradient(circle at 75% 75%, white 2px, transparent 2px);
  background-size: 60px 60px;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 24px 56px;
}

.hero-badge {
  display: inline-block;
  padding: 6px 18px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 16px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.hero-title {
  margin: 0 0 10px;
  font-size: 38px;
  font-weight: 800;
  color: $bg-white;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
}

.search-wrapper {
  position: absolute;
  bottom: -28px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 680px;
  z-index: 10;
}

.search-container {
  width: 100%;
  height: 52px;
  display: flex;
  justify-content: center;
  background: $bg-white;
  box-shadow: 0 6px 24px rgba(71, 92, 134, 0.18);
  border-radius: 26px;

  :deep(.arco-input-wrapper) {
    width: 100%;
    height: 100%;
    border-radius: 26px;
    padding: 0 28px;
    box-sizing: border-box;
    border: none;
    background: transparent;
    box-shadow: none;

    .arco-input {
      font-size: 15px;
      color: $text-secondary;

      &::placeholder {
        color: $text-tertiary;
      }
    }

    .arco-input-suffix {
      font-size: 18px;
      color: $primary-blue;
    }
  }
}

/* ========== 主内容区 ========== */
.main-content {
  position: relative;
  max-width: 1400px;
  margin: 48px auto 32px;
  padding: 0 32px 40px;
}

.content-box {
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  padding: 24px;
  min-height: 500px;
}

.left-col {
  flex: 0 0 auto !important;
  max-width: 200px !important;
}

.right-col {
  flex: 1 !important;
  min-width: 0;
}

/* ========== 左侧分类栏 ========== */
.sidebar {
  background: transparent;
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid #f0f1f3;
  padding-right: 8px;
  margin-right: 8px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 14px;
  border-bottom: 1px solid #f2f3f5;
  flex-shrink: 0;
}

.sidebar-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  transition: all 0.2s ease;

  &:hover {
    background: $bg-gray;
    color: $primary-blue;
  }
}

.category-nav {
  padding: 8px 4px;
  flex: 1;
  overflow-y: auto;

  &.nav-collapsed {
    .category-item {
      justify-content: center;
      padding: 11px 6px;
    }

    .category-icon {
      font-size: 20px;
    }
  }
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: $text-secondary;
  font-size: 14px;
  margin-bottom: 2px;

  &:hover {
    background: #f7f8fa;
    color: $text-primary;
  }

  &.active {
    background: linear-gradient(
      135deg,
      rgba(22, 93, 255, 0.08) 0%,
      rgba(64, 128, 255, 0.06) 100%
    );
    color: $primary-blue;
    font-weight: 500;
  }
}

.category-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 22px;
  text-align: center;
}

.category-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 右侧内容区 ========== */
.right-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.filter-group {
  background: #fafbfc;
  border-radius: 10px;
  padding: 14px 18px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 14px;

  &:not(:last-child) {
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eeeef0;
  }
}

.filter-label {
  font-size: 13px;
  color: $text-tertiary;
  font-weight: 500;
  white-space: nowrap;
  min-width: 70px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 500;
  color: $text-secondary;
  background: $bg-white;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;

  &:hover {
    background: #e8f3ff;
    color: $primary-blue;
    border-color: rgba(22, 93, 255, 0.15);
  }

  &.active {
    background: $primary-blue;
    color: $bg-white;
    border-color: $primary-blue;

    &:hover {
      background: #0e42d2;
      color: $bg-white;
    }
  }

  &.top-tag {
    font-weight: 600;
    
    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #F53F3F;
      margin-right: 4px;
    }
    
    &.active::before {
      background: white;
    }
  }
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 4px;

  .sort-btn {
    font-size: 13px;
    font-weight: 400;
    color: $text-secondary;
    transition: all 0.2s ease;
    padding: 5px 12px;
    border-radius: 6px;

    &.active {
      color: $primary-blue;
      font-weight: 500;
      background: rgba(22, 93, 255, 0.06);
    }
  }
}

.reset-btn {
  color: $text-secondary;
  font-size: 13px;
  transition: color 0.2s;

  &:hover {
    color: $primary-blue;
  }
}

/* ========== 动态卡片网格（3列）- 完全参考商品卡片ItemCard样式 ========== */
.card-grid {
  flex: 1;
  min-height: 320px;
}

.post-card {
  background: $bg-white;
  border-radius: var(--border-radius-medium, 8px);
  overflow: hidden;
  cursor: pointer;
  transition: transform 150ms ease-out, box-shadow 250ms ease-out;
  border: 1px solid var(--color-border-2, #e5e6eb);
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
    border-color: transparent;
  }

  /* 图片容器（1:1正方形，与ItemCard完全一致） */
  .card-image-wrapper {
    position: relative;
    width: 100%;
    padding-top: 100%; /* 1:1比例 */
    background: var(--color-fill-1, #f7f8fa);
    overflow: hidden;
  }

  .card-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 250ms ease-out;
    }

    &:hover img {
      transform: scale(1.05);
    }

    &.card-no-image {
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--color-fill-1, #f7f8fa);
    }

    .image-count {
      position: absolute;
      top: 8px;
      right: 8px;
      background: rgba(0, 0, 0, 0.5);
      color: white;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
      backdrop-filter: blur(4px);
    }
  }

  /* 内容区域（参考ItemCard的info区域） */
  .card-body {
    padding: 12px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .card-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-1, #1d2129);
      line-height: 1.4;
      margin: 0 0 8px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    /* 标签区域 */
    .card-tags {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
      margin-bottom: 8px;

      .tag {
        font-size: 11px;
        color: $primary-blue;
        background: #E8F3FF;
        padding: 2px 8px;
        border-radius: 10px;
        font-weight: 500;
      }
    }

    /* 底部信息栏（参考ItemCard的footer） */
    .card-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-top: auto;
      padding-top: 8px;
      font-size: 12px;
      color: var(--color-text-3, #86909c);

      .author-info {
        display: flex;
        align-items: center;
        gap: 6px;
        flex: 1;
        min-width: 0;

        .author-avatar {
          background: linear-gradient(135deg, #4080FF, #165DFF);
          color: white;
          font-size: 10px;
          font-weight: 600;
          flex-shrink: 0;
        }

        .author-name {
          font-size: 12px;
          color: var(--color-text-3, #86909c);
          font-weight: 400;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      .post-stats {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-shrink: 0;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 3px;
          color: var(--color-text-4, #86909c);
          font-size: 11px;

          .arco-icon {
            font-size: 13px;
          }
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  min-height: 400px;
}

/* ========== 响应式适配 ========== */
@media (max-width: 1023px) {
  .main-content {
    padding: 36px 20px 32px;
  }

  .left-col {
    display: none;
  }

  .right-col {
    width: 100% !important;
    flex: none !important;
  }

  .search-wrapper {
    width: 95%;
  }
}

@media (max-width: 767px) {
  .hero-section {
    height: 260px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .hero-content {
    padding: 30px 16px 50px;
  }

  .search-wrapper {
    bottom: -24px;
  }

  .search-container {
    height: 46px;
    border-radius: 23px;

    :deep(.arco-input-wrapper) {
      padding: 0 20px;

      .arco-input {
        font-size: 14px;
      }
    }
  }

  .main-content {
    margin-top: 40px;
    padding: 20px 16px 28px;
  }

  .content-box {
    padding: 16px;
  }

  .filter-group {
    padding: 12px 14px;
  }

  .filter-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .filter-label {
    width: 100%;
    margin-bottom: -2px;
  }

  .sort-bar {
    flex-wrap: wrap;
    gap: 8px;
  }

  /* 移动端改为2列 */
  .post-card {
    .card-body {
      .card-title {
        -webkit-line-clamp: 1;
      }
      .card-content {
        -webkit-line-clamp: 1;
      }
    }
  }
}
</style>
