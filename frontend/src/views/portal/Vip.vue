<!--
  VIP会员中心组件

  【模块说明】
  - 展示VIP会员信息和充值入口
  - 支持选择套餐和自定义金额充值

  【功能模块】
  1. VIP状态卡：显示会员等级、余额、折扣
  2. 充值套餐：铜牌、银牌、金牌、钻石
  3. 自定义充值：最低100元
  4. 支付方式：微信、支付宝
  5. 充值记录：历史充值流水

  【VIP等级体系】
  - 铜牌: 500元, 95折
  - 银牌: 1000元, 9折
  - 金牌: 3000元, 85折
  - 钻石: 5000元, 8折

  【API调用】
  - getVipMemberInfo: 获取会员信息
  - getVipPackages: 获取套餐列表
  - rechargeVip: 会员充值
  - getVipHistory: 获取充值历史

  【后端对应】
  - Controller: VipController
  - 路径: /vip/member, /vip/packages, /vip/recharge, /vip/history

  【路由对应】
  - /portal/vip
-->
<template>
  <div class="vip-center">
    <div class="page-header">
      <h2>VIP会员中心</h2>
      <p>成为VIP会员，享受专属折扣优惠</p>
    </div>

    <!-- VIP Status Card -->
    <div class="vip-status-card" :class="'level-' + (memberInfo.vipLevel || 0)">
      <div class="vip-badge">
        <el-icon v-if="memberInfo.vipLevel > 0" :size="48"><StarFilled /></el-icon>
        <el-icon v-else :size="48"><User /></el-icon>
      </div>
      <div class="vip-info">
        <h3>{{ getVipLevelName(memberInfo.vipLevel) }}</h3>
        <p class="vip-balance">账户余额：¥{{ (memberInfo.balance || 0).toFixed(2) }}</p>
        <p v-if="memberInfo.vipLevel > 0" class="vip-expire">
          有效期至：{{ formatDate(memberInfo.expireDate) }}
        </p>
        <p v-else class="vip-tip">充值即可成为VIP会员，享受订房折扣</p>
      </div>
      <div class="vip-discount" v-if="memberInfo.vipLevel > 0">
        <span class="discount-num">{{ Math.round((1 - (memberInfo.discountRate || 1)) * 100) }}</span>
        <span class="discount-unit">%</span>
        <span class="discount-label">订房折扣</span>
      </div>
    </div>

    <!-- VIP Packages -->
    <div class="packages-section">
      <h3>选择充值套餐</h3>
      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="pkg in packages" :key="pkg.id" :xs="24" :sm="12" :md="6">
          <div
            class="package-card"
            :class="{ active: selectedPackage === pkg.id }"
            @click="selectedPackage = pkg.id"
          >
            <div class="package-level">{{ pkg.name }}</div>
            <div class="package-amount">¥{{ pkg.minAmount }}</div>
            <div class="package-discount">{{ (pkg.discountRate * 100).toFixed(0) }}折</div>
            <div class="package-gift" v-if="pkg.giftRate > 0">
              额外赠送 {{ (pkg.giftRate * 100).toFixed(0) }}%
            </div>
            <div class="package-desc">{{ pkg.description }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Quick Recharge Amount -->
    <div class="recharge-section">
      <h3>自定义充值金额</h3>
      <div class="amount-input">
        <span class="yuan-symbol">¥</span>
        <el-input-number
          v-model="customAmount"
          :min="100"
          :max="100000"
          :step="100"
          size="large"
        />
      </div>
      <p class="amount-tip">最低充值100元，自定义金额需满足所选套餐的最低要求</p>
    </div>

    <!-- Payment Method -->
    <div class="payment-section">
      <h3>支付方式</h3>
      <el-radio-group v-model="paymentMethod" size="large">
        <el-radio-button label="微信支付">微信支付</el-radio-button>
        <el-radio-button label="支付宝">支付宝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Recharge Button -->
    <div class="recharge-action">
      <el-button type="primary" size="large" :loading="recharging" @click="handleRecharge">
        立即充值 {{ getRechargeAmount() }} 元
      </el-button>
    </div>

    <!-- Recharge History -->
    <div class="history-section" v-if="history.length > 0">
      <h3>充值记录</h3>
      <el-table :data="history" stripe>
        <el-table-column prop="rechargeNo" label="充值单号" width="180" />
        <el-table-column prop="amount" label="充值金额" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="giftAmount" label="赠送金额" width="120">
          <template #default="{ row }">
            <span class="gift-text" v-if="row.giftAmount > 0">+¥{{ row.giftAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120" />
        <el-table-column prop="createTime" label="充值时间">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, User } from '@element-plus/icons-vue'
import { getVipMemberInfo, getVipPackages, rechargeVip, getVipHistory } from '@/api/vip'

const memberInfo = ref({})
const packages = ref([])
const history = ref([])
const loading = ref(false)
const recharging = ref(false)
const selectedPackage = ref(null)
const customAmount = ref(500)
const paymentMethod = ref('微信支付')

const vipLevelNames = ['普通用户', '铜牌会员', '银牌会员', '金牌会员', '钻石会员']

function getVipLevelName(level) {
  return vipLevelNames[level] || '普通用户'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN')
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}

function getRechargeAmount() {
  const pkg = packages.value.find(p => p.id === selectedPackage.value)
  if (pkg && customAmount.value < pkg.minAmount) {
    return pkg.minAmount
  }
  return customAmount.value
}

async function fetchData() {
  loading.value = true
  try {
    const [memberRes, packagesRes, historyRes] = await Promise.all([
      getVipMemberInfo(),
      getVipPackages(),
      getVipHistory()
    ])
    if (memberRes.code === 200) {
      memberInfo.value = memberRes.data || {}
    }
    if (packagesRes.code === 200) {
      packages.value = packagesRes.data || []
      if (packages.value.length > 0 && !selectedPackage.value) {
        selectedPackage.value = packages.value[0].id
      }
    }
    if (historyRes.code === 200) {
      history.value = historyRes.data || []
    }
  } catch (e) {
    console.error('获取VIP信息失败', e)
  } finally {
    loading.value = false
  }
}

async function handleRecharge() {
  if (!selectedPackage.value) {
    ElMessage.warning('请选择充值套餐')
    return
  }
  const pkg = packages.value.find(p => p.id === selectedPackage.value)
  const amount = getRechargeAmount()
  if (amount < pkg.minAmount) {
    ElMessage.warning(`该套餐最低需充值 ${pkg.minAmount} 元`)
    return
  }

  recharging.value = true
  try {
    const res = await rechargeVip({
      packageId: selectedPackage.value,
      amount: amount,
      paymentMethod: paymentMethod.value
    })
    if (res.code === 200) {
      ElMessage.success('充值成功！')
      fetchData()
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (e) {
    ElMessage.error('充值失败')
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.vip-center {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    font-size: 15px;
    color: #909399;
  }
}

.vip-status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);

  &.level-0 {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.level-1 {
    background: linear-gradient(135deg, #b87333 0%, #cd7f32 100%);
    box-shadow: 0 8px 24px rgba(184, 115, 51, 0.3);
  }

  &.level-2 {
    background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
    box-shadow: 0 8px 24px rgba(192, 192, 192, 0.3);
  }

  &.level-3 {
    background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
    box-shadow: 0 8px 24px rgba(255, 215, 0, 0.3);
  }

  &.level-4 {
    background: linear-gradient(135deg, #00d4ff 0%, #7b2ff7 50%, #f107a3 100%);
    box-shadow: 0 8px 24px rgba(123, 47, 247, 0.3);
  }

  .vip-badge {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .vip-info {
    flex: 1;

    h3 {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .vip-balance {
      font-size: 15px;
      font-weight: 600;
      opacity: 0.95;
      margin-bottom: 4px;
    }

    .vip-expire {
      font-size: 14px;
      opacity: 0.9;
    }

    .vip-tip {
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .vip-discount {
    text-align: center;
    padding: 16px 24px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 12px;

    .discount-num {
      font-size: 48px;
      font-weight: 800;
      line-height: 1;
    }

    .discount-unit {
      font-size: 24px;
      font-weight: 700;
    }

    .discount-label {
      display: block;
      font-size: 12px;
      opacity: 0.9;
      margin-top: 4px;
    }
  }
}

.packages-section,
.recharge-section,
.payment-section,
.history-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }
}

.package-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;

  &:hover {
    border-color: #409eff;
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .package-level {
    font-size: 16px;
    font-weight: 600;
    color: #409eff;
    margin-bottom: 12px;
  }

  .package-amount {
    font-size: 28px;
    font-weight: 800;
    color: #303133;
    margin-bottom: 8px;
  }

  .package-discount {
    font-size: 20px;
    font-weight: 700;
    color: #f56c6c;
    margin-bottom: 8px;
  }

  .package-gift {
    font-size: 12px;
    color: #67c23a;
    margin-bottom: 8px;
  }

  .package-desc {
    font-size: 12px;
    color: #909399;
  }
}

.amount-input {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .yuan-symbol {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
  }
}

.amount-tip {
  font-size: 13px;
  color: #909399;
}

.payment-section {
  .el-radio-button {
    margin-right: 12px;
    margin-bottom: 12px;
  }
}

.recharge-action {
  text-align: center;
  margin-bottom: 32px;

  .el-button {
    min-width: 240px;
    height: 52px;
    font-size: 18px;
    font-weight: 600;
  }
}

.amount-text {
  color: #409eff;
  font-weight: 600;
}

.gift-text {
  color: #67c23a;
  font-weight: 600;
}
</style>
