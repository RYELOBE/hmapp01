<template>
  <a-comment
    :author="author"
    :content="content"
    :datetime="formatTime(datetime)"
    :avatar="undefined"
  >
    <template #avatar>
      <a-avatar :size="avatarSize" class="comment-avatar">
        {{ avatarText }}
      </a-avatar>
    </template>

    <template #actions>
      <slot name="actions">
        <template v-if="showActions">
          <span
            v-if="showReply"
            class="action"
            @click="handleReply"
          >
            <icon-message /> 回复
          </span>
          <span
            v-if="showLike"
            class="action like-action"
            :class="{ liked: isLiked }"
            @click="handleLike"
          >
            <icon-heart-fill v-if="isLiked" />
            <icon-heart v-else />
            {{ likeCount || 0 }}
          </span>
        </template>
      </slot>
    </template>

    <template #content>
      <slot name="content">
        <div class="comment-text">{{ content }}</div>
      </slot>
    </template>

    <!-- 嵌套评论（递归渲染） -->
    <Comment
      v-for="child in replies"
      :key="child.id"
      :comment="child"
      :id="child.id"
      :author="child.userName || '匿名用户'"
      :content="child.content"
      :datetime="child.createTime"
      :like-count="child.likeCount || 0"
      :is-liked="child.isLiked || false"
      :replies="child.replies || []"
      :show-actions="showActions"
      :show-reply="showReply"
      :show-like="showLike"
      :avatar-size="childAvatarSize"
      :on-submit-reply="onSubmitReply"
      :on-like="onLike"
      @reply="$emit('reply', $event)"
      @like="$emit('like', $event)"
    />

    <!-- 回复输入框 -->
    <div v-if="showReplyInput" class="reply-input-wrapper">
      <a-comment
        author="我"
        :avatar="undefined"
      >
        <template #avatar>
          <a-avatar :size="32">{{ userInitial }}</a-avatar>
        </template>
        <template #content>
          <a-textarea
            v-model="replyContent"
            :placeholder="replyPlaceholder"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            size="small"
            @press-enter.exact="submitReply"
          />
          <div class="reply-actions">
            <a-button size="small" @click="cancelReply">取消</a-button>
            <a-button
              type="primary"
              size="small"
              :disabled="!replyContent.trim()"
              :loading="submitting"
              @click="submitReply"
            >
              发送
            </a-button>
          </div>
        </template>
      </a-comment>
    </div>
  </a-comment>
</template>

<script setup>
import { ref, computed } from 'vue';
import {
  IconHeart,
  IconHeartFill,
  IconMessage,
} from '@arco-design/web-vue/es/icon';
import { Message } from '@arco-design/web-vue';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  // 评论数据对象
  comment: {
    type: Object,
    default: () => ({}),
  },
  // 兼容单个字段传递
  id: {
    type: [Number, String],
    default: '',
  },
  author: {
    type: String,
    default: '',
  },
  content: {
    type: String,
    default: '',
  },
  datetime: {
    type: String,
    default: '',
  },
  likeCount: {
    type: Number,
    default: 0,
  },
  isLiked: {
    type: Boolean,
    default: false,
  },
  replies: {
    type: Array,
    default: () => [],
  },
  parentId: {
    type: [Number, String],
    default: null,
  },
  replyToName: {
    type: String,
    default: '',
  },
  // 功能控制
  showActions: {
    type: Boolean,
    default: true,
  },
  showReply: {
    type: Boolean,
    default: true,
  },
  showLike: {
    type: Boolean,
    default: true,
  },
  avatarSize: {
    type: Number,
    default: 40,
  },
  childAvatarSize: {
    type: Number,
    default: 32,
  },
  // 提交回调
  onSubmitReply: {
    type: Function,
    default: null,
  },
  onLike: {
    type: Function,
    default: null,
  },
});

const emit = defineEmits(['reply', 'like', 'submit-reply', 'toggle-like']);

const authStore = useAuthStore();

// 回复相关状态
const showReplyInput = ref(false);
const replyContent = ref('');
const submitting = ref(false);

// 计算属性
const avatarText = computed(() => {
  const name = props.comment?.userName || props.author || '匿名用户';
  return name[0];
});

const userInitial = computed(() => {
  return authStore.user?.nickname?.[0] || '我';
});

const replyPlaceholder = computed(() => {
  if (props.replyToName) {
    return `回复 @${props.replyToName}`;
  }
  return '写下你的回复...';
});

// 方法
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
    minute: '2-digit',
  });
}

function handleReply() {
  if (showReplyInput.value) {
    cancelReply();
  } else {
    showReplyInput.value = true;
    replyContent.value = '';
  }
}

function cancelReply() {
  showReplyInput.value = false;
  replyContent.value = '';
}

async function submitReply() {
  const content = replyContent.value.trim();
  if (!content) return;

  if (!authStore.token) {
    Message.warning('请先登录后再回复');
    return;
  }

  submitting.value = true;

  try {
    // 如果有外部回调函数，优先使用
    if (props.onSubmitReply) {
      await props.onSubmitReply({
        parentId: props.id || props.comment?.id,
        replyToName: props.replyToName || props.author,
        content,
      });
    } else {
      // 否则触发事件
      emit('submit-reply', {
        parentId: props.id || props.comment?.id,
        replyToName: props.replyToName || props.author,
        content,
      });
    }

    Message.success('回复成功 💬');
    cancelReply();
  } catch (e) {
    console.error('[Comment] 回复失败:', e);
    Message.error(e.message || '回复失败');
  } finally {
    submitting.value = false;
  }
}

async function handleLike() {
  if (!authStore.token) {
    Message.warning('请先登录后再操作');
    return;
  }

  try {
    if (props.onLike) {
      await props.onLike(props.id || props.comment?.id);
    } else {
      emit('toggle-like', {
        id: props.id || props.comment?.id,
      });
    }
  } catch (e) {
    console.error('[Comment] 点赞失败:', e);
    Message.error(e.message || '操作失败');
  }
}
</script>

<style scoped>
.comment-avatar {
  background: linear-gradient(135deg, #86909C, #B8BFC9);
  color: white;
  font-weight: 600;
}

.action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  color: var(--color-text-1);
  line-height: 24px;
  background: transparent;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.action:hover {
  background: var(--color-fill-3);
  color: #165DFF;
}

.action.like-action {
  &.liked {
    color: #F53F3F;
  }
}

.reply-input-wrapper {
  margin-top: 12px;
  padding-left: 20px;
  border-left: 3px solid #165DFF;

  :deep(.arco-comment) {
    background: #F7F8FA;
    padding: 12px;
    border-radius: 6px;

    .arco-textarea-wrapper {
      border: 1px solid #E5E6EB;
      border-radius: 6px;

      &:focus-within {
        border-color: #165DFF;
      }
    }
  }

  .reply-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 10px;
  }
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-1);
  word-break: break-word;
}
</style>
