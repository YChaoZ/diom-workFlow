# Flowable 工作流服务 API 文档

**版本**: 1.0.0  
**基础URL**: `http://localhost:8086`  
**引擎**: Flowable 6.8.0

---

## 📌 API 兼容性说明

本服务的 REST API 与 Camunda 服务 **完全兼容**，所有接口路径、参数、返回格式保持一致。前端无需修改即可切换使用。

---

## 🔐 认证说明

所有API（除了白名单路径）都需要通过 Gateway 的 JWT 认证。

**请求头**：
```
Authorization: Bearer {JWT_TOKEN}
```

Gateway 会自动注入以下请求头到下游服务：
- `X-User-Id`: 用户ID
- `X-Username`: 用户名

---

## 1. 流程定义管理

### 1.1 获取所有流程定义

**接口**: `GET /workflow/definitions`

**描述**: 获取所有已部署的流程定义（最新版本）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "leave-approval-process:1:123456",
      "key": "leave-approval-process",
      "name": "请假审批流程",
      "version": "1",
      "description": "员工请假审批流程",
      "deploymentId": "deploy-001",
      "suspended": false
    }
  ]
}
```

---

### 1.2 根据Key获取流程定义

**接口**: `GET /workflow/definition/{key}`

**路径参数**:
- `key`: 流程定义Key（如 `leave-approval-process`）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "leave-approval-process:1:123456",
    "key": "leave-approval-process",
    "name": "请假审批流程",
    "version": "1"
  }
}
```

---

### 1.3 获取流程定义的BPMN XML

**接口**: `GET /workflow/definition/{key}/xml`

**路径参数**:
- `key`: 流程定义Key

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...</bpmn:definitions>"
}
```

---

## 2. 流程实例管理

### 2.1 启动流程实例

**接口**: `POST /workflow/start/{processKey}`

**路径参数**:
- `processKey`: 流程定义Key

**请求体**:
```json
{
  "applicant": "张三",
  "reason": "家中有事",
  "days": 3
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "proc-001",
    "processDefinitionId": "leave-approval-process:1:123456",
    "processDefinitionKey": "leave-approval-process",
    "processDefinitionName": "请假审批流程",
    "businessKey": null,
    "ended": false,
    "suspended": false,
    "variables": {
      "applicant": "张三",
      "reason": "家中有事",
      "days": 3
    }
  }
}
```

---

### 2.2 启动流程实例（带业务Key）

**接口**: `POST /workflow/start/{processKey}/business/{businessKey}`

**路径参数**:
- `processKey`: 流程定义Key
- `businessKey`: 业务Key（如订单号、申请单号）

**请求体**: 同 2.1

---

### 2.3 查询流程实例

**接口**: `GET /workflow/instance/{processInstanceId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "proc-001",
    "processDefinitionKey": "leave-approval-process",
    "businessKey": "LEAVE-20231123-001",
    "ended": false,
    "variables": {...}
  }
}
```

---

### 2.4 获取流程变量

**接口**: `GET /workflow/instance/{processInstanceId}/variables`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "applicant": "张三",
    "approved": true,
    "approver": "李四"
  }
}
```

---

### 2.5 设置流程变量

**接口**: `POST /workflow/instance/{processInstanceId}/variables`

**请求体**:
```json
{
  "status": "approved",
  "comment": "同意请假"
}
```

---

### 2.6 删除流程实例

**接口**: `DELETE /workflow/instance/{processInstanceId}`

**查询参数**:
- `reason`: 删除原因

---

## 3. 任务管理

### 3.1 查询用户任务列表

**接口**: `GET /workflow/tasks`

**查询参数**:
- `assignee`: 任务分配人（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "task-001",
      "name": "经理审批",
      "description": "请审批请假申请",
      "assignee": "manager",
      "createTime": "2023-11-23T10:00:00",
      "dueDate": null,
      "processInstanceId": "proc-001",
      "processDefinitionKey": "leave-approval-process",
      "processDefinitionName": "请假审批流程",
      "businessKey": "LEAVE-20231123-001"
    }
  ]
}
```

---

### 3.2 查询任务详情

**接口**: `GET /workflow/task/{taskId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "task-001",
    "name": "经理审批",
    "assignee": "manager",
    "processInstanceId": "proc-001",
    "variables": {
      "applicant": "张三",
      "days": 3
    }
  }
}
```

---

### 3.3 完成任务

**接口**: `POST /workflow/tasks/{taskId}/complete`

**请求体**:
```json
{
  "approved": true,
  "comment": "同意请假"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success"
}
```

---

### 3.4 认领任务

**接口**: `POST /workflow/tasks/{taskId}/claim`

**请求体**:
```json
{
  "userId": "zhangsan"
}
```

---

### 3.5 转办任务

**接口**: `POST /workflow/tasks/{taskId}/delegate`

**请求体**:
```json
{
  "userId": "lisi"
}
```

---

## 4. 历史查询

### 4.1 查询历史流程实例

**接口**: `GET /workflow/history/process-instances`

**查询参数**:
- `processDefinitionKey`: 流程定义Key

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "proc-001",
      "processDefinitionKey": "leave-approval-process",
      "startTime": "2023-11-23T10:00:00",
      "endTime": "2023-11-23T11:00:00",
      "durationInMillis": 3600000
    }
  ]
}
```

---

### 4.2 查询历史任务

**接口**: `GET /workflow/history/tasks`

**查询参数**:
- `processInstanceId`: 流程实例ID

---

## 5. 流程设计器 API

### 5.1 查询流程设计列表

**接口**: `GET /workflow/api/process-design/list`

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页大小（默认10）
- `processName`: 流程名称（模糊查询，可选）
- `status`: 状态过滤（DRAFT/PUBLISHED/DEPRECATED，可选）

**响应示例**:
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
        "version": 1,
        "status": "PUBLISHED",
        "category": "人事",
        "creator": "admin",
        "createTime": "2023-11-20T10:00:00"
      }
    ]
  }
}
```

---

### 5.2 查询流程设计详情

**接口**: `GET /workflow/api/process-design/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "processKey": "leave-approval-process",
    "processName": "请假审批流程",
    "bpmnXml": "<?xml version=\"1.0\"...",
    "status": "PUBLISHED"
  }
}
```

---

### 5.3 保存流程设计

**接口**: `POST /workflow/api/process-design`

**请求体**:
```json
{
  "processKey": "new-process",
  "processName": "新流程",
  "bpmnXml": "<?xml version=\"1.0\"...",
  "description": "流程描述",
  "category": "人事"
}
```

---

### 5.4 更新流程设计

**接口**: `PUT /workflow/api/process-design/{id}`

**请求体**: 同 5.3

---

### 5.5 发布流程

**接口**: `POST /workflow/api/process-design/{id}/publish`

**响应示例**:
```json
{
  "code": 200,
  "message": "流程发布成功",
  "data": {
    "deploymentId": "deploy-001",
    "processDefinitionId": "new-process:1:123456"
  }
}
```

---

### 5.6 验证BPMN

**接口**: `POST /workflow/api/process-design/validate`

**请求体**:
```json
{
  "bpmnXml": "<?xml version=\"1.0\"..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "errors": []
  }
}
```

---

### 5.7 删除流程设计

**接口**: `DELETE /workflow/api/process-design/{id}`

---

## 6. 健康检查与监控

### 6.1 服务健康检查

**接口**: `GET /actuator/health`

**响应示例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "flowable": {
      "status": "UP"
    }
  }
}
```

---

### 6.2 Flowable 引擎信息

**接口**: `GET /actuator/flowable`

---

## 📝 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或Token失效） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**错误响应格式**:
```json
{
  "code": 500,
  "message": "流程启动失败: 找不到流程定义",
  "data": null
}
```

---

## 🔄 与 Camunda API 的区别

本 Flowable 服务的 API 与 Camunda 服务 **完全一致**，唯一区别：

1. **路由前缀**:
   - Camunda: `/workflow/**`
   - Flowable: `/flowable/**`（通过Gateway配置）

2. **底层引擎**:
   - Camunda: Camunda BPM 7.16.0
   - Flowable: Flowable 6.8.0

3. **API 行为**: 完全相同，响应格式一致

---

## 🧪 测试示例

### 完整流程测试

```bash
# 1. 获取流程定义
curl http://localhost:8086/workflow/definitions

# 2. 启动流程
PROCESS_ID=$(curl -s -X POST http://localhost:8086/workflow/start/leave-approval-process \
  -H "Content-Type: application/json" \
  -d '{"applicant":"张三","days":3}' | jq -r '.data.id')

# 3. 查询任务
TASK_ID=$(curl -s "http://localhost:8086/workflow/tasks?assignee=manager" | jq -r '.data[0].id')

# 4. 完成任务
curl -X POST "http://localhost:8086/workflow/tasks/$TASK_ID/complete" \
  -H "Content-Type: application/json" \
  -d '{"approved":true}'
```

---

**最后更新**: 2025-11-23  
**联系方式**: diom@example.com

