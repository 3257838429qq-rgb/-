<template>
  <div class="page-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-bar">
          <el-input v-model="queryParams.roomNo" placeholder="房间号" clearable style="width:120px" @clear="search" />
          <el-select v-model="queryParams.status" placeholder="入住状态" clearable style="width:130px" @change="search">
            <el-option label="待审核" :value="0" />
            <el-option label="入住中" :value="1" />
            <el-option label="已退房" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="退房申请中" :value="4" />
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
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="nights" label="天数" width="80" align="center" />
        <el-table-column prop="roomFee" label="房费" width="100">
          <template #default="{ row }">¥{{ row.roomFee }}</template>
        </el-table-column>
        <el-table-column prop="otherFee" label="其他费用" width="100">
          <template #default="{ row }">¥{{ row.otherFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="totalFee" label="总费用" width="100">
          <template #default="{ row }">¥{{ row.totalFee || row.roomFee }}</template>
        </el-table-column>
        <el-table-column prop="checkInStatus" label="入住状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.checkInStatus)" size="small">
              {{ getStatusText(row.checkInStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.paymentStatus === 1 ? '已支付' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        <el-table-column prop="reviewRating" label="评价" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.checkInStatus === 2">
              <div v-if="row.reviewRating" class="review-cell" @click="viewReview(row)">
                <el-rate v-model="row.reviewRating" disabled size="small" />
              </div>
              <el-tag v-else type="info" size="small">未评价</el-tag>
            </template>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <template v-if="row.checkInStatus === 4">
              <el-button type="success" link size="small" @click="handleCheckoutApprove(row)">审核退房</el-button>
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

    <!-- Detail dialog -->
    <el-dialog v-model="detailVisible" title="住宿记录详情" width="620px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号" :span="2">{{ detailRecord?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="访客姓名">{{ detailRecord?.visitorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailRecord?.visitorPhone || detailRecord?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号" :span="2">{{ detailRecord?.visitorIdCard || detailRecord?.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detailRecord?.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ detailRecord?.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ detailRecord?.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ detailRecord?.nights }} 晚</el-descriptions-item>
        <el-descriptions-item label="房费">¥{{ detailRecord?.roomFee }}</el-descriptions-item>
        <el-descriptions-item label="其他费用">¥{{ detailRecord?.otherFee || 0 }}</el-descriptions-item>
        <el-descriptions-item label="总费用">
          <span class="fee">¥{{ detailRecord?.totalFee || detailRecord?.roomFee }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detailRecord?.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="detailRecord?.paymentStatus === 1 ? 'success' : 'warning'" size="small">
            {{ detailRecord?.paymentStatus === 1 ? '已支付' : '待支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入住状态">
          <el-tag :type="statusTagType(detailRecord?.checkInStatus)" size="small">
            {{ getStatusText(detailRecord?.checkInStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDate(detailRecord?.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="退房时间">{{ formatDate(detailRecord?.checkOutTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRecord?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- Checkout request notice in detail -->
      <div v-if="detailRecord?.checkInStatus === 4" class="checkout-request-notice">
        <div class="notice-title">
          <el-icon><InfoFilled /></el-icon>
          退房申请待审核
        </div>
        <div class="notice-body">用户已提交退房申请，请点击"审核退房"按钮设置额外费用并完成退房操作。</div>
        <div class="notice-actions">
          <el-button type="success" size="small" @click="handleCheckoutApprove(detailRecord); detailVisible = false">审核通过</el-button>
          <el-button type="danger" size="small" @click="handleCheckoutReject(detailRecord)">拒绝申请</el-button>
        </div>
      </div>

      <!-- Review section -->
      <div v-if="detailRecord?.checkInStatus === 2" class="review-section">
        <div class="review-title">住客评价</div>
        <div v-if="detailRecord.reviewRating" class="review-content">
          <div class="review-header">
            <el-rate v-model="detailRecord.reviewRating" disabled />
            <span class="rating-desc">{{ ratingText(detailRecord.reviewRating) }}</span>
          </div>
          <div class="review-text">{{ detailRecord.reviewContent || '该用户未留下文字评价' }}</div>
          <div class="review-time">评价时间：{{ formatDate(detailRecord.reviewTime) }}</div>
          <div v-if="detailRecord.reply" class="admin-reply">
            <div class="reply-label">管理员回复：</div>
            <div class="reply-text">{{ detailRecord.reply }}</div>
            <div class="reply-time">{{ formatDate(detailRecord.replyTime) }}</div>
          </div>
          <div v-else-if="!detailRecord.reply" class="reply-actions">
            <el-button type="primary" size="small" @click="replyVisible = true">回复评价</el-button>
          </div>
        </div>
        <div v-else class="no-review">该订单暂无评价</div>
      </div>
    </el-dialog>

    <!-- Reply dialog -->
    <el-dialog v-model="replyVisible" title="回复评价" width="450px">
      <el-form label-width="80px">
        <el-form-item label="订单号">
          <span>{{ detailRecord?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>

    <!-- Checkout approval dialog -->
    <el-dialog v-model="checkoutApproveVisible" title="审核退房" width="500px" :close-on-click-modal="false">
      <div class="review-order-info">
        <span>订单号：{{ checkoutRecord?.orderNo }}</span>
        <span>房间号：{{ checkoutRecord?.roomNo }}</span>
      </div>
      <el-form label-width="90px" style="margin-top:16px">
        <el-form-item label="住宿天数">
          <span>{{ checkoutRecord?.nights }} 晚</span>
        </el-form-item>
        <el-form-item label="房费">
          <span class="fee">¥{{ checkoutRecord?.roomFee || 0 }}</span>
        </el-form-item>
        <el-form-item label="额外费用" required>
          <el-input-number v-model="checkoutForm.otherFee" :min="0" :precision="2" controls-position="right" style="width:200px" />
        </el-form-item>
        <el-form-item label="合计">
          <span class="total-fee">¥{{ checkoutTotalFee }}</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="checkoutForm.paymentMethod" placeholder="请选择支付方式" style="width:200px">
            <el-option label="微信支付" value="微信支付" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="现金" value="现金" />
            <el-option label="银行卡" value="银行卡" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="checkout-notice">
        <el-icon><InfoFilled /></el-icon>
        <span>确认退房后，房间将恢复为空闲状态，费用将标记为已支付。</span>
      </div>
      <template #footer>
        <el-button @click="checkoutApproveVisible = false">取消</el-button>
        <el-button type="success" :loading="checkoutApproving" @click="submitCheckoutApprove">确认退房</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { getCheckInList } from '@/api/dorm/checkin'
import { getCheckInReview, replyReview as replyReviewApi } from '@/api/review'
import { approveCheckout, rejectCheckout } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const replyVisible = ref(false)
const replying = ref(false)
const detailRecord = ref({})
const reviewDetail = ref({})
const replyForm = reactive({ reply: '' })

// Checkout approval dialog
const checkoutApproveVisible = ref(false)
const checkoutApproving = ref(false)
const checkoutRecord = ref({})
const checkoutForm = reactive({ otherFee: 0, paymentMethod: '现金' })
const checkoutTotalFee = computed(() => {
  const roomFee = Number(checkoutRecord.value?.roomFee || 0)
  const otherFee = Number(checkoutForm.otherFee || 0)
  return (roomFee + otherFee).toFixed(2)
})

const queryParams = reactive({
  current: 1, size: 10,
  roomNo: '', status: null, paymentStatus: null
})

function getStatusText(s) { return { 0: '待审核', 1: '入住中', 2: '已退房', 3: '已取消', 4: '退房申请中' }[s] || '-' }
function statusTagType(s) { return { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }[s] || 'info' }
function formatDate(d) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }
function ratingText(r) { return { 1: '极差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' }[r] || '' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getCheckInList(queryParams)
    if (res.code === 200) {
      const records = res.data.records || []
      tableData.value = records
      total.value = res.data.total || 0
      for (const r of records) {
        if (r.checkInStatus === 2) {
          try {
            const rv = await getCheckInReview(r.id)
            if (rv.code === 200 && rv.data) {
              r.reviewRating = rv.data.rating
              r.reviewContent = rv.data.content
              r.reviewTime = rv.data.createTime
              r.reply = rv.data.reply
              r.replyTime = rv.data.replyTime
            } else {
              r.reviewRating = 0
            }
          } catch { r.reviewRating = 0 }
        }
      }
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function search() { queryParams.current = 1; fetchData() }
function reset() {
  queryParams.roomNo = ''
  queryParams.status = null
  queryParams.paymentStatus = null
  search()
}

async function handleView(row) {
  detailRecord.value = { ...row }
  replyForm.reply = ''
  if (row.checkInStatus === 2) {
    try {
      const rv = await getCheckInReview(row.id)
      if (rv.code === 200 && rv.data) {
        detailRecord.value.reviewRating = rv.data.rating
        detailRecord.value.reviewContent = rv.data.content
        detailRecord.value.reviewTime = rv.data.createTime
        detailRecord.value.reply = rv.data.reply
        detailRecord.value.replyTime = rv.data.replyTime
      } else {
        detailRecord.value.reviewRating = 0
      }
    } catch { detailRecord.value.reviewRating = 0 }
  }
  detailVisible.value = true
}

async function viewReview(row) {
  handleView(row)
}

async function submitReply() {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    const reviewRow = tableData.value.find(r => r.id === detailRecord.value.id)
    const reviewId = reviewRow?.reviewId
    if (!reviewId) {
      ElMessage.error('无法获取评价ID')
      return
    }
    const res = await replyReviewApi(reviewId, replyForm.reply)
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyVisible.value = false
      detailVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('回复失败')
    }
  } finally { replying.value = false }
}

// 打开退房审核弹窗
function handleCheckoutApprove(row) {
  checkoutRecord.value = { ...row }
  checkoutForm.otherFee = 0
  checkoutForm.paymentMethod = '现金'
  checkoutApproveVisible.value = true
}

// 提交退房审核
async function submitCheckoutApprove() {
  if (!checkoutForm.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认退房并标记为已支付？最终费用为 ¥${checkoutTotalFee.value}。`,
      '确认退房',
      { confirmButtonText: '确认退房', cancelButtonText: '取消', type: 'warning' }
    )
  } catch { return }
  checkoutApproving.value = true
  try {
    const res = await approveCheckout(checkoutRecord.value.id, checkoutForm.otherFee, checkoutForm.paymentMethod)
    if (res.code === 200) {
      ElMessage.success('退房审核完成')
      checkoutApproveVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('退房审核失败')
    }
  } finally { checkoutApproving.value = false }
}

// 拒绝退房申请
async function handleCheckoutReject(row) {
  try {
    await ElMessageBox.confirm('确定要拒绝该退房申请吗？订单将恢复为入住状态。', '拒绝退房申请', {
      confirmButtonText: '确认拒绝', cancelButtonText: '取消', type: 'warning'
    })
  } catch { return }
  try {
    const res = await rejectCheckout(row.id)
    if (res.code === 200) {
      ElMessage.success('已拒绝退房申请')
      detailVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.review-cell {
  cursor: pointer;
}

.muted { color: #c0c4cc; }

.fee { color: #f56c6c; font-weight: 600; }

.review-section {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;

  .review-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .review-content {
    .review-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
    }

    .rating-desc {
      font-size: 13px;
      color: #f56c6c;
      font-weight: 600;
    }

    .review-text {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      background: #f5f7fa;
      padding: 10px 14px;
      border-radius: 6px;
      margin-bottom: 8px;
    }

    .review-time {
      font-size: 12px;
      color: #909399;
      text-align: right;
      margin-bottom: 12px;
    }

    .admin-reply {
      background: #ecf5ff;
      padding: 10px 14px;
      border-radius: 6px;
      border-left: 3px solid #409eff;

      .reply-label {
        font-size: 13px;
        font-weight: 600;
        color: #409eff;
        margin-bottom: 6px;
      }

      .reply-text {
        font-size: 14px;
        color: #303133;
        line-height: 1.6;
        margin-bottom: 4px;
      }

      .reply-time {
        font-size: 12px;
        color: #909399;
        text-align: right;
      }
    }

    .reply-actions {
      margin-top: 8px;
    }
  }

  .no-review {
    font-size: 13px;
    color: #c0c4cc;
    font-style: italic;
  }
}

.checkout-request-notice {
  margin-top: 18px;
  padding: 14px 16px;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 6px;

  .notice-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #e6a23c;
    margin-bottom: 8px;
  }

  .notice-body {
    font-size: 13px;
    color: #909399;
    margin-bottom: 12px;
    line-height: 1.5;
  }

  .notice-actions {
    display: flex;
    gap: 8px;
  }
}

.checkout-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 14px;
  padding: 10px 14px;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
  border-radius: 6px;
  font-size: 13px;
  color: #67c23a;
  line-height: 1.5;

  .el-icon {
    flex-shrink: 0;
    margin-top: 1px;
  }
}

.total-fee {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}
</style>
