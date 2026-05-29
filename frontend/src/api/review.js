/**
 * 评价管理相关API接口
 *
 * 【模块说明】
 * - 处理客人入住后的评价
 * - 管理评价回复
 *
 * 【接口列表】
 * 1. submitReview: 提交评价
 * 2. getMyReview: 获取我的评价
 * 3. getCheckInReview: 获取订单评价
 * 4. getRecentReviews: 获取最新评价
 * 5. getRoomReviews: 获取房间评价
 * 6. replyReview: 回复评价
 *
 * 【对应后端】
 * - Controller: ReviewController
 * - 路径前缀: /review
 *
 * 【对应前端组件】
 * - 评价管理: @/views/visitor/review/index.vue
 */

import request from '@/utils/request'

/**
 * 提交评价
 * @param {Object} data - 评价信息 { checkInId, rating, content }
 * @returns {boolean} 是否成功
 */
export function submitReview(data) {
  return request({
    url: '/review',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户对某订单的评价
 * @param {number} checkInId - 订单ID
 * @returns {Object} 评价信息
 */
export function getMyReview(checkInId) {
  return request({
    url: '/review/my',
    method: 'get',
    params: { checkInId }
  })
}

/**
 * 获取某订单的评价
 * @param {number} checkInId - 订单ID
 * @returns {Object} 评价信息
 */
export function getCheckInReview(checkInId) {
  return request({
    url: `/review/checkin/${checkInId}`,
    method: 'get'
  })
}

/**
 * 管理员回复评价
 * @param {number} id - 评价ID
 * @param {string} reply - 回复内容
 * @returns {boolean} 是否成功
 */
export function replyReview(id, reply) {
  return request({
    url: `/review/reply/${id}`,
    method: 'put',
    params: { reply }
  })
}

/**
 * 获取最新评价列表（用于首页展示）
 * @param {number} limit - 返回数量（默认6）
 * @returns {Array} 评价列表
 */
export function getRecentReviews(limit = 6) {
  return request({
    url: '/review/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取某房间的所有评价
 * @param {number} roomId - 房间ID
 * @returns {Array} 评价列表
 */
export function getRoomReviews(roomId) {
  return request({
    url: `/review/room/${roomId}`,
    method: 'get'
  })
}
