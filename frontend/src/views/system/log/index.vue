<!--
  操作日志管理组件（管理端）

  【模块说明】
  - 管理端操作日志查询页面
  - 显示用户在系统中的操作记录

  【功能模块】
  1. 日志列表：按标题、类型筛选
  2. 详情弹窗：查看请求参数和返回结果

  【API调用】
  - getLogList: 获取日志列表

  【后端对应】
  - Controller: SysOperLogController
  - 路径: /system/oper-log

  【路由对应】
  - /admin/system/log
-->
<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.title" placeholder="操作标题" clearable style="width:180px" @clear="search" />
          <el-select v-model="queryParams.operType" placeholder="操作类型" clearable style="width:140px" @change="search">
            <el-option label="新增" value="新增" />
            <el-option label="修改" value="修改" />
            <el-option label="删除" value="删除" />
            <el-option label="登录" value="登录" />
            <el-option label="审核" value="审核" />
            <el-option label="登记入住" value="登记入住" />
            <el-option label="退房结算" value="退房结算" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="title" label="操作标题" min-width="150" />
        <el-table-column prop="operType" label="操作类型" width="100" />
        <el-table-column prop="method" label="方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="100" />
        <el-table-column prop="operUrl" label="请求地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operIp" label="操作IP" width="130" />
        <el-table-column prop="costTime" label="耗时" width="80">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170">
          <template #default="{ row }">{{ formatDate(row.operTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="操作详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作标题">{{ currentLog.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operType }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ currentLog.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.operIp }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 1 ? '正常' : '异常' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
          <span style="color:#f56c6c">{{ currentLog.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="max-height:200px;overflow:auto;margin:0">{{ formatJson(currentLog.operParam) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre style="max-height:200px;overflow:auto;margin:0">{{ formatJson(currentLog.result) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getLogList } from '@/api/system/log'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref({})

const queryParams = reactive({ current: 1, size: 10, title: '', operType: '' })

function formatDate(date) { return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-' }
function formatJson(str) {
  if (!str) return '-'
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getLogList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() { queryParams.current = 1; fetchData() }
function reset() { queryParams.title = ''; queryParams.operType = ''; search() }
function viewDetail(row) { currentLog.value = row; detailVisible.value = true }

onMounted(fetchData)
</script>

<style lang="scss" scoped>
pre {
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
