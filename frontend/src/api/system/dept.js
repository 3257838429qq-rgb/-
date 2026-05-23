import request from '@/utils/request'

export function getDeptList() {
  return request({
    url: '/system/dept/list',
    method: 'get'
  })
}

export function getDeptById(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'get'
  })
}

export function addDept(data) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

export function updateDept(data) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'delete'
  })
}
