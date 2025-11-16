package com.diom.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.workflow.entity.WorkflowNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper
 */
@Mapper
public interface WorkflowNotificationMapper extends BaseMapper<WorkflowNotification> {
}

