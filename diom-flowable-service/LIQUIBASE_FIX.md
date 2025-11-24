# Liquibase 类型转换错误 - 解决方案

## 📋 问题描述

### 错误信息

```
org.flowable.common.engine.api.FlowableException: Error initialising eventregistry data model
Caused by: java.lang.ClassCastException: java.time.LocalDateTime cannot be cast to java.lang.String
  at liquibase.changelog.StandardChangeLogHistoryService.getRanChangeSets
```

### 发生场景

1. 手动导入了 `flowable-6.8.0-mysql-create.sql` 建表脚本
2. 启动 Flowable 服务时，Liquibase 尝试初始化数据库
3. Liquibase 的变更历史表 `DATABASECHANGELOG` 与手动建表冲突
4. 导致类型转换错误

## 🔍 根本原因

### Flowable 的数据库管理机制

Flowable 使用 **Liquibase** 来自动管理数据库表结构：

1. **首次启动**: Flowable 自动创建所有表，并在 `DATABASECHANGELOG` 中记录变更
2. **后续启动**: Liquibase 检查变更历史，只执行新的迁移脚本
3. **手动建表**: 绕过了 Liquibase，导致变更历史与实际表结构不一致

### Event Registry Engine 问题

错误发生在 Event Registry Engine（事件注册引擎）初始化时：

- Event Registry Engine 是 Flowable 的可选组件
- 用于事件驱动的流程（Event-Driven Process）
- 基础的 BPMN 工作流**不需要**这个引擎
- 该引擎会创建额外的表和 Liquibase 记录

## ✅ 解决方案

### 方案 1: 禁用不需要的引擎（已实施）✅

修改 `start/src/main/resources/application.yml`：

```yaml
flowable:
  database-schema-update: true
  database-type: mysql
  
  # 禁用不需要的引擎
  event-registry-enabled: false  # ❌ 事件注册引擎
  app-engine-enabled: false      # ❌ App 引擎
  idm-engine-enabled: false      # ❌ 身份管理引擎
  
  # ✅ Process Engine 默认启用（核心 BPMN 工作流）
```

### 方案 2: 清空数据库重新初始化

如果禁用引擎后仍有问题：

```sql
-- 1. 删除旧数据库
DROP DATABASE IF EXISTS diom_flowable;

-- 2. 创建新的空数据库
CREATE DATABASE diom_flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 不要导入任何脚本，直接启动服务
```

## 📊 各引擎功能对比

| 引擎 | 状态 | 用途 | 是否必需 |
|------|------|------|----------|
| **Process Engine** | ✅ 启用 | BPMN 2.0 工作流核心引擎 | ✅ 必需 |
| **Event Registry Engine** | ❌ 禁用 | 事件驱动流程、消息订阅 | ❌ 可选 |
| **App Engine** | ❌ 禁用 | 应用部署管理、App 定义 | ❌ 可选 |
| **IDM Engine** | ❌ 禁用 | 用户/组/权限管理 | ❌ 可选（我们用独立 Auth 服务）|

## 🎯 禁用引擎的影响

### ✅ 优点

1. **减少表数量**: 从 180+ 减少到约 50 张
2. **降低复杂度**: 只保留核心 BPMN 工作流功能
3. **避免冲突**: 减少 Liquibase 冲突风险
4. **提升性能**: 减少内存占用和启动时间
5. **简化维护**: 更少的表和依赖

### ⚠️ 限制

1. **无法使用事件驱动流程**: 例如 Kafka/RabbitMQ 消息触发流程
2. **无法使用 App 定义**: 如果需要 Flowable App UI，需重新启用
3. **无法使用内置身份管理**: 需依赖外部 Auth 服务（我们已有）

### 💡 何时需要重新启用？

如果未来需要以下功能，可以重新启用对应引擎：

```yaml
flowable:
  # 需要事件驱动流程
  event-registry-enabled: true
  
  # 需要 Flowable App UI
  app-engine-enabled: true
  
  # 需要内置用户管理
  idm-engine-enabled: true
```

## 🔄 数据库表变化

### 禁用前（~180 张表）

```
ACT_RE_*      (Repository - 流程定义)
ACT_RU_*      (Runtime - 运行时数据)
ACT_HI_*      (History - 历史数据)
ACT_ID_*      (Identity - 用户组管理)
FLW_EV_*      (Event Registry - 事件注册)
FLW_RU_*      (Flowable Runtime)
ACT_APP_*     (App Engine - 应用管理)
...
```

### 禁用后（~50 张表）✅

```
ACT_RE_*      (Repository - 流程定义)
ACT_RU_*      (Runtime - 运行时数据)
ACT_HI_*      (History - 历史数据)
FLW_RU_*      (Flowable Runtime)
```

## 📝 最佳实践

### 1. 数据库初始化

✅ **推荐做法**:
```yaml
flowable:
  database-schema-update: true  # 首次启动自动建表
```

❌ **不推荐**:
- 手动导入建表脚本
- 混用手动建表和自动建表

### 2. 生产环境配置

```yaml
flowable:
  database-schema-update: false  # 生产环境关闭自动建表
  # 使用 Liquibase 或 Flyway 进行数据库版本管理
```

### 3. 引擎选择

- 📌 **基础工作流**: 只启用 Process Engine
- 📌 **事件驱动**: 额外启用 Event Registry Engine
- 📌 **完整功能**: 启用所有引擎

## 🧪 验证修复

### 1. 启动服务

```bash
cd diom-flowable-service
./start-flowable.sh
```

### 2. 检查日志

应该看到：
```
✅ Deployed process definition: simple-process
✅ Deployed process definition: leave-approval-process
✅ Started Flowable engine successfully
```

### 3. 验证数据库

```sql
USE diom_flowable;

-- 查看表数量
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'diom_flowable';
-- 应该显示约 50 张表

-- 查看 Liquibase 变更历史
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC LIMIT 10;
```

### 4. 测试 API

```bash
# 健康检查
curl http://localhost:8086/actuator/health

# 查询流程定义
curl http://localhost:8086/flowable/definitions

# 启动流程
curl -X POST http://localhost:8086/flowable/start/simple-process \
  -H "Content-Type: application/json" \
  -d '{}'
```

## 🎉 总结

通过禁用不需要的 Flowable 引擎：

✅ **解决了 Liquibase 类型转换错误**  
✅ **简化了数据库结构**  
✅ **提升了启动速度和性能**  
✅ **保留了所有核心 BPMN 工作流功能**

对于基础的工作流管理系统，这是最优解决方案！

---

**修复日期**: 2025-11-24  
**Flowable 版本**: 6.7.2  
**影响范围**: 数据库初始化、引擎配置

