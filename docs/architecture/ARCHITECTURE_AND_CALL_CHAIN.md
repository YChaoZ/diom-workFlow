# 系统架构与调用链说明

## 📊 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                           前端层 (Vue.js)                            │
│                     http://localhost:5173                            │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               │ HTTP/HTTPS (8080)
                               │ 所有请求统一入口
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    API网关 (Spring Cloud Gateway)                    │
│                         diom-gateway:8080                            │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  功能：                                                         │ │
│  │  1. 统一入口 - 所有外部请求的唯一入口                          │ │
│  │  2. 路由转发 - 根据路径转发到不同的后端服务                    │ │
│  │  3. JWT认证 - 验证Token，拦截未授权请求                        │ │
│  │  4. CORS处理 - 跨域请求处理                                     │ │
│  │  5. 负载均衡 - 通过Nacos服务发现，自动负载均衡                 │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                       │
│  路由规则:                                                            │
│  /auth/**       →  lb://diom-auth-service     (认证服务)            │
│  /api/**        →  lb://diom-web-service      (Web层API)            │
│  /user/**       →  lb://diom-web-service      (用户管理)            │
│  /workflow/**   →  lb://diom-workflow-service (工作流服务)          │
└───────┬───────────────┬───────────────┬───────────────┬─────────────┘
        │               │               │               │
        │ HTTP:8081    │ HTTP:8082    │ HTTP:8082    │ HTTP:8083
        │              │              │              │
        ▼              ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  认证服务     │ │   Web层服务   │ │   Web层服务   │ │  工作流服务   │
│ auth-service │ │  web-service │ │  web-service │ │workflow-svc  │
│   :8081      │ │    :8082     │ │    :8082     │ │   :8083      │
├──────────────┤ ├──────────────┤ ├──────────────┤ ├──────────────┤
│ HTTP接口:    │ │ HTTP接口:    │ │ HTTP接口:    │ │ HTTP接口:    │
│ - 登录       │ │ - 用户管理   │ │ - 用户管理   │ │ - 流程定义   │
│ - 注册       │ │ - 其他业务   │ │ - 其他业务   │ │ - 流程实例   │
│ - Token验证  │ │              │ │              │ │ - 任务管理   │
│ - 用户信息   │ │ Dubbo Consumer │ │ Dubbo Consumer │ │              │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────────────┘
       │                │                │
       │                │                │
       │  MySQL         │  Dubbo RPC     │  Dubbo RPC
       │  :3306         │  :20880-21000  │  :20880-21000
       │                │                │
       ▼                └────────┬───────┘
┌──────────────┐                │
│    MySQL     │                │
│  数据库       │◄───────────────┘
│  :3306       │
├──────────────┤
│ 表:          │
│ - sys_user   │
│ - act_*      │
│   (Camunda)  │
└──────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                  服务注册与发现 - Nacos                              │
│                    localhost:8848                                    │
│  ┌────────────────────────────┬────────────────────────────────┐   │
│  │     HTTP_GROUP             │      DEFAULT_GROUP             │   │
│  │  (供Gateway服务发现使用)    │    (供Dubbo RPC使用)           │   │
│  ├────────────────────────────┼────────────────────────────────┤   │
│  │ diom-auth-service:8081     │ Dubbo Provider:20880          │   │
│  │ diom-web-service:8082      │ Dubbo Provider:20881          │   │
│  │ diom-workflow-service:8083 │ Dubbo Provider:20882          │   │
│  └────────────────────────────┴────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

## 🔄 详细调用链说明

### 场景1：用户登录流程

```
1. 前端发起登录请求
   Vue.js (5173)
   └─→ POST http://localhost:8080/auth/login
       {username: "admin", password: "123456"}

2. Gateway接收并路由
   Gateway (8080)
   ├─→ 匹配路由规则: /auth/** → lb://diom-auth-service
   ├─→ JWT白名单检查: /auth/login 在白名单中，放行
   ├─→ 从Nacos HTTP_GROUP查询 diom-auth-service 实例
   └─→ 转发到: http://192.168.123.105:8081/login

3. 认证服务处理
   Auth-Service (8081)
   ├─→ 接收请求: POST /login
   ├─→ 查询数据库验证密码
   ├─→ 生成JWT Token
   └─→ 返回: {code:200, data:{token:"...", user:{...}}}

4. Gateway返回给前端
   Gateway (8080)
   └─→ 原样返回响应

5. 前端存储Token
   Vue.js
   └─→ localStorage.setItem('token', response.data.token)
```

### 场景2：获取用户信息（需要认证）

```
1. 前端请求用户信息
   Vue.js (5173)
   └─→ GET http://localhost:8080/auth/userinfo
       Headers: Authorization: Bearer eyJhbGc...

2. Gateway JWT认证
   Gateway (8080)
   ├─→ 匹配路由: /auth/** → lb://diom-auth-service
   ├─→ JWT过滤器: 提取Token并验证
   ├─→ Token有效 → 放行
   └─→ 转发到: http://192.168.123.105:8081/userinfo

3. 认证服务返回用户信息
   Auth-Service (8081)
   ├─→ 从Token中解析用户ID
   ├─→ 查询数据库获取用户详情
   └─→ 返回: {code:200, data:{id:1, username:"admin",...}}

4. Gateway返回给前端
```

### 场景3：Web层调用其他服务（Dubbo RPC）

```
1. 前端请求业务数据
   Vue.js (5173)
   └─→ GET http://localhost:8080/user/list
       Headers: Authorization: Bearer eyJhbGc...

2. Gateway JWT认证并路由
   Gateway (8080)
   ├─→ 匹配路由: /user/** → lb://diom-web-service
   ├─→ JWT验证通过
   └─→ 转发到: http://192.168.123.105:8082/user/list

3. Web层服务 (Dubbo Consumer)
   Web-Service (8082)
   ├─→ 接收HTTP请求: GET /user/list
   ├─→ 需要调用其他服务获取数据
   ├─→ 通过Dubbo调用: userService.getUserList()
   │   ├─→ Nacos DEFAULT_GROUP 查找 UserService Provider
   │   └─→ Dubbo RPC调用: dubbo://192.168.64.1:20880
   ├─→ 接收Dubbo响应
   └─→ 返回HTTP响应: {code:200, data:[...]}

4. Gateway返回给前端
```

### 场景4：工作流操作

```
1. 前端请求工作流定义列表
   Vue.js (5173)
   └─→ GET http://localhost:8080/workflow/definitions
       Headers: Authorization: Bearer eyJhbGc...

2. Gateway JWT认证并路由
   Gateway (8080)
   ├─→ 匹配路由: /workflow/** → lb://diom-workflow-service
   ├─→ JWT验证通过
   └─→ 转发到: http://192.168.123.105:8083/workflow/definitions

3. 工作流服务处理
   Workflow-Service (8083)
   ├─→ 接收HTTP请求
   ├─→ 调用Camunda ProcessEngine查询流程定义
   ├─→ 从MySQL查询Camunda表 (act_re_procdef)
   └─→ 返回: {code:200, data:[{key:"...", name:"..."}]}

4. Gateway返回给前端
```

## 🎯 Gateway的核心作用

### 1. **统一入口 (Single Entry Point)**
```
没有Gateway的情况:
前端需要知道所有服务的地址
├─→ http://192.168.123.105:8081  (认证)
├─→ http://192.168.123.105:8082  (Web层)
└─→ http://192.168.123.105:8083  (工作流)

有Gateway的情况:
前端只需要知道一个地址
└─→ http://localhost:8080  (统一网关)
    └─→ Gateway内部路由到各个服务
```

**好处**：
- 前端不需要知道后端服务的具体地址
- 后端服务可以随意扩展、迁移，前端无感知
- 便于部署和维护

### 2. **统一认证 (Centralized Authentication)**
```
┌──────────────────────────────────────────────────┐
│  所有请求都经过Gateway的JWT过滤器                │
│                                                  │
│  1. 白名单请求 (/auth/login, /auth/register)    │
│     └─→ 直接放行                                │
│                                                  │
│  2. 需要认证的请求                               │
│     ├─→ 提取Token                               │
│     ├─→ 验证Token有效性                         │
│     ├─→ Token有效 → 放行                        │
│     └─→ Token无效 → 返回401 Unauthorized        │
└──────────────────────────────────────────────────┘
```

**好处**：
- 后端服务无需关心认证逻辑
- 认证逻辑集中管理，易于维护
- 安全性更高

### 3. **动态路由与负载均衡 (Dynamic Routing & Load Balancing)**
```
假设 diom-web-service 有3个实例:
├─→ 192.168.123.105:8082  (实例1)
├─→ 192.168.123.106:8082  (实例2)
└─→ 192.168.123.107:8082  (实例3)

Gateway通过 lb://diom-web-service 自动负载均衡:
请求1 → 实例1
请求2 → 实例2
请求3 → 实例3
请求4 → 实例1 (轮询)
```

**好处**：
- 自动发现新增/下线的服务实例
- 自动负载均衡，提高系统吞吐量
- 无需手动配置负载均衡器

### 4. **跨域处理 (CORS)**
```
前端: http://localhost:5173
后端: http://localhost:8080

Gateway统一配置CORS:
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Authorization, Content-Type
```

**好处**：
- 前端无需担心跨域问题
- 后端服务无需配置CORS

### 5. **请求过滤与转换 (Request Filtering & Transformation)**
```
Gateway可以:
├─→ 修改请求头 (添加、删除、修改)
├─→ 修改请求路径 (StripPrefix)
├─→ 限流 (Rate Limiting)
├─→ 日志记录 (Logging)
└─→ 监控统计 (Metrics)
```

## 📡 两种通信方式对比

### HTTP通信 (Gateway → 服务)
```
┌─────────────┐
│   特点      │
├─────────────┤
│ 协议: HTTP  │
│ 端口: 808x  │
│ 同步调用    │
│ RESTful API │
│ 易于调试    │
└─────────────┘

使用场景:
✅ 前端 → Gateway → 后端服务
✅ 外部系统调用
✅ 需要跨语言调用
```

### Dubbo RPC通信 (服务间调用)
```
┌─────────────────┐
│   特点          │
├─────────────────┤
│ 协议: Dubbo     │
│ 端口: 2088x     │
│ 高性能          │
│ 二进制序列化    │
│ 服务治理功能强  │
└─────────────────┘

使用场景:
✅ 内部服务间调用
✅ 需要高性能
✅ 复杂的服务治理需求
```

## 🔐 Nacos Group隔离的作用

```
┌─────────────────────────────────────────────────────┐
│                    Nacos注册中心                     │
├──────────────────────────┬──────────────────────────┤
│      HTTP_GROUP          │      DEFAULT_GROUP       │
│   (HTTP服务端口)          │     (Dubbo RPC端口)       │
├──────────────────────────┼──────────────────────────┤
│ diom-auth-service:8081   │ Dubbo Provider:20880    │
│ diom-web-service:8082    │ Dubbo Provider:20881    │
│ diom-workflow-service:   │ Dubbo Provider:20882    │
│                   8083   │                          │
└──────┬───────────────────┴─────────┬────────────────┘
       │                             │
       │ Gateway只查询HTTP_GROUP     │ Dubbo只查询DEFAULT_GROUP
       │ 避免选择到Dubbo端口          │
       │                             │
       ▼                             ▼
   正确路由HTTP请求              正确路由RPC调用
```

**为什么需要Group隔离？**

在引入Dubbo之前的问题：
```
❌ 问题场景:
每个服务同时注册两个端口到Nacos:
├─→ HTTP端口 (8081)
└─→ Dubbo端口 (20880)

Gateway通过 lb://diom-auth-service 查询时:
├─→ 可能返回: 8081  ✅ 正确
└─→ 也可能返回: 20880  ❌ 错误！

结果: 50%概率HTTP请求被路由到Dubbo端口，导致失败
```

引入Group隔离后：
```
✅ 解决方案:
Gateway配置: group: HTTP_GROUP
└─→ 只查询HTTP_GROUP，永远返回HTTP端口

Dubbo配置: group: DEFAULT_GROUP (默认)
└─→ 只查询DEFAULT_GROUP，永远返回Dubbo端口

结果: HTTP和Dubbo完全隔离，互不干扰！
```

## 📋 总结：完整的请求流程

```
用户点击"登录"按钮
     ↓
Vue.js发送HTTP请求
     ↓
请求到达Gateway (8080)
     ↓
Gateway进行JWT认证
     ↓
从Nacos HTTP_GROUP查询服务实例
     ↓
负载均衡选择一个实例
     ↓
转发HTTP请求到后端服务 (808x)
     ↓
后端服务处理请求
  ├─→ 查询数据库
  ├─→ 调用其他服务 (Dubbo RPC)
  └─→ 返回响应
     ↓
Gateway返回响应给前端
     ↓
Vue.js渲染页面
```

## 🎯 核心优势总结

1. **前端视角**: 只需要对接一个Gateway地址，简单！
2. **Gateway视角**: 统一管理认证、路由、负载均衡
3. **后端服务视角**: 只需关注业务逻辑，无需关心认证、跨域
4. **运维视角**: 服务可随意扩展，前端无感知
5. **安全视角**: 统一认证入口，安全性高

这就是我们的**微服务架构 + API网关 + 服务治理**的完整设计！🎉

