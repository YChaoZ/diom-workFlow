package com.diom.flowable.service.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * HR备案Service Task
 *
 * @author diom
 */
@Component("hrRecordService")
public class HrRecordService implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(HrRecordService.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        String applicant = (String) execution.getVariable("applicant");
        String leaveType = (String) execution.getVariable("leaveType");
        String startDate = (String) execution.getVariable("startDate");
        String endDate = (String) execution.getVariable("endDate");
        Object daysObj = execution.getVariable("days");
        Long days = daysObj instanceof Integer ? ((Integer) daysObj).longValue() : (Long) daysObj;
        String approvalComment = (String) execution.getVariable("approvalComment");
        String manager = (String) execution.getVariable("manager");

        log.info("==========================================");
        log.info("📋 HR备案处理");
        log.info("==========================================");
        log.info("员工: {}", applicant);
        log.info("请假类型: {}", leaveType);
        log.info("开始日期: {}", startDate);
        log.info("结束日期: {}", endDate);
        log.info("请假天数: {} 天", days);
        log.info("审批人: {}", manager);
        log.info("审批意见: {}", approvalComment);
        log.info("流程实例ID: {}", execution.getProcessInstanceId());
        log.info("==========================================");

        // TODO: 实际项目中，这里应该：
        // 1. 将请假记录保存到HR系统数据库
        // 2. 扣减员工的年假/病假余额
        // 3. 同步到考勤系统
        // 4. 生成请假单PDF归档

        // 模拟生成请假单编号
        String leaveRequestId = "LR" + System.currentTimeMillis();
        execution.setVariable("leaveRequestId", leaveRequestId);
        execution.setVariable("hrRecorded", true);
        execution.setVariable("recordTime", System.currentTimeMillis());

        log.info("✅ HR备案完成，请假单编号: {}", leaveRequestId);
    }
}

