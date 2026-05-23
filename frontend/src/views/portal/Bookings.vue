<template>
  <div class="portal-bookings">
    <div class="page-header">
      <h2>我的预订</h2>
      <p>查看您提交的所有入住申请和住宿记录</p>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无预订记录">
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="roomNo" label="房间号" width="90" />
        <el-table-column prop="checkInDate" label="入住日期" width="120">
          <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
        </el-table-column>
        <el-table-column prop="checkOutDate" label="退房日期" width="120">
          <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
        </el-table-column>
        <el-table-column prop="nights" label="天数" width="70" align="center" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="fee">¥{{ row.roomFee || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInStatus" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.checkInStatus)" size="small" effect="plain">
              {{ statusText(row.checkInStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small" effect="plain">
              {{ row.paymentStatus === 1 ? '已付' : '待付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyBookings } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

function statusTag(s) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }[s] || 'info'
}

function statusText(s) {
  return { 0: '待审核', 1: '已通过', 2: '已退房', 3: '已拒绝' }[s] || '-'
}

function formatDate(d) { return d ? dayjs(d).format('YYYY-MM-DD') : '-' }
function formatDateTime(d) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyBookings(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.portal-bookings {
  animation: fadeIn 0.4s ease;
}

.page-header {
  margin-bottom: 24px;

  h2 { font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 6px; }
  p { font-size: 14px; color: #909399; }
}

.fee { color: #f56c6c; font-weight: 600; }
</style>
