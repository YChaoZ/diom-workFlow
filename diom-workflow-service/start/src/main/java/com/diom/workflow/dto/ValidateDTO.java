package com.diom.workflow.dto;

import lombok.Data;

/**
 * 验证BPMN DTO
 */
@Data
public class ValidateDTO {
    
    /**
     * BPMN XML内容
     */
    private String bpmnXml;
}

