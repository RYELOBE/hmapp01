<template>
  <div class="circle-home-page">
    <!-- 顶部Banner -->
    <div class="circle-banner">
      <div class="banner-content">
        <h1 class="banner-title">校园圈子</h1>
        <p class="banner-subtitle">分享你的校园生活</p>
      </div>
      <a-button
        type="primary"
        shape="round"
        size="large"
        class="publish-btn"
        @click="handlePublish"
      >
        <template #icon><icon-plus /></template>
        发布动态
      </a-button>
    </div>

    <!-- 标签筛选 -->
    <div class="tag-filter">
      <div class="tag-list">
        <span
          v-for="tag in tags"
          :key="tag.value"
          :class="['tag-item', { active: activeTag === tag.value }]"
          @click="handleTagChange(tag.value)"
        >
          {{ tag.label }}
        </span>
      </div>
    </div>

    <!-- 帖子列表（瀑布流） -->
    <MasonryLayout
      :items="posts"
      :loading="loading"
      :has-more="hasMore"
      @load-more="loadMorePosts"
      @item-click="handlePostClick"
    >
      <template #default="{ item }">
        <div class="post-card">
          <!-- 封面图 -->
          <div v-if="item.coverImage || (item.images && item.images.length > 0)" class="post-cover">
            <img
              :src="item.coverImage || item.images[0]"
              alt="封面图"
            />
          </div>

          <!-- 帖子内容 -->
          <div class="post-body">
            <h3 class="post-title">{{ item.title }}</h3>

            <div class="post-author">
              <a-avatar :size="28" class="author-avatar">
                {{ (item.authorName || '用')[0] }}
              </a-avatar>
              <span class="author-name">{{ item.authorName || '匿名用户' }}</span>
            </div>

            <div class="post-meta">
              <span class="meta-item">
                <icon-heart /> {{ item.likeCount || 0 }}
              </span>
              <span class="meta-item">
                <icon-message /> {{ item.commentCount || 0 }}
              </span>
              <span class="meta-item post-time">
                {{ formatTime(item.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </template>
    </MasonryLayout>

    <!-- 空状态 -->
    <a-empty
      v-if="!loading && posts.length === 0"
      description="暂无帖子，快来发布第一条动态吧！"
      class="empty-state"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconPlus,
  IconHeart,
  IconMessage,
} from '@arco-design/web-vue/es/icon';
import MasonryLayout from '../../../components/data/MasonryLayout/MasonryLayout.vue';
import { useAuthStore } from '../../../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const posts = ref([]);
const hasMore = ref(true);
const page = ref(1);
const activeTag = ref('');

const tags = [
  { label: '全部', value: '' },
  { label: '#学习资料', value: '学习资料' },
  { label: '#生活好物', value: '生活好物' },
  { label: '#闲置转让', value: '闲置转让' },
  { label: '#经验分享', value: '经验分享' },
  { label: '#求助问答', value: '求助问答' },
];

function handlePublish() {
  if (!authStore.isLoggedIn) {
    Message.warning('请先登录后再发布');
    router.push({ path: '/login', query: { redirect: '/portal/circle/publish' } });
    return;
  }
  router.push('/portal/circle/publish');
}

function handleTagChange(tag) {
  activeTag.value = tag;
  page.value = 1;
  posts.value = [];
  loadPosts();
}

function handlePostClick(post) {
  router.push(`/portal/circle/${post.id}`);
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  });
}

async function loadPosts() {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      size: 20,
    };
    if (activeTag.value) {
      params.tag = activeTag.value;
    }

    // 使用通用http调用，因为API可能还未定义
    const response = await fetch(`/api/circle/posts?${new URLSearchParams(params)}`);
    const data = await response.json();

    const list = data.list || data.records || [];
    if (page.value === 1) {
      posts.value = list;
    } else {
      posts.value = [...posts.value, ...list];
    }
    hasMore.value = posts.value.length < (data.total || 0);
  } catch (e) {
    console.error('加载帖子失败:', e);
    Message.error(e.message || '加载帖子失败');
  } finally {
    loading.value = false;
  }
}

function loadMorePosts() {
  if (!loading.value && hasMore.value) {
    page.value += 1;
    loadPosts();
  }
}

onMounted(() => {
  loadPosts();
});
</script>

<style lang="scss" scoped>
.circle-home-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
  padding-bottom: 40px;
}

.circle-banner {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #6AA1FF 100%);
  padding: 48px 24px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;

  .banner-title {
    font-size: 36px;
    font-weight: 700;
    color: #FFFFFF;
    margin: 0 0 8px 0;
  }

  .banner-subtitle {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.85);
    margin: 0;
  }

  .publish-btn {
    height: 44px;
    padding: 0 28px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 22px;
    background: #FFFFFF;
    color: #165DFF;
    border: none;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
    }
  }
}

.tag-filter {
  background: #FFFFFF;
  padding: 16px 24px;
  margin-top: -20px;
  margin-left: 24px;
  margin-right: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 14px;
  color: var(--color-text-2, #4E5969);
  background: var(--color-fill-1, #F7F8FA);
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
  user-select: none;

  &:hover {
    background: #E8F3FF;
    color: #165DFF;
    border-color: #B8D2FF;
  }

  &.active {
    background: #165DFF;
    color: #FFFFFF;
    border-color: #165DFF;
    font-weight: 500;
  }
}

.post-card {
  background: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  }
}

.post-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--color-fill-2, #E5E6EB);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

.post-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .author-avatar {
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .author-name {
    font-size: 13px;
    color: var(--color-text-2, #4E5969);
  }
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: auto;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--color-text-3, #86909C);
  }

  .post-time {
    margin-left: auto;
  }
}

.empty-state {
  margin-top: 80px;
}

@media (max-width: 767px) {
  .circle-banner {
    padding: 32px 16px 24px;
    flex-direction: column;
    text-align: center;

    .banner-title {
      font-size: 28px;
    }

    .banner-subtitle {
      font-size: 14px;
    }

    .publish-btn {
      width: 100%;
    }
  }

  .tag-filter {
    margin: -16px 16px 0;
    padding: 12px 16px;
  }

  .tag-list {
    gap: 8px;
  }

  .tag-item {
    padding: 6px 14px;
    font-size: 13px;
  }
}
</style>
