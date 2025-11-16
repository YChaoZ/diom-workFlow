# 下一个会话开发指南 - 工作流服务

## 🎯 开发目标

在新会话中完整实现 **diom-workflow-service**（工作流服务）

---

## 📋 给 AI 的完整指令

请在新会话的**第一条消息**中复制以下内容：

```
我需要开发 diom-workflow-service 工作流服务，这是一个微服务项目的一部分。

【项目背景】
- 技术栈：Spring Boot 2.4.11 + Java 8 + Camunda 7.15 + Dubbo 3.0.15
- 架构：阿里 COLA 架构（adapter、app、domain、infrastructure、start）
- 注册中心：Nacos (localhost:8848)
- 数据库：MySQL (localhost:3306)，数据库名：diom_workflow
- 项目路径：/Users/yanchao/IdeaProjects/diom-workFlow/diom-workflow-service

【已有基础】
1. ✅ diom-auth-service (8081端口) - 认证服务已完成
2. ✅ diom-gateway (8080端口) - 网关已完成
3. ✅ diom-web-service (8082端口) - Web业务服务已完成
4. ✅ 项目骨架已存在（pom.xml 和模块结构已建立）

【开发要求】
1. 集成 Camunda 7.15
   - Camunda Engine（流程引擎核心）
   - Camunda REST API（流程管理接口）
   - 注意：Camunda Web UI 独立部署，不需要集成到此服务

2. 实现完整的 COLA 架构
   - workflow-adapter：REST Controller，提供 HTTP 接口
   - workflow-app：应用服务，业务编排
   - workflow-domain：领域模型，流程相关实体
   - workflow-infrastructure：Camunda Engine 集成
   - start：启动模块

3. 核心功能实现
   - 流程定义管理（部署、查询、删除）
   - 流程实例管理（启动、查询、终止、挂起）
   - 任务管理（查询、完成、委派、认领）
   - 历史记录查询

4. Dubbo Provider 配置
   - 暂时注释（与 web-service 保持一致）
   - 预留接口定义

5. Nacos 集成
   - 服务注册和发现
   - 配置中心

6. 数据库配置
   - MySQL 连接配置
   - Camunda 表自动初始化
   - 数据库账户：root / 1qaz2wsx

7. 测试和文档
   - 完整的测试脚本
   - README 文档
   - 实施报告

【技术细节】
- 端口：8083
- 服务名：diom-workflow-service
- Camunda 版本：7.15.0
- MyBatis Plus 版本：3.5.5
- Dubbo 版本：3.0.15（暂时注释）
- Lombok 版本：1.18.30

【重要提示】
1. 参考 diom-auth-service 和 diom-web-service 的实现风格
2. 确保版本兼容性（与已完成服务保持一致）
3. 提供完整的测试脚本验证所有功能
4. 包含示例 BPMN 流程文件
5. 所有响应使用中文

请开始实现！
```

---

## 📊 当前项目状态

### 已完成服务

| 服务 | 端口 | 状态 | 功能 |
|------|------|------|------|
| **diom-auth-service** | 8081 | ✅ 完成 | JWT认证、用户管理 |
| **diom-gateway** | 8080 | ✅ 完成 | 路由、认证、负载均衡 |
| **diom-web-service** | 8082 | ✅ 完成 | Web业务层（COLA） |
| **diom-workflow-service** | 8083 | ⏳ 待开发 | 工作流引擎 |

### 系统架构图

```
┌──────────┐
│  客户端   │
└────┬─────┘
     │
     ↓
┌─────────────────────────┐
│ Gateway (8080) ✅       │
│ - JWT 认证              │
│ - 服务路由              │
│ - 负载均衡              │
└────┬────────────────────┘
     │
     ├──────────┬──────────┬──────────┐
     ↓          ↓          ↓          ↓
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌────────┐
│  Auth   │ │   Web   │ │Workflow │ │ Nacos  │
│ Service │ │ Service │ │ Service │ │ :8848  │
│ :8081✅ │ │ :8082✅ │ │ :8083⏳ │ │   ✅   │
└─────────┘ └─────────┘ └─────────┘ └────────┘
                           ↓
                    【Camunda Engine】
                    - 流程引擎
                    - 任务管理
                    - 历史记录
```

---

## 🔧 技术栈版本清单

请在新会话中使用这些版本（已验证兼容）：

```xml
<spring-boot.version>2.4.11</spring-boot.version>
<spring-cloud.version>2020.0.5</spring-cloud.version>
<spring-cloud-alibaba.version>2021.1</spring-cloud-alibaba.version>
<camunda.version>7.15.0</camunda.version>
<dubbo.version>3.0.15</dubbo.version>
<mybatis-plus.version>3.5.5</mybatis-plus.version>
<mysql.version>8.0.23</mysql.version>
<lombok.version>1.18.30</lombok.version>
```

---

## 📁 参考文件位置

在新会话中，可以参考这些已完成的文件：

### Auth Service（认证服务参考）
```
/Users/yanchao/IdeaProjects/diom-workFlow/diom-auth-service/
├── src/main/resources/application.yml    # 配置参考
├── src/main/resources/bootstrap.yml      # Nacos配置参考
└── README.md                              # 文档参考
```

### Web Service（COLA架构参考）
```
/Users/yanchao/IdeaProjects/diom-workFlow/diom-web-service/
├── web-adapter/                           # Adapter层参考
├── web-app/                               # App层参考
├── web-domain/                            # Domain层参考
├── web-infrastructure/                    # Infrastructure层参考
├── web-start/                             # 启动模块参考
└── README.md                              # 架构说明
```

### Gateway（网关配置参考）
```
/Users/yanchao/IdeaProjects/diom-workFlow/diom-gateway/
├── src/main/resources/application.yml    # 路由配置参考
└── README.md                              # 网关说明
```

---

## 🎯 核心开发任务清单

在新会话中需要完成（按优先级）：

### 阶段 1：基础配置（2小时）
- [ ] 更新各模块的 pom.xml
- [ ] 配置 Camunda Engine
- [ ] 配置数据库连接
- [ ] 配置 Nacos 注册
- [ ] 创建启动类和配置文件

### 阶段 2：Camunda 集成（3小时）
- [ ] 集成 Camunda Engine
- [ ] 配置流程引擎
- [ ] 配置历史记录
- [ ] 配置作业执行器
- [ ] 测试引擎启动

### 阶段 3：COLA 架构实现（3小时）
- [ ] Domain 层：流程实体、任务实体
- [ ] Infrastructure 层：Camunda Service封装
- [ ] App 层：流程管理、任务管理服务
- [ ] Adapter 层：REST Controller

### 阶段 4：核心功能（3小时）
- [ ] 流程定义管理（部署、查询）
- [ ] 流程实例管理（启动、查询）
- [ ] 任务管理（查询、完成）
- [ ] 历史记录查询

### 阶段 5：测试和文档（2小时）
- [ ] 创建示例 BPMN 流程
- [ ] 编写测试脚本
- [ ] 完善 README
- [ ] 编写实施报告

**预计总时间：13小时**

---

## 📝 数据库准备

在新会话开始前，确保：

```sql
-- 创建工作流数据库
CREATE DATABASE IF NOT EXISTS diom_workflow 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Camunda 会自动创建所需的表（约20+张）
```

---

## 🔍 Camunda 核心概念

在新会话中需要实现的核心概念：

### 1. Process Definition（流程定义）
- BPMN 2.0 XML 文件
- 流程的蓝图
- 可以有多个版本

### 2. Process Instance（流程实例）
- 流程定义的运行实例
- 有生命周期（running, suspended, completed）
- 包含变量和状态

### 3. Task（任务）
- User Task：需要人工处理
- Service Task：自动执行
- 任务可以分配给用户/组

### 4. History（历史记录）
- 流程实例历史
- 任务历史
- 变量历史

---

## 🎓 示例 BPMN 流程

可以使用这个简单的请假流程作为示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
  <bpmn:process id="leave-process" name="请假流程">
    <bpmn:startEvent id="start"/>
    <bpmn:userTask id="submit" name="提交请假"/>
    <bpmn:userTask id="approve" name="经理审批"/>
    <bpmn:endEvent id="end"/>
    
    <bpmn:sequenceFlow sourceRef="start" targetRef="submit"/>
    <bpmn:sequenceFlow sourceRef="submit" targetRef="approve"/>
    <bpmn:sequenceFlow sourceRef="approve" targetRef="end"/>
  </bpmn:process>
</bpmn:definitions>
```

---

## ✅ 验收标准

新会话完成后，应该能够：

- [ ] 服务成功启动（8083端口）
- [ ] Nacos 注册成功
- [ ] 数据库 Camunda 表创建成功
- [ ] 可以部署 BPMN 流程
- [ ] 可以启动流程实例
- [ ] 可以查询和完成任务
- [ ] 通过网关访问（需要JWT认证）
- [ ] 所有测试脚本通过

---

## 🚀 启动顺序

新会话开发完成后，完整系统的启动顺序：

```bash
# 1. 启动 Nacos
docker ps | grep nacos

# 2. 启动 MySQL
docker ps | grep mysql

# 3. 启动 Auth Service
cd diom-auth-service
mvn spring-boot:run

# 4. 启动 Gateway
cd diom-gateway
mvn spring-boot:run

# 5. 启动 Web Service
cd diom-web-service/web-start
mvn spring-boot:run

# 6. 启动 Workflow Service（新开发）
cd diom-workflow-service/start
mvn spring-boot:run
```

---

## 📚 参考资源

### Camunda 官方文档
- [Camunda 7.15 文档](https://docs.camunda.org/manual/7.15/)
- [Spring Boot Integration](https://docs.camunda.org/manual/7.15/user-guide/spring-boot-integration/)
- [REST API 文档](https://docs.camunda.org/manual/7.15/reference/rest/)

### BPMN 2.0
- [BPMN 2.0 规范](https://www.omg.org/spec/BPMN/2.0/)
- [Camunda BPMN 教程](https://camunda.com/bpmn/)

### 已完成项目
- 参考 `diom-web-service` 的 COLA 架构
- 参考 `diom-auth-service` 的数据库配置
- 参考 `diom-gateway` 的路由配置

---

## 🎉 当前会话成果总结

在这个会话中，我们成功完成了：

### 1. diom-auth-service ✅
- 完整的 JWT 认证
- 用户注册和登录
- MySQL + Redis 集成
- Nacos 服务注册
- 完整测试脚本

### 2. diom-gateway ✅
- 智能路由转发
- JWT Token 验证
- 服务发现和负载均衡
- 全局跨域配置
- 日志记录和异常处理

### 3. diom-web-service ✅
- 完整的 COLA 架构（5个模块）
- Nacos 注册和服务发现
- 网关集成（Header 用户信息注入）
- 业务接口实现
- 完整文档和测试

**这已经是一个完整可用的微服务系统！** 🎊

---

## 💡 给 AI 的额外提示

在新会话中开发时，请注意：

1. **保持一致性**：
   - 使用相同的版本号
   - 遵循相同的代码风格
   - 使用相同的配置模式

2. **参考已有代码**：
   - 不要重复造轮子
   - 复用 common 模块
   - 学习已完成服务的结构

3. **测试驱动**：
   - 先编译通过
   - 再启动成功
   - 最后功能测试

4. **文档完善**：
   - 详细的 README
   - 实施报告
   - API 文档

---

## 📞 如何开始新会话

1. **打开新的对话**

2. **复制上面的完整指令**（在"给 AI 的完整指令"部分）

3. **发送给 AI**

4. **AI 会自动开始开发**

5. **期待您的工作流服务！** 🚀

---

**祝开发顺利！下个会话见！** 👋

