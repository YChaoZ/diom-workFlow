-- ============================================
-- 添加审批流程所需用户
-- ============================================
-- 说明：
-- 1. 所有用户密码都是：123456
-- 2. BCrypt加密后的密码哈希值相同
-- ============================================

-- 添加部门经理用户
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `status`, `create_time`, `update_time`)
VALUES 
('manager', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
 '部门经理', 'manager@diom.com', '13800138001', 1, NOW(), NOW());

-- 添加HR专员用户
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `status`, `create_time`, `update_time`)
VALUES 
('hr', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
 'HR专员', 'hr@diom.com', '13800138002', 1, NOW(), NOW());

-- 验证插入
SELECT username, nickname, email, status FROM sys_user WHERE username IN ('manager', 'hr');

