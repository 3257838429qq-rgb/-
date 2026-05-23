<template>
  <div class="portal-visit">
    <div class="page-header">
      <h2>来访预约</h2>
      <p>提交来访预约申请，审核通过后方可到访</p>
    </div>

    <el-row :gutter="24">
      <el-col :md="16" :span="24">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">提交预约申请</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="form.name" placeholder="请输入您的姓名" />
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
                <el-form-item label="来访单位">
                  <el-input v-model="form.company" placeholder="请输入您的单位" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来访日期" prop="visitDate">
                  <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD"
                    placeholder="选择日期" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来访时段" prop="visitTimeSlot">
                  <el-select v-model="form.visitTimeSlot" style="width:100%">
                    <el-option label="上午" value="上午" />
                    <el-option label="下午" value="下午" />
                    <el-option label="全天" value="全天" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接待人">
                  <el-input v-model="form.hostPerson" placeholder="接待人姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接待人电话">
                  <el-input v-model="form.hostPersonPhone" placeholder="接待人电话" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="来访目的" prop="visitPurpose">
                  <el-input v-model="form.visitPurpose" type="textarea" :rows="3" placeholder="请描述来访目的" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="车牌号">
                  <el-input v-model="form.carPlate" placeholder="如有车辆请填写" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注">
                  <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他补充说明" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="submitForm">提交预约</el-button>
              <el-button @click="formRef?.resetFields()">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :md="8" :span="24">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">我的预约记录</span>
          </template>
          <div v-loading="loading">
            <div v-if="myVisits.length === 0" class="empty-hint">
              <el-empty description="暂无预约记录" :image-size="80" />
            </div>
            <div v-for="v in myVisits" :key="v.id" class="visit-item">
              <div class="visit-header">
                <span class="visit-date">{{ v.visitDate }}</span>
                <el-tag :type="visitStatusTag(v.status)" size="small">
                  {{ visitStatusText(v.status) }}
                </el-tag>
              </div>
              <div class="visit-body">
                <p><strong>{{ v.visitPurpose }}</strong></p>
                <p class="visit-meta">
                  <span>{{ v.visitTimeSlot }}</span>
                  <span v-if="v.hostPerson">接待人: {{ v.hostPerson }}</span>
                </p>
                <p v-if="v.reviewRemark" class="review-note">审核备注: {{ v.reviewRemark }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addVisitor, getVisitorList } from '@/api/visitor'
import { useUserStore } from '@/stores/user'

const submitting = ref(false)
const loading = ref(false)
const myVisits = ref([])
const formRef = ref()
const userStore = useUserStore()

const form = reactive({
  name: '', phone: '', idCard: '', company: '',
  visitDate: '', visitTimeSlot: '', hostPerson: '', hostPersonPhone: '',
  visitPurpose: '', carPlate: '', remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择来访日期', trigger: 'change' }],
  visitTimeSlot: [{ required: true, message: '请选择来访时段', trigger: 'change' }],
  visitPurpose: [{ required: true, message: '请填写来访目的', trigger: 'blur' }]
}

function visitStatusTag(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
}

function visitStatusText(s) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '-'
}

async function fetchMyVisits() {
  loading.value = true
  try {
    const phone = userStore.userInfo?.phone || form.phone
    const res = await getVisitorList({ current: 1, size: 20, phone })
    if (res.code === 200) myVisits.value = res.data.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await addVisitor({ ...form })
      if (res.code === 200) {
        ElMessage.success('预约已提交，请等待审核')
        formRef.value.resetFields()
        fetchMyVisits()
      }
    } catch (e) { console.error(e) }
    finally { submitting.value = false }
  })
}

onMounted(fetchMyVisits)
</script>

<style lang="scss" scoped>
.portal-visit {
  animation: fadeIn 0.4s ease;
  max-width: 1100px;
}

.page-header {
  margin-bottom: 24px;

  h2 { font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 6px; }
  p { font-size: 14px; color: #909399; }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;

  &::before {
    content: '';
    position: absolute;
    left: 0; top: 50%;
    transform: translateY(-50%);
    width: 3px; height: 16px;
    background: #409eff;
    border-radius: 2px;
  }
}

.empty-hint {
  padding: 24px 0;
}

.visit-item {
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child { border-bottom: none; }

  .visit-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;

    .visit-date { font-size: 14px; font-weight: 600; color: #303133; }
  }

  .visit-body {
    p { margin: 4px 0; font-size: 13px; color: #606266; }
    .visit-meta { display: flex; gap: 16px; color: #909399; font-size: 12px; }
    .review-note { color: #e6a23c; font-size: 12px; }
  }
}
</style>
