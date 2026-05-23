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
            <el-menu-item index="/admin/system/user" v-if="hasRole(1)">用户管理</el-menu-item>
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
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled, Setting, User, House, Expand, Fold,
  FullScreen, ArrowDown, Lock, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const activeMenu = ref(route.path)
const activeTab = ref(route.path)
const visitedPages = ref([])
const cachedViews = ref([])

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
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    ElMessageBox.alert('个人中心功能开发中', '提示')
  } else if (command === 'password') {
    ElMessageBox.alert('修改密码功能开发中', '提示')
  }
}

// Init first page
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

// Transitions
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
