import request from '@/utils/request'

/**
 * 获取通知列表
 */
export function getNotifications(username, unreadOnly = false) {
  return request({
    url: '/workflow/notifications',
    method: 'get',
    params: { username, unreadOnly }
  })
}

/**
 * 获取未读数量
 */
export function getUnreadCount(username) {
  return request({
    url: '/workflow/notifications/unread-count',
    method: 'get',
    params: { username }
  })
}

/**
 * 标记为已读
 */
export function markAsRead(id) {
  return request({
    url: `/workflow/notifications/${id}/read`,
    method: 'put'
  })
}

/**
 * 全部标记为已读
 */
export function markAllAsRead(username) {
  return request({
    url: '/workflow/notifications/read-all',
    method: 'put',
    params: { username }
  })
}

/**
 * 删除通知
 */
export function deleteNotification(id) {
  return request({
    url: `/workflow/notifications/${id}`,
    method: 'delete'
  })
}

