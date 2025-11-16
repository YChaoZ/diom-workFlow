# DIOM Workflow 生产环境部署方案

## 📋 目录结构

```
production-deployment/
├── README.md                 # 本文件 - 总体架构说明
├── frontend/                 # 前端配置
│   ├── .env.production      # 生产环境变量
│   ├── config.js            # 运行时配置模板
│   └── nginx.conf           # 前端Nginx配置
├── gateway/                 # 网关配置
│   ├── application-prod.yml # Gateway生产配置
│   └── deployment.md        # Gateway部署说明
├── backend/                 # 后端配置
│   ├── auth-service-prod.yml
│   ├── web-service-prod.yml
│   ├── workflow-service-prod.yml
│   └── deployment.md        # 后端部署说明
├── nginx/                   # Nginx配置
│   ├── frontend-server.conf # 前端服务器配置
│   └── ssl.conf             # HTTPS配置模板
├── scripts/                 # 部署脚本
│   ├── deploy-frontend.sh   # 前端部署脚本
│   ├── deploy-backend.sh    # 后端部署脚本
│   ├── health-check.sh      # 健康检查脚本
│   └── rollback.sh          # 回滚脚本
└── docs/                    # 文档
    ├── ARCHITECTURE.md      # 详细架构设计
    ├── DEPLOYMENT.md        # 部署流程文档
    ├── OPERATION.md         # 运维文档
    └── TROUBLESHOOTING.md   # 故障排查指南
```

---

## 🏗️ 生产环境架构

### 整体架构图

```
                        ┌─────────────────────────────────────┐
                        │         负载均衡器 (可选)            │
                        │    Nginx / HAProxy / F5             │
                        └─────────────────────────────────────┘
                                       │
            ┌──────────────────────────┼──────────────────────────┐
            │                          │                          │
    ┌───────▼────────┐        ┌───────▼────────┐        ┌───────▼────────┐
    │  前端服务器 A   │        │  前端服务器 B   │        │  前端服务器 C   │
    │  机器A (Nginx) │        │  机器B (Nginx) │        │  机器C (Nginx) │
    │  静态文件部署   │        │  静态文件部署   │        │  静态文件部署   │
    └───────┬────────┘        └───────┬────────┘        └───────┬────────┘
            │                          │                          │
            └──────────────────────────┼──────────────────────────┘
                                       │
                                       │ API请求 (反向代理)
                                       ▼
                        ┌─────────────────────────────────────┐
                        │        统一应用网关集群               │
                        │   diom-gateway (Spring Cloud)       │
                        │   服务发现 / 路由 / 认证 / 限流      │
                        └─────────────────────────────────────┘
                                       │
                        ┌──────────────┼──────────────┐
                        │              │              │
                ┌───────▼────────┐ ┌──▼─────────┐ ┌─▼──────────────┐
                │  认证服务集群   │ │  Web服务   │ │  工作流服务     │
                │  auth-service  │ │ web-service│ │workflow-service │
                │  Dubbo提供者   │ │Dubbo消费者 │ │  Camunda引擎   │
                └───────┬────────┘ └──┬─────────┘ └─┬──────────────┘
                        │              │              │
                        └──────────────┼──────────────┘
                                       │ Dubbo RPC
                                       │
                        ┌──────────────▼──────────────┐
                        │      Nacos 注册中心          │
                        │   服务注册 / 配置管理        │
                        └─────────────────────────────┘
                                       │
                        ┌──────────────▼──────────────┐
                        │       MySQL 数据库           │
                        │   业务数据 / Camunda数据     │
                        └─────────────────────────────┘
```

### 服务端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| **前端服务器** | 80/443 | Nginx (HTTP/HTTPS) |
| **Gateway** | 8080 | Spring Cloud Gateway |
| **Auth Service** | 8081 | 认证服务 |
| **Web Service** | 8082 | Web层服务 |
| **Workflow Service** | 8085 | 工作流服务 |
| **Nacos** | 8848 | 注册中心/配置中心 |
| **MySQL** | 3306 | 数据库 |

---

## 🚀 快速部署指南

### 前置条件

- [ ] Java 8+ 已安装
- [ ] MySQL 8.0 已安装并创建数据库
- [ ] Nacos 2.2.3 已部署
- [ ] Nginx 已安装（前端服务器）
- [ ] Node.js 16+ 已安装（用于前端打包）

### 部署步骤

#### 1. 数据库初始化

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS diom_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行初始化脚本（按顺序）
mysql -uroot -p diom_workflow < diom-auth-service/src/main/resources/sql/schema.sql
mysql -uroot -p diom_workflow < diom-auth-service/src/main/resources/sql/data.sql
mysql -uroot -p diom_workflow < diom-workflow-service/start/src/main/resources/sql/camunda_ddl.sql
mysql -uroot -p diom_workflow < diom-workflow-service/start/src/main/resources/sql/workflow_tables.sql
mysql -uroot -p diom_workflow < PROCESS_DESIGNER_INIT.sql
```

#### 2. Nacos配置

```bash
# 1. 启动Nacos
cd /path/to/nacos/bin
./startup.sh -m standalone

# 2. 访问 http://nacos-server:8848/nacos
# 用户名/密码: nacos/nacos

# 3. 创建命名空间: diom-workflow
# 4. 导入配置文件（见 gateway/application-prod.yml）
```

#### 3. 后端服务部署

```bash
cd production-deployment/scripts
./deploy-backend.sh
```

#### 4. 前端部署

```bash
cd production-deployment/scripts
./deploy-frontend.sh
```

#### 5. 验证部署

```bash
./health-check.sh
```

---

## 📊 环境对比

| 环境 | 域名/地址 | 用途 | 数据库 |
|------|----------|------|-------|
| **开发环境** | localhost:3000 | 本地开发 | localhost:3306 |
| **测试环境** | test.company.com | 功能测试 | test-db.company.com |
| **生产环境** | www.company.com | 正式服务 | prod-db.company.com |

---

## 🔒 安全配置

### 1. 网络安全
- ✅ 前端服务器开放 80/443 端口
- ✅ Gateway开放 8080 端口（内网）
- ✅ 后端微服务仅内网访问
- ✅ MySQL仅允许应用服务器连接
- ✅ Nacos设置访问控制

### 2. 应用安全
- ✅ JWT Token过期时间：1小时
- ✅ HTTPS证书（Let's Encrypt）
- ✅ Gateway限流配置
- ✅ 敏感配置加密（Nacos）
- ✅ 日志脱敏

### 3. 数据安全
- ✅ 数据库定期备份
- ✅ 密码强度要求
- ✅ SQL注入防护
- ✅ XSS防护

---

## 📝 配置清单

### 需要修改的配置项

#### Gateway配置
```yaml
# gateway/application-prod.yml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server.company.com:8848  # ✅ 修改为实际Nacos地址
```

#### 后端服务配置
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://db-server.company.com:3306/diom_workflow  # ✅ 修改为实际数据库地址
    username: diom_user  # ✅ 修改为生产环境用户
    password: ${DB_PASSWORD}  # ✅ 使用环境变量
```

#### 前端配置
```javascript
// frontend/config.js
window.APP_CONFIG = {
  GATEWAY_URL: ''  // ✅ 留空，使用Nginx反向代理（推荐）
  // 或者指定: 'http://gateway.company.com:8080'
}
```

---

## 📈 监控和日志

### 日志位置
```
/var/log/diom-workflow/
├── gateway/
│   └── gateway.log
├── auth-service/
│   └── auth.log
├── web-service/
│   └── web.log
└── workflow-service/
    └── workflow.log
```

### 健康检查端点
```bash
# Gateway
curl http://localhost:8080/actuator/health

# 各微服务
curl http://localhost:8081/actuator/health  # auth
curl http://localhost:8082/actuator/health  # web
curl http://localhost:8085/actuator/health  # workflow
```

---

## 🆘 故障排查

### 常见问题

1. **服务无法启动**
   - 检查端口是否被占用
   - 检查日志文件
   - 验证配置文件

2. **服务注册失败**
   - 检查Nacos是否启动
   - 验证网络连接
   - 检查命名空间配置

3. **前端无法访问API**
   - 检查Nginx配置
   - 验证Gateway状态
   - 检查CORS配置

详见: `docs/TROUBLESHOOTING.md`

---

## 📞 联系方式

- 技术负责人: [Name]
- 运维负责人: [Name]
- 紧急联系: [Phone]

---

**生成时间**: 2025-11-15  
**版本**: v1.0.0  
**状态**: ✅ 可投产

