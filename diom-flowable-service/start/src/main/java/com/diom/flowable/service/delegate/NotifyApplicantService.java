package com.diom.flowable.service.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 通知申请人Service Task
 *
 * @author diom
 */
@Component("notifyApplicantService")
public class NotifyApplicantService implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(NotifyApplicantService.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        String applicant = (String) execution.getVariable("applicant");
        Boolean approved = (Boolean) execution.getVariable("approved");
        String approvalComment = (String) execution.getVariable("approvalComment");
        String manager = (String) execution.getVariable("manager");
        String leaveRequestId = (String) execution.getVariable("leaveRequestId");

        log.info("==========================================");
        log.info("📧 通知申请人");
        log.info("==========================================");
        log.info("收件人: {}", applicant);
        log.info("审批结果: {}", approved ? "✅ 通过" : "❌ 拒绝");
        log.info("审批人: {}", manager);
        log.info("审批意见: {}", approvalComment);
        
        if (approved != null && approved) {
            log.info("请假单编号: {}", leaveRequestId);
        }
        
        log.info("流程实例ID: {}", execution.getProcessInstanceId());
        log.info("==========================================");

        // TODO: 实际项目中，这里应该：
        // 1. 发送邮件通知申请人
        // 2. 发送站内消息
        // 3. 发送企业微信/钉钉通知
        // 4. 记录通知日志

        // 设置通知结果
        execution.setVariable("applicantNotified", true);
        execution.setVariable("notifyApplicantTime", System.currentTimeMillis());
        execution.setVariable("finalResult", approved ? "APPROVED" : "REJECTED");

        log.info("✅ 申请人通知发送成功");
    }
}

