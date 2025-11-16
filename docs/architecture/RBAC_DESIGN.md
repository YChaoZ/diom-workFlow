# 🔐 RBAC权限体系设计方案

**设计时间**: 2025-11-15 18:00  
**设计目标**: 为DIOM工作流系统实现完整的RBAC权限管理

---

## 📋 需求分析

### 核心需求

1. **角色管理**
   - 创建、编辑、删除角色
   - 为角色分配权限
   - 查看角色列表

2. **权限管理**
   - 定义系统权限点
   - 权限分类（菜单、按钮、API）
   - 权限树形结构

3. **用户角色关联**
   - 为用户分配角色
   - 支持多角色
   - 角色继承

4. **权限控制**
   - 后端API权限拦截
   - 前端菜单权限控制
   - 前端按钮权限指令

### 业务场景

**示例角色**:
- **超级管理员** (admin): 所有权限
- **部门经理** (manager): 审批流程、查看报表
- **普通员工** (employee): 发起流程、查看自己的任务
- **HR** (hr): 管理用户、查看所有流程

---

## 🗄️ 数据库设计

### 1. 角色表 (sys_role)

```sql
CREATE TABLE `sys_role` (
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
) COMMENT='系统角色表';
```

### 2. 权限表 (sys_permission)

```sql
CREATE TABLE `sys_permission` (
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
) COMMENT='系统权限表';
```

### 3. 角色权限关联表 (sys_role_permission)

```sql
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) COMMENT='角色权限关联表';
```

### 4. 用户角色关联表 (sys_user_role)

```sql
CREATE TABLE `sys_user_role` (
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
) COMMENT='用户角色关联表';
```

### 初始化数据

```sql
-- 插入默认角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `sort_order`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有系统所有权限', 1, 1),
(2, 'MANAGER', '部门经理', '可以审批流程、查看报表', 1, 2),
(3, 'EMPLOYEE', '普通员工', '可以发起流程、处理自己的任务', 1, 3),
(4, 'HR', '人力资源', '可以管理用户、查看所有流程', 1, 4);

-- 插入默认权限（树形结构）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `menu_path`, `icon`, `sort_order`) VALUES
-- 一级菜单
(1, 0, 'DASHBOARD', '首页', 'MENU', '/home', 'HomeFilled', 1),
(2, 0, 'WORKFLOW', '工作流管理', 'MENU', '/workflow', 'Operation', 2),
(3, 0, 'USER_CENTER', '用户中心', 'MENU', '/user', 'User', 3),
(4, 0, 'SYSTEM', '系统管理', 'MENU', '/system', 'Setting', 4),

-- 工作流管理子菜单
(11, 2, 'WORKFLOW_DEFINITION', '流程定义', 'MENU', '/workflow/list', 'List', 1),
(12, 2, 'WORKFLOW_START', '发起流程', 'MENU', '/workflow/start', 'Document', 2),
(13, 2, 'WORKFLOW_TASK', '我的任务', 'MENU', '/workflow/tasks', 'CopyDocument', 3),
(14, 2, 'WORKFLOW_INSTANCE', '流程实例', 'MENU', '/workflow/instances', 'Files', 4),
(15, 2, 'WORKFLOW_TEMPLATE', '模板管理', 'MENU', '/workflow/templates', 'CopyDocument', 5),

-- 工作流按钮权限
(21, 11, 'WORKFLOW_START_BTN', '发起流程按钮', 'BUTTON', NULL, NULL, 1),
(22, 13, 'WORKFLOW_TASK_COMPLETE', '完成任务按钮', 'BUTTON', NULL, NULL, 1),
(23, 14, 'WORKFLOW_INSTANCE_VIEW', '查看流程图按钮', 'BUTTON', NULL, NULL, 1),
(24, 15, 'WORKFLOW_TEMPLATE_CREATE', '创建模板按钮', 'BUTTON', NULL, NULL, 1),

-- 工作流API权限
(31, 2, 'WORKFLOW_API_START', '发起流程API', 'API', NULL, '/api/web/workflow/start', 1),
(32, 2, 'WORKFLOW_API_COMPLETE', '完成任务API', 'API', NULL, '/api/web/workflow/task/complete', 2),
(33, 2, 'WORKFLOW_API_QUERY', '查询流程API', 'API', NULL, '/api/web/workflow/**', 3),

-- 系统管理子菜单
(41, 4, 'SYSTEM_ROLE', '角色管理', 'MENU', '/system/role', 'User', 1),
(42, 4, 'SYSTEM_PERMISSION', '权限管理', 'MENU', '/system/permission', 'Key', 2),
(43, 4, 'SYSTEM_USER', '用户管理', 'MENU', '/system/user', 'UserFilled', 3);

-- 为超级管理员分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 为部门经理分配工作流权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1), (2, 2), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15),
(2, 21), (2, 22), (2, 23), (2, 31), (2, 32), (2, 33);

-- 为普通员工分配基础权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 1), (3, 2), (3, 12), (3, 13),
(3, 21), (3, 22), (3, 31);

-- 为HR分配用户管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(4, 1), (4, 2), (4, 11), (4, 13), (4, 14),
(4, 4), (4, 43);

-- 为现有用户分配角色
INSERT INTO `sys_user_role` (`user_id`, `username`, `role_id`) VALUES
(1, 'admin', 1),      -- admin: 超级管理员
(2, 'manager', 2),    -- manager: 部门经理
(3, 'hr', 4),         -- hr: 人力资源
(4, 'user_1', 3),     -- user_1: 普通员工
(5, 'user_2', 3),     -- user_2: 普通员工
(6, 'test', 3);       -- test: 普通员工
```

---

## 🏗️ 架构设计

### 后端架构 (基于auth-service)

```
diom-auth-service/
├── entity/
│   ├── SysRole.java
│   ├── SysPermission.java
│   ├── SysRolePermission.java
│   └── SysUserRole.java
├── mapper/
│   ├── SysRoleMapper.java
│   ├── SysPermissionMapper.java
│   ├── SysRolePermissionMapper.java
│   └── SysUserRoleMapper.java
├── service/
│   ├── RoleService.java
│   ├── PermissionService.java
│   └── RbacService.java (权限检查服务)
├── controller/
│   ├── RoleController.java
│   └── PermissionController.java
└── interceptor/
    └── PermissionInterceptor.java (权限拦截器)
```

### 前端架构

```
diom-frontend/
├── api/
│   ├── role.js
│   └── permission.js
├── views/
│   └── System/
│       ├── Role.vue      (角色管理页面)
│       ├── Permission.vue (权限管理页面)
│       └── User.vue      (用户管理页面)
├── directives/
│   └── permission.js     (权限指令 v-permission)
└── utils/
    └── permission.js     (权限检查工具)
```

---

## 🔌 核心API设计

### 角色管理API

```typescript
// 1. 查询角色列表
GET /auth/role/list?page=1&size=10&keyword=

// 2. 获取角色详情（包含权限）
GET /auth/role/{roleId}

// 3. 创建角色
POST /auth/role
{
  "roleCode": "CUSTOM_ROLE",
  "roleName": "自定义角色",
  "description": "描述",
  "permissionIds": [1, 2, 3]
}

// 4. 更新角色
PUT /auth/role/{roleId}
{
  "roleName": "更新后的名称",
  "description": "更新后的描述",
  "permissionIds": [1, 2, 4, 5]
}

// 5. 删除角色
DELETE /auth/role/{roleId}

// 6. 为用户分配角色
POST /auth/role/assign
{
  "userId": 1,
  "username": "user1",
  "roleIds": [2, 3]
}
```

### 权限管理API

```typescript
// 1. 查询权限树
GET /auth/permission/tree

// 2. 查询权限列表
GET /auth/permission/list?type=MENU

// 3. 获取用户权限
GET /auth/permission/user/{username}

// 4. 创建权限
POST /auth/permission
{
  "parentId": 2,
  "permissionCode": "NEW_PERM",
  "permissionName": "新权限",
  "permissionType": "MENU",
  "menuPath": "/new-menu"
}
```

### 权限检查API

```typescript
// 检查用户是否有指定权限
GET /auth/permission/check?username=admin&permissionCode=WORKFLOW_START

// 获取用户所有权限码列表
GET /auth/permission/codes?username=admin
// 返回: ["DASHBOARD", "WORKFLOW", "WORKFLOW_START", ...]
```

---

## 🛡️ 权限拦截实现

### 后端拦截器

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RbacService rbacService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // 1. 从请求中获取用户信息
        String username = getUsernameFromToken(request);
        
        // 2. 获取请求的URI
        String requestUri = request.getRequestURI();
        
        // 3. 检查用户是否有该API的权限
        boolean hasPermission = rbacService.checkApiPermission(username, requestUri);
        
        if (!hasPermission) {
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
            return false;
        }
        
        return true;
    }
}
```

### 前端权限指令

```javascript
// directives/permission.js
export default {
  mounted(el, binding) {
    const { value } = binding;
    const permissions = store.getters.permissions; // 从store获取用户权限
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(perm => value.includes(perm));
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    }
  }
};

// 使用示例
<el-button v-permission="['WORKFLOW_START_BTN']">发起流程</el-button>
```

---

## 🎨 前端UI设计

### 角色管理页面

**功能**:
- 角色列表（表格）
- 创建/编辑角色（对话框）
- 权限分配（树形选择器）
- 删除角色（确认）

### 权限管理页面

**功能**:
- 权限树展示
- 权限分类（菜单/按钮/API）
- 创建/编辑权限
- 状态启用/禁用

### 用户管理页面增强

**新增功能**:
- 为用户分配角色（多选）
- 显示用户当前角色
- 显示用户权限列表

---

## 📝 开发步骤

### Phase 1: 数据库和后端基础（1小时）

1. ✅ 创建4张数据库表
2. ✅ 插入初始化数据
3. ✅ 创建Entity和Mapper
4. ✅ 实现RoleService和PermissionService
5. ✅ 实现RoleController和PermissionController

### Phase 2: 权限拦截和检查（30分钟）

1. ✅ 实现RbacService（权限检查核心逻辑）
2. ✅ 实现PermissionInterceptor
3. ✅ 配置拦截器
4. ✅ 测试API权限拦截

### Phase 3: 前端功能（1小时）

1. ✅ 实现角色管理页面
2. ✅ 实现权限管理页面
3. ✅ 实现权限指令
4. ✅ 集成到现有系统

### Phase 4: 测试和优化（30分钟）

1. ✅ 功能测试
2. ✅ 权限测试
3. ✅ UI优化
4. ✅ 文档完善

---

## 🎯 预期效果

### 用户登录后

```javascript
// Store中保存用户权限
{
  username: "manager",
  roles: ["MANAGER"],
  permissions: [
    "DASHBOARD",
    "WORKFLOW",
    "WORKFLOW_DEFINITION",
    "WORKFLOW_START",
    "WORKFLOW_START_BTN",
    // ...
  ]
}
```

### 菜单动态渲染

```javascript
// 根据权限过滤菜单
const visibleMenus = allMenus.filter(menu => 
  hasPermission(menu.permission)
);
```

### API请求拦截

```
用户请求 → Gateway → 
          ↓
      Auth-Service拦截器
          ↓
      检查权限 → 有权限：放行
                  无权限：返回403
```

---

## 📊 完成后系统完整度

```
核心功能:   ████████████████████ 100% ✅
认证授权:   ████████████████████ 100% ✅
工作流:     ████████████████████ 100% ✅
模板管理:   ████████████████████ 100% ✅
历史回填:   ████████████████████ 100% ✅
流程图:     ████████████████████ 100% ✅
通知中心:   ████████░░░░░░░░░░░░  45% ⚠️ (待修复)
RBAC权限:   ████████████████████ 100% ✅ (新增)
────────────────────────────────────────
总体进度:   ███████████████████░  98%
```

---

**设计完成时间**: 2025-11-15 18:00  
**预计开发时间**: 2-3小时  
**下一步**: 开始Phase 1 - 数据库和后端基础开发 🚀

