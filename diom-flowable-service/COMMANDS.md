# Flowable 服务常用命令速查表

## 🔧 初始化（首次运行必须）

```bash
# 1. 创建数据库
mysql -uroot -p1qaz2wsx -e "CREATE DATABASE IF NOT EXISTS diom_flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入 Flowable 引擎表
mysql -uroot -p1qaz2wsx diom_flowable < flowable-6.8.0-mysql-create.sql

# 3. 验证表是否创建成功
mysql -uroot -p1qaz2wsx diom_flowable -e "SHOW TABLES;" | wc -l
# 应该显示 180+ 张表
```

## 🚀 启动服务

```bash
# 方式1: 使用启动脚本（推荐）
cd diom-flowable-service
./start-flowable.sh

# 方式2: 直接运行 JAR
cd diom-flowable-service/start
java -jar target/start-1.0.0-SNAPSHOT.jar

# 方式3: 使用 Maven（开发环境）
cd diom-flowable-service/start
mvn spring-boot:run
```

## 🧪 测试服务

```bash
# 运行完整测试脚本
cd diom-flowable-service
./test-flowable.sh

# 或手动测试
# 1. 健康检查
curl http://localhost:8086/actuator/health

# 2. 查询流程定义
curl http://localhost:8086/flowable/definitions

# 3. 启动流程
curl -X POST http://localhost:8086/flowable/start/simple-process \
  -H "Content-Type: application/json" -d '{}'

# 4. 查询任务
curl "http://localhost:8086/flowable/tasks?assignee=admin"

# 5. 完成任务（替换 TASK_ID）
curl -X POST http://localhost:8086/flowable/tasks/TASK_ID/complete \
  -H "Content-Type: application/json" -d '{"outcome":"approved"}'
```

## 🔍 日志查看

```bash
# 实时查看日志
tail -f start/workflow.log

# 查看最近 100 行
tail -100 start/workflow.log

# 查看错误日志
grep -i error start/workflow.log

# 查看部署成功的流程
grep "Deployed process definition" start/workflow.log
```

## 🛠️ 编译和打包

```bash
# 编译
cd diom-flowable-service
mvn clean compile -DskipTests

# 打包
mvn clean package -DskipTests

# 安装到本地仓库
mvn clean install -DskipTests
```

## 🔄 重启服务

```bash
# 1. 查找进程ID
ps aux | grep flowable | grep -v grep

# 2. 停止服务
kill -15 <PID>

# 3. 重新启动
./start-flowable.sh
```

## 🗑️ 清理

```bash
# 删除编译产物
mvn clean

# 删除日志文件
rm -f start/workflow.log

# 清空数据库（危险！）
mysql -uroot -p1qaz2wsx -e "DROP DATABASE diom_flowable;"
```

## 📊 数据库查询

```bash
# 查看流程定义
mysql -uroot -p1qaz2wsx diom_flowable -e \
  "SELECT KEY_, NAME_, VERSION_ FROM ACT_RE_PROCDEF;"

# 查看运行中的流程实例
mysql -uroot -p1qaz2wsx diom_flowable -e \
  "SELECT PROC_INST_ID_, BUSINESS_KEY_, START_TIME_ FROM ACT_RU_EXECUTION WHERE PARENT_ID_ IS NULL;"

# 查看待办任务
mysql -uroot -p1qaz2wsx diom_flowable -e \
  "SELECT ID_, NAME_, ASSIGNEE_, CREATE_TIME_ FROM ACT_RU_TASK;"

# 查看历史流程实例
mysql -uroot -p1qaz2wsx diom_flowable -e \
  "SELECT PROC_INST_ID_, START_TIME_, END_TIME_, DELETE_REASON_ FROM ACT_HI_PROCINST LIMIT 10;"
```

## 🔧 故障排查

```bash
# 检查端口是否被占用
lsof -i :8086

# 检查 MySQL 连接
mysql -h localhost -P 3306 -uroot -p1qaz2wsx -e "SELECT 1;"

# 检查 Nacos 连接
curl http://localhost:8848/nacos/v1/console/health

# 检查服务是否注册到 Nacos
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-flowable-service&groupName=HTTP_GROUP"

# 查看 Maven 依赖树
mvn dependency:tree

# 验证 BPMN 文件
xmllint --noout start/src/main/resources/processes/*.bpmn
```

## 📦 Docker 部署（可选）

```bash
# 构建 Docker 镜像
docker build -t diom-flowable-service:1.0.0 .

# 运行容器
docker run -d \
  --name diom-flowable \
  -p 8086:8086 \
  -e DB_HOST=mysql \
  -e DB_PASSWORD=yourpassword \
  -e NACOS_SERVER_ADDR=nacos:8848 \
  diom-flowable-service:1.0.0

# 查看日志
docker logs -f diom-flowable

# 停止容器
docker stop diom-flowable

# 删除容器
docker rm diom-flowable
```

## 🌐 Gateway 配置

```bash
# 检查 Gateway 路由配置
curl http://localhost:8080/actuator/gateway/routes | jq '.[] | select(.route_id=="flowable-service")'

# 通过 Gateway 访问 Flowable 服务
curl http://localhost:8080/flowable/definitions
```

## 📌 常用端口

| 服务 | 端口 | 描述 |
|------|------|------|
| Flowable 服务 | 8086 | 新的工作流服务 |
| Camunda 服务 | 8085 | 原工作流服务 |
| Gateway | 8080 | API 网关 |
| Nacos | 8848 | 服务注册中心 |
| MySQL | 3306 | 数据库 |

## 💡 提示

1. **首次启动前**，务必先初始化数据库
2. **确保 Nacos 运行**，否则服务无法注册
3. **查看日志**排查问题，大部分问题可以从日志中找到原因
4. **使用测试脚本**快速验证服务是否正常
5. **参考 QUICKSTART.md** 获取更详细的说明

