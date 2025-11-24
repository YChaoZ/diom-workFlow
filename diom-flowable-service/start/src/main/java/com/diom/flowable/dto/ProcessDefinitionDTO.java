package com.diom.flowable.dto;

import lombok.Data;

/**
 * 流程定义 DTO
 *
 * @author diom
 */
@Data
public class ProcessDefinitionDTO {
    
    /**
     * 流程定义ID
     */
    private String id;
    
    /**
     * 流程定义Key
     */
    private String key;
    
    /**
     * 流程定义名称
     */
    private String name;
    
    /**
     * 流程定义版本
     */
    private String version;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 部署ID
     */
    private String deploymentId;
    
    /**
     * 是否挂起
     */
    private boolean suspended;
}

