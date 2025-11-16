# diom-web-service - Web 业务服务层

## 📋 项目简介

diom-web-service 是微服务架构中的 Web 业务服务层，采用**阿里 COLA 架构**，负责：

- ✅ 接收来自网关的 HTTP 请求
- ✅ 提供 RESTful API 接口
- ✅ 业务逻辑编排和处理
- ✅ 调用后端 RPC 服务（Dubbo）
- ✅ 实现领域模型和业务规则

## 🏗️ COLA 架构设计

本项目采用**多模块**设计，严格遵循 COLA (Clean Object-Oriented and Layered Architecture) 架构：

```
diom-web-service/
├── web-adapter/           # 适配器层：REST Controller，接收 HTTP 请求
├── web-app/               # 应用层：应用服务，业务编排
├── web-domain/            # 领域层：领域模型和业务逻辑
├── web-infrastructure/    # 基础设施层：Dubbo Consumer，外部服务调用
└── web-start/             # 启动模块：Spring Boot 启动类和配置
```

### 各层职责

| 层次 | 模块 | 职责 | 示例 |
|------|------|------|------|
| **Adapter** | web-adapter | 接收外部请求，转换为业务调用 | UserController |
| **App** | web-app | 业务编排，调用 Domain 和 Infrastructure | UserAppService |
| **Domain** | web-domain | 领域模型，业务规则 | UserInfo, UserGateway |
| **Infrastructure** | web-infrastructure | 外部服务调用，技术实现 | UserGatewayImpl（Dubbo Consumer） |
| **Start** | web-start | 启动配置，依赖组装 | WebApplication |

## 🚀 快速开始

### 1. 前置条件

- JDK 8+
- Maven 3.6+
- Nacos 服务（localhost:8848）
- diom-gateway 已启动（端口 8080）

### 2. 编译项目

```bash
# 在项目根目录
cd diom-web-service
mvn clean install -DskipTests
```

### 3. 启动服务

```bash
# 启动 Web 服务
cd web-start
mvn spring-boot:run

# 或使用 jar 包启动
java -jar target/web-start-1.0.0-SNAPSHOT.jar
```

服务启动后访问：
- Web 服务：http://localhost:8082
- 健康检查：http://localhost:8082/actuator/health

### 4. 运行测试

```bash
# 执行完整测试脚本
bash test_web_service.sh
```

## 📡 API 接口

### 用户信息接口

#### 1. 根据ID查询用户
```bash
# 直接访问
curl http://localhost:8082/user/1

# 通过网关访问（需要 Token）
curl http://localhost:8080/api/user/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 2. 根据用户名查询
```bash
# 直接访问
curl http://localhost:8082/user/username/admin

# 通过网关访问
curl http://localhost:8080/api/user/username/admin \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 3. 获取当前登录用户信息
```bash
# 通过网关访问（从 Header 获取用户 ID）
curl http://localhost:8080/api/user/info \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "138****0001",
    "status": 1,
    "statusDesc": "正常",
    "createTime": "2025-11-14T12:00:00",
    "updateTime": "2025-11-14T12:00:00"
  },
  "timestamp": 1699929600000
}
```

## 🔧 配置说明

### application.yml 核心配置

```yaml
server:
  port: 8082

spring:
  application:
    name: diom-web-service

# Dubbo 配置（待启用）
# dubbo:
#   application:
#     name: diom-web-service
#   registry:
#     address: nacos://localhost:8848
#   consumer:
#     timeout: 3000
#     retries: 2
```

### bootstrap.yml（Nacos 配置）

```yaml
spring:
  application:
    name: diom-web-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
```

## 🎯 COLA 架构示例

### Adapter 层 - Controller
```java
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserAppService userAppService;
    
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserInfo userInfo = userAppService.getUserInfo(id);
        UserDTO userDTO = convertToDTO(userInfo);
        return Result.success(userDTO);
    }
}
```

### App 层 - Application Service
```java
@Service
public class UserAppService {
    @Autowired
    private UserGateway userGateway;
    
    public UserInfo getUserInfo(Long userId) {
        // 业务逻辑编排
        UserInfo userInfo = userGateway.getUserById(userId);
        // 可以添加更多业务逻辑...
        return userInfo;
    }
}
```

### Domain 层 - Domain Model & Gateway
```java
// 领域模型
@Data
@Builder
public class UserInfo {
    private Long id;
    private String username;
    private String nickname;
    // ...
}

// 防腐层接口（由 Infrastructure 实现）
public interface UserGateway {
    UserInfo getUserById(Long userId);
}
```

### Infrastructure 层 - Gateway Implementation
```java
@Component
public class UserGatewayImpl implements UserGateway {
    // TODO: 后续改为 Dubbo Consumer
    // @Reference
    // private UserService userService;
    
    @Override
    public UserInfo getUserById(Long userId) {
        // 当前使用模拟数据
        // 后续改为：return userService.getUserById(userId);
        return UserInfo.builder()
                .id(userId)
                .username("user_" + userId)
                .build();
    }
}
```

## 📊 架构流程

```
[网关 8080]
    ↓ HTTP请求
    ↓ /api/user/1
    ↓ (JWT认证，注入用户信息到Header)
    ↓
[Adapter 层]
    ↓ UserController
    ↓ 接收请求，转换参数
    ↓
[App 层]
    ↓ UserAppService
    ↓ 业务逻辑编排
    ↓
[Domain 层]
    ↓ UserInfo (领域模型)
    ↓ UserGateway (防腐层接口)
    ↓
[Infrastructure 层]
    ↓ UserGatewayImpl
    ↓ 调用外部服务
    ↓ (当前模拟数据，后续Dubbo RPC)
    ↓
[返回结果]
    ↑ UserInfo
    ↑ 转换为 UserDTO
    ↑ 封装为 Result<UserDTO>
    ↑
[客户端]
```

## 🧪 测试覆盖

测试脚本 `test_web_service.sh` 包含以下测试场景：

- ✅ Web 服务健康检查
- ✅ Nacos 服务注册验证
- ✅ 直接访问 Web 服务接口
  - 根据 ID 查询用户
  - 根据用户名查询用户
- ✅ 通过网关访问（带 JWT 认证）
  - 查询用户信息
  - 查询当前登录用户
- ✅ COLA 架构完整性验证

## 📦 模块依赖关系

```
web-start
  ├── web-adapter
  │    └── web-app
  ├── web-app
  │    ├── web-domain
  │    └── web-infrastructure
  ├── web-infrastructure
  │    └── web-domain
  └── Spring Boot / Nacos / Dubbo
```

## 🔄 与其他服务的交互

### 1. 网关 (diom-gateway)

```
Client → Gateway (8080) → Web Service (8082)
         ↓ JWT 认证
         ↓ 路由转发: /api/** → diom-web-service
         ↓ 注入 Header: X-User-Id, X-Username
```

### 2. 认证服务 (diom-auth-service)

```
Web Service → (未来可能的直接调用)
   ↓ Token 验证
   ↓ 用户信息查询
```

### 3. 工作流服务 (待开发)

```
Web Service → Dubbo Consumer
   ↓ RPC 调用
   ↓ 流程定义、启动、查询
```

## 🚧 待完善功能

### 短期（1-2 周）
- [ ] 启用 Dubbo 3.x（当前已注释）
- [ ] 实现真实的 RPC 服务调用
- [ ] 添加更多业务接口
- [ ] 完善异常处理

### 中期（1 个月）
- [ ] 集成分布式事务（Seata）
- [ ] 添加接口限流和熔断
- [ ] 完善单元测试和集成测试
- [ ] API 文档自动生成（Swagger）

### 长期（3 个月）
- [ ] 性能优化和压测
- [ ] 缓存策略（Redis）
- [ ] 消息队列集成（RocketMQ）
- [ ] 监控和链路追踪

## 🐛 常见问题

### 1. 服务启动失败

**问题**：Dubbo 初始化失败  
**原因**：Dubbo 版本与 Java 版本不兼容  
**解决**：当前已暂时注释 Dubbo 配置，待后续启用

### 2. 网关访问 404

**问题**：通过网关访问返回 404  
**原因**：路由配置错误或服务未注册  
**解决**：
```bash
# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-web-service

# 检查网关路由
curl http://localhost:8080/actuator/gateway/routes
```

### 3. 用户信息获取失败

**问题**：通过 Header 获取用户信息失败  
**原因**：网关未注入用户信息  
**解决**：确保通过网关访问，并携带有效 Token

## 📚 技术栈

- **Spring Boot**: 2.4.11
- **Spring Cloud Alibaba**: 2021.1
- **Dubbo**: 3.0.15（暂时注释）
- **Nacos**: 2.x（服务发现和配置中心）
- **COLA**: 阿里 COLA 架构
- **Lombok**: 1.18.30

## 📖 参考资料

- [COLA 架构官方文档](https://github.com/alibaba/COLA)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Dubbo 官方文档](https://dubbo.apache.org/zh/)
- [Nacos 官方文档](https://nacos.io/zh-cn/)

## 📝 版本历史

- **v1.0.0** (2025-11-14)
  - ✅ 实现 COLA 架构（adapter、app、domain、infrastructure、start）
  - ✅ 集成 Nacos 服务发现和配置中心
  - ✅ 实现用户信息查询接口
  - ✅ 支持通过网关访问（JWT 认证）
  - ✅ 完整测试脚本和文档

## 👥 维护者

- **DIOM Team**

---

**注意**：当前使用模拟数据，后续将改为真实的 Dubbo RPC 调用。
