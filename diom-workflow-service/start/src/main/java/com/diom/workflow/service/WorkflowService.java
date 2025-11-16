package com.diom.workflow.service;

import com.diom.workflow.dto.ProcessDefinitionDTO;
import com.diom.workflow.dto.ProcessInstanceDTO;
import com.diom.workflow.dto.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流服务
 *
 * @author diom
 */
@Slf4j
@Service
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    // ==================== 流程定义管理 ====================

    /**
     * 获取所有流程定义（最新版本）
     */
    public List<ProcessDefinitionDTO> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();
        
        return definitions.stream()
                .map(this::convertToProcessDefinitionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据Key获取流程定义
     */
    public ProcessDefinitionDTO getProcessDefinitionByKey(String key) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        
        return definition != null ? convertToProcessDefinitionDTO(definition) : null;
    }

    /**
     * 获取流程定义的BPMN XML
     */
    public String getProcessDefinitionXml(String key) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + key);
        }
        
        try {
            java.io.InputStream inputStream = repositoryService.getProcessModel(definition.getId());
            byte[] bytes = readAllBytes(inputStream);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("获取流程定义XML失败: key={}", key, e);
            throw new RuntimeException("获取流程定义XML失败: " + e.getMessage());
        }
    }
    
    /**
     * 读取 InputStream 的所有字节 (Java 8 兼容)
     */
    private byte[] readAllBytes(java.io.InputStream inputStream) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // ==================== 流程实例管理 ====================

    /**
     * 启动流程实例
     */
    public ProcessInstanceDTO startProcess(String processDefinitionKey, Map<String, Object> variables) {
        log.info("启动流程: {}, 变量: {}", processDefinitionKey, variables);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
        
        // 获取流程变量
        Map<String, Object> processVariables = runtimeService.getVariables(instance.getId());
        
        ProcessInstanceDTO dto = convertToProcessInstanceDTO(instance);
        dto.setVariables(processVariables);
        
        return dto;
    }

    /**
     * 启动流程实例（带业务Key）
     */
    public ProcessInstanceDTO startProcessWithBusinessKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        log.info("启动流程: {}, 业务Key: {}, 变量: {}", processDefinitionKey, businessKey, variables);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        
        ProcessInstanceDTO dto = convertToProcessInstanceDTO(instance);
        dto.setVariables(runtimeService.getVariables(instance.getId()));
        
        return dto;
    }

    /**
     * 查询流程实例
     */
    public ProcessInstanceDTO getProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        if (instance == null) {
            return null;
        }
        
        ProcessInstanceDTO dto = convertToProcessInstanceDTO(instance);
        dto.setVariables(runtimeService.getVariables(processInstanceId));
        
        return dto;
    }

    /**
     * 获取流程变量
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    /**
     * 设置流程变量
     */
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        log.info("设置流程变量: processInstanceId={}, variables={}", processInstanceId, variables);
        runtimeService.setVariables(processInstanceId, variables);
    }

    /**
     * 删除流程实例
     */
    public void deleteProcessInstance(String processInstanceId, String reason) {
        log.info("删除流程实例: {}, 原因: {}", processInstanceId, reason);
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    // ==================== 任务管理 ====================

    /**
     * 查询用户任务列表
     */
    public List<TaskDTO> getTasks(String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .desc()
                .list();
        
        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询任务详情（包含变量）
     */
    public TaskDTO getTaskDetail(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = convertToTaskDTO(task);
        
        // 获取任务变量和流程变量
        Map<String, Object> taskVariables = taskService.getVariables(taskId);
        dto.setVariables(taskVariables);
        
        return dto;
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("完成任务: {}, 变量: {}", taskId, variables);
        taskService.complete(taskId, variables);
    }

    /**
     * 认领任务
     */
    public void claimTask(String taskId, String userId) {
        log.info("认领任务: {}, 用户: {}", taskId, userId);
        taskService.claim(taskId, userId);
    }

    /**
     * 转办任务
     */
    public void delegateTask(String taskId, String userId) {
        log.info("转办任务: {}, 目标用户: {}", taskId, userId);
        taskService.setAssignee(taskId, userId);
    }

    /**
     * 获取任务变量
     */
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    /**
     * 设置任务变量
     */
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        log.info("设置任务变量: taskId={}, variables={}", taskId, variables);
        taskService.setVariables(taskId, variables);
    }

    // ==================== 历史查询 ====================

    /**
     * 查询历史流程实例
     */
    public List<Map<String, Object>> getHistoricProcessInstances(String processDefinitionKey) {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .orderByProcessInstanceStartTime()
                .desc()
                .list();
        
        return instances.stream().map(instance -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", instance.getId());
            map.put("processDefinitionKey", instance.getProcessDefinitionKey());
            map.put("processDefinitionName", instance.getProcessDefinitionName());
            map.put("businessKey", instance.getBusinessKey());
            map.put("startTime", instance.getStartTime());
            map.put("endTime", instance.getEndTime());
            map.put("durationInMillis", instance.getDurationInMillis());
            map.put("startUserId", instance.getStartUserId());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 查询历史任务
     */
    public List<Map<String, Object>> getHistoricTasks(String processInstanceId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("name", task.getName());
            map.put("assignee", task.getAssignee());
            map.put("startTime", task.getStartTime());
            map.put("endTime", task.getEndTime());
            map.put("durationInMillis", task.getDurationInMillis());
            return map;
        }).collect(Collectors.toList());
    }

    // ==================== 转换方法 ====================

    private ProcessDefinitionDTO convertToProcessDefinitionDTO(ProcessDefinition definition) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setId(definition.getId());
        dto.setKey(definition.getKey());
        dto.setName(definition.getName());
        dto.setVersion(String.valueOf(definition.getVersion()));
        dto.setDescription(definition.getDescription());
        dto.setDeploymentId(definition.getDeploymentId());
        dto.setSuspended(definition.isSuspended());
        return dto;
    }

    private ProcessInstanceDTO convertToProcessInstanceDTO(ProcessInstance instance) {
        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        dto.setId(instance.getId());
        dto.setProcessDefinitionId(instance.getProcessDefinitionId());
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setEnded(instance.isEnded());
        dto.setSuspended(instance.isSuspended());
        
        // 获取流程定义信息
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId())
                .singleResult();
        if (definition != null) {
            dto.setProcessDefinitionKey(definition.getKey());
            dto.setProcessDefinitionName(definition.getName());
        }
        
        return dto;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setAssignee(task.getAssignee());
        dto.setCreateTime(task.getCreateTime());
        dto.setDueDate(task.getDueDate());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setBusinessKey(runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult()
                .getBusinessKey());
        
        // 获取流程定义信息
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
        if (definition != null) {
            dto.setProcessDefinitionKey(definition.getKey());
            dto.setProcessDefinitionName(definition.getName());
        }
        
        return dto;
    }
}

