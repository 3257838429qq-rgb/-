<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">入住登记</span>
          <el-button v-if="activeTab === 'active'" type="primary" size="default" :icon="Plus" @click="handleAdd">
            新增入住
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange" class="checkin-tabs">
        <el-tab-pane label="入住中" name="active" />
        <el-tab-pane label="待审核" name="pending" />
      </el-tabs>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无记录">
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column label="访客" min-width="100">
          <template #default="{ row }">
            <div class="visitor-cell">
              <el-avatar :size="28" size="small">
                {{ (row.visitorName || 'V').charAt(0) }}
              </el-avatar>
              <span>{{ row.visitorName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="电话" width="130" align="center">
          <template #default="{ row }">{{ row.visitorPhone || row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="身份证" min-width="160">
          <template #default="{ row }">{{ row.visitorIdCard || row.idCard || '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="nights" label="天数" width="80" align="center" />
        <el-table-column prop="roomFee" label="房费" width="110">
          <template #default="{ row }">
            <span class="fee">¥{{ row.roomFee }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalFee" label="总费用" width="110">
          <template #default="{ row }">
            <span class="fee">¥{{ row.totalFee || row.roomFee }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="statusTagType(row.checkInStatus)"
              size="small"
              effect="plain"
            >
              {{ getCheckInStatusText(row.checkInStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small" effect="plain">
              {{ row.paymentStatus === 1 ? '已付' : '待付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDate(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="activeTab === 'pending'">
              <el-button type="success" link size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
            <template v-else>
              <template v-if="row.checkInStatus === 1">
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleCheckOut(row)"
                >
                  退房
                </el-button>
              </template>
              <el-button
                v-else
                type="primary"
                link
                size="small"
                @click="handleView(row)"
              >
                查看
              </el-button>
            </template>
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

    <!-- Check-in dialog -->
    <el-dialog v-model="dialogVisible" title="新增入住" width="620px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="访客姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker
                v-model="form.checkInDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker
                v-model="form.checkOutDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width:100%"
                :disabled="!form.checkInDate"
                :disabled-date="(d) => form.checkInDate && dayjs(d).isBefore(dayjs(form.checkInDate))"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="选择房间" prop="roomId">
              <el-select
                v-model="form.roomId"
                placeholder="请选择空闲房间"
                style="width:100%"
                filterable
                @change="onRoomChange"
              >
                <el-option
                  v-for="r in availableRooms"
                  :key="r.id"
                  :label="`${r.roomNo} | ${r.roomName || r.roomNo} | ¥${r.price}/晚`"
                  :value="r.id"
                >
                  <div class="room-option">
                    <span class="room-no">{{ r.roomNo }}</span>
                    <span class="room-info">{{ r.roomName || r.roomNo }}</span>
                    <span class="room-price">¥{{ r.price }}/晚</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- Fee summary -->
        <div class="fee-summary" v-if="form.roomId && form.checkInDate && form.checkOutDate">
          <div class="fee-item">
            <span class="fee-label">入住天数</span>
            <span class="fee-num">{{ calculatedNights }} 晚</span>
          </div>
          <div class="fee-item">
            <span class="fee-label">预计房费</span>
            <span class="fee-num primary">¥{{ calculatedFee }}</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确认入住
        </el-button>
      </template>
    </el-dialog>

    <!-- Checkout dialog -->
    <el-dialog v-model="checkoutVisible" title="退房结算" width="500px" :close-on-click-modal="false">
      <div class="checkout-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ currentRecord?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ currentRecord?.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentRecord?.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ formatToday() }}</el-descriptions-item>
          <el-descriptions-item label="入住天数" :span="2">
            {{ currentRecord?.nights }} 晚
          </el-descriptions-item>
        </el-descriptions>

        <el-form label-width="80px" class="checkout-form">
          <el-form-item label="房费">
            <span class="fee">¥{{ currentRecord?.roomFee }}</span>
          </el-form-item>
          <el-form-item label="其他费用">
            <el-input-number
              v-model="checkoutForm.otherFee"
              :min="0"
              :precision="2"
              :controls="true"
              style="width:100%"
            />
          </el-form-item>
          <el-form-item label="支付方式">
            <el-select v-model="checkoutForm.paymentMethod" style="width:100%">
              <el-option label="微信支付" value="微信支付" />
              <el-option label="支付宝" value="支付宝" />
              <el-option label="现金" value="现金" />
              <el-option label="银行卡" value="银行卡" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="total-bar">
          <span class="total-label">合计金额</span>
          <span class="total-amount">
            ¥{{ ((currentRecord?.roomFee || 0) + (checkoutForm.otherFee || 0)).toFixed(2) }}
          </span>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkingOut" @click="submitCheckOut">
          确认退房
        </el-button>
      </template>
    </el-dialog>

    <!-- View detail dialog -->
    <el-dialog v-model="viewVisible" title="入住详情" width="620px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号" :span="2">{{ currentRecord?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="访客姓名">{{ currentRecord?.visitorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRecord?.visitorPhone || currentRecord?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号" :span="2">{{ currentRecord?.visitorIdCard || currentRecord?.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentRecord?.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ currentRecord?.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ currentRecord?.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ currentRecord?.nights }} 晚</el-descriptions-item>
        <el-descriptions-item label="房费">¥{{ currentRecord?.roomFee }}</el-descriptions-item>
        <el-descriptions-item label="其他费用">¥{{ currentRecord?.otherFee || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="总费用">
          <span class="fee">¥{{ currentRecord?.totalFee }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRecord?.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="currentRecord?.paymentStatus === 1 ? 'success' : 'warning'" size="small">
            {{ currentRecord?.paymentStatus === 1 ? '已支付' : '待支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入住状态">
          <el-tag :type="statusTagType(currentRecord?.checkInStatus)" size="small">
            {{ getCheckInStatusText(currentRecord?.checkInStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getActiveCheckIns, addCheckIn, checkOut } from '@/api/dorm/checkin'
import { getAvailableRooms } from '@/api/dorm/room'
import { getPendingCheckIns, approveBooking, rejectBooking } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const checkingOut = ref(false)
const tableData = ref([])
const availableRooms = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const checkoutVisible = ref(false)
const viewVisible = ref(false)
const currentRecord = ref({})
const formRef = ref()
const activeTab = ref('active')

const checkoutForm = reactive({ otherFee: 0, paymentMethod: '微信支付' })
const queryParams = reactive({ current: 1, size: 10 })

const form = reactive({
  visitorName: '', phone: '', idCard: '', roomId: null,
  checkInDate: '', checkOutDate: '', remark: ''
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }]
}

const selectedRoomPrice = computed(() => {
  const room = availableRooms.value.find(r => r.id === form.roomId)
  return room ? `¥${room.price}/晚` : '-'
})

const calculatedNights = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  return Math.max(1, dayjs(form.checkOutDate).diff(dayjs(form.checkInDate), 'day'))
})

const calculatedFee = computed(() => {
  if (!form.roomId) return '0.00'
  const room = availableRooms.value.find(r => r.id === form.roomId)
  return room ? (room.price * calculatedNights.value).toFixed(2) : '0.00'
})

function getCheckInStatusText(status) {
  return { 0: '待审核', 1: '入住中', 2: '已退房', 3: '已拒绝', 4: '退房申请中' }[status] || '-'
}

function statusTagType(status) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }[status] || 'info'
}

function formatDate(d) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function formatToday() {
  return dayjs().format('YYYY-MM-DD')
}

async function fetchData() {
  loading.value = true
  try {
    const api = activeTab.value === 'pending' ? getPendingCheckIns : getActiveCheckIns
    const res = await api(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function onTabChange() {
  queryParams.current = 1
  if (activeTab.value === 'pending') {
    queryParams.status = 0
  } else {
    delete queryParams.status
  }
  fetchData()
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定通过订单 ${row.orderNo} 的入住申请吗？`, '审批确认', { type: 'info' })
    const res = await approveBooking(row.id)
    if (res.code === 200) {
      ElMessage.success('已通过，房间已自动分配')
      fetchData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleReject(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '可选：填写拒绝原因'
    })
    const res = await rejectBooking(row.id, reason || '')
    if (res.code === 200) {
      ElMessage.success('已拒绝该申请')
      fetchData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function fetchAvailableRooms() {
  const res = await getAvailableRooms({ current: 1, size: 100 })
  if (res.code === 200) availableRooms.value = res.data.records
}

function onRoomChange() {
  // room changed — fee will auto-update via computed
}

function handleAdd() {
  Object.assign(form, {
    visitorName: '', phone: '', idCard: '', roomId: null,
    checkInDate: dayjs().format('YYYY-MM-DD'),
    checkOutDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
    remark: ''
  })
  checkoutForm.otherFee = 0
  checkoutForm.paymentMethod = '微信支付'
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await addCheckIn(form)
      if (res.code === 200) {
        ElMessage.success('入住登记成功')
        dialogVisible.value = false
        fetchData()
      }
    } catch (e) { console.error(e) }
    finally { submitting.value = false }
  })
}

function handleCheckOut(row) {
  currentRecord.value = row
  checkoutForm.otherFee = 0
  checkoutForm.paymentMethod = '微信支付'
  checkoutVisible.value = true
}

async function submitCheckOut() {
  checkingOut.value = true
  try {
    const res = await checkOut(currentRecord.value.id, checkoutForm.otherFee, checkoutForm.paymentMethod)
    if (res.code === 200) {
      ElMessage.success('退房结算成功')
      checkoutVisible.value = false
      fetchData()
    }
  } catch (e) { console.error(e) }
  finally { checkingOut.value = false }
}

function handleView(row) {
  currentRecord.value = row
  viewVisible.value = true
}


onMounted(() => {
  fetchData()
  fetchAvailableRooms()
})
</script>

<style lang="scss" scoped>
.checkin-tabs {
  margin-bottom: 16px;
}

.visitor-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fee {
  color: #f56c6c;
  font-weight: 600;
}

// Room selector option
.room-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .room-no {
    font-weight: 600;
    color: #303133;
    min-width: 60px;
  }

  .room-info {
    flex: 1;
    margin: 0 12px;
    color: #606266;
    font-size: 13px;
  }

  .room-price {
    color: #f56c6c;
    font-weight: 600;
    font-size: 13px;
  }
}

// Fee summary in dialog
.fee-summary {
  margin-top: 8px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #ecf5ff, #f0f9ff);
  border-radius: 10px;
  display: flex;
  justify-content: space-around;
  border: 1px solid #d9ecff;

  .fee-item {
    text-align: center;

    .fee-label {
      display: block;
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }

    .fee-num {
      font-size: 20px;
      font-weight: 700;
      color: #303133;

      &.primary {
        color: #409eff;
      }
    }
  }
}

// Checkout dialog
.checkout-info {
  .checkout-form {
    margin-top: 16px;
  }

  .total-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 18px;
    margin-top: 12px;
    background: linear-gradient(135deg, #fef0f0, #fdf6ec);
    border-radius: 10px;
    border: 1px solid #fde2e2;

    .total-label {
      font-size: 14px;
      color: #606266;
    }

    .total-amount {
      font-size: 24px;
      font-weight: 700;
      color: #f56c6c;
      letter-spacing: -0.5px;
    }
  }
}
</style>

<style lang="scss">
.el-table__fixed-right,
.el-table__fixed-right .el-table__fixed-body-wrapper,
.el-table__fixed-right .el-table__fixed-header-wrapper {
  background-color: transparent !important;
}
</style>
