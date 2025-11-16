# 🎨 在线流程设计器完整设计方案

**需求来源**: 用户反馈系统缺少可视化流程设计功能  
**核心需求**: 管理员能画、修改流程图并重新发布，发布后才能生效  
**设计时间**: 2025-11-15  
**设计状态**: ✅ 完整方案设计完成

---

## 📋 目录

1. [需求分析](#需求分析)
2. [技术选型](#技术选型)
3. [系统架构](#系统架构)
4. [数据库设计](#数据库设计)
5. [后端API设计](#后端api设计)
6. [前端设计](#前端设计)
7. [权限控制](#权限控制)
8. [发布流程](#发布流程)
9. [版本管理](#版本管理)
10. [实施计划](#实施计划)

---

## 📊 需求分析

### 核心需求

| 需求项 | 描述 | 优先级 |
|--------|------|--------|
| 可视化设计 | 管理员能在浏览器中拖拽式设计BPMN流程图 | ⭐⭐⭐ 必须 |
| 属性配置 | 配置任务名称、办理人、监听器等属性 | ⭐⭐⭐ 必须 |
| 保存草稿 | 保存未完成的设计，状态为DRAFT | ⭐⭐⭐ 必须 |
| 验证BPMN | 发布前验证BPMN语法和逻辑正确性 | ⭐⭐⭐ 必须 |
| 发布流程 | 将草稿发布为正式流程，部署到Camunda引擎 | ⭐⭐⭐ 必须 |
| 版本管理 | 每次发布生成新版本，支持查看历史版本 | ⭐⭐⭐ 必须 |
| 权限控制 | 只有管理员（ADMIN角色）可以操作 | ⭐⭐⭐ 必须 |
| 修改已发布流程 | 基于已发布流程创建新版本草稿，修改后重新发布 | ⭐⭐⭐ 必须 |
| 历史查看 | 查看流程的所有版本和变更历史 | ⭐⭐ 重要 |
| 流程分类 | 按人事、财务、行政等分类管理 | ⭐ 可选 |

### 用户角色与权限

| 角色 | 查看流程列表 | 查看设计器 | 创建流程 | 编辑草稿 | 发布流程 | 删除流程 |
|------|------------|------------|---------|---------|---------|---------|
| **管理员（ADMIN）** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 部门经理（MANAGER） | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 普通员工（EMPLOYEE） | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 🛠 技术选型

### 前端技术栈

| 技术 | 用途 | 理由 |
|------|------|------|
| **bpmn-js** | BPMN可视化设计器 | Camunda官方推荐，功能完善，社区活跃 |
| **bpmn-js-properties-panel** | 属性面板 | 配置任务、网关、监听器等属性 |
| **camunda-bpmn-moddle** | Camunda扩展 | 支持Camunda特有的属性（如assignee、listener） |
| Vue 3 + Element Plus | UI框架 | 与现有系统一致 |
| Pinia | 状态管理 | 管理流程设计状态 |

### 后端技术栈

| 技术 | 用途 | 理由 |
|------|------|------|
| Spring Boot 2.4.11 | 后端框架 | 与现有系统一致 |
| Camunda 7.16.0 | 流程引擎 | BPMN验证和部署 |
| MyBatis Plus | ORM | 数据库操作 |
| MySQL | 数据库 | 存储BPMN设计数据 |

---

## 🏗 系统架构

### 整体架构图

```
┌──────────────────────────────────────────────────────────────┐
│                        用户层（浏览器）                         │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │  流程设计器页面 (ProcessDesigner.vue)              │     │
│  │  ┌──────────────────────────────────────────────┐ │     │
│  │  │  bpmn-js 画布                                │ │     │
│  │  │  - 拖拽任务、网关、事件                      │ │     │
│  │  │  - 连接节点                                  │ │     │
│  │  │  - 缩放、平移画布                            │ │     │
│  │  └──────────────────────────────────────────────┘ │     │
│  │  ┌──────────────────────────────────────────────┐ │     │
│  │  │  属性面板                                    │ │     │
│  │  │  - 任务名称、办理人                          │ │     │
│  │  │  - 监听器配置                                │ │     │
│  │  │  - 表单配置                                  │ │     │
│  │  └──────────────────────────────────────────────┘ │     │
│  │  ┌──────────────────────────────────────────────┐ │     │
│  │  │  工具栏                                      │ │     │
│  │  │  [保存草稿] [验证] [发布] [导出XML] [查看历史]│ │     │
│  │  └──────────────────────────────────────────────┘ │     │
│  └────────────────────────────────────────────────────┘     │
│                            ↓ REST API                         │
├──────────────────────────────────────────────────────────────┤
│                        应用层（Spring Boot）                   │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │  ProcessDesignController                            │     │
│  │  - GET  /api/process-design/list           查询列表│     │
│  │  - GET  /api/process-design/{id}           查询详情│     │
│  │  - POST /api/process-design/save           保存草稿│     │
│  │  - POST /api/process-design/validate       验证BPMN│     │
│  │  - POST /api/process-design/publish        发布流程│     │
│  │  - GET  /api/process-design/{id}/history   查询历史│     │
│  │  - POST /api/process-design/{id}/new-version 新版本│     │
│  │  - DELETE /api/process-design/{id}         删除草稿│     │
│  └────────────────────────────────────────────────────┘     │
│                            ↓                                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │  ProcessDesignService                               │     │
│  │  - 业务逻辑处理                                     │     │
│  │  - BPMN验证（语法、逻辑）                           │     │
│  │  - 部署到Camunda引擎                                │     │
│  │  - 版本管理                                         │     │
│  └────────────────────────────────────────────────────┘     │
│                            ↓                                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Camunda RepositoryService                          │     │
│  │  - createDeployment() 部署BPMN                      │     │
│  │  - getProcessDefinition() 查询流程定义             │     │
│  └────────────────────────────────────────────────────┘     │
│                            ↓                                  │
├──────────────────────────────────────────────────────────────┤
│                        数据层（MySQL）                        │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │  workflow_process_design                            │     │
│  │  - 存储BPMN XML                                     │     │
│  │  - 状态管理（DRAFT/PUBLISHED/DEPRECATED）           │     │
│  │  - 版本控制                                         │     │
│  └────────────────────────────────────────────────────┘     │
│                                                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │  workflow_process_design_history                    │     │
│  │  - 变更历史                                         │     │
│  │  - 操作审计                                         │     │
│  └────────────────────────────────────────────────────┘     │
│                                                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Camunda表（act_re_procdef等）                      │     │
│  │  - 已部署的流程定义                                 │     │
│  │  - 运行时数据                                       │     │
│  └────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

---

## 🗄 数据库设计

### 表结构

#### 1. workflow_process_design（流程设计表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| process_key | VARCHAR(100) | 流程定义Key（唯一标识） |
| process_name | VARCHAR(200) | 流程名称 |
| version | INT | 版本号（从1开始递增） |
| status | VARCHAR(20) | 状态：DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已废弃 |
| bpmn_xml | LONGTEXT | BPMN XML内容 |
| description | VARCHAR(500) | 流程描述 |
| category | VARCHAR(50) | 流程分类 |
| deployment_id | VARCHAR(100) | Camunda部署ID（发布后生成） |
| process_definition_id | VARCHAR(100) | Camunda流程定义ID（发布后生成） |
| deployed_at | DATETIME | 部署时间 |
| creator | VARCHAR(100) | 创建人 |
| creator_name | VARCHAR(100) | 创建人姓名 |
| publisher | VARCHAR(100) | 发布人 |
| publisher_name | VARCHAR(100) | 发布人姓名 |
| publish_time | DATETIME | 发布时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**唯一索引**: `uk_process_key_version` (process_key, version)

#### 2. workflow_process_design_history（流程设计变更历史表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| design_id | BIGINT | 流程设计ID |
| process_key | VARCHAR(100) | 流程定义Key |
| version | INT | 版本号 |
| action | VARCHAR(20) | 操作类型：CREATE-创建, UPDATE-更新, PUBLISH-发布, DEPRECATE-废弃 |
| bpmn_xml | LONGTEXT | BPMN XML内容（快照） |
| change_description | VARCHAR(500) | 变更说明 |
| operator | VARCHAR(100) | 操作人 |
| operator_name | VARCHAR(100) | 操作人姓名 |
| create_time | DATETIME | 操作时间 |

### 状态转换图

```
                 [创建新流程]
                      ↓
              ┌───────────────┐
              │  DRAFT (草稿) │ ←──────┐
              └───────────────┘        │
                      │                │
              [保存草稿/修改]           │
                      ↓                │
              ┌───────────────┐        │
              │  DRAFT (草稿) │        │
              └───────────────┘        │
                      │                │
              [发布流程]               │
                      ↓                │
            ┌───────────────────┐     │
            │ PUBLISHED (已发布) │     │
            └───────────────────┘     │
                      │                │
            [创建新版本]               │
                      │                │
                      └────────────────┘
                      
            [废弃流程]
                      ↓
            ┌───────────────────┐
            │ DEPRECATED (已废弃)│
            └───────────────────┘
```

---

## 🔌 后端API设计

### 1. 查询流程列表

```http
GET /api/process-design/list
```

**请求参数**:
```json
{
  "status": "PUBLISHED",  // 可选：DRAFT, PUBLISHED, DEPRECATED, ALL
  "category": "人事",      // 可选：流程分类
  "keyword": "请假",       // 可选：搜索关键字（匹配流程名称、Key）
  "page": 1,
  "pageSize": 10
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "processKey": "leave-approval-process",
        "processName": "请假审批流程",
        "version": 2,
        "status": "PUBLISHED",
        "category": "人事",
        "description": "员工请假审批流程",
        "creator": "admin",
        "creatorName": "管理员",
        "publisher": "admin",
        "publisherName": "管理员",
        "publishTime": "2025-11-15 14:00:00",
        "createTime": "2025-11-15 10:00:00",
        "updateTime": "2025-11-15 14:00:00",
        "hasNewerVersion": false  // 是否有更新版本
      }
    ]
  }
}
```

### 2. 查询流程详情

```http
GET /api/process-design/{id}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "processKey": "leave-approval-process",
    "processName": "请假审批流程",
    "version": 2,
    "status": "PUBLISHED",
    "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...",
    "description": "员工请假审批流程",
    "category": "人事",
    "deploymentId": "abc123",
    "processDefinitionId": "leave-approval-process:2:abc123",
    "creator": "admin",
    "publisher": "admin",
    "publishTime": "2025-11-15 14:00:00"
  }
}
```

### 3. 保存草稿

```http
POST /api/process-design/save
```

**请求体**:
```json
{
  "id": null,  // 新建时为null，更新时传ID
  "processKey": "leave-approval-process-v2",
  "processName": "请假审批流程V2",
  "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...",
  "description": "优化审批流程，增加HR审批环节",
  "category": "人事"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": {
    "id": 5,
    "version": 1,
    "status": "DRAFT"
  }
}
```

### 4. 验证BPMN

```http
POST /api/process-design/validate
```

**请求体**:
```json
{
  "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
}
```

**响应（验证通过）**:
```json
{
  "code": 200,
  "message": "验证通过",
  "data": {
    "valid": true,
    "processKey": "leave-approval-process",
    "processName": "请假审批流程",
    "startEvents": 1,
    "userTasks": 2,
    "endEvents": 1
  }
}
```

**响应（验证失败）**:
```json
{
  "code": 400,
  "message": "验证失败",
  "data": {
    "valid": false,
    "errors": [
      {
        "line": 15,
        "column": 30,
        "message": "任务'fillLeaveForm'缺少assignee属性"
      },
      {
        "line": 20,
        "message": "流程必须有且只有一个开始事件"
      }
    ]
  }
}
```

### 5. 发布流程

```http
POST /api/process-design/publish
```

**请求体**:
```json
{
  "id": 5,
  "changeDescription": "增加HR审批环节，优化审批流程"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 5,
    "version": 1,
    "status": "PUBLISHED",
    "deploymentId": "def456",
    "processDefinitionId": "leave-approval-process-v2:1:def456",
    "publishTime": "2025-11-15 15:00:00"
  }
}
```

### 6. 查询变更历史

```http
GET /api/process-design/{id}/history
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "version": 2,
      "action": "PUBLISH",
      "changeDescription": "增加HR审批环节",
      "operator": "admin",
      "operatorName": "管理员",
      "createTime": "2025-11-15 15:00:00"
    },
    {
      "id": 9,
      "version": 2,
      "action": "UPDATE",
      "changeDescription": "修改任务名称",
      "operator": "admin",
      "operatorName": "管理员",
      "createTime": "2025-11-15 14:30:00"
    }
  ]
}
```

### 7. 创建新版本

```http
POST /api/process-design/{id}/new-version
```

**说明**: 基于已发布的流程创建新版本草稿

**请求体**:
```json
{
  "changeDescription": "基于v1创建新版本，准备优化流程"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "新版本创建成功",
  "data": {
    "id": 6,
    "version": 2,
    "status": "DRAFT",
    "baseVersion": 1
  }
}
```

### 8. 删除草稿

```http
DELETE /api/process-design/{id}
```

**说明**: 只能删除DRAFT状态的流程，已发布的流程只能废弃

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 🎨 前端设计

### 路由配置

```javascript
// diom-frontend/src/router/index.js
{
  path: '/workflow/designer',
  name: 'ProcessDesigner',
  component: () => import('@/views/Workflow/ProcessDesigner.vue'),
  meta: { 
    title: '流程设计器', 
    requiresAuth: true,
    permission: 'workflow:design:manage'  // 需要管理员权限
  }
},
{
  path: '/workflow/designer/:id',
  name: 'ProcessDesignerEdit',
  component: () => import('@/views/Workflow/ProcessDesigner.vue'),
  meta: { 
    title: '编辑流程', 
    requiresAuth: true,
    permission: 'workflow:design:manage'
  }
}
```

### 页面结构

```vue
<!-- diom-frontend/src/views/Workflow/ProcessDesigner.vue -->
<template>
  <div class="process-designer-container">
    <!-- 顶部工具栏 -->
    <div class="designer-toolbar">
      <el-input v-model="processName" placeholder="流程名称" style="width: 300px" />
      <el-input v-model="processKey" placeholder="流程Key（唯一标识）" style="width: 200px" />
      <el-select v-model="category" placeholder="流程分类" style="width: 150px">
        <el-option label="人事" value="人事" />
        <el-option label="财务" value="财务" />
        <el-option label="行政" value="行政" />
      </el-select>
      
      <div class="toolbar-actions">
        <el-button @click="saveDraft" :loading="saving">
          <el-icon><DocumentAdd /></el-icon> 保存草稿
        </el-button>
        <el-button @click="validate" :loading="validating">
          <el-icon><CircleCheck /></el-icon> 验证
        </el-button>
        <el-button type="primary" @click="publish" :loading="publishing">
          <el-icon><Upload /></el-icon> 发布
        </el-button>
        <el-button @click="viewHistory">
          <el-icon><Clock /></el-icon> 历史版本
        </el-button>
        <el-button @click="exportXML">
          <el-icon><Download /></el-icon> 导出XML
        </el-button>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="designer-content">
      <!-- 左侧：BPMN画布 -->
      <div class="designer-canvas">
        <div ref="bpmnCanvas" class="bpmn-container"></div>
      </div>
      
      <!-- 右侧：属性面板 -->
      <div class="designer-properties">
        <div ref="propertiesPanel" class="properties-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import CamundaBpmnModdle from 'camunda-bpmn-moddle/resources/camunda.json'

const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
let modeler = null

const processName = ref('')
const processKey = ref('')
const category = ref('')

onMounted(() => {
  initBpmnModeler()
})

const initBpmnModeler = () => {
  modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    propertiesPanel: {
      parent: propertiesPanel.value
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule
    ],
    moddleExtensions: {
      camunda: CamundaBpmnModdle
    }
  })
  
  // 加载空白BPMN或已有BPMN
  if (route.params.id) {
    loadExistingProcess(route.params.id)
  } else {
    createNewProcess()
  }
}

const saveDraft = async () => {
  // 获取BPMN XML
  const { xml } = await modeler.saveXML({ format: true })
  
  // 调用API保存
  await api.post('/api/process-design/save', {
    id: designId.value,
    processKey: processKey.value,
    processName: processName.value,
    bpmnXml: xml,
    category: category.value
  })
  
  ElMessage.success('保存成功')
}

const validate = async () => {
  const { xml } = await modeler.saveXML({ format: true })
  
  const result = await api.post('/api/process-design/validate', {
    bpmnXml: xml
  })
  
  if (result.data.valid) {
    ElMessage.success('验证通过')
  } else {
    // 显示错误列表
    showValidationErrors(result.data.errors)
  }
}

const publish = async () => {
  await ElMessageBox.prompt('请输入变更说明', '发布流程', {
    confirmButtonText: '发布',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    await api.post('/api/process-design/publish', {
      id: designId.value,
      changeDescription: value
    })
    
    ElMessage.success('发布成功')
    router.push('/workflow/definitions')
  })
}
</script>
```

### NPM依赖

```json
{
  "dependencies": {
    "bpmn-js": "^14.0.0",
    "bpmn-js-properties-panel": "^3.0.0",
    "camunda-bpmn-moddle": "^7.0.0"
  }
}
```

---

## 🔒 权限控制

### RBAC权限配置

在 `sys_permission` 表中添加新权限：

```sql
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `sort_order`)
VALUES 
('workflow:design:view', '查看流程设计', 'BUTTON', 
 (SELECT id FROM sys_permission WHERE permission_code = 'workflow:manage'), 1),
 
('workflow:design:create', '创建流程设计', 'BUTTON', 
 (SELECT id FROM sys_permission WHERE permission_code = 'workflow:manage'), 2),
 
('workflow:design:edit', '编辑流程设计', 'BUTTON', 
 (SELECT id FROM sys_permission WHERE permission_code = 'workflow:manage'), 3),
 
('workflow:design:publish', '发布流程', 'BUTTON', 
 (SELECT id FROM sys_permission WHERE permission_code = 'workflow:manage'), 4),
 
('workflow:design:delete', '删除流程设计', 'BUTTON', 
 (SELECT id FROM sys_permission WHERE permission_code = 'workflow:manage'), 5);
```

### 后端权限控制

```java
@RestController
@RequestMapping("/api/process-design")
public class ProcessDesignController {
    
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('workflow:design:view')")
    public Result<PageResult<ProcessDesignVO>> list(@RequestParam Map<String, Object> params) {
        // ...
    }
    
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('workflow:design:create', 'workflow:design:edit')")
    public Result<ProcessDesignVO> save(@RequestBody ProcessDesignDTO dto) {
        // ...
    }
    
    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('workflow:design:publish')")
    public Result<ProcessDesignVO> publish(@RequestBody PublishDTO dto) {
        // ...
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow:design:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        // ...
    }
}
```

### 前端权限控制

```vue
<!-- 只有拥有权限的用户才能看到按钮 -->
<el-button 
  v-permission="'workflow:design:create'"
  @click="createProcess">
  新建流程
</el-button>

<el-button 
  v-permission="'workflow:design:publish'"
  @click="publish">
  发布
</el-button>
```

---

## 🚀 发布流程

### 发布流程图

```
┌──────────────┐
│ 1. 保存草稿  │ → DRAFT状态，存入workflow_process_design
└──────────────┘
        ↓
┌──────────────┐
│ 2. 验证BPMN  │ → 检查语法、逻辑、必填属性
└──────────────┘
        ↓
    [验证通过?]
        ↓ Yes
┌──────────────┐
│ 3. 发布确认  │ → 用户输入变更说明
└──────────────┘
        ↓
┌──────────────┐
│ 4. 部署到引擎│ → Camunda RepositoryService.createDeployment()
└──────────────┘
        ↓
    [部署成功?]
        ↓ Yes
┌──────────────┐
│ 5. 更新状态  │ → workflow_process_design.status = 'PUBLISHED'
│              │   deployment_id, process_definition_id
└──────────────┘
        ↓
┌──────────────┐
│ 6. 记录历史  │ → workflow_process_design_history (action='PUBLISH')
└──────────────┘
        ↓
┌──────────────┐
│ 7. 发布成功  │ → 流程可以被用户发起了
└──────────────┘
```

### 关键代码

```java
@Service
public class ProcessDesignService {
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Transactional
    public ProcessDesignVO publish(Long id, String changeDescription, String operator) {
        // 1. 查询草稿
        ProcessDesign design = processDesignMapper.selectById(id);
        if (!"DRAFT".equals(design.getStatus())) {
            throw new BusinessException("只能发布草稿状态的流程");
        }
        
        // 2. 验证BPMN
        ValidationResult validation = validateBpmn(design.getBpmnXml());
        if (!validation.isValid()) {
            throw new BusinessException("BPMN验证失败: " + validation.getErrors());
        }
        
        // 3. 部署到Camunda引擎
        Deployment deployment = repositoryService.createDeployment()
            .name(design.getProcessName() + " v" + design.getVersion())
            .addString(design.getProcessKey() + ".bpmn", design.getBpmnXml())
            .deploy();
        
        // 4. 获取流程定义ID
        ProcessDefinition processDefinition = repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();
        
        // 5. 更新设计状态
        design.setStatus("PUBLISHED");
        design.setDeploymentId(deployment.getId());
        design.setProcessDefinitionId(processDefinition.getId());
        design.setPublisher(operator);
        design.setPublishTime(LocalDateTime.now());
        design.setDeployedAt(LocalDateTime.now());
        processDesignMapper.updateById(design);
        
        // 6. 记录历史
        ProcessDesignHistory history = new ProcessDesignHistory();
        history.setDesignId(design.getId());
        history.setProcessKey(design.getProcessKey());
        history.setVersion(design.getVersion());
        history.setAction("PUBLISH");
        history.setBpmnXml(design.getBpmnXml());
        history.setChangeDescription(changeDescription);
        history.setOperator(operator);
        processDesignHistoryMapper.insert(history);
        
        return convert(design);
    }
}
```

---

## 📦 版本管理

### 版本管理策略

1. **同一流程的版本递增**
   - `leave-approval-process` v1, v2, v3...
   - 版本号自动递增
   - 同一时间只有一个PUBLISHED版本是最新的

2. **创建新版本流程**
   ```
   已发布流程 v1 (PUBLISHED)
        ↓ [点击"创建新版本"]
   新建草稿 v2 (DRAFT) ← 复制v1的BPMN内容
        ↓ [修改并发布]
   已发布流程 v2 (PUBLISHED)
   已发布流程 v1 (PUBLISHED) ← 仍保留，但不是最新版本
   ```

3. **版本查询**
   - 查询流程列表时，默认只显示每个process_key的最新PUBLISHED版本
   - 可以查看某个流程的所有历史版本
   - 可以回滚到历史版本（创建基于历史版本的新草稿）

### 版本管理API

```java
// 查询某个流程的所有版本
GET /api/process-design/versions?processKey=leave-approval-process

// 响应
{
  "code": 200,
  "data": [
    {
      "id": 5,
      "version": 2,
      "status": "PUBLISHED",
      "publishTime": "2025-11-15 15:00:00",
      "isLatest": true
    },
    {
      "id": 1,
      "version": 1,
      "status": "PUBLISHED",
      "publishTime": "2025-11-15 10:00:00",
      "isLatest": false
    }
  ]
}

// 基于某个版本创建新草稿
POST /api/process-design/{id}/new-version
```

---

## 📅 实施计划

### 开发阶段

| 阶段 | 任务 | 预计工时 | 优先级 |
|------|------|---------|--------|
| **Phase 1** | **数据库和实体** | **4小时** | ⭐⭐⭐ |
| 1.1 | 创建数据库表（process_design.sql） | 1小时 | 必须 |
| 1.2 | 创建Entity和Mapper（ProcessDesign, ProcessDesignHistory） | 2小时 | 必须 |
| 1.3 | 编写基础Service和Controller | 1小时 | 必须 |
| **Phase 2** | **后端核心功能** | **12小时** | ⭐⭐⭐ |
| 2.1 | 实现保存草稿API | 2小时 | 必须 |
| 2.2 | 实现BPMN验证逻辑 | 3小时 | 必须 |
| 2.3 | 实现发布流程API（部署到Camunda） | 3小时 | 必须 |
| 2.4 | 实现版本管理API | 2小时 | 必须 |
| 2.5 | 实现历史查询API | 2小时 | 必须 |
| **Phase 3** | **前端设计器界面** | **16小时** | ⭐⭐⭐ |
| 3.1 | 安装bpmn-js依赖，初始化画布 | 2小时 | 必须 |
| 3.2 | 集成属性面板（properties-panel） | 3小时 | 必须 |
| 3.3 | 实现工具栏（保存、验证、发布） | 3小时 | 必须 |
| 3.4 | 实现流程列表页面 | 2小时 | 必须 |
| 3.5 | 实现历史版本查看 | 2小时 | 必须 |
| 3.6 | 样式优化和交互优化 | 4小时 | 重要 |
| **Phase 4** | **权限集成和测试** | **8小时** | ⭐⭐⭐ |
| 4.1 | 添加RBAC权限配置 | 2小时 | 必须 |
| 4.2 | 前后端权限控制集成 | 2小时 | 必须 |
| 4.3 | 端到端功能测试 | 3小时 | 必须 |
| 4.4 | 文档编写 | 1小时 | 重要 |

**总计**: 40小时（约5个工作日）

---

## ✅ 验收标准

### 功能验收

- [ ] 管理员能进入流程设计器页面
- [ ] 能拖拽创建开始事件、用户任务、网关、结束事件
- [ ] 能连接节点形成完整流程
- [ ] 能配置任务属性（名称、办理人、监听器）
- [ ] 能保存草稿（DRAFT状态）
- [ ] 能验证BPMN（显示错误信息）
- [ ] 能发布流程（部署到Camunda引擎）
- [ ] 发布后能在流程定义列表看到新流程
- [ ] 用户能发起已发布的流程
- [ ] 能基于已发布流程创建新版本
- [ ] 能查看流程的所有历史版本
- [ ] 能查看变更历史记录
- [ ] 非管理员无法访问设计器

### 性能验收

- [ ] 设计器画布加载时间 < 2秒
- [ ] 保存草稿响应时间 < 1秒
- [ ] 发布流程响应时间 < 3秒
- [ ] 支持复杂流程（50+节点）

### 安全验收

- [ ] 权限控制生效（非管理员无法访问）
- [ ] BPMN验证防止恶意XML
- [ ] 操作审计记录完整

---

## 🎯 下一步行动

请您确认以下几点：

1. **是否接受此设计方案？**
2. **是否现在开始实施？**
3. **实施顺序建议**：
   - 选项A：完整实施（Phase 1-4，约5个工作日）
   - 选项B：MVP版本（Phase 1-2 + 简化前端，约3个工作日）
   - 选项C：分阶段实施（先完成Phase 1-2，验收后再做Phase 3-4）

---

**设计文档版本**: v1.0  
**设计日期**: 2025-11-15  
**设计者**: AI Assistant  
**审核状态**: 待用户确认

