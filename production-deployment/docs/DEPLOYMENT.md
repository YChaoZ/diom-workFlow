# DIOM Workflow 生产环境部署流程

## 📋 部署检查清单

### 前置条件确认

- [ ] **服务器准备**
  - [ ] 前端服务器 x3 (已安装Nginx)
  - [ ] 后端服务器 x1-3 (已安装Java 8+)
  - [ ] 数据库服务器 x1 (MySQL 8.0)
  - [ ] Nacos服务器 x1-3

- [ ] **网络配置**
  - [ ] 内网互通测试通过
  - [ ] 防火墙规则已配置
  - [ ] 域名DNS已解析
  - [ ] SSL证书已申请（如需HTTPS）

- [ ] **软件版本**
  - [ ] Java: OpenJDK 1.8.0_xxx
  - [ ] Maven: 3.6+
  - [ ] Node.js: 16+
  - [ ] MySQL: 8.0+
  - [ ] Nginx: 1.20+
  - [ ] Nacos: 2.2.3

- [ ] **账号权限**
  - [ ] 服务器SSH访问权限
  - [ ] 数据库管理员账号
  - [ ] Nacos管理员账号
  - [ ] 域名管理权限

---

## 🚀 标准部署流程

### 阶段1: 基础环境准备 (预计2小时)

#### 1.1 安装Java环境

```bash
# 所有后端服务器执行
# 检查Java版本
java -version

# 如未安装，执行安装
sudo yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
# 或
sudo apt-get install -y openjdk-8-jdk

# 配置JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk' | sudo tee -a /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile
source /etc/profile
```

#### 1.2 安装Nginx

```bash
# 所有前端服务器执行
# CentOS
sudo yum install -y nginx

# Ubuntu
sudo apt-get install -y nginx

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx

# 验证
nginx -v
curl http://localhost
```

#### 1.3 创建部署目录

```bash
# 所有服务器执行
sudo mkdir -p /data/diom-workflow
sudo mkdir -p /data/backup/diom-workflow
sudo mkdir -p /var/log/diom-workflow/{gateway,auth-service,web-service,workflow-service}

# 创建应用用户
sudo useradd -r -s /bin/bash diom
sudo chown -R diom:diom /data/diom-workflow
sudo chown -R diom:diom /var/log/diom-workflow
```

---

### 阶段2: 数据库部署 (预计1小时)

#### 2.1 安装MySQL

```bash
# MySQL服务器执行
# 下载MySQL Yum Repository
wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
sudo rpm -Uvh mysql80-community-release-el7-3.noarch.rpm

# 安装MySQL
sudo yum install -y mysql-community-server

# 启动MySQL
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 获取临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 修改root密码
mysql -uroot -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';
FLUSH PRIVILEGES;
```

#### 2.2 创建数据库和用户

```bash
# 登录MySQL
mysql -uroot -p

# 执行以下SQL
CREATE DATABASE IF NOT EXISTS diom_workflow 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

# 创建应用用户
CREATE USER 'diom_user'@'%' IDENTIFIED BY 'DiomPassword123!';
GRANT ALL PRIVILEGES ON diom_workflow.* TO 'diom_user'@'%';
FLUSH PRIVILEGES;

# 验证
SHOW DATABASES;
SELECT user, host FROM mysql.user WHERE user='diom_user';
```

#### 2.3 初始化数据库表

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow

# 按顺序执行初始化脚本
mysql -h db.company.com -udiom_user -p diom_workflow < diom-auth-service/src/main/resources/sql/schema.sql
mysql -h db.company.com -udiom_user -p diom_workflow < diom-auth-service/src/main/resources/sql/data.sql
mysql -h db.company.com -udiom_user -p diom_workflow < diom-workflow-service/start/src/main/resources/sql/camunda_ddl.sql
mysql -h db.company.com -udiom_user -p diom_workflow < diom-workflow-service/start/src/main/resources/sql/workflow_tables.sql
mysql -h db.company.com -udiom_user -p diom_workflow < PROCESS_DESIGNER_INIT.sql

# 验证
mysql -h db.company.com -udiom_user -p diom_workflow -e "SHOW TABLES;"
```

---

### 阶段3: Nacos部署 (预计30分钟)

#### 3.1 安装Nacos

```bash
# Nacos服务器执行
cd /data
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos

# 配置数据库持久化（编辑 conf/application.properties）
cat >> conf/application.properties << EOF
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://db.company.com:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=diom_user
db.password=DiomPassword123!
EOF

# 启动Nacos（单机模式）
sh bin/startup.sh -m standalone

# 查看日志
tail -f logs/start.out
```

#### 3.2 配置Nacos

```bash
# 访问Nacos控制台
# http://nacos.company.com:8848/nacos
# 默认用户名/密码: nacos/nacos

# 创建命名空间
# 命名空间ID: diom-workflow-prod
# 命名空间名: DIOM Workflow生产环境

# 创建配置（可选，如使用动态配置）
# Data ID: diom-gateway-prod.yml
# Group: DIOM_GROUP
# 配置内容: （从 production-deployment/gateway/application-prod.yml 复制）
```

---

### 阶段4: 后端服务部署 (预计2小时)

#### 4.1 准备配置文件

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow

# 复制生产环境配置
cp production-deployment/gateway/application-prod.yml diom-gateway/src/main/resources/
cp production-deployment/backend/auth-service-prod.yml diom-auth-service/src/main/resources/
cp production-deployment/backend/workflow-service-prod.yml diom-workflow-service/start/src/main/resources/

# ⚠️ 重要：修改配置文件中的实际地址
# 1. Nacos地址: nacos.company.com:8848
# 2. MySQL地址: db.company.com:3306
# 3. 数据库用户名/密码
# 4. JWT密钥
```

#### 4.2 编译打包

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow

# Gateway
cd diom-gateway
mvn clean package -DskipTests -Pprod
ls -lh target/diom-gateway*.jar

# Auth Service
cd ../diom-auth-service
mvn clean package -DskipTests -Pprod
ls -lh target/diom-auth-service*.jar

# Web Service
cd ../diom-web-service
mvn clean package -DskipTests -Pprod
ls -lh target/diom-web-service*.jar

# Workflow Service
cd ../diom-workflow-service
mvn clean package -DskipTests -Pprod
ls -lh start/target/diom-workflow-service*.jar
```

#### 4.3 使用部署脚本部署

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow/production-deployment/scripts

# 添加执行权限
chmod +x *.sh

# 部署所有服务
./deploy-backend.sh all prod

# 或单独部署
# ./deploy-backend.sh gateway prod
# ./deploy-backend.sh auth prod
# ./deploy-backend.sh web prod
# ./deploy-backend.sh workflow prod
```

#### 4.4 手动部署（如不使用脚本）

```bash
# 上传JAR文件到服务器
scp diom-gateway/target/diom-gateway.jar diom@backend.company.com:/data/diom-workflow/gateway/
scp diom-auth-service/target/diom-auth-service.jar diom@backend.company.com:/data/diom-workflow/auth-service/
scp diom-web-service/target/diom-web-service.jar diom@backend.company.com:/data/diom-workflow/web-service/
scp diom-workflow-service/start/target/diom-workflow-service.jar diom@backend.company.com:/data/diom-workflow/workflow-service/

# SSH到后端服务器
ssh diom@backend.company.com

# 启动Gateway
cd /data/diom-workflow/gateway
nohup java -Xms512m -Xmx2g -jar diom-gateway.jar --spring.profiles.active=prod > /var/log/diom-workflow/gateway/app.log 2>&1 &

# 启动Auth Service
cd /data/diom-workflow/auth-service
nohup java -Xms512m -Xmx2g -jar diom-auth-service.jar --spring.profiles.active=prod > /var/log/diom-workflow/auth-service/app.log 2>&1 &

# 启动Web Service
cd /data/diom-workflow/web-service
nohup java -Xms512m -Xmx2g -jar diom-web-service.jar --spring.profiles.active=prod > /var/log/diom-workflow/web-service/app.log 2>&1 &

# 启动Workflow Service
cd /data/diom-workflow/workflow-service
nohup java -Xms1g -Xmx4g -jar diom-workflow-service.jar --spring.profiles.active=prod > /var/log/diom-workflow/workflow-service/app.log 2>&1 &

# 查看进程
ps aux | grep diom
```

#### 4.5 验证服务启动

```bash
# 检查进程
ps aux | grep diom | grep -v grep

# 检查端口
ss -tuln | grep -E '8080|8081|8082|8085'

# 检查健康状态
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # Web
curl http://localhost:8085/actuator/health  # Workflow

# 检查Nacos注册
curl "http://nacos.company.com:8848/nacos/v1/ns/instance/list?serviceName=diom-gateway"
```

---

### 阶段5: 前端部署 (预计1小时)

#### 5.1 准备前端配置

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend

# 复制生产环境配置
cp ../production-deployment/frontend/.env.production .env.production

# 修改 vite.config.js（已在开发阶段完成）
```

#### 5.2 打包前端

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend

# 安装依赖（如需要）
npm install

# 打包
npm run build

# 验证dist目录
ls -lh dist/
```

#### 5.3 使用部署脚本部署

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow/production-deployment/scripts

# 部署到前端服务器A
./deploy-frontend.sh prod frontend-a

# 部署到前端服务器B
./deploy-frontend.sh prod frontend-b

# 部署到前端服务器C
./deploy-frontend.sh prod frontend-c
```

#### 5.4 手动部署（如不使用脚本）

```bash
# 压缩dist目录
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend
tar -czf dist.tar.gz dist/

# 上传到前端服务器
scp dist.tar.gz root@frontend-a.company.com:/tmp/

# SSH到前端服务器
ssh root@frontend-a.company.com

# 备份现有版本
sudo mkdir -p /data/backup/diom-frontend
if [ -d "/usr/share/nginx/html/diom-frontend" ]; then
  sudo tar -czf /data/backup/diom-frontend/backup-$(date +%Y%m%d_%H%M%S).tar.gz -C /usr/share/nginx/html/diom-frontend .
fi

# 解压新版本
sudo mkdir -p /usr/share/nginx/html/diom-frontend
sudo tar -xzf /tmp/dist.tar.gz -C /usr/share/nginx/html/diom-frontend --strip-components=1

# 设置权限
sudo chown -R nginx:nginx /usr/share/nginx/html/diom-frontend
sudo chmod -R 755 /usr/share/nginx/html/diom-frontend
```

#### 5.5 配置Nginx

```bash
# SSH到前端服务器
ssh root@frontend-a.company.com

# 复制Nginx配置
sudo cp /path/to/production-deployment/frontend/nginx.conf /etc/nginx/conf.d/diom-frontend.conf

# ⚠️ 修改配置文件中的域名和Gateway地址
sudo vi /etc/nginx/conf.d/diom-frontend.conf
# 修改 server_name: frontend-a.company.com
# 修改 proxy_pass 中的Gateway地址: gateway.company.com:8080

# 测试配置
sudo nginx -t

# 重载Nginx
sudo nginx -s reload
```

---

### 阶段6: 验证和测试 (预计1小时)

#### 6.1 执行健康检查

```bash
# 在开发机器上执行
cd /Users/yanchao/IdeaProjects/diom-workFlow/production-deployment/scripts
./health-check.sh prod
```

#### 6.2 功能测试

```bash
# 1. 访问前端
http://frontend-a.company.com

# 2. 测试登录
# 用户名: admin
# 密码: 123456

# 3. 测试核心功能
# - 用户登录/登出
# - 流程定义查看
# - 流程发起
# - 任务处理
# - 通知查看
# - 流程设计器（超管）

# 4. 测试多前端服务器
http://frontend-b.company.com
http://frontend-c.company.com
```

#### 6.3 性能测试（可选）

```bash
# 使用Apache Bench测试
ab -n 1000 -c 100 http://frontend-a.company.com/

# 使用wrk测试
wrk -t12 -c400 -d30s http://frontend-a.company.com/auth/login \
  -s login.lua
```

---

## 🔄 滚动更新流程

### 更新后端服务

```bash
# 1. 编译新版本
mvn clean package -DskipTests -Pprod

# 2. 备份当前版本
ssh diom@backend.company.com
sudo cp /data/diom-workflow/gateway/diom-gateway.jar \
       /data/backup/diom-workflow/gateway/diom-gateway-$(date +%Y%m%d_%H%M%S).jar

# 3. 停止服务
PID=$(ps aux | grep diom-gateway.jar | grep -v grep | awk '{print $2}')
sudo kill -15 $PID
sleep 5

# 4. 部署新版本
sudo cp /tmp/diom-gateway.jar /data/diom-workflow/gateway/

# 5. 启动服务
cd /data/diom-workflow/gateway
nohup java -Xms512m -Xmx2g -jar diom-gateway.jar --spring.profiles.active=prod > /var/log/diom-workflow/gateway/app.log 2>&1 &

# 6. 验证
sleep 10
curl http://localhost:8080/actuator/health
```

### 更新前端

```bash
# 1. 打包新版本
npm run build

# 2. 上传到服务器（使用deploy-frontend.sh）
cd production-deployment/scripts
./deploy-frontend.sh prod frontend-a

# Nginx会自动使用新版本，无需重启
```

---

## 🆘 回滚流程

### 回滚后端服务

```bash
# SSH到后端服务器
ssh diom@backend.company.com

# 停止当前服务
PID=$(ps aux | grep diom-gateway.jar | grep -v grep | awk '{print $2}')
sudo kill -15 $PID

# 恢复备份版本
cd /data/backup/diom-workflow/gateway
ls -lt  # 查看备份文件
sudo cp diom-gateway-20251115_140000.jar /data/diom-workflow/gateway/diom-gateway.jar

# 重新启动
cd /data/diom-workflow/gateway
nohup java -Xms512m -Xmx2g -jar diom-gateway.jar --spring.profiles.active=prod > /var/log/diom-workflow/gateway/app.log 2>&1 &
```

### 回滚前端

```bash
# SSH到前端服务器
ssh root@frontend-a.company.com

# 恢复备份版本
cd /data/backup/diom-frontend
ls -lt  # 查看备份文件
sudo tar -xzf backup-20251115_140000.tar.gz -C /usr/share/nginx/html/diom-frontend

# 重载Nginx
sudo nginx -s reload
```

---

## 📋 部署后检查清单

### 立即检查 (部署后10分钟内)

- [ ] 所有服务进程正常运行
- [ ] 健康检查端点返回200
- [ ] Nacos显示所有服务已注册
- [ ] 前端页面可正常访问
- [ ] 用户可正常登录
- [ ] 核心功能正常使用

### 短期监控 (部署后1-2小时)

- [ ] 服务器CPU/内存正常
- [ ] 无异常错误日志
- [ ] API响应时间正常
- [ ] 数据库连接正常
- [ ] 无用户反馈问题

### 长期监控 (部署后1-7天)

- [ ] 系统稳定运行
- [ ] 性能指标达标
- [ ] 无重大Bug
- [ ] 用户反馈良好

---

## 📞 紧急联系

**部署出现问题时**:
1. 立即停止部署
2. 执行回滚流程
3. 联系技术负责人
4. 记录问题详情

**紧急联系方式**:
- 技术负责人: [Phone]
- 运维负责人: [Phone]
- DBA: [Phone]

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-15

