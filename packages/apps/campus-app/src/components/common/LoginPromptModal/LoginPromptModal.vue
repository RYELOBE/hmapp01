<template>
  <a-modal
    :visible="visible"
    :title="null"
    :footer="false"
    :closable="true"
    :mask-closable="true"
    :width="440"
    :centered="true"
    class="login-prompt-modal"
    @cancel="handleClose"
    @close="handleClose"
  >
    <div class="login-prompt-content">
      <div class="prompt-icon-wrapper">
        <icon-lock class="prompt-icon" />
      </div>

      <h3 class="prompt-title">{{ title || '需要登录' }}</h3>
      <p class="prompt-description">
        {{ description || '请先登录后再进行此操作' }}
      </p>

      <div class="prompt-actions">
        <a-button type="primary" size="large" long class="login-btn" @click="handleLogin">
          <template #icon><icon-user /></template>
          去登录
        </a-button>
        <a-button size="large" long class="cancel-btn" @click="handleClose">
          取消
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { IconLock, IconUser } from '@arco-design/web-vue/es/icon';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:visible', 'close', 'login']);

const router = useRouter();

function handleClose() {
  emit('update:visible', false);
  emit('close');
}

function handleLogin() {
  emit('update:visible', false);
  emit('login');
  router.push('/login');
}
</script>

<style lang="scss" scoped>
.login-prompt-content {
  padding: 20px 0;
  text-align: center;

  .prompt-icon-wrapper {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #E8F3FF 0%, #F0F5FF 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .prompt-icon {
      font-size: 32px;
      color: #165DFF;
    }
  }

  .prompt-title {
    font-size: 20px;
    font-weight: 600;
    color: #1D2129;
    margin: 0 0 10px;
  }

  .prompt-description {
    font-size: 14px;
    color: #86909C;
    margin: 0 0 28px;
    line-height: 1.6;
  }

  .prompt-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .login-btn {
      height: 46px;
      font-size: 15px;
      font-weight: 600;
      border-radius: 8px;
      background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
      border: none;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
      }
    }

    .cancel-btn {
      height: 44px;
      font-size: 14px;
      border-radius: 8px;
      border-color: #E5E6EB;
      color: #4E5969;

      &:hover {
        background: #F7F8FA;
        border-color:#C9CDD4;
      }
    }
  }
}
</style>
