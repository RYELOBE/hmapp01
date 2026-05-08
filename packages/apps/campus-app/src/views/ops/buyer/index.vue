<template>
  <div class="buyer-page">
    <div class="buyer-page__header">
      <h2>买家管理</h2>
      <span class="buyer-page__desc">管理所有买家用户及其订单统计</span>
    </div>

    <!-- 筛选工具栏 -->
    <div class="buyer-page__filter">
      <a-row :gutter="16" align="center">
        <a-col :flex="'280px'">
          <a-input-search
            v-model="keyword"
            placeholder="搜索买家用户名"
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
            <a-popconfirm content="确定要删除选中的买家吗？此操作不可恢复！" @ok="batchDelete">
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
      :columns="BUYER_COLUMNS"
      row-key="id"
      :row-selection="{ type: 'checkbox', showCheckedAll: true, selectedRowKeys, onSelectionChange: handleSelectionChange }"
      :expandable="expandConfig"
      @page-change="handlePageChange"
    >
      <template #username="{ record }">
        <a-space>
          <a-avatar :size="32">{{ record.username?.[0] || '买' }}</a-avatar>
          <div>
            <div style="font-weight: 500; color: var(--color-text-1);">{{ record.username }}</div>
            <div style="font-size: 12px; color: var(--color-text-4);">ID: {{ record.id }}</div>
          </div>
        </a-space>
      </template>

      <template #totalSpent="{ record }">
        <span style="color: #f53f3f; font-weight: 600;">¥{{ (record.totalSpent || 0).toFixed(2) }}</span>
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
            @click="toggleUserStatus(record)"
          >
            <icon-lock v-if="record.status === 'ACTIVE'" />
            <icon-unlock v-else />
            {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
          </a-button>
          <a-popconfirm content="确定要删除该买家吗？此操作不可恢复！" @ok="deleteUser(record)">
            <a-button type="text" size="small" status="danger">
              <icon-delete /> 删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>

      <!-- 展开行：显示订单列表 -->
      template #expand-row="{ record }">
        <div class="expand-content">
          <h4 style="margin: 0 0 12px; font-size: 14px; color: var(--color-text-1);">
            <icon-file /> 最近订单记录
          </h4>
          <a-table
            :data="expandedOrders[record.id] || []"
            :loading="expandLoading[record.id]"
            :pagination="false"
            size="small"
            :bordered="false"
          >
            <template #columns>
              <a-table-column title="订单号" data-index="orderNo" :width="150" />
              <a-table-column title="商品名称" data-index="itemName" :ellipsis="true" :tooltip="true" />
              <a-table-column title="金额" data-index="amount" :width="100">
                <template #cell="{ record }">
                  <span style="color: #f53f3f; font-weight: 500;">¥{{ (record.amount || 0).toFixed(2) }}</span>
                </template>
              </a-table-column>
              <a-table-column title="状态" data-index="status" :width="100">
                <template #cell="{ record }">
                  <a-tag :color="getOrderStatusColor(record.status)" size="small">
                    {{ getOrderStatusLabel(record.status) }}
                  </a-tag>
                </template>
              </a-table-column>
              <a-table-column title="下单时间" data-index="createdAt" :width="160" />
            </template>
          </a-table>
          <a-empty v-if="!expandLoading[record.id] && (!expandedOrders[record.id] || expandedOrders[record.id].length === 0)" description="暂无订单记录" />
        </div>
      </template>
    </a-table>

    <!-- 用户详情抽屉 -->
    <a-drawer
      v-model:visible="detailVisible"
      :title="`买家详情 - ${currentBuyer?.username}`"
      :width="600"
      :footer="false"
    >
      <a-spin :loading="detailLoading">
        <div v-if="currentBuyer" class="buyer-detail">
          <a-descriptions :column="2" bordered size="medium">
            <a-descriptions-item label="用户ID">{{ currentBuyer.id }}</a-descriptions-item>
            <a-descriptions-item label="用户名">{{ currentBuyer.username }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ currentBuyer.email || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ currentBuyer.phone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="currentBuyer.status === 'ACTIVE' ? 'green' : 'red'" size="small">
                {{ currentBuyer.status === 'ACTIVE' ? '正常' : '已禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="注册时间">{{ formatDateTime(currentBuyer.createdAt) }}</a-descriptions-item>
            <a-descriptions-item label="总订单数">{{ currentBuyer.totalOrders || 0 }}</a-descriptions-item>
            <a-descriptions-item label="已完成订单">{{ currentBuyer.completedOrders || 0 }}</a-descriptions-item>
            <a-descriptions-item label="总消费金额" :span="2">
              <span style="color: #f53f3f; font-weight: 700; font-size: 18px;">
                ¥{{ (currentBuyer.totalSpent || 0).toFixed(2) }}
              </span>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <h4 style="margin-bottom: 12px;">最近5笔订单</h4>
          <a-list :data="recentOrders" :bordered="false">
            <template #item="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.itemName || '商品'">
                  <template #avatar>
                    <a-avatar>{{ item.orderNo?.slice(-4) }}</a-avatar>
                  </template>
                  <template #description>
                    <a-space :size="8">
                      <span>{{ item.orderNo }}</span>
                      <a-tag :color="getOrderStatusColor(item.status)" size="small">
                        {{ getOrderStatusLabel(item.status) }}
                      </a-tag>
                      <span style="color: #f53f3f;">¥{{ (item.amount || 0).toFixed(2) }}</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <span style="font-size: 12px; color: var(--color-text-4);">{{ formatDateTime(item.createdAt) }}</span>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-if="!recentOrders || recentOrders.length === 0" description="暂无订单记录" />
        </div>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconEye,
  IconLock,
  IconUnlock,
  IconDelete,
  IconFile,
} from "@arco-design/web-vue/es/icon";
import { opsHttp as http } from "../../../services/http";
import { BUYER_COLUMNS } from "./const";

const tableData = ref([]);
const loading = ref(false);
const keyword = ref("");
const statusFilter = ref("");
const selectedRowKeys = ref([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: true });

// 展开行相关
const expandConfig = { icon: true };
const expandedOrders = ref({});
const expandLoading = ref({});

// 详情抽屉
const detailVisible = ref(false);
const detailLoading = ref(false);
const currentBuyer = ref(null);
const recentOrders = ref([]);

async function loadData() {
  loading.value = true;
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await http.post("/ops/buyers", params);
    const data = res?.data || res;
    tableData.value = data?.buyers || data?.rows || [];
    pagination.total = data?.totalCount ?? data?.total ?? 0;
  } catch (e) {
    console.error("[Buyer] load error:", e);
    Message.error("加载买家列表失败");
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
  currentBuyer.value = record;
  detailVisible.value = true;
  detailLoading.value = true;

  try {
    const res = await http.get(`/ops/buyers/${record.id}/orders`, { pageSize: 5 });
    const data = res?.data || res;
    recentOrders.value = data?.orders || data?.list || [];
  } catch (e) {
    console.error("[Buyer] load orders error:", e);
    recentOrders.value = [];
  } finally {
    detailLoading.value = false;
  }
}

// 切换用户状态
async function toggleUserStatus(record) {
  const isDisabling = record.status === 'ACTIVE';
  const actionText = isDisabling ? '禁用' : '启用';

  Modal.confirm({
    title: `确认${actionText}`,
    content: isDisabling 
      ? `确定要禁用买家"${record.username}"吗？禁用后该用户将无法登录和下单。`
      : `确定要启用买家"${record.username}"吗？`,
    okText: `确认${actionText}`,
    cancelText: '取消',
    onOk: async () => {
      try {
        await http.post(`/ops/buyers/${record.id}/toggle-status`);
        Message.success(`已${actionText}买家"${record.username}"`);
        await loadData();
      } catch (e) {
        console.error("[Buyer] toggle status error:", e);
        Message.error(`${actionText}失败，请重试`);
      }
    }
  });
}

// 删除用户
async function deleteUser(record) {
  try {
    await http.delete(`/ops/buyers/${record.id}`);
    Message.success(`已删除买家"${record.username}"`);
    
    // 如果当前页只有一条数据且不是第一页，则回到上一页
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current--;
    }
    
    await loadData();
  } catch (e) {
    console.error("[Buyer] delete error:", e);
    Message.error("删除失败，请重试");
  }
}

// 批量切换状态
async function batchToggleStatus() {
  if (selectedRowKeys.value.length === 0) return;

  Modal.confirm({
    title: '批量禁用确认',
    content: `确定要禁用选中的 ${selectedRowKeys.value.length} 个买家吗？禁用后这些用户将无法登录和下单。`,
    okText: '确认禁用',
    cancelText: '取消',
    onOk: async () => {
      try {
        let successCount = 0;
        
        for (const id of selectedRowKeys.value) {
          try {
            await http.post(`/ops/buyers/${id}/toggle-status`);
            successCount++;
          } catch (e) {
            console.error(`批量禁用失败 (${id}):`, e);
          }
        }

        if (successCount > 0) {
          Message.success(`成功禁用 ${successCount} 个买家`);
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
        await http.delete(`/ops/buyers/${id}`);
        successCount++;
      } catch (e) {
        console.error(`批量删除失败 (${id}):`, e);
      }
    }

    if (successCount > 0) {
      Message.success(`成功删除 ${successCount} 个买家`);
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

// 加载展开行的订单数据
async function loadExpandOrders(recordId) {
  expandLoading.value[recordId] = true;
  
  try {
    const res = await http.get(`/ops/buyers/${recordId}/orders`, { pageSize: 10 });
    const data = res?.data || res;
    expandedOrders.value[recordId] = data?.orders || data?.list || [];
  } catch (e) {
    console.error('[ExpandOrders] load error:', e);
    expandedOrders.value[recordId] = [];
  } finally {
    expandLoading.value[recordId] = false;
  }
}

// 辅助函数
function getOrderStatusColor(status) {
  const colors = {
    PENDING_PAYMENT: 'orange',
    PAID: 'blue',
    SHIPPED: 'cyan',
    COMPLETED: 'green',
    CANCELLED: 'gray',
    REFUND_PENDING: 'red',
  };
  return colors[status] || 'default';
}

function getOrderStatusLabel(status) {
  const labels = {
    PENDING_PAYMENT: '待支付',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUND_PENDING: '退款中',
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
.buyer-page {
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
    background: linear-gradient(135deg, #f0f5ff 0%, #fff 100%);
    border-radius: 10px;
    border: 1px solid #e8f3ff;

    .batch-actions {
      display: flex;
      align-items: center;
      gap: 12px;

      .selected-info {
        font-size: 14px;
        font-weight: 500;
        color: #165DFF;
        padding: 4px 12px;
        background: rgba(22, 93, 255, 0.08);
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
}

.buyer-detail {
  :deep(.arco-descriptions-item-label) {
    font-weight: 500;
    color: var(--color-text-2, #4e5969);
  }
  
  :deep(.arco-descriptions-item-value) {
    color: var(--color-text-1, #1d2129);
  }
}

@media screen and (max-width: 767px) {
  .buyer-page {
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
    }
  }
}
</style>
