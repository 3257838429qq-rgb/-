/**
 * 用户状态管理（Pinia Store）
 *
 * 【模块说明】
 * - 管理用户登录状态
 * - 存储用户信息、Token、菜单权限
 * - 提供用户状态持久化（localStorage）
 *
 * 【状态字段】
 * - token: JWT认证令牌
 * - userInfo: 用户基本信息
 * - menus: 用户菜单权限
 * - userType: 用户类型（0=普通用户, 1=管理员）
 *
 * 【存储结构】
 * localStorage存储：
 * - token: 认证令牌
 * - userInfo: 用户信息JSON
 * - menus: 菜单权限JSON
 * - userType: 用户类型
 *
 * 【使用方式】
 * import { useUserStore } from '@/stores/user'
 * const userStore = useUserStore()
 * userStore.token, userStore.userInfo, userStore.isAdmin()
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // ==================== 状态定义 ====================
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))
  const userType = ref(parseInt(localStorage.getItem('userType') || '0'))

  // ==================== 方法定义 ====================

  /**
   * 设置Token
   * @param {string} newToken - 新Token
   */
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param {Object} info - 用户信息对象
   */
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    // 如果包含menus，一并存储
    if (info?.menus) {
      menus.value = info.menus
      localStorage.setItem('menus', JSON.stringify(info.menus))
    }
  }

  /**
   * 设置菜单权限
   * @param {Array} menuList - 菜单列表
   */
  function setMenus(menuList) {
    menus.value = menuList || []
    localStorage.setItem('menus', JSON.stringify(menuList || []))
  }

  /**
   * 设置用户类型
   * @param {number} ut - 用户类型（0=普通用户, 1=管理员）
   */
  function setUserType(ut) {
    userType.value = ut != null ? ut : 0
    localStorage.setItem('userType', String(ut != null ? ut : 0))
  }

  /**
   * 判断是否为管理员
   * @returns {boolean}
   */
  function isAdmin() {
    return userType.value === 1
  }

  /**
   * 判断是否为普通用户
   * @returns {boolean}
   */
  function isGuest() {
    return userType.value === 0
  }

  /**
   * 从服务器获取最新用户信息
   * 用于页面刷新后重新获取用户数据
   */
  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        const data = res.data
        userInfo.value = data.user || data
        localStorage.setItem('userInfo', JSON.stringify(data.user || data))
        if (data.menus) {
          menus.value = data.menus
          localStorage.setItem('menus', JSON.stringify(data.menus))
        }
        if (data.userType != null) {
          setUserType(data.userType)
        }
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  /**
   * 退出登录
   * 清除所有用户状态和存储
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    userType.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')
    localStorage.removeItem('userType')
    ElMessage.success('退出登录成功')
  }

  // ==================== 导出 ====================
  return {
    token, userInfo, menus, userType,
    setToken, setUserInfo, setMenus, setUserType,
    isAdmin, isGuest, fetchUserInfo, logout
  }
})
