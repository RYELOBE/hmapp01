<template>
  <div class="image-uploader">
    <div class="image-uploader__header">
      <div class="upload-area">
        <a-space direction="vertical" :style="{ width: '100%' }">
          <a-upload
            ref="uploadRef"
            :auto-upload="false"
            :fileList="file ? [file] : []"
            :show-file-list="false"
            accept=".png,.jpg,.jpeg"
            :limit="limit"
            image-preview
            @change="onChange"
            @progress="onProgress"
            @before-upload="handleBeforeUpload"
          >
            <template #upload-button>
              <div 
                :class="`arco-upload-list-item${file && file.status === 'error' ? ' arco-upload-list-item-error' : ''}`"
              >
                <div 
                  class="arco-upload-list-picture custom-upload-avatar" 
                  v-if="file && file.url"
                >
                  <img :src="file.url" />
                  <div class="arco-upload-list-picture-mask">
                    <IconEdit />
                  </div>
                  <a-progress
                    v-if="file.status === 'uploading' && file.percent < 100"
                    :percent="file.percent"
                    type="circle"
                    size="mini"
                    :style="{ 
                      position: 'absolute', 
                      left: '50%', 
                      top: '50%', 
                      transform: 'translateX(-50%) translateY(-50%)', 
                    }"
                  />
                </div>
                <div class="arco-upload-picture-card" v-else>
                  <div class="arco-upload-picture-card-text">
                    <icon-plus />
                    <div style="margin-top: 10px; font-weight: 600">上传图片</div>
                  </div>
                </div>
              </div>
            </template>
          </a-upload>
        </a-space>
      </div>
    </div>
    <a-typography-text class="image-uploader__tip">
      支持 JPG/JPEG/PNG 格式，单张图片不超过 5M，最多上传 {{ limit }} 张
    </a-typography-text>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconEdit } from '@arco-design/web-vue/es/icon';
import { getToken } from '@/services/auth';
import { formatJWTToken } from '@/utils/jwt';

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
    default: () => `${window.location.origin}/api/upload`
  },
});

const emit = defineEmits(['update:modelValue']);

const file = ref();
const uploadRef = ref(null);
let objectUrl = null;  // 追踪当前 Blob URL
let isUploading = false;  // 防止并发上传

// 组件卸载时释放内存
onUnmounted(() => {
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
    objectUrl = null;
  }
});

// 初始化已有值（编辑模式）
watch(
  () => props.modelValue,
  (value, oldValue) => {
    console.log('[ImageUploader] modelValue 变化:', { newValue: value, oldValue, currentFile: file.value?.status });
    
    // 如果外部值变化了（比如父组件更新），需要同步更新内部状态
    if (value) {
      const newUrl = typeof value === 'string' ? value : (Array.isArray(value) ? value[0] : null);
      
      // 如果当前没有文件，或者URL变化了，则更新
      if (!file.value || newUrl !== file.value.url || file.value.status === 'error') {
        console.log('[ImageUploader] 重置文件状态为:', newUrl);
        file.value = {
          uid: `init-${Date.now()}`,
          url: newUrl,
          status: 'done',
        };
      }
    } else if (value === '' || value === null || value === undefined) {
      // 如果外部清空了值，也清空内部状态（允许重新上传）
      console.log('[ImageUploader] 清空文件状态');
      file.value = undefined;
    }
  },
  { immediate: true }
);

const handleBeforeUpload = (rawFile) => {
  console.log('[ImageUploader] before-upload 文件:', rawFile);
  
  const isValidType = ['image/png', 'image/jpeg', 'image/jpg'].includes(rawFile.type);
  if (!isValidType) {
    Message.error('只支持 JPG/JPEG/PNG 格式');
    return false;
  }
  
  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    Message.error('图片大小不能超过 5M');
    return false;
  }
  
  return true;
};

const onChange = (_, currentFile) => {
  console.log('[ImageUploader] change 事件:', {
    status: currentFile.status,
    name: currentFile.name,
    hasFile: !!currentFile.file,
    uid: currentFile.uid
  });
  
  // 更新文件状态
  const newObjectUrl = (['init', 'selected', 'uploading'].includes(currentFile.status) && currentFile.file)
    ? URL.createObjectURL(currentFile.file)
    : null;

  // 释放旧的 Blob URL
  if (objectUrl && objectUrl !== newObjectUrl) {
    URL.revokeObjectURL(objectUrl);
  }
  objectUrl = newObjectUrl;

  file.value = {
    ...currentFile,
    ...(newObjectUrl ? { url: newObjectUrl } : {})
  };

  // 如果文件已准备好且是新的选择（不是进度更新），开始上传
  if ((currentFile.status === 'selected' || currentFile.status === 'init') && currentFile.file) {
    console.log('[ImageUploader] 检测到新文件选择，开始上传');
    if (isUploading) {
      console.warn('[ImageUploader] 正在上传中，忽略重复请求');
      return;
    }
    doUpload(currentFile);
  } else if (currentFile.status === 'error') {
    Message.error(currentFile.response?.message || '上传失败');
  }
};

const onProgress = (currentFile) => {
  console.log('[ImageUploader] progress:', currentFile.percent);
  file.value = currentFile;
};

async function doUpload(currentFile) {
  if (isUploading) return;
  isUploading = true;

  // 获取原始文件对象
  const rawFile = currentFile.file || currentFile.originFile;
  
  console.log('[ImageUploader] 开始上传:', {
    name: currentFile.name,
    hasRawFile: !!rawFile,
    fileType: rawFile?.constructor?.name
  });

  if (!rawFile) {
    console.error('[ImageUploader] 无法获取原始文件:', currentFile);
    Message.error('未获取到要上传的文件');
    return;
  }

  try {
    // 更新状态为上传中
    file.value = {
      ...currentFile,
      status: 'uploading',
      percent: 0
    };

    const formData = new FormData();
    formData.append('file', rawFile, rawFile.name || 'image.png');

    const token = getToken();
    const headers = {};
    if (token) {
      headers.Authorization = formatJWTToken(token);
    }

    const uploadUrl = props.uploadUrl.startsWith('http') 
      ? props.uploadUrl 
      : `${window.location.origin}${props.uploadUrl.startsWith('/') ? '' : '/'}${props.uploadUrl}`;

    console.log('[ImageUploader] 上传URL:', uploadUrl);

    // 模拟进度更新
    let lastPercent = 0;
    const progressInterval = setInterval(() => {
      if (lastPercent < 90) {
        lastPercent += Math.random() * 15;
        lastPercent = Math.min(lastPercent, 90);
        file.value = {
          ...file.value,
          percent: Math.round(lastPercent)
        };
      }
    }, 200);

    const response = await fetch(uploadUrl, {
      method: 'POST',
      headers,
      body: formData,
    });

    clearInterval(progressInterval);

    console.log('[ImageUploader] 响应状态:', response.status);

    if (!response.ok) {
      let errorMessage = '上传失败';
      if (response.status === 401 || response.status === 403) {
        errorMessage = '请先登录';
      } else if (response.status === 413) {
        errorMessage = '文件太大，请选择小于5MB的图片';
      }
      
      try {
        const errorData = await response.json();
        console.error('[ImageUploader] 上传失败响应:', errorData);
        errorMessage = errorData.message || errorMessage;
      } catch (e) {
        // 忽略JSON解析错误
      }

      file.value = {
        ...file.value,
        status: 'error',
        response: { message: errorMessage }
      };
      Message.error(errorMessage);
      return;
    }

    const result = await response.json();
    console.log('[ImageUploader] 上传成功响应:', result);

    let url = '';
    
    if (result.url) {
      url = result.url;
    } else if (result.data?.url) {
      url = result.data.url;
    } else if (typeof result === 'string') {
      url = result;
    }
    
    if (!url) {
      console.error('[ImageUploader] 响应中缺少URL:', result);
      throw new Error('上传结果缺少图片地址');
    }

    console.log('[ImageUploader] 提取到图片URL:', url.substring(0, 50) + '...');

    // 更新为完成状态
    file.value = {
      ...file.value,
      status: 'done',
      percent: 100,
      url: url
    };

    // 发射值给父组件
    emitUpdate(url);
    Message.success('上传成功');

  } catch (error) {
    console.error('[ImageUploader] 上传异常:', error);
    file.value = {
      ...file.value,
      status: 'error',
      response: { message: error.message || '上传失败' }
    };
    Message.error(error.message || '上传失败，请重试');
  } finally {
    isUploading = false;
  }
}

function emitUpdate(url) {
  if (props.limit === 1) {
    emit('update:modelValue', url);
  } else {
    // 多图模式暂不支持，可后续扩展
    emit('update:modelValue', [url]);
  }
}
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
  width: fit-content;
}

.custom-upload-avatar {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.arco-upload-picture-card {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  border: 1px dashed var(--color-border, #e5e6eb);
  background: var(--color-bg-1, #ffffff);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:hover {
    border-color: #165dff;
  }

  &-text {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: var(--color-text-3, #86909c);

    .arco-icon {
      font-size: 24px;
    }
  }
}
</style>
