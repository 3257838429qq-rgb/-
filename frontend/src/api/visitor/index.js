/**
 * 访客管理相关API接口
 *
 * 【模块说明】
 * - 管理访客预约登记
 * - 处理访客审核流程
 *
 * 【接口列表】
 * 1. getVisitorList: 分页查询访客
 * 2. getPendingVisitors: 查询待审核访客
 * 3. getApprovedVisitors: 查询已审核访客
 * 4. getVisitorById: 获取访客详情
 * 5. addVisitor: 新增访客
 * 6. updateVisitor: 修改访客
 * 7. deleteVisitor: 删除访客
 * 8. reviewVisitor: 审核访客
 *
 * 【对应后端】
 * - Controller: VisitorController
 * - 路径前缀: /visitor
 *
 * 【对应前端组件】
 * - 访客列表: @/views/visitor/list/index.vue
 * - 来访预约: @/views/portal/Visit.vue
 */

import request from '@/utils/request'

/**
 * 分页查询访客列表
 * @param {Object} params - 查询参数 { current, size, name, phone, status }
 * @returns {Object} 分页结果
 */
export function getVisitorList(params) {
  return request({
    url: '/visitor/page',
    method: 'get',
    params
  })
}

/**
 * 查询待审核的访客列表
 * @param {Object} params - 查询参数 { current, size }
 * @returns {Object} 待审核访客列表
 */
export function getPendingVisitors(params) {
  return request({
    url: '/visitor/pending',
    method: 'get',
    params
  })
}

/**
 * 查询已审核通过的访客列表
 * @param {Object} params - 查询参数 { current, size }
 * @returns {Object} 已通过访客列表
 */
export function getApprovedVisitors(params) {
  return request({
    url: '/visitor/approved',
    method: 'get',
    params
  })
}

/**
 * 获取访客详情
 * @param {number} id - 访客ID
 * @returns {Object} 访客详情
 */
export function getVisitorById(id) {
  return request({
    url: `/visitor/${id}`,
    method: 'get'
  })
}

/**
 * 新增访客记录
 * @param {Object} data - 访客信息 { name, phone, idCard, visitDate, hostName, hostDeptId, ... }
 * @returns {boolean} 是否成功
 */
export function addVisitor(data) {
  return request({
    url: '/visitor',
    method: 'post',
    data
  })
}

/**
 * 修改访客信息
 * @param {Object} data - 访客信息
 * @returns {boolean} 是否成功
 */
export function updateVisitor(data) {
  return request({
    url: '/visitor',
    method: 'put',
    data
  })
}

/**
 * 删除访客记录
 * @param {number} id - 访客ID
 * @returns {boolean} 是否成功
 */
export function deleteVisitor(id) {
  return request({
    url: `/visitor/${id}`,
    method: 'delete'
  })
}

/**
 * 审核访客申请
 * @param {number} id - 访客ID
 * @param {number} status - 审核状态（1=通过, 2=拒绝）
 * @param {string} remark - 审核备注
 * @returns {boolean} 是否成功
 */
export function reviewVisitor(id, status, remark) {
  return request({
    url: `/visitor/review/${id}`,
    method: 'put',
    params: { status, remark }
  })
}
