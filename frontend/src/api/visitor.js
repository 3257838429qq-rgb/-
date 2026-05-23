import request from '@/utils/request'

export function getVisitorList(params) {
  return request({ url: '/visitor/page', method: 'get', params })
}

export function getPendingVisitors(params) {
  return request({ url: '/visitor/pending', method: 'get', params })
}

export function getApprovedVisitors(params) {
  return request({ url: '/visitor/approved', method: 'get', params })
}

export function getVisitorById(id) {
  return request({ url: `/visitor/${id}`, method: 'get' })
}

export function addVisitor(data) {
  return request({ url: '/visitor', method: 'post', data })
}

export function updateVisitor(data) {
  return request({ url: '/visitor', method: 'put', data })
}

export function reviewVisitor(id, status, remark) {
  return request({ url: `/visitor/review/${id}`, method: 'put', params: { status, remark } })
}

export function deleteVisitor(id) {
  return request({ url: `/visitor/${id}`, method: 'delete' })
}
