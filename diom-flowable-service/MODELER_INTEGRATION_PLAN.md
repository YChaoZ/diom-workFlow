# Flowable Modeler 集成开发计划

## 📋 项目目标

将 Flowable 官方原生 Modeler 集成到 `diom-flowable-service`，替代现有的 bpmn.js 前端设计器，并与现有 Auth 系统深度整合。

## 🎯 核心需求

1. ✅ **后端集成**：在 `diom-flowable-service` 中集成 Flowable Modeler REST API
2. ✅ **前端集成**：在 `diom-frontend` 中嵌入 Flowable Modeler UI
3. ✅ **Auth 整合**：与 `diom-auth-service` 的 JWT 认证体系整合
4. ✅ **权限控制**：基于现有 RBAC 的流程设计权限管理
5. ❌ **剔除 bpmn.js**：移除前端项目中的 bpmn.js 相关代码

---

## 📐 架构设计

### 整体架构流程

```
用户浏览器
    ↓
Gateway (8083)
    ↓ /flowable/modeler/** 
diom-flowable-service (8086)
    ├─ Flowable Modeler REST API
    ├─ Flowable Modeler UI (静态资源)
    └─ 与 diom-auth-service 集成
         ↓ Dubbo/Feign
diom-auth-service (8081)
    └─ JWT 验证、用户信息
```

### 关键组件

| 组件 | 作用 | 技术栈 |
|------|------|--------|
| **Flowable Modeler Backend** | 提供流程设计 REST API | flowable-ui-modeler-rest + logic |
| **Flowable Modeler Frontend** | 可视化流程设计器 | Angular + flowable-ui-modeler-app |
| **Auth Integration** | JWT 认证、用户信息映射 | Spring Security + Flowable IDM |
| **Gateway Routing** | 统一路由和认证入口 | Spring Cloud Gateway |

---

## 🔧 技术实施方案

### 一、后端集成（diom-flowable-service）

#### 1.1 添加 Flowable Modeler 依赖

**文件**：`diom-flowable-service/start/pom.xml`

```xml
<dependencies>
    <!-- 现有依赖保持不变 -->
    
    <!-- Flowable Modeler REST API -->
    <dependency>
        <groupId>org.flowable</groupId>
        <artifactId>flowable-ui-modeler-rest</artifactId>
        <version>${flowable.version}</version>
    </dependency>
    
    <!-- Flowable Modeler Logic -->
    <dependency>
        <groupId>org.flowable</groupId>
        <artifactId>flowable-ui-modeler-logic</artifactId>
        <version>${flowable.version}</version>
    </dependency>
    
    <!-- Flowable Modeler 前端资源（可选，如果想直接嵌入）-->
    <dependency>
        <groupId>org.flowable</groupId>
        <artifactId>flowable-ui-modeler-app</artifactId>
        <version>${flowable.version}</version>
    </dependency>
    
    <!-- JSON 处理（Modeler 依赖）-->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-json-org</artifactId>
    </dependency>
</dependencies>
```

#### 1.2 配置 Flowable Modeler

**文件**：`diom-flowable-service/start/src/main/resources/application.yml`

```yaml
flowable:
  # 现有配置保持不变
  
  # Modeler 配置
  modeler:
    app:
      # 部署 API 地址（Modeler 保存流程后，通过此 API 部署到引擎）
      deployment-api-url: http://localhost:8086/flowable-rest/service/repository/deployments
      # REST API 地址
      rest-enabled: true
      # 启用 LDAP（我们不使用，用自己的 Auth）
      ldap-enabled: false
      # 数据源（使用主数据源）
      datasource: default
  
  # REST API 配置
  rest:
    app:
      # 启用认证
      authentication-mode: verify-privilege
      # CORS 配置（如果前端和后端分离）
      cors:
        enabled: true
        allowed-origins: "*"
        allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
        allowed-headers: "*"
        allow-credentials: true

# Web 资源映射（如果使用 flowable-ui-modeler-app）
spring:
  resources:
    static-locations:
      - classpath:/static/
      - classpath:/public/
      - classpath:/resources/
      - classpath:/META-INF/resources/
```

#### 1.3 自定义 Modeler 安全配置

**文件**：`diom-flowable-service/start/src/main/java/com/diom/flowable/config/ModelerSecurityConfig.java`

```java
package com.diom.flowable.config;

import com.diom.flowable.security.FlowableUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Flowable Modeler 安全配置
 * 与现有 Auth 系统整合
 */
@Configuration
@EnableWebSecurity
@Order(1)  // 优先级高于默认配置
public class ModelerSecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private FlowableUserDetailsService flowableUserDetailsService;
    
    @Bean
    public SecurityFilterChain modelerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // 仅匹配 Modeler 路径
            .requestMatchers()
                .antMatchers("/flowable-modeler/**", "/app/**", "/modeler-app/**")
            .and()
            
            // 禁用 CSRF（前后端分离）
            .csrf().disable()
            
            // 配置授权规则
            .authorizeRequests()
                // Modeler 静态资源公开
                .antMatchers("/flowable-modeler/app/**", "/flowable-modeler/editor-app/**").permitAll()
                .antMatchers("/flowable-modeler/scripts/**", "/flowable-modeler/styles/**").permitAll()
                .antMatchers("/flowable-modeler/i18n/**", "/flowable-modeler/images/**").permitAll()
                
                // Modeler API 需要认证
                .antMatchers("/app/rest/**", "/modeler-app/rest/**").authenticated()
                
                // 其他请求需要 PROCESS_DESIGNER 权限
                .anyRequest().hasAuthority("PROCESS_DESIGNER")
            .and()
            
            // 添加 JWT 认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置用户服务
            .userDetailsService(flowableUserDetailsService);
        
        return http.build();
    }
}
```

#### 1.4 实现 Flowable 用户服务（与 Auth 集成）

**文件**：`diom-flowable-service/start/src/main/java/com/diom/flowable/security/FlowableUserDetailsService.java`

```java
package com.diom.flowable.security;

import com.diom.auth.api.UserService;
import com.diom.auth.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Flowable 用户服务
 * 从 diom-auth-service 获取用户信息
 */
@Slf4j
@Service
public class FlowableUserDetailsService implements UserDetailsService {
    
    @DubboReference
    private UserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user for Flowable Modeler: {}", username);
        
        // 从 Auth 服务获取用户信息
        UserDTO userDTO = userService.getUserByUsername(username);
        if (userDTO == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        // 转换权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userDTO.getRoles() != null) {
            userDTO.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role));
            });
        }
        
        // 添加 Modeler 访问权限（可以基于角色判断）
        if (hasModelerAccess(userDTO)) {
            authorities.add(new SimpleGrantedAuthority("PROCESS_DESIGNER"));
        }
        
        return new User(
            userDTO.getUsername(),
            userDTO.getPassword(), // 注意：JWT 认证时不需要密码
            authorities
        );
    }
    
    /**
     * 判断用户是否有 Modeler 访问权限
     */
    private boolean hasModelerAccess(UserDTO user) {
        // 示例：管理员或有特定角色的用户可以访问
        return user.getRoles() != null && 
               (user.getRoles().contains("ROLE_ADMIN") || 
                user.getRoles().contains("ROLE_PROCESS_DESIGNER"));
    }
}
```

#### 1.5 JWT 认证过滤器（复用现有的）

**文件**：`diom-flowable-service/start/src/main/java/com/diom/flowable/security/JwtAuthenticationFilter.java`

```java
package com.diom.flowable.security;

import com.diom.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头或 Gateway 注入的 Header 中获取用户信息
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private FlowableUserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain chain) throws ServletException, IOException {
        
        // 1. 从 Gateway 注入的 Header 获取用户信息（推荐方式）
        String username = request.getHeader("X-Username");
        String userId = request.getHeader("X-User-Id");
        
        // 2. 如果 Gateway 没有注入，尝试从 JWT Token 解析
        if (username == null) {
            String token = extractToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameFromToken(token);
            }
        }
        
        // 3. 加载用户信息并设置到 SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Authentication for user: {}", username);
                
            } catch (Exception e) {
                log.error("Failed to set user authentication", e);
            }
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * 从请求中提取 JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### 1.6 Flowable Modeler 用户信息提供者

**文件**：`diom-flowable-service/start/src/main/java/com/diom/flowable/config/FlowableModelerUserProvider.java`

```java
package com.diom.flowable.config;

import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.modeler.security.ModelerUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Flowable Modeler 用户信息提供者
 * 将 Spring Security 的用户信息转换为 Flowable Modeler 需要的格式
 */
@Component
public class FlowableModelerUserProvider {
    
    /**
     * 获取当前登录用户的 Modeler 用户信息
     */
    public ModelerUser getCurrentModelerUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 创建 ModelerUser 对象
            ModelerUser modelerUser = new ModelerUser();
            modelerUser.setId(userDetails.getUsername()); // 使用 username 作为 ID
            modelerUser.setEmail(userDetails.getUsername() + "@diom.com"); // 可以从 UserDTO 获取真实邮箱
            modelerUser.setFirstName(userDetails.getUsername());
            modelerUser.setLastName("");
            modelerUser.setFullName(userDetails.getUsername());
            
            // 设置权限（检查是否有设计权限）
            boolean hasDesignPrivilege = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "PROCESS_DESIGNER".equals(auth.getAuthority()) || 
                                 "ROLE_ADMIN".equals(auth.getAuthority()));
            
            if (hasDesignPrivilege) {
                modelerUser.setPrivileges(SecurityUtils.PRIVILEGE_ACCESS_MODELER);
            }
            
            return modelerUser;
        }
        
        return null;
    }
}
```

---

### 二、Gateway 路由配置

**文件**：`diom-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Flowable Modeler 路由
        - id: flowable-modeler
          uri: lb://diom-flowable-service
          predicates:
            - Path=/flowable-modeler/**,/app/**,/modeler-app/**
          filters:
            - StripPrefix=0
            # JWT 认证过滤器会自动注入 X-Username 和 X-User-Id
        
        # Flowable REST API 路由
        - id: flowable-rest
          uri: lb://diom-flowable-service
          predicates:
            - Path=/flowable-rest/**
          filters:
            - StripPrefix=0
        
        # 现有的 Flowable 服务路由（保持不变）
        - id: flowable-service
          uri: lb://diom-flowable-service
          predicates:
            - Path=/flowable/**
          filters:
            - StripPrefix=0
```

---

### 三、前端集成（diom-frontend）

#### 3.1 创建 Modeler 嵌入页面

**文件**：`diom-frontend/src/views/ProcessModeler.vue`

```vue
<template>
  <div class="process-modeler-container">
    <!-- 顶部工具栏（可选）-->
    <div class="modeler-toolbar">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/workflow' }">工作流管理</el-breadcrumb-item>
        <el-breadcrumb-item>流程设计器</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="toolbar-actions">
        <el-button @click="refreshModeler" icon="el-icon-refresh">刷新</el-button>
        <el-button @click="openInNewTab" icon="el-icon-full-screen">全屏打开</el-button>
      </div>
    </div>
    
    <!-- Flowable Modeler iframe -->
    <div class="modeler-iframe-wrapper">
      <iframe
        ref="modelerIframe"
        :src="modelerUrl"
        class="modeler-iframe"
        frameborder="0"
        @load="onModelerLoad"
      ></iframe>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth';

export default {
  name: 'ProcessModeler',
  data() {
    return {
      modelerUrl: '',
      loading: true
    };
  },
  created() {
    this.initModelerUrl();
  },
  methods: {
    /**
     * 初始化 Modeler URL
     */
    initModelerUrl() {
      const token = getToken();
      const baseUrl = process.env.VUE_APP_API_BASE_URL || '';
      
      // 如果有流程 ID，打开编辑页面；否则打开流程列表
      const processId = this.$route.query.modelId;
      
      if (processId) {
        // 编辑模式
        this.modelerUrl = `${baseUrl}/flowable-modeler/index.html#/editor/${processId}`;
      } else {
        // 流程列表模式
        this.modelerUrl = `${baseUrl}/flowable-modeler/index.html#/processes`;
      }
      
      // 注入 Token 到 URL（如果 Modeler 支持）或通过 postMessage
      // this.modelerUrl += `?token=${token}`;
    },
    
    /**
     * Modeler 加载完成
     */
    onModelerLoad() {
      this.loading = false;
      
      // 可以通过 postMessage 与 iframe 通信
      const token = getToken();
      this.$refs.modelerIframe.contentWindow.postMessage({
        type: 'AUTH_TOKEN',
        token: token
      }, '*');
    },
    
    /**
     * 刷新 Modeler
     */
    refreshModeler() {
      this.$refs.modelerIframe.contentWindow.location.reload();
    },
    
    /**
     * 在新标签页打开
     */
    openInNewTab() {
      window.open(this.modelerUrl, '_blank');
    }
  }
};
</script>

<style scoped>
.process-modeler-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.modeler-toolbar {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modeler-iframe-wrapper {
  flex: 1;
  overflow: hidden;
}

.modeler-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}
</style>
```

#### 3.2 更新路由配置

**文件**：`diom-frontend/src/router/index.js`

```javascript
import ProcessModeler from '@/views/ProcessModeler.vue';

const routes = [
  // ... 现有路由 ...
  
  {
    path: '/workflow/modeler',
    name: 'ProcessModeler',
    component: ProcessModeler,
    meta: {
      title: '流程设计器',
      requiresAuth: true,
      permissions: ['PROCESS_DESIGNER']
    }
  }
];
```

#### 3.3 移除 bpmn.js 相关代码

**需要删除的文件/代码：**

1. `diom-frontend/src/components/BpmnModeler.vue`（如果存在）
2. `package.json` 中的 bpmn.js 依赖：
   ```json
   // 删除这些依赖
   "bpmn-js": "^x.x.x",
   "bpmn-js-properties-panel": "^x.x.x",
   ```
3. 相关的 import 语句和组件引用

**执行命令：**

```bash
cd diom-frontend
npm uninstall bpmn-js bpmn-js-properties-panel
npm install  # 重新安装依赖
```

---

### 四、数据库配置

Flowable Modeler 需要以下额外的数据库表（会自动创建）：

- `ACT_DE_MODEL` - 流程模型表
- `ACT_DE_MODEL_RELATION` - 模型关系表
- `ACT_DE_MODEL_HISTORY` - 模型历史表

**无需手动建表**，启动时 Flowable 会自动创建（确保 `flowable.database-schema-update=true`）

---

### 五、权限配置

#### 5.1 在 Auth 服务中添加流程设计权限

**文件**：`diom-auth-service/src/main/resources/data.sql`（示例）

```sql
-- 添加流程设计器角色
INSERT INTO sys_role (role_name, role_code, description) 
VALUES ('流程设计师', 'ROLE_PROCESS_DESIGNER', '可以设计和发布流程');

-- 添加流程设计权限
INSERT INTO sys_permission (permission_name, permission_code, resource_type) 
VALUES ('流程设计', 'PROCESS_DESIGNER', 'MENU');

-- 关联角色和权限
INSERT INTO role_permission (role_id, permission_id) 
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'ROLE_PROCESS_DESIGNER' 
  AND p.permission_code = 'PROCESS_DESIGNER';
```

---

## 🧪 测试方案

### 测试 1：Modeler 访问测试

```bash
# 1. 启动服务
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-flowable-service
./start-flowable.sh

# 2. 测试 Modeler 页面访问
curl -I http://localhost:8086/flowable-modeler/

# 3. 测试 Modeler API（需要认证）
curl -H "Authorization: Bearer <your-jwt-token>" \
     http://localhost:8086/app/rest/models
```

### 测试 2：认证集成测试

1. 在前端登录系统（获取 JWT Token）
2. 访问流程设计器页面
3. 验证是否能正常加载 Modeler
4. 验证是否能创建、保存、部署流程

### 测试 3：流程设计完整流程

1. **创建流程**：在 Modeler 中创建新流程
2. **保存草稿**：保存流程定义
3. **部署流程**：发布到流程引擎
4. **验证部署**：调用 REST API 查询已部署的流程定义

```bash
# 查询已部署的流程
curl http://localhost:8086/flowable/definitions
```

---

## 📦 部署清单

### 后端部署

1. ✅ 更新 `pom.xml` 添加 Modeler 依赖
2. ✅ 更新 `application.yml` 配置
3. ✅ 创建 `ModelerSecurityConfig.java`
4. ✅ 创建 `FlowableUserDetailsService.java`
5. ✅ 创建 `JwtAuthenticationFilter.java`
6. ✅ 创建 `FlowableModelerUserProvider.java`
7. ✅ 打包部署 `mvn clean package`

### Gateway 部署

1. ✅ 更新 `application.yml` 路由配置
2. ✅ 重启 Gateway

### 前端部署

1. ✅ 创建 `ProcessModeler.vue`
2. ✅ 更新路由配置
3. ✅ 移除 bpmn.js 依赖
4. ✅ 构建前端 `npm run build`
5. ✅ 部署到 Nginx

---

## ⏱️ 开发时间估算

| 任务 | 预计时间 | 优先级 |
|------|----------|--------|
| 后端依赖和配置 | 1 小时 | P0 |
| Auth 集成（安全配置、用户服务）| 2 小时 | P0 |
| Gateway 路由配置 | 0.5 小时 | P0 |
| 前端页面开发 | 1.5 小时 | P1 |
| 移除 bpmn.js | 0.5 小时 | P1 |
| 测试和调试 | 2 小时 | P0 |
| **总计** | **7.5 小时** | - |

---

## 🎯 里程碑

### 里程碑 1：后端集成完成（Day 1）
- ✅ Modeler 依赖添加
- ✅ 基础配置完成
- ✅ Auth 集成完成
- ✅ 能访问 Modeler REST API

### 里程碑 2：前端集成完成（Day 2）
- ✅ ProcessModeler 页面开发
- ✅ 路由配置
- ✅ 能在前端访问 Modeler UI

### 里程碑 3：完整测试通过（Day 3）
- ✅ 认证流程测试通过
- ✅ 流程设计、保存、部署功能正常
- ✅ bpmn.js 完全移除
- ✅ 生产环境就绪

---

## 📝 注意事项

### 关键点

1. **Token 传递**：确保 JWT Token 正确传递到 Modeler iframe
2. **跨域问题**：配置 CORS 允许前端访问
3. **权限控制**：确保只有授权用户能访问 Modeler
4. **数据持久化**：Modeler 的模型数据会存储到数据库

### 可能的坑

1. **Flowable IDM 冲突**：我们禁用了 IDM Engine，需要自己实现用户服务
2. **前端路由冲突**：Modeler 是 Angular SPA，需要正确配置 iframe 或反向代理
3. **静态资源路径**：确保 Modeler 的 JS/CSS 资源能正确加载

---

## 🚀 下一步行动

我已经创建了完整的开发计划，现在可以开始实施。

**你希望我：**

1. **立即开始实施**：我按照计划逐步完成所有任务
2. **先做后端集成**：先完成后端部分，测试通过后再做前端
3. **只做关键部分**：你告诉我优先做哪些部分
4. **先审查计划**：你想调整或补充计划内容

请告诉我你的选择，我立即开始工作！ 💪

