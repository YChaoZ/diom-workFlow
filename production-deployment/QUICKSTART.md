# 🚀 生产环境部署快速开始指南

## 📦 本目录包含的内容

本目录包含了DIOM Workflow系统投产所需的**所有配置文件、脚本和文档**。

### 📁 目录结构

```
production-deployment/
├── README.md                         # 📘 总体架构说明（必读）
├── QUICKSTART.md                     # 📗 本文件 - 快速开始
│
├── frontend/                         # 前端配置
│   ├── .env.production              # ✅ Vite生产环境变量
│   ├── config.js                    # ✅ 运行时配置模板
│   └── nginx.conf                   # ✅ 前端服务器Nginx配置
│
├── gateway/                          # 网关配置
│   └── application-prod.yml         # ✅ Gateway生产配置
│
├── backend/                          # 后端配置
│   ├── auth-service-prod.yml        # ✅ 认证服务配置
│   └── workflow-service-prod.yml    # ✅ 工作流服务配置
│
├── scripts/                          # 部署脚本（可执行）
│   ├── deploy-frontend.sh           # ✅ 前端部署脚本
│   ├── deploy-backend.sh            # ✅ 后端部署脚本
│   └── health-check.sh              # ✅ 健康检查脚本
│
└── docs/                             # 详细文档
    ├── ARCHITECTURE.md              # 📘 架构设计文档（必读）
    └── DEPLOYMENT.md                # 📘 详细部署流程（必读）
```

---

## ⚡ 5分钟快速理解

### 1️⃣ 我们的生产架构是什么样的？

```
用户浏览器
    ↓
前端服务器集群（Nginx） - 多台机器
    ↓
统一网关（Gateway）
    ↓
后端微服务（Auth、Web、Workflow）
    ↓
Nacos（服务注册中心）+ MySQL（数据库）
```

**关键点**：
- ✅ 前端可以部署在多台机器上（你的需求）
- ✅ 所有前端通过统一Gateway访问后端（你的需求）
- ✅ 使用Nginx反向代理，无跨域问题
- ✅ 服务间使用Dubbo RPC通信

### 2️⃣ 需要准备什么服务器？

| 服务器类型 | 数量 | 配置建议 | 软件要求 |
|-----------|------|---------|---------|
| **前端服务器** | 3台 | 2核4G | Nginx 1.20+ |
| **后端服务器** | 1-3台 | 4核8G | Java 8+ |
| **Nacos服务器** | 1台 | 2核4G | Java 8+ |
| **MySQL服务器** | 1台 | 4核8G | MySQL 8.0+ |

### 3️⃣ 部署步骤是什么？

```bash
# 阶段1: 数据库 (1小时)
mysql -h db-server -uroot -p < 初始化脚本

# 阶段2: Nacos (30分钟)
./nacos/bin/startup.sh -m standalone

# 阶段3: 后端服务 (2小时)
cd production-deployment/scripts
./deploy-backend.sh all prod

# 阶段4: 前端 (1小时)
./deploy-frontend.sh prod frontend-a
./deploy-frontend.sh prod frontend-b
./deploy-frontend.sh prod frontend-c

# 阶段5: 验证
./health-check.sh
```

### 4️⃣ 配置文件需要改什么？

**必须修改的配置项**（标记为 ✅）：

#### Gateway配置 (`gateway/application-prod.yml`)
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos.company.com:8848  # ✅ 改成实际Nacos地址
```

#### 后端服务配置 (`backend/*-prod.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://db.company.com:3306/diom_workflow  # ✅ 改成实际MySQL地址
    username: diom_user  # ✅ 改成实际用户名
    password: ${DB_PASSWORD}  # ✅ 使用环境变量或直接填写密码
```

#### 前端Nginx配置 (`frontend/nginx.conf`)
```nginx
server_name frontend-a.company.com;  # ✅ 改成实际域名

upstream diom_gateway {
    server gateway.company.com:8080;  # ✅ 改成实际Gateway地址
}
```

---

## 📋 完整部署清单

### 🔧 准备阶段（在部署前1周完成）

- [ ] 确认服务器数量和配置
- [ ] 分配服务器IP地址
- [ ] 配置DNS解析（如使用域名）
- [ ] 申请SSL证书（如使用HTTPS）
- [ ] 创建所有服务器账号
- [ ] 配置SSH密钥认证
- [ ] 配置防火墙规则
- [ ] 确认网络连通性

### 📦 软件安装（在部署前3天完成）

- [ ] 安装Java 8+ (后端服务器)
- [ ] 安装Maven 3.6+ (开发机器)
- [ ] 安装Node.js 16+ (开发机器)
- [ ] 安装Nginx (前端服务器)
- [ ] 安装MySQL 8.0 (数据库服务器)
- [ ] 安装Nacos 2.2.3 (Nacos服务器)

### 🗄️ 数据库准备（在部署当天完成）

- [ ] 创建数据库 `diom_workflow`
- [ ] 创建数据库用户 `diom_user`
- [ ] 执行初始化脚本（5个SQL文件）
- [ ] 验证表结构创建成功
- [ ] 插入初始数据（admin用户等）

### 🔌 Nacos配置（在部署当天完成）

- [ ] 启动Nacos服务
- [ ] 访问Nacos控制台
- [ ] 创建命名空间 `diom-workflow-prod`
- [ ] 验证Nacos可正常访问

### 🛠️ 配置文件修改（在部署当天完成）

- [ ] 修改 `gateway/application-prod.yml` 中的Nacos地址
- [ ] 修改 `backend/auth-service-prod.yml` 中的数据库地址
- [ ] 修改 `backend/workflow-service-prod.yml` 中的数据库地址
- [ ] 修改 `frontend/nginx.conf` 中的域名和Gateway地址
- [ ] 修改 `scripts/deploy-*.sh` 中的服务器地址

### 🚀 后端部署（在部署当天完成）

- [ ] 编译打包所有服务（Maven）
- [ ] 上传JAR文件到服务器
- [ ] 启动Gateway
- [ ] 启动Auth Service
- [ ] 启动Web Service
- [ ] 启动Workflow Service
- [ ] 验证所有服务健康检查通过
- [ ] 验证Nacos服务注册成功

### 🎨 前端部署（在部署当天完成）

- [ ] 打包前端代码（npm run build）
- [ ] 上传到前端服务器A
- [ ] 配置Nginx（服务器A）
- [ ] 上传到前端服务器B
- [ ] 配置Nginx（服务器B）
- [ ] 上传到前端服务器C
- [ ] 配置Nginx（服务器C）
- [ ] 验证前端页面可访问

### ✅ 验证测试（在部署当天完成）

- [ ] 执行健康检查脚本
- [ ] 测试用户登录功能
- [ ] 测试流程发起功能
- [ ] 测试任务处理功能
- [ ] 测试通知功能
- [ ] 测试流程设计器（超管）
- [ ] 测试多前端服务器访问
- [ ] 压力测试（可选）

---

## 🎯 典型部署场景

### 场景1：单服务器部署（开发/测试环境）

**服务器**: 1台 (8核16G)  
**部署方案**: 所有服务部署在同一台机器

```bash
# 前端、后端、Nacos、MySQL全部在一台机器上
# 简化配置，所有地址使用 localhost
```

### 场景2：标准部署（小型生产环境）

**服务器**: 5台
- 前端服务器 x2
- 后端服务器 x1（所有微服务）
- Nacos服务器 x1
- MySQL服务器 x1

```bash
# 前端: frontend-a.com, frontend-b.com
# 后端: backend.company.com
# Nacos: nacos.company.com
# MySQL: db.company.com
```

### 场景3：高可用部署（大型生产环境）

**服务器**: 10+台
- 负载均衡器 x1
- 前端服务器 x3
- Gateway x2 (多实例)
- Auth Service x2
- Web Service x2
- Workflow Service x2
- Nacos集群 x3
- MySQL主从 x2

```bash
# 所有服务多实例部署
# Nacos集群模式
# MySQL主从复制
# 负载均衡
```

---

## 📚 必读文档优先级

### 🔴 必须阅读（部署前）
1. **README.md** - 了解整体架构
2. **docs/ARCHITECTURE.md** - 了解技术细节
3. **docs/DEPLOYMENT.md** - 了解详细部署流程

### 🟡 建议阅读（部署时参考）
4. **frontend/nginx.conf** - 前端Nginx配置说明
5. **gateway/application-prod.yml** - Gateway配置说明
6. **scripts/*.sh** - 部署脚本使用说明

### 🟢 可选阅读（遇到问题时）
7. **VITE_PROXY_EXPLANATION.md** (项目根目录) - Vite代理详解

---

## 🆘 常见问题

### Q1: 服务启动失败？

```bash
# 检查日志
tail -100 /var/log/diom-workflow/*/app.log

# 检查端口占用
ss -tuln | grep -E '8080|8081|8082|8085'

# 检查Nacos连接
curl http://nacos-server:8848/nacos/v1/console/health/readiness
```

### Q2: 前端无法访问后端API？

```bash
# 1. 检查Nginx配置
sudo nginx -t

# 2. 检查Gateway是否运行
curl http://gateway-server:8080/actuator/health

# 3. 检查Nginx反向代理配置
curl http://frontend-server/auth/login
```

### Q3: Nacos服务注册失败？

```bash
# 1. 检查Nacos是否启动
curl http://nacos-server:8848/nacos/v1/console/health/readiness

# 2. 检查网络连通性
ping nacos-server

# 3. 检查配置文件中的Nacos地址
grep nacos.discovery.server-addr application-prod.yml
```

### Q4: 数据库连接失败？

```bash
# 1. 检查MySQL是否启动
systemctl status mysqld

# 2. 检查网络连通性
telnet db-server 3306

# 3. 检查用户权限
mysql -h db-server -u diom_user -p -e "SELECT 1"
```

---

## 🔄 快速回滚

如果部署后发现问题，立即执行回滚：

```bash
# 1. 停止新版本服务
ssh backend-server "pkill -f diom-gateway"

# 2. 恢复备份版本
ssh backend-server "cd /data/backup/diom-workflow/gateway && \
  sudo cp diom-gateway-backup-*.jar /data/diom-workflow/gateway/diom-gateway.jar"

# 3. 启动旧版本
ssh backend-server "cd /data/diom-workflow/gateway && \
  nohup java -jar diom-gateway.jar --spring.profiles.active=prod > /var/log/diom-workflow/gateway/app.log 2>&1 &"
```

---

## 📞 获取帮助

**部署前咨询**:
- 技术架构问题: 查看 `docs/ARCHITECTURE.md`
- 部署流程问题: 查看 `docs/DEPLOYMENT.md`
- 脚本使用问题: 查看脚本文件头部注释

**部署中遇到问题**:
1. 检查日志文件 `/var/log/diom-workflow/*/app.log`
2. 执行健康检查 `./scripts/health-check.sh`
3. 查看Nacos控制台 `http://nacos-server:8848/nacos`

**紧急联系**:
- 技术负责人: [Phone]
- 运维负责人: [Phone]

---

## ✅ 部署成功标志

当你看到以下结果时，说明部署成功：

1. ✅ `./health-check.sh` 全部通过
2. ✅ 可以访问前端页面 `http://frontend-a.company.com`
3. ✅ 可以使用admin/123456登录
4. ✅ 可以发起工作流
5. ✅ 可以处理任务
6. ✅ 可以查看通知
7. ✅ 超管可以访问流程设计器

---

**祝部署顺利！** 🎉

如有问题，请先查阅 `docs/DEPLOYMENT.md` 详细文档。

---

**文档版本**: v1.0.0  
**生成时间**: 2025-11-15  
**适用版本**: DIOM Workflow v1.0.0

