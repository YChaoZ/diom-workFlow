package com.diom.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.flowable.entity.ProcessDesignHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程设计变更历史Mapper
 */
@Mapper
public interface ProcessDesignHistoryMapper extends BaseMapper<ProcessDesignHistory> {
}

