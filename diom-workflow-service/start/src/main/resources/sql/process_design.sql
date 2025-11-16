-- ============================================
-- 流程设计器数据库表设计
-- ============================================

-- 流程设计表（存储BPMN设计数据）
CREATE TABLE IF NOT EXISTS `workflow_process_design` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `process_key` VARCHAR(100) NOT NULL COMMENT '流程定义Key（唯一标识，如leave-approval-process）',
    `process_name` VARCHAR(200) NOT NULL COMMENT '流程名称（中文名称，如请假审批流程）',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号（从1开始递增）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已废弃',
    `bpmn_xml` LONGTEXT NOT NULL COMMENT 'BPMN XML内容',
    `description` VARCHAR(500) COMMENT '流程描述',
    `category` VARCHAR(50) COMMENT '流程分类（如：人事、财务、行政）',
    
    -- Camunda部署信息
    `deployment_id` VARCHAR(100) COMMENT 'Camunda部署ID（发布后生成）',
    `process_definition_id` VARCHAR(100) COMMENT 'Camunda流程定义ID（发布后生成）',
    `deployed_at` DATETIME COMMENT '部署时间',
    
    -- 元数据
    `creator` VARCHAR(100) NOT NULL COMMENT '创建人',
    `creator_name` VARCHAR(100) COMMENT '创建人姓名',
    `publisher` VARCHAR(100) COMMENT '发布人',
    `publisher_name` VARCHAR(100) COMMENT '发布人姓名',
    `publish_time` DATETIME COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_key_version` (`process_key`, `version`),
    KEY `idx_process_key` (`process_key`),
    KEY `idx_status` (`status`),
    KEY `idx_creator` (`creator`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程设计表';

-- 流程设计变更历史表（记录每次修改）
CREATE TABLE IF NOT EXISTS `workflow_process_design_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `design_id` BIGINT NOT NULL COMMENT '流程设计ID',
    `process_key` VARCHAR(100) NOT NULL COMMENT '流程定义Key',
    `version` INT NOT NULL COMMENT '版本号',
    `action` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建, UPDATE-更新, PUBLISH-发布, DEPRECATE-废弃',
    `bpmn_xml` LONGTEXT NOT NULL COMMENT 'BPMN XML内容（快照）',
    `change_description` VARCHAR(500) COMMENT '变更说明',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
    `operator_name` VARCHAR(100) COMMENT '操作人姓名',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    -- 索引
    PRIMARY KEY (`id`),
    KEY `idx_design_id` (`design_id`),
    KEY `idx_process_key` (`process_key`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程设计变更历史表';

-- 初始化现有流程数据（将已有的leave-approval-process迁移到表中）
INSERT INTO `workflow_process_design` 
(`process_key`, `process_name`, `version`, `status`, `bpmn_xml`, `description`, `category`, `creator`, `creator_name`, `deployment_id`, `deployed_at`, `publish_time`)
VALUES 
('leave-approval-process', '请假审批流程', 1, 'PUBLISHED', 
'<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn" 
                  id="Definitions_1" 
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="leave-approval-process" name="请假审批流程" isExecutable="true">
    <!-- 完整的BPMN XML内容 -->
  </bpmn:process>
</bpmn:definitions>',
'员工请假审批流程，包含填写请假单和经理审批两个环节', '人事', 'admin', '管理员', 'system-init', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
`update_time` = NOW();

-- 初始化历史记录（只插入一次，避免重复）
INSERT IGNORE INTO `workflow_process_design_history` 
(`design_id`, `process_key`, `version`, `action`, `bpmn_xml`, `change_description`, `operator`, `operator_name`)
SELECT `id`, `process_key`, `version`, 'PUBLISH', `bpmn_xml`, '系统初始化导入', 'system', '系统'
FROM `workflow_process_design`
WHERE `process_key` = 'leave-approval-process' AND `version` = 1;

