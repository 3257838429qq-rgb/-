<!--
  房间管理组件（管理端）

  【模块说明】
  - 管理端房间信息管理页面
  - 支持房间CRUD和状态管理

  【功能模块】
  1. 搜索栏：按房间号、名称、楼层、状态筛选
  2. 房间列表：显示房间信息
  3. 新增/编辑弹窗：房间信息维护
  4. 状态快捷操作：设为空闲、维护

  【房间状态】
  - 1: 空闲
  - 2: 入住
  - 3: 维护
  - 4: 停用

  【API调用】
  - getRoomList: 获取房间列表
  - addRoom: 新增房间
  - updateRoom: 修改房间
  - updateRoomStatus: 修改房间状态
  - getRoomTypeList: 获取房型列表

  【后端对应】
  - Controller: DormRoomController
  - 路径: /dorm/room/page, /dorm/room, /dorm/room/status

  【路由对应】
  - /admin/dorm/room
-->
<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.roomNo" placeholder="房间号" clearable style="width:120px" @clear="search" />
          <el-input v-model="queryParams.roomName" placeholder="房间名称" clearable style="width:140px" @clear="search" />
          <el-select v-model="queryParams.floor" placeholder="楼层" clearable style="width:120px" @change="search">
            <el-option label="1楼" value="1楼" />
            <el-option label="2楼" value="2楼" />
            <el-option label="3楼" value="3楼" />
          </el-select>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:120px" @change="search">
            <el-option label="空闲" :value="1" />
            <el-option label="入住" :value="2" />
            <el-option label="维护" :value="3" />
            <el-option label="停用" :value="4" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>
        <div class="action-bar">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增房间</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="roomName" label="房间名称" width="120" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column label="房型" width="100">
          <template #default="{ row }">{{ getRoomTypeName(row.roomTypeId) }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="可住人数" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}/晚</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="facility" label="设施" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" v-if="row.status !== 1" @click="setStatus(row, 1)">设为空闲</el-button>
            <el-button type="warning" link size="small" v-if="row.status !== 3" @click="setStatus(row, 3)">维护</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房间' : '新增房间'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="房间号" prop="roomNo"><el-input v-model="form.roomNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房间名称" prop="roomName"><el-input v-model="form.roomName" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="房型" prop="roomTypeId">
              <el-select v-model="form.roomTypeId" style="width:100%">
                <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="楼层" prop="floor"><el-select v-model="form.floor" style="width:100%"><el-option label="1楼" value="1楼" /><el-option label="2楼" value="2楼" /><el-option label="3楼" value="3楼" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="可住人数" prop="capacity"><el-input-number v-model="form.capacity" :min="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房价/晚" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="设施"><el-input v-model="form.facility" placeholder="多个设施用|分隔，如: 空调|电视|宽带" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">空闲</el-radio>
            <el-radio :label="2">入住</el-radio>
            <el-radio :label="3">维护</el-radio>
            <el-radio :label="4">停用</el-radio>
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
import { getRoomList, addRoom, updateRoom, updateRoomStatus } from '@/api/dorm/room'
import { getRoomTypeList as getRoomTypes } from '@/api/dorm/roomType'

const loading = ref(false)
const tableData = ref([])
const roomTypes = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const queryParams = reactive({ current: 1, size: 10, roomNo: '', roomName: '', floor: '', status: null })
const form = reactive({ id: null, roomNo: '', roomName: '', roomTypeId: null, floor: '', capacity: 1, maxCapacity: 2, price: 100, facility: '', remark: '', status: 1 })
const rules = { roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }], roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }] }

function getStatusType(status) { return { 1: 'success', 2: 'primary', 3: 'warning', 4: 'info' }[status] || 'info' }
function getStatusText(status) { return { 1: '空闲', 2: '入住', 3: '维护', 4: '停用' }[status] || '-' }
function getRoomTypeName(id) { return roomTypes.value.find(t => t.id === id)?.name || '-' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoomList(queryParams)
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function fetchRoomTypes() {
  const res = await getRoomTypes()
  if (res.code === 200) roomTypes.value = res.data
}

function search() { queryParams.current = 1; fetchData() }
function reset() { queryParams.roomNo = ''; queryParams.roomName = ''; queryParams.floor = ''; queryParams.status = null; search() }

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, roomNo: '', roomName: '', roomTypeId: null, floor: '', capacity: 1, maxCapacity: 2, price: 100, facility: '', remark: '', status: 1 })
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
    const res = isEdit.value ? await updateRoom(form) : await addRoom(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; fetchData() }
  })
}

async function setStatus(row, status) {
  await ElMessageBox.confirm(`确定要将房间${row.roomNo}设置为"${getStatusText(status)}"吗？`, '提示', { type: 'warning' })
  const res = await updateRoomStatus(row.id, status)
  if (res.code === 200) { ElMessage.success('状态更新成功'); fetchData() }
}

onMounted(() => { fetchData(); fetchRoomTypes() })
</script>
