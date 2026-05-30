<!--
  日历预订页面（用户端）
  选择日期范围 → 查询可用房间 → 预订

  路由：/portal/calendar
  后端：GET /dorm/room/available-by-date
        POST /dorm/checkin/booking
-->
<template>
  <div class="portal-calendar">
    <div class="page-header">
      <h2>日历预订</h2>
      <p>选择入住和退房日期，查看可预订的房间</p>
    </div>

    <!-- ========== 日期选择区 ========== -->
    <div class="search-bar">
      <div class="date-range-box">
        <span class="search-label">选择日期范围：</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="退房日期"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          style="width:320px"
        />
        <el-button type="primary" :icon="Search" :loading="loading" @click="searchAvailable">
          查询可用房间
        </el-button>
      </div>
      <div class="filter-extra">
        <el-input v-model="keyword" placeholder="搜索房间号/名称" clearable style="width:200px" @input="filterRooms" />
        <el-select v-model="typeFilter" placeholder="全部房型" clearable style="width:160px" @change="filterRooms">
          <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-input-number v-model="priceMax" :min="0" placeholder="最高价格" style="width:140px" @change="filterRooms" />
      </div>
    </div>

    <!-- ========== 查询结果 ========== -->
    <div v-if="searched" class="result-section">
      <div class="result-header">
        <h3>
          共找到 <span class="count">{{ filteredRooms.length }}</span> 间可用房间
          <span v-if="dateRange.length === 2" class="date-info">
            （{{ dateRange[0] }} ~ {{ dateRange[1] }}，共 {{ nights }} 晚）
          </span>
        </h3>
      </div>

      <!-- 房间卡片网格 -->
      <div v-if="filteredRooms.length > 0">
        <el-row :gutter="20">
          <el-col v-for="room in filteredRooms" :key="room.id" :xs="24" :sm="12" :lg="8" style="margin-bottom:20px">
            <div class="room-card">
              <div class="room-card-img">
                <el-image :src="getRoomImage(room.roomTypeId)" fit="cover" style="width:100%;height:200px">
                  <template #error>
                    <div class="image-placeholder"><el-icon :size="48"><PictureFilled /></el-icon></div>
                  </template>
                </el-image>
                <div class="room-status available">可预订</div>
              </div>
              <div class="room-card-body">
                <div class="room-top">
                  <h3>{{ room.roomNo }} {{ room.roomName }}</h3>
                  <div class="room-price"><strong>¥{{ room.price }}</strong>/晚</div>
                </div>
                <div class="room-info">
                  <span>{{ room.floor }}</span>
                  <span>·</span>
                  <span>{{ getRoomTypeName(room.roomTypeId) }}</span>
                  <span>·</span>
                  <span>{{ room.capacity }}/{{ room.maxCapacity }}人</span>
                </div>
                <div class="room-fee-preview" v-if="dateRange.length === 2">
                  <span>预计费用：</span>
                  <span class="fee-total">¥{{ (room.price * nights).toFixed(2) }}</span>
                  <span v-if="isVip" class="vip-discount">
                    VIP折扣后：¥{{ (room.price * nights * (vipInfo.discountRate || 1)).toFixed(2) }}
                  </span>
                </div>
                <div class="room-action">
                  <el-button type="primary" @click="handleBook(room)">预订</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-empty v-else description="没有符合筛选条件的房间，请调整筛选条件" />
    </div>

    <!-- 未查询时的提示 -->
    <div v-if="!searched" class="search-hint">
      <el-empty description="请选择日期范围后点击【查询可用房间】" :image-size="100" />
    </div>

    <!-- ========== 预订弹窗 ========== -->
    <el-dialog v-model="dialogVisible" title="预订房间" width="560px" destroy-on-close @closed="resetForm">
      <div v-if="selectedRoom.id" class="booking-content">
        <!-- 房间信息 -->
        <div class="booking-room-info">
          <span class="room-label">{{ selectedRoom.roomNo }} {{ selectedRoom.roomName }}</span>
          <span class="room-tag">{{ getRoomTypeName(selectedRoom.roomTypeId) }}</span>
          <span class="room-tag-price">¥{{ selectedRoom.price }}/晚</span>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="margin-top:16px">
          <el-form-item label="入住日期">
            <span class="prefilled">{{ dateRange[0] }}</span>
          </el-form-item>
          <el-form-item label="退房日期">
            <span class="prefilled">{{ dateRange[1] }}</span>
          </el-form-item>
          <el-form-item label="姓名" prop="visitorName">
            <el-input v-model="form.visitorName" placeholder="入住人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="联系电话" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="身份证号码" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如有特殊需求请备注" />
          </el-form-item>
        </el-form>

        <!-- 费用明细 -->
        <div class="fee-summary">
          <div class="fee-row">
            <span>房费</span>
            <span>¥{{ selectedRoom.price }} × {{ nights }} 晚</span>
          </div>
          <div class="fee-row" v-if="isVip">
            <span>VIP折扣</span>
            <span class="discounted">{{ (vipInfo.discountRate * 100).toFixed(0) }}折</span>
          </div>
          <div class="fee-row total">
            <span>合计</span>
            <span>¥{{ totalFee.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBooking">确认预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, PictureFilled } from '@element-plus/icons-vue'
import { getAvailableByDateRange, getRoomTypes } from '@/api/dorm/room'
import { submitBooking as doSubmitBooking } from '@/api/user'
import { getVipMemberInfo } from '@/api/vip'
import { useUserStore } from '@/stores/user'

// ==================== 状态 ====================
const dateRange = ref([])
const rooms = ref([])
const allRooms = ref([])
const typeList = ref([])
const loading = ref(false)
const searched = ref(false)
const keyword = ref('')
const typeFilter = ref(null)
const priceMax = ref(null)

// 预订
const dialogVisible = ref(false)
const submitting = ref(false)
const selectedRoom = ref({})
const formRef = ref()
const form = reactive({ visitorName: '', phone: '', idCard: '', remark: '' })
const rules = {
  visitorName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
  ]
}

// VIP
const vipInfo = ref({})
const userStore = useUserStore()

// ==================== 计算属性 ====================
const nights = computed(() => {
  if (dateRange.value.length !== 2) return 0
  const d1 = new Date(dateRange.value[0])
  const d2 = new Date(dateRange.value[1])
  return Math.max(1, Math.round((d2 - d1) / 86400000))
})

const isVip = computed(() => vipInfo.value && vipInfo.value.vipLevel > 0)

const totalFee = computed(() => {
  let fee = (selectedRoom.value.price || 0) * nights.value
  if (isVip.value && vipInfo.value.discountRate) fee *= vipInfo.value.discountRate
  return fee
})

// 前端筛选（房间号、名称、房型、价格上限）
const filteredRooms = computed(() => {
  let list = allRooms.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(r =>
      r.roomNo?.toLowerCase().includes(kw) ||
      r.roomName?.toLowerCase().includes(kw)
    )
  }
  if (typeFilter.value) {
    list = list.filter(r => r.roomTypeId === typeFilter.value)
  }
  if (priceMax.value && priceMax.value > 0) {
    list = list.filter(r => r.price <= priceMax.value)
  }
  return list
})

// ==================== 方法 ====================
function getRoomTypeName(id) {
  if (!id) return ''
  const t = typeList.value.find(t => t.id === id)
  return t ? t.name : ''
}

function getRoomImage(typeId) {
  const name = getRoomTypeName(typeId)
  const map = { '单人间': '/images/单人间.png', '标准间': '/images/标准间.png', '商务房': '/images/商务房.png', '套房': '/images/套房.png' }
  return map[name] || '/images/标准间.png'
}

// 禁止选择今天之前的日期
function disabledDate(time) {
  return time.getTime() < Date.now() - 86400000
}

async function searchAvailable() {
  if (dateRange.value.length !== 2) {
    ElMessage.warning('请选择完整的日期范围（入住日期和退房日期）')
    return
  }
  loading.value = true
  searched.value = true
  keyword.value = ''
  typeFilter.value = null
  priceMax.value = null
  try {
    const [startDate, endDate] = dateRange.value
    const res = await getAvailableByDateRange({ startDate, endDate })
    allRooms.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function filterRooms() { /* computed 自动触发 */ }

function handleBook(room) {
  selectedRoom.value = { ...room }
  const user = userStore.userInfo
  form.visitorName = user?.realName || user?.username || ''
  form.phone = user?.phone || ''
  form.idCard = ''
  form.remark = ''
  dialogVisible.value = true
}

function resetForm() {
  form.visitorName = ''
  form.phone = ''
  form.idCard = ''
  form.remark = ''
}

async function submitBooking() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await doSubmitBooking({
        roomId: selectedRoom.value.id,
        visitorName: form.visitorName,
        phone: form.phone,
        idCard: form.idCard,
        checkInDate: dateRange.value[0],
        checkOutDate: dateRange.value[1],
        remark: form.remark
      })
      ElMessage.success('预订成功，请等待管理员审核')
      dialogVisible.value = false
      // 刷新可用房间列表
      searchAvailable()
    } catch (e) { console.error(e) }
    finally { submitting.value = false }
  })
}

async function fetchRoomTypes() {
  try {
    const res = await getRoomTypes()
    typeList.value = res.data || []
  } catch (e) { console.error(e) }
}

async function fetchVipInfo() {
  try {
    const res = await getVipMemberInfo()
    if (res.code === 200) vipInfo.value = res.data || {}
  } catch (e) { /* 未登录或非VIP */ }
}

// ==================== 生命周期 ====================
onMounted(() => {
  fetchRoomTypes()
  fetchVipInfo()
})
</script>

<style lang="scss" scoped>
.portal-calendar { animation: fadeIn 0.4s ease; max-width: 1100px; }

.page-header {
  margin-bottom: 24px;
  h2 { font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 6px; }
  p { font-size: 14px; color: #909399; }
}

.search-bar {
  background: #fff; border-radius: 8px; padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06); margin-bottom: 24px;
  .date-range-box { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
  .search-label { font-size: 14px; color: #606266; font-weight: 500; }
  .filter-extra { display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap; }
}

.result-section {
  .result-header {
    margin-bottom: 16px;
    h3 { font-size: 18px; color: #303133; font-weight: 600; }
    .count { color: #409eff; font-size: 22px; }
    .date-info { font-size: 14px; color: #909399; font-weight: 400; }
  }
}

.room-card {
  background: #fff; border-radius: 8px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,.06); transition: transform 0.3s, box-shadow 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.12); }
  .room-card-img { position: relative; }
  .room-status.available {
    position: absolute; top: 8px; right: 8px;
    background: #67c23a; color: #fff; padding: 2px 10px; border-radius: 4px;
    font-size: 12px;
  }
  .image-placeholder {
    width: 100%; height: 200px; display: flex; align-items: center; justify-content: center;
    background: #f5f7fa; color: #c0c4cc;
  }
  .room-card-body { padding: 16px; }
  .room-top {
    display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;
    h3 { font-size: 16px; font-weight: 600; color: #303133; margin: 0; }
  }
  .room-price { color: #f56c6c; strong { font-size: 18px; } }
  .room-info { font-size: 13px; color: #909399; margin-bottom: 10px; display: flex; gap: 4px; }
  .room-fee-preview {
    padding: 8px 12px; background: #fdf6ec; border-radius: 6px; margin-bottom: 12px;
    font-size: 13px; color: #606266;
    .fee-total { color: #f56c6c; font-weight: 600; font-size: 15px; }
    .vip-discount { margin-left: 12px; color: #e6a23c; font-weight: 600; }
  }
  .room-action { display: flex; gap: 8px; }
}

.booking-content {
  .booking-room-info { display: flex; gap: 12px; align-items: center; padding: 12px; background: #f5f7fa; border-radius: 6px; }
  .room-label { font-size: 15px; font-weight: 600; color: #303133; }
  .room-tag { font-size: 12px; padding: 2px 8px; background: #ecf5ff; color: #409eff; border-radius: 4px; }
  .room-tag-price { font-size: 13px; color: #f56c6c; font-weight: 600; margin-left: auto; }
  .prefilled { color: #409eff; font-weight: 500; }
}

.fee-summary {
  margin-top: 16px; padding: 14px; background: #f5f7fa; border-radius: 8px;
  .fee-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; color: #606266; }
  .fee-row.total { border-top: 1px dashed #dcdfe6; margin-top: 6px; padding-top: 10px; font-size: 16px; font-weight: 700; color: #f56c6c; }
  .discounted { color: #e6a23c; font-weight: 600; }
}

.search-hint { padding: 60px 0; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }
</style>
