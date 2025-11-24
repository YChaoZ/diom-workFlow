package com.diom.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.flowable.entity.ProcessDesign;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 流程设计Mapper
 */
@Mapper
public interface ProcessDesignMapper extends BaseMapper<ProcessDesign> {
    
    /**
     * 获取指定流程Key的最大版本号
     * @param processKey 流程Key
     * @return 最大版本号，如果不存在返回0
     */
    @Select("SELECT COALESCE(MAX(version), 0) FROM workflow_process_design WHERE process_key = #{processKey}")
    Integer getMaxVersion(String processKey);
    
    /**
     * 获取指定流程Key的最新已发布版本
     * @param processKey 流程Key
     * @return 最新已发布的流程设计
     */
    @Select("SELECT * FROM workflow_process_design " +
            "WHERE process_key = #{processKey} AND status = 'PUBLISHED' " +
            "ORDER BY version DESC LIMIT 1")
    ProcessDesign getLatestPublished(String processKey);
}

