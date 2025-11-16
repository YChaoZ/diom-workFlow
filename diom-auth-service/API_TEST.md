# diom-auth-service API 测试指南

## 前置条件

1. ✅ MySQL 已启动
2. ✅ 数据库已初始化（执行 `src/main/resources/sql/init.sql`）
3. ✅ Nacos 已启动（如果配置了 Nacos）
4. ✅ diom-common 已编译安装

## 启动服务

```bash
cd diom-auth-service
mvn spring-boot:run
```

服务启动在 **8081** 端口。

---

## API 测试

### 1. 健康检查

```bash
curl http://localhost:8081/health
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "Auth Service is running",
  "timestamp": 1705201234567
}
```

---

### 2. 用户注册

```bash
curl -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "123456",
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null,
  "timestamp": 1705201234567
}
```

**参数验证规则**:
- username: 4-20位，只能包含字母、数字、下划线
- password: 6-20位
- nickname: 最多50位（可选）
- email: 邮箱格式（可选）
- phone: 11位手机号（可选）

---

### 3. 用户登录

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJuaWNrbmFtZSI6IueugeeQhuWRmCIsInN1YiI6IjEiLCJpYXQiOjE3MDUyMDEyMzQsImV4cCI6MTcwNTIwODQzNH0.xxx",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "email": "admin@diom.com"
    }
  },
  "timestamp": 1705201234567
}
```

**默认用户**:
- 用户名: admin，密码: 123456
- 用户名: test，密码: 123456

---

### 4. Token 验证

```bash
curl http://localhost:8081/validate \
  -H "Authorization: Bearer <your-token>"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "Token 有效",
  "data": {
    "userId": 1
  },
  "timestamp": 1705201234567
}
```

---

### 5. Token 刷新

```bash
curl -X POST http://localhost:8081/refresh \
  -H "Authorization: Bearer <your-token>"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "Token 刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
  },
  "timestamp": 1705201234567
}
```

---

## 错误场景测试

### 1. 登录失败 - 用户不存在

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "notexist",
    "password": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 600,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1705201234567
}
```

---

### 2. 登录失败 - 密码错误

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

**预期响应**:
```json
{
  "code": 600,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1705201234567
}
```

---

### 3. 注册失败 - 用户名已存在

```bash
curl -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 600,
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1705201234567
}
```

---

### 4. 参数校验失败

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "password": ""
  }'
```

**预期响应**:
```json
{
  "code": 400,
  "message": "用户名不能为空; 密码不能为空; ",
  "data": null,
  "timestamp": 1705201234567
}
```

---

## 与网关集成测试

假设网关运行在 8080 端口：

### 1. 通过网关登录

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 2. 通过网关访问受保护的接口

```bash
# 先登录获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | jq -r '.data.token')

# 使用 token 访问受保护接口
curl http://localhost:8080/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

---

## 使用 Postman 测试

### 1. 导入环境变量

创建环境，设置：
- `base_url`: http://localhost:8081
- `token`: （登录后自动设置）

### 2. 登录请求

**POST** `{{base_url}}/login`

Body (JSON):
```json
{
  "username": "admin",
  "password": "123456"
}
```

**Tests** (自动保存 token):
```javascript
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
}
```

### 3. 后续请求使用 token

在 Headers 中添加：
```
Authorization: Bearer {{token}}
```

---

## 数据库验证

### 查看用户

```sql
SELECT * FROM sys_user;
```

### 查看加密密码

```sql
SELECT id, username, password FROM sys_user WHERE username = 'admin';
```

---

## 常见问题

### Q1: 启动失败 - 数据库连接错误

**错误**: `Communications link failure`

**解决**:
1. 检查 MySQL 是否启动
2. 检查 `application.yml` 中的数据库配置
3. 确认数据库 `diom_workflow` 已创建

### Q2: 登录失败 - 密码不匹配

**错误**: `用户名或密码错误`

**解决**:
1. 运行测试生成正确的 BCrypt 密码
2. 更新数据库中的密码
```bash
cd diom-auth-service
mvn test -Dtest=PasswordEncoderTest#generatePassword
```

### Q3: Token 验证失败

**错误**: `Token 无效或已过期`

**解决**:
1. 检查 JWT Secret 是否一致（网关和认证服务）
2. 检查 Token 是否过期（默认 2 小时）
3. 确认 Token 格式：`Bearer <token>`

---

## 性能测试

### 使用 Apache Bench

```bash
# 登录接口压测
ab -n 1000 -c 10 -p login.json -T application/json http://localhost:8081/login
```

**login.json**:
```json
{"username":"admin","password":"123456"}
```

### 使用 wrk

```bash
wrk -t4 -c100 -d30s --latency http://localhost:8081/health
```

---

## 日志查看

```bash
# 应用日志
tail -f logs/diom-auth-service.log

# 错误日志
tail -f logs/diom-auth-service-error.log
```

---

## 监控

### Actuator 端点

```bash
# 健康检查
curl http://localhost:8081/actuator/health

# 应用信息
curl http://localhost:8081/actuator/info
```

---

**测试完成后，请确认所有 API 都能正常工作！** ✅

