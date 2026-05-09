import { ref } from 'vue'
import { getDictOptions } from './ops/index'

// 缓存枚举数据
const enums = ref({})
const loading = ref(false)

/**
 * 获取所有枚举数据
 */
export async function loadEnums() {
  if (loading.value) return enums.value

  if (Object.keys(enums.value).length > 0) {
    return enums.value
  }

  loading.value = true
  try {
    const response = await getDictOptions()
    console.log('[Enums] Raw response:', response)

    let data = response
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      if ('data' in data && data.data !== undefined) {
        data = data.data
        if (data && typeof data === 'object' && !Array.isArray(data) && 'data' in data && data.data !== undefined) {
          data = data.data
        }
      }
    }

    console.log('[Enums] Parsed data:', data)
    enums.value = (data && typeof data === 'object') ? data : {}
    return enums.value
  } catch (error) {
    console.error('[Enums] Failed to load enums:', error)
    return {}
  } finally {
    loading.value = false
  }
}

/**
 * 获取用户状态选项
 */
export async function getUserStatuses() {
  const data = await loadEnums()
  return data.userStatuses || []
}

/**
 * 获取用户角色选项
 */
export async function getUserRoles() {
  const data = await loadEnums()
  return data.userRoles || []
}

/**
 * 获取审核状态选项
 */
export async function getReviewStatuses() {
  const data = await loadEnums()
  return data.reviewStatuses || []
}

/**
 * 获取商品状态选项
 */
export async function getItemStatuses() {
  const data = await loadEnums()
  return data.itemStatuses || []
}

/**
 * 获取订单状态选项
 */
export async function getOrderStatuses() {
  const data = await loadEnums()
  return data.orderStatuses || []
}

/**
 * 获取评价审核状态选项（评价管理模块）
 */
export async function getReviewModerationStatuses() {
  const data = await loadEnums()
  return data.reviewModerationStatuses || []
}

/**
 * 获取评价类型选项
 */
export async function getReviewTypes() {
  const data = await loadEnums()
  return data.reviewTypes || []
}

/**
 * 获取成色选项
 */
export async function getConditions() {
  const data = await loadEnums()
  return data.conditions || []
}

/**
 * 获取权限列表
 */
export async function getPermissions() {
  const data = await loadEnums()
  return data.permissions || []
}

/**
 * 根据值获取状态标签
 */
export function getStatusLabel(status, statusList = []) {
  const item = statusList.find(item => item.value === status)
  return item ? item.label : status
}

/**
 * 根据值获取状态颜色
 */
export function getStatusColor(status, statusList = []) {
  const item = statusList.find(item => item.value === status)
  return item ? item.color : 'gray'
}

/**
 * 根据值获取角色标签
 */
export function getRoleLabel(role, roleList = []) {
  const item = roleList.find(item => item.value === role)
  return item ? item.label : role
}

/**
 * 根据值获取角色颜色
 */
export function getRoleColor(role, roleList = []) {
  const item = roleList.find(item => item.value === role)
  return item ? item.color : 'gray'
}

/**
 * 格式化筛选选项（添加空选项）
 */
export function formatFilterOptions(options, emptyLabel = '全部') {
  const hasEmpty = options.some(item => !item.value)
  if (hasEmpty) return options
  
  return [
    { value: '', label: emptyLabel, color: '' },
    ...options.filter(item => item.value)
  ]
}

/**
 * 权限分组
 */
export function groupPermissions(permissions) {
  const groups = {
    item: { key: 'item', label: '📦 商品管理', permissions: [] },
    order: { key: 'order', label: '🛒 订单管理', permissions: [] },
    user: { key: 'user', label: '👤 用户管理', permissions: [] },
    ops: { key: 'ops', label: '⚙️ 运营管理', permissions: [] },
    system: { key: 'system', label: '🔐 系统管理', permissions: [] }
  }

  permissions.forEach(perm => {
    const key = perm.value.split(':')[0]
    if (groups[key]) {
      groups[key].permissions.push(perm)
    }
  })

  return Object.values(groups).filter(group => group.permissions.length > 0)
}

// 清除缓存（用于测试或强制刷新）
export function clearEnumCache() {
  enums.value = {}
  loading.value = false
}
