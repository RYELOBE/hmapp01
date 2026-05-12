<template>
  <div class="circle-detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-nav">
      <div class="nav-container">
        <a-button type="text" class="back-btn" @click="$router.back()">
          <template #icon><icon-arrow-left /></template>
          返回圈子
        </a-button>
        <span class="nav-title">{{ post?.title || '帖子详情' }}</span>
      </div>
    </div>

    <div :loading="loading" class="detail-container">
      <template v-if="post">
        <a-row :gutter="[20, 20]">
          <!-- 左侧：帖子主体内容 -->
          <a-col :xs="24" :lg="16" class="main-col">
            <!-- 帖子卡片 -->
            <div class="post-card">
              <!-- 作者信息栏 -->
              <div class="author-section">
                <a-avatar :size="48" class="author-avatar">
                  {{ (post.authorName || '用')[0] }}
                </a-avatar>
                <div class="author-info">
                  <h3 class="author-name">{{ post.authorName || '匿名用户' }}</h3>
                  <span class="post-meta">{{ formatTime(post.createdAt) }} · 发布于校园圈子</span>
                </div>
              </div>

              <!-- 标题 -->
              <h1 class="post-title">{{ post.title || '分享动态' }}</h1>

              <!-- 内容 -->
              <div v-if="post.content" class="post-content">
                {{ post.content }}
              </div>

              <!-- 图片展示 -->
              <div v-if="post.images && post.images.length > 0" class="post-images">
                <div
                  v-for="(img, idx) in post.images.slice(0, 9)"
                  :key="idx"
                  class="image-item"
                  :style="{ backgroundImage: `url(${img})` }"
                  @click="previewImage(img)"
                />
                <div v-if="post.images.length > 9" class="more-images">
                  +{{ post.images.length - 9 }}
                </div>
              </div>

              <!-- 标签 -->
              <div v-if="post.tags && post.tags.length > 0" class="post-tags">
                <span v-for="tag in post.tags" :key="tag" class="tag">#{{ tag }}</span>
              </div>

              <!-- 互动操作栏 -->
              <div class="interaction-bar">
                <button
                  :class="['action-btn', { active: isLiked }]"
                  @click="handleLike"
                  :disabled="liking"
                >
                  <icon-heart-fill v-if="isLiked" />
                  <icon-heart v-else />
                  <span>点赞</span>
                  <span class="count">{{ likeCount }}</span>
                </button>
                <button class="action-btn" @click="scrollToComments">
                  <icon-message />
                  <span>评论</span>
                  <span class="count">{{ commentCount }}</span>
                </button>
                <button class="action-btn" @click="handleShare">
                  <icon-share-alt />
                  <span>分享</span>
                </button>
              </div>
            </div>

            <!-- 评论区 -->
            <div class="comments-section" ref="commentsRef">
              <h3 class="section-title">评论 ({{ commentCount }})</h3>

              <!-- 评论输入框：OPS角色不能评论 -->
              <div v-if="authStore.token && !authStore.roles.includes('OPS')" class="comment-input-box">
                <a-textarea
                  v-model="commentInput"
                  placeholder="写下你的评论..."
                  :auto-size="{ minRows: 3, maxRows: 6 }"
                  :disabled="submittingComment"
                />
                <div class="input-actions">
                  <span class="tip">按 Ctrl+Enter 发送</span>
                  <a-button
                    type="primary"
                    shape="round"
                    size="small"
                    :disabled="!commentInput.trim() || submittingComment"
                    :loading="submittingComment"
                    @click="submitComment"
                  >
                    发表评论
                  </a-button>
                </div>
              </div>

              <!-- 运营人员提示 -->
              <div v-else-if="authStore.roles.includes('OPS')" class="ops-tip">
                <p>运营人员仅负责审核，不参与评论互动</p>
              </div>

              <!-- 未登录提示 -->
              <div v-else class="login-tip">
                <p>👋 登录后即可参与讨论</p>
                <a-button type="primary" size="small" @click="$router.push('/login?redirect=' + encodeURIComponent($route.fullPath))">
                  立即登录
                </a-button>
              </div>

              <!-- 评论列表（使用二次封装的 Comment 组件） -->
              <div v-if="comments.length > 0" class="comments-list">
                <Comment
                  v-for="comment in comments"
                  :key="comment.id"
                  :comment="comment"
                  :id="comment.id"
                  :author="comment.userName || '匿名用户'"
                  :content="comment.content"
                  :datetime="comment.createTime"
                  :like-count="comment.likeCount"
                  :is-liked="comment.isLiked"
                  :replies="comment.replies || []"
                  :show-actions="authStore.token"
                  :show-reply="authStore.token"
                  :show-like="authStore.token"
                  :on-submit-reply="handleCommentSubmitReply"
                  :on-like="handleCommentLike"
                />
              </div>

              <!-- 空状态 -->
              <div v-else class="empty-comments">
                <icon-message size="48" style="color: #c9cdd4;" />
                <p>暂无评论，快来发表第一条评论吧！</p>
              </div>

              <!-- 加载更多 -->
              <div v-if="hasMoreComments" class="load-more">
                <a-button long @click="loadMoreComments" :loading="loadingComments">
                  加载更多评论
                </a-button>
              </div>
            </div>
          </a-col>

          <!-- 右侧：作者信息卡 -->
          <!-- 右侧：侧边栏 -->
          <a-col :xs="24" :lg="8" class="side-col">
            <div class="author-card">
              <div class="card-header">
                <a-avatar :size="64" class="big-avatar">
                  {{ (post.authorName || '用')[0] }}
                </a-avatar>
                <h4 class="card-name">{{ post.authorName || '匿名用户' }}</h4>
                <p class="card-desc">校园生活分享者</p>
              </div>
              <div class="card-stats">
                <div class="stat-item single-stat">
                  <icon-eye style="font-size: 24px; color: #165DFF;" />
                  <strong>{{ post.viewCount || 0 }}</strong>
                  <span>浏览量</span>
                </div>
              </div>
            </div>

            <!-- 相关推荐 -->
            <div class="recommend-card">
              <h4 class="recommend-title">🔥 热门推荐</h4>
              <div class="recommend-list">
                <div
                  v-for="(item, index) in recommendPosts"
                  :key="index"
                  class="recommend-item"
                  @click="$router.push(`/portal/circle/${item.id}`)"
                >
                  <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                  <div class="recommend-info">
                    <h5 class="recommend-title-text">{{ item.title }}</h5>
                    <span class="recommend-stats">💬 {{ item.commentCount || 0 }} 评论</span>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </template>

      <!-- 加载失败/空状态 -->
      <a-result
        v-else-if="!loading && !post"
        status="warning"
        title="帖子不存在或已被删除"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/portal/circle')">
            返回圈子首页
          </a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconArrowLeft,
  IconHeart,
  IconHeartFill,
  IconMessage,
  IconShareAlt,
} from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '../../../stores/auth';
import Comment from '../../../components/comment/Comment.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 状态管理
const loading = ref(false);
const post = ref(null);

// 互动状态
const isLiked = ref(false);
const liking = ref(false);
const likeCount = ref(0);
const commentCount = ref(0);

// 评论相关
const comments = ref([]);
const commentInput = ref('');
const submittingComment = ref(false);
const loadingComments = ref(false);
const hasMoreComments = ref(false);
const commentsPage = ref(1);

// 推荐帖子
const recommendPosts = ref([]);

// 监听路由参数变化（处理热门推荐帖子点击后页面不刷新的问题）
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      // 重置所有状态
      post.value = null;
      comments.value = [];
      commentsPage.value = 1;
      isLiked.value = false;
      likeCount.value = 0;
      commentCount.value = 0;
      commentInput.value = '';

      // 重新加载数据
      await loadPost();
      
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
);

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

// 获取认证请求头
function getAuthHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  if (authStore.token) {
    headers['Authorization'] = `Bearer ${authStore.token}`;
  }
  return headers;
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
    // 加载帖子详情
    const response = await fetch(`/api/circle/posts/${postId}`);
    const data = await response.json();

    console.log('[CircleDetail] 帖子API返回:', data);

    if (!response.ok || !data.data) {
      throw new Error(data.message || '帖子不存在');
    }

    // 设置帖子数据
    post.value = data.data;

    // 映射作者名称字段
    post.value.authorName = post.value.authorName || post.value.userName;

    // 处理图片
    if (typeof post.value.images === 'string') {
      try {
        post.value.images = JSON.parse(post.value.images || '[]');
      } catch (e) {
        post.value.images = [];
      }
    }
    
    // 处理标签
    if (typeof post.value.tags === 'string') {
      post.value.tags = post.value.tags.split(',').filter(t => t.trim());
    }

    // 设置统计数据
    likeCount.value = data.data.likeCount || 0;
    commentCount.value = data.data.commentCount || 0;

    // 检查是否已点赞（需要调用接口或根据用户历史判断）
    await checkLikeStatus(postId);

    // 加载评论
    await loadComments(true);
    
    // 加载推荐帖子
    await loadRecommendPosts();

  } catch (e) {
    console.error('[CircleDetail] 加载帖子失败:', e);
    Message.error(e.message || '加载失败');
    post.value = null;
  } finally {
    loading.value = false;
  }
}

async function checkLikeStatus(postId) {
  if (!authStore.token) {
    isLiked.value = false;
    return;
  }

  try {
    // 这里可以调用检查是否点赞的接口
    // 如果没有这个接口，默认未点赞
    isLiked.value = false;
  } catch (e) {
    isLiked.value = false;
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

    console.log('[CircleDetail] 评论API返回:', data);

    if (!response.ok) {
      throw new Error(data.message || '加载评论失败');
    }

    const list = data.data?.comments || [];

    // 处理评论数据
    const processedList = await Promise.all(list.map(async c => {
      const commentObj = {
        ...c,
        replies: c.replies || [],
        isLiked: false,
        userName: c.userName || '匿名用户',
      };

      // 检查父评论点赞状态
      if (authStore.token) {
        try {
          const likeResponse = await fetch(`/api/circle/comments/${c.id}/like-status`, {
            headers: getAuthHeaders(),
          });
          if (likeResponse.ok) {
            const likeData = await likeResponse.json();
            commentObj.isLiked = likeData.data.liked;
          }
        } catch (e) {
          console.warn('[CircleDetail] 检查评论点赞状态失败:', e);
        }

        // 检查子评论点赞状态
        if (commentObj.replies && commentObj.replies.length > 0) {
          commentObj.replies = await Promise.all(commentObj.replies.map(async reply => {
            const replyObj = { ...reply, isLiked: false, userName: reply.userName || '匿名用户' };
            try {
              const replyLikeResponse = await fetch(`/api/circle/comments/${reply.id}/like-status`, {
                headers: getAuthHeaders(),
              });
              if (replyLikeResponse.ok) {
                const replyLikeData = await replyLikeResponse.json();
                replyObj.isLiked = replyLikeData.data.liked;
              }
            } catch (e) {
              console.warn('[CircleDetail] 检查子评论点赞状态失败:', e);
            }
            return replyObj;
          }));
        }
      }

      return commentObj;
    }));

    if (reset) {
      comments.value = processedList;
    } else {
      comments.value = [...comments.value, ...processedList];
    }

    // 更新总数
    const totalCount = data.data?.total || list.length;
    commentCount.value = totalCount;
    
    // 判断是否还有更多
    hasMoreComments.value = comments.value.length < totalCount;

    console.log('[CircleDetail] 评论加载完成:', {
      当前页: commentsPage.value,
      本页数量: list.length,
      总数: totalCount,
      已加载: comments.value.length
    });

  } catch (e) {
    console.error('[CircleDetail] 加载评论失败:', e);
    Message.error(e.message || '加载评论失败');
  } finally {
    loadingComments.value = false;
  }
}

async function loadRecommendPosts() {
  try {
    const response = await fetch('/api/circle/posts?page=1&size=5');
    const data = await response.json();
    
    if (data.data?.posts) {
      recommendPosts.value = data.data.posts
        .filter(p => p.id !== Number(route.params.id))
        .slice(0, 5)
        .map(p => ({
          id: p.id,
          title: p.title || '无标题',
          commentCount: p.commentCount || 0,
        }));
    }
  } catch (e) {
    console.error('[CircleDetail] 加载推荐失败:', e);
  }
}

function loadMoreComments() {
  commentsPage.value += 1;
  loadComments();
}

function scrollToComments() {
  commentsRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function handleLike() {
  const postId = route.params.id;
  
  if (!authStore.token) {
    Message.warning('请先登录后再点赞');
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
    return;
  }

  if (liking.value) return;
  liking.value = true;

  try {
    const method = isLiked.value ? 'DELETE' : 'POST';
    const response = await fetch(`/api/circle/posts/${postId}/like`, { 
      method,
      headers: getAuthHeaders(),
    });

    const data = await response.json();
    console.log('[CircleDetail] 点赞API返回:', { status: response.status, data });

    if (response.ok) {
      // 切换状态
      isLiked.value = !isLiked.value;
      
      // 更新计数
      if (isLiked.value) {
        likeCount.value++;
        Message.success('点赞成功 ❤️');
      } else {
        likeCount.value--;
        Message.info('已取消点赞');
      }
      
      console.log('[CircleDetail] 点赞状态更新:', { isLiked: isLiked.value, count: likeCount.value });
    } else if (response.status === 401) {
      Message.error('登录已过期，请重新登录');
      authStore.$reset();
      router.push('/login');
    } else {
      throw new Error(data.message || '操作失败');
    }
  } catch (e) {
    console.error('[CircleDetail] 点赞失败:', e);
    Message.error(e.message || '操作失败，请稍后重试');
  } finally {
    liking.value = false;
  }
}

async function submitComment() {
  const content = commentInput.value.trim();
  if (!content) return;

  if (!authStore.token) {
    Message.warning('请先登录后再评论');
    return;
  }

  submittingComment.value = true;
  
  try {
    const postId = route.params.id;
    
    console.log('[CircleDetail] 发表评论:', { postId, content });

    const response = await fetch(`/api/circle/posts/${postId}/comments`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ content }),
    });

    const data = await response.json();
    console.log('[CircleDetail] 发表评论API返回:', { status: response.status, data });

    if (response.ok) {
      // 清空输入框
      commentInput.value = '';
      Message.success('评论已提交，待审核后展示');
      
      // 重新加载评论（从头开始）
      await loadComments(true);
      
      console.log('[CircleDetail] 评论发表成功，已刷新评论列表');
    } else if (response.status === 401) {
      Message.error('登录已过期，请重新登录');
      authStore.$reset();
      router.push('/login');
    } else {
      throw new Error(data.message || '发表评论失败');
    }
  } catch (e) {
    console.error('[CircleDetail] 发表评论失败:', e);
    Message.error(e.message || '发表评论失败，请稍后重试');
  } finally {
    submittingComment.value = false;
  }
}

/**
 * 处理评论回复（回调函数）
 */
async function handleCommentSubmitReply({ parentId, replyToName, content }) {
  if (!content || !authStore.token) return;

  const postId = route.params.id;

  const requestBody = {
    content,
    parentId,
  };

  if (replyToName) {
    requestBody.replyToName = replyToName;
  }

  const response = await fetch(`/api/circle/posts/${postId}/comments`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(requestBody),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || '回复失败');
  }

  Message.success('回复已提交，待审核后展示');
  
  // 刷新评论列表
  await loadComments(true);
}

/**
 * 处理评论点赞（回调函数）
 */
async function handleCommentLike(commentId) {
  if (!authStore.token) {
    throw new Error('请先登录');
  }

  const response = await fetch(`/api/circle/comments/${commentId}/like`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || '操作失败');
  }

  // 刷新评论列表以更新点赞状态
  await loadComments(true);
}

function handleShare() {
  const url = window.location.href;
  navigator.clipboard.writeText(url).then(() => {
    Message.success('链接已复制到剪贴板 📋');
  }).catch(() => {
    Message.info('分享功能开发中...');
  });
}

function previewImage(imgUrl) {
  window.open(imgUrl, '_blank');
}

onMounted(() => {
  loadPost();
});
</script>

<style lang="scss" scoped>
$primary-blue: #165DFF;
$bg-gray: #F5F7FA;
$text-primary: #1D2129;
$text-secondary: #4E5969;
$text-tertiary: #86909C;

.circle-detail-page {
  min-height: 100vh;
  background: $bg-gray;
}

.detail-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #E5E6EB;
  padding: 10px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  .nav-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 40px;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .nav-title {
    font-size: 15px;
    font-weight: 500;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .back-btn {
    color: $text-secondary;
    font-size: 14px;
    flex-shrink: 0;
    
    &:hover {
      color: $primary-blue;
    }
  }
}

.detail-container {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 40px;
}

.main-col {
  flex: 1 !important;
  min-width: 0;
}

.side-col {
  width: 280px !important;
  flex-shrink: 0 !important;
}

.post-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }
}

.author-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #F2F3F5;

  .author-avatar {
    background: linear-gradient(135deg, $primary-blue, #4080FF);
    color: white;
    font-size: 20px;
    font-weight: 700;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba($primary-blue, 0.25);
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.05);
    }
  }

  .author-info {
    flex: 1;
    min-width: 0;

    .author-name {
      font-size: 17px;
      font-weight: 600;
      color: $text-primary;
      margin: 0 0 6px 0;
      transition: color 0.3s ease;

      &:hover {
        color: $primary-blue;
        cursor: pointer;
      }
    }

    .post-meta {
      font-size: 13px;
      color: $text-tertiary;
      display: flex;
      align-items: center;
      gap: 8px;

      &::before {
        content: "📅";
        font-size: 14px;
      }
    }
  }
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.4;
  margin: 0 0 24px 0;
  letter-spacing: -0.02em;
}

.post-content {
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.9;
  margin-bottom: 24px;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 20px;
  background: #FAFBFC;
  border-radius: 12px;
  border-left: 4px solid $primary-blue;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;

  .image-item {
    aspect-ratio: 16 / 10;
    border-radius: 10px;
    background-size: cover;
    background-position: center;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    &:hover {
      transform: scale(1.03) translateY(-2px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
      z-index: 2;
    }
  }

  .more-images {
    aspect-ratio: 16 / 10;
    background: linear-gradient(135deg, rgba($primary-blue, 0.08), rgba($primary-blue, 0.15));
    color: $primary-blue;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    border-radius: 10px;
    cursor: default;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, rgba($primary-blue, 0.12), rgba($primary-blue, 0.2));
      transform: scale(1.02);
    }
  }
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;

  .tag {
    font-size: 13px;
    color: $primary-blue;
    background: linear-gradient(135deg, #E8F3FF, #D6E9FF);
    padding: 6px 14px;
    border-radius: 16px;
    font-weight: 500;
    transition: all 0.3s ease;
    border: 1px solid transparent;

    &:hover {
      background: $primary-blue;
      color: white;
      border-color: $primary-blue;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba($primary-blue, 0.25);
    }
  }
}

.interaction-bar {
  display: flex;
  gap: 18px;
  padding-top: 24px;
  border-top: 2px solid #F2F3F5;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: linear-gradient(135deg, #F7F8FA, #EEF0F3);
    border: none;
    border-radius: 22px;
    font-size: 14px;
    color: $text-secondary;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: 50%;
      left: 50%;
      width: 0;
      height: 0;
      background: radial-gradient(circle, currentColor 0%, transparent 70%);
      opacity: 0;
      transition: all 0.5s ease;
      transform: translate(-50%, -50%);
    }

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #E8F3FF, #D6E9FF);
      color: $primary-blue;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba($primary-blue, 0.2);

      &::before {
        width: 100%;
        height: 100%;
        opacity: 0.05;
      }
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    &.active {
      background: linear-gradient(135deg, #FFF1F0, #FFE8E6);
      color: #F53F3F;
      box-shadow: 0 4px 12px rgba(245, 63, 63, 0.2);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(245, 63, 63, 0.25);
      }
    }

    .count {
      font-weight: 700;
      margin-left: 4px;
      min-width: 16px;
      text-align: center;
    }
  }
}

.comments-section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-primary;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 10px;

  &::before {
    content: "💬";
    font-size: 20px;
  }
}

.comment-input-box {
  background: linear-gradient(135deg, #FAFBFC, #F5F6F8);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 28px;
  border: 2px solid transparent;
  transition: all 0.3s ease;

  &:focus-within {
    border-color: $primary-blue;
    background: white;
    box-shadow: 0 4px 20px rgba($primary-blue, 0.1);
  }

  :deep(.arco-textarea-wrapper) {
    background: transparent;
    border: 1px solid #E5E6EB;

    &:focus-within {
      border-color: $primary-blue;
      box-shadow: 0 0 0 3px rgba($primary-blue, 0.1);
    }
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;

    .tip {
      font-size: 13px;
      color: $text-tertiary;

      &::before {
        content: "⌨️ ";
      }
    }
  }
}

.login-tip {
  text-align: center;
  padding: 40px;
  background: linear-gradient(135deg, #FAFBFC, #F5F6F8);
  border-radius: 12px;
  margin-bottom: 28px;
  border: 2px dashed #E5E6EB;

  p {
    margin: 0 0 16px 0;
    font-size: 15px;
    color: $text-secondary;
  }

  a-button {
    border-radius: 22px;
    padding: 10px 24px;
    font-weight: 600;
  }
}

.ops-tip {
  text-align: center;
  padding: 40px;
  background: linear-gradient(135deg, #e8f3ff, #f0f7ff);
  border-radius: 12px;
  margin-bottom: 28px;
  border: 1px solid #94bfff;

  p {
    margin: 0;
    font-size: 15px;
    color: #165dff;
  }
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.empty-comments {
  text-align: center;
  padding: 48px 20px;
  color: $text-tertiary;

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}

.load-more {
  margin-top: 24px;
  text-align: center;
}

.author-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }

  .card-header {
    text-align: center;
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 2px solid #F2F3F5;

    .big-avatar {
      background: linear-gradient(135deg, $primary-blue, #4080FF);
      color: white;
      font-size: 26px;
      font-weight: 700;
      margin-bottom: 14px;
      width: 80px;
      height: 80px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: auto;
      margin-right: auto;
      box-shadow: 0 6px 20px rgba($primary-blue, 0.3);
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05) rotate(3deg);
      }
    }

    .card-name {
      font-size: 18px;
      font-weight: 700;
      color: $text-primary;
      margin: 0 0 8px 0;
      transition: color 0.3s ease;

      &:hover {
        color: $primary-blue;
        cursor: pointer;
      }
    }

    .card-desc {
      font-size: 13px;
      color: $text-tertiary;
      margin: 0;
      line-height: 1.6;
    }
  }

  .card-stats {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-bottom: 24px;

    .single-stat {
      text-align: center;
      padding: 18px 36px;
      background: linear-gradient(135deg, #E8F3FF 0%, #F0F5FF 100%);
      border-radius: 12px;
      flex: 1;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba($primary-blue, 0.15);
      }

      strong {
        display: block;
        font-size: 32px;
        font-weight: 800;
        color: $primary-blue;
        margin: 10px 0 6px 0;
        letter-spacing: -0.02em;
      }

      span {
        font-size: 13px;
        color: $text-tertiary;
        font-weight: 500;
      }
    }
  }

  .follow-btn {
    width: 100%;
    border-radius: 22px;
    padding: 12px 24px;
    font-weight: 600;
    font-size: 15px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 18px rgba($primary-blue, 0.25);
    }
  }
}

.recommend-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }

  .recommend-title {
    font-size: 17px;
    font-weight: 700;
    color: $text-primary;
    margin: 0 0 20px 0;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: "🔥";
      font-size: 18px;
    }
  }

  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .recommend-item {
      display: flex;
      gap: 14px;
      padding: 14px;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      background: #FAFBFC;

      &:hover {
        background: linear-gradient(135deg, #E8F3FF, #F0F5FF);
        transform: translateX(4px);
        box-shadow: 0 4px 12px rgba($primary-blue, 0.1);

        .rank {
          transform: scale(1.1);
        }
      }

      .rank {
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: 800;
        color: $text-tertiary;
        background: linear-gradient(135deg, #F2F3F5, #E5E6EB);
        border-radius: 8px;
        flex-shrink: 0;
        transition: all 0.3s ease;

        &.top3 {
          background: linear-gradient(135deg, #FF6B35, #F53F3F);
          color: white;
          box-shadow: 0 4px 10px rgba(245, 63, 63, 0.25);

          &:hover {
            box-shadow: 0 6px 15px rgba(245, 63, 63, 0.35);
          }
        }
      }

      .recommend-info {
        flex: 1;
        min-width: 0;

        .recommend-title-text {
          font-size: 14px;
          font-weight: 600;
          color: $text-primary;
          line-height: 1.5;
          margin: 0 0 6px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          transition: color 0.3s ease;

          &:hover {
            color: $primary-blue;
          }
        }

        .recommend-stats {
          font-size: 13px;
          color: $text-tertiary;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 4px;

          &::before {
            content: "💬";
            font-size: 12px;
          }
        }
      }
    }
  }
}

@media (max-width: 1023px) {
  .side-col {
    display: none;
  }

  .main-col {
    width: 100% !important;
  }

  .detail-nav .nav-container,
  .detail-container {
    padding: 0 20px;
  }
}

@media (max-width: 767px) {
  .detail-container {
    padding: 0 12px;
  }

  .post-card {
    padding: 20px 16px;
  }

  .post-title {
    font-size: 19px;
  }

  .post-images {
    grid-template-columns: repeat(2, 1fr);
  }

  .comments-section {
    padding: 16px;
  }
}
</style>
