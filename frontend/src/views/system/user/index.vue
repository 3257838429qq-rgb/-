<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.username" placeholder="用户名" clearable style="width:160px" @clear="search" />
          <el-input v-model="queryParams.realName" placeholder="姓名" clearable style="width:160px" @clear="search" />
          <el-select v-model="queryParams.roleId" placeholder="角色" clearable style="width:140px" @change="search">
            <el-option label="超级管理员" :value="1" />
            <el-option label="校园接待员" :value="2" />
            <el-option label="招待所宿管" :value="3" />
          </el-select>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:120px" @change="search">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
        <div class="action-bar">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="roleId" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.roleId === 1 ? 'danger' : row.roleId === 2 ? 'primary' : 'success'" size="small">
              {{ getRoleName(row.roleId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '不修改请留空' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width:100%">
            <el-option label="超级管理员" :value="1" />
            <el-option label="校园接待员" :value="2" />
            <el-option label="招待所宿管" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, updateUserStatus } from '@/api/system/user'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const queryParams = reactive({
  current: 1, size: 10,
  username: '', realName: '', roleId: null, status: null
})

const form = reactive({
  id: null, username: '', password: '', realName: '', phone: '', email: '', roleId: null, status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function getRoleName(roleId) {
  const map = { 1: '超级管理员', 2: '校园接待员', 3: '招待所宿管' }
  return map[roleId] || '-'
}

function formatDate(date) {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() {
  queryParams.current = 1
  fetchData()
}

function reset() {
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.roleId = null
  queryParams.status = null
  search()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', roleId: null, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = isEdit.value ? await updateUser(form) : await addUser(form)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
      }
    } catch (e) { console.error(e) }
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
  const res = await deleteUser(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  }
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm('确定要重置该用户密码为123456吗？', '提示', { type: 'warning' })
  const res = await resetPassword(row.id)
  if (res.code === 200) ElMessage.success('密码已重置为123456')
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateUserStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
}

onMounted(fetchData)
</script>
