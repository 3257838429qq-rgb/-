package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.DormRoom;

import java.time.LocalDate;
import java.util.List;

public interface DormRoomService extends IService<DormRoom> {

    IPage<DormRoom> selectPage(Long current, Long size, DormRoom room);

    IPage<DormRoom> selectAvailableRooms(Long current, Long size);

    boolean updateStatus(Long roomId, Integer status);

    /**
     * 查询房态图数据
     * 获取所有房间 + 当前入住客人信息
     * @param status 可选的房间状态过滤
     * @return 房间列表（含客人信息）
     */
    List<DormRoom> selectStatusBoard(Integer status);

    /**
     * 按日期范围查询可用房间
     * 排除在指定日期范围内有冲突入住记录的房间
     * @param startDate 入住日期
     * @param endDate 退房日期
     * @return 可用房间列表
     */
    List<DormRoom> selectAvailableByDateRange(LocalDate startDate, LocalDate endDate);
}
