package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.Role;
import com.neu.hotel.mapper.RoleMapper;
import com.neu.hotel.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> selectAll() {
        return baseMapper.selectList(null);
    }
}
