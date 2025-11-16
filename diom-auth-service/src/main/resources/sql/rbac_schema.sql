-- =====================================================
-- RBAC 权限体系数据库表设计
-- =====================================================

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（唯一）',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表（资源表）
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码（唯一）',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型：menu-菜单，button-按钮，api-接口',
  `resource_path` VARCHAR(200) DEFAULT NULL COMMENT '资源路径',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID，0表示顶级',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =====================================================
-- 初始化基础角色数据
-- =====================================================

-- 插入系统角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `sort_order`)
VALUES 
('ROLE_ADMIN', '系统管理员', '拥有系统所有权限', 1, 1),
('ROLE_MANAGER', '部门经理', '部门管理权限，可审批员工请假', 1, 2),
('ROLE_HR', 'HR专员', '人力资源管理权限，可查看和管理员工信息', 1, 3),
('ROLE_USER', '普通用户', '基础用户权限，可发起流程和查看自己的任务', 1, 4)
ON DUPLICATE KEY UPDATE 
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`);

-- =====================================================
-- 初始化权限数据
-- =====================================================

-- 菜单权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource_type`, `resource_path`, `parent_id`, `icon`, `sort_order`)
VALUES 
-- 一级菜单
('menu:home', '首页', 'menu', '/home', 0, 'House', 1),
('menu:workflow', '工作流管理', 'menu', '/workflow', 0, 'Document', 2),
('menu:user', '用户中心', 'menu', '/user', 0, 'User', 3),
('menu:system', '系统管理', 'menu', '/system', 0, 'Setting', 4),

-- 工作流子菜单
('menu:workflow:definitions', '流程定义', 'menu', '/workflow/definitions', 0, 'DocumentCopy', 21),
('menu:workflow:tasks', '我的任务', 'menu', '/workflow/tasks', 0, 'List', 22),
('menu:workflow:instances', '流程实例', 'menu', '/workflow/instances', 0, 'Files', 23),

-- 用户中心子菜单
('menu:user:profile', '个人信息', 'menu', '/user/profile', 0, 'UserFilled', 31),
('menu:user:settings', '账号设置', 'menu', '/user/settings', 0, 'Tools', 32),

-- 系统管理子菜单
('menu:system:users', '用户管理', 'menu', '/system/users', 0, 'User', 41),
('menu:system:roles', '角色管理', 'menu', '/system/roles', 0, 'UserFilled', 42),
('menu:system:permissions', '权限管理', 'menu', '/system/permissions', 0, 'Lock', 43),

-- 按钮权限（工作流）
('button:workflow:start', '发起流程', 'button', NULL, 0, NULL, 101),
('button:workflow:complete', '完成任务', 'button', NULL, 0, NULL, 102),
('button:workflow:view', '查看流程图', 'button', NULL, 0, NULL, 103),
('button:workflow:history', '查看历史', 'button', NULL, 0, NULL, 104),

-- 按钮权限（用户管理）
('button:user:add', '新增用户', 'button', NULL, 0, NULL, 201),
('button:user:edit', '编辑用户', 'button', NULL, 0, NULL, 202),
('button:user:delete', '删除用户', 'button', NULL, 0, NULL, 203),
('button:user:reset', '重置密码', 'button', NULL, 0, NULL, 204),

-- 按钮权限（角色管理）
('button:role:add', '新增角色', 'button', NULL, 0, NULL, 301),
('button:role:edit', '编辑角色', 'button', NULL, 0, NULL, 302),
('button:role:delete', '删除角色', 'button', NULL, 0, NULL, 303),
('button:role:assign', '分配权限', 'button', NULL, 0, NULL, 304),

-- API权限
('api:auth:*', '认证服务所有接口', 'api', '/auth/**', 0, NULL, 1001),
('api:workflow:*', '工作流服务所有接口', 'api', '/workflow/**', 0, NULL, 1002),
('api:user:query', '查询用户接口', 'api', '/user/list', 0, NULL, 1003),
('api:user:add', '新增用户接口', 'api', '/user/add', 0, NULL, 1004),
('api:user:update', '更新用户接口', 'api', '/user/update', 0, NULL, 1005),
('api:user:delete', '删除用户接口', 'api', '/user/delete', 0, NULL, 1006)
ON DUPLICATE KEY UPDATE 
  `permission_name` = VALUES(`permission_name`),
  `resource_type` = VALUES(`resource_type`),
  `resource_path` = VALUES(`resource_path`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`);

-- =====================================================
-- 初始化角色权限关联
-- =====================================================

-- 系统管理员：所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 部门经理：工作流管理 + 查看权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_MANAGER'
  AND (p.permission_code LIKE 'menu:workflow%' 
    OR p.permission_code LIKE 'button:workflow%'
    OR p.permission_code = 'api:workflow:*'
    OR p.permission_code = 'menu:home'
    OR p.permission_code = 'menu:user:profile')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- HR专员：用户管理 + 工作流查看
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_HR'
  AND (p.permission_code LIKE 'menu:workflow%' 
    OR p.permission_code LIKE 'menu:system:users'
    OR p.permission_code LIKE 'button:user%'
    OR p.permission_code LIKE 'api:user%'
    OR p.permission_code = 'menu:home'
    OR p.permission_code = 'menu:user')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 普通用户：基础权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_USER'
  AND (p.permission_code = 'menu:home'
    OR p.permission_code = 'menu:workflow'
    OR p.permission_code = 'menu:workflow:tasks'
    OR p.permission_code = 'menu:workflow:instances'
    OR p.permission_code = 'menu:user:profile'
    OR p.permission_code = 'button:workflow:start'
    OR p.permission_code = 'button:workflow:complete'
    OR p.permission_code = 'button:workflow:view')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- =====================================================
-- 初始化用户角色关联
-- =====================================================

-- 为现有用户分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'manager' AND r.role_code = 'ROLE_MANAGER'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'hr' AND r.role_code = 'ROLE_HR'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username IN ('user_1', 'user_2', 'test') AND r.role_code = 'ROLE_USER'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- =====================================================
-- 验证查询（可选）
-- =====================================================

-- 查询所有角色及其权限数量
-- SELECT r.role_name, COUNT(rp.permission_id) as permission_count
-- FROM sys_role r
-- LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
-- GROUP BY r.id, r.role_name;

-- 查询某个用户的所有权限
-- SELECT DISTINCT p.permission_code, p.permission_name, p.resource_type
-- FROM sys_user u
-- JOIN sys_user_role ur ON u.id = ur.user_id
-- JOIN sys_role r ON ur.role_id = r.id
-- JOIN sys_role_permission rp ON r.id = rp.role_id
-- JOIN sys_permission p ON rp.permission_id = p.id
-- WHERE u.username = 'admin'
-- ORDER BY p.resource_type, p.sort_order;

