/**
 * 用户管理相关API接口
 *
 * 【模块说明】
 * - 管理系统用户信息
 * - 处理密码修改
 *
 * 【接口列表】
 * 1. updatePassword: 修改用户密码
 *
 * 【对应后端】
 * - Controller: UserController
 * - 路径前缀: /system/user
 *
 * 【对应前端组件】
 * - 用户管理: @/views/system/user/index.vue（不存在）
 */

import request from '@/utils/request'

/**
 * 修改用户密码
 * @param {Object} data - 密码参数 { userId, oldPassword, newPassword }
 * @returns {boolean} 是否成功
 */
export function updatePassword(data) {
  return request({
    url: '/system/user/password',
    method: 'put',
    params: data
  })
}
