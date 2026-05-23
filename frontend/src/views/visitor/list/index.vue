<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.name" placeholder="访客姓名" clearable style="width:140px" @clear="search" />
          <el-input v-model="queryParams.phone" placeholder="联系电话" clearable style="width:140px" @clear="search" />
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:130px" @change="search">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已到访" :value="3" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">录入访客</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="访客姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="company" label="来访单位" min-width="150" />
        <el-table-column prop="visitPurpose" label="来访目的" min-width="150" show-overflow-tooltip />
        <el-table-column prop="visitDate" label="来访日期" width="120">
          <template #default="{ row }">{{ row.visitDate }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hostPerson" label="接待人" width="100" />
        <el-table-column prop="createTime" label="录入时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" v-if="row.status === 0" @click="handleReview(row)">审核</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑访客' : '录入访客'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="访客姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来访单位" prop="company"><el-input v-model="form.company" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来访日期" prop="visitDate"><el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来访时段" prop="visitTimeSlot">
              <el-select v-model="form.visitTimeSlot" style="width:100%">
                <el-option label="上午" value="上午" />
                <el-option label="下午" value="下午" />
                <el-option label="全天" value="全天" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接待人" prop="hostPerson"><el-input v-model="form.hostPerson" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接待人电话" prop="hostPersonPhone"><el-input v-model="form.hostPersonPhone" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="来访目的" prop="visitPurpose"><el-input v-model="form.visitPurpose" type="textarea" :rows="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车牌号"><el-input v-model="form.carPlate" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="审核访客" width="450px">
      <el-form label-width="80">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="reviewForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getVisitorList, addVisitor, updateVisitor, deleteVisitor, reviewVisitor } from '@/api/visitor'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const reviewVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const queryParams = reactive({ current: 1, size: 10, name: '', phone: '', status: null })

const form = reactive({
  id: null, name: '', phone: '', idCard: '', company: '', visitPurpose: '',
  visitDate: '', visitTimeSlot: '', hostPerson: '', hostPersonPhone: '',
  carPlate: '', remark: ''
})

const reviewForm = reactive({ id: null, status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function getStatusType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'primary' }[status] || 'info'
}

function getStatusText(status) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已到访' }[status] || '-'
}

function formatDate(date) { return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getVisitorList(queryParams)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() { queryParams.current = 1; fetchData() }
function reset() { queryParams.name = ''; queryParams.phone = ''; queryParams.status = null; search() }

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', phone: '', idCard: '', company: '', visitPurpose: '', visitDate: '', visitTimeSlot: '', hostPerson: '', hostPersonPhone: '', carPlate: '', remark: '' })
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
    const res = isEdit.value ? await updateVisitor(form) : await addVisitor(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '录入成功'); dialogVisible.value = false; fetchData() }
  })
}

function handleReview(row) {
  reviewForm.id = row.id
  reviewForm.status = 1
  reviewForm.remark = ''
  reviewVisible.value = true
}

async function submitReview() {
  const res = await reviewVisitor(reviewForm.id, reviewForm.status, reviewForm.remark)
  if (res.code === 200) { ElMessage.success('审核完成'); reviewVisible.value = false; fetchData() }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该访客记录吗？', '提示', { type: 'warning' })
  const res = await deleteVisitor(row.id)
  if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
}

onMounted(fetchData)
</script>
