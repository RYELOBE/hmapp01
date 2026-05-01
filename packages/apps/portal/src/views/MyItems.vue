<template>
  <div class="my-items-page">
    <a-card title="我的商品" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="$router.push('/publish')">
          <template #icon><icon-plus /></template>
          发布商品
        </a-button>
      </template>

      <a-table
        :data="rows"
        :loading="loading"
        :pagination="paginationConfig"
        :row-key="(record) => record.id"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="商品信息" :width="280">
            <template #cell="{ record }">
              <div class="item-cell">
                <a-image
                  :src="getImageUrl(record)"
                  width="56"
                  height="56"
                  fit="cover"
                  style="border-radius: 6px;"
                />
                <div class="item-info">
                  <a-typography-text class="item-title" ellipsis>
                    {{ record.title }}
                  </a-typography-text>
                  <a-typography-text type="danger" class="item-price">
                    ¥{{ record.price }}
                  </a-typography-text>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="分类" data-index="category" :width="100" />
          <a-table-column title="审核状态" :width="100" align="center">
            <template #cell="{ record }">
              <StatusTag :status="record.reviewStatus" />
            </template>
          </a-table-column>
          <a-table-column title="驳回原因" data-index="rejectReason" :width="180">
            <template #cell="{ record }">
              <a-typography-text v-if="record.rejectReason" type="danger" ellipsis>
                {{ record.rejectReason }}
              </a-typography-text>
              <span v-else class="text-muted">-</span>
            </template>
          </a-table-column>
          <a-table-column title="发布时间" data-index="createdAt" :width="160">
            <template #cell="{ record }">
              {{ formatDate(record.createdAt) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="160" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="viewDetail(record)">
                  <template #icon><icon-eye /></template>
                  查看
                </a-button>
                <a-button type="text" size="small" @click="editItem(record)">
                  <template #icon><icon-edit /></template>
                  编辑
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconPlus, IconEye, IconEdit } from "@arco-design/web-vue/es/icon";
import { StatusTag } from "commonprovide/status-tag";
import { getMyItems } from "../services/api";

const router = useRouter();

const rows = ref([]);
const loading = ref(false);

const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50],
});

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      return JSON.parse(urls)[0] || "";
    } catch {
      return urls || "";
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadData() {
  loading.value = true;
  try {
    const result = await getMyItems({
      pageNo: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
    });
    rows.value = result.items || result.rows || [];
    paginationConfig.total = result.total || result.totalCount || 0;
  } catch (error) {
    Message.error(error.message || "加载失败");
  } finally {
    loading.value = false;
  }
}

function handlePageChange(page) {
  paginationConfig.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  paginationConfig.pageSize = size;
  paginationConfig.current = 1;
  loadData();
}

function viewDetail(record) {
  router.push(`/item/${record.id}`);
}

function editItem(record) {
  router.push(`/publish?id=${record.id}`);
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.my-items-page {
  padding: 0;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  .item-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 180px;
  }

  .item-title {
    font-size: 13px;
    color: #1d2129;
    max-width: 160px;
  }

  .item-price {
    font-size: 14px;
    font-weight: 600;
  }
}

.text-muted {
  color: #86909c;
}
</style>
