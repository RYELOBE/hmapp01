<template>
  <div class="circle-detail-page">
    <a-spin :loading="loading" class="detail-container">
      <template v-if="post">
        <!-- 返回按钮 -->
        <div class="back-bar">
          <a-button @click="$router.back()" type="text" size="small">
            <template #icon><icon-arrow-left /></template>
            返回圈子
          </a-button>
        </div>

        <!-- 帖子内容区 -->
        <a-card :bordered="false" class="post-content-card">
          <!-- 标题 -->
          <h1 class="post-title">{{ post.title }}</h1>

          <!-- 作者信息栏 -->
          <div class="author-bar">
            <a-avatar :size="44" class="author-avatar">
              {{ (post.authorName || '用')[0] }}
            </a-avatar>
            <div class="author-info">
              <div class="author-name">{{ post.authorName || '匿名用户' }}</div>
              <div class="post-time">{{ formatTime(post.createdAt) }}</div>
            </div>
            <a-button
              :type="isFollowing ? 'outline' : 'primary'"
              size="small"
              class="follow-btn"
              @click="toggleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </a-button>
          </div>

          <!-- 富文本内容渲染 -->
          <div class="post-body" v-html="post.content"></div>

          <!-- 图片展示 -->
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <img
              v-for="(img, idx) in post.images"
              :key="idx"
              :src="img"
              alt="帖子图片"
              @click="previewImage(img)"
            />
          </div>

          <!-- 标签 -->
          <div v-if="post.tags && post.tags.length > 0" class="post-tags">
            <a-tag v-for="tag in post.tags" :key="tag" color="arcoblue">
              {{ tag }}
            </a-tag>
          </div>

          <!-- 互动栏 -->
          <div class="interaction-bar">
            <button
              :class="['action-btn', { active: isLiked }]"
              @click="handleLike"
            >
              <icon-heart-fill v-if="isLiked" />
              <icon-heart v-else />
              <span>点赞 {{ likeCount }}</span>
            </button>
            <button class="action-btn" @click="scrollToComments">
              <icon-message />
              <span>评论 {{ commentCount }}</span>
            </button>
            <button class="action-btn" @click="handleShare">
              <icon-share-alt />
              <span>分享</span>
            </button>
          </div>
        </a-card>

        <!-- 评论区 -->
        <a-card :bordered="false" class="comments-card" ref="commentsRef">
          <template #title>
            <div class="comments-header">
              <span class="comments-title">评论 ({{ commentCount }})</span>
            </div>
          </template>

          <!-- 评论输入框 -->
          <div class="comment-input-wrapper">
            <a-input
              v-model="commentInput"
              placeholder="发表评论，按Enter发送..."
              size="large"
              @press-enter="submitComment"
            >
              <template #suffix>
                <a-button
                  type="primary"
                  size="small"
                  :disabled="!commentInput.trim()"
                  @click="submitComment"
                >
                  发送
                </a-button>
              </template>
            </a-input>
          </div>

          <!-- 评论列表 -->
          <div v-if="comments.length > 0" class="comments-list">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <a-avatar :size="36" class="comment-avatar">
                {{ (comment.userName || '用')[0] }}
              </a-avatar>
              <div class="comment-body">
                <div class="comment-user-name">{{ comment.userName || '匿名用户' }}</div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
              </div>
            </div>
          </div>

          <a-empty v-else description="暂无评论，快来发表第一条评论吧！" />

          <!-- 加载更多评论 -->
          <div v-if="hasMoreComments" class="load-more-comments">
            <a-button long @click="loadMoreComments" :loading="loadingComments">
              加载更多评论
            </a-button>
          </div>
        </a-card>
      </template>

      <!-- 空状态 -->
      <a-result
        v-else-if="!loading"
        status="warning"
        title="帖子不存在或已被删除"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/portal/circle')">
            返回圈子首页
          </a-button>
        </template>
      </a-result>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconArrowLeft,
  IconHeart,
  IconHeartFill,
  IconMessage,
  IconShareAlt,
} from '@arco-design/web-vue/es/icon';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const post = ref(null);

// 互动状态
const isLiked = ref(false);
const isFollowing = ref(false);
const likeCount = ref(0);
const commentCount = ref(0);

// 评论相关
const comments = ref([]);
const commentInput = ref('');
const loadingComments = ref(false);
const hasMoreComments = ref(false);
const commentsPage = ref(1);
const commentsRef = ref(null);

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
  if (days < 30) return `${days}天前`;
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

async function loadPost() {
  const postId = route.params.id;
  if (!postId) {
    Message.error('帖子ID不存在');
    router.back();
    return;
  }

  loading.value = true;
  try {
    const response = await fetch(`/api/circle/posts/${postId}`);
    const data = await response.json();

    post.value = data;
    likeCount.value = data.likeCount || 0;
    commentCount.value = data.commentCount || 0;
    isLiked.value = data.isLiked || false;

    // 加载评论
    await loadComments(true);
  } catch (e) {
    console.error('加载帖子失败:', e);
    Message.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadComments(reset = false) {
  const postId = route.params.id;
  if (reset) {
    commentsPage.value = 1;
    comments.value = [];
  }

  loadingComments.value = true;
  try {
    const response = await fetch(
      `/api/circle/posts/${postId}/comments?page=${commentsPage.value}&size=10`
    );
    const data = await response.json();

    const list = data.list || data.records || [];
    if (reset) {
      comments.value = list;
    } else {
      comments.value = [...comments.value, ...list];
    }
    hasMoreComments.value = comments.value.length < (data.total || 0);
    commentCount.value = data.total || commentCount.value;
  } catch (e) {
    console.error('加载评论失败:', e);
  } finally {
    loadingComments.value = false;
  }
}

function loadMoreComments() {
  commentsPage.value += 1;
  loadComments();
}

function scrollToComments() {
  commentsRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function handleLike() {
  const postId = route.params.id;
  try {
    const method = isLiked.value ? 'DELETE' : 'POST';
    const response = await fetch(`/api/circle/posts/${postId}/like`, { method });

    if (response.ok) {
      isLiked.value = !isLiked.value;
      likeCount.value += isLiked.value ? 1 : -1;
      Message.success(isLiked.value ? '点赞成功' : '已取消点赞');
    }
  } catch (e) {
    console.error('操作失败:', e);
    Message.error('操作失败');
  }
}

async function toggleFollow() {
  // TODO: 实现关注/取关逻辑
  isFollowing.value = !isFollowing.value;
  Message.success(isFollowing.value ? '已关注作者' : '已取消关注');
}

async function submitComment() {
  const content = commentInput.value.trim();
  if (!content) return;

  const postId = route.params.id;
  try {
    const response = await fetch(`/api/circle/posts/${postId}/comments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ content }),
    });

    if (response.ok) {
      commentInput.value = '';
      Message.success('评论成功');
      await loadComments(true);
    }
  } catch (e) {
    console.error('评论失败:', e);
    Message.error('评论失败');
  }
}

function handleShare() {
  // 复制链接到剪贴板
  const url = window.location.href;
  navigator.clipboard.writeText(url).then(() => {
    Message.success('链接已复制到剪贴板');
  }).catch(() => {
    Message.info('分享功能开发中');
  });
}

function previewImage(imgUrl) {
  // TODO: 实现图片预览功能
  window.open(imgUrl, '_blank');
}

onMounted(loadPost);
</script>

<style lang="scss" scoped>
.circle-detail-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
  padding: 20px;
}

.detail-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-bar {
  margin-bottom: 16px;
}

.post-content-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);

  :deep(.arco-card-body) {
    padding: 28px;
  }
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-1, #1D2129);
  line-height: 1.4;
  margin: 0 0 20px 0;
}

.author-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

  .author-avatar {
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .author-info {
    flex: 1;

    .author-name {
      font-size: 15px;
      font-weight: 600;
      color: var(--color-text-1, #1D2129);
      margin-bottom: 4px;
    }

    .post-time {
      font-size: 13px;
      color: var(--color-text-3, #86909C);
    }
  }

  .follow-btn {
    flex-shrink: 0;
    border-radius: 16px;
  }
}

.post-body {
  color: var(--color-text-1, #1D2129);
  line-height: 1.8;
  font-size: 15px;
  margin-bottom: 20px;
  word-wrap: break-word;

  img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    cursor: pointer;
  }
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;

  img {
    width: 100%;
    height: 180px;
    object-fit: cover;
    border-radius: 8px;
    cursor: pointer;
    transition: transform 0.25s ease;

    &:hover {
      transform: scale(1.03);
    }
  }
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.interaction-bar {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-1, #E5E6EB);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: var(--color-fill-1, #F7F8FA);
  border: none;
  border-radius: 20px;
  font-size: 14px;
  color: var(--color-text-2, #4E5969);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: #E8F3FF;
    color: #165DFF;
  }

  &.active {
    background: #FFF1F0;
    color: #F53F3F;
  }
}

.comments-card {
  margin-top: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);

  :deep(.arco-card-head) {
    border-bottom: 1px solid var(--color-border-1, #E5E6EB);
    padding: 16px 24px;
  }

  :deep(.arco-card-body) {
    padding: 20px 24px;
  }
}

.comments-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .comments-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }
}

.comment-input-wrapper {
  margin-bottom: 20px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;

  .comment-avatar {
    background: linear-gradient(135deg, #86909C 0%, #B8BFC9 100%);
    color: #fff;
    font-size: 13px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .comment-body {
    flex: 1;
    min-width: 0;

    .comment-user-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-1, #1D2129);
      margin-bottom: 6px;
    }

    .comment-content {
      font-size: 14px;
      color: var(--color-text-1, #1D2129);
      line-height: 1.6;
      margin-bottom: 6px;
    }

    .comment-time {
      font-size: 12px;
      color: var(--color-text-3, #86909C);
    }
  }
}

.load-more-comments {
  margin-top: 20px;
}

@media (max-width: 767px) {
  .circle-detail-page {
    padding: 12px;
  }

  .post-content-card {
    :deep(.arco-card-body) {
      padding: 20px 16px;
    }
  }

  .post-title {
    font-size: 20px;
  }

  .interaction-bar {
    flex-wrap: wrap;
    gap: 12px;

    .action-btn {
      flex: 1;
      justify-content: center;
    }
  }
}
</style>
