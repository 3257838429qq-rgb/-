/**
 * 房间管理相关API接口
 *
 * 【模块说明】
 * - 管理酒店房间信息
 * - 查询空闲房间和房间类型
 *
 * 【接口列表】
 * 1. getRoomList: 分页查询房间
 * 2. getAvailableRooms: 查询空闲房间
 * 3. getRoomTypes: 获取房间类型
 * 4. getRoomById: 获取房间详情
 * 5. addRoom: 新增房间
 * 6. updateRoom: 修改房间
 * 7. updateRoomStatus: 修改房间状态
 * 8. deleteRoom: 删除房间
 *
 * 【对应后端】
 * - Controller: DormRoomController
 * - 路径前缀: /dorm/room
 *
 * 【对应前端组件】
 * - 房间管理: @/views/dorm/room/index.vue
 * - 客房浏览: @/views/portal/Rooms.vue
 */

import request from '@/utils/request'

/**
 * 分页查询房间列表
 * @param {Object} params - 查询参数 { current, size, roomNo, roomName, status, floor, roomTypeId }
 * @returns {Object} 分页结果
 */
export function getRoomList(params) {
  return request({
    url: '/dorm/room/page',
    method: 'get',
    params
  })
}

/**
 * 查询空闲房间（用户预订时使用）
 * @param {Object} params - 查询参数 { current, size }
 * @returns {Object} 空闲房间列表
 */
export function getAvailableRooms(params) {
  return request({
    url: '/dorm/room/available',
    method: 'get',
    params
  })
}

/**
 * 获取所有房间类型
 * @returns {Array} 房间类型列表
 */
export function getRoomTypes() {
  return request({
    url: '/dorm/room/types',
    method: 'get'
  })
}

/**
 * 获取房间详情
 * @param {number} id - 房间ID
 * @returns {Object} 房间详情
 */
export function getRoomById(id) {
  return request({
    url: `/dorm/room/${id}`,
    method: 'get'
  })
}

/**
 * 新增房间
 * @param {Object} data - 房间信息 { roomNo, roomName, roomTypeId, floor, price, status, ... }
 * @returns {boolean} 是否成功
 */
export function addRoom(data) {
  return request({
    url: '/dorm/room',
    method: 'post',
    data
  })
}

/**
 * 修改房间信息
 * @param {Object} data - 房间信息
 * @returns {boolean} 是否成功
 */
export function updateRoom(data) {
  return request({
    url: '/dorm/room',
    method: 'put',
    data
  })
}

/**
 * 删除房间
 * @param {number} id - 房间ID
 * @returns {boolean} 是否成功
 */
export function deleteRoom(id) {
  return request({
    url: `/dorm/room/${id}`,
    method: 'delete'
  })
}

/**
 * 修改房间状态
 * @param {number} id - 房间ID
 * @param {number} status - 状态（1=空闲, 2=入住, 3=维护, 4=停用）
 * @returns {boolean} 是否成功
 */
export function updateRoomStatus(id, status) {
  return request({
    url: `/dorm/room/status/${id}`,
    method: 'put',
    params: { status }
  })
}
