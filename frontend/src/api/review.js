import request from '@/utils/request'

export function submitReview(data) {
  return request({
    url: '/review',
    method: 'post',
    data
  })
}

export function getMyReview(checkInId) {
  return request({
    url: '/review/my',
    method: 'get',
    params: { checkInId }
  })
}

export function getCheckInReview(checkInId) {
  return request({
    url: `/review/checkin/${checkInId}`,
    method: 'get'
  })
}

export function replyReview(id, reply) {
  return request({
    url: `/review/reply/${id}`,
    method: 'put',
    params: { reply }
  })
}

export function getRecentReviews(limit = 6) {
  return request({
    url: '/review/recent',
    method: 'get',
    params: { limit }
  })
}

export function getRoomReviews(roomId) {
  return request({
    url: `/review/room/${roomId}`,
    method: 'get'
  })
}
