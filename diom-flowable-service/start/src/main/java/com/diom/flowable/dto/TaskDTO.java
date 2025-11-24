package com.diom.flowable.dto;

import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * 任务 DTO
 *
 * @author diom
 */
@Data
public class TaskDTO {
    
    /**
     * 任务ID
     */
    private String id;
    
    /**
     * 任务名称
     */
    private String name;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 办理人
     */
    private String assignee;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 到期时间
     */
    private Date dueDate;
    
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    
    /**
     * 流程定义Key
     */
    private String processDefinitionKey;
    
    /**
     * 流程定义名称
     */
    private String processDefinitionName;
    
    /**
     * 业务Key
     */
    private String businessKey;
    
    /**
     * 任务变量
     */
    private Map<String, Object> variables;
}

