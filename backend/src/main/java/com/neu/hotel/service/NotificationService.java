package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.common.result.PageResult;
import com.neu.hotel.entity.Notification;

public interface NotificationService extends IService<Notification> {

    PageResult getMyNotifications(Long userId, Long current, Long size);

    int getUnreadCount(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    void sendNotification(Long userId, String title, String content, String type, Long businessId);
}
