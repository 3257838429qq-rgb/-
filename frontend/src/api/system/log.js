import request from '@/utils/request'

export function getLogList(params) {
  return request({
    url: '/system/oper-log/page',
    method: 'get',
    params
  })
}

export function deleteLog(id) {
  return request({
    url: `/system/oper-log/${id}`,
    method: 'delete'
  })
}

export function batchDeleteLog(ids) {
  return request({
    url: '/system/oper-log/batch',
    method: 'delete',
    params: { ids }
  })
}
