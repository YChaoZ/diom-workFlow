-- ============================================
-- 流程设计器权限初始化脚本
-- 执行前请确保已连接到 diom_workflow 数据库
-- ============================================

USE diom_workflow;

-- 1. 添加流程设计器权限到sys_permission表
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `sort_order`, `status`) 
VALUES 
-- 流程设计器父级权限
('workflow:design', '流程设计器', 'MENU', NULL, 40, 1),

-- 流程设计器子权限
('workflow:design:view', '查看流程设计', 'BUTTON', 
    (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'workflow:design') AS temp), 
    1, 1),
    
('workflow:design:create', '创建流程', 'BUTTON', 
    (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'workflow:design') AS temp), 
    2, 1),
    
('workflow:design:edit', '编辑流程', 'BUTTON', 
    (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'workflow:design') AS temp), 
    3, 1),
    
('workflow:design:delete', '删除流程', 'BUTTON', 
    (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'workflow:design') AS temp), 
    4, 1),
    
('workflow:design:publish', '发布流程', 'BUTTON', 
    (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'workflow:design') AS temp), 
    5, 1)
ON DUPLICATE KEY UPDATE 
`permission_name` = VALUES(`permission_name`),
`update_time` = CURRENT_TIMESTAMP;

-- 2. 分配流程设计器权限给ADMIN角色
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    r.id AS role_id,
    p.id AS permission_id
FROM 
    `sys_role` r,
    `sys_permission` p
WHERE 
    r.role_code = 'SUPER_ADMIN'
    AND p.permission_code IN (
        'workflow:design',
        'workflow:design:view',
        'workflow:design:create',
        'workflow:design:edit',
        'workflow:design:delete',
        'workflow:design:publish'
    )
ON DUPLICATE KEY UPDATE 
`role_id` = VALUES(`role_id`);

-- 3. 验证权限添加成功
SELECT 
    p.permission_code,
    p.permission_name,
    p.permission_type,
    p.status,
    p.create_time
FROM 
    `sys_permission` p
WHERE 
    p.permission_code LIKE 'workflow:design%'
ORDER BY 
    p.sort_order;

-- 4. 验证角色权限关联成功
SELECT 
    r.role_code,
    r.role_name,
    p.permission_code,
    p.permission_name
FROM 
    `sys_role` r
    INNER JOIN `sys_role_permission` rp ON r.id = rp.role_id
    INNER JOIN `sys_permission` p ON rp.permission_id = p.id
WHERE 
    p.permission_code LIKE 'workflow:design%'
ORDER BY 
    r.role_code, p.sort_order;

-- ============================================
-- 权限初始化完成！
-- ============================================

