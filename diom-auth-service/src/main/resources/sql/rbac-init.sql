-- =============================================
-- RBAC权限体系数据库初始化脚本
-- 创建时间: 2025-11-15
-- =============================================

-- 1. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码（唯一）',
  `role_name` VARCHAR(128) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0:禁用 1:启用）',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 2. 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID（0为顶级）',
  `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码（唯一）',
  `permission_name` VARCHAR(128) NOT NULL COMMENT '权限名称',
  `permission_type` VARCHAR(32) NOT NULL COMMENT '权限类型（MENU:菜单 BUTTON:按钮 API:接口）',
  `menu_path` VARCHAR(255) DEFAULT NULL COMMENT '菜单路径（前端路由）',
  `api_pattern` VARCHAR(255) DEFAULT NULL COMMENT 'API匹配模式（如：/workflow/**）',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0:禁用 1:启用）',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type_status` (`permission_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 3. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 4. 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `sort_order`, `create_by`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有系统所有权限', 1, 1, 'system'),
(2, 'MANAGER', '部门经理', '可以审批流程、查看报表', 1, 2, 'system'),
(3, 'EMPLOYEE', '普通员工', '可以发起流程、处理自己的任务', 1, 3, 'system'),
(4, 'HR', '人力资源', '可以管理用户、查看所有流程', 1, 4, 'system');

-- 插入默认权限（树形结构）
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `menu_path`, `icon`, `sort_order`) VALUES
-- 一级菜单
(1, 0, 'DASHBOARD', '首页', 'MENU', '/home', 'HomeFilled', 1),
(2, 0, 'WORKFLOW', '工作流管理', 'MENU', '/workflow', 'Operation', 2),
(3, 0, 'USER_CENTER', '用户中心', 'MENU', '/user', 'User', 3),
(4, 0, 'SYSTEM', '系统管理', 'MENU', '/system', 'Setting', 4),

-- 工作流管理子菜单
(11, 2, 'WORKFLOW_DEFINITION', '流程定义', 'MENU', '/workflow/list', 'List', 1),
(12, 2, 'WORKFLOW_START', '发起流程', 'MENU', '/workflow/start/:processKey', 'Document', 2),
(13, 2, 'WORKFLOW_TASK', '我的任务', 'MENU', '/workflow/tasks', 'CopyDocument', 3),
(14, 2, 'WORKFLOW_INSTANCE', '流程实例', 'MENU', '/workflow/instances', 'Files', 4),
(15, 2, 'WORKFLOW_TEMPLATE', '模板管理', 'MENU', '/workflow/templates', 'CopyDocument', 5),
(16, 2, 'WORKFLOW_NOTIFICATION', '消息通知', 'MENU', '/notifications', 'Bell', 6),

-- 工作流按钮权限
(21, 11, 'WORKFLOW_START_BTN', '发起流程按钮', 'BUTTON', NULL, NULL, 1),
(22, 13, 'WORKFLOW_TASK_COMPLETE', '完成任务按钮', 'BUTTON', NULL, NULL, 1),
(23, 14, 'WORKFLOW_INSTANCE_VIEW', '查看流程图按钮', 'BUTTON', NULL, NULL, 1),
(24, 15, 'WORKFLOW_TEMPLATE_CREATE', '创建模板按钮', 'BUTTON', NULL, NULL, 1),
(25, 15, 'WORKFLOW_TEMPLATE_DELETE', '删除模板按钮', 'BUTTON', NULL, NULL, 2),

-- 工作流API权限
(31, 2, 'WORKFLOW_API_START', '发起流程API', 'API', NULL, '/api/web/workflow/start', 1),
(32, 2, 'WORKFLOW_API_COMPLETE', '完成任务API', 'API', NULL, '/api/web/workflow/task/complete', 2),
(33, 2, 'WORKFLOW_API_QUERY', '查询流程API', 'API', NULL, '/api/web/workflow/**', 3),
(34, 2, 'WORKFLOW_API_TEMPLATE', '模板管理API', 'API', NULL, '/api/workflow/template/**', 4),

-- 系统管理子菜单
(41, 4, 'SYSTEM_ROLE', '角色管理', 'MENU', '/system/role', 'User', 1),
(42, 4, 'SYSTEM_PERMISSION', '权限管理', 'MENU', '/system/permission', 'Key', 2),
(43, 4, 'SYSTEM_USER', '用户管理', 'MENU', '/system/user', 'UserFilled', 3);

-- 为超级管理员分配所有权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 为部门经理分配工作流权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1), (2, 2), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16),
(2, 21), (2, 22), (2, 23), (2, 24),
(2, 31), (2, 32), (2, 33), (2, 34);

-- 为普通员工分配基础权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 1), (3, 2), (3, 12), (3, 13), (3, 16),
(3, 21), (3, 22), (3, 31);

-- 为HR分配用户管理权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(4, 1), (4, 2), (4, 11), (4, 13), (4, 14), (4, 16),
(4, 4), (4, 43);

-- 为现有用户分配角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `username`, `role_id`) VALUES
(1, 'admin', 1),      -- admin: 超级管理员
(2, 'manager', 2),    -- manager: 部门经理
(3, 'hr', 4),         -- hr: 人力资源
(4, 'user_1', 3),     -- user_1: 普通员工
(5, 'user_2', 3),     -- user_2: 普通员工
(6, 'test', 3);       -- test: 普通员工

