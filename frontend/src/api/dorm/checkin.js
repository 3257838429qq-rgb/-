/**
 * 入住管理相关API接口
 *
 * 【模块说明】
 * - 管理入住、退房、预订业务流程
 * - 支持入住记录查询和操作
 *
 * 【接口列表】
 * 1. getCheckInList: 分页查询入住记录
 * 2. getActiveCheckIns: 查询当前入住列表
 * 3. getStatistics: 获取入住统计
 * 4. getCheckInById: 获取入住详情
 * 5. addCheckIn: 新增入住记录
 * 6. checkOut: 退房操作
 * 7. submitCheckoutRequest: 用户提交退房申请
 * 8. approveCheckout: 审批退房申请
 * 9. rejectCheckout: 拒绝退房申请
 * 10. recordPayment: 记录支付
 * 11. deleteCheckIn: 删除记录
 *
 * 【对应后端】
 * - Controller: CheckInController
 * - 路径前缀: /dorm/checkin
 *
 * 【对应前端组件】
 * - 入住管理: @/views/dorm/checkin/index.vue
 * - 仪表盘: @/views/dashboard/index.vue
 */

import request from '@/utils/request'

/**
 * 分页查询入住记录
 * @param {Object} params - 查询参数 { current, size, visitorName, roomNo, status }
 * @returns {Object} 分页结果
 */
export function getCheckInList(params) {
  return request({
    url: '/dorm/checkin/page',
    method: 'get',
    params
  })
}

/**
 * 查询当前入住中的记录
 * @param {Object} params - 查询参数 { current, size }
 * @returns {Object} 当前入住列表
 */
export function getActiveCheckIns(params) {
  return request({
    url: '/dorm/checkin/active',
    method: 'get',
    params
  })
}

/**
 * 获取入住统计数据
 * @returns {Object} { activeCheckIns, availableRooms, totalRooms, occupancyRate, pendingBookings, pendingCheckouts, pendingVisitors, monthIncome }
 */
export function getStatistics() {
  return request({
    url: '/dorm/checkin/statistics',
    method: 'get'
  })
}

/**
 * 获取入住记录详情
 * @param {number} id - 入住记录ID
 * @returns {Object} 入住详情
 */
export function getCheckInById(id) {
  return request({
    url: `/dorm/checkin/${id}`,
    method: 'get'
  })
}

/**
 * 新增入住记录（前台操作）
 * @param {Object} data - 入住信息 { visitorName, phone, idCard, roomId, checkInDate, checkOutDate, remark }
 * @returns {Object} 新增结果
 */
export function addCheckIn(data) {
  return request({
    url: '/dorm/checkin',
    method: 'post',
    data
  })
}

/**
 * 用户提交退房申请
 * @param {number} id - 入住记录ID
 * @returns {boolean} 是否成功
 */
export function submitCheckoutRequest(id) {
  return request({
    url: `/dorm/checkin/checkout-request/${id}`,
    method: 'post'
  })
}

/**
 * 管理员审核退房申请
 * @param {number} id - 入住记录ID
 * @param {number} otherFee - 额外费用
 * @param {string} paymentMethod - 支付方式
 * @returns {boolean} 是否成功
 */
export function approveCheckout(id, otherFee, paymentMethod) {
  return request({
    url: `/dorm/checkin/checkout-approve/${id}`,
    method: 'put',
    data: { otherFee, paymentMethod }
  })
}

/**
 * 管理员拒绝退房申请
 * @param {number} id - 入住记录ID
 * @returns {boolean} 是否成功
 */
export function rejectCheckout(id) {
  return request({
    url: `/dorm/checkin/checkout-reject/${id}`,
    method: 'put'
  })
}

/**
 * 管理员直接退房
 * @param {number} id - 入住记录ID
 * @param {number} otherFee - 额外费用
 * @param {string} paymentMethod - 支付方式
 * @returns {boolean} 是否成功
 */
export function checkOut(id, otherFee, paymentMethod) {
  return request({
    url: `/dorm/checkin/checkout/${id}`,
    method: 'put',
    data: { otherFee, paymentMethod }
  })
}

/**
 * 记录支付信息
 * @param {number} id - 入住记录ID
 * @param {number} paidAmount - 支付金额
 * @param {string} paymentMethod - 支付方式
 * @returns {boolean} 是否成功
 */
export function recordPayment(id, paidAmount, paymentMethod) {
  return request({
    url: `/dorm/checkin/pay/${id}`,
    method: 'put',
    data: { paidAmount, paymentMethod }
  })
}

/**
 * 删除入住记录
 * @param {number} id - 入住记录ID
 * @returns {boolean} 是否成功
 */
export function deleteCheckIn(id) {
  return request({
    url: `/dorm/checkin/${id}`,
    method: 'delete'
  })
}
