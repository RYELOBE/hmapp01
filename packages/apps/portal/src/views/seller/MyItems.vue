<template>
  <div class="my-items-page">
    <div class="my-items-page__header">
      <h2>我的闲置</h2>
      <a-button type="primary" @click="$router.push('/seller/publish')">
        <template #icon><span>➕</span></template>发布闲置
      </a-button>
    </div>

    <!-- 状态 Tab -->
    <a-tabs v-model:active-key="activeStatus" @change="handleTabChange">
      <a-tab-pane key="" title="全部" />
      <a-tab-pane key="PENDING_REVIEW" title="审核中" />
      <a-tab-pane key="APPROVED" title="已上架" />
      <a-tab-pane key="REJECTED" title="已驳回" />
      <a-tab-pane key="OFF_SHELF" title="已下架" />
    </a-tabs>

    <!-- 商品列表 -->
    <a-table :data="items" :loading="loading" :pagination="pagination" @page-change="handlePageChange">
      <template #columns>
        <a-table-column title="商品" :width="300">
          <template #cell="{ record }">
            <div class="item-cell">
              <img v-if="record.coverUrl" :src="record.coverUrl" class="item-cell__img" />
              <div v-else class="item-cell__img item-cell__img--empty">📷</div>
              <div class="item-cell__info">
                <div class="item-cell__title">{{ record.title }}</div>
                <div class="item-cell__price">¥{{ record.price }}</div>
              </div>
            </div>
          </template>
        </a-table-column>
        <a-table-column title="分类" data-index="category" :width="100">
          <template #cell="{ record }">
            <a-tag size="small">{{ categoryLabel(record.category) }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="状态" :width="100">
          <template #cell="{ record }">
            <a-tag :color="statusColor(record.reviewStatus)" size="small">{{ statusLabel(record.reviewStatus) }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="160" align="right">
          <template #cell="{ record }">
            <a-space>
              <a-button v-if="record.reviewStatus === 'APPROVED'" type="text" size="small" status="warning" @click="handleOffShelf(record)">
                下架
              </a-button>
              <a-button type="text" size="small" @click="$router.push(`/item/${record.id}`)">查看</a-button>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import { http } from "commonprovide/http";

const items = ref([]);
const loading = ref(false);
const activeStatus = ref("");
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
});

const STATUS_MAP = {
  PENDING: { label: "审核中", color: "orange" },
  PENDING_REVIEW: { label: "审核中", color: "orange" },
  APPROVED: { label: "已上架", color: "green" },
  REJECTED: { label: "已驳回", color: "red" },
  OFF_SHELF: { label: "已下架", color: "gray" },
};

const CATEGORY_MAP = {
  digital: "数码", book: "教材", clothing: "服饰",
  daily: "生活", sport: "运动", instrument: "乐器", other: "其他",
};

function statusLabel(s) { return STATUS_MAP[s]?.label || s || "未知"; }
function statusColor(s) { return STATUS_MAP[s]?.color || "gray"; }
function categoryLabel(c) { return CATEGORY_MAP[c] || c || "—"; }

async function loadData() {
  loading.value = true;
  try {
    const params = {
      mine: true,
      status: activeStatus.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    const res = await http.get("/items/mine", { params });
    items.value = res?.items || res?.rows || [];
    pagination.value.total = res?.totalCount ?? res?.total ?? 0;
  } catch (e) {
    console.error("[MyItems] load error:", e);
  } finally {
    loading.value = false;
  }
}

function handleTabChange() {
  pagination.value.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.value.current = page;
  loadData();
}

function handleOffShelf(record) {
  Modal.confirm({
    title: "确认下架",
    content: `确定要下架「${record.title}」吗？下架后买家将无法看到此商品。`,
    onOk: async () => {
      try {
        await http.post(`/items/${record.id}/off-shelf`);
        Message.success("已下架");
        loadData();
      } catch (e) {
        Message.error(e.message || "操作失败");
      }
    },
  });
}

onMounted(loadData);
</script>

<style lang="scss" scoped>
.my-items-page {
  background: #fff;
  border-radius: 16px;
  padding: 24px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    h2 { margin: 0; font-size: 20px; font-weight: 700; }
  }
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  &__img {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f2f3f5;
      color: #c9cdd4;
      font-size: 24px;
    }
  }

  &__info { min-width: 0; }
  &__title {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  &__price {
    font-size: 14px;
    font-weight: 700;
    color: #f53f3f;
    margin-top: 2px;
  }
}
</style>
