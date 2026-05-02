<template>
  <div class="portal-list">
    <a-table
      :data="portals"
      :loading="loading"
      :pagination="pagination"
      :bordered="false"
      row-key="portalCode"
      class="portal-table"
      @page-change="handlePageChange"
    >
      <template #columns>
        <a-table-column title="门户名称" data-index="portalName" :width="180">
          <template #cell="{ record }">
            <div class="portal-name-cell">
              <div class="portal-name-cell__icon" :style="{ background: record.themeColor }">
                {{ record.portalName[0] }}
              </div>
              <span>{{ record.portalName }}</span>
            </div>
          </template>
        </a-table-column>
        <a-table-column title="门户编码" data-index="portalCode" :width="120" />
        <a-table-column title="模板类型" data-index="templateType" :width="120">
          <template #cell="{ record }">
            <a-tag size="small" :color="record.templateType === 'backstage' ? 'blue' : 'green'">
              {{ record.templateType === 'backstage' ? '后台门户' : '开放门户' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="所属分组" data-index="groupName" :width="120">
          <template #cell="{ record }">
            <span>{{ record.groupName || '-' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="关联资源系统" data-index="appCode" :width="140">
          <template #cell="{ record }">
            <a-tag v-if="record.appCode" size="small" color="arcoblue">
              {{ record.appCode === 'ops' ? '运营系统(ops)' : '门户系统(portal)' }}
            </a-tag>
            <span v-else style="color: #c9cdd4;">未关联</span>
          </template>
        </a-table-column>
        <a-table-column title="更新时间" data-index="updatedAt" :width="180" />
        <a-table-column title="操作" :width="300" align="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="primary" size="small" @click="$emit('design', record)">
                <template #icon><icon-palette /></template>
                设计
              </a-button>
              <a-button size="small" @click="$emit('preview', record)">
                <template #icon><icon-eye /></template>
                预览
              </a-button>
              <a-button size="small" @click="$emit('edit', record)">
                <template #icon><icon-edit /></template>
                编辑
              </a-button>
              <a-popconfirm content="确认删除此门户？" @ok="$emit('delete', record.portalCode)">
                <a-button size="small" status="danger">
                  <template #icon><icon-delete /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { IconPalette, IconEye, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon';

defineProps({
  portals: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  pagination: { type: Object, default: () => ({ pageSize: 10, current: 1, total: 0 }) },
});

const emit = defineEmits(['design', 'preview', 'edit', 'delete', 'page-change']);

function handlePageChange(page) {
  emit('page-change', page);
}
</script>

<style lang="scss" scoped>
.portal-list {
  height: 100%;
}

.portal-table {
  border-radius: 8px;
  overflow: hidden;
}

.portal-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #336ad8 0%, #6d9fff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;
  }
}
</style>