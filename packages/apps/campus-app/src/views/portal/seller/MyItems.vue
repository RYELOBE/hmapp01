<template>
  <div class="my-items-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">我的商品</h2>
      <a-button type="primary" @click="$router.push('/portal/seller/publish')">
        <template #icon><icon-plus /></template>
        发布新商品
      </a-button>
    </div>

    <a-card :bordered="false" class="items-card">
      <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
        <a-tab-pane key="" title="全部" />
        <a-tab-pane key="DRAFT" title="草稿" />
        <a-tab-pane key="PENDING_REVIEW" title="待审核" />
        <a-tab-pane key="APPROVED" title="已通过" />
        <a-tab-pane key="REJECTED" title="已拒绝" />
        <a-tab-pane key="OFF_SHELF" title="已下架" />
      </a-tabs>

      <a-spin :loading="loading" style="width: 100%">
        <div v-if="items.length > 0" class="items-list">
          <div v-for="item in items" :key="item.id" class="item-card">
            <div class="item-image-wrapper">
              <img
                v-if="getImageUrl(item)"
                :src="getImageUrl(item)"
                class="item-image"
              />
              <div v-else class="item-image item-image--empty">📷</div>
            </div>

            <div class="item-info">
              <h3 class="item-title">{{ item.title }}</h3>
              <div class="item-meta">
                <span class="item-price">¥{{ item.price }}</span>
                <ConditionTag v-if="item.conditionLevel" :condition="item.conditionLevel" size="small" />
                <a-tag size="small">{{ categoryLabel(item.category) }}</a-tag>
                <StatusTag :status="item.reviewStatus" size="small" />
              </div>
              <div class="item-stats">
                <span>浏览 {{ item.viewCount || 0 }}</span>
                <span>售出 {{ item.soldCount || 0 }}</span>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>

            <div class="item-actions">
              <template v-if="item.reviewStatus === 'DRAFT'">
                <a-button type="primary" size="small" @click="editItem(item)">编辑</a-button>
                <a-button size="small" @click="publishDraft(item)">发布</a-button>
                <a-popconfirm content="确定要删除吗？" @ok="deleteItem(item)">
                  <a-button type="text" status="danger" size="small">删除</a-button>
                </a-popconfirm>
              </template>

              <template v-else-if="item.reviewStatus === 'PENDING_REVIEW'">
                <a-button size="small" @click="viewDetail(item)">查看</a-button>
                <a-button type="text" status="warning" size="small" @click="withdrawItem(item)">撤回</a-button>
              </template>

              <template v-else-if="item.reviewStatus === 'APPROVED'">
                <a-button type="text" status="warning" size="small" @click="offlineItem(item)">下架</a-button>
                <a-button size="small" @click="editItem(item, true)">编辑需重审</a-button>
              </template>

              <template v-else-if="item.reviewStatus === 'REJECTED'">
                <a-button type="primary" size="small" @click="editItem(item)">修改后重新提交</a-button>
                <a-popconfirm content="确定要删除吗？" @ok="deleteItem(item)">
                  <a-button type="text" status="danger" size="small">删除</a-button>
                </a-popconfirm>
              </template>

              <template v-else-if="item.reviewStatus === 'OFF_SHELF'">
                <a-button type="primary" size="small" @click="onlineItem(item)">上架</a-button>
                <a-button size="small" @click="editItem(item)">编辑</a-button>
                <a-popconfirm content="确定要删除吗？" @ok="deleteItem(item)">
                  <a-button type="text" status="danger" size="small">删除</a-button>
                </a-popconfirm>
              </template>
            </div>
          </div>
        </div>

        <a-empty v-else-if="!loading" description="暂无商品，去发布第一个吧~">
          <template #image>
            <icon-apps size="64" />
          </template>
          <a-button type="primary" @click="$router.push('/portal/seller/publish')">
            发布商品
          </a-button>
        </a-empty>
      </a-spin>

      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <a-pagination
          :current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          show-total
          show-page-size
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconPlus,
  IconApps,
} from "@arco-design/web-vue/es/icon";
import StatusTag from "../../../shared-components/StatusTag/StatusTag.vue";
import ConditionTag from "../../../components/sub/ConditionTag.vue";
import { getMyItems, offShelfItem, deleteItem as apiDeleteItem, updateItem } from "../../../services/api";

const router = useRouter();
const loading = ref(false);
const items = ref([]);
const activeStatus = ref("");
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const STATUS_MAP = {
  DRAFT: { label: "草稿", color: "gray" },
  PENDING_REVIEW: { label: "审核中", color: "orange" },
  APPROVED: { label: "已通过", color: "green" },
  REJECTED: { label: "已拒绝", color: "red" },
  OFF_SHELF: { label: "已下架", color: "gray" },
};

const CATEGORY_MAP = {
  digital: "数码", book: "教材", clothing: "服饰",
  daily: "生活", sport: "运动", instrument: "乐器", other: "其他",
};

function getImageUrl(record) {
  const urls = record.imageUrls || record.images || record.coverUrl || [];
  if (typeof urls === "string") {
    try { return JSON.parse(urls)[0]; } catch { return urls; }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : null;
}

function formatTime(dateStr) {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString("zh-CN");
}

function categoryLabel(c) { return CATEGORY_MAP[c] || c || ""; }

async function loadData() {
  loading.value = true;
  try {
    const params = {
      mine: true,
      status: activeStatus.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await getMyItems(params);
    items.value = res?.items || res?.rows || [];
    pagination.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    Message.error(e.message || "加载失败");
  } finally {
    loading.value = false;
  }
}

function handleTabChange(key) {
  activeStatus.value = key;
  pagination.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.current = page;
  loadData();
}

function handlePageSizeChange(size) {
  pagination.pageSize = size;
  pagination.current = 1;
  loadData();
}

function viewDetail(item) { router.push(`/portal/item/${item.id}`); }
function editItem(item, needReaudit = false) { router.push(`/portal/seller/publish?id=${item.id}${needReaudit ? '&reaudit=1' : ''}`); }

async function publishDraft(item) {
  try {
    await updateItem(item.id, { reviewStatus: "PENDING_REVIEW" });
    Message.success("已提交审核");
    loadData();
  } catch (e) { Message.error(e.message || "操作失败"); }
}

async function offlineItem(item) {
  Modal.confirm({
    title: "确认下架",
    content: `确定要下架「${item.title}」吗？`,
    onOk: async () => {
      try {
        await offShelfItem(item.id);
        Message.success("已下架");
        loadData();
      } catch (e) { Message.error(e.message || "操作失败"); }
    },
  });
}

async function onlineItem(item) {
  try {
    await updateItem(item.id, { reviewStatus: "PENDING_REVIEW" });
    Message.success("已提交上架申请");
    loadData();
  } catch (e) { Message.error(e.message || "操作失败"); }
}

async function withdrawItem(item) {
  Modal.confirm({ title: "确认撤回", content: "确定要撤回审核吗？", onOk: async () => {
    try {
      await updateItem(item.id, { reviewStatus: "DRAFT" });
      Message.success("已撤回");
      loadData();
    } catch (e) { Message.error(e.message || "操作失败"); }
  }});
}

async function deleteItem(item) {
  try {
    await apiDeleteItem(item.id);
    Message.success("已删除");
    loadData();
  } catch (e) { Message.error(e.message || "删除失败"); }
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.my-items-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }
}

.items-card {
  border-radius: 12px;

  :deep(.arco-tabs-nav) { padding: 0 8px; }
  :deep(.arco-tabs-content) { padding-top: 16px; }
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.2s;

  &:hover {
    background: #f5f7fa;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  .item-image-wrapper {
    flex-shrink: 0;
  }

  .item-image {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      font-size: 28px;
    }
  }

  .item-info {
    flex: 1;
    min-width: 0;

    .item-title {
      margin: 0 0 8px;
      font-size: 15px;
      font-weight: 500;
      color: #1d2129;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .item-price {
        font-size: 17px;
        font-weight: 700;
        color: #f53f3f;
      }
    }

    .item-stats {
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: #86909c;
    }
  }

  .item-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
}

:deep(.arco-empty) { padding: 80px 20px; }
</style>
