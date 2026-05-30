<!--
  房态图页面（管理端）
  类似酒店前台大屏幕，用颜色区分房间状态
  点击房间可查看详情并快捷操作

  路由：/admin/dorm/room-status
  后端：GET /dorm/room/status-board
-->
<template>
  <div class="room-status-container">
    <!-- ========== 顶部操作栏 ========== -->
    <div class="status-board-header">
      <div class="header-left">
        <h2>房态图</h2>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:160px" @change="fetchData">
          <el-option label="全部" :value="null" />
          <el-option label="空闲" :value="1" />
          <el-option label="入住" :value="2" />
          <el-option label="维护" :value="3" />
          <el-option label="停用" :value="4" />
        </el-select>
      </div>
      <!-- 颜色图例 -->
      <div class="legend">
        <span v-for="item in legends" :key="item.status" class="legend-item">
          <span class="legend-dot" :style="{ background: item.color }"></span>
          {{ item.label }}
        </span>
      </div>
      <!-- 统计数据 -->
      <div class="stats-row">
        <span class="stat-item">共 <strong>{{ rooms.length }}</strong> 间</span>
        <span class="stat-item free">空闲 <strong>{{ counts[1] || 0 }}</strong></span>
        <span class="stat-item occupied">入住 <strong>{{ counts[2] || 0 }}</strong></span>
        <span class="stat-item maintenance">维护 <strong>{{ counts[3] || 0 }}</strong></span>
        <span class="stat-item disabled">停用 <strong>{{ counts[4] || 0 }}</strong></span>
      </div>
      <el-button type="primary" :icon="RefreshRight" @click="fetchData" :loading="loading">刷新</el-button>
    </div>

    <!-- ========== 房间网格（按楼层分组） ========== -->
    <div v-loading="loading">
      <template v-for="floor in floors" :key="floor">
        <div class="floor-label">{{ floor || '未分配楼层' }}</div>
        <div class="room-grid">
          <div
            v-for="room in roomsByFloor[floor]"
            :key="room.id"
            class="room-card"
            :class="roomCardClass(room.status)"
            @click="openDetail(room)"
          >
            <div class="room-card-header">
              <span class="room-no">{{ room.roomNo }}</span>
              <el-tag :type="statusTagType(room.status)" size="small" effect="dark">
                {{ statusText(room.status) }}
              </el-tag>
            </div>
            <div class="room-card-body">
              <p class="room-name">{{ room.roomName || room.roomNo }}</p>
              <p class="room-type" v-if="room.roomTypeId">{{ getRoomTypeName(room.roomTypeId) }}</p>
              <p class="room-price">¥{{ room.price }}/晚</p>
              <p class="room-info">{{ room.floor }} · {{ room.capacity }}/{{ room.maxCapacity }}人</p>
              <div v-if="room.status === 2 && room.guestName" class="guest-brief">
                <span class="guest-name">{{ room.guestName }}</span>
                <span class="guest-date">{{ room.guestCheckInDate }} ~ {{ room.guestCheckOutDate }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-if="rooms.length === 0 && !loading" description="暂无房间数据" />
    </div>

    <!-- ========== 房间详情弹窗 ========== -->
    <el-dialog v-model="detailVisible" :title="`${currentRoom.roomNo} 房间详情`" width="500px" destroy-on-close>
      <div v-if="currentRoom.id" class="detail-content">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="房间号">{{ currentRoom.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="房间名称">{{ currentRoom.roomName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ getRoomTypeName(currentRoom.roomTypeId) }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ currentRoom.floor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ currentRoom.price }}/晚</el-descriptions-item>
          <el-descriptions-item label="容量">{{ currentRoom.capacity }}/{{ currentRoom.maxCapacity }}人</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentRoom.status)">{{ statusText(currentRoom.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设施">{{ currentRoom.facility || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRoom.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 入住客人信息 -->
        <div v-if="currentRoom.status === 2 && currentRoom.guestName" class="guest-detail">
          <h4>当前入住客人</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ currentRoom.guestName }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ currentRoom.guestPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="入住日期">{{ currentRoom.guestCheckInDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预退日期">{{ currentRoom.guestCheckOutDate || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <!-- 空闲 → 办理入住 | 设为维护 | 设为停用 -->
          <template v-if="currentRoom.status === 1">
            <el-button type="success" @click="openCheckin(currentRoom)">办理入住</el-button>
            <el-button type="warning" @click="changeStatus(currentRoom, 3)">设为维护</el-button>
            <el-button type="info" @click="changeStatus(currentRoom, 4)">设为停用</el-button>
          </template>
          <!-- 入住 → 办理退房 -->
          <template v-if="currentRoom.status === 2">
            <el-button type="danger" @click="openCheckout(currentRoom)">办理退房</el-button>
          </template>
          <!-- 维护 → 设为空闲 -->
          <template v-if="currentRoom.status === 3">
            <el-button type="success" @click="changeStatus(currentRoom, 1)">设为空闲</el-button>
            <el-button type="info" @click="changeStatus(currentRoom, 4)">设为停用</el-button>
          </template>
          <!-- 停用 → 设为空闲 | 设为维护 -->
          <template v-if="currentRoom.status === 4">
            <el-button type="success" @click="changeStatus(currentRoom, 1)">设为空闲</el-button>
            <el-button type="warning" @click="changeStatus(currentRoom, 3)">设为维护</el-button>
          </template>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ========== 快捷入住弹窗 ========== -->
    <el-dialog v-model="checkinVisible" title="办理入住" width="500px" destroy-on-close @closed="resetCheckinForm">
      <el-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" label-width="100px">
        <el-form-item label="房间号">
          <el-input :model-value="checkinRoom.roomNo" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="visitorName">
          <el-input v-model="checkinForm.visitorName" placeholder="客人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="checkinForm.phone" placeholder="客人手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="checkinForm.idCard" placeholder="客人身份证号" />
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker v-model="checkinForm.checkInDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker v-model="checkinForm.checkOutDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkinForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkinVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkinSubmitting" @click="submitCheckin">确认入住</el-button>
      </template>
    </el-dialog>

    <!-- ========== 退房确认弹窗 ========== -->
    <el-dialog v-model="checkoutVisible" title="办理退房" width="450px" destroy-on-close>
      <div v-if="checkoutRoom.guestName">
        <p><strong>房间：</strong>{{ checkoutRoom.roomNo }} {{ checkoutRoom.roomName }}</p>
        <p><strong>客人：</strong>{{ checkoutRoom.guestName }} {{ checkoutRoom.guestPhone }}</p>
        <p><strong>入住日期：</strong>{{ checkoutRoom.guestCheckInDate }}</p>
        <p><strong>预退日期：</strong>{{ checkoutRoom.guestCheckOutDate }}</p>
      </div>
      <el-form label-width="100px" style="margin-top:16px">
        <el-form-item label="额外费用">
          <el-input-number v-model="checkoutOtherFee" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="checkoutMethod" style="width:100%">
            <el-option label="微信支付" value="微信支付" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="余额支付" value="余额支付" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="danger" :loading="checkoutSubmitting" @click="submitCheckout">确认退房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getStatusBoard, getRoomTypes, updateRoomStatus } from '@/api/dorm/room'
import { addCheckIn, checkOut } from '@/api/dorm/checkin'

// ==================== 常量 ====================
const STATUS_MAP = { 1: '空闲', 2: '入住', 3: '维护', 4: '停用' }
const STATUS_TAG = { 1: 'success', 2: 'primary', 3: 'warning', 4: 'info' }
const STATUS_CLASS = { 1: 'card-free', 2: 'card-occupied', 3: 'card-maintenance', 4: 'card-disabled' }
const legends = [
  { status: 1, label: '空闲', color: '#67c23a' },
  { status: 2, label: '入住', color: '#409eff' },
  { status: 3, label: '维护', color: '#e6a23c' },
  { status: 4, label: '停用', color: '#909399' }
]

// ==================== 状态 ====================
const loading = ref(false)
const rooms = ref([])
const statusFilter = ref(null)
const detailVisible = ref(false)
const currentRoom = ref({})
const typeList = ref([])

// 快捷入住
const checkinVisible = ref(false)
const checkinSubmitting = ref(false)
const checkinRoom = ref({})
const checkinFormRef = ref()
const checkinForm = reactive({ visitorName: '', phone: '', idCard: '', checkInDate: '', checkOutDate: '', remark: '' })
const checkinRules = {
  visitorName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
  ],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }]
}

// 退房
const checkoutVisible = ref(false)
const checkoutSubmitting = ref(false)
const checkoutRoom = ref({})
const checkoutOtherFee = ref(0)
const checkoutMethod = ref('微信支付')

// ==================== 计算属性 ====================
// 提取所有楼层并排序
const floors = computed(() => {
  const set = new Set(rooms.value.map(r => r.floor).filter(Boolean))
  return Array.from(set).sort((a, b) => {
    const na = parseInt(a), nb = parseInt(b)
    if (!isNaN(na) && !isNaN(nb)) return na - nb
    return a.localeCompare(b)
  })
})

// 按楼层分组
const roomsByFloor = computed(() => {
  const map = {}
  rooms.value.forEach(r => {
    const f = r.floor || '未分配楼层'
    if (!map[f]) map[f] = []
    map[f].push(r)
  })
  return map
})

// 各状态房间数量
const counts = computed(() => {
  const cnt = { 1: 0, 2: 0, 3: 0, 4: 0 }
  rooms.value.forEach(r => { if (cnt[r.status] != null) cnt[r.status]++ })
  return cnt
})

// ==================== 方法 ====================
function statusText(s) { return STATUS_MAP[s] || '-' }
function statusTagType(s) { return STATUS_TAG[s] || 'info' }
function roomCardClass(s) { return STATUS_CLASS[s] || '' }

function getRoomTypeName(id) {
  if (!id) return ''
  const t = typeList.value.find(t => t.id === id)
  return t ? t.name : ''
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStatusBoard(statusFilter.value)
    rooms.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function fetchRoomTypes() {
  try {
    const res = await getRoomTypes()
    typeList.value = res.data || []
  } catch (e) { console.error(e) }
}

// 打开房间详情
function openDetail(room) {
  currentRoom.value = { ...room }
  detailVisible.value = true
}

// 房间状态变更
async function changeStatus(room, status) {
  const label = STATUS_MAP[status]
  try {
    await ElMessageBox.confirm(`确认将 ${room.roomNo} 设为【${label}】？`, '状态变更', { type: 'warning' })
  } catch { return }
  try {
    await updateRoomStatus(room.id, status)
    ElMessage.success(`已设为${label}`)
    detailVisible.value = false
    fetchData()
  } catch (e) { console.error(e) }
}

// 快捷入住
function openCheckin(room) {
  checkinRoom.value = { ...room }
  resetCheckinForm()
  checkinVisible.value = true
}

function resetCheckinForm() {
  checkinForm.visitorName = ''
  checkinForm.phone = ''
  checkinForm.idCard = ''
  checkinForm.checkInDate = ''
  checkinForm.checkOutDate = ''
  checkinForm.remark = ''
}

async function submitCheckin() {
  if (!checkinFormRef.value) return
  await checkinFormRef.value.validate(async (valid) => {
    if (!valid) return
    checkinSubmitting.value = true
    try {
      await addCheckIn({ ...checkinForm, roomId: checkinRoom.value.id })
      ElMessage.success('入住办理成功')
      checkinVisible.value = false
      detailVisible.value = false
      fetchData()
    } catch (e) { console.error(e) }
    finally { checkinSubmitting.value = false }
  })
}

// 退房
function openCheckout(room) {
  checkoutRoom.value = { ...room }
  checkoutOtherFee.value = 0
  checkoutMethod.value = '微信支付'
  checkoutVisible.value = true
}

async function submitCheckout() {
  checkoutSubmitting.value = true
  try {
    await checkOut(checkoutRoom.value.checkInId, checkoutOtherFee.value, checkoutMethod.value)
    ElMessage.success('退房成功')
    checkoutVisible.value = false
    detailVisible.value = false
    fetchData()
  } catch (e) { console.error(e) }
  finally { checkoutSubmitting.value = false }
}

// ==================== 生命周期 ====================
onMounted(() => {
  fetchRoomTypes()
  fetchData()
})
</script>

<style lang="scss" scoped>
.room-status-container { padding: 16px; animation: fadeIn 0.4s ease; }

.status-board-header {
  display: flex; align-items: center; gap: 20px; flex-wrap: wrap;
  margin-bottom: 20px; padding: 16px;
  background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,.06);
  .header-left { display: flex; align-items: center; gap: 12px; h2 { margin: 0; font-size: 20px; } }
  .legend { display: flex; gap: 16px; }
  .legend-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }
  .legend-dot { width: 12px; height: 12px; border-radius: 3px; display: inline-block; }
  .stats-row { display: flex; gap: 16px; }
  .stat-item { font-size: 13px; color: #606266; strong { font-size: 16px; margin: 0 2px; } }
  .stat-item.free strong { color: #67c23a; }
  .stat-item.occupied strong { color: #409eff; }
  .stat-item.maintenance strong { color: #e6a23c; }
  .stat-item.disabled strong { color: #909399; }
}

.floor-label {
  font-size: 15px; font-weight: 600; color: #303133;
  margin: 16px 0 10px; padding-left: 8px;
  border-left: 3px solid #409eff;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.room-card {
  padding: 16px; border-radius: 8px; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 2px solid transparent;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.12); }
  &.card-free { border-color: #67c23a; background: #f0f9eb; }
  &.card-occupied { border-color: #409eff; background: #ecf5ff; }
  &.card-maintenance { border-color: #e6a23c; background: #fdf6ec; }
  &.card-disabled { border-color: #909399; background: #f4f4f5; opacity: 0.75; }
  .room-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
  .room-no { font-size: 18px; font-weight: 700; color: #303133; }
  .room-card-body { font-size: 13px; color: #606266; p { margin: 3px 0; } }
  .room-name { font-weight: 600; color: #303133; }
  .room-type { color: #909399; }
  .room-price { color: #f56c6c; font-weight: 600; font-size: 15px; }
  .room-info { color: #909399; }
  .guest-brief {
    margin-top: 8px; padding-top: 8px; border-top: 1px dashed #dcdfe6;
    display: flex; flex-direction: column; gap: 2px;
    .guest-name { font-weight: 600; color: #303133; }
    .guest-date { font-size: 12px; color: #909399; }
  }
}

.detail-content {
  .guest-detail { margin-top: 16px; h4 { margin: 0 0 8px; font-size: 14px; color: #303133; } }
}

.dialog-actions { display: flex; gap: 8px; justify-content: flex-end; flex-wrap: wrap; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }
</style>
