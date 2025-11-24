# Docker 容器配置说明

## 概述

本项目使用 Docker Compose 管理 MySQL 和 Redis 两个独立容器。

## 容器信息

### MySQL 容器
- **容器名**: `diom-mysql`
- **镜像**: `mysql:8.0`
- **端口映射**: `3306:3306`
- **root 密码**: `1qaz2wsx`
- **默认数据库**: `diom_flowable`
- **字符集**: `utf8mb4`
- **时区**: `Asia/Shanghai`
- **数据持久化**: `mysql-data` volume

### Redis 容器
- **容器名**: `diom-redis`
- **镜像**: `redis:7.2-alpine`
- **端口映射**: `6379:6379`
- **密码**: `1qaz2wsx`
- **持久化模式**: AOF (appendonly yes)
- **数据持久化**: `redis-data` volume

## 常用命令

### 启动所有容器
```bash
docker-compose up -d
```

### 停止所有容器
```bash
docker-compose down
```

### 停止并删除所有数据（慎用！）
```bash
docker-compose down -v
```

### 查看容器状态
```bash
docker-compose ps
```

### 查看容器日志
```bash
# 查看所有容器日志
docker-compose logs -f

# 查看 MySQL 日志
docker-compose logs -f mysql

# 查看 Redis 日志
docker-compose logs -f redis
```

### 重启容器
```bash
# 重启所有容器
docker-compose restart

# 重启 MySQL
docker-compose restart mysql

# 重启 Redis
docker-compose restart redis
```

## 连接信息

### MySQL 连接
```bash
# 命令行连接
mysql -h localhost -P 3306 -u root -p1qaz2wsx

# JDBC URL
jdbc:mysql://localhost:3306/diom_flowable?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
```

### Redis 连接
```bash
# 命令行连接
redis-cli -h localhost -p 6379 -a 1qaz2wsx

# Spring Boot 配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=1qaz2wsx
```

## 健康检查

两个容器都配置了健康检查，可以通过以下命令查看：

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

如果状态显示 `(healthy)` 表示容器运行正常。

## 故障排查

### 端口被占用
如果提示端口被占用，检查是否有其他服务在使用 3306 或 6379 端口：
```bash
lsof -i :3306
lsof -i :6379
```

### 容器无法启动
查看详细日志：
```bash
docker-compose logs mysql
docker-compose logs redis
```

### 重建容器
如果需要完全重建容器：
```bash
docker-compose down -v
docker-compose up -d
```

## 生产环境注意事项

⚠️ **生产环境部署时请务必修改以下内容**：

1. 修改 MySQL root 密码
2. 修改 Redis 密码
3. 配置适当的资源限制
4. 使用外部持久化存储
5. 配置备份策略
6. 启用 SSL/TLS 连接

## 数据备份

### MySQL 备份
```bash
docker exec diom-mysql mysqldump -u root -p1qaz2wsx diom_flowable > backup_$(date +%Y%m%d).sql
```

### Redis 备份
```bash
docker exec diom-redis redis-cli -a 1qaz2wsx SAVE
docker cp diom-redis:/data/dump.rdb ./redis_backup_$(date +%Y%m%d).rdb
```

## 数据恢复

### MySQL 恢复
```bash
docker exec -i diom-mysql mysql -u root -p1qaz2wsx diom_flowable < backup.sql
```

### Redis 恢复
```bash
docker cp redis_backup.rdb diom-redis:/data/dump.rdb
docker-compose restart redis
```

