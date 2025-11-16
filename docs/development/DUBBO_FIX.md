# 🔧 Dubbo依赖冲突修复

## ❌ 问题

```
java.lang.ClassNotFoundException: com.alibaba.nacos.shaded.com.google.common.collect.Maps
```

## ✅ 解决方案

**根本原因**: Dubbo 3.0.15 需要 `dubbo-registry-nacos` 依赖来正确集成Nacos注册中心。

### 修复内容

在三个服务的 `pom.xml` 中，将：

```xml
<!-- Nacos for Dubbo -->
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-client</artifactId>
</dependency>
```

**替换为**:

```xml
<!-- Dubbo Nacos Registry -->
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-registry-nacos</artifactId>
    <version>${dubbo.version}</version>
</dependency>
```

### 受影响的服务

1. ✅ `diom-auth-service/pom.xml`
2. ✅ `diom-web-service/web-infrastructure/pom.xml`
3. ✅ `diom-workflow-service/start/pom.xml`

---

## 🚀 现在可以启动了

```bash
# 方法1: IDE启动（推荐）
# 在IDE中直接运行各个服务的Application类

# 方法2: Maven启动
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-auth-service
mvn spring-boot:run
```

所有服务应该可以正常启动了！🎉

