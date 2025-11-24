#!/bin/bash

echo "=========================================="
echo "  启动 Flowable 工作流服务"
echo "=========================================="

# 设置环境变量
export JAVA_HOME=${JAVA_HOME:-/usr/bin/java}
export SPRING_PROFILES_ACTIVE=dev

# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=diom_flowable
export DB_USERNAME=root
export DB_PASSWORD=1qaz2wsx

# Nacos 配置
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=

# 进入项目目录
cd "$(dirname "$0")/start"

# 检查 JAR 文件是否存在
JAR_FILE="target/start-1.0.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR 文件不存在，请先执行: mvn clean package"
    exit 1
fi

echo "✅ 找到 JAR 文件: $JAR_FILE"
echo "🚀 正在启动服务..."
echo ""

# 启动服务
java -jar $JAR_FILE \
    --server.port=8086 \
    --spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true \
    --spring.datasource.username=${DB_USERNAME} \
    --spring.datasource.password=${DB_PASSWORD} \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_SERVER_ADDR} \
    --spring.cloud.nacos.config.server-addr=${NACOS_SERVER_ADDR}

