# diom-flowable-service

Flowable 6.8.0 工作流服务，基于COLA架构实现，与 Camunda 服务并行部署。

## 📋 服务信息

| 项目 | 配置 |
|------|------|
| **端口** | 8086 |
| **Flowable版本** | 6.7.2 |
| **数据库** | MySQL 8.0 (diom_flowable) |
| **架构** | COLA (Adapter-App-Domain-Infrastructure-Client-Start) |
| **Spring Boot** | 2.4.11 |

---

## 🏗️ 项目结构

```
diom-flowable-service/
├── flowable-adapter/        # 适配器层：REST Controller
├── flowable-app/            # 应用层：业务编排
├── flowable-domain/         # 领域层：领域模型
├── flowable-infrastructure/ # 基础设施层：Dubbo、数据访问
├── flowable-client/         # 客户端接口定义
├── start/                   # 启动模块
│   ├── src/main/resources/
│   │   ├── application.yml  # 主配置
│   │   ├── bootstrap.yml    # Nacos配置
│   │   ├── processes/       # BPMN流程定义
│   │   └── sql/             # 业务表SQL脚本
│   └── pom.xml
├── flowable-6.8.0-mysql-create.sql  # 数据库建表脚本
├── test-flowable.sh         # 快速测试脚本
└── README.md
```

---

## 🚀 快速启动

### 1. 数据库准备

**方式1：自动建表（推荐开发环境）**
```bash
# 数据库已在初始化时创建
# 首次启动时 Flowable 会自动创建所有表（约180+张）
# application.yml 中设置：
#   flowable.database-schema-update: true
```

**方式2：手动建表（推荐生产环境）**
```bash
# 执行完整建表脚本（需要从 Flowable 官方下载）
docker exec -i meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_flowable \
  < flowable-6.8.0-mysql-all-create.sql
```

### 2. 启动服务

```bash
cd start
mvn spring-boot:run

# 或后台启动
nohup mvn spring-boot:run > flowable.log 2>&1 &
```

### 3. 验证服务

```bash
# 方式1：运行测试脚本
./test-flowable.sh

# 方式2：手动验证
curl http://localhost:8086/actuator/health
curl http://localhost:8086/workflow/definitions
```

---

## 🔧 关键配置

### application.yml

```yaml
server:
  port: 8086  # 新端口，与 Camunda 服务并行

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diom_flowable
    username: root
    password: 1qaz2wsx

flowable:
  database-schema-update: true  # 首次启动自动建表
  database-type: mysql
  async-executor-activate: true
  history-level: full
  check-process-definitions: true
  process-definition-location-prefix: classpath*:/processes/
```

### bootstrap.yml

```yaml
spring:
  application:
    name: diom-flowable-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        group: HTTP_GROUP  # 与 Camunda 服务相同的分组
```

---

## 📡 API接口

### 流程管理

```bash
# 获取所有流程定义
GET /workflow/definitions

# 启动流程实例
POST /workflow/start/{processDefinitionKey}

# 查询流程实例
GET /workflow/instance/{processInstanceId}
```

### 任务管理

```bash
# 查询用户任务
GET /workflow/tasks?assignee={username}

# 完成任务
POST /workflow/tasks/{taskId}/complete
```

### 健康检查

```bash
# 服务健康
GET /actuator/health

# Flowable引擎信息
GET /actuator/flowable
```

### 流程设计器

```bash
# 流程设计列表
GET /workflow/api/process-design/list

# 保存流程设计
POST /workflow/api/process-design

# 发布流程
POST /workflow/api/process-design/{id}/publish
```

---

## 📝 BPMN流程开发

### 1. 创建流程文件

在`start/src/main/resources/processes/`目录下添加`.bpmn`文件：

```
processes/
├── leave-approval-process.bpmn    # 请假审批流程
└── simple-process.bpmn            # 简单流程示例
```

### 2. 实现Service Task

```java
@Component("myServiceTask")
public class MyServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // 业务逻辑
        String businessKey = execution.getBusinessKey();
        execution.setVariable("result", "success");
    }
}
```

在BPMN中引用：`${myServiceTask}` 或 `flowable:delegateExpression="${myServiceTask}"`

### 3. 重启服务自动部署

修改BPMN文件后，重启服务即可自动部署。

---

## 🆚 Camunda vs Flowable

### API 兼容性

| 功能 | Camunda API | Flowable API | 兼容性 |
|------|------------|--------------|--------|
| 流程实例 | RuntimeService | RuntimeService | ✅ 完全兼容 |
| 任务管理 | TaskService | TaskService | ✅ 完全兼容 |
| 流程定义 | RepositoryService | RepositoryService | ✅ 完全兼容 |
| 历史查询 | HistoryService | HistoricService | ⚠️ 名称略有不同 |
| 执行监听器 | ExecutionListener | ExecutionListener | ✅ 完全兼容 |
| 任务监听器 | TaskListener | TaskListener | ✅ 完全兼容 |
| Service Task | JavaDelegate | JavaDelegate | ✅ 完全兼容 |

### 命名空间

```xml
<!-- Camunda -->
<bpmn:definitions xmlns:camunda="http://camunda.org/schema/1.0/bpmn">
  <camunda:executionListener ... />
</bpmn:definitions>

<!-- Flowable -->
<bpmn:definitions xmlns:flowable="http://flowable.org/bpmn">
  <flowable:executionListener ... />
</bpmn:definitions>
```

**注意**：Flowable 完全兼容 Camunda 命名空间，无需修改即可运行。

---

## 🧪 测试

### 快速测试脚本

```bash
./test-flowable.sh
```

### 完整测试流程

```bash
# 1. 健康检查
curl http://localhost:8086/actuator/health

# 2. 获取流程定义
curl http://localhost:8086/workflow/definitions

# 3. 启动流程
curl -X POST http://localhost:8086/workflow/start/leave-approval-process \
  -H "Content-Type: application/json" \
  -d '{"applicant":"test","reason":"测试","days":3}'

# 4. 查询任务
curl "http://localhost:8086/workflow/tasks?assignee=admin"

# 5. 完成任务
curl -X POST http://localhost:8086/workflow/tasks/{taskId}/complete \
  -H "Content-Type: application/json" \
  -d '{"approved":true}'
```

---

## ⚠️ 重要提醒

### 1. 数据库配置

**首次启动**：设置 `database-schema-update: true`，Flowable 会自动创建所有表。

**生产环境**：建表完成后，务必修改为 `database-schema-update: false`，避免自动修改表结构。

### 2. 与 Camunda 服务并行

- **Camunda 服务**：端口 8085，数据库 diom_workflow
- **Flowable 服务**：端口 8086，数据库 diom_flowable
- 两个服务完全独立，互不影响
- 前端通过路径区分：`/workflow/**` → Camunda，`/flowable/**` → Flowable

### 3. Gateway 路由

```yaml
# Gateway 配置
routes:
  - id: workflow-service
    uri: lb://diom-workflow-service  # Camunda
    predicates:
      - Path=/workflow/**
  
  - id: flowable-service
    uri: lb://diom-flowable-service  # Flowable
    predicates:
      - Path=/flowable/**
```

### 4. 前端兼容性

- bpmn.js 完全兼容 Flowable
- API 接口保持不变
- 无需修改前端代码

---

## 🔗 相关资源

### 官方文档
- [Flowable 6.8.0 Documentation](https://www.flowable.com/open-source/docs)
- [Flowable REST API Reference](https://www.flowable.com/open-source/docs/bpmn/ch15-REST)
- [BPMN 2.0 Specification](https://www.omg.org/spec/BPMN/2.0/)

### 项目文档
- `flowable-6.8.0-mysql-create.sql` - 数据库建表脚本
- `test-flowable.sh` - 快速测试脚本
- `API.md` - 详细 API 文档

---

## 📊 数据库表说明

Flowable 引擎约 180+ 张表，分为以下类别：

| 类别 | 前缀 | 说明 |
|------|------|------|
| 流程仓库 | ACT_RE_* | 流程定义、部署 |
| 运行时 | ACT_RU_* | 流程实例、任务、变量 |
| 历史 | ACT_HI_* | 历史记录、审计 |
| 身份 | ACT_ID_* | 用户、组、权限 |
| 通用 | ACT_GE_* | 二进制数据、属性 |
| Flowable | FLW_* | Flowable 特有表 |

**业务表**（自定义）：
- `workflow_process_design` - 流程设计表
- `workflow_process_design_history` - 流程设计历史
- `workflow_notification` - 通知表
- `workflow_template` - 模板表

---

## 🛠️ 故障排除

### 服务无法启动

1. 检查MySQL是否运行：`docker ps | grep mysql`
2. 检查数据库是否存在：
   ```bash
   docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx \
     -e "SHOW DATABASES LIKE 'diom_flowable';"
   ```
3. 查看日志：`tail -f start/flowable.log`

### 流程无法部署

1. 验证BPMN文件语法
2. 确认文件在`resources/processes/`目录下
3. 查看启动日志中的ERROR信息
4. 检查命名空间是否正确

### 表未自动创建

1. 确认配置：`database-schema-update: true`
2. 检查数据库连接配置
3. 查看启动日志，确认Flowable引擎初始化

---

## 📞 技术栈

- **Spring Boot**: 2.4.11
- **Flowable**: 6.8.0
- **数据库**: MySQL 8.0
- **注册中心**: Nacos
- **架构**: COLA
- **RPC**: Dubbo 3.0.15（预留）

---

## 🎯 迁移完成清单

- ✅ 项目结构创建（COLA 架构）
- ✅ Maven 依赖配置（Flowable 6.8.0）
- ✅ 数据库初始化（diom_flowable）
- ✅ 配置文件迁移（application.yml、bootstrap.yml）
- ✅ Entity、DTO、VO 类迁移
- ✅ Mapper 接口和 XML 迁移
- ✅ Service 层 API 替换（Camunda → Flowable）
- ✅ 监听器和 Service Task 迁移
- ✅ Controller 层迁移（API 接口保持不变）
- ✅ BPMN 流程文件复制
- ✅ 业务表创建
- ✅ Gateway 路由配置
- ✅ 测试脚本编写
- ✅ 文档编写

---

**最后更新**: 2025-11-23  
**状态**: ✅ 迁移完成，准备测试

