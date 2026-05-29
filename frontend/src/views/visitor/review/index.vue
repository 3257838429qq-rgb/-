<!--
  访客预约审核组件（管理端）

  【模块说明】
  - 管理端访客预约审核页面
  - 支持查看和审核预约申请

  【功能模块】
  1. 状态筛选：全部、待审核、已通过、已拒绝
  2. 预约列表：显示预约信息
  3. 审核弹窗：审核通过/拒绝
  4. 详情弹窗：查看预约详情

  【API调用】
  - getVisitorList: 获取预约列表
  - reviewVisitor: 审核预约

  【后端对应】
  - Controller: VisitorController
  - 路径: /visitor/page, /visitor/review

  【路由对应】
  - /admin/visitor/review
-->
<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">来访预约审核</span>
          <el-radio-group v-model="statusFilter" @change="fetchData">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="0">待审核</el-radio-button>
            <el-radio-button :label="1">已通过</el-radio-button>
            <el-radio-button :label="2">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="name" label="访客姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="company" label="来访单位" min-width="150" />
        <el-table-column prop="visitPurpose" label="来访目的" min-width="150" show-overflow-tooltip />
        <el-table-column prop="visitDate" label="来访日期" width="120" />
        <el-table-column prop="visitTimeSlot" label="时段" width="80" />
        <el-table-column prop="hostPerson" label="接待人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="审核时间" width="170">
          <template #default="{ row }">{{ formatDate(row.reviewTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" v-if="row.status === 0" @click="handleReview(row)">审核</el-button>
            <el-button type="info" link size="small" v-else @click="handleView(row)">查看</el-button>
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

    <el-dialog v-model="reviewVisible" title="审核预约" width="600px">
      <el-descriptions :column="2" border style="margin-bottom:20px">
        <el-descriptions-item label="访客姓名">{{ currentVisitor.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentVisitor.phone }}</el-descriptions-item>
        <el-descriptions-item label="来访单位">{{ currentVisitor.company }}</el-descriptions-item>
        <el-descriptions-item label="来访日期">{{ currentVisitor.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="来访时段">{{ currentVisitor.visitTimeSlot }}</el-descriptions-item>
        <el-descriptions-item label="接待人">{{ currentVisitor.hostPerson }}</el-descriptions-item>
        <el-descriptions-item label="来访目的" :span="2">{{ currentVisitor.visitPurpose }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentVisitor.remark }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">
              <el-tag type="success">通过</el-tag>
            </el-radio>
            <el-radio :label="2">
              <el-tag type="danger">拒绝</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注"><el-input v-model="reviewForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确定审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="预约详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="访客姓名">{{ currentVisitor.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentVisitor.phone }}</el-descriptions-item>
        <el-descriptions-item label="来访单位">{{ currentVisitor.company }}</el-descriptions-item>
        <el-descriptions-item label="来访日期">{{ currentVisitor.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="来访目的" :span="2">{{ currentVisitor.visitPurpose }}</el-descriptions-item>
        <el-descriptions-item label="接待人">{{ currentVisitor.hostPerson }}</el-descriptions-item>
        <el-descriptions-item label="接待人电话">{{ currentVisitor.hostPersonPhone }}</el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2">{{ currentVisitor.reviewRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getVisitorList, reviewVisitor } from '@/api/visitor'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const statusFilter = ref(0)
const reviewVisible = ref(false)
const viewVisible = ref(false)
const currentVisitor = ref({})

const queryParams = reactive({ current: 1, size: 10 })

function getStatusType(status) { return { 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info' }
function getStatusText(status) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '-' }
function formatDate(date) { return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchData() {
  loading.value = true
  const params = { ...queryParams }
  if (statusFilter.value !== null) params.status = statusFilter.value
  try {
    const res = await getVisitorList(params)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function handleReview(row) {
  currentVisitor.value = row
  reviewForm.id = row.id
  reviewForm.status = 1
  reviewForm.remark = ''
  reviewVisible.value = true
}

function handleView(row) { currentVisitor.value = row; viewVisible.value = true }

const reviewForm = reactive({ id: null, status: 1, remark: '' })

async function submitReview() {
  const res = await reviewVisitor(reviewForm.id, reviewForm.status, reviewForm.remark)
  if (res.code === 200) { ElMessage.success('审核完成'); reviewVisible.value = false; fetchData() }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
