# Flowable 依赖问题解决方案

## 问题描述

在初始迁移时使用 Flowable 6.8.0，遇到了依赖解析错误：
```
Unresolved dependency: 'org.flowable:flowable-app-engine:jar:6.8.0'
```

## 根本原因

1. **版本兼容性问题**: Flowable 6.8.0 与 Spring Boot 2.4.11 的兼容性存在问题
2. **MyBatis 冲突**: MyBatis Plus 与 Flowable 内置的 MyBatis 版本冲突
3. **API 差异**: Camunda 和 Flowable 的 API 存在细微差异

## 解决方案

### 1. 降级 Flowable 版本

将 Flowable 版本从 6.8.0 降级到 6.7.2，这是与 Spring Boot 2.4.11 完全兼容的稳定版本。

**修改位置**: `diom-flowable-service/pom.xml`

```xml
<properties>
    <flowable.version>6.7.2</flowable.version>  <!-- 原 6.8.0 -->
</properties>
```

### 2. 保留 MyBatis Plus 依赖

初始尝试排除 MyBatis 依赖导致 `@MapperScan` 注解找不到。最终保留完整的 MyBatis Plus 依赖。

**修改位置**: `start/pom.xml`

```xml
<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.4.3</version>
</dependency>
```

### 3. API 适配修复

#### 3.1 JavaDelegate 和监听器

Flowable 的 `JavaDelegate.execute()` 和 `ExecutionListener.notify()` 方法**不抛出** checked exception。

**错误示例** (Camunda 风格):
```java
@Override
public void execute(DelegateExecution execution) throws Exception {
    // 实现
}
```

**正确示例** (Flowable):
```java
@Override
public void execute(DelegateExecution execution) {
    // 实现
}
```

**受影响的文件**:
- `NotifyApplicantService.java`
- `NotifyManagerService.java`
- `HrRecordService.java`
- `ProcessStartListener.java`
- `ProcessEndListener.java`

#### 3.2 Task 类导入路径

**Camunda**:
```java
import org.camunda.bpm.engine.task.Task;
```

**Flowable**:
```java
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
```

#### 3.3 DelegateExecution API 变更

| Camunda API | Flowable API |
|-------------|--------------|
| `getProcessBusinessKey()` | `getProcessInstanceBusinessKey()` |
| `getCurrentActivityName()` | `getCurrentActivityId()` |

#### 3.4 HistoricVariableInstance API

**Camunda**:
```java
variable.getName()
```

**Flowable**:
```java
variable.getVariableName()
```

#### 3.5 BPMN 模型 API 重写

Camunda 使用自己的 BPMN 模型库，Flowable 使用标准 BPMN 2.0 模型。

**Camunda 代码**:
```java
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
```

**Flowable 代码**:
```java
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import javax.xml.stream.XMLInputFactory;

BpmnXMLConverter converter = new BpmnXMLConverter();
XMLInputFactory xif = XMLInputFactory.newInstance();
XMLStreamReader xtr = xif.createXMLStreamReader(new StringReader(bpmnXml));
BpmnModel bpmnModel = converter.convertToBpmnModel(xtr);
```

#### 3.6 UserTask API

**Camunda**:
```java
userTask.getCamundaAssignee()
```

**Flowable**:
```java
userTask.getAssignee()
```

#### 3.7 DeploymentBuilder API

**Camunda**:
```java
repositoryService.createDeployment()
    .enableDuplicateFiltering(false)  // 接受 boolean 参数
    .deploy();
```

**Flowable**:
```java
repositoryService.createDeployment()
    .enableDuplicateFiltering()  // 无参数
    .deploy();
```

#### 3.8 类名冲突解决

`Process` 类与 `java.lang.Process` 冲突，需要使用完整类名。

**修复前**:
```java
import org.flowable.bpmn.model.*;

Process process = bpmnModel.getMainProcess();  // 编译错误：引用不明确
```

**修复后**:
```java
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.bpmn.model.EndEvent;

org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();  // 显式指定完整类名
```

## 验证

编译成功：
```bash
cd diom-flowable-service
mvn clean compile -DskipTests
# [INFO] BUILD SUCCESS
```

## 经验总结

1. **优先选择稳定版本**: 新版本可能存在未发现的兼容性问题，生产环境应使用经过验证的稳定版本
2. **API 差异需要逐一测试**: 工作流引擎的 API 差异较多，需要仔细对照文档进行调整
3. **避免依赖冲突**: MyBatis 等常见依赖容易冲突，需要明确版本管理策略
4. **使用完整类名避免冲突**: 当类名与 JDK 类冲突时，使用完整包名可以避免编译错误

## 后续建议

1. **单元测试**: 为所有修改过的 API 调用添加单元测试
2. **集成测试**: 测试完整的流程定义部署和执行流程
3. **性能测试**: 验证 Flowable 6.7.2 与 Camunda 的性能差异
4. **监控告警**: 添加对过时 API 的监控（当前有 deprecation 警告）

## 4. BPMN 文件命名空间问题

### 问题描述

启动应用时遇到 XML 解析错误：
```
org.flowable.bpmn.exceptions.XMLException: javax.xml.stream.XMLStreamException: 
ParseError at [row,col]:[23,85]
Message: AttributePrefixUnbound?bpmn:userTask&flowable:assignee&flowable
```

### 根本原因

BPMN 文件从 Camunda 复制过来后：
1. XML 文件头声明的是 `camunda` 命名空间
2. 但文件内容使用的是 `flowable:` 属性
3. 两者不匹配导致 XML 解析失败

### 解决方案

修改所有 BPMN 文件的命名空间声明：

**修改前**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  ...>
  <bpmn:userTask id="task1" name="用户任务" flowable:assignee="admin"/>
</bpmn:definitions>
```

**修改后**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:flowable="http://flowable.org/bpmn"
                  ...>
  <bpmn:userTask id="task1" name="用户任务" flowable:assignee="admin"/>
</bpmn:definitions>
```

**关键点**:
- Flowable 命名空间 URI: `http://flowable.org/bpmn`
- 所有 `flowable:` 属性都需要在文件头声明对应的命名空间
- 建议同时更新 `exporter` 和 `exporterVersion` 元数据

**受影响的文件**:
- `processes/simple-process.bpmn`
- `processes/leave-approval-process.bpmn`

## 5. Liquibase 类型转换错误

### 问题描述

启动应用时遇到 Liquibase 类型转换错误：
```
org.flowable.common.engine.api.FlowableException: Error initialising eventregistry data model
Caused by: java.lang.ClassCastException: java.time.LocalDateTime cannot be cast to java.lang.String
```

### 根本原因

1. Flowable 使用 Liquibase 自动管理数据库表结构
2. 手动导入建表脚本后，Liquibase 的变更历史表 `DATABASECHANGELOG` 存在数据类型不匹配
3. Event Registry、App Engine、IDM Engine 等额外引擎会创建额外的表和 Liquibase 记录
4. 对于基本的 BPMN 工作流，这些额外引擎并非必需

### 解决方案

**方案1: 重建空数据库（推荐用于首次部署）**

```sql
-- 删除旧数据库
DROP DATABASE IF EXISTS diom_flowable;

-- 创建新的空数据库
CREATE DATABASE diom_flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后**不要手动导入建表脚本**，直接启动服务让 Flowable 自动建表。

**方案2: 禁用不需要的引擎（推荐用于简化部署）**

修改 `application.yml`，禁用三个不必要的引擎：

```yaml
flowable:
  database-schema-update: true
  
  # 禁用不需要的引擎
  event-registry-enabled: false  # 事件注册引擎
  app-engine-enabled: false      # App 引擎
  idm-engine-enabled: false      # 身份管理引擎
```

**说明**：
- **Process Engine**: ✅ 保留（核心 BPMN 工作流引擎，必需）
- **Event Registry Engine**: ❌ 禁用（事件驱动架构，基础工作流不需要）
- **App Engine**: ❌ 禁用（应用部署管理，基础工作流不需要）
- **IDM Engine**: ❌ 禁用（身份管理，我们使用独立的 Auth 服务）

### 影响评估

禁用这三个引擎后：
- ✅ 减少数据库表数量（从 180+ 减少到约 50）
- ✅ 降低 Liquibase 冲突风险
- ✅ 减少内存占用和启动时间
- ✅ 基本的 BPMN 流程功能完全不受影响
- ⚠️ 无法使用事件驱动的流程（如果需要可以重新启用）

### 验证

启动服务后，检查数据库表：

```sql
-- 查看所有 Flowable 表
USE diom_flowable;
SHOW TABLES;

-- 应该看到以下核心表：
-- ACT_RE_* (Repository - 流程定义)
-- ACT_RU_* (Runtime - 运行时数据)
-- ACT_HI_* (History - 历史数据)
-- FLW_RU_* (Flowable Runtime)
```

## 版本信息

- **Spring Boot**: 2.4.11
- **Flowable**: 6.7.2
- **MyBatis Plus**: 3.4.3
- **MySQL Connector**: 8.0.33
- **Java**: 1.8
- **启用的引擎**: Process Engine (核心)

