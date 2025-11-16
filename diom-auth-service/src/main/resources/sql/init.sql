-- 创建数据库
CREATE DATABASE IF NOT EXISTS diom_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE diom_workflow;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` INT(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认用户
-- 密码: 123456 (BCrypt 加密后)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`)
VALUES 
('admin', '$2a$10$/DQ3w/xvtoycvr8.Gaama.QOU/nHdnKM6gjgeNSBMOOHjWwmg8TQG', '管理员', 'admin@diom.com', 1),
('user_1', '$2a$10$/DQ3w/xvtoycvr8.Gaama.QOU/nHdnKM6gjgeNSBMOOHjWwmg8TQG', '用户1', 'user1@diom.com', 1),
('user_2', '$2a$10$/DQ3w/xvtoycvr8.Gaama.QOU/nHdnKM6gjgeNSBMOOHjWwmg8TQG', '用户2', 'user2@diom.com', 1),
('test', '$2a$10$/DQ3w/xvtoycvr8.Gaama.QOU/nHdnKM6gjgeNSBMOOHjWwmg8TQG', '测试用户', 'test@diom.com', 1);

-- 说明：
-- 所有用户的密码都是: 123456
-- BCrypt 加密生成方式：
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String encodedPassword = encoder.encode("123456");

