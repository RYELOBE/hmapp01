<template>
  <div class="rich-editor" :class="{ 'rich-editor--disabled': disabled }">
    <div class="rich-editor__toolbar" v-if="!disabled">
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('bold') }"
        @click="editor?.chain().focus().toggleBold().run()"
        title="加粗"
      >
        B
      </button>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('italic') }"
        @click="editor?.chain().focus().toggleItalic().run()"
        title="斜体"
      >
        I
      </button>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('strike') }"
        @click="editor?.chain().focus().toggleStrike().run()"
        title="删除线"
      >
        S
      </button>
      <div class="rich-editor__divider"></div>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('bulletList') }"
        @click="editor?.chain().focus().toggleBulletList().run()"
        title="无序列表"
      >
        • List
      </button>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('orderedList') }"
        @click="editor?.chain().focus().toggleOrderedList().run()"
        title="有序列表"
      >
        1. List
      </button>
      <div class="rich-editor__divider"></div>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('heading', { level: 1 }) }"
        @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
        title="标题1"
      >
        H1
      </button>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('heading', { level: 2 }) }"
        @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
        title="标题2"
      >
        H2
      </button>
      <div class="rich-editor__divider"></div>
      <button
        type="button"
        class="rich-editor__button"
        @click="triggerImageUpload"
        title="插入图片"
      >
        🖼️
      </button>
      <button
        type="button"
        class="rich-editor__button"
        :class="{ 'rich-editor__button--active': editor?.isActive('link') }"
        @click="setLink"
        title="插入链接"
      >
        🔗
      </button>
    </div>

    <div class="rich-editor__content">
      <editor-content :editor="editor" />
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      style="display: none"
      @change="handleImageUpload"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { useEditor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入内容...',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  height: {
    type: [Number, String],
    default: 200,
  },
});

const emit = defineEmits(['update:modelValue', 'image-upload:before', 'image-upload:success', 'image-upload:error']);

const computedHeight = computed(() => {
  return typeof props.height === 'number' ? `${props.height}px` : props.height;
});

const fileInputRef = ref(null);

const editor = useEditor({
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2],
      },
    }),
    Image.configure({
      inline: true,
    }),
    Link.configure({
      openOnClick: false,
    }),
    Placeholder.configure({
      placeholder: props.placeholder,
    }),
  ],
  content: props.modelValue,
  editable: !props.disabled,
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML());
  },
});

watch(
  () => props.modelValue,
  (value) => {
    if (editor.value && value !== editor.value.getHTML()) {
      editor.value.commands.setContent(value);
    }
  }
);

watch(
  () => props.disabled,
  (value) => {
    if (editor.value) {
      editor.value.setEditable(!value);
    }
  }
);

function triggerImageUpload() {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
}

async function handleImageUpload(event) {
  const file = event.target.files[0];
  if (!file) return;

  const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
  if (!validTypes.includes(file.type)) {
    emit('image-upload:error', new Error('只支持 JPG/PNG/WebP 格式'));
    return;
  }

  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    emit('image-upload:error', new Error('图片大小不能超过 5MB'));
    return;
  }

  emit('image-upload:before', file);

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fetch('/api/upload/image', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('上传失败');
    }

    const result = await response.json();
    const url = result.url || result.data?.url || '';

    if (url && editor.value) {
      editor.value.chain().focus().setImage({ src: url }).run();
    }

    emit('image-upload:success', url);
  } catch (error) {
    emit('image-upload:error', error);
  } finally {
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
  }
}

function setLink() {
  if (!editor.value) return;

  const previousUrl = editor.value.getAttributes('link').href;
  const url = window.prompt('输入链接地址:', previousUrl);

  if (url === null) return;

  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run();
    return;
  }

  editor.value.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
}

onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy();
  }
});
</script>

<style lang="scss" scoped>
.rich-editor {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  background: var(--color-bg-1, #ffffff);

  &--disabled {
    opacity: 0.6;
    pointer-events: none;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    gap: 4px;
    height: 44px;
    padding: 0 12px;
    background: #f7f8fa;
    border-bottom: 1px solid #e5e6eb;
  }

  &__button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    border: none;
    border-radius: 4px;
    background: transparent;
    color: var(--color-text-1, #1d2129);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: #e5e6eb;
    }

    &--active {
      background: #165dff;
      color: #ffffff;
    }
  }

  &__divider {
    width: 1px;
    height: 20px;
    background: #e5e6eb;
    margin: 0 4px;
  }

  &__content {
    min-height: v-bind(computedHeight);
    padding: 12px;

    :deep(.tiptap) {
      outline: none;
      min-height: calc(v-bind(computedHeight) - 24px);

      &:focus {
        outline: none;
      }

      img {
        max-width: 100%;
        height: auto;
        border-radius: 4px;
      }

      h1 {
        font-size: 24px;
        font-weight: 700;
        margin: 16px 0 8px;
      }

      h2 {
        font-size: 20px;
        font-weight: 600;
        margin: 14px 0 6px;
      }

      ul,
      ol {
        padding-left: 20px;
        margin: 8px 0;
      }

      a {
        color: #165dff;
        text-decoration: underline;
        cursor: pointer;
      }

      p.is-editor-empty:first-child::before {
        content: attr(data-placeholder);
        float: left;
        color: var(--color-text-4, #c9cdd4);
        pointer-events: none;
        height: 0;
      }
    }
  }
}
</style>
