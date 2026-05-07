<template>
  <div class="circle-publish-page">
    <div class="publish-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <a-button @click="$router.back()" type="text" size="small" class="back-btn">
          <template #icon><icon-arrow-left /></template>
          返回
        </a-button>
        <h2 class="page-title">发布动态</h2>
      </div>

      <!-- 发布表单 -->
      <a-spin :loading="submitting" class="form-spin">
        <div class="publish-form">
          <!-- 标题输入 -->
          <div class="form-section title-section">
            <label class="section-label">标题</label>
            <a-input
              v-model="form.title"
              placeholder="请输入标题（最多50字）"
              :max-length="50"
              show-word-limit
              size="large"
            />
          </div>

          <!-- 富文本编辑器 / 预览切换 -->
          <div class="form-section content-section">
            <label class="section-label">内容</label>
            <div class="editor-tabs">
              <span
                :class="['tab-item', { active: !isPreview }]"
                @click="isPreview = false"
              >
                编辑
              </span>
              <span
                :class="['tab-item', { active: isPreview }]"
                @click="isPreview = true"
              >
                预览
              </span>
            </div>

            <RichEditor
              v-if="!isPreview"
              v-model="form.content"
              placeholder="分享你的想法、经验或发现..."
              :height="400"
            />

            <!-- 预览模式 -->
            <div v-else class="preview-content">
              <div v-if="form.content" v-html="form.content"></div>
              <a-empty v-else description="暂无内容" />
            </div>
          </div>

          <!-- 话题标签 -->
          <div class="form-section tags-section">
            <label class="section-label">话题标签（选填，最多5个）</label>
            <div class="tags-input-wrapper">
              <div class="tags-list">
                <a-tag
                  v-for="(tag, index) in form.tags"
                  :key="index"
                  closable
                  color="arcoblue"
                  @close="removeTag(index)"
                >
                  {{ tag }}
                </a-tag>
              </div>
              <a-input
                v-model="tagInput"
                placeholder="输入标签，按回车添加（以#开头）"
                size="small"
                style="width: 240px;"
                @press-enter="addTag"
              />
            </div>
            <p class="tags-tip">热门标签：#学习资料 #生活好物 #闲置转让 #经验分享 #求助问答</p>
          </div>

          <!-- 操作按钮 -->
          <div class="action-bar">
            <a-button size="large" @click="saveDraft">
              保存草稿
            </a-button>
            <a-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="!canSubmit"
              @click="submitPost"
            >
              发布动态
            </a-button>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconArrowLeft } from '@arco-design/web-vue/es/icon';
import RichEditor from '../../../components/form/RichEditor/RichEditor.vue';

const router = useRouter();

const submitting = ref(false);
const isPreview = ref(false);
const tagInput = ref('');

const form = ref({
  title: '',
  content: '',
  tags: [],
});

const canSubmit = computed(() => {
  return form.value.title.trim().length > 0 && form.value.content.trim().length > 0;
});

let draftTimer = null;

function addTag() {
  const tag = tagInput.value.trim();
  if (!tag) return;

  if (!tag.startsWith('#')) {
    Message.warning('标签需要以 # 开头');
    return;
  }

  if (form.value.tags.length >= 5) {
    Message.warning('最多添加5个标签');
    return;
  }

  if (form.value.tags.includes(tag)) {
    Message.warning('该标签已存在');
    return;
  }

  form.value.tags.push(tag);
  tagInput.value = '';
}

function removeTag(index) {
  form.value.tags.splice(index, 1);
}

function saveDraft() {
  const draft = {
    ...form.value,
    savedAt: new Date().toISOString(),
  };
  localStorage.setItem('circle_draft', JSON.stringify(draft));
  Message.success('草稿已保存');
}

function loadDraft() {
  try {
    const draftStr = localStorage.getItem('circle_draft');
    if (draftStr) {
      const draft = JSON.parse(draftStr);
      form.value = {
        title: draft.title || '',
        content: draft.content || '',
        tags: draft.tags || [],
      };
    }
  } catch (e) {
    console.error('加载草稿失败:', e);
  }
}

async function submitPost() {
  if (!canSubmit.value) {
    Message.warning('请填写标题和内容');
    return;
  }

  submitting.value = true;
  try {
    // 提取内容中的图片URL
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = form.value.content;
    const images = Array.from(tempDiv.querySelectorAll('img')).map(img => img.src);

    const postData = {
      title: form.value.title,
      content: form.value.content,
      images,
      tags: form.value.tags,
    };

    const response = await fetch('/api/circle/posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(postData),
    });

    if (!response.ok) {
      throw new Error('发布失败');
    }

    // 清除草稿
    localStorage.removeItem('circle_draft');

    Message.success('发布成功！');
    router.push('/portal/circle');
  } catch (e) {
    console.error('发布失败:', e);
    Message.error(e.message || '发布失败');
  } finally {
    submitting.value = false;
  }
}

// 自动保存草稿（每30秒）
function autoSaveDraft() {
  if (form.value.title || form.value.content) {
    saveDraft();
  }
}

onMounted(() => {
  loadDraft();
  draftTimer = setInterval(autoSaveDraft, 30000);
});

onUnmounted(() => {
  if (draftTimer) {
    clearInterval(draftTimer);
  }
});
</script>

<style lang="scss" scoped>
.circle-publish-page {
  min-height: 100vh;
  background: var(--color-bg-2, #F5F6F7);
  padding: 20px;
}

.publish-container {
  max-width: 800px;
  margin: 0 auto;
}

.form-spin {
  width: 100%;
}

.publish-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

  .back-btn {
    color: var(--color-text-2, #4E5969);
  }

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-1, #1D2129);
  }
}

.form-section {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-1, #E5E6EB);

  &:last-of-type {
    border-bottom: none;
  }
}

.section-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  margin-bottom: 12px;
}

.title-section {
  :deep(.arco-input-wrapper) {
    border-radius: 8px;

    .arco-input {
      font-size: 16px;
    }
  }
}

.editor-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  background: var(--color-fill-1, #F7F8FA);
  padding: 3px;
  border-radius: 8px;
  width: fit-content;

  .tab-item {
    padding: 6px 20px;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.25s ease;
    color: var(--color-text-2, #4E5969);
    user-select: none;

    &:hover {
      color: var(--color-text-1, #1D2129);
    }

    &.active {
      background: #FFFFFF;
      color: #165DFF;
      font-weight: 500;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    }
  }
}

.preview-content {
  min-height: 400px;
  padding: 16px;
  border: 1px solid var(--color-border-1, #E5E6EB);
  border-radius: 8px;
  background: var(--color-bg-1, #FFFFFF);
  line-height: 1.8;
  color: var(--color-text-1, #1D2129);

  img {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
  }
}

.tags-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.tags-tip {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
  margin: 8px 0 0 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px;
  background: var(--color-bg-2, #F7F8FA);
  border-top: 1px solid var(--color-border-1, #E5E6EB);

  button {
    flex: 1;
  }
}

@media (max-width: 767px) {
  .circle-publish-page {
    padding: 12px;
  }

  .page-header,
  .form-section,
  .action-bar {
    padding-left: 16px;
    padding-right: 16px;
  }

  .tags-input-wrapper {
    :deep(.arco-input-wrapper) {
      width: 100% !important;
    }
  }
}
</style>
