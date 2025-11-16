# 服务发现问题解决方案

## 🎯 问题背景

在使用 **Dubbo + Spring Cloud Gateway + Nacos** 的架构中，遇到了一个关键问题：

每个服务同时注册了两个端口到 Nacos：
- **HTTP 端口**（如 8081）：用于 HTTP 接口调用
- **Dubbo 端口**（如 20880）：用于 RPC 调用

当 Spring Cloud Gateway 使用 `lb://service-name` 进行服务发现时，LoadBalancer 会随机从 Nacos 获取实例，有 50% 的概率会选择 Dubbo 端口，导致 HTTP 请求失败：
```
reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response
```

## ❌ 尝试过的方案

###方案 1: 自定义 LoadBalancerConfig（失败）
**尝试**: 创建自定义 `ServiceInstanceListSupplier`，根据 metadata 过滤实例
**问题**: 
- Spring Cloud LoadBalancer 3.0.x 的 API 复杂，难以正确实现
- `ServiceInstanceListSupplier.builder()` 方法签名不匹配
- 编译错误频繁

### 方案 2: GlobalFilter 二次验证（失败）
**尝试**: 创建 `GlobalFilter`，在 LoadBalancer 选择实例后验证端口
**问题**:
- LoadBalancer 的实例选择在 Filter 之前完成
- Filter 无法改变已经选定的实例

### 方案 3: 固定 IP 地址（不推荐）
**尝试**: 在 Gateway 路由配置中使用固定 IP
```yaml
uri: http://192.168.123.105:8081
```
**问题**:
- 依赖特定 IP，不利于扩展
- 无法实现动态服务发现和负载均衡

## ✅ 最终解决方案: Nacos Group 隔离

### 核心思路

**使用 Nacos Group 特性，将 HTTP 服务和 Dubbo 服务注册到不同的 Group：**
- **HTTP_GROUP**: 用于 HTTP 服务注册和发现
- **DEFAULT_GROUP**: 用于 Dubbo RPC 服务注册和发现

这样，Gateway 只从 `HTTP_GROUP` 发现实例，Dubbo 只从 `DEFAULT_GROUP` 发现实例，两者完全隔离。

### 配置步骤

#### 1. 为所有业务服务配置 HTTP_GROUP

**`bootstrap.yml`**:
```yaml
spring:
  application:
    name: diom-auth-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        group: HTTP_GROUP  # HTTP服务使用独立Group
```

**`application.yml`**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        group: HTTP_GROUP  # 与bootstrap.yml保持一致
```

**注意**: 需要在 `bootstrap.yml` 和 `application.yml` 中都配置 `group`，因为 `application.yml` 会覆盖 `bootstrap.yml` 的部分配置。

#### 2. Gateway 配置 HTTP_GROUP

**`diom-gateway/src/main/resources/application.yml`**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        group: HTTP_GROUP  # 只发现HTTP_GROUP的服务
    gateway:
      discovery:
        locator:
          enabled: false  # 关闭自动路由，使用手动配置
      routes:
        - id: auth-service
          uri: lb://diom-auth-service  # 使用服务发现
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
```

#### 3. Dubbo 配置保持DEFAULT_GROUP

Dubbo 服务默认使用 `DEFAULT_GROUP`，无需额外配置：
```yaml
dubbo:
  registry:
    address: nacos://localhost:8848
    parameters:
      namespace: ${NACOS_NAMESPACE:}
      # group默认为DEFAULT_GROUP
```

### 验证结果

**查询 HTTP_GROUP 中的服务**:
```bash
curl -s 'http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-auth-service&groupName=HTTP_GROUP'
```

**结果**:
```
✅ HTTP_GROUP服务列表:
diom-auth-service: 1 instances -> 192.168.123.105:8081
diom-web-service: 1 instances -> 192.168.123.105:8082
diom-workflow-service: 1 instances -> 192.168.123.105:8083
```

**查询 DEFAULT_GROUP 中的服务**:
```bash
curl -s 'http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-auth-service'
```

**结果**:
```
✅ DEFAULT_GROUP服务列表（Dubbo端口）:
192.168.64.1:20880
192.168.64.1:20881
192.168.64.1:20882
```

### 功能测试

**登录测试**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```
**结果**: ✅ `code: 200, message: 登录成功`

**用户信息测试**:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/auth/userinfo
```
**结果**: ✅ `code: 200, message: 获取用户信息成功`

**工作流列表测试**:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/workflow/definitions
```
**结果**: ✅ `code: 200, message: 操作成功`

## 🎉 方案优势

1. **完全隔离**: HTTP和Dubbo服务在Nacos中完全分离，无相互干扰
2. **自动发现**: Gateway可以自动发现所有HTTP服务，支持负载均衡
3. **易于扩展**: 新增服务实例时，只需配置HTTP_GROUP即可自动加入
4. **零侵入**: 不需要修改Gateway或LoadBalancer的底层实现
5. **配置简单**: 只需在配置文件中添加`group: HTTP_GROUP`

## 📝 配置清单

### 需要配置的文件

| 服务 | 文件路径 | 配置项 |
|------|---------|--------|
| auth-service | `diom-auth-service/src/main/resources/bootstrap.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| auth-service | `diom-auth-service/src/main/resources/application.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| web-service | `diom-web-service/web-start/src/main/resources/bootstrap.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| web-service | `diom-web-service/web-start/src/main/resources/application.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| workflow-service | `diom-workflow-service/start/src/main/resources/bootstrap.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| workflow-service | `diom-workflow-service/start/src/main/resources/application.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |
| gateway | `diom-gateway/src/main/resources/application.yml` | `spring.cloud.nacos.discovery.group: HTTP_GROUP` |

## 🔧 故障排查

### 问题 1: 服务没有注册到 HTTP_GROUP

**症状**: 查询 HTTP_GROUP 返回 0 实例

**解决方案**:
1. 检查 `bootstrap.yml` 和 `application.yml` 中都有 `group: HTTP_GROUP` 配置
2. 重启服务，清理编译缓存：`mvn clean`
3. 查看启动日志，确认 Nacos 注册信息

### 问题 2: Gateway 找不到服务

**症状**: `503 Unable to find instance for service-name`

**解决方案**:
1. 确认 Gateway 配置了 `group: HTTP_GROUP`
2. 确认业务服务已成功注册到 HTTP_GROUP
3. 重启 Gateway

### 问题 3: Dubbo RPC调用失败

**症状**: Dubbo Consumer 无法发现 Provider

**解决方案**:
Dubbo 配置中不要设置 `group`，保持默认的 `DEFAULT_GROUP`

## ✨ 总结

通过 **Nacos Group 隔离方案**，成功实现了：
- ✅ HTTP 服务自动发现
- ✅ 避免 LoadBalancer 选择到 Dubbo 端口
- ✅ 支持负载均衡和服务扩展
- ✅ 配置简单，易于维护

这是在 **Dubbo + Spring Cloud Gateway + Nacos** 架构下，解决服务发现冲突的最佳实践！

