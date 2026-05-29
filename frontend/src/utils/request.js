/**
 * Axios请求封装工具
 *
 * 【模块说明】
 * - 基于Axios封装HTTP请求
 * - 自动添加认证Token
 * - 统一处理响应和错误
 * - 集成Element Plus消息提示
 *
 * 【配置项】
 * - baseURL: API基础路径（来自环境变量VITE_API_BASE_URL，默认为/api）
 * - timeout: 请求超时时间（15秒）
 *
 * 【请求拦截器】
 * - 自动在请求头添加Authorization: Bearer {token}
 * - 从localStorage读取Token
 *
 * 【响应拦截器】
 * - code=200: 返回data数据
 * - code=401: Token过期，弹出重新登录提示
 * - code=403: 无权限，显示错误消息
 * - 其他code: 显示错误消息
 * - 网络错误: 显示网络连接失败
 *
 * 【使用方式】
 * import request from '@/utils/request'
 * request({ url: '/xxx', method: 'get' })
 */

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',  // API基础路径
  timeout: 15000  // 请求超时15秒
})

// ==================== 请求拦截器 ====================
service.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('token')
    if (token) {
      // 添加Bearer Token到请求头
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误', error)
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================
service.interceptors.response.use(
  response => {
    const res = response.data
    // 成功响应
    if (res.code === 200) {
      return res
    }
    // Token过期
    if (res.code === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.clear()
        router.push('/login')
      })
      return Promise.reject(new Error('登录已过期'))
    }
    // 无权限
    if (res.code === 403) {
      ElMessage.error(res.message || '没有权限')
      return Promise.reject(new Error(res.message || '没有权限'))
    }
    // 其他错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // HTTP状态码处理
    if (error.response) {
      switch (error.response.status) {
        case 401:  // 未授权
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            localStorage.clear()
            router.push('/login')
          })
          break
        case 403:  // 禁止访问
          ElMessage.error('没有权限访问该资源')
          break
        case 500:  // 服务器错误
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.message || '网络请求失败')
      }
    } else {
      // 网络连接失败
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default service
