package com.diom.workflow.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程设计VO
 */
@Data
public class ProcessDesignVO {
    
    private Long id;
    private String processKey;
    private String processName;
    private Integer version;
    private String status;
    private String bpmnXml;
    private String description;
    private String category;
    private String deploymentId;
    private String processDefinitionId;
    private LocalDateTime deployedAt;
    private String creator;
    private String creatorName;
    private String publisher;
    private String publisherName;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 是否有更新版本
     */
    private Boolean hasNewerVersion;
}

