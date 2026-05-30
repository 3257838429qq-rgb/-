package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 房间实体类
 *
 * 【数据库表】biz_dorm_room - 房间表
 *
 * 【主要功能】
 * - 管理酒店所有房间的信息
 * - 记录房间状态（空闲、入住、维护、停用）
 * - 关联房间类型和价格信息
 *
 * 【字段说明】
 * - id: 房间唯一标识（自增主键）
 * - roomNo: 房间号（如"301"表示3楼01房间）
 * - roomName: 房间名称/别名
 * - roomTypeId: 关联的房间类型ID（对应 biz_dorm_room_type 表）
 * - floor: 所在楼层
 * - capacity: 当前可入住人数
 * - maxCapacity: 最大可入住人数
 * - price: 房间单价
 * - status: 房间状态（1=空闲，2=入住，3=维护，4=停用）
 * - facility: 房间设施（JSON格式存储）
 * - remark: 备注信息
 * - priceMin/priceMax: 非数据库字段，用于价格区间查询
 *
 * 【关联关系】
 * - 通过 roomTypeId 关联 DormRoomType 实体
 * - 通过 CheckIn 实体的一对多关系，记录入住记录
 * - 通过 DormRoomService 操作用户数据
 * - 通过 DormRoomController 提供房间管理API
 *
 * 【对应前端】
 * - 路由：/admin/dorm/room
 * - API：@/api/dorm/room.js
 * - Vue组件：@/views/dorm/room/index.vue
 */
@Data
@TableName("biz_dorm_room")
public class DormRoom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;           // 房间唯一标识

    private String roomNo;     // 房间号

    private String roomName;   // 房间名称

    private Long roomTypeId;   // 关联房间类型ID

    private String floor;      // 所在楼层

    private Integer capacity;   // 当前可入住人数

    private Integer maxCapacity; // 最大可入住人数

    private BigDecimal price;  // 房间单价

    private Integer status;     // 房间状态

    private String facility;   // 房间设施

    private String remark;      // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;   // 逻辑删除标记

    // ========== 非数据库字段（用于查询和展示） ==========

    @TableField(exist = false)
    private BigDecimal priceMin;  // 价格区间最小值

    @TableField(exist = false)
    private BigDecimal priceMax;  // 价格区间最大值

    // ========== 非数据库字段（房态图用——关联客人信息） ==========

    @TableField(exist = false)
    private String guestName;         // 当前入住客人姓名

    @TableField(exist = false)
    private String guestPhone;        // 当前入住客人手机号

    @TableField(exist = false)
    private LocalDate guestCheckInDate;   // 当前入住客人入住日期

    @TableField(exist = false)
    private LocalDate guestCheckOutDate;  // 当前入住客人预退日期

    @TableField(exist = false)
    private Long checkInId;          // 当前入住记录ID（快捷退房用）
}
