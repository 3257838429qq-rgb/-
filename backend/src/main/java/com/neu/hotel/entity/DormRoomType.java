package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房间类型实体类
 *
 * 【数据库表】biz_dorm_room_type - 房间类型表
 *
 * 【主要功能】
 * - 定义酒店的房型分类（如单人间、标准间、套房等）
 * - 设置每种房型的基本价格和容纳人数
 * - 管理房型的设施配置
 *
 * 【字段说明】
 * - id: 类型唯一标识（自增主键）
 * - name: 房型名称（如"单人间"、"套房"）
 * - description: 房型描述
 * - basePrice: 基础价格
 * - bedCount: 床位数量
 * - facility: 设施配置（JSON格式）
 * - status: 状态（1=启用，0=停用）
 *
 * 【关联关系】
 * - 被 DormRoom 实体通过 roomTypeId 引用（一对多）
 * - 通过 DormRoomTypeService 处理房型业务逻辑
 * - 通过 DormRoomTypeController 提供房型管理API
 *
 * 【对应前端】
 * - 路由：/admin/dorm/room-type
 * - API：@/api/dorm/roomType.js
 * - Vue组件：@/views/dorm/room-type/index.vue
 */
@Data
@TableName("biz_dorm_room_type")
public class DormRoomType implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 类型唯一标识

    private String name;          // 房型名称

    private String description;   // 房型描述

    private BigDecimal basePrice; // 基础价格

    private Integer bedCount;     // 床位数量

    private String facility;      // 设施配置

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;      // 逻辑删除标记
}
