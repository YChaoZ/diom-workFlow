-- =====================================================
-- 流程模板和草稿数据库表设计
-- =====================================================

-- 流程模板表
CREATE TABLE IF NOT EXISTS `workflow_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
  `process_definition_key` VARCHAR(100) NOT NULL COMMENT '流程定义Key',
  `template_data` TEXT NOT NULL COMMENT '模板数据（JSON格式）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_process_key` (`process_definition_key`),
  KEY `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程模板表';

-- 流程草稿表
CREATE TABLE IF NOT EXISTS `workflow_draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `draft_name` VARCHAR(200) DEFAULT NULL COMMENT '草稿名称',
  `process_definition_key` VARCHAR(100) NOT NULL COMMENT '流程定义Key',
  `draft_data` TEXT NOT NULL COMMENT '草稿数据（JSON格式）',
  `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_process_key` (`process_definition_key`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程草稿表';

-- =====================================================
-- 初始化示例模板数据
-- =====================================================

-- 插入请假模板示例
INSERT INTO `workflow_template` 
  (`template_name`, `process_definition_key`, `template_data`, `description`, `is_public`, `creator_id`, `creator_name`)
VALUES 
(
  '年假申请模板', 
  'leave-approval-process',
  '{"leaveType":"annual","manager":"manager","days":5,"reason":"年度休假"}',
  '标准年假申请模板，默认5天',
  1,
  1,
  'admin'
),
(
  '事假申请模板', 
  'leave-approval-process',
  '{"leaveType":"personal","manager":"manager","days":1,"reason":"个人事务处理"}',
  '短期事假申请模板',
  1,
  1,
  'admin'
),
(
  '病假申请模板', 
  'leave-approval-process',
  '{"leaveType":"sick","manager":"manager","days":3,"reason":"身体不适需要休息"}',
  '病假申请模板，默认3天',
  1,
  1,
  'admin'
)
ON DUPLICATE KEY UPDATE 
  `template_data` = VALUES(`template_data`),
  `description` = VALUES(`description`);

-- =====================================================
-- 验证查询（可选）
-- =====================================================

-- 查询所有公开模板
-- SELECT 
--   id,
--   template_name,
--   process_definition_key,
--   description,
--   use_count,
--   creator_name,
--   create_time
-- FROM workflow_template
-- WHERE is_public = 1
-- ORDER BY use_count DESC, create_time DESC;

-- 查询某个用户的草稿
-- SELECT 
--   id,
--   draft_name,
--   process_definition_key,
--   creator_name,
--   create_time,
--   update_time
-- FROM workflow_draft
-- WHERE creator_id = 1
-- ORDER BY update_time DESC;

