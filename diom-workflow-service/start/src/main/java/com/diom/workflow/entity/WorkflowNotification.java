package com.diom.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("workflow_notification")
public class WorkflowNotification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String username;
    private String type;
    private String title;
    private String content;
    private String linkType;
    private String linkId;
    private Integer isRead;
    private String priority;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
}

