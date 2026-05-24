-- ===============================================
-- 校园访客接待与招待所管理系统 - 数据库建表脚本
-- 数据库: hotel_db (utf8mb4)
-- ===============================================

CREATE DATABASE IF NOT EXISTS hotel_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE hotel_db;

-- -----------------------------------------------
-- 1. 部门表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    code VARCHAR(50) COMMENT '部门编码',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    order_num INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- -----------------------------------------------
-- 2. 角色表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -----------------------------------------------
-- 3. 用户表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(Bcrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role_id BIGINT COMMENT '角色ID',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    avatar VARCHAR(500) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------
-- 4. 菜单权限表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(100) COMMENT '图标',
    perms VARCHAR(100) COMMENT '权限标识',
    menu_type INT DEFAULT 1 COMMENT '类型: 0目录 1菜单 2按钮',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- -----------------------------------------------
-- 5. 角色菜单关联表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- -----------------------------------------------
-- 6. 操作日志表
-- -----------------------------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    title VARCHAR(200) COMMENT '操作标题',
    business_type VARCHAR(50) COMMENT '业务类型',
    oper_type VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '方法名',
    request_method VARCHAR(20) COMMENT '请求方式',
    user_id BIGINT COMMENT '操作用户ID',
    oper_url VARCHAR(500) COMMENT '请求URL',
    oper_ip VARCHAR(50) COMMENT '操作IP',
    oper_param TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    status INT DEFAULT 1 COMMENT '状态: 0异常 1正常',
    error_msg TEXT COMMENT '错误信息',
    cost_time BIGINT COMMENT '消耗时间(ms)',
    oper_time DATETIME COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------
-- 7. 访客信息表
-- -----------------------------------------------
DROP TABLE IF EXISTS biz_visitor;
CREATE TABLE biz_visitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访客ID',
    name VARCHAR(50) NOT NULL COMMENT '访客姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    id_card VARCHAR(20) COMMENT '身份证号',
    company VARCHAR(100) COMMENT '来访单位',
    visit_purpose VARCHAR(200) COMMENT '来访目的',
    visit_date DATE COMMENT '来访日期',
    visit_time_slot VARCHAR(50) COMMENT '来访时段',
    host_dept_id BIGINT COMMENT '接待部门ID',
    host_person VARCHAR(50) COMMENT '接待人',
    host_person_phone VARCHAR(20) COMMENT '接待人电话',
    car_plate VARCHAR(20) COMMENT '车牌号',
    remark VARCHAR(500) COMMENT '备注',
    status INT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已拒绝 3已到访',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_remark VARCHAR(200) COMMENT '审核备注',
    review_time DATETIME COMMENT '审核时间',
    create_user_id BIGINT COMMENT '录入人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_phone (phone),
    INDEX idx_visit_date (visit_date),
    INDEX idx_status (status),
    INDEX idx_create_user (create_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客信息表';

-- -----------------------------------------------
-- 8. 招待所房型表
-- -----------------------------------------------
DROP TABLE IF EXISTS biz_dorm_room_type;
CREATE TABLE biz_dorm_room_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房型ID',
    name VARCHAR(50) NOT NULL COMMENT '房型名称',
    description VARCHAR(200) COMMENT '房型描述',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格(元/晚)',
    bed_count INT DEFAULT 2 COMMENT '床位数量',
    facility VARCHAR(500) COMMENT '设施配置(JSON)',
    status INT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招待所房型表';

-- -----------------------------------------------
-- 9. 招待所房间表
-- -----------------------------------------------
DROP TABLE IF EXISTS biz_dorm_room;
CREATE TABLE biz_dorm_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
    room_no VARCHAR(20) NOT NULL COMMENT '房间号',
    room_name VARCHAR(50) COMMENT '房间名称/标识',
    room_type_id BIGINT COMMENT '房型ID',
    floor VARCHAR(20) COMMENT '楼层',
    capacity INT DEFAULT 1 COMMENT '可住人数',
    max_capacity INT DEFAULT 2 COMMENT '最大容量',
    price DECIMAL(10,2) NOT NULL COMMENT '房价(元/晚)',
    status INT DEFAULT 1 COMMENT '状态: 1空闲 2入住 3维护 4停用',
    facility VARCHAR(500) COMMENT '房间设施',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_room_no (room_no),
    INDEX idx_room_type (room_type_id),
    INDEX idx_status (status),
    INDEX idx_floor (floor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招待所房间表';

-- -----------------------------------------------
-- 10. 入住记录表
-- -----------------------------------------------
DROP TABLE IF EXISTS biz_check_in;
CREATE TABLE biz_check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    visitor_id BIGINT NOT NULL COMMENT '访客ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_no VARCHAR(20) COMMENT '房间号',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '预计退房日期',
    check_in_status INT DEFAULT 0 COMMENT '入住状态: 0待审核 1入住中 2已退房 3已取消 4退房申请中',
    nights INT DEFAULT 1 COMMENT '住宿天数',
    room_fee DECIMAL(10,2) DEFAULT 0 COMMENT '房费',
    other_fee DECIMAL(10,2) DEFAULT 0 COMMENT '其他费用',
    total_fee DECIMAL(10,2) DEFAULT 0 COMMENT '费用合计',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已付金额',
    payment_status INT DEFAULT 0 COMMENT '支付状态: 0待支付 1已支付 2部分退款 3已退款',
    payment_method VARCHAR(50) COMMENT '支付方式',
    remark VARCHAR(500) COMMENT '备注',
    check_in_user_id BIGINT COMMENT '入住办理人',
    check_out_user_id BIGINT COMMENT '退房办理人',
    check_in_time DATETIME COMMENT '实际入住时间',
    check_out_time DATETIME COMMENT '实际退房时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_visitor (visitor_id),
    INDEX idx_room (room_id),
    INDEX idx_check_in_status (check_in_status),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入住记录表';

-- -----------------------------------------------
-- 11. 评分评论表
-- -----------------------------------------------
DROP TABLE IF EXISTS biz_review;
CREATE TABLE biz_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    check_in_id BIGINT NOT NULL COMMENT '入住记录ID',
    user_id BIGINT NOT NULL COMMENT '评价用户ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    rating INT NOT NULL COMMENT '评分 1-5',
    content VARCHAR(500) NOT NULL COMMENT '评价内容',
    reply VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
    reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
    reply_user_id BIGINT DEFAULT NULL COMMENT '回复人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分评论表';

-- -----------------------------------------------
-- 初始化数据
-- -----------------------------------------------

-- 插入角色
INSERT INTO sys_role (name, code, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1),
('校园接待员', 'RECEPTIONIST', '负责访客接待和来访审核', 1),
('招待所宿管', 'DORM_MANAGER', '负责招待所房间和入住管理', 1);

-- 插入部门
INSERT INTO sys_dept (name, parent_id, code, leader, phone, order_num, status) VALUES
('学校办公室', 0, 'OFFICE', '张主任', '0411-12345601', 1, 1),
('保卫处', 0, 'SECURITY', '李处长', '0411-12345602', 2, 1),
('后勤集团', 0, 'LOGISTICS', '王经理', '0411-12345603', 3, 1),
('招待所', 3, 'GUESTHOUSE', '赵主管', '0411-12345604', 1, 1),
('计算机学院', 0, 'CS', '刘院长', '0411-12345605', 4, 1),
('外国语学院', 0, 'FOREIGN', '孙院长', '0411-12345606', 5, 1);

-- 插入用户 (密码均为 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, role_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800000001', 'admin@neu.edu.cn', 1, 1),
('receptionist1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '接待员甲', '13800000002', 'reception@neu.edu.cn', 2, 1),
('receptionist2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '接待员乙', '13800000003', 'reception2@neu.edu.cn', 2, 1),
('dorm_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '宿管张', '13800000004', 'dorm@neu.edu.cn', 3, 1),
('dorm_keeper', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '宿管李', '13800000005', 'dorm2@neu.edu.cn', 3, 1);

-- 插入菜单 (超级管理员的完整菜单)
INSERT INTO sys_menu (name, path, component, parent_id, order_num, icon, perms, menu_type, status) VALUES
-- 一级菜单
('系统管理', '/system', NULL, 0, 1, 'Setting', NULL, 0, 1),
('访客管理', '/visitor', NULL, 0, 2, 'User', NULL, 0, 1),
('招待所管理', '/dorm', NULL, 0, 3, 'House', NULL, 0, 1),
('首页', '/dashboard', 'layout/MainLayout', 0, 0, 'HomeFilled', NULL, 1, 1),
-- 系统管理子菜单
('用户管理', '/system/user', 'system/user/index', 1, 1, 'UserFilled', 'system:user:list', 1, 1),
('角色管理', '/system/role', 'system/role/index', 1, 2, 'User', 'system:role:list', 1, 1),
('菜单管理', '/system/menu', 'system/menu/index', 1, 3, 'Menu', 'system:menu:list', 1, 1),
('部门管理', '/system/dept', 'system/dept/index', 1, 4, 'Office', 'system:dept:list', 1, 1),
('操作日志', '/system/log', 'system/log/index', 1, 5, 'Document', 'system:log:list', 1, 1),
-- 访客管理子菜单
('访客信息', '/visitor/list', 'visitor/list/index', 2, 1, 'List', 'visitor:list', 1, 1),
('来访预约', '/visitor/reservation', 'visitor/reservation/index', 2, 2, 'Calendar', 'visitor:reservation', 1, 1),
('预约审核', '/visitor/review', 'visitor/review/index', 2, 3, 'Check', 'visitor:review', 1, 1),
-- 招待所管理子菜单
('房型管理', '/dorm/room-type', 'dorm/room-type/index', 3, 1, 'Bed', 'dorm:room-type', 1, 1),
('房间管理', '/dorm/room', 'dorm/room/index', 3, 2, 'House', 'dorm:room', 1, 1),
('入住登记', '/dorm/checkin', 'dorm/checkin/index', 3, 3, 'Plus', 'dorm:checkin', 1, 1),
('住宿记录', '/dorm/record', 'dorm/record/index', 3, 4, 'Tickets', 'dorm:record', 1, 1);
-- 角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (2, 1), (3, 1);

-- 为角色分配菜单 (超级管理员: 所有菜单)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17);

-- 校园接待员菜单 (访客管理)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 9), (2, 10), (2, 11), (2, 12);

-- 招待所宿管菜单 (招待所管理)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, 13), (3, 14), (3, 15), (3, 16), (3, 17);

-- 插入房型
INSERT INTO biz_dorm_room_type (name, description, base_price, bed_count, facility, status) VALUES
('单人间', '独立卫浴、空调、书桌、衣柜', 128.00, 1, '["空调","独立卫浴","电视","宽带","书桌"]', 1),
('标准间', '两张单人床，独立卫浴', 168.00, 2, '["空调","独立卫浴","电视","宽带","沙发"]', 1),
('商务间', '大床房，配电脑', 198.00, 2, '["空调","独立卫浴","电视","宽带","电脑","冰箱"]', 1),
('套房', '一室一厅，适合家庭', 288.00, 3, '["空调","独立卫浴","电视","宽带","客厅","冰箱","洗衣机"]', 1);

-- 插入房间
INSERT INTO biz_dorm_room (room_no, room_name, room_type_id, floor, capacity, max_capacity, price, status, facility) VALUES
('101', '101客房', 1, '1楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌'),
('102', '102客房', 1, '1楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌'),
('103', '103客房', 2, '1楼', 2, 2, 168.00, 1, '空调|电视|宽带'),
('104', '104客房', 2, '1楼', 2, 2, 168.00, 2, '空调|电视|宽带'),
('105', '105客房', 3, '1楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑'),
('201', '201客房', 1, '2楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌'),
('202', '202客房', 2, '2楼', 2, 2, 168.00, 1, '空调|电视|宽带'),
('203', '203客房', 3, '2楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑'),
('204', '204客房', 4, '2楼', 3, 4, 288.00, 1, '空调|电视|宽带|客厅|冰箱'),
('301', '301客房', 3, '3楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑'),
('302', '302客房', 4, '3楼', 3, 4, 288.00, 1, '空调|电视|宽带|客厅|冰箱|洗衣机'),
('303', '303客房', 1, '3楼', 1, 1, 128.00, 3, '空调|电视|宽带|书桌');

-- 插入测试访客
INSERT INTO biz_visitor (name, phone, id_card, company, visit_purpose, visit_date, visit_time_slot, host_person, host_person_phone, status) VALUES
('王教授', '13900000001', '210102197001011234', '清华大学', '学术交流', '2026-05-20', '上午', '刘院长', '13800000005', 1),
('李博士', '13900000002', '310102198503022345', '中科院计算所', '科研合作', '2026-05-21', '下午', '张教授', '13800000006', 0),
('张先生', '13900000003', '440102198804033456', '企业来访', '校企合作洽谈', '2026-05-22', '全天', '王主任', '13800000007', 1),
('赵老师', '13900000004', '510102197512124567', '兄弟院校', '教学研讨', '2026-05-23', '上午', '刘院长', '13800000005', 0),
('陈学生', '13900000005', '610102200005015678', '高中来访', '校园参观', '2026-05-24', '下午', '招办', '13800000008', 1);
