package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.mapper.VisitorMapper;
import com.neu.hotel.service.VisitorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    @Override
    public IPage<Visitor> selectPage(Long current, Long size, Map<String, Object> params) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        return baseMapper.selectVisitorPage(page, params);
    }

    @Override
    public boolean review(Long visitorId, Integer status, Long reviewerId, String remark) {
        Visitor visitor = baseMapper.selectById(visitorId);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        visitor.setStatus(status);
        visitor.setReviewerId(reviewerId);
        visitor.setReviewRemark(remark);
        visitor.setReviewTime(LocalDateTime.now());
        return baseMapper.updateById(visitor) > 0;
    }

    @Override
    public IPage<Visitor> selectPendingVisitors(Long current, Long size) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, 0);
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Visitor> selectApprovedVisitors(Long current, Long size) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, 1);
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Visitor> selectAllVisitors(Long current, Long size, String name, String phone, Integer status) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Visitor::getName, name);
        }
        if (StringUtils.isNotBlank(phone)) {
            wrapper.like(Visitor::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(Visitor::getStatus, status);
        }
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
