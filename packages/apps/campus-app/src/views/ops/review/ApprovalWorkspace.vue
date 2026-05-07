<template>
  <div class="approval-workspace">
    <PageContainer title="审批工作台" :extra-content="totalPendingBadge">
      <a-tabs v-model:active-key="activeTab" type="card" @change="handleTabChange">
        <a-tab-pane key="items" :title="tabTitle('items', '待审核商品')">
          <div class="tab-content">
            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingItems.length > 0">
                <div
                  v-for="item in pendingItems"
                  :key="item.id"
                  class="review-card item-card"
                >
                  <div class="card-image">
                    <a-image
                      :src="getFirstImage(item)"
                      width="120"
                      height="120"
                      fit="cover"
                    />
                  </div>
                  <div class="card-info">
                    <h3 class="item-title">{{ item.title }}</h3>
                    <div class="item-meta">
                      <span class="price">¥{{ formatPrice(item.price) }}</span>
                      <a-tag size="small">{{ item.category || '未分类' }}</a-tag>
                      <a-tag size="small" :color="getConditionColor(item.conditionLevel)">
                        {{ getConditionLabel(item.conditionLevel) }}
                      </a-tag>
                    </div>
                    <div class="seller-info">
                      <a-avatar :size="24">{{ (item.sellerName || '卖')[0] }}</a-avatar>
                      <span>{{ item.sellerName || '未知卖家' }}</span>
                      <span class="time">{{ formatDate(item.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="card-actions">
                    <a-button type="primary" status="success" @click="handleApprove(item, 'items')">
                      通过
                    </a-button>
                    <a-button type="primary" status="danger" @click="openRejectModal(item, 'items')">
                      拒绝
                    </a-button>
                  </div>
                </div>
              </div>
              <a-empty v-else description="暂无待审核商品" />

              <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
                <a-pagination
                  :current="pagination.current"
                  :page-size="pagination.pageSize"
                  :total="pagination.total"
                  show-total
                  show-page-size
                  :page-size-options="[10, 20, 50]"
                  @change="handlePageChange"
                  @page-size-change="handlePageSizeChange"
                />
              </div>
            </a-spin>
          </div>
        </a-tab-pane>

        <a-tab-pane key="reviews" :title="tabTitle('reviews', '待审核评价')">
          <div class="tab-content">
            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingReviews.length > 0">
                <div
                  v-for="review in pendingReviews"
                  :key="review.id"
                  class="review-card review-card-item"
                >
                  <div class="card-image">
                    <a-image
                      :src="review.itemImage || ''"
                      width="80"
                      height="80"
                      fit="cover"
                    />
                  </div>
                  <div class="card-info">
                    <div class="item-name">{{ review.itemTitle || '商品' }}</div>
                    <div class="buyer-info">
                      <a-avatar :size="20">{{ (review.buyerName || '买')[0] }}</a-avatar>
                      <span>{{ review.buyerName || '未知买家' }}</span>
                    </div>
                    <a-rate :model-value="review.rating || 0" disabled allow-half />
                    <p class="review-content">{{ truncateText(review.content, 100) }}</p>
                  </div>
                  <div class="card-actions">
                    <a-button type="primary" status="success" size="small" @click="handleApprove(review, 'reviews')">
                      通过
                    </a-button>
                    <a-button type="primary" status="danger" size="small" @click="openRejectModal(review, 'reviews')">
                      拒绝
                    </a-button>
                  </div>
                </div>
              </div>
              <a-empty v-else description="暂无待审核评价" />

              <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
                <a-pagination
                  :current="pagination.current"
                  :page-size="pagination.pageSize"
                  :total="pagination.total"
                  show-total
                  show-page-size
                  :page-size-options="[10, 20, 50]"
                  @change="handlePageChange"
                  @page-size-change="handlePageSizeChange"
                />
              </div>
            </a-spin>
          </div>
        </a-tab-pane>

        <a-tab-pane key="circle" :title="tabTitle('circle', '待审核帖子')">
          <div class="tab-content">
            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingPosts.length > 0">
                <div
                  v-for="post in pendingPosts"
                  :key="post.id"
                  class="review-card post-card"
                >
                  <div class="card-image" v-if="post.coverImage">
                    <a-image
                      :src="post.coverImage"
                      width="120"
                      height="80"
                      fit="cover"
                    />
                  </div>
                  <div class="card-info">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <div class="author-info">
                      <a-avatar :size="20">{{ (post.authorName || '作')[0] }}</a-avatar>
                      <span>{{ post.authorName || '未知作者' }}</span>
                      <span class="time">{{ formatDate(post.createdAt) }}</span>
                    </div>
                    <p class="post-content">{{ truncateText(post.content, 100) }}</p>
                    <div class="tags" v-if="post.tags && post.tags.length > 0">
                      <a-tag v-for="tag in post.tags.slice(0, 3)" :key="tag" size="small">
                        {{ tag }}
                      </a-tag>
                    </div>
                  </div>
                  <div class="card-actions">
                    <a-button type="primary" status="success" size="small" @click="handleApprove(post, 'circle')">
                      通过
                    </a-button>
                    <a-button type="primary" status="danger" size="small" @click="openRejectModal(post, 'circle')">
                      拒绝
                    </a-button>
                  </div>
                </div>
              </div>
              <a-empty v-else description="暂无待审核帖子" />

              <div class="pagination-wrapper" v-if="pagination.total > pagination.pageSize">
                <a-pagination
                  :current="pagination.current"
                  :page-size="pagination.pageSize"
                  :total="pagination.total"
                  show-total
                  show-page-size
                  :page-size-options="[10, 20, 50]"
                  @change="handlePageChange"
                  @page-size-change="handlePageSizeChange"
                />
              </div>
            </a-spin>
          </div>
        </a-tab-pane>
      </a-tabs>
    </PageContainer>

    <a-modal
      v-model:visible="approveModalVisible"
      :title="`确认通过 - ${getTypeLabel(currentType)}`"
      @ok="confirmApprove"
      @cancel="approveModalVisible = false"
      :ok-loading="submitLoading"
    >
      <p>确定要通过该{{ getTypeLabel(currentType) }}吗？</p>
    </a-modal>

    <a-modal
      v-model:visible="rejectModalVisible"
      title="拒绝原因"
      @ok="confirmReject"
      @cancel="rejectModalVisible = false"
      :ok-loading="submitLoading"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="请输入拒绝原因（必填）" field="reason" required>
          <a-textarea
            v-model="rejectForm.reason"
            placeholder="请输入拒绝原因"
            :max-length="200"
            show-word-limit
            :auto-size="{ minRows: 3, maxRows: 6 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { Message } from "@arco-design/web-vue";
import PageContainer from "../../../components/layout/PageContainer/PageContainer.vue";
import { opsHttp as http } from "../../../services/http";

const activeTab = ref("items");
const loading = ref(false);
const submitLoading = ref(false);
const approveModalVisible = ref(false);
const rejectModalVisible = ref(false);
const currentItem = ref(null);
const currentType = ref("");

const pendingItems = ref([]);
const pendingReviews = ref([]);
const pendingPosts = ref([]);

const counts = reactive({
  items: 0,
  reviews: 0,
  circle: 0,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const rejectForm = reactive({
  reason: "",
});

const totalPendingBadge = computed(() => {
  const total = counts.items + counts.reviews + counts.circle;
  return total > 0 ? `待审核总数: ${total}` : null;
});

function tabTitle(key, label) {
  const count = counts[key];
  return count > 0 ? `${label}(${count})` : label;
}

function getTypeLabel(type) {
  const labels = {
    items: "商品",
    reviews: "评价",
    circle: "帖子",
  };
  return labels[type] || "内容";
}

function getFirstImage(record) {
  const urls = record.imageUrls || record.images || [];
  if (typeof urls === "string") {
    try {
      const parsed = JSON.parse(urls);
      return parsed[0] || "";
    } catch {
      return urls || "";
    }
  }
  return Array.isArray(urls) && urls.length > 0 ? urls[0] : "";
}

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function truncateText(text, maxLength) {
  if (!text) return "";
  return text.length > maxLength ? text.slice(0, maxLength) + "..." : text;
}

function getConditionColor(condition) {
  const colors = {
    NEW: "green",
    LIKE_NEW: "cyan",
    GOOD: "blue",
    FAIR: "orange",
    POOR: "red",
  };
  return colors[condition] || "gray";
}

function getConditionLabel(condition) {
  const labels = {
    NEW: "全新",
    LIKE_NEW: "几乎全新",
    GOOD: "良好",
    FAIR: "一般",
    POOR: "较差",
  };
  return labels[condition] || condition;
}

async function loadCounts() {
  try {
    const res = await http.get("/ops/pending-counts");
    const data = res?.data || res;
    if (data) {
      counts.items = data.items ?? 0;
      counts.reviews = data.reviews ?? 0;
      counts.circle = data.circle ?? 0;
    }
  } catch (e) {
    console.error("[ApprovalWorkspace] load counts error:", e);
  }
}

async function loadData() {
  loading.value = true;
  try {
    let url = "";
    switch (activeTab.value) {
      case "items":
        url = "/ops/pending-items";
        break;
      case "reviews":
        url = "/ops/reviews";
        break;
      case "circle":
        url = "/ops/circle/pending";
        break;
    }

    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };

    const res = await http.get(url, { params });
    const data = res?.data || res;

    switch (activeTab.value) {
      case "items":
        pendingItems.value = data?.items || data?.list || [];
        break;
      case "reviews":
        pendingReviews.value = data?.items || data?.list || [];
        break;
      case "circle":
        pendingPosts.value = data?.items || data?.list || [];
        break;
    }

    pagination.total = data?.totalCount ?? data?.total ?? 0;
  } catch (e) {
    console.error("[ApprovalWorkspace] load error:", e);
    Message.error("加载数据失败，请刷新重试");
  } finally {
    loading.value = false;
  }
}

function handleTabChange(key) {
  activeTab.value = key;
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

function openRejectModal(item, type) {
  currentItem.value = item;
  currentType.value = type;
  rejectForm.reason = "";
  rejectModalVisible.value = true;
}

function handleApprove(item, type) {
  currentItem.value = item;
  currentType.value = type;
  approveModalVisible.value = true;
}

async function confirmApprove() {
  if (!currentItem.value) return;

  submitLoading.value = true;
  try {
    let url = "";
    switch (currentType.value) {
      case "items":
        url = `/ops/reviews/${currentItem.value.id}/approve`;
        break;
      case "reviews":
        url = `/ops/reviews/${currentItem.value.id}/approve`;
        break;
      case "circle":
        url = `/ops/circle/${currentItem.value.id}/approve`;
        break;
    }

    await http.post(url);
    Message.success(`${getTypeLabel(currentType.value)}已通过`);
    approveModalVisible.value = false;
    await Promise.all([loadData(), loadCounts()]);
  } catch (e) {
    console.error("[ApprovalWorkspace] approve error:", e);
    Message.error("操作失败，请重试");
  } finally {
    submitLoading.value = false;
  }
}

async function confirmReject() {
  if (!currentItem.value || !rejectForm.reason.trim()) {
    Message.warning("请输入拒绝原因");
    return;
  }

  submitLoading.value = true;
  try {
    let url = "";
    switch (currentType.value) {
      case "items":
        url = `/ops/reviews/${currentItem.value.id}/reject`;
        break;
      case "reviews":
        url = `/ops/reviews/${currentItem.value.id}/reject`;
        break;
      case "circle":
        url = `/ops/circle/${currentItem.value.id}/reject`;
        break;
    }

    await http.post(url, { reason: rejectForm.reason });
    Message.success(`已拒绝该${getTypeLabel(currentType.value)}`);
    rejectModalVisible.value = false;
    await Promise.all([loadData(), loadCounts()]);
  } catch (e) {
    console.error("[ApprovalWorkspace] reject error:", e);
    Message.error("操作失败，请重试");
  } finally {
    submitLoading.value = false;
  }
}

onMounted(() => {
  loadCounts();
  loadData();
});
</script>

<style lang="scss" scoped>
.approval-workspace {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;
}

.tab-content {
  min-height: 400px;
}

.review-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.review-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
  border: 1px solid #f2f3f5;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
    border-color: #165dff40;
  }
}

.card-image {
  flex-shrink: 0;

  :deep(.arco-image) {
    border-radius: 6px;
    overflow: hidden;
  }
}

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-title,
.post-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .price {
    font-size: 18px;
    font-weight: 700;
    color: #f53f3f;
  }
}

.seller-info,
.author-info,
.buyer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4e5969;

  .time {
    margin-left: auto;
    color: #86909c;
    font-size: 12px;
  }
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
}

.review-content,
.post-content {
  font-size: 13px;
  color: #4e5969;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.card-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media screen and (max-width: 1023px) {
  .review-list {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 767px) {
  .review-card {
    flex-direction: column;
  }

  .card-actions {
    flex-direction: row;
    width: 100%;

    .arco-btn {
      flex: 1;
    }
  }

  .seller-info,
  .author-info,
  .buyer-info {
    .time {
      margin-left: 0;
    }
  }
}
</style>
