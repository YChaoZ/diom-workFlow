# 🔧 MyBatis Plus 配置修复

## 问题描述

**错误信息**：
```
加载模板和草稿失败: Error: 获取公开模板失败: Invalid bound statement (not found): 
com.diom.flowable.mapper.WorkflowTemplateMapper.selectList
```

## 问题原因

`MyBatisPlusConfig.java` 中自定义的 `SqlSessionFactory` 配置与 MyBatis Plus 的自动配置冲突，导致 MyBatis Plus 的 `BaseMapper` 方法无法正常工作。

## 解决方案

### ✅ 已修复

**修改文件**: `diom-flowable-service/start/src/main/java/com/diom/flowable/config/MyBatisPlusConfig.java`

**修改内容**：
- ✅ 移除了自定义的 `SqlSessionFactory` 配置
- ✅ 移除了自定义的 `SqlSessionTemplate` 配置
- ✅ 保留了 `MybatisPlusInterceptor` 分页插件配置
- ✅ 让 MyBatis Plus 使用默认的自动配置

**修改后的配置**：

```java
@Configuration
@MapperScan("com.diom.flowable.mapper")
public class MyBatisPlusConfig {
    
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

## 🚀 下一步操作

### 1. 重启 Flowable 服务

**在 IDEA 中**：
1. 停止当前运行的 `FlowableApplication`（点击红色停止按钮）
2. 等待完全停止（约 5 秒）
3. 重新运行 `FlowableApplication.main()`
4. 等待启动完成（约 30 秒）

### 2. 验证服务

启动成功后，运行：

```bash
# 健康检查
curl http://localhost:8086/actuator/health

# 应该返回：
# {"status":"UP"}
```

### 3. 刷新浏览器

回到浏览器中的流程定义页面，刷新并重试点击"发起流程"按钮：

```
http://localhost:3000/workflow/list
```

## 📊 预期结果

修复后，点击"发起流程"按钮应该：
- ✅ 不再出现 `Invalid bound statement` 错误
- ✅ 正确加载模板列表
- ✅ 弹出发起流程的对话框
- ✅ 可以输入流程变量并启动流程

## 🔍 技术说明

### 为什么会出现这个问题？

1. **自定义 SqlSessionFactory 与 MyBatis Plus 冲突**
   - MyBatis Plus 有自己的自动配置
   - 自定义的 SqlSessionFactory 覆盖了默认配置
   - 导致 BaseMapper 的方法映射失效

2. **Mapper XML 位置配置不当**
   - 配置中设置了 `classpath*:/mapper/**/*.xml`
   - 但 MyBatis Plus 的 BaseMapper 不需要 XML 文件
   - 这个配置可能干扰了 MyBatis Plus 的工作

3. **与 Flowable 的冲突**
   - Flowable 有自己的 SqlSessionFactory
   - 自定义配置试图用 @Primary 解决冲突
   - 但实际上引入了新的问题

### 正确的做法

- ✅ 让 MyBatis Plus 使用默认的自动配置
- ✅ 只配置必要的插件（如分页插件）
- ✅ 使用 @MapperScan 扫描 Mapper 接口
- ✅ 实体类使用 @TableName 注解指定表名
- ✅ Mapper 接口继承 BaseMapper<T>

### MyBatis Plus 自动配置的优势

1. **自动生成 SQL**: 不需要写 XML 文件
2. **通用 CRUD 方法**: BaseMapper 提供常用方法
3. **条件构造器**: 灵活构建查询条件
4. **分页支持**: 配合分页插件轻松分页
5. **性能优化**: 自动优化 SQL 语句

## 📚 相关文档

- **MyBatis Plus 官方文档**: https://baomidou.com/
- **Spring Boot 集成文档**: https://baomidou.com/pages/56bac0/#spring-boot
- **BaseMapper 方法说明**: https://baomidou.com/pages/49cc81/#mapper-crud-接口

---

**修复时间**: 2025-11-26  
**修复文件**: `MyBatisPlusConfig.java`  
**状态**: ✅ 已修复，等待重启服务验证

