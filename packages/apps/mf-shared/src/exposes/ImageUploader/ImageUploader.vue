<template>
  <div class="image-uploader">
    <div class="image-uploader__header">
      <div class="upload-area">
        <a-upload
          ref="uploadRef"
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
              <icon-plus />
              <div class="upload-text">上传图片</div>
            </div>
          </template>
        </a-upload>
      </div>
    </div>
    <a-typography-text class="image-uploader__tip">
      支持 JPG/JPEG/PNG 格式，单张图片不超过 2M，最多上传 {{ limit }} 张
    </a-typography-text>
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
const uploadRef = ref(null);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      const urls = Array.isArray(value) ? value : [value];
      fileList.value = urls
        .filter(url => url)
        .map((url, index) => ({
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
    
    const currentUrls = fileList.value
      .filter(f => f.status === 'done' && f.url)
      .map(f => f.url);
    
    if (props.limit === 1) {
      emit('update:modelValue', url);
    } else {
      emit('update:modelValue', [...currentUrls, url]);
    }
    
    Message.success('上传成功');
  } catch (error) {
    onError(error);
    Message.error('上传失败，请重试');
  }
};

const handleRemove = (file) => {
  const urls = fileList.value
    .filter(f => f.uid !== file.uid && f.status === 'done' && f.url)
    .map(f => f.url);
  
  if (props.limit === 1) {
    emit('update:modelValue', '');
  } else {
    emit('update:modelValue', urls);
  }
};
</script>

<style lang="scss" scoped>
.image-uploader {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__header {
    width: 100%;
  }

  &__tip {
    font-size: 12px;
    color: var(--color-text-3, #86909c);
  }
}

.upload-area {
  :deep(.arco-upload-list) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  :deep(.arco-upload-picture-card) {
    width: 96px;
    height: 96px;
    border-radius: 8px;
    border: 1px dashed var(--color-border, #e5e6eb);
    background: var(--color-bg-1, #ffffff);
    transition: all 0.2s;

    &:hover {
      border-color: #165dff;
    }
  }
}

.upload-button {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--color-text-3, #86909c);
  width: 100%;
  height: 100%;

  :deep(.arco-icon) {
    font-size: 24px;
    margin-bottom: 4px;
  }
}

.upload-text {
  font-size: 12px;
  color: #86909c;
}
</style>
