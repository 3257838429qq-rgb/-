package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.DormRoom;

public interface DormRoomService extends IService<DormRoom> {

    IPage<DormRoom> selectPage(Long current, Long size, DormRoom room);

    IPage<DormRoom> selectAvailableRooms(Long current, Long size);

    boolean updateStatus(Long roomId, Integer status);
}
