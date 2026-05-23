<template>
  <div class="portal-profile">
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理您的个人信息和密码</p>
    </div>

    <el-row :gutter="24">
      <el-col :md="12" :span="24">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-form ref="formRef" :model="form" label-width="80px" :disabled="!editing">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button v-if="!editing" type="primary" @click="editing = true">编辑</el-button>
              <template v-else>
                <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </template>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :md="12" :span="24">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">修改密码</span>
          </template>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/user'
import { updatePassword } from '@/api/system/user'

const editing = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const formRef = ref()
const pwdRef = ref()
const originalForm = ref({})

const form = reactive({
  id: null, username: '', realName: '', phone: '', email: ''
})

const pwdForm = reactive({
  oldPassword: '', newPassword: '', confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码不少于6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_, v, cb) => v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200) {
      const data = res.data.user || res.data
      Object.assign(form, { id: data.id, username: data.username, realName: data.realName || '', phone: data.phone || '', email: data.email || '' })
      originalForm.value = { ...form }
    }
  } catch (e) { console.error(e) }
}

function cancelEdit() {
  Object.assign(form, originalForm.value)
  editing.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    const res = await updateProfile({ id: form.id, realName: form.realName, phone: form.phone, email: form.email })
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      originalForm.value = { ...form }
      editing.value = false
    }
  } catch (e) { console.error(e) }
  finally { saving.value = false }
}

async function changePassword() {
  if (!pwdRef.value) return
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      const res = await updatePassword({ id: form.id, oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        pwdForm.oldPassword = ''
        pwdForm.newPassword = ''
        pwdForm.confirmPassword = ''
      }
    } catch (e) { console.error(e) }
    finally { changingPwd.value = false }
  })
}

onMounted(loadProfile)
</script>

<style lang="scss" scoped>
.portal-profile {
  animation: fadeIn 0.4s ease;
  max-width: 900px;
}

.page-header {
  margin-bottom: 24px;

  h2 { font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 6px; }
  p { font-size: 14px; color: #909399; }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;

  &::before {
    content: '';
    position: absolute;
    left: 0; top: 50%;
    transform: translateY(-50%);
    width: 3px; height: 16px;
    background: #409eff;
    border-radius: 2px;
  }
}

:deep(.el-card) {
  margin-bottom: 20px;
}
</style>
