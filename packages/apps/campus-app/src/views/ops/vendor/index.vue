<template>
  <div class="vendor-page">
    <div class="vendor-page__header">
      <h2>供方管理</h2>
      <span class="vendor-page__desc">管理所有卖方用户及其商品统计</span>
    </div>

    <!-- 筛选工具栏 -->
    <div class="vendor-page__filter">
      <a-row :gutter="16" align="center">
        <a-col :flex="'280px'">
          <a-input-search
            v-model="keyword"
            placeholder="搜索卖家用户名"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :flex="'160px'">
          <a-select v-model="statusFilter" placeholder="用户状态" @change="handleStatusChange" allow-clear>
            <a-option value="ACTIVE">正常</a-option>
            <a-option value="DISABLED">已禁用</a-option>
          </a-select>
        </a-col>
        <a-col :flex="1">
          <div class="batch-actions" v-if="selectedRowKeys.length > 0">
            <span class="selected-info">已选 {{ selectedRowKeys.length }} 项</span>
            <a-button type="primary" status="warning" size="small" @click="batchToggleStatus">
              <icon-lock /> 批量禁用
            </a-button>
            <a-popconfirm content="确定要删除选中的卖家吗？此操作将同时删除其所有商品和订单数据！" @ok="batchDelete">
              <a-button type="primary" status="danger" size="small">
                <icon-delete /> 批量删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 数据表格 -->
    <a-table
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :columns="VENDOR_COLUMNS"
      row-key="id"
      :row-selection="{ type: 'checkbox', showCheckedAll: true, selectedRowKeys, onSelectionChange: handleSelectionChange }"
      :expandable="expandConfig"
      @page-change="handlePageChange"
    >
      <template #username="{ record }">
        <a-space>
          <a-avatar :size="32">{{ record.username?.[0] || '供' }}</a-avatar>
          <div>
            <div style="font-weight: 500; color: var(--color-text-1);">{{ record.username }}</div>
            <div style="font-size: 12px; color: var(--color-text-4);">ID: {{ record.id }}</div>
          </div>
        </a-space>
      </template>

      <template #activeItems="{ record }">
        <a-tag color="green" size="small">{{ record.activeItems || 0 }}</a-tag>
      </template>

      <template #pendingItems="{ record }">
        <a-badge 
          v-if="record.pendingItems > 0" 
          :count="record.pendingItems" 
          :max-count="99"
          @click.stop="goToReview(record)"
        >
          <a-tag color="orange" size="small" style="cursor: pointer;">
            {{ record.pendingItems }}
          </a-tag>
        </a-badge>
        <span v-else style="color: #c9cdd4;">0</span>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'" size="small">
          {{ record.status === 'ACTIVE' ? '正常' : '已禁用' }}
        </a-tag>
      </template>

      <template #operations="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="viewDetail(record)">
            <icon-eye /> 详情
          </a-button>
          <a-button 
            type="text" 
            size="small" 
            :status="record.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleVendorStatus(record)"
          >
            <icon-lock v-if="record.status === 'ACTIVE'" />
            <icon-unlock v-else />
            {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
          </a-button>
          <a-popconfirm 
            content="确定要删除该卖家吗？此操作将同时删除其所有商品、订单和相关数据，且不可恢复！" 
            @ok="deleteVendor(record)"
          >
            <a-button type="text" size="small" status="danger">
              <icon-delete /> 删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>

      <!-- 展开行：显示商品列表 -->
      template #expand-row="{ record }">
        <div class="expand-content">
          <div class="expand-header">
            <h4 style="margin: 0; font-size: 14px; color: var(--color-text-1);">
              <icon-storage /> 商品列表 ({{ expandedItems[record.id]?.length || 0 }})
            </h4>
            <a-button type="primary" size="small" @click="goToReview(record)">
              <icon-check-circle /> 快速审核待审核商品
            </a-button>
          </div>
          
          <a-table
            :data="expandedItems[record.id] || []"
            :loading="expandLoading[record.id]"
            :pagination="false"
            size="small"
            :bordered="false"
          >
            <template #columns>
              <a-table-column title="商品图片" data-index="image" :width="80">
                <template #cell="{ record }">
                  <a-image 
                    :src="getFirstImage(record)" 
                    width="60" 
                    height="60" 
                    fit="cover"
                    style="border-radius: 6px;"
                  />
                </template>
              </a-table-column>
              <a-table-column title="商品名称" data-index="title" :ellipsis="true" :tooltip="true" />
              <a-table-column title="价格" data-index="price" :width="100">
                <template #cell="{ record }">
                  <span style="color: #f53f3f; font-weight: 600;">¥{{ formatPrice(record.price) }}</span>
                </template>
              </a-table-column>
              <a-table-column title="状态" data-index="status" :width="100">
                <template #cell="{ record }">
                  <a-tag :color="getItemStatusColor(record.status)" size="small">
                    {{ getItemStatusLabel(record.status) }}
                  </a-tag>
                </template>
              </a-table-column>
              <a-table-column title="发布时间" data-index="createdAt" :width="160" />
              <a-table-column title="操作" data-index="operations" :width="120">
                <template #cell="{ record }">
                  <a-button v-if="record.status === 'PENDING'" type="text" size="small" status="success" @click="goToItemReview(record)">
                    审核
                  </a-button>
                </template>
              </a-table-column>
            </template>
          </a-table>
          
          <a-empty v-if="!expandLoading[record.id] && (!expandedItems[record.id] || expandedItems[record.id].length === 0)" description="暂无商品" />
        </div>
      </template>
    </a-table>

    <!-- 卖家详情抽屉 -->
    <a-drawer
      v-model:visible="detailVisible"
      :title="`卖家详情 - ${currentVendor?.username}`"
      :width="640"
      :footer="false"
    >
      <a-spin :loading="detailLoading">
        <div v-if="currentVendor" class="vendor-detail">
          <a-descriptions :column="2" bordered size="medium">
            <a-descriptions-item label="用户ID">{{ currentVendor.id }}</a-descriptions-item>
            <a-descriptions-item label="用户名">{{ currentVendor.username }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ currentVendor.email || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ currentVendor.phone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="currentVendor.status === 'ACTIVE' ? 'green' : 'red'" size="small">
                {{ currentVendor.status === 'ACTIVE' ? '正常' : '已禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="注册时间">{{ formatDateTime(currentVendor.createdAt) }}</a-descriptions-item>
            <a-descriptions-item label="商品总数">{{ currentVendor.totalItems || 0 }}</a-descriptions-item>
            <a-descriptions-item label="在售商品">{{ currentVendor.activeItems || 0 }}</a-descriptions-item>
            <a-descriptions-item label="已售商品">{{ currentVendor.soldItems || 0 }}</a-descriptions-item>
            <a-descriptions-item label="待审核商品">
              <a-badge v-if="(currentVendor.pendingItems || 0) > 0" :count="currentVendor.pendingItems">
                <a-tag color="orange" size="small">{{ currentVendor.pendingItems }}</a-tag>
              </a-badge>
              <span v-else>0</span>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <div class="detail-actions">
            <a-space>
              <a-button type="primary" @click="goToReview(currentVendor)">
                <icon-check-circle /> 审核待审核商品
              </a-button>
              <a-button @click="viewAllItems(currentVendor)">
                <icon-storage /> 查看所有商品
              </a-button>
            </a-space>
          </div>

          <a-divider />

          <h4 style="margin-bottom: 12px;">最近发布的5个商品</h4>
          <a-list :data="recentItems" :bordered="false">
            <template #item="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.title || '未命名商品'">
                  <template #avatar>
                    <a-image 
                      :src="getFirstImage(item)" 
                      width="48" 
                      height="48" 
                      fit="cover"
                      style="border-radius: 6px;"
                    />
                  </template>
                  <template #description>
                    <a-space :size="8">
                      <a-tag :color="getItemStatusColor(item.status)" size="small">
                        {{ getItemStatusLabel(item.status) }}
                      </a-tag>
                      <span style="color: #f53f3f; font-weight: 500;">¥{{ formatPrice(item.price) }}</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <span style="font-size: 12px; color: var(--color-text-4);">{{ formatDateTime(item.createdAt) }}</span>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-if="!recentItems || recentItems.length === 0" description="暂无商品" />
        </div>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconEye,
  IconLock,
  IconUnlock,
  IconDelete,
  IconStorage,
  IconCheckCircle,
} from "@arco-design/web-vue/es/icon";
import { opsHttp as http } from "../../../services/http";
import { VENDOR_COLUMNS } from "./const";

const router = useRouter();
const tableData = ref([]);
const loading = ref(false);
const keyword = ref("");
const statusFilter = ref("");
const selectedRowKeys = ref([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true });

// 展开行相关
const expandConfig = { icon: true };
const expandedItems = ref({});
const expandLoading = ref({});

// 详情抽屉
const detailVisible = ref(false);
const detailLoading = ref(false);
const currentVendor = ref(null);
const recentItems = ref([]);

async function loadData() {
  loading.value = true;
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await http.post("/ops/vendors", params);
    const data = res?.data || res;
    tableData.value = data?.vendors || data?.rows || [];
    pagination.total = data?.totalCount ?? data?.total ?? 0;
  } catch (e) {
    console.error("[Vendor] load error:", e);
    Message.error("加载卖家列表失败");
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}

function handleStatusChange() {
  pagination.current = 1;
  loadData();
}

function handlePageChange(page) {
  pagination.current = page;
  loadData();
}

function handleSelectionChange(keys) {
  selectedRowKeys.value = keys;
}

// 查看详情
async function viewDetail(record) {
  currentVendor.value = record;
  detailVisible.value = true;
  detailLoading.value = true;

  try {
    const res = await http.get(`/ops/vendors/${record.id}/items`, { pageSize: 5 });
    const data = res?.data || res;
    recentItems.value = data?.items || data?.list || [];
  } catch (e) {
    console.error("[Vendor] load items error:", e);
    recentItems.value = [];
  } finally {
    detailLoading.value = false;
  }
}

// 切换卖家状态
async function toggleVendorStatus(record) {
  const isDisabling = record.status === 'ACTIVE';
  const actionText = isDisabling ? '禁用' : '启用';

  Modal.confirm({
    title: `确认${actionText}`,
    content: isDisabling 
      ? `⚠️ 确定要禁用卖家"${record.username}"吗？<br/><br/>
         <strong>注意：</strong>禁用后：<br/>
         • 该卖家将无法登录<br/>
         • 其所有在售商品将被自动下架<br/>
         • 无法发布新商品`
      : `确定要启用卖家"${record.username}"吗？启用后该卖家可正常使用。`,
    okText: `确认${actionText}`,
    cancelText: '取消',
    onOk: async () => {
      try {
        await http.post(`/ops/vendors/${record.id}/toggle-status`);
        Message.success(`已${actionText}卖家"${record.username}"${isDisabling ? '，其商品已自动下架' : ''}`);
        await loadData();
      } catch (e) {
        console.error("[Vendor] toggle status error:", e);
        Message.error(`${actionText}失败，请重试`);
      }
    }
  });
}

// 删除卖家
async function deleteVendor(record) {
  try {
    await http.delete(`/ops/vendors/${record.id}`);
    Message.success(`已删除卖家"${record.username}"及其所有相关数据`);
    
    // 如果当前页只有一条数据且不是第一页，则回到上一页
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current--;
    }
    
    await loadData();
  } catch (e) {
    console.error("[Vendor] delete error:", e);
    Message.error("删除失败，请重试");
  }
}

// 批量切换状态
async function batchToggleStatus() {
  if (selectedRowKeys.value.length === 0) return;

  Modal.confirm({
    title: '批量禁用确认',
    content: `⚠️ 确定要禁用选中的 ${selectedRowKeys.value.length} 个卖家吗？<br/><br/>
       <strong>影响：</strong><br/>
       • 这些卖家将无法登录<br/>
       • 其所有在售商品将被自动下架<br/>
       • 无法发布新商品`,
    okText: '确认禁用',
    cancelText: '取消',
    onOk: async () => {
      try {
        let successCount = 0;
        
        for (const id of selectedRowKeys.value) {
          try {
            await http.post(`/ops/vendors/${id}/toggle-status`);
            successCount++;
          } catch (e) {
            console.error(`批量禁用失败 (${id}):`, e);
          }
        }

        if (successCount > 0) {
          Message.success(`成功禁用 ${successCount} 个卖家，其商品已自动下架`);
        }
        
        selectedRowKeys.value = [];
        await loadData();
      } catch (e) {
        console.error('[BatchToggle] error:', e);
        Message.error('批量操作失败');
      }
    }
  });
}

// 批量删除
async function batchDelete() {
  if (selectedRowKeys.value.length === 0) return;

  try {
    let successCount = 0;
    
    for (const id of selectedRowKeys.value) {
      try {
        await http.delete(`/ops/vendors/${id}`);
        successCount++;
      } catch (e) {
        console.error(`批量删除失败 (${id}):`, e);
      }
    }

    if (successCount > 0) {
      Message.success(`成功删除 ${successCount} 个卖家及其所有数据`);
    }
    
    selectedRowKeys.value = [];
    
    // 检查是否需要调整页码
    if (tableData.value.length === successCount && pagination.current > 1) {
      pagination.current--;
    }
    
    await loadData();
  } catch (e) {
    console.error('[BatchDelete] error:', e);
    Message.error('批量删除失败');
  }
}

// 跳转到审核工作台（筛选该卖家的商品）
function goToReview(record) {
  router.push({
    path: '/ops/review',
    query: { tab: 'items', vendorId: record.id },
  });
  Message.info('正在跳转到审核工作台...');
}

// 跳转到单个商品审核
function goToItemReview(item) {
  router.push({
    path: '/ops/review',
    query: { tab: 'items', itemId: item.id },
  });
}

// 查看所有商品
function viewAllItems(record) {
  // 可以跳转到单独的商品管理页面或刷新展开行数据
  detailVisible.value = false;
  
  // 触发展开当前行（如果表格支持）
  Message.info('请在列表中点击展开按钮查看所有商品');
}

// 加载展开行的商品数据
async function loadExpandItems(recordId) {
  expandLoading.value[recordId] = true;
  
  try {
    const res = await http.get(`/ops/vendors/${recordId}/items`, { pageSize: 20 });
    const data = res?.data || res;
    expandedItems.value[recordId] = data?.items || data?.list || [];
  } catch (e) {
    console.error('[ExpandItems] load error:', e);
    expandedItems.value[recordId] = [];
  } finally {
    expandLoading.value[recordId] = false;
  }
}

// 辅助函数
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

function getItemStatusColor(status) {
  const colors = {
    ACTIVE: 'green',
    PENDING: 'orange',
    SOLD: 'blue',
    REJECTED: 'red',
    OFFLINE: 'gray',
  };
  return colors[status] || 'default';
}

function getItemStatusLabel(status) {
  const labels = {
    ACTIVE: '在售',
    PENDING: '待审核',
    SOLD: '已售',
    REJECTED: '已拒绝',
    OFFLINE: '已下架',
  };
  return labels[status] || status;
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.vendor-page {
  background: var(--ops-bg-white, #fff);
  border-radius: var(--ops-radius-lg, 12px);
  padding: 24px;

  &__header {
    margin-bottom: 20px;
    
    h2 { 
      margin: 0 0 6px; 
      font-size: 22px; 
      font-weight: 700; 
      color: var(--ops-text-1, #1d2129); 
    }
  }

  &__desc { 
    font-size: 13px; 
    color: var(--ops-text-3, #86909c); 
  }

  &__filter {
    margin-bottom: 20px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #fff7e8 0%, #fff 100%);
    border-radius: 10px;
    border: 1px solid #ffe8cc;

    .batch-actions {
      display: flex;
      align-items: center;
      gap: 12px;

      .selected-info {
        font-size: 14px;
        font-weight: 500;
        color: #FF7D00;
        padding: 4px 12px;
        background: rgba(255, 125, 0, 0.08);
        border-radius: 4px;
      }
    }
  }
}

.expand-content {
  padding: 16px 24px;
  background: #fafbfc;
  border-radius: 8px;
  margin: 8px 0;

  .expand-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
}

.vendor-detail {
  :deep(.arco-descriptions-item-label) {
    font-weight: 500;
    color: var(--color-text-2, #4e5969);
  }
  
  :deep(.arco-descriptions-item-value) {
    color: var(--color-text-1, #1d2129);
  }

  .detail-actions {
    margin-bottom: 16px;
    padding: 16px;
    background: linear-gradient(135deg, #fff7e8 0%, #fff 100%);
    border-radius: 8px;
    border: 1px solid #ffe8cc;
  }
}

@media screen and (max-width: 767px) {
  .vendor-page {
    padding: 16px;

    &__header h2 {
      font-size: 18px;
    }

    &__filter {
      flex-direction: column;
      
      .arco-col {
        width: 100% !important;
        margin-bottom: 8px;
      }

      .batch-actions {
        flex-wrap: wrap;
      }
    }
  }

  .expand-content .expand-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
