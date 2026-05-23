package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.SysOperLog;
import com.neu.hotel.mapper.SysOperLogMapper;
import com.neu.hotel.service.SysOperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public IPage<SysOperLog> selectPage(Long current, Long size, SysOperLog log) {
        Page<SysOperLog> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (log != null) {
            if (StringUtils.isNotBlank(log.getTitle())) {
                wrapper.like(SysOperLog::getTitle, log.getTitle());
            }
            if (StringUtils.isNotBlank(log.getOperType())) {
                wrapper.eq(SysOperLog::getOperType, log.getOperType());
            }
            if (log.getStatus() != null) {
                wrapper.eq(SysOperLog::getStatus, log.getStatus());
            }
            if (log.getUserId() != null) {
                wrapper.eq(SysOperLog::getUserId, log.getUserId());
            }
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
