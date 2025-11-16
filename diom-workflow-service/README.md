# diom-workflow-service

Camunda 7.16工作流服务，基于COLA架构实现。

## 📋 服务信息

| 项目 | 配置 |
|------|------|
| **端口** | 8083 |
| **Camunda版本** | 7.16.0 |
| **数据库** | MySQL 8.0 |
| **架构** | COLA (Adapter-App-Domain-Infrastructure-Client-Start) |

---

## 🏗️ 项目结构

```
diom-workflow-service/
├── workflow-adapter/        # 适配器层：REST Controller
├── workflow-app/            # 应用层：业务编排
├── workflow-domain/         # 领域层：领域模型
├── workflow-infrastructure/ # 基础设施层：Dubbo、数据访问
├── workflow-client/         # 客户端接口定义
├── start/                   # 启动模块
│   ├── src/main/resources/
│   │   ├── application.yml  # 主配置
│   │   ├── bootstrap.yml    # Nacos配置
│   │   └── processes/       # BPMN流程定义
│   └── pom.xml
├── camunda-716-mysql-create.sql  # 数据库建表脚本（重要）
├── test-camunda.sh          # 快速测试脚本
└── README.md
```

---

## 🚀 快速启动

### 1. 数据库准备

**⚠️ 重要**: 本服务使用**手动建表**方式，不会自动创建表。

```bash
# 确保MySQL 8.0已启动
docker ps | grep mysql

# 执行建表脚本（首次部署）
docker exec -i meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow \
  < camunda-716-mysql-create.sql

# 验证表创建（应该有44个表）
docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow \
  -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'diom_workflow';"
```

### 2. 启动服务

```bash
cd start
mvn spring-boot:run

# 或后台启动
nohup mvn spring-boot:run > workflow.log 2>&1 &
```

### 3. 验证服务

```bash
# 方式1：运行测试脚本
./test-camunda.sh

# 方式2：手动验证
curl http://localhost:8083/actuator/health
curl http://localhost:8083/workflow/definitions
```

---

## 🔧 关键配置

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diom_workflow
    username: root
    password: 1qaz2wsx

camunda:
  bpm:
    database:
      schema-update: false  # ⚠️ 必须为false，使用手动建表
      type: mysql
    admin-user:
      id: admin
      password: admin
```

### bootstrap.yml

```yaml
spring:
  application:
    name: diom-workflow-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
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

# Camunda引擎信息
GET /engine-rest/engine
```

---

## 📝 BPMN流程开发

### 1. 创建流程文件

在`start/src/main/resources/processes/`目录下添加`.bpmn`文件：

```
processes/
├── simple-process.bpmn       # 简单流程示例
├── approval-process.bpmn     # 审批流程
└── order-process.bpmn        # 订单流程
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

在BPMN中引用：`${myServiceTask}`

### 3. 重启服务自动部署

修改BPMN文件后，重启服务即可自动部署。

---

## 🧪 测试

### 快速测试脚本

```bash
./test-camunda.sh
```

### 完整测试流程

```bash
# 1. 健康检查
curl http://localhost:8083/actuator/health

# 2. 获取流程定义
curl http://localhost:8083/workflow/definitions

# 3. 启动流程
curl -X POST http://localhost:8083/workflow/start/simple-process \
  -H "Content-Type: application/json" \
  -d '{}'

# 4. 查询任务
curl "http://localhost:8083/workflow/tasks?assignee=admin"

# 5. 完成任务
curl -X POST http://localhost:8083/workflow/tasks/{taskId}/complete \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## ⚠️ 重要提醒

### 1. 数据库配置

**必须保持** `schema-update: false`，因为：
- 使用手动建表方式
- Camunda 7.16与MySQL 8.0自动建表有兼容性问题
- 修改此配置会导致启动失败

### 2. 升级Camunda版本

升级时需要手动执行升级脚本：

```bash
# 例如：7.16 → 7.17
docker exec -i meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow \
  < mysql_engine_7.16_to_7.17.sql
```

### 3. 备份数据

定期备份工作流数据：

```bash
docker exec meeting-admin-mysql mysqldump -uroot -p1qaz2wsx diom_workflow \
  > camunda_backup_$(date +%Y%m%d).sql
```

---

## 🔗 相关资源

### 官方文档
- [Camunda 7.16 Documentation](https://docs.camunda.org/manual/7.16/)
- [REST API Reference](https://docs.camunda.org/manual/7.16/reference/rest/)
- [BPMN 2.0 Reference](https://docs.camunda.org/manual/7.16/reference/bpmn20/)

### 项目文档
- `camunda-716-mysql-create.sql` - 完整建表脚本
- `test-camunda.sh` - 快速测试脚本

---

## 📊 数据库表说明

共44个表，分为5类：

| 类别 | 前缀 | 数量 | 说明 |
|------|------|------|------|
| 通用表 | ACT_GE_* | 3 | 二进制数据、属性、日志 |
| 仓库表 | ACT_RE_* | 6 | 流程定义、部署 |
| 运行时表 | ACT_RU_* | 16 | 流程实例、任务、变量 |
| 历史表 | ACT_HI_* | 13 | 历史记录、审计 |
| 身份表 | ACT_ID_* | 6 | 用户、组、权限 |

---

## 🛠️ 故障排除

### 服务无法启动

1. 检查MySQL是否运行：`docker ps | grep mysql`
2. 检查表是否存在：`docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow -e "SHOW TABLES;"`
3. 查看日志：`tail -f start/workflow.log`

### 流程无法部署

1. 验证BPMN文件语法
2. 确认文件在`resources/processes/`目录下
3. 查看启动日志中的ERROR信息

### 任务无法完成

1. 检查任务ID是否正确
2. 确认流程变量是否完整
3. 查询历史表：`SELECT * FROM ACT_HI_TASKINST WHERE ID_ = '{taskId}';`

---

## 📞 技术栈

- **Spring Boot**: 2.4.11
- **Camunda BPM**: 7.16.0
- **数据库**: MySQL 8.0
- **注册中心**: Nacos
- **架构**: COLA
- **RPC**: Dubbo 3.0.15（预留）

---

**最后更新**: 2025-11-15  
**状态**: ✅ 生产就绪
