package com.diom.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程草稿实体
 *
 * @author diom
 */
@Data
@TableName("workflow_draft")
public class WorkflowDraft {

    /**
     * 草稿ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 草稿名称
     */
    private String draftName;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 草稿数据（JSON格式）
     */
    private String draftData;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

