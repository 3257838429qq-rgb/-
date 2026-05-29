<!--
  访客预约管理组件（管理端）

  【模块说明】
  - 管理端访客预约管理页面
  - 支持查看和审核预约申请

  【功能模块】
  1. 搜索栏：按姓名、电话、状态筛选
  2. 预约列表：显示所有预约记录
  3. 新增预约弹窗：录入访客预约
  4. 审核弹窗：审核通过/拒绝
  5. 详情弹窗：查看预约详情

  【API调用】
  - getVisitorList: 获取预约列表
  - addVisitor: 新增预约
  - reviewVisitor: 审核预约

  【后端对应】
  - Controller: VisitorController
  - 路径: /visitor

  【路由对应】
  - /admin/visitor/reservation
-->
<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">线上来访预约</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增预约</el-button>
        </div>
      </template>
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #title>
          <span>预约说明：访客可通过本系统提交来访预约申请，预约提交后将由管理员进行审核，审核通过后方可到访。</span>
        </template>
      </el-alert>

      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.name" placeholder="访客姓名" clearable style="width:140px" @clear="search" />
          <el-input v-model="queryParams.phone" placeholder="联系电话" clearable style="width:140px" @clear="search" />
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:130px" @change="search">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="name" label="访客姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="company" label="来访单位" min-width="150" />
        <el-table-column prop="visitPurpose" label="来访目的" min-width="150" show-overflow-tooltip />
        <el-table-column prop="visitDate" label="来访日期" width="120" />
        <el-table-column prop="visitTimeSlot" label="来访时段" width="100" />
        <el-table-column prop="hostPerson" label="接待人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" v-if="row.status === 0" @click="handleReview(row)">审核</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增预约" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="访客姓名" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来访单位"><el-input v-model="form.company" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来访日期" prop="visitDate"><el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来访时段" prop="visitTimeSlot"><el-select v-model="form.visitTimeSlot" style="width:100%"><el-option label="上午" value="上午" /><el-option label="下午" value="下午" /><el-option label="全天" value="全天" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接待人"><el-input v-model="form.hostPerson" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接待人电话"><el-input v-model="form.hostPersonPhone" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="来访目的" prop="visitPurpose"><el-input v-model="form.visitPurpose" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="车牌号"><el-input v-model="form.carPlate" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="预约详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="访客姓名">{{ currentVisitor.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentVisitor.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentVisitor.idCard }}</el-descriptions-item>
        <el-descriptions-item label="来访单位">{{ currentVisitor.company }}</el-descriptions-item>
        <el-descriptions-item label="来访目的" :span="2">{{ currentVisitor.visitPurpose }}</el-descriptions-item>
        <el-descriptions-item label="来访日期">{{ currentVisitor.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="来访时段">{{ currentVisitor.visitTimeSlot }}</el-descriptions-item>
        <el-descriptions-item label="接待人">{{ currentVisitor.hostPerson }}</el-descriptions-item>
        <el-descriptions-item label="接待人电话">{{ currentVisitor.hostPersonPhone }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentVisitor.carPlate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentVisitor.remark }}</el-descriptions-item>
        <el-descriptions-item label="预约时间" :span="2">{{ formatDate(currentVisitor.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2">{{ currentVisitor.reviewRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="审核预约" width="450px">
      <el-form label-width="80">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注"><el-input v-model="reviewForm.remark" type="textarea" :rows="3" /></el-form-item>
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
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getVisitorList, addVisitor, reviewVisitor } from '@/api/visitor'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const reviewVisible = ref(false)
const currentVisitor = ref({})
const formRef = ref()

const queryParams = reactive({ current: 1, size: 10, name: '', phone: '', status: null })
const form = reactive({ name: '', phone: '', idCard: '', company: '', visitPurpose: '', visitDate: '', visitTimeSlot: '', hostPerson: '', hostPersonPhone: '', carPlate: '', remark: '' })
const reviewForm = reactive({ id: null, status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
  ],
  visitDate: [{ required: true, message: '请选择来访日期', trigger: 'change' }]
}

function getStatusType(status) { return { 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info' }
function getStatusText(status) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '-' }
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
  Object.assign(form, { name: '', phone: '', idCard: '', company: '', visitPurpose: '', visitDate: '', visitTimeSlot: '', hostPerson: '', hostPersonPhone: '', carPlate: '', remark: '' })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const res = await addVisitor(form)
    if (res.code === 200) { ElMessage.success('预约提交成功，等待审核'); dialogVisible.value = false; fetchData() }
  })
}

function handleView(row) { currentVisitor.value = row; viewVisible.value = true }

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

onMounted(fetchData)
</script>
