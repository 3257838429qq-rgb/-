/* ============================================================
   东北大学校招待所系统 - VIP会员模块SQL脚本
   ============================================================

   【模块说明】
   - VIP会员系统的数据库表结构定义
   - 包含会员等级、充值记录、充值套餐配置

   【包含表结构】(共3张表)
   1. biz_vip_member   - VIP会员表（每个用户一条记录）
   2. biz_vip_recharge - VIP充值记录表（充值流水）
   3. biz_vip_package - VIP充值套餐表（等级配置）

   【VIP等级体系】
   - 等级0: 普通用户（无折扣）
   - 等级1: 铜牌会员（最低500元，95折，额外赠送5%）
   - 等级2: 银牌会员（最低1000元，9折，额外赠送10%）
   - 等级3: 金牌会员（最低3000元，85折，额外赠送15%）
   - 等级4: 钻石会员（最低5000元，8折，额外赠送20%）

   【业务逻辑】
   - 充值时根据金额自动判断VIP等级
   - 充值金额达到更高等级时自动升级
   - 余额可用于预订房间支付
   - 折扣率应用于房价计算

   【前端对应】
   - VIP页面: frontend/src/views/portal/Vip.vue
   - VIP API:  frontend/src/api/vip.js
   - VIP Service: backend/src/main/java/com/neu/hotel/service/VipService.java

   【后端对应】
   - Controller: VipController
   - 路径: /vip/member, /vip/packages, /vip/recharge, /vip/history

   ============================================================ */

SET NAMES utf8mb4;

-- VIP会员等级表
DROP TABLE IF EXISTS biz_vip_member;
CREATE TABLE biz_vip_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会员ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    vip_level INT DEFAULT 0 COMMENT 'VIP等级: 0普通用户 1铜牌会员 2银牌会员 3金牌会员 4钻石会员',
    balance DECIMAL(10,2) DEFAULT 0 COMMENT '账户余额',
    total_recharge DECIMAL(10,2) DEFAULT 0 COMMENT '累计充值金额',
    discount_rate DECIMAL(5,2) DEFAULT 1.00 COMMENT '折扣率(0-1)',
    expire_date DATETIME COMMENT '会员到期时间',
    status INT DEFAULT 1 COMMENT '状态: 0冻结 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '成为会员时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_user_id (user_id),
    INDEX idx_vip_level (vip_level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP会员表';

-- VIP充值记录表
DROP TABLE IF EXISTS biz_vip_recharge;
CREATE TABLE biz_vip_recharge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '充值记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    member_id BIGINT COMMENT '会员ID',
    recharge_no VARCHAR(50) NOT NULL COMMENT '充值单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    gift_amount DECIMAL(10,2) DEFAULT 0 COMMENT '赠送金额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    status INT DEFAULT 1 COMMENT '状态: 0失败 1成功 2处理中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_recharge_no (recharge_no),
    INDEX idx_user_id (user_id),
    INDEX idx_member_id (member_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP充值记录表';

-- VIP等级配置（可通过此表配置不同等级门槛）
DROP TABLE IF EXISTS biz_vip_package;
CREATE TABLE biz_vip_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '套餐ID',
    name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    level INT NOT NULL COMMENT '对应VIP等级',
    min_amount DECIMAL(10,2) NOT NULL COMMENT '最低充值金额',
    discount_rate DECIMAL(5,2) NOT NULL COMMENT '享受折扣率',
    description VARCHAR(200) COMMENT '套餐描述',
    gift_rate DECIMAL(5,2) DEFAULT 0 COMMENT '赠送比例',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP充值套餐表';

-- 初始化VIP套餐
INSERT INTO biz_vip_package (name, level, min_amount, discount_rate, description, gift_rate, status) VALUES
('铜牌会员', 1, 500.00, 0.95, '充值满500元，享95折优惠', 0.05, 1),
('银牌会员', 2, 1000.00, 0.90, '充值满1000元，享9折优惠', 0.10, 1),
('金牌会员', 3, 3000.00, 0.85, '充值满3000元，享85折优惠', 0.15, 1),
('钻石会员', 4, 5000.00, 0.80, '充值满5000元，享8折优惠', 0.20, 1);
