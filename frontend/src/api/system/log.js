/**
 * 系统操作日志相关API接口
 *
 * 【模块说明】
 * - 查询用户在系统中的操作记录
 * - 支持日志删除和清理
 *
 * 【接口列表】
 * 1. getLogList: 分页查询操作日志
 * 2. deleteLog: 删除单条日志
 * 3. batchDeleteLog: 批量删除日志
 *
 * 【对应后端】
 * - Controller: SysOperLogController
 * - 路径前缀: /system/oper-log
 *
 * 【对应前端组件】
 * - 日志管理: @/views/system/log/index.vue
 */

import request from '@/utils/request'

/**
 * 分页查询操作日志
 * @param {Object} params - 查询参数 { current, size, title, operType, status }
 * @returns {Object} 分页结果
 */
export function getLogList(params) {
  return request({
    url: '/system/oper-log/page',
    method: 'get',
    params
  })
}

/**
 * 删除单条日志
 * @param {number} id - 日志ID
 * @returns {boolean} 是否成功
 */
export function deleteLog(id) {
  return request({
    url: `/system/oper-log/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除日志
 * @param {Array} ids - 日志ID数组
 * @returns {boolean} 是否成功
 */
export function batchDeleteLog(ids) {
  return request({
    url: '/system/oper-log/batch',
    method: 'delete',
    params: { ids }
  })
}
