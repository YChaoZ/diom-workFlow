package com.diom.workflow.controller;

import com.diom.common.dto.Result;
import com.diom.workflow.dto.ProcessDefinitionDTO;
import com.diom.workflow.dto.ProcessInstanceDTO;
import com.diom.workflow.dto.TaskDTO;
import com.diom.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流 REST API
 *
 * @author diom
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    // ==================== 流程定义 ====================

    /**
     * 获取所有流程定义
     */
    @GetMapping("/definitions")
    public Result<List<ProcessDefinitionDTO>> getProcessDefinitions() {
        try {
            List<ProcessDefinitionDTO> definitions = workflowService.getProcessDefinitions();
            return Result.success(definitions);
        } catch (Exception e) {
            log.error("获取流程定义失败", e);
            return Result.fail("获取流程定义失败: " + e.getMessage());
        }
    }

    /**
     * 根据Key获取流程定义
     */
    @GetMapping("/definition/{key}")
    public Result<ProcessDefinitionDTO> getProcessDefinitionByKey(@PathVariable String key) {
        try {
            ProcessDefinitionDTO definition = workflowService.getProcessDefinitionByKey(key);
            if (definition != null) {
                return Result.success(definition);
            } else {
                return Result.fail(404, "流程定义不存在");
            }
        } catch (Exception e) {
            log.error("获取流程定义失败", e);
            return Result.fail("获取流程定义失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程定义的BPMN XML
     */
    @GetMapping("/definition/{key}/xml")
    public Result<String> getProcessDefinitionXml(@PathVariable String key) {
        try {
            String xml = workflowService.getProcessDefinitionXml(key);
            return Result.success(xml);
        } catch (Exception e) {
            log.error("获取流程定义XML失败: key={}", key, e);
            return Result.fail("获取流程定义XML失败: " + e.getMessage());
        }
    }

    // ==================== 流程实例 ====================

    /**
     * 启动流程实例
     */
    @PostMapping("/start/{processKey}")
    public Result<ProcessInstanceDTO> startProcess(
            @PathVariable String processKey,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            ProcessInstanceDTO instance = workflowService.startProcess(processKey, variables);
            log.info("流程启动成功: processKey={}, instanceId={}", processKey, instance.getId());
            return Result.success(instance);
        } catch (Exception e) {
            log.error("启动流程失败: processKey={}", processKey, e);
            return Result.fail("启动流程失败: " + e.getMessage());
        }
    }

    /**
     * 启动流程实例（带业务Key）
     */
    @PostMapping("/start/{processKey}/business/{businessKey}")
    public Result<ProcessInstanceDTO> startProcessWithBusinessKey(
            @PathVariable String processKey,
            @PathVariable String businessKey,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            ProcessInstanceDTO instance = workflowService.startProcessWithBusinessKey(processKey, businessKey, variables);
            log.info("流程启动成功: processKey={}, businessKey={}, instanceId={}", processKey, businessKey, instance.getId());
            return Result.success(instance);
        } catch (Exception e) {
            log.error("启动流程失败: processKey={}, businessKey={}", processKey, businessKey, e);
            return Result.fail("启动流程失败: " + e.getMessage());
        }
    }

    /**
     * 查询流程实例
     */
    @GetMapping("/instance/{instanceId}")
    public Result<ProcessInstanceDTO> getProcessInstance(@PathVariable String instanceId) {
        try {
            ProcessInstanceDTO instance = workflowService.getProcessInstance(instanceId);
            if (instance != null) {
                return Result.success(instance);
            } else {
                return Result.fail(404, "流程实例不存在");
            }
        } catch (Exception e) {
            log.error("查询流程实例失败: instanceId={}", instanceId, e);
            return Result.fail("查询流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程变量
     */
    @GetMapping("/instance/{instanceId}/variables")
    public Result<Map<String, Object>> getProcessVariables(@PathVariable String instanceId) {
        try {
            Map<String, Object> variables = workflowService.getProcessVariables(instanceId);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取流程变量失败: instanceId={}", instanceId, e);
            return Result.fail("获取流程变量失败: " + e.getMessage());
        }
    }

    /**
     * 设置流程变量
     */
    @PostMapping("/instance/{instanceId}/variables")
    public Result<String> setProcessVariables(
            @PathVariable String instanceId,
            @RequestBody Map<String, Object> variables) {
        try {
            workflowService.setProcessVariables(instanceId, variables);
            return Result.success("设置流程变量成功");
        } catch (Exception e) {
            log.error("设置流程变量失败: instanceId={}", instanceId, e);
            return Result.fail("设置流程变量失败: " + e.getMessage());
        }
    }

    /**
     * 删除流程实例
     */
    @DeleteMapping("/instance/{instanceId}")
    public Result<String> deleteProcessInstance(
            @PathVariable String instanceId,
            @RequestParam(required = false) String reason) {
        try {
            workflowService.deleteProcessInstance(instanceId, reason);
            return Result.success("删除流程实例成功");
        } catch (Exception e) {
            log.error("删除流程实例失败: instanceId={}", instanceId, e);
            return Result.fail("删除流程实例失败: " + e.getMessage());
        }
    }

    // ==================== 任务管理 ====================

    /**
     * 查询用户任务列表
     */
    @GetMapping("/tasks")
    public Result<List<TaskDTO>> getTasks(@RequestParam String assignee) {
        try {
            List<TaskDTO> tasks = workflowService.getTasks(assignee);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询任务列表失败: assignee={}", assignee, e);
            return Result.fail("查询任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<TaskDTO> getTaskDetail(@PathVariable String taskId) {
        try {
            TaskDTO task = workflowService.getTaskDetail(taskId);
            if (task != null) {
                return Result.success(task);
            } else {
                return Result.fail(404, "任务不存在");
            }
        } catch (Exception e) {
            log.error("查询任务详情失败: taskId={}", taskId, e);
            return Result.fail("查询任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务
     */
    @PostMapping("/task/{taskId}/complete")
    public Result<String> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            workflowService.completeTask(taskId, variables);
            log.info("任务完成成功: taskId={}", taskId);
            return Result.success("任务完成成功");
        } catch (Exception e) {
            log.error("完成任务失败: taskId={}", taskId, e);
            return Result.fail("完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 认领任务
     */
    @PostMapping("/task/{taskId}/claim")
    public Result<String> claimTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        try {
            workflowService.claimTask(taskId, userId);
            log.info("认领任务成功: taskId={}, userId={}", taskId, userId);
            return Result.success("认领任务成功");
        } catch (Exception e) {
            log.error("认领任务失败: taskId={}, userId={}", taskId, userId, e);
            return Result.fail("认领任务失败: " + e.getMessage());
        }
    }

    /**
     * 转办任务
     */
    @PostMapping("/task/{taskId}/delegate")
    public Result<String> delegateTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        try {
            workflowService.delegateTask(taskId, userId);
            log.info("转办任务成功: taskId={}, userId={}", taskId, userId);
            return Result.success("转办任务成功");
        } catch (Exception e) {
            log.error("转办任务失败: taskId={}, userId={}", taskId, userId, e);
            return Result.fail("转办任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务变量
     */
    @GetMapping("/task/{taskId}/variables")
    public Result<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        try {
            Map<String, Object> variables = workflowService.getTaskVariables(taskId);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取任务变量失败: taskId={}", taskId, e);
            return Result.fail("获取任务变量失败: " + e.getMessage());
        }
    }

    /**
     * 设置任务变量
     */
    @PostMapping("/task/{taskId}/variables")
    public Result<String> setTaskVariables(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) {
        try {
            workflowService.setTaskVariables(taskId, variables);
            return Result.success("设置任务变量成功");
        } catch (Exception e) {
            log.error("设置任务变量失败: taskId={}", taskId, e);
            return Result.fail("设置任务变量失败: " + e.getMessage());
        }
    }

    // ==================== 历史查询 ====================

    /**
     * 查询历史流程实例
     */
    @GetMapping("/history/instances")
    public Result<List<Map<String, Object>>> getHistoricProcessInstances(
            @RequestParam String processDefinitionKey) {
        try {
            List<Map<String, Object>> instances = workflowService.getHistoricProcessInstances(processDefinitionKey);
            return Result.success(instances);
        } catch (Exception e) {
            log.error("查询历史流程实例失败: processDefinitionKey={}", processDefinitionKey, e);
            return Result.fail("查询历史流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 查询历史任务
     */
    @GetMapping("/history/tasks")
    public Result<List<Map<String, Object>>> getHistoricTasks(
            @RequestParam String processInstanceId) {
        try {
            List<Map<String, Object>> tasks = workflowService.getHistoricTasks(processInstanceId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询历史任务失败: processInstanceId={}", processInstanceId, e);
            return Result.fail("查询历史任务失败: " + e.getMessage());
        }
    }
}

