package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入住记录实体类
 *
 * 【数据库表】biz_check_in - 入住记录表
 *
 * 【主要功能】
 * - 记录每位客人的入住信息
 * - 管理房间预订、入住、退房流程
 * - 处理费用计算和支付
 *
 * 【字段说明】
 * - id: 入住记录唯一标识（自增主键）
 * - orderNo: 订单号（唯一，用于查询）
 * - visitorId: 关联的访客ID（对应 biz_visitor 表）
 * - roomId: 入住的房间ID（对应 biz_dorm_room 表）
 * - roomNo: 房间号（冗余字段，便于展示）
 * - checkInDate: 入住日期
 * - checkOutDate: 退房日期
 * - checkInStatus: 入住状态（1=已预订，2=已入住，3=已退房，4=已取消）
 * - nights: 入住天数
 * - roomFee: 房费
 * - otherFee: 其他费用
 * - totalFee: 总费用
 * - paidAmount: 已支付金额
 * - paymentStatus: 支付状态（1=已支付，0=待支付）
 * - paymentMethod: 支付方式
 * - checkInUserId/checkOutUserId: 入住/退房操作员ID
 * - checkInTime/checkOutTime: 入住/退房具体时间
 *
 * 【关联关系】
 * - 通过 visitorId 关联 Visitor 实体，获取访客信息
 * - 通过 roomId 关联 DormRoom 实体，获取房间信息
 * - 通过 CheckInService 处理入住业务逻辑
 * - 通过 CheckInController 提供入住管理API
 *
 * 【对应前端】
 * - 路由：/admin/dorm/checkin
 * - API：@/api/dorm/checkin.js
 * - Vue组件：@/views/dorm/checkin/index.vue
 */
@Data
@TableName("biz_check_in")
public class CheckIn implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 入住记录唯一标识

    private String orderNo;       // 订单号

    private Long visitorId;       // 关联访客ID

    @TableField(exist = false)
    private String idCard;        // 访客身份证（非数据库字段）

    private Long roomId;          // 关联房间ID

    private String roomNo;        // 房间号

    private LocalDate checkInDate;  // 入住日期

    private LocalDate checkOutDate; // 退房日期

    private Integer checkInStatus;  // 入住状态

    private Integer nights;         // 入住天数

    private BigDecimal roomFee;     // 房费

    private BigDecimal otherFee;     // 其他费用

    private BigDecimal totalFee;     // 总费用

    private BigDecimal paidAmount;   // 已支付金额

    private Integer paymentStatus;  // 支付状态

    private String paymentMethod;   // 支付方式

    private String remark;          // 备注

    private Long checkInUserId;     // 入住操作员ID

    private Long checkOutUserId;     // 退房操作员ID

    private LocalDateTime checkInTime;   // 入住时间

    private LocalDateTime checkOutTime;  // 退房时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    // ========== 非数据库字段（用于关联查询和展示） ==========

    @TableField(exist = false)
    private String visitorName;     // 访客姓名

    @TableField(exist = false)
    private String visitorPhone;    // 访客电话

    @TableField(exist = false)
    private String visitorIdCard;   // 访客身份证

    @TableField(exist = false)
    private Integer reviewRating;   // 评价评分

    @TableField(exist = false)
    private String reviewContent;   // 评价内容

    @TableField(exist = false)
    private LocalDateTime reviewTime;  // 评价时间

    @TableField(exist = false)
    private String reply;           // 评价回复

    @TableField(exist = false)
    private LocalDateTime replyTime; // 回复时间

    @TableField(exist = false)
    private Long reviewId;          // 评价ID

    @TableField(exist = false)
    private Boolean hasReview;     // 是否已评价

    @TableLogic
    private Integer deleted;        // 逻辑删除标记
}
