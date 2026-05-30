-- 补充房间数据（12→30间）
-- 1楼 106-110 (5间)
INSERT INTO biz_dorm_room (room_no, room_name, room_type_id, floor, capacity, max_capacity, price, status, facility, remark, create_time, update_time, deleted) VALUES
('106', '106客房', 1, '1楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌', NULL, NOW(), NOW(), 0),
('107', '107客房', 2, '1楼', 2, 2, 168.00, 1, '空调|电视|宽带', NULL, NOW(), NOW(), 0),
('108', '108客房', 2, '1楼', 2, 2, 168.00, 1, '空调|电视|宽带', NULL, NOW(), NOW(), 0),
('109', '109客房', 3, '1楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑', NULL, NOW(), NOW(), 0),
('110', '110客房', 4, '1楼', 3, 4, 288.00, 3, '空调|电视|宽带|客厅|冰箱', '定期维护中', NOW(), NOW(), 0);

-- 2楼 205-210 (6间)
INSERT INTO biz_dorm_room (room_no, room_name, room_type_id, floor, capacity, max_capacity, price, status, facility, remark, create_time, update_time, deleted) VALUES
('205', '205客房', 1, '2楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌', NULL, NOW(), NOW(), 0),
('206', '206客房', 1, '2楼', 1, 1, 128.00, 3, '空调|电视|宽带|书桌', '卫生间维修', NOW(), NOW(), 0),
('207', '207客房', 2, '2楼', 2, 2, 168.00, 1, '空调|电视|宽带', NULL, NOW(), NOW(), 0),
('208', '208客房', 3, '2楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑', NULL, NOW(), NOW(), 0),
('209', '209客房', 3, '2楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑', NULL, NOW(), NOW(), 0),
('210', '210客房', 4, '2楼', 3, 4, 288.00, 1, '空调|电视|宽带|客厅|冰箱|洗衣机', NULL, NOW(), NOW(), 0);

-- 3楼 304-310 (7间)
INSERT INTO biz_dorm_room (room_no, room_name, room_type_id, floor, capacity, max_capacity, price, status, facility, remark, create_time, update_time, deleted) VALUES
('304', '304客房', 1, '3楼', 1, 1, 128.00, 1, '空调|电视|宽带|书桌', NULL, NOW(), NOW(), 0),
('305', '305客房', 2, '3楼', 2, 2, 168.00, 1, '空调|电视|宽带', NULL, NOW(), NOW(), 0),
('306', '306客房', 2, '3楼', 2, 2, 168.00, 1, '空调|电视|宽带', NULL, NOW(), NOW(), 0),
('307', '307客房', 3, '3楼', 2, 2, 198.00, 1, '空调|电视|宽带|电脑', NULL, NOW(), NOW(), 0),
('308', '308客房', 4, '3楼', 3, 4, 288.00, 1, '空调|电视|宽带|客厅|冰箱', NULL, NOW(), NOW(), 0),
('309', '309客房', 4, '3楼', 3, 4, 288.00, 2, '空调|电视|宽带|客厅|冰箱|洗衣机', NULL, NOW(), NOW(), 0),
('310', '310客房', 1, '3楼', 1, 1, 128.00, 4, '空调|电视|宽带|书桌', '长期停用', NOW(), NOW(), 0);
