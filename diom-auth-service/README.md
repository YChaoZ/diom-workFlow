# diom-auth-service

## 项目说明

认证服务，提供用户登录、JWT 生成和管理功能。

## 核心功能

- **用户登录**: 用户名密码登录，生成 JWT Token
- **Token 刷新**: 刷新过期的 Token
- **用户注册**: 用户注册功能（可选）
- **密码加密**: BCrypt 密码加密
- **Token 管理**: JWT Token 生成、验证、解析

## 端口配置

- 默认端口: **8081**

## 数据库表

### sys_user（用户表）

```sql
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` INT(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

## API 文档

### 用户登录

```
POST /login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
```

### Token 刷新

```
POST /refresh
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 用户注册

```
POST /register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "email": "test@example.com"
}
```

## 启动方式

### 1. 初始化数据库

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/sql/init.sql
```

### 2. 启动服务

```bash
mvn spring-boot:run
```

### 3. 验证启动

```bash
curl http://localhost:8081/health
```

## 默认用户

- 用户名: admin，密码: 123456
- 用户名: test，密码: 123456

## 完整功能

✅ **已实现的功能**:

1. **用户管理**
   - User 实体（sys_user 表）
   - UserMapper（MyBatis Plus）

2. **认证功能**
   - 用户登录（AuthService.login）
   - 用户注册（AuthService.register）
   - Token 刷新（AuthService.refreshToken）
   - Token 验证（AuthService.validateToken）

3. **JWT 服务**
   - Token 生成（JwtTokenService.generateToken）
   - Token 解析（JwtTokenService.parseToken）
   - Token 验证（JwtTokenService.validateToken）

4. **安全配置**
   - Spring Security 配置
   - BCrypt 密码加密
   - 无状态会话管理

5. **异常处理**
   - 全局异常处理器
   - 参数校验
   - 业务异常处理

6. **API 接口**
   - POST /login - 用户登录
   - POST /register - 用户注册
   - POST /refresh - 刷新 Token
   - GET /validate - 验证 Token
   - GET /health - 健康检查

## 详细测试

参见 [API_TEST.md](API_TEST.md)

