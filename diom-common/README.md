# diom-common

## 项目说明

公共依赖库，提供通用的基础组件供所有服务使用。

## 模块内容

- **dto**: 通用数据传输对象
- **enums**: 枚举类型定义
- **exception**: 自定义异常
- **constant**: 常量定义
- **utils**: 工具类

## 使用方式

在其他服务的 `pom.xml` 中引入：

```xml
<dependency>
    <groupId>com.diom</groupId>
    <artifactId>diom-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 发布

本地安装到 Maven 仓库：

```bash
mvn clean install
```

发布到私有仓库：

```bash
mvn clean deploy
```

