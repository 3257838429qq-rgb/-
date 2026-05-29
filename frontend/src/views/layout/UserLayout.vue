<!--
  用户端主布局组件

  【模块说明】
  - 用户端页面的整体布局框架
  - 包含顶部导航栏、主内容区、页脚

  【布局结构】
  1. 顶部导航：Logo、导航菜单、通知、用户信息
  2. 主内容区：页面组件渲染
  3. 页脚：联系方式和版权信息
  4. 聊天组件：右下角智能客服

  【功能模块】
  1. 导航菜单：首页、客房浏览、来访预约、我的预订、VIP会员
  2. 通知中心：显示系统通知，支持已读标记
  3. VIP余额：显示VIP用户余额
  4. 用户下拉：个人中心、修改密码、退出登录
  5. 智能客服：右下角聊天组件

  【状态管理】
  - 使用useUserStore获取用户信息
  - vipBalance: VIP用户余额
  - isVipUser: 是否VIP用户

  【API调用】
  - getNotificationList: 获取通知列表
  - getUnreadCount: 获取未读数量
  - getVipMemberInfo: 获取VIP信息
  - markAsRead/markAllAsRead: 标记已读

  【路由对应】
  - /portal/home: 首页
  - /portal/rooms: 客房浏览
  - /portal/bookings: 我的预订
  - /portal/visit: 来访预约
  - /portal/vip: VIP会员
  - /portal/profile: 个人中心
-->
<template>
  <div class="user-layout">
    <!-- Header -->
    <header class="user-header">
      <div class="header-inner">
        <div class="header-logo" @click="$router.push('/portal/home')">
          <img src="/418.webp" alt="logo" class="logo-img" />
          <span class="logo-text">东北大学校招待所</span>
        </div>
        <nav class="header-nav">
          <router-link to="/portal/home" class="nav-item" active-class="nav-active">首页</router-link>
          <router-link to="/portal/rooms" class="nav-item" active-class="nav-active">客房浏览</router-link>
          <router-link to="/portal/visit" class="nav-item" active-class="nav-active">来访预约</router-link>
          <router-link to="/portal/bookings" class="nav-item" active-class="nav-active">我的预订</router-link>
          <router-link to="/portal/vip" class="nav-item" active-class="nav-active">VIP会员</router-link>
        </nav>
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

          <div v-if="isVipUser" class="balance-display">
            <el-icon><Wallet /></el-icon>
            <span class="balance-amount">¥{{ vipBalance }}</span>
          </div>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar" :class="{ 'vip-avatar': isVipUser }">
                {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
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
      </div>
    </header>

    <!-- Main -->
    <main class="user-main">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="user-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <img src="/418.webp" alt="logo" class="footer-logo" />
          <span>东北大学校招待所</span>
        </div>
        <div class="footer-info">
          <p>地址：辽宁省沈阳市和平区文化路3号巷11号</p>
          <p>电话：024-83680000 | 邮箱：hotel@neu.edu.cn</p>
          <p class="copy">&copy; 2026 东北大学校招待所 版权所有</p>
        </div>
      </div>
    </footer>

    <ChatWidget />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { User, Lock, SwitchButton, ArrowDown, Bell, Wallet } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { getVipMemberInfo } from '@/api/vip'
import ChatWidget from './ChatWidget.vue'

const router = useRouter()
const userStore = useUserStore()

// VIP state
const vipBalance = ref('0.00')
const isVipUser = ref(false)

async function fetchVipInfo() {
  try {
    const res = await getVipMemberInfo()
    if (res.code === 200 && res.data) {
      isVipUser.value = !!(res.data.vipLevel > 0 && res.data.active)
      vipBalance.value = (res.data.balance || 0).toFixed(2)
    }
  } catch (e) {
    console.error('获取VIP信息失败', e)
  }
}

// Notification state
const notifPopoverRef = ref()
const notifications = ref([])
const unreadCount = ref(0)
let notifTimer = null

async function handleCommand(command) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    stopNotifPolling()
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/portal/profile')
  } else if (command === 'password') {
    router.push('/portal/profile')
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
  fetchVipInfo()
})

onUnmounted(() => {
  stopNotifPolling()
})
</script>

<style lang="scss" scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

// Header
.user-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;

  .logo-img {
    width: 36px;
    height: 36px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: #1a3a5c;
    letter-spacing: 0.5px;
  }
}

.header-nav {
  display: flex;
  gap: 4px;

  .nav-item {
    padding: 8px 20px;
    border-radius: 8px;
    color: #606266;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.2s;
    text-decoration: none;

    &:hover { color: #409eff; background: #ecf5ff; }

    &.nav-active {
      color: #409eff;
      background: #ecf5ff;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;

  .header-icon {
    font-size: 18px;
    color: #606266;
    cursor: pointer;
    padding: 6px;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      color: #409eff;
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
    }

    .username { font-size: 14px; color: #303133; }
  }

  .balance-display {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: linear-gradient(135deg, #fff7e6, #fffbe6);
    border: 1px solid #f5dab1;
    border-radius: 20px;
    font-size: 13px;
    color: #e6a23c;
    font-weight: 600;
    cursor: default;
    white-space: nowrap;

    .el-icon {
      font-size: 14px;
    }
  }
}

.vip-avatar {
  box-shadow: 0 0 0 3px #ffd700, 0 0 12px rgba(255, 215, 0, 0.5);
}

// Main content
.user-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

// Footer
.user-footer {
  background: #1a1a2e;
  color: #fff;
  padding: 40px 24px 24px;
  margin-top: auto;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;

  .footer-logo {
    width: 32px;
    height: 32px;
    opacity: 0.8;
  }
}

.footer-info {
  text-align: right;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;

  .copy {
    margin-top: 4px;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.3);
  }
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
</style>
