# Workflow Service API 文档

## 📋 目录

- [流程定义管理](#流程定义管理)
- [流程实例管理](#流程实例管理)
- [任务管理](#任务管理)
- [历史查询](#历史查询)
- [请假审批流程示例](#请假审批流程示例)

---

## 流程定义管理

### 1. 获取所有流程定义

获取系统中部署的所有流程定义（最新版本）

**请求**

```http
GET /workflow/definitions
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "leave-approval-process:1:xxx",
      "key": "leave-approval-process",
      "name": "请假审批流程",
      "version": "1",
      "description": null,
      "deploymentId": "xxx",
      "suspended": false
    }
  ]
}
```

### 2. 根据Key获取流程定义

**请求**

```http
GET /workflow/definition/{key}
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| key | string | 是 | 流程定义Key |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "leave-approval-process:1:xxx",
    "key": "leave-approval-process",
    "name": "请假审批流程",
    "version": "1",
    "description": null,
    "deploymentId": "xxx",
    "suspended": false
  }
}
```

---

## 流程实例管理

### 1. 启动流程实例

**请求**

```http
POST /workflow/start/{processKey}
Content-Type: application/json
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processKey | string | 是 | 流程定义Key（路径参数） |
| variables | object | 否 | 流程变量（请求体） |

**请求体示例**

```json
{
  "applicant": "zhangsan",
  "manager": "lisi",
  "leaveType": "annual",
  "startDate": "2025-12-01",
  "endDate": "2025-12-03",
  "days": 3,
  "reason": "年度旅游"
}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "process-instance-id",
    "processDefinitionId": "leave-approval-process:1:xxx",
    "processDefinitionKey": "leave-approval-process",
    "processDefinitionName": "请假审批流程",
    "businessKey": null,
    "ended": false,
    "suspended": false,
    "variables": {
      "applicant": "zhangsan",
      "manager": "lisi",
      "leaveType": "annual",
      "startDate": "2025-12-01",
      "endDate": "2025-12-03",
      "days": 3,
      "reason": "年度旅游"
    },
    "startTime": null
  }
}
```

### 2. 启动流程实例（带业务Key）

**请求**

```http
POST /workflow/start/{processKey}/business/{businessKey}
Content-Type: application/json
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processKey | string | 是 | 流程定义Key |
| businessKey | string | 是 | 业务Key（如订单号） |
| variables | object | 否 | 流程变量 |

### 3. 查询流程实例

**请求**

```http
GET /workflow/instance/{instanceId}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "process-instance-id",
    "processDefinitionId": "leave-approval-process:1:xxx",
    "processDefinitionKey": "leave-approval-process",
    "processDefinitionName": "请假审批流程",
    "businessKey": null,
    "ended": false,
    "suspended": false,
    "variables": {
      // 流程变量
    },
    "startTime": null
  }
}
```

### 4. 获取流程变量

**请求**

```http
GET /workflow/instance/{instanceId}/variables
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "applicant": "zhangsan",
    "manager": "lisi",
    "approved": true,
    "leaveRequestId": "LR1731639123456"
  }
}
```

### 5. 设置流程变量

**请求**

```http
POST /workflow/instance/{instanceId}/variables
Content-Type: application/json
```

**请求体**

```json
{
  "customField1": "value1",
  "customField2": "value2"
}
```

### 6. 删除流程实例

**请求**

```http
DELETE /workflow/instance/{instanceId}?reason=取消流程
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| instanceId | string | 是 | 流程实例ID |
| reason | string | 否 | 删除原因 |

---

## 任务管理

### 1. 查询用户任务列表

**请求**

```http
GET /workflow/tasks?assignee={username}
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assignee | string | 是 | 任务办理人 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "task-id",
      "name": "填写请假单",
      "description": null,
      "assignee": "zhangsan",
      "createTime": "2025-11-15T10:00:00",
      "dueDate": null,
      "processInstanceId": "process-instance-id",
      "processDefinitionKey": "leave-approval-process",
      "processDefinitionName": "请假审批流程",
      "businessKey": null,
      "variables": null
    }
  ]
}
```

### 2. 查询任务详情

**请求**

```http
GET /workflow/task/{taskId}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "task-id",
    "name": "部门经理审批",
    "description": null,
    "assignee": "lisi",
    "createTime": "2025-11-15T10:05:00",
    "dueDate": null,
    "processInstanceId": "process-instance-id",
    "processDefinitionKey": "leave-approval-process",
    "processDefinitionName": "请假审批流程",
    "businessKey": null,
    "variables": {
      "applicant": "zhangsan",
      "leaveType": "annual",
      "days": 3,
      "reason": "年度旅游"
    }
  }
}
```

### 3. 完成任务

**请求**

```http
POST /workflow/task/{taskId}/complete
Content-Type: application/json
```

**请求体**

```json
{
  "approved": true,
  "approvalComment": "同意请假"
}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "任务完成成功"
}
```

### 4. 认领任务

**请求**

```http
POST /workflow/task/{taskId}/claim?userId=zhangsan
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | string | 是 | 任务ID |
| userId | string | 是 | 用户ID |

### 5. 转办任务

**请求**

```http
POST /workflow/task/{taskId}/delegate?userId=wangwu
```

**参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | string | 是 | 任务ID |
| userId | string | 是 | 目标用户ID |

### 6. 获取任务变量

**请求**

```http
GET /workflow/task/{taskId}/variables
```

### 7. 设置任务变量

**请求**

```http
POST /workflow/task/{taskId}/variables
Content-Type: application/json
```

**请求体**

```json
{
  "customField": "value"
}
```

---

## 历史查询

### 1. 查询历史流程实例

**请求**

```http
GET /workflow/history/instances?processDefinitionKey=leave-approval-process
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "process-instance-id",
      "processDefinitionKey": "leave-approval-process",
      "processDefinitionName": "请假审批流程",
      "businessKey": null,
      "startTime": "2025-11-15T10:00:00",
      "endTime": "2025-11-15T10:10:00",
      "durationInMillis": 600000,
      "startUserId": null
    }
  ]
}
```

### 2. 查询历史任务

**请求**

```http
GET /workflow/history/tasks?processInstanceId={instanceId}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "task-id-1",
      "name": "填写请假单",
      "assignee": "zhangsan",
      "startTime": "2025-11-15T10:00:00",
      "endTime": "2025-11-15T10:02:00",
      "durationInMillis": 120000
    },
    {
      "id": "task-id-2",
      "name": "部门经理审批",
      "assignee": "lisi",
      "startTime": "2025-11-15T10:05:00",
      "endTime": "2025-11-15T10:10:00",
      "durationInMillis": 300000
    }
  ]
}
```

---

## 请假审批流程示例

### 完整流程演示

```bash
#!/bin/bash

BASE_URL="http://localhost:8083/workflow"

# 1. 启动流程
curl -X POST "$BASE_URL/start/leave-approval-process" \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": "zhangsan",
    "manager": "lisi",
    "leaveType": "annual",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "days": 3,
    "reason": "年度旅游"
  }'

# 2. 查询申请人任务
curl "$BASE_URL/tasks?assignee=zhangsan"

# 3. 完成填写请假单
curl -X POST "$BASE_URL/task/{taskId}/complete" \
  -H "Content-Type: application/json" \
  -d '{}'

# 4. 查询经理任务
curl "$BASE_URL/tasks?assignee=lisi"

# 5. 经理审批（同意）
curl -X POST "$BASE_URL/task/{taskId}/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "approvalComment": "同意请假"
  }'

# 6. 查询流程历史
curl "$BASE_URL/history/instances?processDefinitionKey=leave-approval-process"
```

### 流程节点说明

| 节点 | 类型 | 办理人 | 说明 |
|------|------|--------|------|
| 发起请假申请 | 开始事件 | - | 流程开始 |
| 填写请假单 | 用户任务 | applicant | 申请人填写请假信息 |
| 通知部门经理 | 服务任务 | - | 自动通知经理 |
| 部门经理审批 | 用户任务 | manager | 经理审批请假申请 |
| 审批结果判断 | 排他网关 | - | 根据approved变量分支 |
| HR备案 | 服务任务 | - | 自动记录到HR系统 |
| 通知申请人 | 服务任务 | - | 通知申请人审批结果 |
| 结束 | 结束事件 | - | 流程结束 |

### 流程变量

| 变量名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| applicant | string | 是 | 申请人 |
| manager | string | 是 | 审批人 |
| leaveType | string | 是 | 请假类型（annual/sick/personal） |
| startDate | string | 是 | 开始日期 |
| endDate | string | 是 | 结束日期 |
| days | number | 是 | 请假天数 |
| reason | string | 是 | 请假原因 |
| approved | boolean | - | 审批结果（经理审批时设置） |
| approvalComment | string | - | 审批意见 |
| leaveRequestId | string | - | 请假单编号（自动生成） |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 测试工具

### 1. 快速测试脚本

```bash
# 测试请假审批流程
./test-leave-approval.sh
```

### 2. 基础功能测试

```bash
# 测试Camunda基础功能
./test-camunda.sh
```

---

**最后更新**: 2025-11-15  
**API版本**: 1.0.0

