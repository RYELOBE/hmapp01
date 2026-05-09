<template>
  <div class="circle-detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-nav">
      <div class="nav-container">
        <a-button type="text" class="back-btn" @click="$router.back()">
          <template #icon><icon-arrow-left /></template>
          返回圈子
        </a-button>
      </div>
    </div>

    <a-spin :loading="loading" class="detail-container">
      <template v-if="post">
        <a-row :gutter="[24, 0]">
          <!-- 左侧：帖子主体内容 -->
          <a-col :span="16" class="main-col">
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

              <!-- 评论输入框 -->
              <div v-if="authStore.token" class="comment-input-box">
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
              
              <!-- 未登录提示 -->
              <div v-else class="login-tip">
                <p>👋 登录后即可参与讨论</p>
                <a-button type="primary" size="small" @click="$router.push('/login?redirect=' + encodeURIComponent($route.fullPath))">
                  立即登录
                </a-button>
              </div>

              <!-- 评论列表 -->
              <div v-if="comments.length > 0" class="comments-list">
                <div
                  v-for="comment in comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <a-avatar :size="40" class="comment-avatar">
                    {{ (comment.userName || '用')[0] }}
                  </a-avatar>
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="user-name">{{ comment.userName || '匿名用户' }}</span>
                      <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                    </div>
                    <p class="comment-content">{{ comment.content }}</p>

                    <!-- 回复按钮和子评论 -->
                    <div class="comment-actions">
                      <button
                        v-if="authStore.token"
                        class="reply-btn"
                        @click="showReplyBox(comment.id)"
                      >
                        <icon-reply /> 回复
                      </button>
                      <button 
                        v-if="authStore.token"
                        class="like-btn" 
                        @click="likeComment(comment)"
                        :class="{ liked: comment.isLiked }"
                      >
                        <icon-heart-fill v-if="comment.isLiked" style="color: #F53F3F;" />
                        <icon-heart v-else />
                        {{ comment.likeCount || 0 }}
                      </button>
                    </div>

                    <!-- 回复输入框（点击回复后显示） -->
                    <div v-if="activeReplyId === comment.id" class="reply-input-box">
                      <a-input
                        v-model="replyContent"
                        :placeholder="`回复 ${comment.userName || '用户'}...`"
                        size="small"
                        @press-enter="submitReply(comment)"
                      >
                        <template #suffix>
                          <a-button
                            type="text"
                            size="small"
                            :disabled="!replyContent.trim()"
                            :loading="replying"
                            @click="submitReply(comment)"
                          >
                            发送
                          </a-button>
                        </template>
                      </a-input>
                    </div>

                    <!-- 子评论列表（回复） -->
                    <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                      <div
                        v-for="reply in comment.replies"
                        :key="reply.id"
                        class="reply-item"
                      >
                        <a-avatar :size="28" class="reply-avatar">
                          {{ (reply.userName || '用')[0] }}
                        </a-avatar>
                        <div class="reply-body">
                          <span class="reply-user">{{ reply.userName || '匿名' }}</span>
                          <span v-if="reply.replyToName" class="reply-to">
                            回复 <strong>@{{ reply.replyToName }}</strong>
                          </span>
                          <span class="reply-text">: {{ reply.content }}</span>
                          <div class="reply-meta">
                            <span>{{ formatTime(reply.createdAt) }}</span>
                            <button 
                              v-if="authStore.token"
                              class="reply-btn-small" 
                              @click="showReplyBox(reply.parentId || comment.id, reply.userName)"
                            >
                              回复
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
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
          <a-col :span="8" class="side-col">
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
  IconReply,
  IconEye,
} from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '../../../stores/auth';

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
const commentsRef = ref(null);

// 回复相关
const activeReplyId = ref(null);
const replyContent = ref('');
const replying = ref(false);

// 推荐帖子
const recommendPosts = ref([]);

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
    const processedList = list.map(c => ({
      ...c,
      replies: c.replies || [],
      isLiked: false,
      userName: c.userName || '匿名用户',
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
      Message.success('评论发表成功！✍️');
      
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

function showReplyBox(commentId, toUserName = '') {
  if (activeReplyId.value === commentId) {
    activeReplyId.value = null;
    replyContent.value = '';
  } else {
    activeReplyId.value = commentId;
    replyContent.value = toUserName ? `@${toUserName} ` : '';
  }
}

async function submitReply(parentComment) {
  let content = replyContent.value.trim();
  
  // 去除@用户名部分
  if (content.startsWith('@')) {
    content = content.replace(/^@\S+\s*/, '').trim();
  }
  
  if (!content) return;

  if (!authStore.token) {
    Message.warning('请先登录后再回复');
    return;
  }

  replying.value = true;

  try {
    const postId = route.params.id;
    
    console.log('[CircleDetail] 发表回复:', { 
      postId, 
      parentId: parentComment.id,
      content 
    });

    const response = await fetch(`/api/circle/posts/${postId}/comments`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        content,
        parentId: parentComment.id,
      }),
    });

    const data = await response.json();
    console.log('[CircleDetail] 发表回复API返回:', { status: response.status, data });

    if (response.ok) {
      // 关闭输入框
      activeReplyId.value = null;
      replyContent.value = '';
      Message.success('回复成功！💬');
      
      // 重新加载评论
      await loadComments(true);
      
      console.log('[CircleDetail] 回复成功，已刷新评论列表');
    } else if (response.status === 401) {
      Message.error('登录已过期，请重新登录');
      authStore.$reset();
      router.push('/login');
    } else {
      throw new Error(data.message || '回复失败');
    }
  } catch (e) {
    console.error('[CircleDetail] 回复失败:', e);
    Message.error(e.message || '回复失败');
  } finally {
    replying.value = false;
  }
}

async function likeComment(comment) {
  if (!authStore.token) {
    Message.warning('请先登录后再操作');
    return;
  }

  try {
    // 切换本地状态
    comment.isLiked = !comment.isLiked;
    comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1);
    
    // TODO: 调用点赞评论API（如果后端支持）
    console.log('[CircleDetail] 评论点赞:', { 
      commentId: comment.id, 
      isLiked: comment.isLiked 
    });
  } catch (e) {
    // 回滚状态
    comment.isLiked = !comment.isLiked;
    comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1);
    Message.error('操作失败');
  }
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #E5E6EB;
  padding: 12px 0;

  .nav-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 32px;
  }

  .back-btn {
    color: $text-secondary;
    font-size: 14px;
    
    &:hover {
      color: $primary-blue;
    }
  }
}

.detail-container {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 32px;
}

.main-col {
  flex: none !important;
  width: 66.666% !important;
}

.side-col {
  flex: none !important;
  width: 33.333% !important;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid #F2F3F5;

  .author-avatar {
    background: linear-gradient(135deg, $primary-blue, #4080FF);
    color: white;
    font-size: 18px;
    font-weight: 700;
    flex-shrink: 0;
  }

  .author-info {
    flex: 1;
    min-width: 0;

    .author-name {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      margin: 0 0 4px 0;
    }

    .post-meta {
      font-size: 13px;
      color: $text-tertiary;
    }
  }
}

.post-title {
  font-size: 22px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.5;
  margin: 0 0 18px 0;
}

.post-content {
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;
  margin-bottom: 20px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 18px;

  .image-item {
    aspect-ratio: 16 / 10;
    border-radius: 8px;
    background-size: cover;
    background-position: center;
    cursor: pointer;
    transition: transform 0.25s ease;

    &:hover {
      transform: scale(1.03);
    }
  }

  .more-images {
    aspect-ratio: 16 / 10;
    background: rgba($primary-blue, 0.08);
    color: $primary-blue;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 600;
    border-radius: 8px;
    cursor: default;
  }
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;

  .tag {
    font-size: 13px;
    color: $primary-blue;
    background: #E8F3FF;
    padding: 4px 12px;
    border-radius: 14px;
    font-weight: 500;
  }
}

.interaction-bar {
  display: flex;
  gap: 16px;
  padding-top: 18px;
  border-top: 1px solid #F2F3F5;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: #F7F8FA;
    border: none;
    border-radius: 20px;
    font-size: 14px;
    color: $text-secondary;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover:not(:disabled) {
      background: #E8F3FF;
      color: $primary-blue;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    &.active {
      background: #FFF1F0;
      color: #F53F3F;
    }

    .count {
      font-weight: 600;
      margin-left: 2px;
    }
  }
}

.comments-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: $text-primary;
  margin: 0 0 20px 0;
}

.comment-input-box {
  background: #FAFBFC;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 24px;

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
    margin-top: 10px;

    .tip {
      font-size: 12px;
      color: $text-tertiary;
    }
  }
}

.login-tip {
  text-align: center;
  padding: 32px;
  background: #FAFBFC;
  border-radius: 10px;
  margin-bottom: 24px;

  p {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: $text-secondary;
  }
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 14px;

  .comment-avatar {
    background: linear-gradient(135deg, #86909C, #B8BFC9);
    color: white;
    font-size: 14px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .comment-body {
    flex: 1;
    min-width: 0;

    .comment-header {
      display: flex;
      align-items: baseline;
      gap: 10px;
      margin-bottom: 6px;

      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: $text-primary;
      }

      .comment-time {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    .comment-content {
      font-size: 14px;
      color: $text-primary;
      line-height: 1.6;
      margin: 0 0 10px 0;
    }

    .comment-actions {
      display: flex;
      gap: 16px;

      .reply-btn,
      .like-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: none;
        border: none;
        border-radius: 6px;
        font-size: 13px;
        color: $text-tertiary;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background: #F2F3F5;
          color: $primary-blue;
        }

        &.liked {
          color: #F53F3F;
        }
      }
    }

    .reply-input-box {
      margin-top: 12px;
      padding-left: 12px;
      border-left: 2px solid $primary-blue;

      :deep(.arco-input-wrapper) {
        border-radius: 6px;
        
        &:focus-within {
          border-color: $primary-blue;
        }
      }
    }

    .replies-list {
      margin-top: 14px;
      padding: 14px;
      background: #FAFBFC;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .reply-item {
        display: flex;
        gap: 10px;

        .reply-avatar {
          background: linear-gradient(135deg, #B8BFC9, #D1D5DB);
          color: white;
          font-size: 11px;
          font-weight: 600;
          flex-shrink: 0;
        }

        .reply-body {
          flex: 1;
          font-size: 13px;
          line-height: 1.6;
          color: $text-secondary;

          .reply-user {
            font-weight: 600;
            color: $text-primary;
          }

          .reply-to {
            color: $text-tertiary;
            
            strong {
              color: $primary-blue;
            }
          }

          .reply-text {
            color: $text-secondary;
          }

          .reply-meta {
            display: flex;
            gap: 12px;
            margin-top: 6px;
            font-size: 12px;
            color: $text-tertiary;

            .reply-btn-small {
              background: none;
              border: none;
              color: $text-tertiary;
              cursor: pointer;
              
              &:hover {
                color: $primary-blue;
              }
            }
          }
        }
      }
    }
  }
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
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;

  .card-header {
    text-align: center;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #F2F3F5;

    .big-avatar {
      background: linear-gradient(135deg, $primary-blue, #4080FF);
      color: white;
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 12px;
    }

    .card-name {
      font-size: 17px;
      font-weight: 600;
      color: $text-primary;
      margin: 0 0 6px 0;
    }

    .card-desc {
      font-size: 13px;
      color: $text-tertiary;
      margin: 0;
    }
  }

  .card-stats {
    display: flex;
    justify-content: center;
    margin-bottom: 18px;

    .single-stat {
      text-align: center;
      padding: 16px 32px;
      background: linear-gradient(135deg, #E8F3FF 0%, #F0F5FF 100%);
      border-radius: 12px;

      strong {
        display: block;
        font-size: 28px;
        font-weight: 700;
        color: $primary-blue;
        margin: 8px 0 4px 0;
      }

      span {
        font-size: 13px;
        color: $text-tertiary;
      }
    }
  }
}

.recommend-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  .recommend-title {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
    margin: 0 0 16px 0;
  }

  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 14px;

    .recommend-item {
      display: flex;
      gap: 12px;
      padding: 10px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: #F7F8FA;
      }

      .rank {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 13px;
        font-weight: 700;
        color: $text-tertiary;
        background: #F2F3F5;
        border-radius: 6px;
        flex-shrink: 0;

        &.top3 {
          background: linear-gradient(135deg, #FF6B35, #F53F3F);
          color: white;
        }
      }

      .recommend-info {
        flex: 1;
        min-width: 0;

        .recommend-title-text {
          font-size: 13px;
          font-weight: 500;
          color: $text-primary;
          line-height: 1.4;
          margin: 0 0 4px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .recommend-stats {
          font-size: 12px;
          color: $text-tertiary;
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
