import request from '@/utils/request'

// Room browsing (public)
export function getRoomList(params) {
  return request({ url: '/dorm/room/page', method: 'get', params })
}

export function getRoomTypeList() {
  return request({ url: '/dorm/room-type/list', method: 'get' })
}

export function getAvailableRooms(params) {
  return request({ url: '/dorm/room/available', method: 'get', params })
}

// My bookings
export function submitBooking(data) {
  return request({ url: '/dorm/checkin/booking', method: 'post', data })
}

export function getMyBookings(params) {
  return request({ url: '/dorm/checkin/my-bookings', method: 'get', params })
}

// Admin approval
export function getPendingCheckIns(params) {
  return request({ url: '/dorm/checkin/pending', method: 'get', params })
}

export function approveBooking(id) {
  return request({ url: `/dorm/checkin/approve/${id}`, method: 'put' })
}

export function rejectBooking(id, reason) {
  return request({ url: `/dorm/checkin/reject/${id}`, method: 'put', params: { reason } })
}

// Profile
export function getProfile() {
  return request({ url: '/auth/info', method: 'get' })
}

export function updateProfile(data) {
  return request({ url: '/system/user', method: 'put', data })
}
