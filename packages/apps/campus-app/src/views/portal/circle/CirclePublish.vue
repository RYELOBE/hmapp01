<template>
  <div class="circle-publish-page">
    <PageHeader title="发布动态" subtitle="分享你的校园生活" />

    <div class="publish-container">
      <div class="publish-form">
        <!-- 标题 -->
        <div class="form-section">
          <label class="section-label">标题</label>
          <a-input
            v-model="form.title"
            placeholder="请输入标题（最多50字）"
            :max-length="50"
            show-word-limit
            size="large"
          />
        </div>

        <!-- 内容 -->
        <div class="form-section">
          <label class="section-label">内容</label>
          <a-textarea
            v-model="form.content"
            placeholder="分享你的想法、经验或发现..."
            :max-length="2000"
            show-word-limit
            :auto-size="{ minRows: 5, maxRows: 10 }"
          />
        </div>

        <!-- 图片上传（本地预览） -->
        <div class="form-section">
          <label class="section-label">
            图片
            <span class="label-hint">选填，最多9张</span>
          </label>
          <div class="image-grid">
            <div
              v-for="(img, index) in previewImages"
              :key="index"
              class="image-item"
            >
              <img :src="img" alt="" />
              <span class="image-delete" @click="removeImage(index)">
                <icon-close />
              </span>
            </div>
            <label v-if="previewImages.length < 9" class="image-add" @click="triggerUpload">
              <icon-plus />
              <span>添加图片</span>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                multiple
                style="display: none"
                @change="handleFileSelect"
              />
            </label>
          </div>
        </div>

        <!-- 话题标签 -->
        <div class="form-section">
          <label class="section-label">
            话题标签
            <span class="label-hint">选填</span>
          </label>
          <div class="tags-row">
            <a-tag
              v-for="tag in hotTags"
              :key="tag"
              :class="{ 'tag-active': form.tags.includes(tag) }"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </a-tag>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="action-bar">
          <a-button
            type="primary"
            size="large"
            long
            :loading="submitting"
            :disabled="!canSubmit"
            @click="submitPost"
          >
            发布动态
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { IconPlus, IconClose } from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '../../../stores/auth';
import PageHeader from '../../../components/common/PageHeader/PageHeader.vue';

const router = useRouter();
const authStore = useAuthStore();

const submitting = ref(false);
const fileInputRef = ref(null);
const hotTags = ref([]);
const previewImages = ref([]);

const form = ref({
  title: '',
  content: '',
  tags: [],
});

const canSubmit = computed(() => {
  return form.value.title.trim().length > 0 && form.value.content.trim().length > 0;
});

onMounted(async () => {
  try {
    const res = await fetch('/api/dict/options');
    const data = await res.json();
    const tags = data?.data?.tags || [];
    hotTags.value = tags.map(t => t.value || t.label);
  } catch (e) {
    console.error('加载标签失败:', e);
    hotTags.value = ['#学习资料', '#生活好物', '#闲置转让', '#经验分享', '#求助问答'];
  }
});

function toggleTag(tag) {
  const idx = form.value.tags.indexOf(tag);
  if (idx > -1) {
    form.value.tags.splice(idx, 1);
  } else {
    form.value.tags.push(tag);
  }
}

function triggerUpload() {
  fileInputRef.value?.click();
}

function handleFileSelect(e) {
  const files = Array.from(e.target.files || []);
  const remaining = 9 - previewImages.value.length;
  if (remaining <= 0) return;

  files.slice(0, remaining).forEach(file => {
    if (!file.type.startsWith('image/')) return;
    if (file.size > 5 * 1024 * 1024) {
      Message.warning(`文件 ${file.name} 超过5MB限制`);
      return;
    }

    const reader = new FileReader();
    reader.onload = (ev) => {
      previewImages.value.push(ev.target.result);
    };
    reader.readAsDataURL(file);
  });

  e.target.value = '';
}

function removeImage(index) {
  previewImages.value.splice(index, 1);
}

async function submitPost() {
  if (!authStore.isLoggedIn) {
    Modal.confirm({
      title: '需要登录',
      content: '发布动态需要先登录账号，是否前往登录/注册？',
      okText: '去登录',
      cancelText: '取消',
      onOk() {
        router.push({ path: '/login', query: { redirect: '/portal/circle/publish' } });
      },
    });
    return;
  }

  if (!canSubmit.value) {
    Message.warning('请填写标题和内容');
    return;
  }

  submitting.value = true;
  try {
    const postData = {
      title: form.value.title,
      content: form.value.content,
      images: JSON.stringify(previewImages.value),
      tags: form.value.tags.join(','),
    };

    const response = await fetch('/api/circle/posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`,
      },
      body: JSON.stringify(postData),
    });

    const result = await response.json();

    if (result.code === 200) {
      Message.success('动态已提交，等待审核通过后即可显示');
      router.push('/portal/circle');
    } else if (response.status === 401 || response.status === 403) {
      Modal.confirm({
        title: '登录已过期',
        content: '请重新登录后再发布动态',
        okText: '去登录',
        cancelText: '取消',
        onOk() {
          router.push({ path: '/login', query: { redirect: '/portal/circle/publish' } });
        },
      });
    } else {
      throw new Error(result.message || '发布失败');
    }
  } catch (e) {
    console.error('发布失败:', e);
    Message.error(e.message || '发布失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="scss" scoped>
.circle-publish-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
}

.publish-container {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.publish-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.form-section {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-fill-2, #F2F3F5);

  &:last-of-type {
    border-bottom: none;
  }
}

.section-label {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  margin-bottom: 12px;

  .label-hint {
    font-weight: 400;
    font-size: 13px;
    color: var(--color-text-3, #86909C);
  }
}

:deep(.arco-input-wrapper),
:deep(.arco-textarea-wrapper) {
  border-radius: 8px;

  .arco-input,
  .arco-textarea {
    font-size: 14px;
  }
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border-1, #E5E6EB);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-delete {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.2s;

    &:hover {
      background: rgba(255, 71, 87, 0.85);
    }
  }

  &:hover .image-delete {
    opacity: 1;
  }
}

.image-add {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  border: 1.5px dashed var(--color-border-2, #C9CDD4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-3, #86909C);
  transition: all 0.2s;
  font-size: 12px;

  &:hover {
    border-color: rgb(22, 93, 255);
    color: rgb(22, 93, 255);
    background: rgba(22, 93, 255, 0.03);
  }

  :deep(.arco-icon) {
    font-size: 22px;
  }
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .arco-tag {
    cursor: pointer;
    border-radius: 16px;
    padding: 4px 14px;
    font-size: 13px;
    transition: all 0.2s;
    border-color: var(--color-border-1, #E5E6EB);

    &:hover:not(.tag-active) {
      border-color: rgb(22, 93, 255);
      color: rgb(22, 93, 255);
    }

    &.tag-active {
      background: rgba(22, 93, 255, 0.08);
      border-color: rgb(22, 93, 255);
      color: rgb(22, 93, 255);
    }
  }
}

.action-bar {
  padding: 20px 24px;

  :deep(.arco-btn-primary) {
    height: 44px;
    font-size: 15px;
    font-weight: 500;
    border-radius: 10px;

    &.arco-btn-disabled {
      opacity: 0.5;
    }
  }
}

@media (max-width: 767px) {
  .publish-container {
    padding: 12px 12px 30px;
  }

  .form-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .image-item,
  .image-add {
    width: 80px;
    height: 80px;
  }
}
</style>
