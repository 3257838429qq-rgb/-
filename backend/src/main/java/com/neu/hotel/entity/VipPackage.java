package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP套餐实体类
 *
 * 【数据库表】biz_vip_package - VIP套餐表
 *
 * 【主要功能】
 * - 定义VIP会员等级套餐
 * - 设置各级别的充值门槛和折扣
 * - 配置充值赠送规则
 *
 * 【字段说明】
 * - id: 套餐唯一标识（自增主键）
 * - name: 套餐名称（如"银卡会员"、"金卡会员"）
 * - level: 等级数值（1=青铜，2=白银，3=黄金，4=钻石）
 * - minAmount: 成为该等级需要的最低累计充值金额
 * - discountRate: 享受的折扣率
 * - description: 套餐描述/权益说明
 * - giftRate: 充值赠送比例（如0.1表示充值100送10）
 * - status: 状态（1=启用，0=停用）
 *
 * 【关联关系】
 * - 被 VipMember 实体引用，获取会员等级权益
 * - 通过 VipService 处理套餐业务逻辑
 * - 通过 VipController 提供套餐管理API
 *
 * 【对应前端】
 * - 路由：/portal/vip
 * - API：@/api/vip.js
 * - Vue组件：@/views/portal/Vip.vue
 */
@Data
@TableName("biz_vip_package")
public class VipPackage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 套餐唯一标识

    private String name;          // 套餐名称

    private Integer level;        // 等级数值

    private BigDecimal minAmount; // 最低充值金额

    private BigDecimal discountRate;   // 折扣率

    private String description;    // 套餐描述

    private BigDecimal giftRate;   // 赠送比例

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;       // 逻辑删除标记
}
