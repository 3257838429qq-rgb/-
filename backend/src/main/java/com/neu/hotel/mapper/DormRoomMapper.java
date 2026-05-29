package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 房间Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供房间基础的CRUD操作
 * - 定义空闲房间查询方法
 *
 * 【自定义查询方法】
 * 1. selectAvailableRooms: 查询空闲房间列表
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/DormRoomMapper.xml
 *
 * 【关联关系】
 * - 对应实体：DormRoom
 * - 被DormRoomService、CheckInService调用
 *
 * 【对应前端】
 * - API：@/api/dorm/room.js
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {

    /**
     * 查询空闲房间列表
     * 用于用户预订时选择可用房间
     * @param page MyBatis-Plus分页对象
     * @param status 房间状态（1=空闲）
     * @return 空闲房间分页结果
     */
    IPage<DormRoom> selectAvailableRooms(Page<?> page, @Param("status") Integer status);
}
