SET NAMES utf8mb4;

-- 1. Add user_type column
ALTER TABLE sys_user ADD COLUMN user_type INT DEFAULT 0 COMMENT '0=普通用户 1=管理员';

-- 2. Set existing users as admin
UPDATE sys_user SET user_type = 1;

-- 3. Add guest role
INSERT INTO sys_role (name, code, description, status) VALUES ('普通用户', 'guest', '普通访客/客人角色', 1);

-- 4. Add test customer accounts (password: 123456)
INSERT INTO sys_user (username, password, real_name, phone, email, role_id, status, user_type) VALUES
('guest1', '$2b$12$lMgo/qc6vZ8offctrEW7mu8bPL0y10V0OtMokJICDRhCdTA5tSzKe', '测试访客', '13900001001', NULL, 4, 1, 0),
('guest2', '$2b$12$lMgo/qc6vZ8offctrEW7mu8bPL0y10V0OtMokJICDRhCdTA5tSzKe', '张老师', '13900001002', NULL, 4, 1, 0);
