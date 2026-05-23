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

export function checkOut(id, otherFee, paymentMethod) {
  return request({
    url: `/dorm/checkin/checkout/${id}`,
    method: 'put',
    params: { otherFee, paymentMethod }
  })
}

export function recordPayment(id, paidAmount, paymentMethod) {
  return request({
    url: `/dorm/checkin/pay/${id}`,
    method: 'put',
    params: { paidAmount, paymentMethod }
  })
}

export function deleteCheckIn(id) {
  return request({
    url: `/dorm/checkin/${id}`,
    method: 'delete'
  })
}
