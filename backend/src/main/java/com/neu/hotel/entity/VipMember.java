package com.neu.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP会员实体类
 *
 * 【数据库表】biz_vip_member - VIP会员表
 *
 * 【主要功能】
 * - 管理酒店VIP会员信息
 * - 记录会员余额和充值记录
 * - 计算会员折扣和等级权益
 *
 * 【字段说明】
 * - id: 会员唯一标识（自增主键）
 * - userId: 关联用户ID（对应 sys_user 表）
 * - vipLevel: VIP等级（1=普通会员，2=银卡，3=金卡，4=钻卡等）
 * - balance: 当前余额
 * - totalRecharge: 累计充值金额
 * - discountRate: 享受的折扣率（如0.9表示9折）
 * - expireDate: 会员有效期
 * - status: 状态（1=正常，0=冻结）
 * - active: 是否有效（非数据库字段）
 *
 * 【关联关系】
 * - 通过 userId 关联 User 实体
 * - 通过 VipRecharge 实体记录充值记录（一对多）
 * - 通过 VipPackage 实体定义等级权益
 * - 通过 VipService 处理VIP业务逻辑
 * - 通过 VipController 提供VIP管理API
 *
 * 【对应前端】
 * - 路由：/admin/vip（后台管理）
 * - 路由：/portal/vip（前台展示）
 * - API：@/api/vip.js
 * - Vue组件：@/views/portal/Vip.vue
 */
@Data
@TableName("biz_vip_member")
public class VipMember implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;              // 会员唯一标识

    private Long userId;         // 关联用户ID

    private Integer vipLevel;     // VIP等级

    private BigDecimal balance;   // 当前余额

    private BigDecimal totalRecharge;  // 累计充值

    private BigDecimal discountRate;   // 折扣率

    private LocalDateTime expireDate;  // 有效期

    private Integer status;       // 状态

    private boolean active;       // 是否有效（非数据库字段）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间

    @TableLogic
    private Integer deleted;       // 逻辑删除标记

    /**
     * 计算会员是否有效
     * 条件：状态正常 + 未过期 + 等级大于0
     */
    public boolean computeActive() {
        if (status == null || status != 1) {
            this.active = false;
            return false;
        }
        if (expireDate != null && expireDate.isBefore(LocalDateTime.now())) {
            this.active = false;
            return false;
        }
        this.active = vipLevel != null && vipLevel > 0;
        return this.active;
    }
}
