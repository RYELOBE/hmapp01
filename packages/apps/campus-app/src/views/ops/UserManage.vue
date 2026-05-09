<template>
  <OpsUnifiedTable
    title="用户管理"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :pagination="pagination"
    row-key="id"
    :stripe="true"
    @page-change="handlePageChange"
  >
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索用户名/昵称/手机号"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
      <a-select v-model="roleFilter" placeholder="全部角色" style="width: 140px" allow-clear @change="handleSearch">
        <a-option value="BUYER">买家</a-option>
        <a-option value="SELLER">卖家</a-option>
        <a-option value="OPS">运营</a-option>
      </a-select>
      <a-select v-model="statusFilter" placeholder="全部状态" style="width: 120px" allow-clear @change="handleSearch">
        <a-option value="ACTIVE">正常</a-option>
        <a-option value="DISABLED">已禁用</a-option>
      </a-select>
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <template #avatar="{ record }">
      <a-avatar :size="36" :style="{ backgroundColor: getAvatarColor(record.username) }">
        {{ (record.username || '用')[0]?.toUpperCase() }}
      </a-avatar>
    </template>

    <template #username="{ record }">
      <a-space>
        <span style="font-weight:500">{{ record.username }}</span>
        <a-tag v-if="record.isVip" color="gold" size="small">VIP</a-tag>
      </a-space>
    </template>

    <template #roles="{ record }">
      <a-space wrap>
        <a-tag v-if="hasRole(record.roles, 'BUYER')" color="blue" size="small">买家</a-tag>
        <a-tag v-if="hasRole(record.roles, 'SELLER')" color="green" size="small">卖家</a-tag>
        <a-tag v-if="hasRole(record.roles, 'OPS_ADMIN')" color="purple" size="small">运营</a-tag>
        <a-tag v-if="!record.roles || record.roles.length === 0" color="gray" size="small">未知</a-tag>
      </a-space>
    </template>

    <template #createdAt="{ record }">
      {{ formatDate(record.createdAt) }}
    </template>

    <template #status="{ record }">
      <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'" size="small">
        {{ record.status === 'ACTIVE' ? '正常' : '已禁用' }}
      </a-tag>
    </template>

    <template #operations="{ record }">
      <a-space>
        <a-button type="text" size="small" @click="viewDetail(record)">查看详情</a-button>
        <a-button
          type="text"
          :status="record.status === 'DISABLED' ? 'success' : 'danger'"
          size="small"
          @click="toggleUserStatus(record)"
        >
          {{ record.status === 'DISABLED' ? '启用' : '禁用' }}
        </a-button>
      </a-space>
    </template>
  </OpsUnifiedTable>

  <a-drawer
    v-model:visible="detailVisible"
    :title="`用户详情 - ${currentUser?.username || ''}`"
    :width="560"
    placement="right"
    unmount-on-close
  >
    <a-descriptions v-if="currentUser" :column="2" bordered size="medium">
      <a-descriptions-item label="用户ID">{{ currentUser.id || '-' }}</a-descriptions-item>
      <a-descriptions-item label="用户名">{{ currentUser.username || '-' }}</a-descriptions-item>
      <a-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</a-descriptions-item>
      <a-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</a-descriptions-item>
      <a-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</a-descriptions-item>
      <a-descriptions-item label="角色" :span="2">
        <a-space wrap>
          <a-tag v-if="hasRole(currentUser.roles, 'BUYER')" color="blue" size="small">买家</a-tag>
          <a-tag v-if="hasRole(currentUser.roles, 'SELLER')" color="green" size="small">卖家</a-tag>
          <a-tag v-if="hasRole(currentUser.roles, 'OPS_ADMIN')" color="purple" size="small">运营</a-tag>
        </a-space>
      </a-descriptions-item>
      <a-descriptions-item label="账号状态" :span="2">
        <a-tag :color="currentUser?.status === 'ACTIVE' ? 'green' : 'red'" size="small">
          {{ currentUser?.status === 'ACTIVE' ? '正常' : '已禁用' }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="注册时间" :span="2">{{ formatDate(currentUser?.createdAt) }}</a-descriptions-item>
    </a-descriptions>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import OpsUnifiedTable from '@/components/ops/OpsUnifiedTable.vue'
import { getUsers, updateUserStatus, getUserDetail } from '@/services/ops/index'

const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const tableData = ref([])
const detailVisible = ref(false)
const currentUser = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 15, 20, 50],
})

const tableColumns = computed(() => [
  { title: '头像', dataIndex: 'avatar', width: 80, align: 'center', slotName: 'avatar' },
  { title: '用户名', dataIndex: 'username', width: 130, slotName: 'username' },
  { title: '昵称', dataIndex: 'nickname', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 130 },
  { title: '角色', dataIndex: 'roles', width: 180, slotName: 'roles' },
  { title: '注册时间', dataIndex: 'createdAt', width: 160, slotName: 'createdAt' },
  { title: '状态', dataIndex: 'status', width: 90, align: 'center', slotName: 'status' },
  { title: '操作', width: 160, fixed: 'right', align: 'center', slotName: 'operations' }
])

function getAvatarColor(username) {
  const colors = ['#165DFF', '#00B42A', '#FF7D00', '#F53F3F', '#722ED1', '#14C9C9']
  const index = username ? username.charCodeAt(0) % colors.length : 0
  return colors[index]
}

function hasRole(roles, role) {
  if (!roles) return false
  if (Array.isArray(roles)) return roles.includes(role)
  if (typeof roles === 'string') return roles.includes(role)
  return false
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined,
      status: statusFilter.value || undefined,
    }
    const res = await getUsers(params)
    tableData.value = res?.users || res?.rows || []
    pagination.total = res?.totalCount ?? res?.total ?? 0
  } catch (e) {
    console.error('[UserManage] load error:', e)
    Message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(record) {
  try {
    const res = await getUserDetail(record.id)
    currentUser.value = res?.user || res || record
    detailVisible.value = true
  } catch (e) {
    currentUser.value = record
    detailVisible.value = true
  }
}

function toggleUserStatus(record) {
  const newStatus = record.status === 'DISABLED' ? 'ACTIVE' : 'DISABLED'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'
  updateUserStatus(record.id, newStatus)
    .then(() => {
      Message.success(`用户已${action}`)
      loadData()
    })
    .catch(() => {
      Message.error(`${action}用户失败`)
    })
}

function handleSearch() { pagination.current = 1; loadData() }

function handleReset() {
  keyword.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  handleSearch()
}

function handlePageChange(page) { pagination.current = page; loadData() }

onMounted(() => { loadData() })
</script>
