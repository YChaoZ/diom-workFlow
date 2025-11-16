package com.diom.workflow.dto;

import lombok.Data;

/**
 * 流程设计DTO（用于保存草稿）
 */
@Data
public class ProcessDesignDTO {
    
    /**
     * ID（新建时为null，更新时传ID）
     */
    private Long id;
    
    /**
     * 流程定义Key
     */
    private String processKey;
    
    /**
     * 流程名称
     */
    private String processName;
    
    /**
     * BPMN XML内容
     */
    private String bpmnXml;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 流程分类
     */
    private String category;
}

