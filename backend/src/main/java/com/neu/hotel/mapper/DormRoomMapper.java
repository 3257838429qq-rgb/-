package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.hotel.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {

    IPage<DormRoom> selectAvailableRooms(Page<?> page, @Param("status") Integer status);
}
