# Diom Workflow - 企业级工作流管理系统

## 项目简介

基于 **Spring Boot 2.4.11 + Java 8 + Camunda 7.16 + Dubbo + Nacos** 的企业级工作流管理系统。采用**微服务架构**，提供完整的工作流解决方案，包括流程设计、流程执行、任务管理、权限控制、消息通知等功能。

### ✨ 核心特性

- 🚀 **微服务架构** - Gateway + Web层 + 业务服务，清晰的分层设计
- 🔐 **统一认证** - JWT + RBAC权限体系，细粒度权限控制
- 📊 **工作流引擎** - Camunda集成，支持BPMN 2.0标准
- 🎨 **在线设计器** - 可视化流程设计，拖拽式建模
- 📝 **流程模板** - 模板管理、草稿保存、历史回填
- 🔔 **消息通知** - 实时任务通知，未读消息提醒
- 📦 **服务治理** - Nacos服务发现，Dubbo RPC通信
- 🌐 **现代前端** - Vue3 + Element Plus，响应式设计
- 🚢 **生产就绪** - 完整的生产部署方案和自动化脚本

### 🎯 适用场景

- 企业OA审批流程（请假、报销、采购等）
- 业务工作流管理（订单处理、客户服务等）
- 项目协作流程（任务分配、进度跟踪等）
- 自定义业务流程（可通过在线设计器自定义）

## 技术栈

### 核心框架
- **Spring Boot**: 2.4.11
- **Spring Cloud**: 2020.0.3
- **Spring Cloud Gateway**: 3.0.3
- **Spring Cloud Alibaba**: 2.2.6.RELEASE
- **Java**: 8

### 微服务组件
- **Dubbo**: 2.7.12（RPC 框架）
- **Nacos**: 2.0.4（注册中心 + 配置中心）
- **Seata**: 1.4.2（分布式事务）

### 工作流引擎
- **Camunda BPM**: 7.15.0

### 数据库
- **MySQL**: 5.7+ / 8.0
- **Redis**: 5.0+（可选）

### 前端
- **Vue.js**: 2.6.x / 3.2.x
- **Element UI**: 对应版本

## 系统架构

```
┌─────────────┐
│  前端 (Vue) │
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────────┐
│  网关层 (Gateway)│  ← 统一鉴权、路由转发
└──────┬──────────┘
       │ HTTP
       ▼
┌─────────────────┐
│  Web 层         │  ← HTTP/Dubbo 协议转换
└──────┬──────────┘
       │ Dubbo RPC
       ▼
┌─────────────────────────┐
│  服务层 (COLA 架构)      │
│  - Workflow Service     │  ← 工作流服务
│  - User Service         │  ← 用户服务
│  - ...                  │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────┐
│  基础设施层       │
│  - MySQL         │
│  - Nacos         │
│  - Seata         │
│  - Camunda       │
└─────────────────┘
```

## 应用清单

| 应用名称 | 端口 | 说明 | 状态 |
|---------|------|------|------|
| diom-common | - | 公共依赖库 | ✅ 完成 |
| diom-gateway | 8080 | 统一网关（JWT认证、路由转发、限流） | ✅ 完成 |
| diom-auth-service | 8081 | 认证服务（用户登录、JWT、RBAC） | ✅ 完成 |
| diom-web-service | 8082 | Web层服务（Dubbo消费者） | ✅ 完成 |
| diom-workflow-service | 8085 | 工作流服务（Camunda、流程设计器） | ✅ 完成 |
| diom-frontend | 3000 | 前端应用（Vue3、Element Plus） | ✅ 完成 |

## 目录结构

```
diom-workFlow/
├── diom-common/                        # 公共依赖库
│   ├── pom.xml
│   └── src/main/java/com/diom/common/
│       ├── dto/                        # 通用 DTO
│       ├── enums/                      # 枚举类型
│       ├── exception/                  # 异常定义
│       ├── constant/                   # 常量
│       └── utils/                      # 工具类
│
├── diom-gateway/                       # 网关服务
│   ├── pom.xml
│   ├── src/main/java/com/diom/gateway/
│   │   ├── GatewayApplication.java
│   │   ├── config/                     # JWT认证、CORS配置
│   │   ├── filter/                     # 鉴权过滤器
│   │   └── utils/                      # 工具类
│   └── src/main/resources/
│       └── application.yml             # Gateway路由配置
│
├── diom-auth-service/                  # 认证服务
│   ├── pom.xml
│   └── src/main/java/com/diom/auth/
│       ├── AuthApplication.java
│       ├── controller/                 # 登录、注册、用户信息
│       ├── service/                    # JWT生成、RBAC权限
│       ├── mapper/                     # MyBatis Plus
│       ├── entity/                     # 用户、角色、权限实体
│       └── config/                     # Security配置
│
├── diom-web-service/                   # Web 层服务
│   ├── pom.xml
│   └── src/main/java/com/diom/web/
│       ├── WebApplication.java
│       ├── controller/                 # RESTful API
│       └── service/                    # Dubbo消费者
│
├── diom-workflow-service/              # 工作流服务
│   ├── pom.xml (父 POM)
│   ├── start/                          # 启动模块
│   │   ├── src/main/java/com/diom/workflow/
│   │   │   ├── controller/             # 工作流API、流程设计器API
│   │   │   ├── service/                # Camunda集成、通知服务
│   │   │   ├── entity/                 # 流程、任务、通知、模板实体
│   │   │   ├── mapper/                 # MyBatis Plus
│   │   │   ├── listener/               # Camunda任务监听器
│   │   │   └── config/                 # Camunda配置
│   │   └── src/main/resources/
│   │       ├── processes/              # BPMN流程定义
│   │       └── sql/                    # 数据库初始化脚本
│   └── workflow-client/                # 客户端 API
│
├── diom-frontend/                      # 前端应用（Vue3）
│   ├── package.json
│   ├── vite.config.js                  # Vite配置、代理配置
│   └── src/
│       ├── api/                        # API接口（Axios）
│       ├── views/                      # 页面组件
│       │   ├── Login.vue               # 登录页
│       │   ├── Home.vue                # 首页Dashboard
│       │   ├── Workflow/               # 工作流模块
│       │   │   ├── WorkflowList.vue    # 流程定义列表
│       │   │   ├── WorkflowStart.vue   # 发起流程
│       │   │   ├── TaskList.vue        # 我的任务
│       │   │   ├── InstanceList.vue    # 流程实例
│       │   │   ├── Templates.vue       # 模板管理
│       │   │   ├── ProcessDesignList.vue  # 流程设计器列表
│       │   │   ├── ProcessDesigner.vue    # 在线BPMN设计器
│       │   │   └── Toolbar.vue         # 自定义工具栏
│       │   ├── User/                   # 用户模块
│       │   ├── Notification/           # 通知中心
│       │   └── System/                 # 系统管理（RBAC）
│       ├── components/                 # 公共组件
│       ├── stores/                     # Pinia状态管理
│       ├── router/                     # Vue Router
│       ├── directives/                 # 自定义指令（v-permission）
│       └── utils/                      # 工具类（request.js）
│
├── production-deployment/              # 生产部署方案（✨新增）
│   ├── README.md                       # 架构说明
│   ├── QUICKSTART.md                   # 快速开始
│   ├── frontend/                       # 前端配置（Nginx、环境变量）
│   ├── gateway/                        # Gateway生产配置
│   ├── backend/                        # 后端服务配置
│   ├── scripts/                        # 自动化部署脚本
│   └── docs/                           # 详细文档（架构、部署）
│
├── docs/                               # 文档中心（✨新增）
│   ├── README.md                       # 文档索引
│   ├── reports/                        # 完成报告、测试报告
│   ├── guides/                         # 操作指南、快速开始
│   ├── architecture/                   # 架构设计、技术方案
│   └── development/                    # 开发计划、状态追踪
│
├── start-all-services.sh               # 一键启动所有服务
├── PROCESS_DESIGNER_INIT.sql           # 流程设计器数据库初始化
└── README.md                           # 本文件
```

## 快速开始

### 环境准备

1. **JDK 8**
2. **Maven 3.6+**
3. **MySQL 5.7+ / 8.0**
4. **Nacos 2.0.4**
5. **Node.js 14+**（前端）

### 基础设施启动

#### 1. 启动 Nacos

```bash
# 下载 Nacos 2.0.4
wget https://github.com/alibaba/nacos/releases/download/2.0.4/nacos-server-2.0.4.tar.gz
tar -xzf nacos-server-2.0.4.tar.gz
cd nacos/bin

# 单机模式启动
sh startup.sh -m standalone

# 访问控制台: http://localhost:8848/nacos
# 默认账号密码: nacos/nacos
```

#### 2. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE diom_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构（见各服务的 SQL 文件）
```

#### 3. 启动 Seata Server（可选）

```bash
# 下载 Seata 1.4.2
wget https://github.com/seata/seata/releases/download/v1.4.2/seata-server-1.4.2.tar.gz
tar -xzf seata-server-1.4.2.tar.gz
cd seata/bin

# 启动
sh seata-server.sh
```

#### 4. 部署 Camunda Standalone（可选）

```bash
# 下载 Camunda 7.15.0 Tomcat 版本
wget https://downloads.camunda.cloud/release/camunda-bpm/tomcat/7.15/camunda-bpm-tomcat-7.15.0.tar.gz
tar -xzf camunda-bpm-tomcat-7.15.0.tar.gz

# 配置 MySQL 数据源后启动
cd camunda-bpm-tomcat-7.15.0/bin
sh startup.sh

# 访问: http://localhost:8080/camunda
# 默认账号: demo/demo
```

### 服务启动顺序

#### 1. 编译公共模块

```bash
cd diom-common
mvn clean install
```

#### 2. 启动网关

```bash
cd diom-gateway
mvn spring-boot:run
```

#### 3. 启动认证服务

```bash
cd diom-auth-service
mvn spring-boot:run
```

#### 4. 启动 Web 服务

```bash
cd diom-web-service
mvn spring-boot:run
```

#### 5. 启动业务服务

```bash
cd diom-workflow-service/start
mvn spring-boot:run
```

#### 6. 启动前端

```bash
cd diom-frontend
npm install
npm run serve
```

### Docker 部署

每个服务都提供了 Dockerfile，可以使用 Docker Compose 一键部署：

```bash
docker-compose up -d
```

## 配置说明

### Nacos 配置

在 Nacos 配置中心创建以下配置文件：

1. `common-config.yaml` - 公共配置
2. `diom-gateway-dev.yaml` - 网关配置
3. `diom-auth-service-dev.yaml` - 认证服务配置
4. ...

### 环境变量

各服务支持以下环境变量：

| 变量名 | 说明 | 默认值 |
|-------|------|-------|
| NACOS_SERVER_ADDR | Nacos 地址 | localhost:8848 |
| DB_HOST | 数据库地址 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | diom_workflow |
| JWT_SECRET | JWT 密钥 | （见配置文件） |

## 开发指南

### COLA 架构说明

业务服务（如 workflow-service）采用 COLA 架构：

- **adapter**: 对外接口适配（Dubbo 实现、MQ 监听）
- **app**: 应用服务（用例编排、CQRS）
- **domain**: 领域模型（核心业务逻辑）
- **infrastructure**: 基础设施（数据库、缓存、外部服务）
- **client**: 客户端 API（接口定义，供其他服务引用）

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一使用 Result<T> 返回结果
- 异常使用 BizException 和 SysException
- 日志使用 SLF4J

### API 文档

各服务启动后可访问：

- 网关: http://localhost:8080/actuator/gateway/routes
- 认证服务: http://localhost:8081/actuator/health
- Web 服务: http://localhost:8082/actuator/health

## 监控与运维

### 健康检查

所有服务都提供了 Actuator 健康检查端点：

```bash
curl http://localhost:8080/actuator/health
```

### 日志

日志文件位于各服务的 `logs/` 目录。

### 性能监控

可集成 Prometheus + Grafana 进行监控。

## 已完成功能

### ✅ 核心功能

- [x] **统一网关** - Spring Cloud Gateway，JWT认证，路由转发
- [x] **认证服务** - 用户登录/注册，JWT Token生成，BCrypt密码加密
- [x] **RBAC权限体系** - 角色管理、权限管理、动态菜单、权限指令
- [x] **工作流引擎** - Camunda 7.16集成，流程定义、实例管理、任务处理
- [x] **流程模板** - 模板管理、草稿保存、历史参数自动回填
- [x] **消息通知中心** - 任务分配通知、任务完成通知、未读消息提醒
- [x] **在线流程设计器** - BPMN可视化设计、拖拽式建模、流程版本管理
- [x] **服务注册发现** - Nacos集成，服务健康检查，配置管理
- [x] **Dubbo RPC** - 微服务间通信，负载均衡，服务降级
- [x] **前端界面** - Vue3 + Element Plus，响应式布局，权限控制

### 🎨 前端页面

- [x] 用户登录/注册
- [x] 首页Dashboard（统计数据）
- [x] 流程定义列表（查看、启动）
- [x] 发起流程（表单填写、模板选择、草稿保存）
- [x] 我的任务（待办任务、任务处理）
- [x] 流程实例（实例列表、详情查看）
- [x] 模板管理（创建、编辑、删除）
- [x] 消息通知（通知列表、未读提醒）
- [x] 流程设计器（BPMN设计、发布、历史版本）
- [x] 角色管理（RBAC权限配置）

### 🚀 生产部署

- [x] **生产部署方案** - 完整的生产环境配置
- [x] **前端配置** - Nginx配置、环境变量、代理设置
- [x] **后端配置** - Gateway、微服务生产配置
- [x] **自动化脚本** - 前端/后端一键部署、健康检查
- [x] **详细文档** - 架构设计、部署流程、运维指南

## 技术文档

### 📚 文档导航

**快速开始**:
- [快速开始指南](docs/guides/QUICK_START.md)
- [快速测试指南](docs/guides/QUICK_TEST_GUIDE.md)
- [Vite代理详解](docs/guides/VITE_PROXY_EXPLANATION.md)

**架构设计**:
- [系统架构与调用链](docs/architecture/ARCHITECTURE_AND_CALL_CHAIN.md)
- [RBAC权限设计](docs/architecture/RBAC_DESIGN.md)
- [流程设计器设计](docs/architecture/PROCESS_DESIGNER_DESIGN.md)
- [服务发现方案](docs/architecture/SERVICE_DISCOVERY_SOLUTION.md)

**生产部署**:
- [生产部署报告](docs/reports/PRODUCTION_DEPLOYMENT_REPORT.md)
- [部署配置目录](production-deployment/)

**开发文档**:
- [待办事项](docs/development/TODO_LIST.md)
- [当前状态](docs/development/CURRENT_STATUS.md)
- [优化路线图](docs/development/OPTIMIZATION_ROADMAP.md)

**完整文档**: 查看 [docs/](docs/) 目录，包含所有技术文档、报告、指南

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 联系方式

- 作者: 简单
- Email: 164992121@qq.com
- 项目地址: https://github.com/YChaoZ/diom-workFlow

---

## 📊 项目统计

- **开发周期**: 2024年11月14日 - 2024年11月16日
- **代码行数**: 约30,000行（后端 + 前端）
- **文档数量**: 80+篇技术文档
- **功能模块**: 10+个核心模块
- **API接口**: 50+个RESTful API
- **数据库表**: 30+张业务表（含Camunda表）

## ⚠️ 注意事项

### 开发环境

- 本项目已完成核心功能开发，可直接用于学习和二次开发
- 开发环境使用Vite代理，生产环境使用Nginx反向代理
- 数据库密码等敏感信息请使用环境变量配置

### 生产部署

- 生产部署方案见 `production-deployment/` 目录
- 部署前请仔细阅读 `production-deployment/QUICKSTART.md`
- 确保修改所有配置文件中标记为 ✅ 的配置项

### 已知限制

- Seata分布式事务暂未集成（预留接口）
- Camunda与MySQL 8.0兼容性问题已通过手动建表解决
- 前端暂未实现响应式移动端适配

### 后续优化建议

- 集成Seata实现分布式事务
- 添加Redis缓存提升性能
- 实现更多的BPMN元素支持
- 添加流程可视化展示（流程图）
- 完善单元测试和集成测试

---

**项目状态**: ✅ 核心功能完成，生产就绪

