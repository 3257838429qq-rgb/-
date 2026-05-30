/**
 * 路由配置文件
 *
 * 【模块说明】
 * - 配置前端路由规则
 * - 实现页面访问控制和权限管理
 * - 根据用户类型(userType)自动跳转
 *
 * 【路由结构】
 * 1. /login - 登录页（公开访问）
 * 2. /admin/* - 管理端路由（userType=1，管理员）
 * 3. /portal/* - 用户端路由（userType=0，普通用户）
 *
 * 【权限控制】
 * - meta.public: 公开页面，无需登录
 * - meta.adminOnly: 仅管理员可访问
 * - meta.guestOnly: 仅普通用户可访问
 * - meta.roles: 角色权限控制
 *
 * 【默认跳转逻辑】
 * - userType=1 -> /admin/dashboard
 * - userType=0 -> /portal/home
 * - 未登录 -> /login
 *
 * 【导航守卫】
 * - 每次路由切换检查登录状态
 * - 根据userType限制访问管理端/用户端
 * - Token过期自动跳转登录页
 */

import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  // ==================== 公开路由 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }  // public=true 表示无需登录即可访问
  },

  // ==================== 管理端路由（userType=1） ====================
  {
    path: '/admin',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { adminOnly: true },  // 仅管理员可访问
    children: [
      // 首页/仪表盘
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页' } },
      // 系统管理
      { path: 'system/role', name: 'RoleManagement', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理', roles: [1] } },
      { path: 'system/dept', name: 'DeptManagement', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理', roles: [1] } },
      { path: 'system/log', name: 'OperLog', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志', roles: [1] } },
      // 访客管理
      { path: 'visitor/list', name: 'VisitorList', component: () => import('@/views/visitor/list/index.vue'), meta: { title: '访客信息', roles: [1] } },
      { path: 'visitor/reservation', name: 'VisitorReservation', component: () => import('@/views/visitor/reservation/index.vue'), meta: { title: '来访预约', roles: [1] } },
      { path: 'visitor/review', name: 'VisitorReview', component: () => import('@/views/visitor/review/index.vue'), meta: { title: '预约审核', roles: [1] } },
      // 房间管理
      { path: 'dorm/room-type', name: 'RoomType', component: () => import('@/views/dorm/room-type/index.vue'), meta: { title: '房型管理', roles: [1] } },
      { path: 'dorm/room', name: 'DormRoom', component: () => import('@/views/dorm/room/index.vue'), meta: { title: '房间管理', roles: [1] } },
      { path: 'dorm/room-status', name: 'RoomStatus', component: () => import('@/views/dorm/room-status/index.vue'), meta: { title: '房态图', roles: [1] } },
      { path: 'dorm/checkin', name: 'CheckIn', component: () => import('@/views/dorm/checkin/index.vue'), meta: { title: '入住登记', roles: [1] } },
      { path: 'dorm/record', name: 'StayRecord', component: () => import('@/views/dorm/record/index.vue'), meta: { title: '住宿记录', roles: [1] } }
    ]
  },

  // ==================== 用户端路由（userType=0） ====================
  {
    path: '/portal',
    component: () => import('@/views/layout/UserLayout.vue'),
    redirect: '/portal/home',
    meta: { guestOnly: true },  // 仅普通用户可访问
    children: [
      { path: 'home', name: 'PortalHome', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'rooms', name: 'PortalRooms', component: () => import('@/views/portal/Rooms.vue'), meta: { title: '客房浏览' } },
      { path: 'calendar', name: 'PortalCalendar', component: () => import('@/views/portal/Calendar.vue'), meta: { title: '日历预订' } },
      { path: 'bookings', name: 'PortalBookings', component: () => import('@/views/portal/Bookings.vue'), meta: { title: '我的预订' } },
      { path: 'visit', name: 'PortalVisit', component: () => import('@/views/portal/Visit.vue'), meta: { title: '来访预约' } },
      { path: 'vip', name: 'PortalVip', component: () => import('@/views/portal/Vip.vue'), meta: { title: 'VIP会员' } },
      { path: 'profile', name: 'PortalProfile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },

  // ==================== 根路径重定向 ====================
  { path: '/', redirect: getDefaultPath() }
]

/**
 * 获取默认跳转路径
 * 根据localStorage中的userType判断跳转
 */
function getDefaultPath() {
  try {
    const ut = parseInt(localStorage.getItem('userType') || '0')
    return ut === 1 ? '/admin/dashboard' : '/portal/home'
  } catch {
    return '/portal/home'
  }
}

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：路由切换前进行权限检查
router.beforeEach((to, from, next) => {
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - 东北大学校招待所` : '东北大学校招待所'

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 检查是否登录
  const token = localStorage.getItem('token')
  if (!token) {
    next('/login')
    return
  }

  const userType = parseInt(localStorage.getItem('userType') || '0')

  // 管理端路由检查（仅userType=1可访问）
  if (to.meta.adminOnly) {
    if (userType !== 1) {
      ElMessage.warning('您没有管理权限')
      next('/portal/home')
      return
    }
    // 角色权限检查
    if (to.meta.roles && to.meta.roles.length > 0) {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        if (!to.meta.roles.includes(userInfo.roleId)) {
          ElMessage.warning('您没有权限访问该页面')
          next('/admin/dashboard')
          return
        }
      } catch { /* 继续执行 */ }
    }
    next()
    return
  }

  // 用户端路由检查（仅userType=0可访问）
  if (to.meta.guestOnly && userType !== 0) {
    next('/admin/dashboard')
    return
  }

  next()
})

export default router
