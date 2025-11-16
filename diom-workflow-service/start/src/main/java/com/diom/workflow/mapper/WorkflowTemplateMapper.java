package com.diom.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.workflow.entity.WorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程模板Mapper
 *
 * @author diom
 */
@Mapper
public interface WorkflowTemplateMapper extends BaseMapper<WorkflowTemplate> {
}

