import request from '@/utils/request'

export function getRoomList(params) {
  return request({
    url: '/dorm/room/page',
    method: 'get',
    params
  })
}

export function getAvailableRooms(params) {
  return request({
    url: '/dorm/room/available',
    method: 'get',
    params
  })
}

export function getRoomTypes() {
  return request({
    url: '/dorm/room/types',
    method: 'get'
  })
}

export function getRoomById(id) {
  return request({
    url: `/dorm/room/${id}`,
    method: 'get'
  })
}

export function addRoom(data) {
  return request({
    url: '/dorm/room',
    method: 'post',
    data
  })
}

export function updateRoom(data) {
  return request({
    url: '/dorm/room',
    method: 'put',
    data
  })
}

export function deleteRoom(id) {
  return request({
    url: `/dorm/room/${id}`,
    method: 'delete'
  })
}

export function updateRoomStatus(id, status) {
  return request({
    url: `/dorm/room/status/${id}`,
    method: 'put',
    params: { status }
  })
}
