package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP充值记录实体类
 *
 * 【数据库表】biz_vip_recharge - VIP充值记录表
 *
 * 【主要功能】
 * - 记录VIP会员的每次充值
 * - 管理充值金额和赠送金额
 * - 追踪充值支付方式
 *
 * 【字段说明】
 * - id: 充值记录唯一标识（自增主键）
 * - userId: 充值用户ID（对应 sys_user 表）
 * - memberId: 会员ID（对应 biz_vip_member 表）
 * - rechargeNo: 充值流水号
 * - amount: 充值金额
 * - giftAmount: 赠送金额
 * - paymentMethod: 支付方式
 * - status: 状态（1=成功，0=失败）
 *
 * 【关联关系】
 * - 通过 memberId 关联 VipMember 实体
 * - 通过 VipService 处理充值业务逻辑
 * - 通过 VipController 提供充值管理API
 *
 * 【对应前端】
 * - 路由：/portal/vip
 * - API：@/api/vip.js
 * - Vue组件：@/views/portal/Vip.vue
 */
@Data
@TableName("biz_vip_recharge")
public class VipRecharge implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 充值记录唯一标识

    private Long userId;         // 充值用户ID

    private Long memberId;       // 会员ID

    private String rechargeNo;    // 充值流水号

    private BigDecimal amount;    // 充值金额

    private BigDecimal giftAmount;  // 赠送金额

    private String paymentMethod;  // 支付方式

    private Integer status;       // 状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;       // 逻辑删除标记
}
