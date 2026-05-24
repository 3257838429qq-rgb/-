import request from '@/utils/request'

export function getCheckInList(params) {
  return request({
    url: '/dorm/checkin/page',
    method: 'get',
    params
  })
}

export function getActiveCheckIns(params) {
  return request({
    url: '/dorm/checkin/active',
    method: 'get',
    params
  })
}

export function getStatistics() {
  return request({
    url: '/dorm/checkin/statistics',
    method: 'get'
  })
}

export function getCheckInById(id) {
  return request({
    url: `/dorm/checkin/${id}`,
    method: 'get'
  })
}

export function addCheckIn(data) {
  return request({
    url: '/dorm/checkin',
    method: 'post',
    data
  })
}

// 退房申请（用户提交，管理员审核）
export function submitCheckoutRequest(id) {
  return request({
    url: `/dorm/checkin/checkout-request/${id}`,
    method: 'post'
  })
}

// 管理员审核退房申请
export function approveCheckout(id, otherFee, paymentMethod) {
  return request({
    url: `/dorm/checkin/checkout-approve/${id}`,
    method: 'put',
    data: { otherFee, paymentMethod }
  })
}

// 管理员拒绝退房申请
export function rejectCheckout(id) {
  return request({
    url: `/dorm/checkin/checkout-reject/${id}`,
    method: 'put'
  })
}

// 管理员直接退房（管理员使用）
export function checkOut(id, otherFee, paymentMethod) {
  return request({
    url: `/dorm/checkin/checkout/${id}`,
    method: 'put',
    data: { otherFee, paymentMethod }
  })
}

// 记录支付
export function recordPayment(id, paidAmount, paymentMethod) {
  return request({
    url: `/dorm/checkin/pay/${id}`,
    method: 'put',
    data: { paidAmount, paymentMethod }
  })
}

export function deleteCheckIn(id) {
  return request({
    url: `/dorm/checkin/${id}`,
    method: 'delete'
  })
}
