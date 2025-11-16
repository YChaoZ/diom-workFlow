FROM openjdk:8-jre-alpine

LABEL maintainer="diom"

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用目录
WORKDIR /app

# 复制 jar 包
COPY target/diom-gateway-1.0.0-SNAPSHOT.jar /app/diom-gateway.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/diom-gateway.jar"]

