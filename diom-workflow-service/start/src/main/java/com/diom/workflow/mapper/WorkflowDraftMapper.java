package com.diom.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.workflow.entity.WorkflowDraft;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程草稿Mapper
 *
 * @author diom
 */
@Mapper
public interface WorkflowDraftMapper extends BaseMapper<WorkflowDraft> {
}

