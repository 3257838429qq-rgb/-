package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.Visitor;

import java.util.Map;

public interface VisitorService extends IService<Visitor> {

    IPage<Visitor> selectPage(Long current, Long size, Map<String, Object> params);

    boolean review(Long visitorId, Integer status, Long reviewerId, String remark);

    IPage<Visitor> selectPendingVisitors(Long current, Long size);

    IPage<Visitor> selectApprovedVisitors(Long current, Long size);

    IPage<Visitor> selectAllVisitors(Long current, Long size, String name, String phone, Integer status);
}
