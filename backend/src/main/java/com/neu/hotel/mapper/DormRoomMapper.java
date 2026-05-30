package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * 查询房态图数据
     * LEFT JOIN入住记录和访客表，获取所有房间 + 当前入住客人信息
     * @param status 可选的房间状态过滤（null=全部）
     * @return 房间列表（含客人信息）
     */
    List<DormRoom> selectStatusBoard(@Param("status") Integer status);

    /**
     * 按日期范围查询可用房间
     * 排除在指定日期范围内有冲突入住记录的房间
     * @param startDate 查询开始日期
     * @param endDate 查询结束日期
     * @return 可用房间列表
     */
    List<DormRoom> selectAvailableByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
