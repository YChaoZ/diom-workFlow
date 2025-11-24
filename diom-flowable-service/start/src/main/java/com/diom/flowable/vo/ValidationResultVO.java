package com.diom.flowable.vo;

import lombok.Data;
import java.util.List;

/**
 * BPMN验证结果VO
 */
@Data
public class ValidationResultVO {
    
    /**
     * 是否验证通过
     */
    private Boolean valid;
    
    /**
     * 流程定义Key
     */
    private String processKey;
    
    /**
     * 流程名称
     */
    private String processName;
    
    /**
     * 开始事件数量
     */
    private Integer startEvents;
    
    /**
     * 用户任务数量
     */
    private Integer userTasks;
    
    /**
     * 结束事件数量
     */
    private Integer endEvents;
    
    /**
     * 验证错误列表
     */
    private List<ValidationError> errors;
    
    @Data
    public static class ValidationError {
        /**
         * 错误行号
         */
        private Integer line;
        
        /**
         * 错误列号
         */
        private Integer column;
        
        /**
         * 错误信息
         */
        private String message;
    }
}

