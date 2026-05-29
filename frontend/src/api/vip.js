/**
 * VIP会员相关API接口
 *
 * 【模块说明】
 * - 管理VIP会员信息
 * - 处理会员充值和折扣计算
 *
 * 【VIP等级】
 * - 铜牌: 500元, 95折
 * - 银牌: 1000元, 9折
 * - 金牌: 3000元, 85折
 * - 钻石: 5000元, 8折
 *
 * 【接口列表】
 * 1. getVipMemberInfo: 获取会员信息
 * 2. getVipPackages: 获取VIP套餐
 * 3. rechargeVip: 会员充值
 * 4. getVipHistory: 获取充值历史
 * 5. getVipDiscount: 获取折扣率
 * 6. calculateDiscount: 计算折扣价格
 *
 * 【对应后端】
 * - Controller: VipController
 * - 路径前缀: /vip
 *
 * 【对应前端组件】
 * - VIP会员页: @/views/portal/Vip.vue
 */

import request from '@/utils/request'

/**
 * 获取当前用户的VIP会员信息
 * @returns {Object} { vipLevel, balance, totalRecharge, discountRate, status, expireDate }
 */
export function getVipMemberInfo() {
  return request({ url: '/vip/member', method: 'get' })
}

/**
 * 获取所有可用的VIP套餐列表
 * @returns {Array} 套餐列表
 */
export function getVipPackages() {
  return request({ url: '/vip/packages', method: 'get' })
}

/**
 * 会员充值
 * @param {Object} data - 充值参数 { packageId, amount, paymentMethod }
 * @returns {Object} 更新后的会员信息
 */
export function rechargeVip(data) {
  return request({ url: '/vip/recharge', method: 'post', data })
}

/**
 * 获取充值历史记录
 * @returns {Array} 充值记录列表
 */
export function getVipHistory() {
  return request({ url: '/vip/history', method: 'get' })
}

/**
 * 获取当前用户折扣率
 * @returns {Object} { discountRate, isVip }
 */
export function getVipDiscount() {
  return request({ url: '/vip/discount', method: 'get' })
}

/**
 * 计算折扣后的价格
 * @param {number} price - 原价
 * @returns {Object} { originalPrice, discountedPrice, discountRate, savedAmount, isVip }
 */
export function calculateDiscount(price) {
  return request({ url: '/vip/calculate', method: 'get', params: { price } })
}
