<template>
  <a-modal
    :visible="visible"
    :title="title"
    @cancel="handleCancel"
    @ok="handleOk"
    :width="720"
    :ok-text="confirmText"
    :cancel-text="cancelText"
  >
    <div class="resource-select-modal">
      <div class="search-bar">
        <a-input
          v-model="searchText"
          placeholder="搜索资源名称"
          allow-clear
          @search="handleSearch"
        >
          <template #prefix-icon>
            <icon-search />
          </template>
        </a-input>
      </div>

      <div class="resource-grid">
        <a-spin :loading="loading">
          <div
            v-for="resource in filteredResources"
            :key="resource.id"
            class="resource-item"
            :class="{ active: selectedResource?.id === resource.id }"
            @click="handleSelect(resource)"
          >
            <div class="resource-preview">
              <div v-if="resource.type === 'image'" class="image-preview" :style="{ backgroundImage: `url(${resource.url})` }"></div>
              <div v-else-if="resource.type === 'icon'" class="icon-preview">{{ resource.icon }}</div>
              <div v-else class="default-preview">
                <icon-file />
              </div>
            </div>
            <div class="resource-info">
              <div class="resource-name">{{ resource.name }}</div>
              <div class="resource-type">{{ getTypeLabel(resource.type) }}</div>
            </div>
          </div>
          <a-empty v-if="!loading && filteredResources.length === 0" />
        </a-spin>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { IconSearch, IconFile } from '@arco-design/web-vue/es/icon';
import { getResourceMenus } from '../services/api';

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '选择资源' },
  confirmText: { type: String, default: '确认' },
  cancelText: { type: String, default: '取消' },
  resourceType: { type: String, default: '' },
});

const emit = defineEmits(['update:visible', 'select', 'cancel']);

const loading = ref(false);
const searchText = ref('');
const resources = ref([]);
const selectedResource = ref(null);

const mockResources = [
  { id: 1, name: '校园Logo 1', type: 'image', url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZTlmNWZmIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkxvZ28gMTwvdGV4dD48L3N2Zz4=', icon: '' },
  { id: 2, name: '校园Logo 2', type: 'image', url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZTlmNWZmIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkxvZ28gMjwvdGV4dD48L3N2Zz4=', icon: '' },
  { id: 3, name: 'Logo 图标', type: 'icon', url: '', icon: '🏫' },
  { id: 4, name: '背景图 1', type: 'image', url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZTlmNWZmIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuS6u+S6lOe6ouS6lw08L3RleHQ+PC9zdmc+', icon: '' },
  { id: 5, name: '图标 购物车', type: 'icon', url: '', icon: '🛒' },
  { id: 6, name: '图标 用户', type: 'icon', url: '', icon: '👤' },
  { id: 7, name: '背景图 2', type: 'image', url: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZTlmNWZmIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuS6u+S6lOe6ouS6lwyPC90ZXh0Pjwvc3ZnPg==', icon: '' },
  { id: 8, name: '图标 消息', type: 'icon', url: '', icon: '📢' },
];

const filteredResources = computed(() => {
  let result = resources.value;
  if (props.resourceType) {
    result = result.filter(r => r.type === props.resourceType);
  }
  if (searchText.value) {
    const text = searchText.value.toLowerCase();
    result = result.filter(r => r.name.toLowerCase().includes(text));
  }
  return result;
});

function getTypeLabel(type) {
  const labels = { image: '图片', icon: '图标', file: '文件' };
  return labels[type] || type;
}

async function loadResources() {
  loading.value = true;
  try {
    resources.value = mockResources;
  } catch (e) {
    resources.value = mockResources;
  } finally {
    loading.value = false;
  }
}

function handleSelect(resource) {
  selectedResource.value = resource;
}

function handleSearch() {
  // 搜索逻辑已通过 computed 属性实现
}

function handleCancel() {
  emit('update:visible', false);
  emit('cancel');
  reset();
}

function handleOk() {
  if (selectedResource.value) {
    emit('select', selectedResource.value);
    emit('update:visible', false);
    reset();
  }
}

function reset() {
  searchText.value = '';
  selectedResource.value = null;
}

watch(() => props.visible, (val) => {
  if (val) {
    loadResources();
  }
});
</script>

<style lang="scss" scoped>
.resource-select-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-bar {
  padding: 0 4px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}

.resource-item {
  display: flex;
  flex-direction: column;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.15s;
  background: #f7f8fa;

  &:hover {
    transform: scale(1.02);
  }

  &.active {
    border-color: #336ad8;
  }
}

.resource-preview {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.image-preview {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.icon-preview {
  font-size: 48px;
}

.default-preview {
  font-size: 40px;
  color: #c9cdd4;
}

.resource-info {
  padding: 10px 12px;
  text-align: center;
}

.resource-name {
  font-size: 13px;
  color: #1d2129;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-type {
  font-size: 12px;
  color: #86909c;
}
</style>
