package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.result.PageResult;
import com.neu.hotel.entity.Notification;
import com.neu.hotel.mapper.NotificationMapper;
import com.neu.hotel.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知消息服务实现类
 *
 * 【功能说明】
 * - 管理用户的通知消息
 * - 提供消息分页查询
 * - 管理消息已读状态
 * - 支持批量标记已读
 *
 * 【通知类型(type)】
 * - booking_request: 新预订申请
 * - booking_approved: 预订审核通过
 * - booking_rejected: 预订审核拒绝
 * - checkout_request: 退房申请
 * - checkout_approved: 退房审核通过
 * - checkout_rejected: 退房审核拒绝
 * - visitor_approved: 访客预约通过
 * - visitor_rejected: 访客预约拒绝
 *
 * 【关联关系】
 * - 依赖 NotificationMapper 操作通知数据
 * - 被 CheckInServiceImpl、VisitorServiceImpl 调用发送通知
 * - 被 NotificationController 调用
 *
 * 【对应前端】
 * - API：@/api/notification.js
 * - Vue组件：@/views/layout/ChatWidget.vue
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    /**
     * 分页查询当前用户的通知消息
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 消息分页结果
     */
    @Override
    public PageResult getMyNotifications(Long userId, Long current, Long size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime);  // 按时间倒序

        Page<Notification> page = new Page<>(current, size);
        Page<Notification> result = this.page(page, wrapper);

        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getReadStatus, 0);  // 0=未读
        return (int) this.count(wrapper);
    }

    /**
     * 标记单条消息为已读
     * @param id 消息ID
     * @param userId 用户ID（验证归属）
     */
    @Override
    public void markAsRead(Long id, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId);

        Notification notification = this.getOne(wrapper);
        if (notification != null) {
            notification.setReadStatus(1);  // 1=已读
            this.updateById(notification);
        }
    }

    /**
     * 标记当前用户所有消息为已读
     * @param userId 用户ID
     */
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getReadStatus, 0);  // 未读

        List<Notification> unreadList = this.list(wrapper);
        unreadList.forEach(n -> n.setReadStatus(1));
        this.updateBatchById(unreadList);
    }

    /**
     * 发送通知（供其他Service调用）
     * @param userId 接收用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param businessId 关联业务ID（如订单ID）
     */
    @Override
    public void sendNotification(Long userId, String title, String content, String type, Long businessId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessId(businessId);
        notification.setReadStatus(0);  // 默认未读
        this.save(notification);
    }
}
