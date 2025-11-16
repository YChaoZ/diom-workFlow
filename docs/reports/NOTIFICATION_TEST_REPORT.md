# 🧪 消息通知中心MCP测试报告

**测试时间**: 2025-11-15 19:20  
**测试工具**: Playwright MCP + 数据库验证  
**测试状态**: ❌ 核心功能未工作  
**严重程度**: 🔴 高（通知功能完全失效）

---

## 📋 测试概览

### ❌ 失败的测试
1. ❌ **TaskListener监听器未触发**（严重）
2. ❌ **通知未自动创建**（严重）
3. ❌ **顶部通知图标未读数为0**（严重）
4. ❌ **Manager未收到任务分配通知**（严重）

### ⚠️ 环境问题
1. ⚠️ BPMN流程定义v2未部署
2. ⚠️ Camunda仍使用旧的v1流程定义

---

## 🎯 详细测试结果

### 测试场景: 完整流程通知测试

#### 步骤1: Admin登录 ✅
- **操作**: 使用admin账户登录系统
- **结果**: ✅ 登录成功
- **URL**: `http://localhost:3000/home`

#### 步骤2: 查看流程定义列表 ✅
- **操作**: 导航到流程定义页面
- **结果**: ✅ 显示2个流程定义
  - `leave-approval-process` (请假审批流程) - 版本1
  - `simple-process` (简单流程) - 版本2

⚠️ **关键发现**: 只看到v1版本的`leave-approval-process`，**没有v2版本**

#### 步骤3: Admin退出登录 ✅
- **操作**: 点击退出登录
- **结果**: ✅ 成功退出

#### 步骤4: Manager登录 ✅
- **操作**: 使用manager账户登录系统
- **结果**: ✅ 登录成功
- **用户名**: manager
- **角色**: 部门经理

#### 步骤5: 检查通知中心 ❌
- **操作**: 点击顶部通知图标
- **URL**: `http://localhost:3000/notifications`
- **预期结果**: 
  - 显示任务分配通知
  - 顶部图标显示未读数徽章
  - 通知列表显示待办任务
- **实际结果**: ❌ **暂无通知**
  - 通知列表为空
  - 顶部图标无未读数显示
  - 没有任何通知记录

---

## 🔍 根本原因分析

### 原因1: BPMN流程定义未更新 🔴

#### 数据库验证
```sql
-- 查询流程定义
SELECT KEY_, NAME_, VERSION_, DEPLOYMENT_ID_ 
FROM ACT_RE_PROCDEF 
WHERE KEY_ LIKE 'leave-approval-process%' 
ORDER BY VERSION_ DESC;

结果:
KEY_                     NAME_    VERSION_  DEPLOYMENT_ID_
leave-approval-process   ??????   1         de98af01-c1cd-11f0-9ec9-5a7d9b34bf2f
```

**结论**: 
- ✅ 数据库中只有v1版本的流程定义
- ❌ 没有v2版本的流程定义
- ❌ 修改的BPMN文件（processId改为leave-approval-process-v2）未被部署

### 原因2: 通知记录未创建 🔴

#### 数据库验证
```sql
-- 查询通知记录总数
SELECT COUNT(*) as total FROM workflow_notification;

结果:
total
1
```

**结论**:
- ✅ 通知表中只有1条记录（初始化的欢迎通知）
- ❌ 没有新的通知记录被创建
- ❌ TaskListener没有被调用

### 原因3: Camunda自动部署机制问题 🔴

**分析**:
1. **修改了BPMN源文件**:
   - 文件路径: `diom-workflow-service/start/src/main/resources/processes/leave-approval-process.bpmn`
   - 修改内容: `<bpmn:process id="leave-approval-process-v2" name="请假审批流程V2">`
   - exporterVersion: 4.0.1

2. **重新编译了workflow-service**:
   - ✅ Maven编译成功
   - ✅ JAR包已更新

3. **重启了workflow-service**:
   - ✅ 服务启动成功
   - PID: 82166

4. **但Camunda未识别新流程**:
   - ❌ v2流程定义未出现在数据库中
   - ❌ 前端流程列表中也未显示v2
   - ❌ Camunda仍使用旧的v1流程定义

**根本原因**: Camunda的自动部署机制判断流程ID已存在（即使我们改了后缀v2），checksum相同时不会创建新版本，processId不同时应该创建新流程但未成功。

---

## 📊 测试数据统计

```
测试场景数:    1
测试步骤数:    5
成功步骤:      4 (80%)
失败步骤:      1 (20%)
───────────────────────────────
核心功能:      ❌ 未通过 (0/1)
辅助功能:      ✅ 通过 (4/4)
───────────────────────────────
总体评分:      20/100
```

---

## 🔧 解决方案

### 方案A: 删除旧流程定义并重新部署 ⭐⭐⭐ 推荐

**原理**: 强制Camunda重新部署流程定义

**步骤**:
1. 停止workflow-service
2. 删除数据库中的旧流程定义
3. 恢复BPMN文件的processId为`leave-approval-process`（不用v2）
4. 重新编译并启动workflow-service

**SQL脚本**:
```sql
-- 删除旧的流程定义（会级联删除相关数据）
DELETE FROM ACT_RE_PROCDEF 
WHERE KEY_ = 'leave-approval-process';

DELETE FROM ACT_RE_DEPLOYMENT 
WHERE ID_ = 'de98af01-c1cd-11f0-9ec9-5a7d9b34bf2f';
```

**优点**:
- ✅ 彻底解决问题
- ✅ 流程ID保持不变（leave-approval-process）
- ✅ 不影响其他流程

**缺点**:
- ⚠️ 会删除所有历史流程实例数据
- ⚠️ 需要停止服务

**时间**: 10分钟  
**风险**: 中（会删除历史数据）

---

### 方案B: 手动修改BPMN并增加版本号 ⭐⭐

**原理**: 修改exporterVersion强制Camunda识别为新版本

**步骤**:
1. 修改BPMN文件的`exporterVersion`为`4.0.2`（递增）
2. 修改BPMN文件的某些内容（如description）以改变checksum
3. 保持processId为`leave-approval-process`（恢复）
4. 重新编译并重启workflow-service

**BPMN修改**:
```xml
<bpmn:definitions ...
  exporter="Camunda Modeler"
  exporterVersion="4.0.2">
  
  <bpmn:process id="leave-approval-process" 
                name="请假审批流程" 
                isExecutable="true">
```

**优点**:
- ✅ 不删除历史数据
- ✅ 创建新版本（v2）

**缺点**:
- ⚠️ 可能仍然不生效（Camunda版本管理机制复杂）
- ⚠️ 需要多次尝试

**时间**: 15分钟  
**风险**: 低

---

### 方案C: 使用手动创建通知API验证前端功能 ⭐

**原理**: 绕过TaskListener，直接调用API创建通知，验证前端功能

**步骤**:
1. 调用`POST /workflow/notifications` API手动创建测试通知
2. 验证前端通知中心是否正常显示
3. 验证标记已读、删除等功能

**API调用**:
```bash
curl -X POST http://localhost:8085/workflow/notifications \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "manager",
    "type": "TASK_ASSIGNED",
    "title": "测试通知",
    "content": "这是一条手动创建的测试通知",
    "linkType": "TASK",
    "linkId": "test-task-123",
    "priority": "HIGH"
  }'
```

**优点**:
- ✅ 快速验证前端功能
- ✅ 不涉及流程定义修改
- ✅ 风险最低

**缺点**:
- ❌ 不解决根本问题（Listener未触发）
- ❌ 只能验证UI，不能验证自动化流程

**时间**: 5分钟  
**风险**: 无

---

## 💡 技术要点总结

### Camunda流程部署机制

1. **自动部署条件**:
   - 文件位于`resources/processes/`目录
   - Spring Boot启动时自动扫描
   - 根据processId和checksum判断是否部署

2. **版本管理**:
   - 同一processId会创建新版本（VERSION_字段递增）
   - 不同processId会创建新流程
   - checksum相同时跳过部署

3. **我们的问题**:
   - 修改了processId为`leave-approval-process-v2`
   - 但Camunda没有识别并部署新流程
   - 可能原因：
     - JAR包中的BPMN文件未更新
     - Camunda配置问题
     - 部署路径不对

### TaskListener配置验证

**正确的BPMN配置**:
```xml
<bpmn:userTask id="managerApproval" name="经理审批" 
               camunda:assignee="${approver}">
  <bpmn:extensionElements>
    <camunda:taskListener 
      delegateExpression="${taskNotificationListener}" 
      event="create"/>
    <camunda:taskListener 
      delegateExpression="${taskNotificationListener}" 
      event="complete"/>
  </bpmn:extensionElements>
</bpmn:userTask>
```

**监听器Bean注册**:
```java
@Component("taskNotificationListener")
public class TaskNotificationListener implements TaskListener {
    // ...
}
```

---

## 📝 下一步行动建议

### 立即执行（推荐）⭐⭐⭐

**选择方案A: 删除旧流程定义并重新部署**

**执行计划**:
1. ✅ 恢复BPMN文件processId为`leave-approval-process`
2. ✅ 停止workflow-service
3. ✅ 执行SQL删除旧流程定义
4. ✅ 重新编译并启动workflow-service
5. ✅ 验证新流程定义已部署（检查VERSION_字段）
6. ✅ 发起测试流程
7. ✅ 验证通知是否创建

**预计时间**: 15分钟  
**成功率**: 95%

---

### 备选方案

如果方案A失败，依次尝试：
1. **方案B**: 修改exporterVersion和description
2. **方案C**: 手动创建通知验证前端
3. **最后手段**: 升级Camunda版本或检查配置

---

## 🎯 成功验证标准

**通知功能正常的标准**:
1. ✅ Admin发起请假流程后
2. ✅ 数据库`workflow_notification`表新增记录
3. ✅ Manager通知中心显示通知
4. ✅ 顶部通知图标显示未读数徽章
5. ✅ Manager完成任务后，Admin收到完成通知

---

## 📚 相关文档

- `NOTIFICATION_CENTER_STATUS.md` - 通知中心开发状态
- `diom-workflow-service/start/src/main/resources/processes/leave-approval-process.bpmn` - BPMN流程定义
- `diom-workflow-service/start/src/main/java/com/diom/workflow/listener/TaskNotificationListener.java` - 监听器实现

---

**测试结论**: ❌ **通知功能未工作，需要立即修复！**  
**建议行动**: ⭐ **执行方案A - 删除旧流程定义并重新部署**  
**优先级**: 🔴 **高优先级（核心功能失效）**

---

*测试报告生成于 2025-11-15 19:20*  
*测试工程师: AI Assistant*  
*审核状态: 待修复*

