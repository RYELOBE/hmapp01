<template>
  <OpsUnifiedTable
    title="圈子审核"
    :data="[]"
    :columns="[]"
    :pagination="false"
  >
    <!-- Tabs 插槽 -->
    <template #tabs>
      <a-tabs v-model:active-key="activeTab" type="line" @change="handleTabChange">
        <a-tab-pane key="posts" title="待审核"></a-tab-pane>
        <a-tab-pane key="comments" title="已审核"></a-tab-pane>
      </a-tabs>
    </template>

    <!-- 搜索栏 -->
    <template #actions>
      <a-input-search
        v-model="keyword"
        placeholder="搜索标题/内容/作者"
        style="width: 260px"
        search-button
        @search="handleSearch"
        allow-clear
      />
    </template>

    <template #extra>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
    </template>

    <!-- 审核列表内容 -->
    <div class="review-content">
      <a-spin :loading="loading">
        <!-- 帖子列表 -->
        <template v-if="activeTab === 'posts'">
          <div v-if="tableData.length > 0" class="post-list">
            <div v-for="item in tableData" :key="item.id" class="post-card">
              <div v-if="getFirstImage(item)" class="post-cover">
                <a-image :src="getFirstImage(item)" width="80" height="80" fit="cover" style="border-radius:8px" />
              </div>
              <div class="post-body">
                <div class="post-title">{{ item.title }}</div>
                <div class="post-meta">
                  <a-avatar :size="20" :style="{ backgroundColor: '#165DFF' }">{{ (item.userName || item.authorName || '用')[0] }}</a-avatar>
                  <span class="author">{{ item.userName || item.authorName }}</span>
                  <span class="time">{{ formatDate(item.createdAt) }}</span>
                </div>
                <p class="post-content">{{ truncate(item.content, 150) }}</p>
                <div class="post-tags" v-if="parseTags(item.tags).length">
                  <a-tag v-for="tag in parseTags(item.tags)" :key="tag" size="small" color="arcoblue">{{ tag }}</a-tag>
                </div>
              </div>
              <div class="post-actions">
                <a-button type="primary" status="success" size="small" @click="approvePost(item)">通过</a-button>
                <a-button type="primary" status="danger" size="small" @click="openRejectModal(item)">拒绝</a-button>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无待审核帖子" style="padding:40px 0" />
        </template>

        <!-- 评论列表 -->
        <template v-if="activeTab === 'comments'">
          <div v-if="tableData.length > 0" class="comment-list">
            <div v-for="item in tableData" :key="item.id" class="comment-card">
              <div class="comment-body">
                <div class="comment-meta">
                  <a-avatar :size="20" :style="{ backgroundColor: '#722ED1' }">{{ (item.userName || item.authorName || '用')[0] }}</a-avatar>
                  <span class="author">{{ item.userName || item.authorName }}</span>
                  <span class="time">{{ formatDate(item.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ item.content }}</p>
                <div class="post-ref">所属帖子：{{ item.postTitle || `帖子 #${item.postId}` }}</div>
              </div>
              <div class="post-actions">
                <a-button type="primary" status="success" size="small" @click="approveComment(item)">通过</a-button>
                <a-button type="primary" status="danger" size="small" @click="deleteComment(item)">删除</a-button>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无待审核评论" style="padding:40px 0" />
        </template>
      </a-spin>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          show-total
          show-page-size
          :page-size-options="[10, 15, 20, 50]"
          size="small"
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </OpsUnifiedTable>

  <!-- 拒绝弹窗 -->
  <a-modal v-model:visible="rejectModalVisible" title="拒绝原因" @ok="doReject" :ok-loading="rejecting">
    <a-textarea v-model="rejectReason" placeholder="请输入拒绝原因" :max-length="200" show-word-limit :auto-size="{ minRows: 3 }" />
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { opsHttp as http } from '../../../services/http'
import OpsUnifiedTable from '../../../components/ops/OpsUnifiedTable.vue'

const loading = ref(false)
const activeTab = ref('posts')
const keyword = ref('')
const tableData = ref([])
const rejectModalVisible = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const currentRejectItem = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
})

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'posts') {
      const res = await http.post('/ops/circle/pending', {
        keyword: keyword.value || undefined,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      })
      const data = res?.data?.data ?? res?.data ?? res
      tableData.value = data?.items || data?.posts || data?.rows || []
      pagination.total = data?.totalCount ?? data?.total ?? tableData.value.length
    } else {
      const res = await http.post('/circle/comments/pending', {
        keyword: keyword.value || undefined,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      })
      const data = res?.data?.data ?? res?.data ?? res
      tableData.value = data?.comments || data?.rows || []
      pagination.total = data?.totalCount ?? data?.total ?? tableData.value.length
    }
  } catch (e) {
    console.error('[CircleReview] load error:', e)
    Message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  tableData.value = []
  pagination.current = 1
  loadData()
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  handleSearch()
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

function handlePageSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadData()
}

async function approvePost(item) {
  try {
    await http.post(`/ops/circle/${item.id}/approve`)
    Message.success('帖子已通过审核')
    loadData()
  } catch (e) { Message.error('操作失败') }
}

function openRejectModal(item) {
  currentRejectItem.value = item
  rejectReason.value = ''
  rejectModalVisible.value = true
}

async function doReject() {
  if (!rejectReason.value.trim()) { Message.warning('请输入拒绝原因'); return }
  rejecting.value = true
  try {
    await http.post(`/ops/circle/${currentRejectItem.value.id}/reject`, { reason: rejectReason.value })
    Message.success('帖子已拒绝')
    rejectModalVisible.value = false
    loadData()
  } catch (e) { Message.error('操作失败') } finally { rejecting.value = false }
}

async function approveComment(item) {
  try {
    await http.post(`/circle/comments/${item.id}/approve`)
    Message.success('评论已通过')
    loadData()
  } catch (e) { Message.error('操作失败') }
}

async function deleteComment(item) {
  try {
    await http.post(`/circle/comments/${item.id}/reject`)
    Message.success('评论已删除')
    loadData()
  } catch (e) { Message.error('操作失败') }
}

function parseTags(rawTags) {
  if (!rawTags) return []
  if (Array.isArray(rawTags)) return rawTags
  return String(rawTags).split(',').map(t => t.trim()).filter(Boolean)
}

function getFirstImage(record) {
  const imgs = record.images || record.imageUrls || ''
  if (!imgs) return ''
  if (typeof imgs === 'string') {
    try { const arr = JSON.parse(imgs); return Array.isArray(arr) ? arr[0] : imgs } catch { return imgs }
  }
  return Array.isArray(imgs) ? imgs[0] : ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>
.review-content {
  padding: 16px 20px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  margin-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.post-list, .comment-list { display: flex; flex-direction: column; gap: 12px; }

.post-card, .comment-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e6eb;
  transition: box-shadow .2s;

  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,.08); }

  .post-cover { flex-shrink: 0; }

  .post-body, .comment-body {
    flex: 1;
    min-width: 0;

    .post-title {
      font-size: 15px;
      font-weight: 600;
      color: #1d2129;
      margin-bottom: 8px;
    }

    .post-meta, .comment-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #86909c;
      margin-bottom: 8px;

      .author { font-weight: 500; color: #4e5969; }
      .time { margin-left: auto; }
    }

    .post-content, .comment-content {
      font-size: 13px;
      color: #4e5969;
      margin: 0 0 8px;
      line-height: 1.6;
    }

    .post-tags { display: flex; flex-wrap: wrap; gap: 6px; }

    .post-ref {
      font-size: 12px;
      color: #86909c;
      margin-top: 4px;
    }
  }

  .post-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex-shrink: 0;
  }
}
</style>
