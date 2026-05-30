<!--
  管理端首页/仪表盘组件

  【模块说明】
  - 显示招待所的运营数据概览
  - 包含统计卡片、图表、当前入住列表

  【功能模块】
  1. 统计卡片：入住中、空闲房间、总房间数
  2. 房间状态饼图：显示空闲/入住/维护/停用分布
  3. 入住率仪表盘：显示当前入住率百分比
  4. 当前入住列表：显示最新入住记录

  【API调用】
  - getStatistics: 获取统计数据
  - getActiveCheckIns: 获取当前入住列表
  - getRoomList: 获取房间列表（用于图表）

  【后端对应】
  - Controller: CheckInController
  - 路径: /dorm/checkin/statistics, /dorm/checkin/active
  - Controller: DormRoomController
  - 路径: /dorm/room/page

  【路由对应】
  - /admin/dashboard
-->
<template>
  <div class="dashboard">
    <!-- Stat cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-purple">
          <div class="stat-icon"><el-icon :size="28"><User /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.activeCheckIns }}</p>
            <p class="stat-label">入住中</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-green">
          <div class="stat-icon"><el-icon :size="28"><House /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.availableRooms }}</p>
            <p class="stat-label">空闲房间</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-gray">
          <div class="stat-icon"><el-icon :size="28"><Tickets /></el-icon></div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.totalRooms }}</p>
            <p class="stat-label">总房间数</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="chart-row">
      <el-col :lg="15" :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">房间状态分布</span>
          </template>
          <div ref="roomChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :lg="9" :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">入住率统计</span>
          </template>
          <div ref="occupancyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Active checkins -->
    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">当前入住列表</span>
              <el-button type="primary" size="default" :icon="Plus" @click="$router.push('/admin/dorm/checkin')">
                新增入住
              </el-button>
            </div>
          </template>
          <el-table :data="activeCheckIns" stripe v-loading="tableLoading" empty-text="暂无入住记录">
            <el-table-column prop="orderNo" label="订单号" width="160" />
            <el-table-column prop="roomNo" label="房间号" width="100" />
            <el-table-column label="访客" min-width="120">
              <template #default="{ row }">
                <div class="visitor-cell">
                  <el-avatar :size="28" class="visitor-avatar">
                    {{ (row.visitorName || 'V').charAt(0) }}
                  </el-avatar>
                  <span>{{ row.visitorName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="checkInDate" label="入住日期" width="120">
              <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
            </el-table-column>
            <el-table-column prop="checkOutDate" label="退房日期" width="120">
              <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
            </el-table-column>
            <el-table-column prop="nights" label="天数" width="80" align="center" />
            <el-table-column label="费用" width="120">
              <template #default="{ row }">
                <span class="fee">¥{{ (row.totalFee || row.roomFee || 0).toFixed ? (row.totalFee || row.roomFee || 0) : row.totalFee || row.roomFee }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentStatus" label="支付" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small" effect="plain">
                  {{ row.paymentStatus === 1 ? '已付' : '待付' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { User, House, Tickets, Plus } from '@element-plus/icons-vue'
import { getStatistics, getActiveCheckIns } from '@/api/dorm/checkin'
import { getRoomList } from '@/api/dorm/room'

const stats = ref({
  activeCheckIns: 0,
  availableRooms: 0,
  totalRooms: 0
})

const activeCheckIns = ref([])
const tableLoading = ref(false)
let roomChart = null
let occupancyChart = null
const roomChartRef = ref()
const occupancyChartRef = ref()

function formatDate(date) {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

async function fetchStats() {
  try {
    const res = await getStatistics()
    if (res.code === 200) stats.value = res.data
  } catch (e) { console.error(e) }
}

async function fetchActiveCheckIns() {
  tableLoading.value = true
  try {
    const res = await getActiveCheckIns({ current: 1, size: 10 })
    if (res.code === 200) activeCheckIns.value = res.data.records || []
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

function initRoomChart(roomData) {
  if (!roomChartRef.value) return
  roomChart = echarts.init(roomChartRef.value)
  const count = (s) => roomData.filter(r => r.status === s).length

  roomChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}间 ({d}%)'
    },
    legend: {
      bottom: 0,
      textStyle: { fontSize: 12, color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['50%', '48%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        position: 'outside',
        formatter: (p) => {
          // 入住：按可入住房间（空闲+入住）为分母计算百分比，与入住率口径一致
          if (p.name === '入住') {
            const rentableTotal = count(1) + count(2)
            const rate = rentableTotal > 0 ? (count(2) * 100 / rentableTotal).toFixed(1) : '0'
            return `入住\n${rate}%`
          }
          return `${p.name}\n${p.percent}%`
        },
        fontSize: 12
      },
      emphasis: {
        label: { fontSize: 16, fontWeight: 'bold' },
        scaleSize: 8
      },
      data: [
        { value: count(1), name: '空闲', itemStyle: { color: '#67c23a' } },
        { value: count(2), name: '入住', itemStyle: { color: '#409eff' } },
        { value: count(3), name: '维护', itemStyle: { color: '#e6a23c' } },
        { value: count(4), name: '停用', itemStyle: { color: '#c0c4cc' } }
      ]
    }]
  })
}

function initOccupancyChart() {
  if (!occupancyChartRef.value) return
  occupancyChart = echarts.init(occupancyChartRef.value)
  const rate = stats.value.occupancyRate ? stats.value.occupancyRate.toFixed(1) : 0

  occupancyChart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      center: ['50%', '55%'],
      radius: '90%',
      min: 0,
      max: 100,
      splitNumber: 10,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#67c23a' },
            { offset: 0.5, color: '#409eff' },
            { offset: 1, color: '#f56c6c' }
          ]
        }
      },
      progress: {
        show: true,
        width: 14,
        roundCap: true
      },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 14, color: [[1, '#e4e7ed']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: {
        show: true,
        offsetCenter: [0, '30%'],
        fontSize: 13,
        color: '#909399'
      },
      detail: {
        valueAnimation: true,
        offsetCenter: [0, '-5%'],
        fontSize: 40,
        fontWeight: 'bold',
        formatter: '{value}%',
        color: '#303133'
      },
      data: [{ value: parseFloat(rate), name: '入住率' }]
    }]
  })
}

async function fetchRoomData() {
  try {
    const res = await getRoomList({ current: 1, size: 100 })
    if (res.code === 200) initRoomChart(res.data.records || [])
  } catch (e) { console.error(e) }
}

function resizeCharts() {
  roomChart?.resize()
  occupancyChart?.resize()
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchActiveCheckIns(), fetchRoomData()])
  initOccupancyChart()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  roomChart?.dispose()
  occupancyChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    margin-bottom: 20px;
  }

  .chart-row {
    margin-bottom: 20px;
  }

  .chart-card, .table-card {
    margin-bottom: 0;
  }

  .chart-container {
    width: 100%;
    height: 340px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text);
    position: relative;
    padding-left: 12px;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      background: var(--primary);
      border-radius: 2px;
    }
  }

  .visitor-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .visitor-avatar {
      background: linear-gradient(135deg, #409eff, #337ecc);
      color: #fff;
      font-size: 12px;
      font-weight: 600;
    }
  }

  .fee {
    color: #f56c6c;
    font-weight: 600;
  }

  :deep(.el-card__header) {
    padding: 16px 20px;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}
</style>
