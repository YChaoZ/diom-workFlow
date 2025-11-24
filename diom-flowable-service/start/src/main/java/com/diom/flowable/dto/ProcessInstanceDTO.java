package com.diom.flowable.dto;

import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * 流程实例 DTO
 *
 * @author diom
 */
@Data
public class ProcessInstanceDTO {
    
    /**
     * 流程实例ID
     */
    private String id;
    
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    
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
     * 是否结束
     */
    private boolean ended;
    
    /**
     * 是否挂起
     */
    private boolean suspended;
    
    /**
     * 流程变量
     */
    private Map<String, Object> variables;
    
    /**
     * 开始时间
     */
    private Date startTime;
}
