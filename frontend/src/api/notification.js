/**
 * 通知消息相关API接口
 *
 * 【模块说明】
 * - 管理用户收到的系统通知
 * - 处理消息已读/未读状态
 *
 * 【接口列表】
 * 1. getNotificationList: 获取消息列表
 * 2. getUnreadCount: 获取未读数量
 * 3. markAsRead: 标记单条已读
 * 4. markAllAsRead: 标记全部已读
 *
 * 【对应后端】
 * - Controller: NotificationController
 * - 路径前缀: /notification
 *
 * 【对应前端组件】
 * - 聊天组件: @/views/layout/ChatWidget.vue
 */

import request from '@/utils/request'

/**
 * 分页查询通知消息列表
 * @param {Object} params - 查询参数 { current, size }
 * @returns {Object} 分页结果
 */
export function getNotificationList(params) {
  return request({ url: '/notification/page', method: 'get', params })
}

/**
 * 获取未读消息数量
 * @returns {number} 未读数量
 */
export function getUnreadCount() {
  return request({ url: '/notification/unread-count', method: 'get' })
}

/**
 * 标记单条消息为已读
 * @param {number} id - 消息ID
 * @returns {null}
 */
export function markAsRead(id) {
  return request({ url: `/notification/read/${id}`, method: 'put' })
}

/**
 * 标记所有消息为已读
 * @returns {null}
 */
export function markAllAsRead() {
  return request({ url: '/notification/read-all', method: 'put' })
}
