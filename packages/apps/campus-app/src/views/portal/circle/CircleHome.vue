<template>
  <div class="my-circle-page">
    <!-- 统一页面头部（参考订单页） -->
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" class="back-btn" @click="$router.back()">
          <template #icon><icon-arrow-left /></template>
          返回
        </a-button>
        <h2 class="page-title">我的圈子</h2>
      </div>
      <div class="header-right">
        <span class="item-count">共 {{ posts.length }} 条动态</span>
        <a-button type="primary" shape="round" size="small" @click="$router.push('/portal/circle/publish')">
          <template #icon><icon-plus /></template>
          发布动态
        </a-button>
      </div>
    </div>

    <!-- 状态筛选Tab（参考订单页） -->
    <div class="status-tabs">
      <div 
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['tab-item', { active: activeStatus === tab.value }]"
        @click="handleStatusChange(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-input-search
        v-model="searchKeyword"
        placeholder="搜索我的动态..."
        allow-clear
        @search="handleSearch"
      />
    </div>

    <!-- 内容区域 -->
    <a-spin :loading="loading" style="width: 100%; min-height: 400px;">
      <!-- 帖子列表 -->
      <div v-if="filteredPosts.length > 0" class="posts-list">
        <div
          v-for="post in filteredPosts"
          :key="post.id"
          class="post-item"
          @click="handlePostClick(post)"
        >
          <!-- 帖子头部 -->
          <div class="post-header">
            <div class="author-info">
              <a-avatar :size="36" class="author-avatar">
                {{ (post.authorName || '用')[0] }}
              </a-avatar>
              <div class="author-meta">
                <span class="author-name">{{ post.authorName || '匿名用户' }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-status-tag">
              <a-tag v-if="post.status === 'PUBLISHED'" color="green">已发布</a-tag>
              <a-tag v-else-if="post.status === 'PENDING'" color="orange">审核中</a-tag>
              <a-tag v-else color="gray">{{ post.status }}</a-tag>
            </div>
          </div>

          <!-- 帖子内容 -->
          <div class="post-body">
            <h3 class="post-title">{{ post.title || '无标题' }}</h3>
            <p v-if="post.content" class="post-content">{{ post.content }}</p>

            <!-- 图片预览 -->
            <div v-if="post.images && post.images.length > 0" class="post-images">
              <div
                v-for="(img, index) in post.images.slice(0, 3)"
                :key="index"
                class="image-thumb"
                :style="{ backgroundImage: `url(${img})` }"
              />
              <div v-if="post.images.length > 3" class="more-images">
                +{{ post.images.length - 3 }}
              </div>
            </div>

            <!-- 标签 -->
            <div v-if="post.tags && post.tags.length > 0" class="post-tags">
              <a-tag v-for="tag in post.tags.slice(0, 3)" :key="tag" size="small">
                #{{ tag }}
              </a-tag>
            </div>
          </div>

          <!-- 帖子底部操作 -->
          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item">
                <icon-eye /> {{ post.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <icon-heart /> {{ post.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <icon-message /> {{ post.commentCount || 0 }}
              </span>
            </div>
            <div class="post-actions">
              <a-button type="text" size="small" @click.stop="handleEdit(post)">
                <icon-edit /> 编辑
              </a-button>
              <a-button type="text" size="small" status="danger" @click.stop="handleDelete(post)">
                <icon-delete /> 删除
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" class="empty-state">
        <template #image>
          <icon-file style="font-size: 64px; color: #c9cdd4;" />
        </template>
        <template #description>
          <span style="font-size: 14px; color: #86909c;">暂无相关动态</span>
        </template>
        <a-button type="primary" shape="round" @click="$router.push('/portal/circle/publish')">
          发布第一条动态
        </a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import {
  IconPlus,
  IconHeart,
  IconMessage,
  IconEye,
  IconEdit,
  IconDelete,
  IconArrowLeft,
  IconFile,
} from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '../../../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// 状态管理
const loading = ref(false);
const posts = ref([]);
const activeStatus = ref('all');
const searchKeyword = ref('');

// 状态筛选Tab（参考订单页）
const statusTabs = ref([
  { label: '全部', value: 'all' },
  { label: '已发布', value: 'published' },
  { label: '审核中', value: 'pending' },
]);

// 计算属性：过滤后的帖子
const filteredPosts = computed(() => {
  let items = posts.value;

  // 状态筛选
  if (activeStatus.value !== 'all') {
    const statusMap = {
      'published': 'PUBLISHED',
      'pending': 'PENDING',
    };
    items = items.filter(p => p.status === statusMap[activeStatus.value]);
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    items = items.filter(p =>
      (p.title || '').toLowerCase().includes(keyword) ||
      (p.content || '').toLowerCase().includes(keyword)
    );
  }

  return items;
});

// 方法
function handleStatusChange(status) {
  activeStatus.value = status;
}

function handleSearch(keyword) {
  searchKeyword.value = keyword;
}

function handlePostClick(post) {
  router.push(`/portal/circle/${post.id}`);
}

function handleEdit(post) {
  // TODO: 跳转到编辑页面
  Message.info('编辑功能开发中...');
}

function handleDelete(post) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除"${post.title}"吗？此操作不可恢复。`,
    okText: '删除',
    okProps: { status: 'danger' },
    onOk: async () => {
      try {
        await fetch(`/api/circle/posts/${post.id}`, { method: 'DELETE' });
        Message.success('删除成功');
        posts.value = posts.value.filter(p => p.id !== post.id);
      } catch (e) {
        Message.error('删除失败，请稍后重试');
      }
    },
  });
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

async function loadMyPosts() {
  loading.value = true;
  try {
    const userId = authStore.user?.id;
    const params = new URLSearchParams({
      page: '1',
      size: '50',
      userId: userId,
    });

    const response = await fetch(`/api/circle/posts?${params}`);
    const data = await response.json();

    const rawData = data.data?.posts || data.posts || data.list || [];
    posts.value = (Array.isArray(rawData) ? rawData : []).map(item => ({
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
      status: item.status || 'PUBLISHED',
      createdAt: item.createdAt || item.create_time || new Date().toISOString(),
    }));

    // 更新Tab计数
    statusTabs.value[1].count = posts.value.filter(p => p.status === 'PUBLISHED').length;
    statusTabs.value[2].count = posts.value.filter(p => p.status === 'PENDING').length;

  } catch (error) {
    console.error('[MyCircle] 加载失败:', error);
    Message.error('加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadMyPosts();
});
</script>

<style lang="scss" scoped>
.my-circle-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f7f8fa;
  min-height: 100vh;
}

/* ========== 统一页面头部（与订单页一致） ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #4080FF 0%, #165DFF 50%, #0E42D2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    color: rgba(255, 255, 255, 0.9);
    &:hover {
      color: white;
      background: rgba(255, 255, 255, 0.15);
    }
  }

  .page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .item-count {
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

/* ========== 状态筛选Tab（参考订单页） ========== */
.status-tabs {
  display: flex;
  gap: 8px;
  padding: 16px 24px;
  background: white;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .tab-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    color: #4E5969;
    transition: all 0.25s ease;
    border: 1px solid transparent;

    &:hover {
      background: #F2F3F5;
    }

    &.active {
      background: #165DFF;
      color: white;
      font-weight: 500;
      box-shadow: 0 2px 8px rgba(22, 93, 255, 0.25);
    }

    .tab-count {
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

/* ========== 搜索栏 ========== */
.search-bar {
  margin-bottom: 16px;

  :deep(.arco-input-wrapper) {
    border-radius: 8px;
    
    &:focus-within {
      border-color: #165DFF;
      box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
    }
  }
}

/* ========== 帖子列表 ========== */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .author-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .author-avatar {
      background: linear-gradient(135deg, #4080FF, #165DFF);
      color: white;
      font-weight: 600;
    }

    .author-meta {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .author-name {
        font-size: 15px;
        font-weight: 600;
        color: #1D2129;
      }

      .post-time {
        font-size: 13px;
        color: #86909C;
      }
    }
  }
}

.post-body {
  margin-bottom: 16px;

  .post-title {
    font-size: 17px;
    font-weight: 600;
    color: #1D2129;
    line-height: 1.5;
    margin: 0 0 12px 0;
  }

  .post-content {
    font-size: 14px;
    color: #4E5969;
    line-height: 1.7;
    margin: 0 0 12px 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .post-images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-bottom: 12px;

    .image-thumb {
      width: 100%;
      height: 120px;
      background-size: cover;
      background-position: center;
      border-radius: 6px;
      background-color: #f5f6f8;
    }

    .more-images {
      grid-column: 3;
      grid-row: 1 / span 2;
      background: rgba(22, 93, 255, 0.1);
      color: #165DFF;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      font-weight: 600;
      border-radius: 6px;
      cursor: default;
    }
  }

  .post-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;

  .post-stats {
    display: flex;
    gap: 20px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #86909C;

      &:hover {
        color: #165DFF;
      }
    }
  }

  .post-actions {
    display: flex;
    gap: 8px;

    :deep(.arco-btn) {
      font-size: 13px;
    }
  }
}

/* ========== 空状态 ========== */
.empty-state {
  padding: 80px 20px;
  background: white;
  border-radius: 10px;
}
</style>
