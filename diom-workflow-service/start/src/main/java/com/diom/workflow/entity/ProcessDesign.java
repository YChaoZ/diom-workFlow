package com.diom.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程设计实体
 * 用于存储BPMN流程设计数据
 */
@Data
@TableName("workflow_process_design")
public class ProcessDesign {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程定义Key（唯一标识）
     * 例如：leave-approval-process
     */
    private String processKey;
    
    /**
     * 流程名称（中文名称）
     * 例如：请假审批流程
     */
    private String processName;
    
    /**
     * 版本号（从1开始递增）
     */
    private Integer version;
    
    /**
     * 状态：DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已废弃
     */
    private String status;
    
    /**
     * BPMN XML内容
     */
    private String bpmnXml;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 流程分类（如：人事、财务、行政）
     */
    private String category;
    
    /**
     * Camunda部署ID（发布后生成）
     */
    private String deploymentId;
    
    /**
     * Camunda流程定义ID（发布后生成）
     */
    private String processDefinitionId;
    
    /**
     * 部署时间
     */
    private LocalDateTime deployedAt;
    
    /**
     * 创建人
     */
    private String creator;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
    
    /**
     * 发布人
     */
    private String publisher;
    
    /**
     * 发布人姓名
     */
    private String publisherName;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

