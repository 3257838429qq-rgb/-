import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  // Login — public
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },

  // Admin routes (userType=1, staff)
  {
    path: '/admin',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { adminOnly: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页' } },
      { path: 'system/user', name: 'UserManagement', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理', roles: [1] } },
      { path: 'system/role', name: 'RoleManagement', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理', roles: [1] } },
      { path: 'system/dept', name: 'DeptManagement', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理', roles: [1] } },
      { path: 'system/log', name: 'OperLog', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志', roles: [1] } },
      { path: 'visitor/list', name: 'VisitorList', component: () => import('@/views/visitor/list/index.vue'), meta: { title: '访客信息', roles: [1, 2] } },
      { path: 'visitor/reservation', name: 'VisitorReservation', component: () => import('@/views/visitor/reservation/index.vue'), meta: { title: '来访预约', roles: [1, 2] } },
      { path: 'visitor/review', name: 'VisitorReview', component: () => import('@/views/visitor/review/index.vue'), meta: { title: '预约审核', roles: [1, 2] } },
      { path: 'dorm/room-type', name: 'RoomType', component: () => import('@/views/dorm/room-type/index.vue'), meta: { title: '房型管理', roles: [1, 3] } },
      { path: 'dorm/room', name: 'DormRoom', component: () => import('@/views/dorm/room/index.vue'), meta: { title: '房间管理', roles: [1, 3] } },
      { path: 'dorm/checkin', name: 'CheckIn', component: () => import('@/views/dorm/checkin/index.vue'), meta: { title: '入住登记', roles: [1, 3] } },
      { path: 'dorm/record', name: 'StayRecord', component: () => import('@/views/dorm/record/index.vue'), meta: { title: '住宿记录', roles: [1, 3] } }
    ]
  },

  // Portal routes (userType=0, guests)
  {
    path: '/portal',
    component: () => import('@/views/layout/UserLayout.vue'),
    redirect: '/portal/home',
    meta: { guestOnly: true },
    children: [
      { path: 'home', name: 'PortalHome', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'rooms', name: 'PortalRooms', component: () => import('@/views/portal/Rooms.vue'), meta: { title: '客房浏览' } },
      { path: 'bookings', name: 'PortalBookings', component: () => import('@/views/portal/Bookings.vue'), meta: { title: '我的预订' } },
      { path: 'visit', name: 'PortalVisit', component: () => import('@/views/portal/Visit.vue'), meta: { title: '来访预约' } },
      { path: 'profile', name: 'PortalProfile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },

  // Root redirect
  { path: '/', redirect: getDefaultPath() }
]

function getDefaultPath() {
  try {
    const ut = parseInt(localStorage.getItem('userType') || '0')
    return ut === 1 ? '/admin/dashboard' : '/portal/home'
  } catch {
    return '/portal/home'
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 东北大学校招待所` : '东北大学校招待所'

  // Login — always accessible
  if (to.meta.public) {
    next()
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    next('/login')
    return
  }

  const userType = parseInt(localStorage.getItem('userType') || '0')

  // Admin routes — only staff (userType=1)
  if (to.meta.adminOnly) {
    if (userType !== 1) {
      ElMessage.warning('您没有管理权限')
      next('/portal/home')
      return
    }
    // Role-based check on admin child routes
    if (to.meta.roles && to.meta.roles.length > 0) {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        if (!to.meta.roles.includes(userInfo.roleId)) {
          ElMessage.warning('您没有权限访问该页面')
          next('/admin/dashboard')
          return
        }
      } catch { /* proceed */ }
    }
    next()
    return
  }

  // Guest routes — only guests (userType=0)
  if (to.meta.guestOnly && userType !== 0) {
    next('/admin/dashboard')
    return
  }

  next()
})

export default router
