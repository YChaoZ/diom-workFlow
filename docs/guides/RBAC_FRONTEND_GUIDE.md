# 🚀 RBAC前端开发指南 - 新会话启动手册

**创建时间**: 2025-11-15 18:10  
**用途**: 在新会话中专门开发RBAC前端UI  
**前置条件**: RBAC后端已100%完成并测试通过

---

## 📋 任务清单

### Phase 1: 基础功能（必需）⭐⭐⭐ - 预计1小时

#### 1.1 权限指令实现 (15分钟)
**目标**: 创建 `v-permission` 指令控制按钮显示

**文件**: `diom-frontend/src/directives/permission.js`

```javascript
import { useUserStore } from '@/stores/user'

export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(perm => value.includes(perm))
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error('需要指定权限码数组！如 v-permission="[\'WORKFLOW_START\']"')
    }
  }
}
```

**注册指令**: `diom-frontend/src/main.js`

```javascript
import permission from './directives/permission'

app.directive('permission', permission)
```

#### 1.2 更新用户Store (20分钟)
**目标**: 在登录时获取并存储用户权限

**文件**: `diom-frontend/src/stores/user.js`

**需要添加的字段**:
```javascript
state: () => ({
  // ... 现有字段
  roles: [],          // 用户角色列表
  permissions: [],    // 用户权限编码列表
})
```

**需要更新的方法**:
```javascript
async getUserInfo() {
  const res = await getUserInfo()
  if (res.code === 200) {
    this.userInfo = res.data
    this.roles = res.data.roles || []
    this.permissions = res.data.permissions || []
  }
}
```

#### 1.3 动态菜单渲染 (25分钟)
**目标**: 根据用户权限过滤侧边栏菜单

**文件**: `diom-frontend/src/components/Layout/index.vue`

**核心逻辑**:
```javascript
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const permissions = computed(() => userStore.permissions)

// 权限检查函数
const hasPermission = (permissionCode) => {
  return permissions.value.includes(permissionCode)
}

// 过滤菜单
const visibleMenus = computed(() => {
  const allMenus = [
    { 
      name: '首页', 
      path: '/home', 
      icon: 'HomeFilled',
      permission: 'DASHBOARD'
    },
    {
      name: '工作流管理',
      icon: 'Operation',
      permission: 'WORKFLOW',
      children: [
        { name: '流程定义', path: '/workflow/list', permission: 'WORKFLOW_DEFINITION' },
        { name: '发起流程', path: '/workflow/start/:processKey', permission: 'WORKFLOW_START' },
        { name: '我的任务', path: '/workflow/tasks', permission: 'WORKFLOW_TASK' },
        { name: '流程实例', path: '/workflow/instances', permission: 'WORKFLOW_INSTANCE' },
        { name: '模板管理', path: '/workflow/templates', permission: 'WORKFLOW_TEMPLATE' },
      ]
    },
    // ... 其他菜单
  ]
  
  return allMenus.filter(menu => {
    if (menu.permission && !hasPermission(menu.permission)) {
      return false
    }
    if (menu.children) {
      menu.children = menu.children.filter(child => 
        !child.permission || hasPermission(child.permission)
      )
    }
    return true
  })
})
```

---

### Phase 2: 角色管理页面（推荐）⭐⭐⭐ - 预计1小时

#### 2.1 创建API服务
**文件**: `diom-frontend/src/api/role.js`

```javascript
import request from '@/utils/request'

// 查询角色列表
export function getRoleList(params) {
  return request({
    url: '/auth/role/list',
    method: 'get',
    params
  })
}

// 获取角色详情
export function getRoleById(roleId) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/auth/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(roleId, data) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(roleId) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'delete'
  })
}

// 为用户分配角色
export function assignRolesToUser(data) {
  return request({
    url: '/auth/role/assign',
    method: 'post',
    data
  })
}

// 获取用户的角色列表
export function getUserRoles(username) {
  return request({
    url: `/auth/role/user/${username}`,
    method: 'get'
  })
}
```

#### 2.2 创建角色管理页面
**文件**: `diom-frontend/src/views/System/Role.vue`

**主要功能**:
1. 角色列表（表格 + 分页）
2. 创建/编辑角色（对话框）
3. 删除角色（确认）
4. 权限分配（树形选择器）

**UI结构**:
```vue
<template>
  <div class="role-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate" v-permission="['SYSTEM_ROLE']">
        <el-icon><Plus /></el-icon>
        创建角色
      </el-button>
    </div>
    
    <el-table :data="roleList" style="width: 100%">
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link @click="handleEdit(row)">编辑</el-button>
          <el-button link @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="loadRoles"
    />
    
    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限分配对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限">
      <el-tree
        ref="permissionTree"
        :data="permissionTree"
        :props="{ children: 'children', label: 'permissionName' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedPermissions"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

---

### Phase 3: 权限管理页面（可选）⭐⭐ - 预计1小时

#### 3.1 创建API服务
**文件**: `diom-frontend/src/api/permission.js`

```javascript
import request from '@/utils/request'

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/auth/permission/tree',
    method: 'get'
  })
}

// 获取权限列表
export function getPermissionList(params) {
  return request({
    url: '/auth/permission/list',
    method: 'get',
    params
  })
}

// 获取用户权限
export function getUserPermissions(username) {
  return request({
    url: `/auth/permission/user/${username}`,
    method: 'get'
  })
}

// 获取用户权限编码列表
export function getUserPermissionCodes(username) {
  return request({
    url: '/auth/permission/codes',
    method: 'get',
    params: { username }
  })
}

// 检查用户权限
export function checkPermission(username, permissionCode) {
  return request({
    url: '/auth/permission/check',
    method: 'get',
    params: { username, permissionCode }
  })
}

// 创建权限
export function createPermission(data) {
  return request({
    url: '/auth/permission',
    method: 'post',
    data
  })
}

// 更新权限
export function updatePermission(permissionId, data) {
  return request({
    url: `/auth/permission/${permissionId}`,
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(permissionId) {
  return request({
    url: `/auth/permission/${permissionId}`,
    method: 'delete'
  })
}
```

#### 3.2 创建权限管理页面
**文件**: `diom-frontend/src/views/System/Permission.vue`

**主要功能**:
1. 树形展示所有权限
2. 创建/编辑/删除权限
3. 按类型筛选（菜单/按钮/API）
4. 权限启用/禁用

---

### Phase 4: 用户管理增强（可选）⭐⭐ - 预计30分钟

**文件**: `diom-frontend/src/views/System/User.vue` (如果存在)

**需要添加的功能**:
1. 用户列表增加"角色"列
2. 用户编辑/创建时添加角色选择器（多选）
3. 显示用户权限列表（只读）

---

### Phase 5: 路由配置 - 预计15分钟

**文件**: `diom-frontend/src/router/index.js`

**添加路由**:
```javascript
{
  path: '/system',
  name: 'System',
  component: Layout,
  meta: { title: '系统管理', icon: 'Setting' },
  children: [
    {
      path: 'role',
      name: 'SystemRole',
      component: () => import('@/views/System/Role.vue'),
      meta: { title: '角色管理', icon: 'User' }
    },
    {
      path: 'permission',
      name: 'SystemPermission',
      component: () => import('@/views/System/Permission.vue'),
      meta: { title: '权限管理', icon: 'Key' }
    },
    {
      path: 'user',
      name: 'SystemUser',
      component: () => import('@/views/System/User.vue'),
      meta: { title: '用户管理', icon: 'UserFilled' }
    }
  ]
}
```

---

### Phase 6: 测试和优化 - 预计30分钟

#### 6.1 功能测试
- [ ] 权限指令测试（v-permission）
- [ ] 动态菜单测试（不同角色看到不同菜单）
- [ ] 角色CRUD测试
- [ ] 权限分配测试
- [ ] 用户角色分配测试

#### 6.2 用户体验优化
- [ ] 加载状态
- [ ] 错误提示
- [ ] 成功提示
- [ ] 表单验证

#### 6.3 样式优化
- [ ] 表格样式
- [ ] 对话框样式
- [ ] 按钮样式
- [ ] 树形选择器样式

---

## 🔧 后端API参考

### 角色管理API

```bash
# 查询角色列表
GET http://localhost:8083/api/auth/role/list?page=1&size=10

# 获取角色详情
GET http://localhost:8083/api/auth/role/{roleId}

# 创建角色
POST http://localhost:8083/api/auth/role
{
  "roleCode": "DEVELOPER",
  "roleName": "开发人员",
  "description": "开发人员角色",
  "status": 1,
  "sortOrder": 5,
  "permissionIds": [1, 2, 11, 13]
}

# 更新角色
PUT http://localhost:8083/api/auth/role/{roleId}
{
  "roleName": "更新后的名称",
  "permissionIds": [1, 2, 3, 4]
}

# 删除角色
DELETE http://localhost:8083/api/auth/role/{roleId}

# 为用户分配角色
POST http://localhost:8083/api/auth/role/assign
{
  "userId": 4,
  "username": "user_1",
  "roleIds": [2, 3]
}

# 获取用户的角色列表
GET http://localhost:8083/api/auth/role/user/{username}
```

### 权限管理API

```bash
# 获取权限树
GET http://localhost:8083/api/auth/permission/tree

# 获取权限列表（可按类型过滤）
GET http://localhost:8083/api/auth/permission/list?type=MENU

# 获取用户权限
GET http://localhost:8083/api/auth/permission/user/{username}

# 获取用户权限编码列表
GET http://localhost:8083/api/auth/permission/codes?username=admin

# 检查用户权限
GET http://localhost:8083/api/auth/permission/check?username=admin&permissionCode=WORKFLOW_START

# 创建权限
POST http://localhost:8083/api/auth/permission
{
  "parentId": 2,
  "permissionCode": "NEW_PERMISSION",
  "permissionName": "新权限",
  "permissionType": "MENU",
  "menuPath": "/new-path",
  "icon": "Document",
  "status": 1
}

# 更新权限
PUT http://localhost:8083/api/auth/permission/{permissionId}

# 删除权限
DELETE http://localhost:8083/api/auth/permission/{permissionId}
```

---

## 📊 测试数据

### 预置角色（4个）
1. **SUPER_ADMIN** (超级管理员) - 所有权限
2. **MANAGER** (部门经理) - 工作流相关权限
3. **EMPLOYEE** (普通员工) - 基础权限
4. **HR** (人力资源) - 用户管理权限

### 预置权限（22个）
- **MENU**: 10个菜单权限
- **BUTTON**: 5个按钮权限
- **API**: 7个API权限

### 测试用户账号
```
admin / 123456     → SUPER_ADMIN (所有权限)
manager / 123456   → MANAGER (工作流权限)
hr / 123456        → HR (用户管理权限)
user_1 / 123456    → EMPLOYEE (基础权限)
```

---

## 🎯 开发优先级建议

### 最小可用版本 (MVP) - 1小时
✅ Phase 1: 权限指令 + 动态菜单

**效果**: 
- 不同角色看到不同的菜单
- 按钮根据权限显示/隐藏
- 核心权限控制已实现

### 完整版本 - 2.5小时
✅ Phase 1 + Phase 2 + Phase 5 + Phase 6

**效果**:
- MVP功能
- 可视化角色管理
- 完整的CRUD操作
- 权限分配界面

### 完美版本 - 3.5小时
✅ 全部Phase

**效果**:
- 完整版功能
- 权限管理页面
- 用户管理增强
- 完整的RBAC UI

---

## 🐛 常见问题

### Q1: 权限指令不生效
**原因**: userStore中permissions未正确加载  
**解决**: 检查登录后是否调用了getUserInfo，确认permissions字段存在

### Q2: 菜单没有根据权限过滤
**原因**: 菜单配置中缺少permission字段  
**解决**: 为每个菜单项添加对应的权限编码

### Q3: API返回中文乱码
**原因**: HTTP响应编码问题  
**解决**: 已知问题，不影响功能，JSON解析后中文正常显示

### Q4: 树形权限选择器不显示已选中
**原因**: default-checked-keys格式错误  
**解决**: 确保传入的是权限ID数组（数字类型）

---

## 📚 参考文档

### 项目已完成功能
- ✅ 认证服务（登录、注册、JWT）
- ✅ 工作流服务（Camunda集成）
- ✅ 网关服务（Spring Cloud Gateway）
- ✅ Web服务层（Dubbo RPC）
- ✅ 前端基础框架（Vue 3 + Element Plus）
- ✅ 模板和草稿管理
- ✅ 历史参数回填
- ✅ 通知中心（后端完成，监听器待修复）
- ✅ BPMN可视化
- ✅ RBAC后端（本次完成）

### 重要文件位置
- 后端RBAC代码: `diom-auth-service/src/main/java/com/diom/auth/`
- 数据库脚本: `diom-auth-service/src/main/resources/sql/rbac-init.sql`
- 前端Store: `diom-frontend/src/stores/user.js`
- 前端API: `diom-frontend/src/api/`
- 前端路由: `diom-frontend/src/router/index.js`

### 技术栈
- **后端**: Spring Boot 2.4.11 + MyBatis Plus + Dubbo + Nacos
- **前端**: Vue 3 + Vite + Element Plus + Pinia + Vue Router
- **数据库**: MySQL 8.0
- **权限**: 自研RBAC（基于角色和权限编码）

---

## 🚀 开始开发

### 1. 启动命令（在新会话中）

```bash
# 对话开始语
我要继续开发DIOM工作流系统的RBAC前端UI。
后端RBAC已经100%完成并测试通过。
请参考 RBAC_FRONTEND_GUIDE.md 文件开始开发。
从Phase 1（权限指令和动态菜单）开始。
```

### 2. 文件准备确认

确认以下文件存在：
- ✅ `RBAC_DESIGN.md` - 设计方案
- ✅ `RBAC_BACKEND_COMPLETED.md` - 后端完成报告
- ✅ `RBAC_FRONTEND_GUIDE.md` - 本文档（前端开发指南）

### 3. 后端服务确认

确认以下服务正在运行：
- ✅ MySQL (Docker)
- ✅ Nacos (Docker)
- ✅ diom-auth-service (端口8081)
- ✅ diom-gateway (端口8083)
- ✅ diom-web-service (端口8082)
- ✅ diom-workflow-service (端口8085)
- ✅ diom-frontend (端口3000)

---

## 🎓 预期成果

### 完成后的系统状态
```
系统功能完整度:

核心功能:   ████████████████████ 100% ✅
认证授权:   ████████████████████ 100% ✅  (RBAC完整)
工作流:     ████████████████████ 100% ✅
模板管理:   ████████████████████ 100% ✅
历史回填:   ████████████████████ 100% ✅
流程图:     ████████████████████ 100% ✅
通知中心:   ████████░░░░░░░░░░░░  45% ⚠️
RBAC权限:   ████████████████████ 100% ✅  (前后端完整)
────────────────────────────────────────────────────────
总体进度:   ███████████████████░  98%
```

### 用户体验
- ✅ 不同角色登录看到不同的菜单
- ✅ 按钮根据权限显示/隐藏
- ✅ 可视化角色管理界面
- ✅ 树形权限分配界面
- ✅ 完整的权限控制体系

---

**文档创建时间**: 2025-11-15 18:10  
**预计开发时间**: 2-3小时  
**建议优先级**: Phase 1 → Phase 2 → Phase 5 → Phase 6 → Phase 3 → Phase 4  
**祝开发顺利！** 🎉

