package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.DormRoomType;
import com.neu.hotel.mapper.DormRoomTypeMapper;
import com.neu.hotel.service.DormRoomTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormRoomTypeServiceImpl extends ServiceImpl<DormRoomTypeMapper, DormRoomType> implements DormRoomTypeService {

    @Override
    public List<DormRoomType> selectAll() {
        return baseMapper.selectList(null);
    }
}
