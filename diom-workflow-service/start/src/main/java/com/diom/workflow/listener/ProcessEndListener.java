package com.diom.workflow.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 流程结束监听器
 * 
 * 在BPMN中使用：
 * <bpmn:process id="xxx">
 *   <bpmn:extensionElements>
 *     <camunda:executionListener delegateExpression="${processEndListener}" event="end" />
 *   </bpmn:extensionElements>
 * </bpmn:process>
 *
 * @author diom
 */
@Component("processEndListener")
public class ProcessEndListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessEndListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("==========================================");
        log.info("🏁 流程结束监听器");
        log.info("==========================================");
        log.info("流程实例ID: {}", execution.getProcessInstanceId());
        log.info("流程定义ID: {}", execution.getProcessDefinitionId());
        log.info("业务Key: {}", execution.getProcessBusinessKey());
        log.info("结束时间: {}", new java.util.Date());
        
        // 计算流程执行时长
        Object startTime = execution.getVariable("processStartTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - (Long) startTime;
            long durationSeconds = duration / 1000;
            long durationMinutes = durationSeconds / 60;
            long durationHours = durationMinutes / 60;
            
            log.info("流程执行时长:");
            log.info("  毫秒: {} ms", duration);
            log.info("  秒: {} s", durationSeconds);
            log.info("  分钟: {} min", durationMinutes);
            log.info("  小时: {} h", durationHours);
        }
        
        // 记录最终结果
        Object finalResult = execution.getVariable("finalResult");
        if (finalResult != null) {
            log.info("最终结果: {}", finalResult);
        }
        
        log.info("==========================================");
        
        // TODO: 实际项目中可以：
        // 1. 记录流程完成日志到数据库
        // 2. 发送流程完成通知
        // 3. 归档流程数据
        // 4. 更新业务系统状态
        // 5. 生成流程报告
    }
}

