package com.diom.workflow.listener;

import com.diom.workflow.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务通知监听器
 * 在任务创建和完成时自动发送通知
 */
@Slf4j
@Component("taskNotificationListener")
public class TaskNotificationListener implements TaskListener {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        String assignee = delegateTask.getAssignee();
        String taskName = delegateTask.getName();
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        log.info("任务事件: event={}, taskName={}, assignee={}", eventName, taskName, assignee);

        try {
            if ("create".equals(eventName) && assignee != null) {
                // 任务创建通知
                notificationService.createNotification(
                    assignee,
                    "TASK_ASSIGNED",
                    "您有新的待办任务",
                    String.format("流程中有新任务\"%s\"待您处理", taskName),
                    "TASK",
                    delegateTask.getId(),
                    "HIGH"
                );
            } else if ("complete".equals(eventName)) {
                // 任务完成通知（通知流程发起人）
                String applicant = (String) delegateTask.getVariable("applicant");
                if (applicant != null && assignee != null && !applicant.equals(assignee)) {
                    notificationService.createNotification(
                        applicant,
                        "TASK_COMPLETED",
                        "您的任务已被处理",
                        String.format("任务\"%s\"已由%s处理完成", taskName, assignee),
                        "PROCESS",
                        processInstanceId,
                        "NORMAL"
                    );
                }
            }
        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }
}

