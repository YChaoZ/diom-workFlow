package com.diom.flowable.service.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 通知部门经理Service Task
 *
 * @author diom
 */
@Component("notifyManagerService")
public class NotifyManagerService implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(NotifyManagerService.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        String applicant = (String) execution.getVariable("applicant");
        String leaveType = (String) execution.getVariable("leaveType");
        String startDate = (String) execution.getVariable("startDate");
        String endDate = (String) execution.getVariable("endDate");
        Object daysObj = execution.getVariable("days");
        Long days = daysObj instanceof Integer ? ((Integer) daysObj).longValue() : (Long) daysObj;
        String reason = (String) execution.getVariable("reason");
        String manager = (String) execution.getVariable("manager");

        log.info("==========================================");
        log.info("📧 通知部门经理");
        log.info("==========================================");
        log.info("收件人: {}", manager);
        log.info("申请人: {}", applicant);
        log.info("请假类型: {}", leaveType);
        log.info("开始日期: {}", startDate);
        log.info("结束日期: {}", endDate);
        log.info("请假天数: {} 天", days);
        log.info("请假原因: {}", reason);
        log.info("流程实例ID: {}", execution.getProcessInstanceId());
        log.info("==========================================");

        // TODO: 实际项目中，这里应该：
        // 1. 调用邮件服务发送通知
        // 2. 或调用消息服务（站内信、企业微信等）
        // 3. 记录通知日志到数据库

        // 模拟通知成功
        execution.setVariable("managerNotified", true);
        execution.setVariable("notifyTime", System.currentTimeMillis());

        log.info("✅ 经理通知发送成功");
    }
}

