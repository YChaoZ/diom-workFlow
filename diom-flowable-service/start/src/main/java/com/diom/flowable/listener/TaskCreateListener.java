package com.diom.flowable.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 任务创建监听器
 * 
 * 在BPMN的UserTask中使用：
 * <bpmn:userTask id="xxx">
 *   <bpmn:extensionElements>
 *     <camunda:taskListener delegateExpression="${taskCreateListener}" event="create" />
 *   </bpmn:extensionElements>
 * </bpmn:userTask>
 *
 * @author diom
 */
@Component("taskCreateListener")
public class TaskCreateListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(TaskCreateListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("==========================================");
        log.info("📝 任务创建监听器");
        log.info("==========================================");
        log.info("任务ID: {}", delegateTask.getId());
        log.info("任务名称: {}", delegateTask.getName());
        log.info("办理人: {}", delegateTask.getAssignee());
        log.info("流程实例ID: {}", delegateTask.getProcessInstanceId());
        log.info("创建时间: {}", delegateTask.getCreateTime());
        log.info("==========================================");
        
        // TODO: 实际项目中可以：
        // 1. 发送任务通知给办理人
        // 2. 记录任务创建日志
        // 3. 设置任务优先级
        // 4. 自动分配办理人（如果未指定）
        // 5. 设置任务到期时间
        
        // 示例：设置任务创建时间戳
        delegateTask.setVariable("taskCreateTime", System.currentTimeMillis());
    }
}

