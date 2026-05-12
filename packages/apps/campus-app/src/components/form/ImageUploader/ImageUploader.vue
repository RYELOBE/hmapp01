<template>
  <div class="image-uploader">
    <div class="image-list">
      <!-- 已上传的图片列表 -->
      <div
        v-for="(file, index) in files"
        :key="file.uid"
        class="image-item"
      >
        <img :src="file.url" />
        <div class="image-mask">
          <icon-eye @click="handlePreview(file)" />
          <icon-delete @click="handleRemove(index)" />
        </div>
        <a-progress
          v-if="file.status === 'uploading'"
          :percent="file.percent"
          type="circle"
          size="mini"
          class="upload-progress"
        />
      </div>

      <!-- 上传按钮：始终显示，除非达到limit -->
      <div
        v-if="files.length < limit"
        class="upload-btn"
        @click="triggerUpload"
      >
        <icon-plus />
        <div class="upload-text">上传图片</div>
      </div>
    </div>

    <!-- 隐藏的input -->
    <input
      ref="inputRef"
      type="file"
      accept=".png,.jpg,.jpeg"
      style="display: none"
      @change="handleFileChange"
    />

    <a-typography-text class="image-uploader__tip">
      支持 JPG/JPEG/PNG 格式，单张图片不超过 5M，最多上传 {{ limit }} 张
    </a-typography-text>

    <!-- 图片预览 -->
    <a-image-preview-group v-if="previewVisible" v-model:visible="previewVisible">
      <a-image :src="previewUrl" style="display: none" />
    </a-image-preview-group>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted, computed } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconEye, IconDelete } from '@arco-design/web-vue/es/icon';
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

const files = ref([]);
const inputRef = ref(null);
const previewVisible = ref(false);
const previewUrl = ref('');
const uploadingIndex = ref(-1);

// 初始化已有值
watch(
  () => props.modelValue,
  (value) => {
    if (!value) {
      files.value = [];
      return;
    }

    const urls = typeof value === 'string'
      ? (value ? [value] : [])
      : (Array.isArray(value) ? value : []);

    // 只在初始化或外部更新时同步
    if (files.value.length === 0 || urls.length === 0) {
      files.value = urls.map((url, i) => ({
        uid: `init-${i}-${Date.now()}`,
        url,
        status: 'done',
      }));
    }
  },
  { immediate: true }
);

function triggerUpload() {
  if (files.value.length >= props.limit) {
    Message.warning(`最多上传 ${props.limit} 张图片`);
    return;
  }
  inputRef.value?.click();
}

async function handleFileChange(e) {
  const rawFile = e.target.files?.[0];
  if (!rawFile) return;

  // 验证文件
  const isValidType = ['image/png', 'image/jpeg', 'image/jpg'].includes(rawFile.type);
  if (!isValidType) {
    Message.error('只支持 JPG/JPEG/PNG 格式');
    return;
  }

  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    Message.error('图片大小不能超过 5M');
    return;
  }

  // 检查数量限制
  if (files.value.length >= props.limit) {
    Message.warning(`最多上传 ${props.limit} 张图片`);
    return;
  }

  // 创建预览URL
  const previewUrl = URL.createObjectURL(rawFile);
  const fileItem = {
    uid: `upload-${Date.now()}`,
    url: previewUrl,
    status: 'uploading',
    percent: 0,
    file: rawFile,
  };

  files.value.push(fileItem);
  uploadingIndex.value = files.value.length - 1;

  // 清空input
  e.target.value = '';

  // 开始上传
  await doUpload(fileItem, files.value.length - 1);
}

async function doUpload(fileItem, index) {
  try {
    const formData = new FormData();
    formData.append('file', fileItem.file, fileItem.file.name || 'image.png');

    const token = getToken();
    const headers = {};
    if (token) {
      headers.Authorization = formatJWTToken(token);
    }

    const uploadUrl = props.uploadUrl.startsWith('http')
      ? props.uploadUrl
      : `${window.location.origin}${props.uploadUrl.startsWith('/') ? '' : '/'}${props.uploadUrl}`;

    // 模拟进度
    const progressInterval = setInterval(() => {
      if (files.value[index]?.percent < 90) {
        files.value[index].percent += Math.random() * 15;
        files.value[index].percent = Math.min(files.value[index].percent, 90);
      }
    }, 200);

    const response = await fetch(uploadUrl, {
      method: 'POST',
      headers,
      body: formData,
    });

    clearInterval(progressInterval);

    if (!response.ok) {
      throw new Error(response.status === 401 ? '请先登录' : '上传失败');
    }

    const result = await response.json();
    const url = result.url || result.data?.url || result;

    if (!url) {
      throw new Error('上传结果缺少图片地址');
    }

    // 更新状态
    files.value[index] = {
      ...files.value[index],
      status: 'done',
      percent: 100,
      url,
    };

    // 释放预览URL
    if (fileItem.url?.startsWith('blob:')) {
      URL.revokeObjectURL(fileItem.url);
    }

    emitUpdate();
    Message.success('上传成功');

  } catch (error) {
    files.value[index].status = 'error';
    Message.error(error.message || '上传失败');
    // 移除失败的文件
    setTimeout(() => {
      files.value.splice(index, 1);
    }, 1000);
  }
}

function handleRemove(index) {
  const file = files.value[index];
  if (file.url?.startsWith('blob:')) {
    URL.revokeObjectURL(file.url);
  }
  files.value.splice(index, 1);
  emitUpdate();
}

function handlePreview(file) {
  previewUrl.value = file.url;
  previewVisible.value = true;
}

function emitUpdate() {
  const urls = files.value
    .filter(f => f.status === 'done' && f.url && !f.url.startsWith('blob:'))
    .map(f => f.url);

  if (props.limit === 1) {
    emit('update:modelValue', urls[0] || '');
  } else {
    emit('update:modelValue', urls);
  }
}

onUnmounted(() => {
  files.value.forEach(f => {
    if (f.url?.startsWith('blob:')) {
      URL.revokeObjectURL(f.url);
    }
  });
});
</script>

<style lang="scss" scoped>
.image-uploader {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border, #e5e6eb);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    opacity: 0;
    transition: opacity 0.2s;
    cursor: pointer;

    .arco-icon {
      font-size: 20px;
      color: #fff;
    }
  }

  &:hover .image-mask {
    opacity: 1;
  }

  .upload-progress {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.upload-btn {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  border: 1px dashed var(--color-border, #e5e6eb);
  background: var(--color-bg-1, #ffffff);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--color-text-3, #86909c);

  &:hover {
    border-color: #165dff;
    color: #165dff;
  }

  .arco-icon {
    font-size: 24px;
  }

  .upload-text {
    font-size: 12px;
    font-weight: 500;
  }
}

.image-uploader__tip {
  font-size: 12px;
  color: var(--color-text-3, #86909c);
}
</style>
