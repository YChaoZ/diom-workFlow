package com.diom.flowable.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程设计历史VO
 */
@Data
public class ProcessDesignHistoryVO {
    
    private Long id;
    private Long designId;
    private String processKey;
    private Integer version;
    private String action;
    private String changeDescription;
    private String operator;
    private String operatorName;
    private LocalDateTime createTime;
}

