import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/system/user/page',
    method: 'get',
    params
  })
}

export function getUserAll() {
  return request({
    url: '/system/user/list',
    method: 'get'
  })
}

export function getUserById(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

export function resetPassword(id) {
  return request({
    url: `/system/user/reset-password/${id}`,
    method: 'put'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/system/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export function updatePassword(data) {
  return request({
    url: '/system/user/password',
    method: 'put',
    params: data
  })
}
