# Flowable 服务快速启动指南

## 📋 前置条件

1. **MySQL 数据库** (推荐 5.7+)
2. **Nacos 服务** (1.4.0+)
3. **Java 8**
4. **Maven 3.6+**

## 🔧 初始化步骤

### 1. 创建数据库

```bash
# 连接到 MySQL
mysql -uroot -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS diom_flowable 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

# 退出
exit;
```

### 2. ⚠️ 不要手动导入建表脚本

**重要**: Flowable 会在首次启动时**自动创建所有需要的表**，无需手动导入 SQL 脚本。

保持数据库为空，直接进入下一步。

### 3. 确保 Nacos 运行

```bash
# 检查 Nacos 是否运行
curl http://localhost:8848/nacos/

# 如果未运行，启动 Nacos
cd $NACOS_HOME/bin
sh startup.sh -m standalone
```

## 🚀 启动服务

### 方式1：使用启动脚本（推荐）

```bash
cd diom-flowable-service

# 如果还未打包，先打包
mvn clean package -DskipTests

# 启动服务
./start-flowable.sh
```

### 方式2：直接运行 JAR

```bash
cd diom-flowable-service/start

java -jar target/start-1.0.0-SNAPSHOT.jar \
  --server.port=8086 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/diom_flowable?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai \
  --spring.datasource.username=root \
  --spring.datasource.password=YOUR_PASSWORD
```

### 方式3：使用 Maven（开发环境）

```bash
cd diom-flowable-service/start
mvn spring-boot:run
```

## ✅ 验证服务

### 1. 健康检查

```bash
curl http://localhost:8086/actuator/health
```

期望响应：
```json
{
  "status": "UP"
}
```

### 2. 查询流程定义

```bash
curl http://localhost:8086/flowable/definitions
```

应该看到两个流程定义：
- `simple-process` - 简单流程
- `leave-approval-process` - 请假审批流程

### 3. 启动一个流程实例

```bash
# 启动简单流程
curl -X POST http://localhost:8086/flowable/start/simple-process \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 4. 查询任务列表

```bash
# 查询 admin 用户的待办任务
curl "http://localhost:8086/flowable/tasks?assignee=admin"
```

### 5. 完成任务

```bash
# 假设任务ID为 12345
curl -X POST http://localhost:8086/flowable/tasks/12345/complete \
  -H "Content-Type: application/json" \
  -d '{"outcome":"approved"}'
```

## 🧪 运行完整测试脚本

```bash
cd diom-flowable-service
chmod +x test-flowable.sh
./test-flowable.sh
```

该脚本会自动：
1. 检查服务健康状态
2. 获取流程定义列表
3. 启动一个流程实例
4. 查询任务
5. 完成任务
6. 查询历史记录

## 🔍 日志查看

### 实时查看日志

```bash
# 如果使用启动脚本
tail -f start/workflow.log

# 如果使用 Docker 部署
docker logs -f diom-flowable-service
```

### 常见日志关键字

- `✅` - 成功信息
- `❌` - 错误信息
- `Deployed process definition` - 流程定义部署成功
- `Started process instance` - 流程实例启动

## 🐛 故障排查

### 问题1：端口冲突

**错误**：`Port 8086 was already in use`

**解决**：
```bash
# 查找占用端口的进程
lsof -i :8086

# 或修改端口
export SERVER_PORT=8087
```

### 问题2：数据库连接失败

**错误**：`Communications link failure`

**检查**：
1. MySQL 是否运行：`mysql -uroot -p`
2. 数据库是否存在：`SHOW DATABASES LIKE 'diom_flowable';`
3. 用户名密码是否正确
4. 防火墙规则

### 问题3：Nacos 连接失败

**错误**：`Unable to register with Nacos`

**检查**：
1. Nacos 是否运行：`curl http://localhost:8848/nacos/`
2. 网络是否可达
3. 配置的 Group 是否正确（应为 `HTTP_GROUP`）

### 问题4：流程定义未部署

**错误**：`Process definition not found`

**检查**：
1. 查看日志中是否有 "Deployed process definition" 记录
2. 检查 BPMN 文件是否在 `src/main/resources/processes/` 目录
3. 验证 BPMN 文件 XML 格式是否正确
4. 确认命名空间声明使用 `flowable:` 而不是 `camunda:`

## 🔗 相关端口

| 服务 | 端口 | 用途 |
|------|------|------|
| **Flowable服务** | 8086 | 新的 Flowable 工作流服务 |
| **Camunda服务** | 8085 | 原有的 Camunda 工作流服务（并行） |
| **Gateway** | 8080 | API 网关 |
| **Auth服务** | 8081 | 认证服务 |
| **Nacos** | 8848 | 服务注册与配置中心 |
| **MySQL** | 3306 | 数据库 |

## 📚 下一步

1. **配置 Gateway 路由**：在 `diom-gateway` 中添加 Flowable 服务路由
2. **前端集成**：更新前端 API 调用指向新服务
3. **数据迁移**：如果需要，将 Camunda 的流程数据迁移到 Flowable
4. **性能测试**：压测验证服务性能
5. **监控告警**：配置 Prometheus + Grafana 监控

## 📖 更多文档

- [API 文档](./API.md)
- [迁移完成报告](./MIGRATION_COMPLETE.md)
- [依赖问题解决方案](./DEPENDENCY_FIX.md)
- [完整 README](./README.md)

