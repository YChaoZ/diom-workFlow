# 工作流核心功能开发计划

## 🎯 开发目标

实现一个完整的工作流管理系统，包括流程定义、流程实例、任务管理等核心功能。

---

## 📋 功能清单与优先级

### Phase 1: 流程实例管理（Day 1-2）⭐⭐⭐⭐⭐

#### 1.1 启动流程实例
```java
POST /workflow/process/start
{
  "processDefinitionKey": "leave-process",
  "businessKey": "LEAVE-2024001",
  "variables": {
    "applicant": "张三",
    "days": 3,
    "reason": "事假"
  }
}
```

#### 1.2 查询流程实例列表
```java
GET /workflow/process/instances?status=ACTIVE&page=1&size=10
```

#### 1.3 查询流程实例详情
```java
GET /workflow/process/instances/{processInstanceId}
```

#### 1.4 删除/取消流程实例
```java
DELETE /workflow/process/instances/{processInstanceId}?reason=用户取消
```

---

### Phase 2: 任务管理（Day 3-4）⭐⭐⭐⭐⭐

#### 2.1 查询待办任务列表
```java
GET /workflow/tasks/todo?assignee=userId&page=1&size=10
```

#### 2.2 查询任务详情
```java
GET /workflow/tasks/{taskId}
```

#### 2.3 完成任务
```java
POST /workflow/tasks/{taskId}/complete
{
  "variables": {
    "approved": true,
    "comment": "同意"
  }
}
```

#### 2.4 认领任务
```java
POST /workflow/tasks/{taskId}/claim?userId=user123
```

#### 2.5 委派任务
```java
POST /workflow/tasks/{taskId}/delegate
{
  "userId": "user456"
}
```

---

### Phase 3: 流程变量管理（Day 5）⭐⭐⭐

#### 3.1 获取流程变量
```java
GET /workflow/process/instances/{processInstanceId}/variables
```

#### 3.2 设置流程变量
```java
POST /workflow/process/instances/{processInstanceId}/variables
{
  "status": "approved",
  "approver": "李四"
}
```

---

### Phase 4: 流程部署（Day 6）⭐⭐⭐

#### 4.1 部署流程定义
```java
POST /workflow/deployment
Content-Type: multipart/form-data
file: leave-process.bpmn
```

#### 4.2 删除流程部署
```java
DELETE /workflow/deployment/{deploymentId}
```

---

### Phase 5: 历史查询（Day 7）⭐⭐

#### 5.1 查询历史流程实例
```java
GET /workflow/history/process-instances
```

#### 5.2 查询历史任务
```java
GET /workflow/history/tasks
```

---

## 🏗️ 代码结构设计

### 目录结构
```
diom-workflow-service/
├── workflow-adapter/
│   └── web/
│       ├── ProcessInstanceController.java    # 流程实例控制器
│       ├── TaskController.java                # 任务控制器
│       ├── ProcessDefinitionController.java   # 流程定义控制器 ✅已有
│       └── DeploymentController.java          # 部署控制器
│
├── workflow-app/
│   ├── service/
│   │   ├── ProcessInstanceService.java        # 流程实例服务
│   │   ├── TaskService.java                   # 任务服务
│   │   └── DeploymentService.java             # 部署服务
│   ├── dto/
│   │   ├── StartProcessDTO.java               # 启动流程DTO
│   │   ├── CompleteTaskDTO.java               # 完成任务DTO
│   │   ├── ProcessInstanceDTO.java            # 流程实例DTO
│   │   └── TaskDTO.java                       # 任务DTO
│   └── assembler/
│       ├── ProcessInstanceAssembler.java      # 流程实例装配器
│       └── TaskAssembler.java                 # 任务装配器
│
├── workflow-domain/
│   └── service/
│       └── CamundaService.java                # Camunda领域服务
│
└── workflow-infrastructure/
    └── camunda/
        └── CamundaConfig.java                 # ✅已有
```

---

## 📝 开发步骤（详细）

### Step 1: 创建DTO类（10分钟）

#### StartProcessDTO
```java
@Data
public class StartProcessDTO {
    @NotBlank(message = "流程定义Key不能为空")
    private String processDefinitionKey;
    
    private String businessKey;
    
    private Map<String, Object> variables;
}
```

#### ProcessInstanceDTO
```java
@Data
public class ProcessInstanceDTO {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String businessKey;
    private Boolean suspended;
    private String startTime;
    private String endTime;
}
```

#### TaskDTO
```java
@Data
public class TaskDTO {
    private String id;
    private String name;
    private String assignee;
    private String processInstanceId;
    private String createTime;
    private String dueDate;
    private Integer priority;
}
```

---

### Step 2: 实现服务层（2-3小时）

#### ProcessInstanceService
```java
@Service
public class ProcessInstanceService {
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private RepositoryService repositoryService;
    
    /**
     * 启动流程实例
     */
    public ProcessInstanceDTO startProcess(StartProcessDTO dto) {
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(
                dto.getProcessDefinitionKey(),
                dto.getBusinessKey(),
                dto.getVariables()
            );
        
        return ProcessInstanceAssembler.toDTO(processInstance);
    }
    
    /**
     * 查询流程实例列表
     */
    public PageResult<ProcessInstanceDTO> getProcessInstances(
        String status, Integer page, Integer size) {
        
        ProcessInstanceQuery query = runtimeService
            .createProcessInstanceQuery();
        
        if ("ACTIVE".equals(status)) {
            query.active();
        } else if ("SUSPENDED".equals(status)) {
            query.suspended();
        }
        
        long total = query.count();
        List<ProcessInstance> list = query
            .listPage((page - 1) * size, size);
        
        List<ProcessInstanceDTO> dtoList = list.stream()
            .map(ProcessInstanceAssembler::toDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(total, dtoList);
    }
}
```

#### TaskService
```java
@Service
public class TaskService {
    
    @Autowired
    private org.camunda.bpm.engine.TaskService taskService;
    
    /**
     * 查询待办任务
     */
    public PageResult<TaskDTO> getTodoTasks(
        String assignee, Integer page, Integer size) {
        
        TaskQuery query = taskService.createTaskQuery()
            .taskAssignee(assignee)
            .orderByTaskCreateTime().desc();
        
        long total = query.count();
        List<Task> tasks = query.listPage((page - 1) * size, size);
        
        List<TaskDTO> dtoList = tasks.stream()
            .map(TaskAssembler::toDTO)
            .collect(Collectors.toList());
        
        return new PageResult<>(total, dtoList);
    }
    
    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }
    
    /**
     * 认领任务
     */
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }
}
```

---

### Step 3: 实现Controller层（1小时）

#### ProcessInstanceController
```java
@RestController
@RequestMapping("/workflow/process")
public class ProcessInstanceController {
    
    @Autowired
    private ProcessInstanceService processInstanceService;
    
    @PostMapping("/start")
    public Result<ProcessInstanceDTO> startProcess(
        @RequestBody @Valid StartProcessDTO dto) {
        
        ProcessInstanceDTO result = processInstanceService.startProcess(dto);
        return Result.success("流程启动成功", result);
    }
    
    @GetMapping("/instances")
    public Result<PageResult<ProcessInstanceDTO>> getInstances(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size) {
        
        PageResult<ProcessInstanceDTO> result = 
            processInstanceService.getProcessInstances(status, page, size);
        return Result.success("查询成功", result);
    }
}
```

#### TaskController
```java
@RestController
@RequestMapping("/workflow/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @GetMapping("/todo")
    public Result<PageResult<TaskDTO>> getTodoTasks(
        @RequestParam String assignee,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size) {
        
        PageResult<TaskDTO> result = 
            taskService.getTodoTasks(assignee, page, size);
        return Result.success("查询成功", result);
    }
    
    @PostMapping("/{taskId}/complete")
    public Result<Void> completeTask(
        @PathVariable String taskId,
        @RequestBody(required = false) Map<String, Object> variables) {
        
        taskService.completeTask(taskId, variables);
        return Result.success("任务完成");
    }
}
```

---

## 🧪 测试计划

### 测试流程1：请假流程

#### 1. 准备BPMN文件
```xml
<!-- leave-process.bpmn -->
<bpmn:process id="leave-process" name="请假流程">
  <bpmn:startEvent id="start"/>
  <bpmn:userTask id="approve" name="经理审批" assignee="manager"/>
  <bpmn:endEvent id="end"/>
</bpmn:process>
```

#### 2. 部署流程
```bash
POST /workflow/deployment
```

#### 3. 启动流程
```bash
POST /workflow/process/start
{
  "processDefinitionKey": "leave-process",
  "businessKey": "LEAVE-001",
  "variables": {
    "applicant": "张三",
    "days": 3
  }
}
```

#### 4. 查询待办
```bash
GET /workflow/tasks/todo?assignee=manager
```

#### 5. 完成任务
```bash
POST /workflow/tasks/{taskId}/complete
{
  "variables": {
    "approved": true
  }
}
```

---

## 📊 开发进度跟踪

```
□ Phase 1: 流程实例管理 (0/4)
  □ 启动流程实例
  □ 查询流程实例列表
  □ 查询流程实例详情
  □ 删除流程实例

□ Phase 2: 任务管理 (0/5)
  □ 查询待办任务
  □ 查询任务详情
  □ 完成任务
  □ 认领任务
  □ 委派任务

□ Phase 3: 流程变量管理 (0/2)
  □ 获取流程变量
  □ 设置流程变量

□ Phase 4: 流程部署 (0/2)
  □ 部署BPMN文件
  □ 删除部署

□ Phase 5: 历史查询 (0/2)
  □ 历史流程实例
  □ 历史任务
```

---

## 🚀 现在开始！

我将按以下顺序实现：
1. ✅ 创建DTO类
2. ✅ 实现ProcessInstanceService
3. ✅ 实现TaskService  
4. ✅ 实现Controller
5. ✅ 测试验证

准备好了吗？让我们开始编码！🎉

