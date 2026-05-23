import request from '@/utils/request'

export function getRoomTypeList() {
  return request({
    url: '/dorm/room-type/list',
    method: 'get'
  })
}

export function getRoomTypeById(id) {
  return request({
    url: `/dorm/room-type/${id}`,
    method: 'get'
  })
}

export function addRoomType(data) {
  return request({
    url: '/dorm/room-type',
    method: 'post',
    data
  })
}

export function updateRoomType(data) {
  return request({
    url: '/dorm/room-type',
    method: 'put',
    data
  })
}

export function deleteRoomType(id) {
  return request({
    url: `/dorm/room-type/${id}`,
    method: 'delete'
  })
}
