<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.roomNo" placeholder="房间号" clearable style="width:120px" @clear="search" />
          <el-select v-model="queryParams.status" placeholder="入住状态" clearable style="width:130px" @change="search">
            <el-option label="入住中" :value="1" />
            <el-option label="已退房" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
          <el-select v-model="queryParams.paymentStatus" placeholder="支付状态" clearable style="width:130px" @change="search">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="nights" label="天数" width="80" />
        <el-table-column prop="roomFee" label="房费" width="100">
          <template #default="{ row }">¥{{ row.roomFee }}</template>
        </el-table-column>
        <el-table-column prop="otherFee" label="其他费用" width="100">
          <template #default="{ row }">¥{{ row.otherFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="totalFee" label="总费用" width="100">
          <template #default="{ row }">¥{{ row.totalFee || row.roomFee }}</template>
        </el-table-column>
        <el-table-column prop="checkInStatus" label="入住状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.checkInStatus === 1 ? 'primary' : row.checkInStatus === 2 ? 'success' : 'info'" size="small">
              {{ getStatusText(row.checkInStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.paymentStatus === 1 ? '已支付' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
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
import { Search, Refresh } from '@element-plus/icons-vue'
import { getCheckInList } from '@/api/dorm/checkin'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ current: 1, size: 10, roomNo: '', status: null, paymentStatus: null })

function getStatusText(status) { return { 1: '入住中', 2: '已退房', 3: '已取消' }[status] || '-' }
function formatDate(d) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getCheckInList(queryParams)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() { queryParams.current = 1; fetchData() }
function reset() { queryParams.roomNo = ''; queryParams.status = null; queryParams.paymentStatus = null; search() }

onMounted(fetchData)
</script>
