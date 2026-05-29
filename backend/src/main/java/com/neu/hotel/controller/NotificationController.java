package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 通知消息控制器
 *
 * 【功能说明】
 * - 管理用户收到的系统通知消息
 * - 提供消息已读/未读状态管理
 * - 支持消息列表查询和批量标记已读
 *
 * 【API前缀】/notification
 *
 * 【主要接口】
 * 1. 分页查询我的消息 - GET /notification/page
 * 2. 获取未读消息数量 - GET /notification/unread-count
 * 3. 标记单条消息已读 - PUT /notification/read/{id}
 * 4. 标记全部消息已读 - PUT /notification/read-all
 *
 * 【关联关系】
 * - 依赖 NotificationService 处理通知业务逻辑
 * - 关联 Notification 实体
 * - 被 CheckInServiceImpl、VisitorServiceImpl 调用发送通知
 *
 * 【对应前端】
 * - API：@/api/notification.js
 * - Vue组件：@/views/layout/ChatWidget.vue（聊天组件中的消息入口）
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 分页查询当前用户的通知消息列表
     * GET /notification/page?current=1&size=10
     * 返回消息列表，按创建时间倒序排列
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getMyNotifications(userId, current, size));
    }

    /**
     * 获取当前用户的未读消息数量
     * GET /notification/unread-count
     * 用于前端显示红点标记
     */
    @GetMapping("/unread-count")
    public Result getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getUnreadCount(userId));
    }

    /**
     * 标记单条消息为已读
     * PUT /notification/read/{id}
     */
    @PutMapping("/read/{id}")
    public Result markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    /**
     * 标记当前用户所有消息为已读
     * PUT /notification/read-all
     */
    @PutMapping("/read-all")
    public Result markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }
}
