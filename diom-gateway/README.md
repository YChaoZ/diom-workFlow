# diom-gateway - 统一网关服务

## 📋 功能概述

diom-gateway 是整个微服务架构的统一入口，提供以下核心功能：

- ✅ **服务路由转发**：基于路径的智能路由，自动转发请求到后端服务
- ✅ **JWT 认证鉴权**：全局 JWT Token 验证，保护后端服务安全
- ✅ **服务发现**：集成 Nacos，自动发现和路由到可用服务实例
- ✅ **负载均衡**：使用 Spring Cloud LoadBalancer 实现客户端负载均衡
- ✅ **跨域配置**：统一处理 CORS 跨域请求
- ✅ **日志记录**：记录所有请求的详细日志，包括耗时、状态码等
- ✅ **异常处理**：统一异常处理和响应格式化
- ✅ **健康检查**：提供 Actuator 端点用于监控和管理

## 🚀 快速开始

### 1. 前置条件

- JDK 8+
- Maven 3.6+
- Nacos 服务（localhost:8848）
- diom-auth-service 已启动（端口 8081）

### 2. 启动服务

```bash
# 编译项目
mvn clean package -DskipTests

# 启动网关
mvn spring-boot:run

# 或使用 jar 包启动
java -jar target/diom-gateway-1.0.0-SNAPSHOT.jar
```

服务启动后访问：
- 网关地址：http://localhost:8080
- 健康检查：http://localhost:8080/actuator/health
- 路由信息：http://localhost:8080/actuator/gateway/routes

### 3. 运行测试

```bash
# 执行完整测试脚本
bash test_gateway.sh
```

## 🔌 路由配置

### 已配置路由

| 路由规则 | 目标服务 | 说明 |
|---------|---------|------|
| `/auth/**` | diom-auth-service | 认证服务（登录、注册、Token管理） |
| `/api/**` | diom-web-service | Web业务服务 |
| `/workflow/**` | diom-workflow-service | 工作流服务 |

### 示例请求

```bash
# 通过网关登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 通过网关验证 Token
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer YOUR_TOKEN"

# 通过网关注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"123456","email":"new@example.com"}'
```

## 🔒 JWT 认证机制

### 白名单路径（无需认证）

以下路径无需提供 Token：

- `/auth/login` - 用户登录
- `/auth/register` - 用户注册
- `/actuator/**` - 健康检查和监控端点
- `/favicon.ico` - 图标文件

### 认证流程

1. **客户端请求** → 携带 JWT Token（Header: `Authorization: Bearer {token}`）
2. **网关验证** → JWT 过滤器验证 Token 有效性
3. **Token 解析** → 提取用户信息（userId, username）
4. **请求转发** → 在请求头中添加用户信息，转发到后端服务
5. **响应返回** → 统一格式化响应返回给客户端

### 后端服务获取用户信息

网关会在请求头中注入以下信息，后端服务可直接使用：

```java
// 在后端服务的 Controller 中获取
String userId = request.getHeader("X-User-Id");
String username = request.getHeader("X-Username");
```

## 📝 配置说明

### application.yml 核心配置

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      # 路由配置
      routes:
        - id: auth-service
          uri: lb://diom-auth-service
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
      
      # 跨域配置
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
            allowCredentials: true

# JWT 配置
jwt:
  secret: your-secret-key-at-least-256-bits
  expiration: 7200
  header: Authorization
  tokenPrefix: "Bearer "
  whitelist:
    - /auth/login
    - /auth/register
    - /actuator/**
```

### bootstrap.yml（Nacos 配置）

```yaml
spring:
  application:
    name: diom-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
```

## 🏗️ 架构设计

### 核心组件

```
diom-gateway/
├── config/
│   ├── GatewayConfig.java        # 网关配置类
│   └── JwtProperties.java        # JWT 配置属性
├── filter/
│   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   └── LoggingFilter.java            # 日志过滤器
├── exception/
│   └── GlobalExceptionHandler.java   # 全局异常处理
├── utils/
│   └── JwtUtil.java                  # JWT 工具类
└── GatewayApplication.java           # 启动类
```

### 过滤器执行顺序

1. **LoggingFilter** (order: -99) - 记录请求日志
2. **JwtAuthenticationFilter** (order: -100) - JWT 认证验证
3. **Spring Cloud Gateway Filters** - 路由转发

## 🔍 监控和管理

### Actuator 端点

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看所有路由
curl http://localhost:8080/actuator/gateway/routes

# 查看路由详情
curl http://localhost:8080/actuator/gateway/routes/{routeId}

# 刷新路由配置
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### 查看 Nacos 注册信息

```bash
# 查看网关在 Nacos 的注册信息
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-gateway
```

## 🧪 测试覆盖

测试脚本 `test_gateway.sh` 包含以下测试场景：

- ✅ 网关健康检查
- ✅ Nacos 服务注册检查
- ✅ 白名单路径访问（无需 Token）
- ✅ 未携带 Token 访问受保护接口（应返回 401）
- ✅ 携带 Token 访问受保护接口（应成功）
- ✅ Token 验证功能
- ✅ Token 刷新功能
- ✅ 用户注册功能
- ✅ 路由配置检查

## 🐛 故障排查

### 1. 503 Service Unavailable

**原因**：后端服务未启动或未注册到 Nacos

**解决方案**：
```bash
# 检查后端服务是否启动
curl http://localhost:8081/actuator/health

# 检查 Nacos 注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-auth-service
```

### 2. 401 Unauthorized

**原因**：Token 无效、已过期或缺失

**解决方案**：
- 确认请求头包含 `Authorization: Bearer {token}`
- 检查 Token 是否过期（默认 7200 秒）
- 重新登录获取新 Token

### 3. 路由不生效

**原因**：路由配置错误或服务名称不匹配

**解决方案**：
```bash
# 查看实际路由配置
curl http://localhost:8080/actuator/gateway/routes

# 检查服务发现
curl http://localhost:8080/actuator/health
```

## 📊 性能优化建议

1. **启用缓存**：对 JWT Token 验证结果进行短期缓存
2. **限流配置**：添加 RequestRateLimiter 过滤器
3. **连接池优化**：调整 Gateway 的 HttpClient 连接池参数
4. **监控告警**：集成 Prometheus + Grafana 监控

## 🔐 安全建议

1. **JWT 密钥管理**：
   - 使用强随机密钥（至少 256 位）
   - 定期轮换密钥
   - 通过环境变量或配置中心管理密钥

2. **HTTPS 启用**：
   - 生产环境强制使用 HTTPS
   - 配置 SSL 证书

3. **限流和熔断**：
   - 添加 Sentinel 或 Resilience4j
   - 防止 DDoS 攻击

4. **IP 白名单**：
   - 对敏感接口添加 IP 白名单限制

## 📚 参考资料

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [JWT 介绍](https://jwt.io/introduction)

## 📝 版本历史

- **v1.0.0** (2025-11-14)
  - ✅ 初始版本发布
  - ✅ 实现基础路由转发
  - ✅ 集成 JWT 认证
  - ✅ 集成 Nacos 服务发现
  - ✅ 实现全局异常处理
  - ✅ 配置 CORS 跨域支持
  - ✅ 添加日志记录功能

## 👥 维护者

- **DIOM Team**

---

**注意**：本文档持续更新中，如有问题请提交 Issue。
