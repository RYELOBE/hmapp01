<template>
  <div class="category-management">
    <div class="header">
      <h2>分类管理</h2>
      <div class="header-actions">
        <a-button type="primary" @click="openAddModal">
          <template #icon><icon-plus /></template>
          新增分类
        </a-button>
        <a-button @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </div>
    </div>

    <a-table
      :columns="columns"
      :data="categoryList"
      :pagination="pagination"
      @page-change="handlePageChange"
      :loading="loading"
      row-key="id"
    >
      <template #icon="{ record }">
        <span class="category-icon">{{ record.icon }}</span>
      </template>
      <template #status="{ record }">
        <a-switch
          :model-value="record.status === 1"
          @change="toggleStatus(record)"
          :checked-value="1"
          :unchecked-value="0"
        />
      </template>
      <template #productCount="{ record }">
        <a-tag color="arcoblue">{{ record.productCount }} 件</a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-link @click="openEditModal(record)">编辑</a-link>
          <a-link status="danger" @click="confirmDelete(record)">删除</a-link>
        </a-space>
      </template>
    </a-table>

    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      @before-ok="handleSubmit"
      @cancel="handleCancel"
      :width="480"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="分类名称" field="name">
          <a-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            maxlength="20"
            show-word-limit
          />
        </a-form-item>
        <a-form-item label="分类图标" field="icon">
          <div class="icon-select">
            <div
              v-for="icon in iconOptions"
              :key="icon"
              class="icon-option"
              :class="{ active: formData.icon === icon }"
              @click="formData.icon = icon"
            >
              {{ icon }}
            </div>
          </div>
        </a-form-item>
        <a-form-item label="排序权重" field="sortOrder">
          <a-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="9999"
            placeholder="数值越大越靠前"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="状态" field="status">
          <a-switch
            v-model="formData.status"
            :checked-value="1"
            :unchecked-value="0"
          >
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </a-switch>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="deleteModalVisible"
      title="确认删除"
      @before-ok="handleDelete"
      @cancel="deleteModalVisible = false"
      :width="400"
    >
      <div class="delete-confirm">
        <icon-exclamation-circle-fill class="warning-icon" />
        <p>确定要删除分类 <strong>{{ currentCategory?.name }}</strong> 吗？</p>
        <p class="warning-text">删除后，该分类下的商品将变为"未分类"状态</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'

const loading = ref(false)
const modalVisible = ref(false)
const deleteModalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentCategory = ref(null)

const columns = [
  { title: '分类ID', dataIndex: 'id', width: 100, align: 'center' },
  { title: '分类名称', dataIndex: 'name', minWidth: 150 },
  { title: '分类图标', dataIndex: 'icon', width: 120, align: 'center' },
  { title: '排序权重', dataIndex: 'sortOrder', width: 100, align: 'center', sortable: true },
  { title: '商品数量', dataIndex: 'productCount', width: 120, align: 'center', sortable: true },
  { title: '状态', dataIndex: 'status', width: 100, align: 'center' },
  { title: '创建时间', dataIndex: 'createdAt', width: 180, align: 'center' },
  { title: '操作', slotName: 'actions', width: 150, align: 'center', fixed: 'right' }
]

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
  showTotal: true,
  showPageSize: true,
  showJumper: true
})

const categoryList = ref([])

const iconOptions = [
  '📚', '💻', '🏠', '👗', '⚽', '🎫', '🔌', '📦',
  '🎮', '📱', '💄', '👜', '👟', '🎧', '⌚', '📷',
  '🛋️', '🪑', '💡', '🔧', '📖', '🎓', '✏️', '🧸'
]

const formData = reactive({
  name: '',
  icon: '📦',
  sortOrder: 0,
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称' },
    { minLength: 1, maxLength: 20, message: '分类名称长度为1-20个字符' }
  ],
  icon: [{ required: true, message: '请选择分类图标' }],
  sortOrder: [{ required: true, message: '请输入排序权重' }]
}

const initialCategories = [
  { id: 1, name: '书籍教材', icon: '📚', sortOrder: 100, status: 1, productCount: 128, createdAt: '2024-01-15 10:30:00' },
  { id: 2, name: '电子产品', icon: '💻', sortOrder: 95, status: 1, productCount: 86, createdAt: '2024-01-15 10:35:00' },
  { id: 3, name: '生活用品', icon: '🏠', sortOrder: 90, status: 1, productCount: 156, createdAt: '2024-01-15 10:40:00' },
  { id: 4, name: '服饰鞋包', icon: '👗', sortOrder: 85, status: 1, productCount: 203, createdAt: '2024-01-15 10:45:00' },
  { id: 5, name: '运动健身', icon: '⚽', sortOrder: 80, status: 1, productCount: 67, createdAt: '2024-01-15 10:50:00' },
  { id: 6, name: '票券卡类', icon: '🎫', sortOrder: 75, status: 1, productCount: 42, createdAt: '2024-01-15 10:55:00' },
  { id: 7, name: '数码配件', icon: '🔌', sortOrder: 70, status: 1, productCount: 95, createdAt: '2024-01-15 11:00:00' },
  { id: 8, name: '其他分类', icon: '📦', sortOrder: 10, status: 1, productCount: 34, createdAt: '2024-01-15 11:05:00' }
]

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    const sortedData = [...initialCategories].sort((a, b) => b.sortOrder - a.sortOrder)
    categoryList.value = sortedData
    pagination.total = sortedData.length
    loading.value = false
    Message.success('数据刷新成功')
  }, 500)
}

const openAddModal = () => {
  isEdit.value = false
  Object.assign(formData, {
    name: '',
    icon: '📦',
    sortOrder: 50,
    status: 1
  })
  modalVisible.value = true
}

const openEditModal = (record) => {
  isEdit.value = true
  currentCategory.value = record
  Object.assign(formData, {
    name: record.name,
    icon: record.icon,
    sortOrder: record.sortOrder,
    status: record.status
  })
  modalVisible.value = true
}

const handleSubmit = async (done) => {
  const validateRes = await formRef.value.validate()
  if (validateRes) {
    done(false)
    return
  }

  if (isEdit.value) {
    const index = categoryList.value.findIndex(item => item.id === currentCategory.value.id)
    if (index !== -1) {
      categoryList.value[index] = {
        ...categoryList.value[index],
        name: formData.name,
        icon: formData.icon,
        sortOrder: formData.sortOrder,
        status: formData.status
      }
      categoryList.value = [...categoryList.value].sort((a, b) => b.sortOrder - a.sortOrder)
    }
    Message.success('分类更新成功')
  } else {
    const newId = Math.max(...categoryList.value.map(item => item.id)) + 1
    const now = new Date().format('yyyy-MM-dd hh:mm:ss')
    categoryList.value.push({
      id: newId,
      name: formData.name,
      icon: formData.icon,
      sortOrder: formData.sortOrder,
      status: formData.status,
      productCount: 0,
      createdAt: now
    })
    categoryList.value = [...categoryList.value].sort((a, b) => b.sortOrder - a.sortOrder)
    Message.success('分类添加成功')
  }

  modalVisible.value = false
  done(true)
}

const handleCancel = () => {
  modalVisible.value = false
}

const toggleStatus = (record) => {
  const newStatus = record.status === 1 ? 0 : 1
  const index = categoryList.value.findIndex(item => item.id === record.id)
  if (index !== -1) {
    categoryList.value[index].status = newStatus
    categoryList.value = [...categoryList.value]
  }
  Message.success(`分类${newStatus === 1 ? '启用' : '禁用'}成功`)
}

const confirmDelete = (record) => {
  currentCategory.value = record
  deleteModalVisible.value = true
}

const handleDelete = (done) => {
  const index = categoryList.value.findIndex(item => item.id === currentCategory.value.id)
  if (index !== -1) {
    categoryList.value.splice(index, 1)
    categoryList.value = [...categoryList.value]
  }
  deleteModalVisible.value = false
  Message.success('分类删除成功')
  done(true)
}

const handlePageChange = (page) => {
  pagination.current = page
}

Date.prototype.format = function(fmt) {
  const o = {
    'yyyy': this.getFullYear(),
    'MM': String(this.getMonth() + 1).padStart(2, '0'),
    'dd': String(this.getDate()).padStart(2, '0'),
    'hh': String(this.getHours()).padStart(2, '0'),
    'mm': String(this.getMinutes()).padStart(2, '0'),
    'ss': String(this.getSeconds()).padStart(2, '0')
  }
  for (const k in o) {
    fmt = fmt.replace(k, o[k])
  }
  return fmt
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-management {
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.category-icon {
  font-size: 24px;
}

.icon-select {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.icon-option {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  border: 1px solid #e5e6eb;
}

.icon-option:hover {
  border-color: #165dff;
  background: #f2f3f5;
}

.icon-option.active {
  border-color: #165dff;
  background: #e8f0ff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2);
}

.delete-confirm {
  text-align: center;
  padding: 16px 0;
}

.warning-icon {
  font-size: 48px;
  color: #ff7d00;
  margin-bottom: 16px;
}

.delete-confirm p {
  margin: 8px 0;
  color: #1d2129;
}

.warning-text {
  color: #86909c !important;
  font-size: 12px;
}
</style>
