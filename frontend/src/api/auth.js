/**
 * 认证相关API接口
 *
 * 【模块说明】
 * - 处理用户登录、注册、退出等认证流程
 * - 获取用户信息和菜单权限
 *
 * 【接口列表】
 * 1. login: 用户登录
 * 2. register: 用户注册
 * 3. getUserInfo: 获取用户信息
 * 4. logout: 退出登录
 *
 * 【对应后端】
 * - Controller: AuthController
 * - 路径前缀: /auth
 *
 * 【对应前端组件】
 * - 登录页: @/views/login/index.vue
 */

import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录参数 { username, password }
 * @returns {Object} { token, user, menus, userType }
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Object} { user, menus, userType }
 */
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

/**
 * 退出登录
 * @returns {string} 退出成功消息
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册参数 { username, password, realName, ... }
 * @returns {string} 注册成功消息
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}
