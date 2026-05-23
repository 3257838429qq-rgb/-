package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.mapper.DormRoomMapper;
import com.neu.hotel.service.DormRoomService;
import org.springframework.stereotype.Service;

@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {

    @Override
    public IPage<DormRoom> selectPage(Long current, Long size, DormRoom room) {
        Page<DormRoom> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        if (room.getRoomNo() != null && !room.getRoomNo().isEmpty()) {
            wrapper.like(DormRoom::getRoomNo, room.getRoomNo());
        }
        if (room.getRoomName() != null && !room.getRoomName().isEmpty()) {
            wrapper.like(DormRoom::getRoomName, room.getRoomName());
        }
        if (room.getStatus() != null) {
            wrapper.eq(DormRoom::getStatus, room.getStatus());
        }
        if (room.getFloor() != null && !room.getFloor().isEmpty()) {
            wrapper.eq(DormRoom::getFloor, room.getFloor());
        }
        if (room.getRoomTypeId() != null) {
            wrapper.eq(DormRoom::getRoomTypeId, room.getRoomTypeId());
        }
        if (room.getPriceMin() != null) {
            wrapper.ge(DormRoom::getPrice, room.getPriceMin());
        }
        if (room.getPriceMax() != null) {
            wrapper.le(DormRoom::getPrice, room.getPriceMax());
        }
        wrapper.orderByDesc(DormRoom::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<DormRoom> selectAvailableRooms(Long current, Long size) {
        Page<DormRoom> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DormRoom::getStatus, 1);
        wrapper.orderByAsc(DormRoom::getRoomNo);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean updateStatus(Long roomId, Integer status) {
        DormRoom room = baseMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        room.setStatus(status);
        return baseMapper.updateById(room) > 0;
    }
}
