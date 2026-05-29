/**
 * 部门管理相关API接口
 *
 * 【模块说明】
 * - 管理酒店内部组织架构
 * - 支持树形结构展示
 *
 * 【接口列表】
 * 1. getDeptList: 获取部门列表
 * 2. getDeptById: 获取部门详情
 * 3. addDept: 新增部门
 * 4. updateDept: 修改部门
 * 5. deleteDept: 删除部门
 *
 * 【对应后端】
 * - Controller: DeptController
 * - 路径前缀: /system/dept
 *
 * 【对应前端组件】
 * - 部门管理: @/views/system/dept/index.vue
 */

import request from '@/utils/request'

/**
 * 获取部门树形列表
 * @returns {Array} 部门树
 */
export function getDeptList() {
  return request({
    url: '/system/dept/list',
    method: 'get'
  })
}

/**
 * 获取部门详情
 * @param {number} id - 部门ID
 * @returns {Object} 部门详情
 */
export function getDeptById(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'get'
  })
}

/**
 * 新增部门
 * @param {Object} data - 部门信息 { name, parentId, manager, phone, status }
 * @returns {boolean} 是否成功
 */
export function addDept(data) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

/**
 * 修改部门信息
 * @param {Object} data - 部门信息
 * @returns {boolean} 是否成功
 */
export function updateDept(data) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

/**
 * 删除部门
 * @param {number} id - 部门ID
 * @returns {boolean} 是否成功
 */
export function deleteDept(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'delete'
  })
}
