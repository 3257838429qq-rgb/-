package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 *
 * 【数据库表】biz_review - 评价表
 *
 * 【主要功能】
 * - 记录客人入住后的评价信息
 * - 支持评价回复功能
 * - 为酒店服务改进提供依据
 *
 * 【字段说明】
 * - id: 评价唯一标识（自增主键）
 * - checkInId: 关联的入住记录ID（对应 biz_check_in 表）
 * - userId: 评价人ID（对应 sys_user 表）
 * - roomId: 入住房间ID（对应 biz_dorm_room 表）
 * - rating: 评分（1-5星）
 * - content: 评价内容
 * - reply: 管理员回复内容
 * - replyTime: 回复时间
 * - replyUserId: 回复人ID
 * - roomNo/checkInDate/checkOutDate: 非数据库字段，用于展示
 *
 * 【关联关系】
 * - 通过 checkInId 关联 CheckIn 实体
 * - 通过 roomId 关联 DormRoom 实体
 * - 通过 ReviewService 处理评价业务逻辑
 * - 通过 ReviewController 提供评价管理API
 *
 * 【对应前端】
 * - 路由：/admin/review（后台管理）
 * - 路由：/portal/review（前台展示）
 * - API：@/api/review.js
 * - Vue组件：@/views/visitor/review/index.vue
 */
@Data
@TableName("biz_review")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 评价唯一标识

    private Long checkInId;       // 关联入住记录ID

    private Long userId;         // 评价人ID

    private Long roomId;         // 房间ID

    private Integer rating;       // 评分

    private String content;       // 评价内容

    private String reply;         // 回复内容

    private LocalDateTime replyTime;  // 回复时间

    private Long replyUserId;     // 回复人ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    // ========== 非数据库字段（用于关联查询和展示） ==========

    @TableField(exist = false)
    private String roomNo;        // 房间号

    @TableField(exist = false)
    private String checkInDate;   // 入住日期

    @TableField(exist = false)
    private String checkOutDate;  // 退房日期

    @TableLogic
    private Integer deleted;       // 逻辑删除标记
}
