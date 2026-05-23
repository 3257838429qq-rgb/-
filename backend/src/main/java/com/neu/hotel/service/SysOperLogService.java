package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {

    IPage<SysOperLog> selectPage(Long current, Long size, SysOperLog log);
}
