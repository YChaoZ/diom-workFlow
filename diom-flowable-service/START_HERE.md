# 🚀 Flowable 服务 - 3 步启动

## 步骤 1: 创建空数据库

```sql
-- 连接 MySQL（使用你的实际密码）
mysql -uroot -p

-- 执行以下 SQL
DROP DATABASE IF EXISTS diom_flowable;
CREATE DATABASE diom_flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

⚠️ **重要**: 只需创建空数据库，**不要**导入任何建表脚本！Flowable 会自动建表。

## 步骤 2: 确保 Nacos 运行

```bash
# 检查 Nacos 是否运行
curl http://localhost:8848/nacos/

# 如果未运行，启动 Nacos
cd $NACOS_HOME/bin
sh startup.sh -m standalone
```

## 步骤 3: 启动 Flowable 服务

```bash
cd diom-flowable-service

# 如果还未打包，先打包
mvn clean package -DskipTests

# 启动服务
./start-flowable.sh
```

## ✅ 验证服务

在另一个终端运行：

```bash
# 1. 健康检查
curl http://localhost:8086/actuator/health

# 2. 查询流程定义
curl http://localhost:8086/flowable/definitions

# 3. 启动一个简单流程
curl -X POST http://localhost:8086/flowable/start/simple-process \
  -H "Content-Type: application/json" \
  -d '{}'
```

## 📊 查看数据库表

```sql
USE diom_flowable;
SHOW TABLES;
```

应该看到约 50 张表，包括：
- `ACT_RE_*` - 流程定义仓库表
- `ACT_RU_*` - 运行时数据表
- `ACT_HI_*` - 历史数据表
- `FLW_RU_*` - Flowable 运行时表

## 🐛 遇到问题？

### 问题: 端口 8086 已被占用

```bash
# 查找占用端口的进程
lsof -i :8086

# 或修改端口
export SERVER_PORT=8087
./start-flowable.sh
```

### 问题: 无法连接 MySQL

检查：
1. MySQL 是否运行
2. 用户名密码是否正确（默认 root/1qaz2wsx）
3. 数据库 `diom_flowable` 是否存在

### 问题: 无法连接 Nacos

检查：
1. Nacos 是否运行在 8848 端口
2. 防火墙规则
3. 配置的 Group 是否为 `HTTP_GROUP`

## 📚 更多文档

- [完整启动指南](./QUICKSTART.md)
- [常用命令](./COMMANDS.md)
- [API 文档](./API.md)
- [问题排查](./DEPENDENCY_FIX.md)

---

**提示**: 首次启动会自动创建所有数据库表，可能需要 10-30 秒。请耐心等待！

