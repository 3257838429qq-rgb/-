package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.Dept;
import com.neu.hotel.mapper.DeptMapper;
import com.neu.hotel.service.DeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Override
    public List<Dept> selectTree() {
        List<Dept> allDepts = baseMapper.selectList(null);
        return buildTree(allDepts, 0L);
    }

    private List<Dept> buildTree(List<Dept> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .collect(Collectors.toList());
    }
}
