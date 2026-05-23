import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))
  const userType = ref(parseInt(localStorage.getItem('userType') || '0'))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    if (info?.menus) {
      menus.value = info.menus
      localStorage.setItem('menus', JSON.stringify(info.menus))
    }
  }

  function setMenus(menuList) {
    menus.value = menuList || []
    localStorage.setItem('menus', JSON.stringify(menuList || []))
  }

  function setUserType(ut) {
    userType.value = ut != null ? ut : 0
    localStorage.setItem('userType', String(ut != null ? ut : 0))
  }

  function isAdmin() {
    return userType.value === 1
  }

  function isGuest() {
    return userType.value === 0
  }

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

  return {
    token, userInfo, menus, userType,
    setToken, setUserInfo, setMenus, setUserType,
    isAdmin, isGuest, fetchUserInfo, logout
  }
})
