-- =============================================
-- 评分评论表
-- =============================================
CREATE TABLE IF NOT EXISTS `biz_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `check_in_id` BIGINT NOT NULL COMMENT '入住记录ID',
    `user_id` BIGINT NOT NULL COMMENT '评价用户ID',
    `room_id` BIGINT NOT NULL COMMENT '房间ID',
    `rating` INT NOT NULL COMMENT '评分 1-5',
    `content` VARCHAR(500) NOT NULL COMMENT '评价内容',
    `reply` VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `reply_user_id` BIGINT DEFAULT NULL COMMENT '回复人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX `idx_check_in_id` (`check_in_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分评论表';
