package com.neu.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.hotel.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知消息Mapper接口
 *
 * 【功能说明】
 * - 继承BaseMapper，提供通知消息基础的CRUD操作
 * - 无需自定义复杂查询方法（Service层使用LambdaQueryWrapper）
 *
 * 【SQL映射文件】
 * - 对应XML：resources/mapper/NotificationMapper.xml
 *
 * 【关联关系】
 * - 对应实体：Notification
 * - 被NotificationService、CheckInService、VisitorService调用
 *
 * 【对应前端】
 * - API：@/api/notification.js
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    // 继承BaseMapper即可满足基本CRUD需求
}
