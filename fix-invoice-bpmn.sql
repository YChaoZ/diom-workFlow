-- 修复发票报销流程：添加用户任务的assignee属性
-- 执行命令：mysql -uroot -p1qaz2wsx diom_workflow < fix-invoice-bpmn.sql

UPDATE workflow_process_design 
SET bpmn_xml = '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  id="Definitions_1" 
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="invoice-reimbursement-process" name="发票报销流程" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="Task_Submit" name="提交报销申请" camunda:assignee="${applicant}">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Task_Approve" name="经理审批" camunda:assignee="manager">
      <bpmn:incoming>Flow_2</bpmn:incoming>
      <bpmn:outgoing>Flow_3</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Task_Finance" name="财务审核" camunda:assignee="hr">
      <bpmn:incoming>Flow_3</bpmn:incoming>
      <bpmn:outgoing>Flow_4</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="EndEvent_1" name="结束">
      <bpmn:incoming>Flow_4</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_Submit" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_Submit" targetRef="Task_Approve" />
    <bpmn:sequenceFlow id="Flow_3" sourceRef="Task_Approve" targetRef="Task_Finance" />
    <bpmn:sequenceFlow id="Flow_4" sourceRef="Task_Finance" targetRef="EndEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="invoice-reimbursement-process">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="99" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="187" y="142" width="22" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_Submit_di" bpmnElement="Task_Submit">
        <dc:Bounds x="270" y="77" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_Approve_di" bpmnElement="Task_Approve">
        <dc:Bounds x="430" y="77" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_Finance_di" bpmnElement="Task_Finance">
        <dc:Bounds x="590" y="77" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="752" y="99" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="760" y="142" width="22" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="215" y="117" />
        <di:waypoint x="270" y="117" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
        <di:waypoint x="370" y="117" />
        <di:waypoint x="430" y="117" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_3_di" bpmnElement="Flow_3">
        <di:waypoint x="530" y="117" />
        <di:waypoint x="590" y="117" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_4_di" bpmnElement="Flow_4">
        <di:waypoint x="690" y="117" />
        <di:waypoint x="752" y="117" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>'
WHERE process_key = 'invoice-reimbursement-process';

-- 验证更新结果
SELECT id, process_name, process_key, 
       CASE WHEN bpmn_xml LIKE '%camunda:assignee%' THEN '✅ 已添加assignee' ELSE '❌ 缺少assignee' END as status
FROM workflow_process_design 
WHERE process_key = 'invoice-reimbursement-process';

