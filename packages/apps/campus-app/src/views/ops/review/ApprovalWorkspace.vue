<template>
  <div class="approval-workspace">
    <PageContainer title="审批工作台" :extra-content="totalPendingBadge">
      <!-- 筛选条件提示 -->
      <div v-if="showFilterTip" class="filter-tip-bar">
        <a-tag v-if="filterVendorId" color="blue" closable @close="clearFilter('vendorId')">
          卖家ID: {{ filterVendorId }}
        </a-tag>
        <a-tag v-if="filterItemId" color="green" closable @close="clearFilter('itemId')">
          商品ID: {{ filterItemId }}
        </a-tag>
        <a-button type="text" size="small" @click="clearAllFilters">
          清除所有筛选
        </a-button>
      </div>

      <a-tabs v-model:active-key="activeTab" type="card" @change="handleTabChange">
        <a-tab-pane key="items" :title="tabTitle('items', '待审核商品')">
          <div class="tab-content">
            <!-- 批量操作工具栏 -->
            <div v-if="pendingItems.length > 0" class="batch-toolbar">
              <a-checkbox 
                :checked="isAllItemsSelected" 
                :indeterminate="isItemsIndeterminate"
                @change="(val) => toggleSelectAll('items', val)"
              >
                全选
              </a-checkbox>
              <span class="selected-count">已选 {{ selectedItemsCount }} 项</span>
              <div class="batch-actions">
                <a-button 
                  type="primary" 
                  status="success" 
                  size="small"
                  :disabled="selectedItemsCount === 0"
                  @click="batchApprove('items')"
                >
                  批量通过
                </a-button>
                <a-button 
                  type="primary" 
                  status="danger" 
                  size="small"
                  :disabled="selectedItemsCount === 0"
                  @click="openBatchRejectModal('items')"
                >
                  批量拒绝
                </a-button>
              </div>
            </div>

            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingItems.length > 0">
                <div
                  v-for="item in pendingItems"
                  :key="item.id"
                  class="review-card item-card"
                  :class="{ 'review-card--selected': item._selected }"
                >
                  <div class="card-checkbox">
                    <a-checkbox 
                      v-model="item._selected"
                      @change="updateSelection()"
                    />
                  </div>
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
                      :src="review.itemImage || getFirstImage(review) || ''"
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

        <a-tab-pane key="comments" :title="tabTitle('comments', '待审核评论')">
          <div class="tab-content">
            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingComments.length > 0">
                <div
                  v-for="comment in pendingComments"
                  :key="comment.id"
                  class="review-card comment-card"
                >
                  <div class="card-image">
                    <a-avatar :size="48">{{ (comment.authorName || '用')[0] }}</a-avatar>
                  </div>
                  <div class="card-info">
                    <h3 class="post-title">{{ comment.content?.substring(0, 50) || '无内容' }}{{ comment.content?.length > 50 ? '...' : '' }}</h3>
                    <div class="author-info">
                      <span>{{ comment.authorName || '匿名用户' }}</span>
                      <span class="meta-separator">·</span>
                      <span>帖子: {{ comment.postTitle || '未知' }}</span>
                    </div>
                    <div class="time-info">
                      {{ formatTime(comment.createdAt) }}
                    </div>
                  </div>
                </div>
              </div>
              <a-empty v-else description="暂无需审核的评论" />
            </a-spin>
          </div>
        </a-tab-pane>

        <a-tab-pane key="orders" :title="tabTitle('orders', '异常订单')">
          <div class="tab-content">
            <a-spin :loading="loading">
              <div class="review-list" v-if="pendingOrders.length > 0">
                <div
                  v-for="order in pendingOrders"
                  :key="order.id"
                  class="review-card order-card"
                >
                  <div class="card-icon">
                    <icon-file size="32" color="#FF7D00" />
                  </div>
                  <div class="card-info">
                    <h3 class="post-title">订单 #{{ order.id }}</h3>
                    <div class="author-info">
                      <span>买家: {{ order.buyerName || '未知' }}</span>
                      <span class="meta-separator">·</span>
                      <span>金额: ¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
                    </div>
                    <div class="time-info">
                      {{ formatTime(order.createdAt) }}
                    </div>
                  </div>
                </div>
              </div>
              <a-empty v-else description="暂无异常订单" />
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
import { useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconStorage,
  IconStar,
  IconMessage,
  IconEdit,
  IconFile,
} from "@arco-design/web-vue/es/icon";
import PageContainer from "../../../components/layout/PageContainer/PageContainer.vue";
import { opsHttp as http } from "../../../services/http";

const route = useRoute();
const activeTab = ref("items");
const loading = ref(false);
const submitLoading = ref(false);
const approveModalVisible = ref(false);
const rejectModalVisible = ref(false);
const currentItem = ref(null);
const currentType = ref("");

// 从路由查询参数获取筛选条件
const filterVendorId = ref(null);
const filterItemId = ref(null);
const showFilterTip = computed(() => {
  return filterVendorId.value || filterItemId.value;
});

const pendingItems = ref([]);
const pendingReviews = ref([]);
const pendingPosts = ref([]);
const pendingComments = ref([]);
const pendingOrders = ref([]);

const counts = reactive({
  items: 0,
  reviews: 0,
  circle: 0,
  comments: 0,
  orders: 0,
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
    comments: "评论",
    orders: "订单",
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
    LIKE_NEW: "99新",
    EXCELLENT: "95新",
    GOOD: "8成新",
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
        url = "/reviews/pending";
        break;
      case "circle":
        url = "/ops/circle/pending";
        break;
      case "comments":
        url = "/circle/comments/pending";
        break;
      case "orders":
        url = "/orders?status=REFUND_PENDING";
        break;
    }

    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };

    const res = activeTab.value === 'orders' 
      ? await http.get(url, params)
      : await http.post(url, params);
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
      case "comments":
        pendingComments.value = data?.items || data?.list || [];
        break;
      case "orders":
        pendingOrders.value = Array.isArray(data) ? data : (data?.items || data?.list || []);
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
        url = `/reviews/${currentItem.value.id}/approve`;
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
        url = `/reviews/${currentItem.value.id}/reject`;
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

// ========== 批量操作功能 ==========
const selectedItemsCount = computed(() => {
  const list = getCurrentList();
  return list.filter(item => item._selected).length;
});

const isAllItemsSelected = computed(() => {
  const list = getCurrentList();
  return list.length > 0 && list.every(item => item._selected);
});

const isItemsIndeterminate = computed(() => {
  const list = getCurrentList();
  const selected = list.filter(item => item._selected);
  return selected.length > 0 && selected.length < list.length;
});

function getCurrentList() {
  switch (activeTab.value) {
    case 'items': return pendingItems.value;
    case 'reviews': return pendingReviews.value;
    case 'circle': return pendingPosts.value;
    default: return [];
  }
}

function toggleSelectAll(type, val) {
  const list = type === activeTab.value ? getCurrentList() : [];
  list.forEach(item => { item._selected = !!val; });
}

function updateSelection() {
  // 触发计算属性更新
}

async function batchApprove(type) {
  if (type !== activeTab.value) return;
  
  const selectedList = getCurrentList().filter(item => item._selected);
  if (selectedList.length === 0) return;

  submitLoading.value = true;
  try {
    let successCount = 0;
    
    for (const item of selectedList) {
      try {
        let url = '';
        switch (type) {
          case 'items': url = `/ops/reviews/${item.id}/approve`; break;
          case 'reviews': url = `/reviews/${item.id}/approve`; break;
          case 'circle': url = `/ops/circle/${item.id}/approve`; break;
        }
        
        await http.post(url);
        successCount++;
      } catch (e) {
        console.error(`批量通过失败 (${item.id}):`, e);
      }
    }

    if (successCount > 0) {
      Message.success(`成功通过 ${successCount} 项`);
    }
    
    await Promise.all([loadData(), loadCounts()]);
  } catch (e) {
    console.error('[BatchApprove] error:', e);
    Message.error('批量操作失败');
  } finally {
    submitLoading.value = false;
  }
}

let batchRejectType = ref('');

function openBatchRejectModal(type) {
  if (type !== activeTab.value || selectedItemsCount.value === 0) return;
  
  batchRejectType.value = type;
  rejectForm.reason = '';
  rejectModalVisible.value = true;
}

// 覆盖原有的confirmReject以支持批量操作
const originalConfirmReject = async function confirmReject() {
  if (!rejectForm.reason.trim()) {
    Message.warning('请输入拒绝原因');
    return;
  }

  submitLoading.value = true;
  try {
    // 如果是批量拒绝模式
    if (batchRejectType.value) {
      const selectedList = getCurrentList().filter(item => item._selected);
      let successCount = 0;
      
      for (const item of selectedList) {
        try {
          let url = '';
          switch (batchRejectType.value) {
            case 'items': url = `/ops/reviews/${item.id}/reject`; break;
            case 'reviews': url = `/reviews/${item.id}/reject`; break;
            case 'circle': url = `/ops/circle/${item.id}/reject`; break;
          }
          
          await http.post(url, { reason: rejectForm.reason });
          successCount++;
        } catch (e) {
          console.error(`批量拒绝失败 (${item.id}):`, e);
        }
      }
      
      if (successCount > 0) {
        Message.success(`已拒绝 ${successCount} 项`);
      }
      
      batchRejectType.value = '';
    } else {
      // 单个拒绝（原有逻辑）
      if (!currentItem.value) return;
      
      let url = '';
      switch (currentType.value) {
        case 'items': url = `/ops/reviews/${currentItem.value.id}/reject`; break;
        case 'reviews': url = `/reviews/${currentItem.value.id}/reject`; break;
        case 'circle': url = `/ops/circle/${currentItem.value.id}/reject`; break;
      }
      
      await http.post(url, { reason: rejectForm.reason });
      Message.success(`已拒绝该${getTypeLabel(currentType.value)}`);
    }
    
    rejectModalVisible.value = false;
    await Promise.all([loadData(), loadCounts()]);
  } catch (e) {
    console.error('[ConfirmReject] error:', e);
    Message.error('操作失败，请重试');
  } finally {
    submitLoading.value = false;
  }
};

// 暴出给模板使用
window.confirmReject = originalConfirmReject;

// 初始化路由参数
function initRouteParams() {
  const query = route.query;
  
  // 设置当前Tab
  if (query.tab && ['items', 'reviews', 'circle', 'comments', 'orders'].includes(query.tab)) {
    activeTab.value = query.tab;
  }
  
  // 设置筛选条件
  filterVendorId.value = query.vendorId || null;
  filterItemId.value = query.itemId || null;
  
  // 如果有筛选条件，显示提示信息
  if (showFilterTip.value) {
    setTimeout(() => {
      Message.info(`已应用筛选条件：${filterVendorId.value ? '指定卖家' : ''}${filterItemId.value ? '指定商品' : ''}`);
    }, 500);
  }
}

onMounted(() => {
  initRouteParams();
  loadCounts();
  loadData();
});

// 监听路由变化（当从其他页面跳转过来时）
watch(() => route.query, () => {
  initRouteParams();
  pagination.current = 1;
  loadData();
}, { deep: true });

// 清除单个筛选条件
function clearFilter(type) {
  if (type === 'vendorId') {
    filterVendorId.value = null;
  } else if (type === 'itemId') {
    filterItemId.value = null;
  }
  
  // 更新URL
  const query = { ...route.query };
  delete query[type];
  
  // 使用replace避免浏览器历史记录堆积
  // 注意：这里简化处理，实际可能需要使用router.replace
}

// 清除所有筛选条件
function clearAllFilters() {
  filterVendorId.value = null;
  filterItemId.value = null;
  
  Message.success('已清除所有筛选条件');
  
  // 延迟刷新数据
  setTimeout(() => {
    loadData();
  }, 300);
}
</script>

<style lang="scss" scoped>
.approval-workspace {
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
  padding: 20px;
}

// 筛选条件提示条
.filter-tip-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 8px;

  .arco-tag {
    font-size: 13px;
    font-weight: 500;
  }
}

.tab-content {
  min-height: 400px;
}

// 批量操作工具栏
.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: var(--color-bg-white, #fff);
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border-2, #e5e6eb);

  .selected-count {
    font-size: 13px;
    color: var(--color-text-3, #86909c);
    font-weight: 500;
  }

  .batch-actions {
    display: flex;
    gap: 8px;
    margin-left: auto;
  }
}

// 选中状态
.review-card--selected {
  border-color: #165DFF !important;
  background: linear-gradient(135deg, #f0f5ff 0%, #fff 100%);

  .card-checkbox {
    :deep(.arco-checkbox) {
      .arco-checkbox-icon {
        border-color: #165DFF;
        background: #165DFF;
      }
    }
  }
}

.card-checkbox {
  flex-shrink: 0;
  padding-top: 4px;

  :deep(.arco-checkbox) {
    .arco-checkbox-icon {
      border-radius: 4px;
    }
  }
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
