<!--
  管理端主布局组件

  【模块说明】
  - 管理端页面的整体布局框架
  - 包含侧边栏菜单、顶栏、标签页、主内容区

  【布局结构】
  1. 侧边栏：可折叠的导航菜单
  2. 顶栏：面包屑、通知、用户信息
  3. 标签栏：多标签页切换
  4. 主内容区：页面组件渲染

  【功能模块】
  1. 侧边栏菜单：根据用户权限显示菜单项
  2. 通知中心：显示系统通知，支持已读标记
  3. 用户下拉：个人中心、修改密码、退出登录
  4. 标签页：记录访问过的页面，支持关闭

  【状态管理】
  - 使用useUserStore获取用户信息
  - menus: 从服务器获取的菜单权限
  - unreadCount: 未读通知数量

  【API调用】
  - getNotificationList: 获取通知列表
  - getUnreadCount: 获取未读数量
  - markAsRead: 标记已读
  - markAllAsRead: 全部已读
  - logout: 退出登录

  【路由对应】
  - /admin/dashboard: 首页
  - /admin/system/*: 系统管理
  - /admin/visitor/*: 访客管理
  - /admin/dorm/*: 招待所管理
-->
<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/admin/dashboard')">
        <img src="/418.webp" alt="logo" class="logo-img" />
        <transition name="fade">
          <span v-if="!isCollapsed" class="logo-text">东北大学校招待所</span>
        </transition>
      </div>

      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :unique-opened="true"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="/admin/system" v-if="hasMenuPermission('/system')">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/system/role" v-if="hasRole(1)">角色管理</el-menu-item>
            <el-menu-item index="/admin/system/dept" v-if="hasRole(1)">部门管理</el-menu-item>
            <el-menu-item index="/admin/system/log" v-if="hasRole(1)">操作日志</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/admin/visitor" v-if="hasMenuPermission('/visitor')">
            <template #title>
              <el-icon><User /></el-icon>
              <span>访客管理</span>
            </template>
            <el-menu-item index="/admin/visitor/list">访客信息</el-menu-item>
            <el-menu-item index="/admin/visitor/reservation">来访预约</el-menu-item>
            <el-menu-item index="/admin/visitor/review">预约审核</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/admin/dorm" v-if="hasMenuPermission('/dorm')">
            <template #title>
              <el-icon><House /></el-icon>
              <span>招待所管理</span>
            </template>
            <el-menu-item index="/admin/dorm/room-type">房型管理</el-menu-item>
            <el-menu-item index="/admin/dorm/room">房间管理</el-menu-item>
            <el-menu-item index="/admin/dorm/room-status">房态图</el-menu-item>
            <el-menu-item index="/admin/dorm/checkin">入住登记</el-menu-item>
            <el-menu-item index="/admin/dorm/record">住宿记录</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-footer" v-if="!isCollapsed">
        <span>v1.0.0</span>
      </div>
    </el-aside>

    <!-- Main area -->
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
              <el-icon style="vertical-align: -2px"><HomeFilled /></el-icon>
              <span style="margin-left:4px">首页</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- Notification bell -->
          <el-popover
            ref="notifPopoverRef"
            placement="bottom-end"
            :width="380"
            trigger="click"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
                <el-icon class="header-icon notif-icon">
                  <Bell />
                </el-icon>
              </el-badge>
            </template>
            <div class="notif-panel">
              <div class="notif-header">
                <span class="notif-title">我的通知</span>
                <el-button type="primary" link size="small" @click="markAllRead" v-if="unreadCount > 0">
                  全部已读
                </el-button>
              </div>
              <el-scrollbar style="height: 340px">
                <div v-if="notifications.length === 0" class="notif-empty">
                  <el-icon size="32" color="#c0c4cc"><Bell /></el-icon>
                  <p>暂无通知</p>
                </div>
                <div
                  v-for="item in notifications"
                  :key="item.id"
                  class="notif-item"
                  :class="{ unread: item.readStatus === 0 }"
                  @click="handleNotifClick(item)"
                >
                  <div class="notif-item-dot" v-if="item.readStatus === 0"></div>
                  <div class="notif-item-body">
                    <div class="notif-item-title">{{ item.title }}</div>
                    <div class="notif-item-content">{{ item.content }}</div>
                    <div class="notif-item-time">{{ formatTime(item.createTime) }}</div>
                  </div>
                </div>
              </el-scrollbar>
            </div>
          </el-popover>

          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullScreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Page tabs -->
      <div class="tabs-bar" v-if="visitedPages.length > 0">
        <el-tabs
          v-model="activeTab"
          type="card"
          closable
          @tab-click="onTabClick"
          @tab-remove="onTabRemove"
        >
          <el-tab-pane
            v-for="page in visitedPages"
            :key="page.path"
            :label="page.title"
            :name="page.path"
          />
        </el-tabs>
      </div>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedViews">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  HomeFilled, Setting, User, House, Expand, Fold,
  FullScreen, ArrowDown, Lock, SwitchButton, Bell
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const activeMenu = ref(route.path)
const activeTab = ref(route.path)
const visitedPages = ref([])
const cachedViews = ref([])

// Notification state
const notifPopoverRef = ref()
const notifications = ref([])
const unreadCount = ref(0)
let notifTimer = null

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
  activeTab.value = newPath
  addVisitedPage(newPath, route.meta.title || newPath)
})

function addVisitedPage(path, title) {
  if (!visitedPages.value.find(p => p.path === path)) {
    visitedPages.value.push({ path, title })
  }
  if (!cachedViews.value.includes(path)) {
    cachedViews.value.push(path)
  }
}

function onTabClick(tab) {
  router.push(tab.props.name)
}

function onTabRemove(name) {
  const idx = visitedPages.value.findIndex(p => p.path === name)
  if (idx === -1) return
  visitedPages.value.splice(idx, 1)
  cachedViews.value = cachedViews.value.filter(v => v !== name)
  if (name === activeTab.value) {
    const next = visitedPages.value[idx] || visitedPages.value[idx - 1]
    if (next) {
      activeTab.value = next.path
      router.push(next.path)
    } else {
      router.push('/dashboard')
    }
  }
}

const currentTitle = computed(() => route.meta.title)

function hasRole(roleId) {
  return userStore.userInfo?.roleId === roleId
}

function hasMenuPermission(path) {
  const menus = userStore.menus || []
  return menus.some(m => m.path === path || (m.path && m.path.startsWith(path)))
}

function handleMenuSelect(index) {
  router.push(index)
}

function toggleFullScreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

async function handleCommand(command) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    try { await logout() } catch (e) { /* ignore */ }
    stopNotifPolling()
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    ElMessageBox.alert('个人中心功能开发中', '提示')
  } else if (command === 'password') {
    ElMessageBox.alert('修改密码功能开发中', '提示')
  }
}

async function fetchNotifications() {
  try {
    const res = await getNotificationList({ current: 1, size: 20 })
    if (res.code === 200) {
      notifications.value = res.data.records || res.data || []
    }
  } catch (e) {
    console.error('获取通知列表失败', e)
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (e) {
    console.error('获取未读数失败', e)
  }
}

async function markAllRead() {
  try {
    await markAllAsRead()
    unreadCount.value = 0
    notifications.value.forEach(n => n.readStatus = 1)
  } catch (e) {
    console.error('标记全部已读失败', e)
  }
}

async function handleNotifClick(item) {
  if (item.readStatus === 0) {
    try {
      await markAsRead(item.id)
      item.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
  notifPopoverRef.value?.hide()
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return timeStr.slice(0, 16).replace('T', ' ')
}

function startNotifPolling() {
  fetchNotifications()
  fetchUnreadCount()
  notifTimer = setInterval(() => {
    fetchUnreadCount()
    if (notifications.value.length > 0) {
      fetchNotifications()
    }
  }, 30000)
}

function stopNotifPolling() {
  if (notifTimer) {
    clearInterval(notifTimer)
    notifTimer = null
  }
}

onMounted(() => {
  startNotifPolling()
})

onUnmounted(() => {
  stopNotifPolling()
})

addVisitedPage(route.path, route.meta.title || route.path)
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

// ===== Sidebar =====
.aside {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    flex-shrink: 0;

    .logo-img {
      width: 34px;
      height: 34px;
      filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
    }

    .logo-text {
      color: #fff;
      font-size: 17px;
      font-weight: 700;
      white-space: nowrap;
      letter-spacing: 1px;
    }
  }

  .sidebar-menu {
    background: transparent;
    border: none;
    flex: 1;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.65);
      height: 48px;
      line-height: 48px;
      margin: 2px 8px;
      border-radius: 8px;
      transition: all 0.2s ease;
      font-size: 14px;

      .el-icon {
        font-size: 18px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #fff;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: linear-gradient(135deg, #409eff, #337ecc);
      color: #fff;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
    }

    :deep(.el-sub-menu) {
      .el-sub-menu__title {
        .el-sub-menu__icon-arrow {
          color: rgba(255, 255, 255, 0.4);
          right: 16px;
        }
      }

      &.is-opened > .el-sub-menu__title {
        color: #fff;
      }

      .el-menu {
        background: rgba(0, 0, 0, 0.15);
        margin: 0 8px;
        border-radius: 8px;

        .el-menu-item {
          padding-left: 56px !important;
          height: 42px;
          line-height: 42px;
          font-size: 13px;
        }
      }
    }
  }

  .sidebar-footer {
    padding: 12px;
    text-align: center;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    flex-shrink: 0;

    span {
      color: rgba(255, 255, 255, 0.3);
      font-size: 12px;
    }
  }
}

// ===== Header =====
.main-container {
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  height: 56px;
  z-index: 10;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 14px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      padding: 6px;
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        color: var(--primary);
        background: #ecf5ff;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-icon {
      font-size: 18px;
      color: #606266;
      cursor: pointer;
      padding: 6px;
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        color: var(--primary);
        background: #ecf5ff;
      }
    }

    .notif-icon {
      font-size: 20px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 10px;
      border-radius: 8px;
      transition: all 0.2s;

      &:hover { background: #f5f7fa; }

      .user-avatar {
        background: linear-gradient(135deg, #409eff, #337ecc);
        color: #fff;
        font-weight: 600;
        font-size: 14px;
      }

      .username {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }

      .dropdown-icon {
        font-size: 12px;
        color: #909399;
        transition: transform 0.2s;
      }
    }
  }
}

// ===== Page Tabs =====
.tabs-bar {
  background: #fff;
  padding: 0 12px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;

  :deep(.el-tabs) {
    --el-tabs-header-height: 36px;

    .el-tabs__header {
      margin: 0;
      border: none;

      .el-tabs__nav {
        border: none;
      }

      .el-tabs__item {
        height: 32px;
        line-height: 32px;
        margin: 4px 2px;
        border-radius: 6px;
        font-size: 12px;
        border: 1px solid transparent;
        background: transparent;

        &.is-active {
          background: #ecf5ff;
          color: var(--primary);
          border-color: #d9ecff;
        }

        &:hover {
          color: var(--primary);
        }
      }
    }
  }
}

// ===== Main content =====
.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

// ===== Notification panel =====
.notif-badge {
  :deep(.el-badge__content) {
    top: 2px;
    right: 2px;
  }
}

.notif-panel {
  margin: -12px;
  user-select: none;

  .notif-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;

    .notif-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }

  .notif-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    color: #909399;

    p {
      margin-top: 8px;
      font-size: 14px;
    }
  }

  .notif-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 16px;
    cursor: pointer;
    transition: background 0.15s;
    border-bottom: 1px solid #f5f7fa;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f5f7fa;
    }

    &.unread {
      background: #ecf5ff;

      &:hover {
        background: #d9ecff;
      }
    }

    .notif-item-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #f56c6c;
      flex-shrink: 0;
      margin-top: 5px;
    }

    .notif-item-body {
      flex: 1;
      min-width: 0;

      .notif-item-title {
        font-size: 13px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
        line-height: 1.4;
      }

      .notif-item-content {
        font-size: 12px;
        color: #606266;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 4px;
      }

      .notif-item-time {
        font-size: 11px;
        color: #909399;
      }
    }
  }
}

// Transitions
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
