package com.diom.flowable.dto;

import lombok.Data;

/**
 * 发布流程DTO
 */
@Data
public class PublishDTO {
    
    /**
     * 流程设计ID
     */
    private Long id;
    
    /**
     * 变更说明
     */
    private String changeDescription;
}

