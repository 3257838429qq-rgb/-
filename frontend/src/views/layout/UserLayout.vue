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
        </nav>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { User, Lock, SwitchButton, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

async function handleCommand(command) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/portal/profile')
  } else if (command === 'password') {
    router.push('/portal/profile')
  }
}
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
</style>
