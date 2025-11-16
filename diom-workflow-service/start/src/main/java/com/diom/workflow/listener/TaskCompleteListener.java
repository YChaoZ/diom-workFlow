package com.diom.workflow.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 任务完成监听器
 * 
 * 在BPMN的UserTask中使用：
 * <bpmn:userTask id="xxx">
 *   <bpmn:extensionElements>
 *     <camunda:taskListener delegateExpression="${taskCompleteListener}" event="complete" />
 *   </bpmn:extensionElements>
 * </bpmn:userTask>
 *
 * @author diom
 */
@Component("taskCompleteListener")
public class TaskCompleteListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(TaskCompleteListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("==========================================");
        log.info("✅ 任务完成监听器");
        log.info("==========================================");
        log.info("任务ID: {}", delegateTask.getId());
        log.info("任务名称: {}", delegateTask.getName());
        log.info("办理人: {}", delegateTask.getAssignee());
        log.info("流程实例ID: {}", delegateTask.getProcessInstanceId());
        
        // 计算任务处理时长
        Object createTime = delegateTask.getVariable("taskCreateTime");
        if (createTime != null) {
            long duration = System.currentTimeMillis() - (Long) createTime;
            long durationSeconds = duration / 1000;
            long durationMinutes = durationSeconds / 60;
            
            log.info("任务处理时长:");
            log.info("  毫秒: {} ms", duration);
            log.info("  秒: {} s", durationSeconds);
            log.info("  分钟: {} min", durationMinutes);
        }
        
        log.info("完成时间: {}", new java.util.Date());
        log.info("==========================================");
        
        // TODO: 实际项目中可以：
        // 1. 记录任务完成日志
        // 2. 发送任务完成通知
        // 3. 更新业务数据
        // 4. 触发后续流程
        // 5. 计算SLA指标
    }
}

