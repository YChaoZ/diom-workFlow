# Flowable 工作流服务

> 从 Camunda BPM 7.16.0 迁移到 Flowable 6.7.2 的企业级工作流服务

## 🚀 3 步快速启动

```bash
# 1. 创建空数据库
mysql -uroot -p -e "CREATE DATABASE diom_flowable CHARACTER SET utf8mb4;"

# 2. 启动服务
cd diom-flowable-service
./start-flowable.sh

# 3. 测试服务
./test-flowable.sh
```

**详细说明**: 请阅读 [START_HERE.md](./START_HERE.md)

---

## 📖 文档索引

### 🎯 快速开始
- **[START_HERE.md](./START_HERE.md)** ⭐ - 3 步快速启动指南
- **[COMMANDS.md](./COMMANDS.md)** - 常用命令速查表
- **[QUICKSTART.md](./QUICKSTART.md)** - 详细启动指南

### 🔧 故障排查
- **[LIQUIBASE_FIX.md](./LIQUIBASE_FIX.md)** ⭐ - Liquibase 错误解决方案
- **[DEPENDENCY_FIX.md](./DEPENDENCY_FIX.md)** - 依赖问题解决方案

### 📚 项目文档
- **[FINAL_STATUS.md](./FINAL_STATUS.md)** - 项目最终状态报告
- **[MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md)** - 迁移工作总结
- **[MIGRATION_COMPLETE.md](./MIGRATION_COMPLETE.md)** - 详细迁移报告
- **[API.md](./API.md)** - API 接口文档
- **[README.md](./README.md)** - 完整项目说明

---

## ⚡ 核心特性

### ✅ 已实现功能

- 🔄 **BPMN 2.0 流程引擎** - 完整的 BPMN 2.0 标准支持
- 📝 **流程设计** - 可视化流程设计和部署
- 🎯 **任务管理** - 用户任务的创建、查询、完成
- 📊 **历史查询** - 流程实例和任务的历史记录
- 🔍 **流程监控** - 流程实例的实时监控
- 🌐 **服务注册** - 自动注册到 Nacos（HTTP_GROUP）
- 🔐 **权限集成** - 通过 Gateway 与 Auth 服务集成
- 📦 **自动建表** - 首次启动自动创建所有数据库表

### 🎨 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Flowable** | 6.7.2 | BPMN 工作流引擎 |
| **Spring Boot** | 2.4.11 | 应用框架 |
| **MyBatis Plus** | 3.4.3 | ORM 框架 |
| **MySQL** | 5.7+ | 数据库 |
| **Nacos** | 1.4.0+ | 服务注册与配置 |
| **Java** | 1.8 | 开发语言 |

---

## 🏗️ 项目结构

```
diom-flowable-service/
├── flowable-client/          # 客户端接口定义
├── flowable-domain/          # 领域层
├── flowable-app/             # 应用层
├── flowable-infrastructure/  # 基础设施层
├── flowable-adapter/         # 适配器层
└── start/                    # 启动模块 ⭐
    ├── src/main/java/
    │   └── com/diom/flowable/
    │       ├── config/       # 配置类
    │       ├── controller/   # REST 控制器
    │       ├── service/      # 业务服务
    │       ├── entity/       # 实体类
    │       ├── mapper/       # MyBatis Mapper
    │       ├── listener/     # 流程监听器
    │       ├── dto/          # 数据传输对象
    │       └── vo/           # 视图对象
    └── src/main/resources/
        ├── application.yml   # 应用配置
        ├── bootstrap.yml     # Nacos 配置
        └── processes/        # BPMN 流程文件
```

---

## 🌐 API 端点

### 健康检查
```bash
GET http://localhost:8086/actuator/health
```

### 流程定义管理
```bash
# 获取所有流程定义
GET /flowable/definitions

# 获取流程定义 XML
GET /flowable/definitions/{key}/xml
```

### 流程实例管理
```bash
# 启动流程实例
POST /flowable/start/{processDefinitionKey}

# 查询流程实例
GET /flowable/instances
```

### 任务管理
```bash
# 查询用户任务
GET /flowable/tasks?assignee={userId}

# 完成任务
POST /flowable/tasks/{taskId}/complete
```

**完整 API 文档**: 见 [API.md](./API.md)

---

## 🔌 与其他服务集成

### 服务注册（Nacos）
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        group: HTTP_GROUP  # HTTP 服务专用组
```

### API 网关路由
```yaml
# 在 diom-gateway 中配置
spring:
  cloud:
    gateway:
      routes:
        - id: flowable-service
          uri: lb://diom-flowable-service
          predicates:
            - Path=/flowable/**
```

### 权限认证
- 通过 Gateway 的 JWT 过滤器进行认证
- Gateway 在请求头中注入 `X-User-Id` 和 `X-Username`
- Flowable 服务直接读取请求头获取用户信息

---

## 📊 数据库

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diom_flowable
    username: root
    password: yourpassword
```

### 自动建表
```yaml
flowable:
  database-schema-update: true  # 自动创建/更新表
```

### 核心表（约 50 张）

| 前缀 | 说明 | 示例 |
|------|------|------|
| `ACT_RE_*` | Repository（流程定义） | `ACT_RE_PROCDEF` |
| `ACT_RU_*` | Runtime（运行时数据） | `ACT_RU_TASK` |
| `ACT_HI_*` | History（历史数据） | `ACT_HI_PROCINST` |
| `FLW_RU_*` | Flowable Runtime | `FLW_RU_BATCH` |

---

## 🐛 常见问题

### Q1: 启动时报 Liquibase 错误

**错误**: `java.lang.ClassCastException: java.time.LocalDateTime cannot be cast to java.lang.String`

**解决**: 见 [LIQUIBASE_FIX.md](./LIQUIBASE_FIX.md)

### Q2: 无法连接 MySQL

**检查**:
1. MySQL 是否运行
2. 数据库 `diom_flowable` 是否存在
3. 用户名密码是否正确

### Q3: 无法注册到 Nacos

**检查**:
1. Nacos 是否运行在 8848 端口
2. Group 配置是否为 `HTTP_GROUP`
3. 网络是否可达

### Q4: 流程定义未部署

**检查**:
1. BPMN 文件是否在 `processes/` 目录
2. 文件扩展名是否为 `.bpmn`
3. XML 命名空间是否正确（使用 `flowable:`）

---

## 🔧 配置说明

### 核心配置
```yaml
flowable:
  # 数据库自动建表
  database-schema-update: true
  database-type: mysql
  
  # 禁用不需要的引擎
  event-registry-enabled: false
  app-engine-enabled: false
  idm-engine-enabled: false
  
  # 异步执行器
  async-executor-activate: true
  
  # 历史级别
  history-level: full
  
  # 自动部署流程
  check-process-definitions: true
  process-definition-location-prefix: classpath*:/processes/
```

---

## 📈 性能优化

### 数据库连接池
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### JVM 参数
```bash
java -jar \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  start-1.0.0-SNAPSHOT.jar
```

---

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
./test-flowable.sh
```

### 手动测试
```bash
# 1. 健康检查
curl http://localhost:8086/actuator/health

# 2. 启动流程
curl -X POST http://localhost:8086/flowable/start/simple-process \
  -H "Content-Type: application/json" \
  -d '{}'

# 3. 查询任务
curl "http://localhost:8086/flowable/tasks?assignee=admin"
```

---

## 📦 部署

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 启动
java -jar start/target/start-1.0.0-SNAPSHOT.jar
```

### Docker 部署
```bash
# 构建镜像
docker build -t diom-flowable:1.0.0 .

# 运行容器
docker run -d \
  --name diom-flowable \
  -p 8086:8086 \
  -e DB_HOST=mysql \
  -e NACOS_SERVER_ADDR=nacos:8848 \
  diom-flowable:1.0.0
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 👥 联系方式

- **项目负责人**: [您的名字]
- **邮箱**: [您的邮箱]
- **项目地址**: `/Users/yanchao/IdeaProjects/diom-workFlow/diom-flowable-service/`

---

## 🎯 快速链接

- [🚀 3 步启动](./START_HERE.md)
- [💻 命令速查](./COMMANDS.md)
- [🔧 错误排查](./LIQUIBASE_FIX.md)
- [📡 API 文档](./API.md)
- [📊 项目状态](./FINAL_STATUS.md)

---

**最后更新**: 2025-11-24  
**版本**: 1.0.0-SNAPSHOT  
**状态**: 🟢 Ready for Deployment

