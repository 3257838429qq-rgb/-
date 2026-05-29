package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 *
 * 【数据库表】sys_notification - 系统通知表
 *
 * 【主要功能】
 * - 向用户推送系统通知
 * - 记录访客审核、预订、入住等业务提醒
 * - 支持已读/未读状态管理
 *
 * 【字段说明】
 * - id: 通知唯一标识（自增主键）
 * - userId: 接收通知的用户ID
 * - title: 通知标题
 * - content: 通知内容
 * - type: 通知类型（visitor_review=访客审核，booking_request=预订请求，checkout_request=退房请求，booking_approved=预订通过，booking_rejected=预订拒绝）
 * - businessId: 关联的业务ID（如访客ID、入住ID等）
 * - readStatus: 已读状态（0=未读，1=已读）
 *
 * 【关联关系】
 * - 通过 userId 关联 User 实体
 * - 被 VisitorService、CheckInService 等业务服务调用创建通知
 * - 通过 NotificationService 处理通知业务逻辑
 * - 通过 NotificationController 提供通知管理API
 *
 * 【对应前端】
 * - API：@/api/notification.js
 * - 组件：@/views/layout/ChatWidget.vue（聊天组件中的通知功能）
 */
@Data
@TableName("sys_notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 通知唯一标识

    /** 接收人用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /**
     * 业务类型: visitor_review, booking_request, checkout_request,
     * booking_approved, booking_rejected
     */
    private String type;

    /** 关联业务ID */
    private Long businessId;

    /** 是否已读: 0未读 1已读 */
    private Integer readStatus;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
