package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.DormRoomType;

import java.util.List;

public interface DormRoomTypeService extends IService<DormRoomType> {

    List<DormRoomType> selectAll();
}
