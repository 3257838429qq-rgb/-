/**
 * 角色管理相关API接口
 *
 * 【模块说明】
 * - 管理系统角色
 * - 定义角色权限
 *
 * 【接口列表】
 * 1. getRoleList: 获取角色列表
 * 2. getRoleById: 获取角色详情
 * 3. addRole: 新增角色
 * 4. updateRole: 修改角色
 * 5. deleteRole: 删除角色
 *
 * 【对应后端】
 * - Controller: RoleController
 * - 路径前缀: /system/role
 *
 * 【对应前端组件】
 * - 角色管理: @/views/system/role/index.vue
 */

import request from '@/utils/request'

/**
 * 获取所有角色列表
 * @returns {Array} 角色列表
 */
export function getRoleList() {
  return request({ url: '/system/role/list', method: 'get' })
}

/**
 * 获取角色详情
 * @param {number} id - 角色ID
 * @returns {Object} 角色详情
 */
export function getRoleById(id) {
  return request({ url: `/system/role/${id}`, method: 'get' })
}

/**
 * 新增角色
 * @param {Object} data - 角色信息 { name, code, description, status }
 * @returns {boolean} 是否成功
 */
export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

/**
 * 修改角色
 * @param {Object} data - 角色信息
 * @returns {boolean} 是否成功
 */
export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {boolean} 是否成功
 */
export function deleteRole(id) {
  return request({ url: `/system/role/${id}`, method: 'delete' })
}
