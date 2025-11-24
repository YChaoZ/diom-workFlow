package com.diom.flowable.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 流程启动监听器
 * 
 * 在BPMN中使用：
 * <bpmn:process id="xxx">
 *   <bpmn:extensionElements>
 *     <camunda:executionListener delegateExpression="${processStartListener}" event="start" />
 *   </bpmn:extensionElements>
 * </bpmn:process>
 *
 * @author diom
 */
@Component("processStartListener")
public class ProcessStartListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessStartListener.class);

    @Override
    public void notify(DelegateExecution execution) {
        log.info("==========================================");
        log.info("🚀 流程启动监听器");
        log.info("==========================================");
        log.info("流程实例ID: {}", execution.getProcessInstanceId());
        log.info("流程定义ID: {}", execution.getProcessDefinitionId());
        log.info("业务Key: {}", execution.getProcessInstanceBusinessKey());
        log.info("当前活动ID: {}", execution.getCurrentActivityId());
        log.info("当前活动ID: {}", execution.getCurrentActivityId());
        log.info("启动时间: {}", new java.util.Date());
        
        // 记录流程变量
        log.info("流程变量:");
        execution.getVariables().forEach((key, value) -> 
            log.info("  {} = {}", key, value)
        );
        
        log.info("==========================================");
        
        // TODO: 实际项目中可以：
        // 1. 记录流程启动日志到数据库
        // 2. 发送通知
        // 3. 初始化一些默认变量
        // 4. 调用外部系统API
        
        // 示例：设置流程启动时间戳
        execution.setVariable("processStartTime", System.currentTimeMillis());
    }
}

