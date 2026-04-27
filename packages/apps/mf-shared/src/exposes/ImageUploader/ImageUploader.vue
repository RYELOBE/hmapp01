<template>
  <div class="wrap">
    <a-upload
      list-type="picture-card"
      accept=".png,.jpg,.jpeg"
      v-model:file-list="fileList"
      :limit="limit"
      image-preview
      :custom-request="handleUpload"
      @before-upload="handleBeforeUpload"
      @remove="handleRemove"
    >
      <template #upload-button>
        <div class="upload-button">
          <IconPlus />
          <div class="upload-text">上传图片</div>
        </div>
      </template>
    </a-upload>
    <div class="arco-upload-tooltip">支持JPG/JPEG/PNG格式，大小不超过2M</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { getToken } from '../auth-sdk.js';

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: '',
  },
  limit: {
    type: Number,
    default: 1,
  },
  uploadUrl: {
    type: String,
    default: '/api/upload',
  },
});

const emit = defineEmits(['update:modelValue']);

const fileList = ref([]);

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      const urls = Array.isArray(value) ? value : [value];
      fileList.value = urls.map((url, index) => ({
        uid: Date.now() + index,
        url,
        status: 'done',
      }));
    } else {
      fileList.value = [];
    }
  },
  { immediate: true }
);

// 上传前校验
const handleBeforeUpload = (file) => {
  const isValidType = ['image/png', 'image/jpeg', 'image/jpg'].includes(file.type);
  if (!isValidType) {
    Message.error('只支持 JPG/JPEG/PNG 格式');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    Message.error('图片大小不能超过 2M');
    return false;
  }
  return true;
};

// 自定义上传
const handleUpload = async (options) => {
  const { file, onProgress, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append('file', file);

  try {
    const token = getToken();
    const response = await fetch(props.uploadUrl, {
      method: 'POST',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error('上传失败');
    }

    const result = await response.json();
    const url = result.url || result.data?.url || '';

    onSuccess(url);
    emit('update:modelValue', url);
    Message.success('上传成功');
  } catch (error) {
    onError(error);
    Message.error('上传失败，请重试');
  }
};

// 移除图片
const handleRemove = () => {
  emit('update:modelValue', '');
};
</script>

<style scoped>
.wrap {
  display: flex;
  flex-direction: column;
}

.upload-button {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--color-text-3, #86909c);
}

.upload-text {
  margin-top: 8px;
  font-size: 14px;
}

.arco-upload-tooltip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-3, #86909c);
}
</style>
