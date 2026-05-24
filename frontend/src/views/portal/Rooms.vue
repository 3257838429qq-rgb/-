<template>
  <div class="portal-rooms">
    <div class="page-header">
      <h2>客房浏览</h2>
      <p>选择您心仪的房间，提交入住申请</p>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <!-- Keyword search -->
      <el-input
        v-model="filterKeyword"
        placeholder="搜索房间号/名称"
        clearable
        style="width: 220px"
        @clear="doFilter"
        @keyup.enter="doFilter"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <!-- Price range -->
      <el-input-number
        v-model="filterPriceMin"
        :min="0"
        :precision="0"
        placeholder="最低价"
        controls-position="right"
        style="width: 130px"
        :step="50"
        :clearable="true"
        @change="onPriceChange"
      />
      <span class="price-sep">-</span>
      <el-input-number
        v-model="filterPriceMax"
        :min="0"
        :precision="0"
        placeholder="最高价"
        controls-position="right"
        style="width: 130px"
        :step="50"
        :clearable="true"
        @change="onPriceChange"
      />

      <el-button type="primary" :icon="Search" @click="doFilter">搜索</el-button>
      <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
    </div>

    <!-- Room type filter tabs -->
    <div class="room-type-tabs">
      <el-radio-group v-model="filterTypeId" size="large" @change="doFilter">
        <el-radio-button label="__all__">全部</el-radio-button>
        <el-radio-button v-for="t in roomTypes" :key="t.id" :label="t.id">
          {{ t.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- Room count summary -->
    <div class="result-summary" v-if="!loading && rooms.length > 0">
      共找到 <strong>{{ rooms.length }}</strong> 间可预订房间
    </div>

    <!-- Room grid -->
    <el-row :gutter="20" v-loading="loading">
      <el-col v-for="room in rooms" :key="room.id" :xs="24" :sm="12" :lg="8">
        <div class="room-card">
          <div class="room-card-img">
            <img src="/418.webp" alt="room" />
            <div class="room-status" :class="statusClass(room.status)">
              {{ statusText(room.status) }}
            </div>
          </div>
          <div class="room-card-body">
            <div class="room-top">
              <h3>{{ room.roomNo }} {{ room.roomName }}</h3>
              <div class="room-price">
                <span class="price">¥{{ room.price }}</span><span class="unit">/晚</span>
              </div>
            </div>
            <div class="room-info">
              <span><el-icon><Location /></el-icon> {{ room.floor }}</span>
              <span><el-icon><User /></el-icon> {{ room.capacity }}人</span>
              <span>{{ roomTypeName(room.roomTypeId) }}</span>
            </div>
            <div class="room-facilities">
              <el-tag size="small" v-for="f in parseFacilities(room.facility)" :key="f" class="f-tag">
                {{ f }}
              </el-tag>
            </div>
            <div class="room-action">
              <el-button
                type="primary" link size="small"
                @click="handleViewReviews(room)"
              >
                查看评价
              </el-button>
              <el-button
                type="primary"
                :disabled="room.status !== 1"
                @click="handleBook(room)"
              >
                {{ room.status === 1 ? '立即预订' : '暂不可订' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && rooms.length === 0" description="暂无可选房间" />

    <!-- Booking dialog -->
    <el-dialog v-model="dialogVisible" title="提交入住申请" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择房间">
          <el-input :value="selectedRoomLabel" disabled />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入您的姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker v-model="form.checkInDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker v-model="form.checkOutDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%"
                :disabled="!form.checkInDate"
                :disabled-date="(d) => form.checkInDate && dayjs(d).isBefore(dayjs(form.checkInDate))"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选：特殊需求说明" />
        </el-form-item>

        <div class="fee-summary" v-if="form.checkInDate && form.checkOutDate">
          <div class="fee-row">
            <span>预计天数</span>
            <strong>{{ nights }} 晚</strong>
          </div>
          <div class="fee-row">
            <span>预计房费</span>
            <strong class="price">¥{{ totalFee.toFixed(2) }}</strong>
          </div>
        </div>
        <el-alert type="info" :closable="false" show-icon style="margin-top:12px">
          提交后需等待管理员审核，审核通过后方可入住。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- Room reviews dialog -->
    <el-dialog v-model="reviewDialogVisible" :title="`${currentRoom.roomNo} 住客评价`" width="550px">
      <div v-if="roomReviews.length > 0" class="review-summary">
        <span class="avg-rating">平均评分</span>
        <el-rate v-model="avgRating" disabled show-score text-color="#ff9900" />
      </div>
      <div v-if="reviewLoading" class="review-spin" v-loading="reviewLoading" style="min-height:120px" />
      <div v-else-if="roomReviews.length === 0" class="review-empty">
        <el-empty description="暂无评价" :image-size="80" />
      </div>
      <div v-else class="review-list">
        <div v-for="r in roomReviews" :key="r.id" class="review-item">
          <div class="review-item-top">
            <span class="review-user">{{ r.userName }}</span>
            <el-rate v-model="r.rating" disabled size="small" />
            <span class="review-time">{{ formatReviewTime(r.createTime) }}</span>
          </div>
          <p class="review-item-text">{{ r.content }}</p>
          <div v-if="r.reply" class="review-item-reply">
            <span class="reply-label">管理员回复：</span>{{ r.reply }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, User, Search, Refresh } from '@element-plus/icons-vue'
import { getRoomList, getRoomTypeList, submitBooking } from '@/api/user'
import { getRoomReviews } from '@/api/review'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const rooms = ref([])
const roomTypes = ref([])
const filterTypeId = ref('__all__')
const filterKeyword = ref('')
const filterPriceMin = ref(null)
const filterPriceMax = ref(null)
const dialogVisible = ref(false)
const selectedRoom = ref({})
const formRef = ref()

const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const currentRoom = ref({})
const roomReviews = ref([])

const avgRating = computed(() => {
  if (roomReviews.value.length === 0) return 0
  return (roomReviews.value.reduce((s, r) => s + r.rating, 0) / roomReviews.value.length).toFixed(1)
})

const form = reactive({
  roomId: null, visitorName: '', phone: '', idCard: '',
  checkInDate: '', checkOutDate: '', remark: ''
})

const rules = {
  visitorName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }]
}

const selectedRoomLabel = computed(() =>
  selectedRoom.value.roomNo ? `${selectedRoom.value.roomNo} | ¥${selectedRoom.value.price}/晚` : ''
)

const nights = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  return Math.max(1, dayjs(form.checkOutDate).diff(dayjs(form.checkInDate), 'day'))
})

const totalFee = computed(() => (selectedRoom.value.price || 0) * nights.value)

function statusClass(s) { return { 1: 'available', 2: 'occupied', 3: 'maintenance', 4: 'disabled' }[s] || '' }
function statusText(s) { return { 1: '空闲', 2: '已入住', 3: '维护中', 4: '已停用' }[s] || '' }
function roomTypeName(id) { return roomTypes.value.find(t => t.id === id)?.name || '' }

function parseFacilities(f) {
  if (!f) return []
  return f.split('|').map(s => s.trim()).filter(Boolean)
}

async function doFilter() {
  loading.value = true
  try {
    const params = { current: 1, size: 100, status: 1 }
    if (filterTypeId.value !== '__all__') params.roomTypeId = filterTypeId.value
    if (filterKeyword.value.trim()) params.roomNo = filterKeyword.value.trim()
    if (filterPriceMin.value !== null && filterPriceMin.value !== undefined) params.priceMin = filterPriceMin.value
    if (filterPriceMax.value !== null && filterPriceMax.value !== undefined) params.priceMax = filterPriceMax.value
    const res = await getRoomList(params)
    if (res.code === 200) rooms.value = res.data.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function onPriceChange() { doFilter() }

function resetFilter() {
  filterTypeId.value = '__all__'
  filterKeyword.value = ''
  filterPriceMin.value = null
  filterPriceMax.value = null
  doFilter()
}

async function fetchRoomTypes() {
  try {
    const res = await getRoomTypeList()
    if (res.code === 200) roomTypes.value = res.data || []
  } catch (e) { console.error(e) }
}

function handleBook(room) {
  selectedRoom.value = room
  form.roomId = room.id
  form.visitorName = ''
  form.phone = ''
  form.idCard = ''
  form.checkInDate = dayjs().format('YYYY-MM-DD')
  form.checkOutDate = dayjs().add(1, 'day').format('YYYY-MM-DD')
  form.remark = ''
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await submitBooking({ ...form })
      if (res.code === 200) {
        ElMessage.success('预订申请已提交，请等待管理员审核')
        dialogVisible.value = false
        doFilter()
      }
    } catch (e) { console.error(e) }
    finally { submitting.value = false }
  })
}

function formatReviewTime(d) { return d ? dayjs(d).format('YYYY-MM-DD') : '' }

async function handleViewReviews(room) {
  currentRoom.value = room
  reviewDialogVisible.value = true
  roomReviews.value = []
  reviewLoading.value = true
  try {
    const res = await getRoomReviews(room.id)
    if (res.code === 200) roomReviews.value = res.data || []
  } catch (e) { console.error(e) }
  finally { reviewLoading.value = false }
}

onMounted(() => { fetchRoomTypes(); doFilter() })
</script>

<style lang="scss" scoped>
.portal-rooms {
  animation: fadeIn 0.4s ease;
}

.page-header {
  margin-bottom: 24px;

  h2 { font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 6px; }
  p { font-size: 14px; color: #909399; }
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 10px;
  border: 1px solid #e8ecf1;

  .price-sep {
    color: #909399;
    font-size: 14px;
  }
}

.result-summary {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;

  strong { color: #409eff; }
}

.room-type-tabs {
  margin-bottom: 24px;
}

// Room card
.room-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
  transition: all 0.3s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); }
}

.room-card-img {
  height: 180px;
  background: linear-gradient(135deg, #e4e7ed, #dcdfe6);
  position: relative;
  overflow: hidden;

  img { width: 100%; height: 100%; object-fit: cover; opacity: 0.7; }

  .room-status {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    color: #fff;
    font-weight: 500;

    &.available { background: #67c23a; }
    &.occupied { background: #409eff; }
    &.maintenance { background: #e6a23c; }
    &.disabled { background: #909399; }
  }
}

.room-card-body {
  padding: 18px;

  .room-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    h3 { font-size: 16px; font-weight: 600; color: #303133; }

    .room-price {
      .price { font-size: 22px; font-weight: 700; color: #f56c6c; }
      .unit { font-size: 12px; color: #909399; margin-left: 2px; }
    }
  }

  .room-info {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #606266;
    margin-bottom: 10px;

    span { display: flex; align-items: center; gap: 3px; }
  }

  .room-facilities {
    display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 14px;
  }

  .room-action {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

// Review dialog
.review-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;

  .avg-rating { font-size: 14px; color: #606266; }
}

.review-empty {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.review-spin {
  display: flex;
  justify-content: center;
}

.review-list {
  max-height: 400px;
  overflow-y: auto;
}

.review-item {
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child { border-bottom: none; }
}

.review-item-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;

  .review-user { font-size: 14px; font-weight: 600; color: #303133; }
  .review-time { font-size: 12px; color: #c0c4cc; margin-left: auto; }
}

.review-item-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-item-reply {
  font-size: 13px;
  color: #409eff;
  background: #ecf5ff;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #409eff;

  .reply-label { font-weight: 600; }
}

// Fee summary
.fee-summary {
  padding: 14px 16px;
  background: linear-gradient(135deg, #ecf5ff, #f0f9ff);
  border-radius: 10px;
  border: 1px solid #d9ecff;

  .fee-row {
    display: flex; justify-content: space-between; align-items: center;
    font-size: 14px; color: #606266;
    margin-bottom: 6px;

    &:last-child { margin-bottom: 0; }

    strong { font-size: 16px; color: #303133; }
    .price { color: #f56c6c; font-size: 20px; }
  }
}
</style>
