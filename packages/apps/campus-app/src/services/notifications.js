import { opsHttp as http } from './http'

// 获取消息列表
export function getNotifications(params = {}) {
  return http.get('/notifications', { params })
}

// 获取未读消息数量
export function getUnreadCount() {
  return http.get('/notifications/unread-count')
}

// 获取最近消息
export function getRecentNotifications(limit = 5) {
  return http.get('/notifications/recent', { params: { limit } })
}

// 标记消息为已读
export function markAsRead(id) {
  return http.post(`/notifications/${id}/read`)
}

// 批量标记为已读
export function markMultipleAsRead(ids) {
  return http.post('/notifications/mark-read', ids)
}

// 标记全部为已读
export function markAllAsRead() {
  return http.post('/notifications/mark-all-read')
}

// 删除消息
export function deleteNotification(id) {
  return http.delete(`/notifications/${id}`)
}

// 发送系统通知
export function sendNotification(notification) {
  return http.post('/notifications/send', notification)
}

// 消息类型映射
export const notificationTypes = {
  SYSTEM: { label: '系统通知', color: 'blue' },
  REVIEW: { label: '审核通知', color: 'orange' },
  ORDER: { label: '订单通知', color: 'green' },
  USER: { label: '用户通知', color: 'purple' },
  BUSINESS: { label: '业务通知', color: 'cyan' }
}

// 优先级映射
export const notificationPriorities = {
  LOW: { label: '低', color: 'gray' },
  MEDIUM: { label: '中', color: 'blue' },
  HIGH: { label: '高', color: 'orange' },
  URGENT: { label: '紧急', color: 'red' }
}

// 获取消息类型标签
export function getNotificationTypeLabel(type) {
  return notificationTypes[type]?.label || type
}

// 获取消息类型颜色
export function getNotificationTypeColor(type) {
  return notificationTypes[type]?.color || 'blue'
}

// 获取优先级标签
export function getPriorityLabel(priority) {
  return notificationPriorities[priority]?.label || priority
}

// 获取优先级颜色
export function getPriorityColor(priority) {
  return notificationPriorities[priority]?.color || 'blue'
}
