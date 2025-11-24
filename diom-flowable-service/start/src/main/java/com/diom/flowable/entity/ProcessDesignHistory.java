package com.diom.flowable.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程设计变更历史实体
 * 用于记录流程设计的每次变更
 */
@Data
@TableName("workflow_process_design_history")
public class ProcessDesignHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程设计ID
     */
    private Long designId;
    
    /**
     * 流程定义Key
     */
    private String processKey;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 操作类型：CREATE-创建, UPDATE-更新, PUBLISH-发布, DEPRECATE-废弃
     */
    private String action;
    
    /**
     * BPMN XML内容（快照）
     */
    private String bpmnXml;
    
    /**
     * 变更说明
     */
    private String changeDescription;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}

