<template>
  <div class="login-container">
    <!-- Animated background circles -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="login-box">
      <div class="login-header">
        <img src="/418.webp" alt="logo" class="login-logo" />
        <h1>东北大学校招待所系统</h1>
        <p>Northeastern University Guesthouse Management System</p>
      </div>

      <!-- Login form -->
      <el-form v-show="!isRegister" ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="login-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="switch-text">还没有账号？</span>
          <el-button type="primary" link @click="isRegister = true">立即注册</el-button>
        </div>
      </el-form>

      <!-- Register form -->
      <el-form v-show="isRegister" ref="registerFormRef" :model="registerForm" :rules="registerRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            size="large"
            :prefix-icon="UserFilled"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="login-input"
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="registering"
            class="login-btn"
            @click="handleRegister"
          >
            {{ registering ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <span class="switch-text">已有账号？</span>
          <el-button type="primary" link @click="isRegister = false">返回登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Phone } from '@element-plus/icons-vue'
import { login, register as registerApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const registering = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      const data = res.data
      userStore.setToken(data.token)
      userStore.setUserInfo(data.user)
      userStore.setMenus(data.menus)
      userStore.setUserType(data.userType)
      ElMessage.success('登录成功')
      const target = (data.userType === 1) ? '/admin/dashboard' : '/portal/home'
      router.push(target)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}

async function handleRegister() {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    registering.value = true
    try {
      await registerApi({
        username: registerForm.username,
        realName: registerForm.realName,
        phone: registerForm.phone,
        password: registerForm.password,
        userType: 0,
        status: 1
      })
      ElMessage.success('注册成功，请登录')
      registerFormRef.value.resetFields()
      isRegister.value = false
      loginForm.username = registerForm.username
    } catch (e) {
      console.error(e)
    } finally {
      registering.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0c1929 0%, #1a3a5c 30%, #2d6a9f 60%, #4a9fd4 100%);
  position: relative;
  overflow: hidden;
}

// Floating background shapes
.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.08;
    background: #fff;
  }

  .shape-1 {
    width: 500px; height: 500px;
    top: -15%; left: -10%;
    animation: float 20s ease-in-out infinite;
  }

  .shape-2 {
    width: 350px; height: 350px;
    bottom: -10%; right: -5%;
    animation: float 18s ease-in-out 2s infinite;
  }

  .shape-3 {
    width: 200px; height: 200px;
    top: 40%; right: 15%;
    animation: float 15s ease-in-out 1s infinite;
  }

  .shape-4 {
    width: 150px; height: 150px;
    bottom: 20%; left: 20%;
    animation: float 12s ease-in-out 3s infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%  { transform: translate(30px, -30px) scale(1.05); }
  66%  { transform: translate(-20px, 20px) scale(0.95); }
}

// Login box
.login-box {
  width: 440px;
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(255,255,255,0.1);
  z-index: 1;
  backdrop-filter: blur(10px);
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;

  .login-logo {
    width: 56px;
    height: 56px;
    margin-bottom: 16px;
    filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
  }

  h1 {
    font-size: 20px;
    color: #1a3a5c;
    margin-bottom: 6px;
    font-weight: 700;
    letter-spacing: 0.5px;
  }

  p {
    font-size: 11px;
    color: #909399;
    letter-spacing: 1.5px;
    text-transform: uppercase;
  }
}

.login-form {
  .login-input :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 2px 14px;
    box-shadow: 0 0 0 1px #e4e7ed inset;
  }

  .login-btn {
    width: 100%;
    height: 46px;
    font-size: 16px;
    letter-spacing: 8px;
    border-radius: 10px;
    margin-top: 8px;
    font-weight: 600;
  }
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin-top: -8px;

  .switch-text {
    font-size: 13px;
    color: #909399;
  }
}

.login-footer {
  text-align: center;

  p {
    font-size: 12px;
    color: #c0c4cc;
    margin-top: 4px;
  }
}
</style>
