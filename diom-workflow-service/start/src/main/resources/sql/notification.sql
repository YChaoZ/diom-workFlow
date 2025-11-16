-- =====================================================
-- 消息通知数据库表设计
-- =====================================================

CREATE TABLE IF NOT EXISTS `workflow_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `username` VARCHAR(100) NOT NULL COMMENT '接收用户名',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型: TASK_ASSIGNED, TASK_COMPLETED, PROCESS_STARTED, PROCESS_COMPLETED',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `link_type` VARCHAR(50) COMMENT '链接类型: TASK, PROCESS, NONE',
  `link_id` VARCHAR(100) COMMENT '链接ID（任务ID或流程实例ID）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `priority` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: LOW, NORMAL, HIGH, URGENT',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =====================================================
-- 初始化示例通知数据
-- =====================================================

-- 为admin用户创建欢迎通知
INSERT INTO `workflow_notification` 
  (`user_id`, `username`, `type`, `title`, `content`, `link_type`, `priority`)
VALUES 
  (1, 'admin', 'SYSTEM', '欢迎使用DIOM工作流系统', '您好！欢迎使用DIOM工作流系统。系统已为您准备好模板和快捷功能，开始您的高效工作吧！', 'NONE', 'NORMAL');

-- =====================================================
-- 通知类型说明
-- =====================================================

-- TASK_ASSIGNED: 任务分配通知
--   当新任务分配给用户时触发
--   标题: "您有新的待办任务"
--   内容: "流程"{流程名称}"中有新任务待您处理"
--   链接: 任务详情页面

-- TASK_COMPLETED: 任务完成通知
--   当用户提交的任务被处理时触发
--   标题: "您的任务已被处理"
--   内容: "您提交的"{流程名称}"已被{处理人}处理"
--   链接: 流程实例页面

-- PROCESS_STARTED: 流程启动通知
--   当用户发起流程成功时触发
--   标题: "流程启动成功"
--   内容: "您发起的"{流程名称}"已成功启动"
--   链接: 流程实例页面

-- PROCESS_COMPLETED: 流程完成通知
--   当用户发起的流程完成时触发
--   标题: "流程已完成"
--   内容: "您发起的"{流程名称}"已完成处理"
--   链接: 流程实例页面

-- SYSTEM: 系统通知
--   系统公告或重要提醒
--   标题: 自定义
--   内容: 自定义
--   链接: 无或自定义

