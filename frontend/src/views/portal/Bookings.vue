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
            <span class="fee">¥{{ row.totalFee || row.roomFee || 0 }}</span>
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
        <el-table-column label="评价" width="90" align="center">
          <template #default="{ row }">
            <template v-if="row.checkInStatus === 2">
              <el-tag v-if="row.hasReview" type="success" size="small" effect="plain">
                已评价
              </el-tag>
              <el-button v-else type="primary" link size="small" @click="handleReview(row)">
                去评价
              </el-button>
            </template>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <template v-if="row.checkInStatus === 1">
              <el-button
                v-if="row.paymentStatus !== 1"
                type="warning"
                link
                size="small"
                @click="handlePayOpen(row)"
              >
                付款
              </el-button>
              <el-button type="danger" link size="small" @click="handleCheckoutOpen(row)">
                申请退房
              </el-button>
            </template>
            <el-tag v-else-if="row.checkInStatus === 4" type="warning" size="small" effect="plain">
              退房待审
            </el-tag>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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

    <!-- Review dialog -->
    <el-dialog v-model="reviewVisible" title="服务评价" width="500px">
      <div class="review-order-info">
        <span>订单号：{{ currentRecord?.orderNo }}</span>
        <span>房间号：{{ currentRecord?.roomNo }}</span>
      </div>
      <el-form label-width="70px" style="margin-top:16px">
        <el-form-item label="评分">
          <div class="rating-stars">
            <el-rate v-model="reviewForm.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" size="large" />
            <span class="rating-label">{{ ratingLabel }}</span>
          </div>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="分享您的入住体验，帮助我们做得更好"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="handleSubmitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- Checkout request dialog -->
    <el-dialog v-model="checkoutVisible" title="申请退房" width="500px" :close-on-click-modal="false">
      <div class="review-order-info">
        <span>订单号：{{ currentRecord?.orderNo }}</span>
        <span>房间号：{{ currentRecord?.roomNo }}</span>
      </div>
      <div class="checkout-info" style="margin-top:16px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="入住日期">{{ formatDate(currentRecord?.checkInDate) }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ formatDate(currentRecord?.checkOutDate) }}</el-descriptions-item>
          <el-descriptions-item label="住宿天数">{{ currentRecord?.nights }} 晚</el-descriptions-item>
          <el-descriptions-item label="房费">
            <span class="fee">¥{{ currentRecord?.roomFee || 0 }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="checkout-notice">
          <el-icon><InfoFilled /></el-icon>
          <span>退房申请已提交，请等待管理员审核。管理员将根据实际消费确认最终费用。</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkingOut" @click="handleCheckoutRequest">提交退房申请</el-button>
      </template>
    </el-dialog>

    <!-- Payment dialog -->
    <el-dialog v-model="payVisible" title="订单付款" width="450px" :close-on-click-modal="false">
      <div class="review-order-info">
        <span>订单号：{{ currentRecord?.orderNo }}</span>
        <span>房间号：{{ currentRecord?.roomNo }}</span>
      </div>
      <el-form label-width="80px" style="margin-top:16px">
        <el-form-item label="应付金额">
          <span class="fee">¥{{ currentRecord?.roomFee || 0 }}</span>
        </el-form-item>
        <el-form-item label="付款金额" required>
          <el-input-number
            v-model="payForm.paidAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.paymentMethod" style="width:100%">
            <el-option label="微信支付" value="微信支付" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="银行卡" value="银行卡" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="handlePaySubmit">确认付款</el-button>
      </template>
    </el-dialog>

    <!-- View review dialog -->
    <el-dialog v-model="viewReviewVisible" title="我的评价" width="500px">
      <div class="view-review">
        <div class="review-order-info">
          <span>订单号：{{ currentRecord?.orderNo }}</span>
          <span>房间号：{{ currentRecord?.roomNo }}</span>
        </div>
        <div class="review-content">
          <div class="review-rating">
            <el-rate v-model="currentReview.rating" disabled text-color="#ff9900" />
            <span class="rating-desc">{{ ratingText(currentReview.rating) }}</span>
          </div>
          <div class="review-text">{{ currentReview.content }}</div>
          <div class="review-time">评价时间：{{ formatDateTime(currentReview.createTime) }}</div>
          <div v-if="currentReview.reply" class="review-reply">
            <div class="reply-header">管理员回复：</div>
            <div class="reply-text">{{ currentReview.reply }}</div>
            <div class="reply-time">{{ formatDateTime(currentReview.replyTime) }}</div>
          </div>
          <div v-else class="reply-pending">暂无回复</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, submitCheckoutRequest, userPay } from '@/api/user'
import { submitReview as submitReviewApi, getMyReview } from '@/api/review'
import { InfoFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const reviewing = ref(false)
const checkingOut = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10 })

const reviewVisible = ref(false)
const viewReviewVisible = ref(false)
const checkoutVisible = ref(false)
const payVisible = ref(false)
const paying = ref(false)
const currentRecord = ref({})
const currentReview = ref({})
const reviewForm = reactive({ rating: 5, content: '' })
const payForm = reactive({ paidAmount: 0, paymentMethod: '微信支付' })

const ratingLabel = computed(() => {
  return { 1: '极差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' }[reviewForm.rating] || ''
})

function ratingText(r) {
  return { 1: '极差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' }[r] || ''
}

function statusTag(s) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }[s] || 'info'
}

function statusText(s) {
  return { 0: '待审核', 1: '已通过', 2: '已退房', 3: '已拒绝', 4: '退房申请中' }[s] || '-'
}

function formatDate(d) { return d ? dayjs(d).format('YYYY-MM-DD') : '-' }
function formatDateTime(d) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyBookings(queryParams)
    if (res.code === 200) {
      const records = res.data.records || []
      tableData.value = records
      total.value = res.data.total || 0
      for (const r of records) {
        if (r.checkInStatus === 2) {
          try {
            const rv = await getMyReview(r.id)
            r.hasReview = !!(rv.code === 200 && rv.data)
          } catch { r.hasReview = false }
        } else {
          r.hasReview = false
        }
      }
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleReview(row) {
  try {
    const rv = await getMyReview(row.id)
    if (rv.code === 200 && rv.data) {
      currentRecord.value = row
      currentReview.value = rv.data
      viewReviewVisible.value = true
    } else {
      currentRecord.value = row
      reviewForm.rating = 5
      reviewForm.content = ''
      reviewVisible.value = true
    }
  } catch {
    currentRecord.value = row
    reviewForm.rating = 5
    reviewForm.content = ''
    reviewVisible.value = true
  }
}

async function handleSubmitReview() {
  if (reviewForm.rating === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  reviewing.value = true
  try {
    const res = await submitReviewApi({
      checkInId: currentRecord.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    })
    if (res.code === 200) {
      ElMessage.success('评价提交成功，感谢您的反馈')
      reviewVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('评价提交失败')
    }
  } finally { reviewing.value = false }
}

function handlePayOpen(row) {
  currentRecord.value = row
  payForm.paidAmount = row.roomFee || 0
  payForm.paymentMethod = '微信支付'
  payVisible.value = true
}

async function handlePaySubmit() {
  paying.value = true
  try {
    const res = await userPay(currentRecord.value.id, payForm.paidAmount, payForm.paymentMethod)
    if (res.code === 200) {
      ElMessage.success('付款成功')
      payVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('付款失败')
    }
  } finally { paying.value = false }
}

function handleCheckoutOpen(row) {
  currentRecord.value = row
  checkoutVisible.value = true
}

async function handleCheckoutRequest() {
  try {
    await ElMessageBox.confirm(
      `确定要申请退房吗？管理员审核后将对费用进行确认。`,
      '申请退房',
      { confirmButtonText: '确认申请', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  checkingOut.value = true
  try {
    const res = await submitCheckoutRequest(currentRecord.value.id)
    if (res.code === 200) {
      ElMessage.success('退房申请已提交，请等待管理员审核')
      checkoutVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('退房申请提交失败')
    }
  } finally { checkingOut.value = false }
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
.muted { color: #c0c4cc; }

.rating-stars {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label {
  font-size: 14px;
  color: #606266;
}

.review-order-info {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  padding: 10px 14px;
  border-radius: 6px;
}

.review-content {
  margin-top: 16px;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.rating-desc {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 600;
}

.review-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  padding: 12px 14px;
  background: #f5f7fa;
  border-radius: 6px;
  min-height: 60px;
  margin-bottom: 10px;
}

.review-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.review-reply {
  margin-top: 14px;
  padding: 12px 14px;
  background: #ecf5ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;

  .reply-header {
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

.reply-pending {
  margin-top: 14px;
  font-size: 13px;
  color: #c0c4cc;
  font-style: italic;
}

.total-fee {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.checkout-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 14px;
  padding: 10px 14px;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 6px;
  font-size: 13px;
  color: #e6a23c;
  line-height: 1.5;

  .el-icon {
    flex-shrink: 0;
    margin-top: 1px;
  }
}
</style>
