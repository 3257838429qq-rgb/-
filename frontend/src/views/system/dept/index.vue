<!--
  部门管理组件（管理端）

  【模块说明】
  - 管理端部门管理页面
  - 支持部门的增删改和树形结构展示

  【功能模块】
  1. 部门树形列表
  2. 新增/编辑部门弹窗
  3. 子部门创建

  【API调用】
  - getDeptList: 获取部门列表
  - addDept: 新增部门
  - updateDept: 修改部门
  - deleteDept: 删除部门

  【后端对应】
  - Controller: DeptController
  - 路径: /system/dept

  【路由对应】
  - /admin/system/dept
-->
<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar"></div>
        <div class="action-bar">
          <el-button type="primary" :icon="Plus" @click="() => { parentId = 0; dialogVisible = true }">新增部门</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" row-key="id" default-expand-all>
        <el-table-column prop="name" label="部门名称" min-width="180" />
        <el-table-column prop="code" label="部门编码" width="150" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAddChild(row)">新增子部门</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDeptList, addDept, updateDept, deleteDept } from '@/api/system/dept'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const parentId = ref(0)
const parentName = ref('无（顶级部门）')
const formRef = ref()

const form = reactive({ id: null, name: '', code: '', parentId: 0, leader: '', phone: '', orderNum: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDeptList()
    if (res.code === 200) tableData.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function handleAddChild(row) {
  parentId.value = row.id
  parentName.value = row.name
  isEdit.value = false
  Object.assign(form, { id: null, name: '', code: '', parentId: row.id, leader: '', phone: '', orderNum: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  parentId.value = row.parentId
  parentName.value = row.parentId === 0 ? '无（顶级部门）' : tableData.value.find(d => d.id === row.parentId)?.name || '无'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const res = isEdit.value ? await updateDept(form) : await addDept(form)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    }
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该部门吗？', '提示', { type: 'warning' })
  const res = await deleteDept(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchData()
  }
}

onMounted(fetchData)
</script>
