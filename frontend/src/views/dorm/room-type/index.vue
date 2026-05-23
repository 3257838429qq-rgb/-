<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar"></div>
        <div class="action-bar">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增房型</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="房型名称" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="basePrice" label="价格(元/晚)" width="120">
          <template #default="{ row }">¥{{ row.basePrice }}</template>
        </el-table-column>
        <el-table-column prop="bedCount" label="床位数" width="100" />
        <el-table-column prop="facility" label="设施配置" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房型' : '新增房型'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="房型名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="基础价格" prop="basePrice"><el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="床位数量" prop="bedCount"><el-input-number v-model="form.bedCount" :min="1" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="设施配置"><el-input v-model="form.facility" placeholder="多个设施用逗号分隔，如: 空调,电视,宽带" /></el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { getRoomTypeList, addRoomType, updateRoomType, deleteRoomType } from '@/api/dorm/roomType'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({ id: null, name: '', description: '', basePrice: 100, bedCount: 1, facility: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoomTypeList()
    if (res.code === 200) tableData.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', description: '', basePrice: 100, bedCount: 1, facility: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const res = isEdit.value ? await updateRoomType(form) : await addRoomType(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; fetchData() }
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该房型吗？', '提示', { type: 'warning' })
  const res = await deleteRoomType(row.id)
  if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
}

onMounted(fetchData)
</script>
