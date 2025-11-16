# 🚀 快速开始指南 - 下一步做什么？

## 📊 当前状态

✅ **已完成**：基础架构（4个微服务全部运行）  
⏳ **当前阶段**：需要实现具体业务功能  
🎯 **目标**：做一个可用的业务系统

---

## 🎯 本周任务：实现第一个业务流程

### 推荐：请假审批流程 ⭐

**为什么选这个？**
- ✅ 简单易懂
- ✅ 流程典型
- ✅ 功能完整
- ✅ 容易演示

---

## 📋 5步完成请假流程

### 第1步：设计流程（30分钟）

画出流程图：
```
[员工提交] → [部门经理审批] → [HR审批] → [结束]
```

流程变量：
- applicant（申请人）
- days（天数）
- reason（理由）
- status（状态）

---

### 第2步：创建BPMN文件（1小时）

```bash
# 1. 下载Camunda Modeler
https://camunda.com/download/modeler/

# 2. 创建文件
diom-workflow-service/start/src/main/resources/processes/leave-process.bpmn

# 3. 添加元素
- Start Event（开始）
- User Task（部门经理审批）assignee: manager
- User Task（HR审批）assignee: hr
- End Event（结束）
```

---

### 第3步：创建数据库表（30分钟）

```sql
-- 请假申请表
CREATE TABLE `leave_application` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `username` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `days` INT NOT NULL COMMENT '请假天数',
  `reason` VARCHAR(500) NOT NULL COMMENT '请假理由',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  `process_instance_id` VARCHAR(64) COMMENT '流程实例ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### 第4步：开发API（2-3小时）

#### 需要的API：

```java
// 1. 提交请假申请
POST /api/leave/apply
{
  "startDate": "2025-11-15",
  "endDate": "2025-11-17",
  "days": 3,
  "reason": "家庭事务"
}

// 2. 查询我的申请
GET /api/leave/my

// 3. 查询我的待办任务
GET /api/leave/tasks

// 4. 审批（同意/拒绝）
POST /api/leave/approve/{taskId}
{
  "approved": true,
  "comment": "同意"
}

// 5. 查询申请详情
GET /api/leave/{id}
```

---

### 第5步：测试（1小时）

```bash
# 1. 启动工作流服务
cd diom-workflow-service/start
mvn spring-boot:run

# 2. 提交申请
curl -X POST http://localhost:8083/api/leave/apply \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-11-15",
    "endDate": "2025-11-17",
    "days": 3,
    "reason": "家庭事务"
  }'

# 3. 查询待办
curl http://localhost:8083/api/leave/tasks?assignee=manager

# 4. 审批
curl -X POST http://localhost:8083/api/leave/approve/{taskId} \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comment": "同意"}'
```

---

## 📁 需要创建的文件

```
diom-workflow-service/
├── start/
│   ├── src/main/resources/
│   │   └── processes/
│   │       └── leave-process.bpmn          ← 新建
│   └── src/main/java/com/diom/workflow/
│       ├── controller/
│       │   └── LeaveController.java        ← 新建
│       ├── service/
│       │   └── LeaveService.java           ← 新建
│       ├── entity/
│       │   └── LeaveApplication.java       ← 新建
│       ├── mapper/
│       │   └── LeaveMapper.java            ← 新建
│       └── dto/
│           ├── LeaveApplyRequest.java      ← 新建
│           └── ApproveRequest.java         ← 新建
└── init-leave.sql                          ← 新建
```

---

## 🎯 完成后的效果

1. ✅ 员工可以提交请假申请
2. ✅ 经理可以看到待审批任务
3. ✅ 经理可以同意/拒绝
4. ✅ HR可以看到待审批任务
5. ✅ HR可以同意/拒绝
6. ✅ 员工可以查看申请状态

---

## 💡 代码示例

### LeaveController.java（简化版）

```java
@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    
    @Autowired
    private LeaveService leaveService;
    
    // 提交申请
    @PostMapping("/apply")
    public Result<LeaveApplication> apply(@RequestBody LeaveApplyRequest request) {
        return Result.success(leaveService.apply(request));
    }
    
    // 我的申请
    @GetMapping("/my")
    public Result<List<LeaveApplication>> myApplications(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(leaveService.getMyApplications(userId));
    }
    
    // 待办任务
    @GetMapping("/tasks")
    public Result<List<TaskDTO>> tasks(@RequestParam String assignee) {
        return Result.success(leaveService.getTasks(assignee));
    }
    
    // 审批
    @PostMapping("/approve/{taskId}")
    public Result<String> approve(
            @PathVariable String taskId,
            @RequestBody ApproveRequest request) {
        leaveService.approve(taskId, request);
        return Result.success("审批完成");
    }
}
```

---

## ⏰ 时间分配建议

| 任务 | 预计时间 | 难度 |
|------|---------|------|
| 设计流程 | 30分钟 | ⭐ |
| 创建BPMN | 1小时 | ⭐⭐ |
| 数据库设计 | 30分钟 | ⭐ |
| 开发API | 2-3小时 | ⭐⭐⭐ |
| 测试验证 | 1小时 | ⭐⭐ |
| **总计** | **5-6小时** | |

**一天就能完成！** 🎉

---

## 🆘 需要帮助？

### 不知道怎么写BPMN？
→ 查看现有的 `simple-process.bpmn` 作为参考  
→ 使用Camunda Modeler的图形界面拖拽

### 不知道怎么调用Camunda API？
→ 查看现有的 `WorkflowService.java`  
→ 参考 `WorkflowController.java` 的实现

### MyBatis不熟悉？
→ 查看 `diom-auth-service` 的 `UserMapper` 作为参考  
→ 使用MyBatis Plus简化开发

---

## 🎯 下下步做什么？

完成请假流程后，可以选择：

### 选项A：开发前端（推荐）
- 用Vue 3做一个界面
- 可以提交申请、查看待办
- 有可视化界面

### 选项B：实现更多流程
- 报销流程
- 采购流程
- 合同审批流程

### 选项C：完善权限
- 不同角色看到不同数据
- 数据权限过滤

---

## 📝 开发检查清单

开发前：
- [ ] 安装Camunda Modeler
- [ ] 准备数据库
- [ ] 确认工作流服务运行正常

开发中：
- [ ] BPMN文件创建完成
- [ ] 数据库表创建完成
- [ ] Entity和Mapper创建完成
- [ ] Service实现完成
- [ ] Controller实现完成

开发后：
- [ ] 提交申请测试通过
- [ ] 查询待办测试通过
- [ ] 审批功能测试通过
- [ ] 流程结束验证通过

---

## 🌟 成功标志

当你能做到以下几点，就说明完成了：

1. ✅ 能用Postman提交一个请假申请
2. ✅ 能查询到待办任务
3. ✅ 能完成审批操作
4. ✅ 整个流程能走通
5. ✅ 数据能正确保存到数据库

---

## 📚 参考资源

- **现有代码参考**：
  - `diom-workflow-service/start/src/main/java/com/diom/workflow/`
  - `diom-auth-service/` （MyBatis用法）

- **Camunda教程**：
  - 官方文档：https://docs.camunda.org/
  - Get Started：https://docs.camunda.org/get-started/

- **BPMN建模**：
  - Camunda Modeler下载：https://camunda.com/download/modeler/

---

## 💪 你能行！

这不是一个复杂的任务，按照步骤一步一步来：

1. 画流程图（纸上画也行）
2. 用工具创建BPMN文件
3. 写数据库表
4. 写Java代码
5. 测试

**5-6小时就能完成你的第一个工作流！**

---

## 🎉 开始吧！

```bash
# 现在就开始
cd diom-workflow-service/start

# 创建BPMN文件目录
mkdir -p src/main/resources/processes

# 下载Camunda Modeler并打开
# 开始设计你的第一个流程！
```

**祝你成功！** 🚀

---

**文档创建**: 2025-11-14  
**适用于**: 第二阶段开发第一周

