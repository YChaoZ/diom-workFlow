package com.diom.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diom.workflow.dto.ProcessDesignDTO;
import com.diom.workflow.dto.PublishDTO;
import com.diom.workflow.dto.ValidateDTO;
import com.diom.workflow.entity.ProcessDesign;
import com.diom.workflow.entity.ProcessDesignHistory;
import com.diom.workflow.mapper.ProcessDesignHistoryMapper;
import com.diom.workflow.mapper.ProcessDesignMapper;
import com.diom.workflow.vo.ProcessDesignHistoryVO;
import com.diom.workflow.vo.ProcessDesignVO;
import com.diom.workflow.vo.ValidationResultVO;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程设计服务
 */
@Slf4j
@Service
public class ProcessDesignService {
    
    @Autowired
    private ProcessDesignMapper processDesignMapper;
    
    @Autowired
    private ProcessDesignHistoryMapper historyMapper;
    
    @Autowired
    private RepositoryService repositoryService;
    
    /**
     * 分页查询流程设计列表
     */
    public Page<ProcessDesignVO> list(Map<String, Object> params) {
        int page = Integer.parseInt(params.getOrDefault("page", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        String status = (String) params.get("status");
        String category = (String) params.get("category");
        String keyword = (String) params.get("keyword");
        
        Page<ProcessDesign> pageParam = new Page<>(page, pageSize);
        QueryWrapper<ProcessDesign> wrapper = new QueryWrapper<>();
        
        // 状态过滤
        if (StringUtils.hasText(status) && !"ALL".equals(status)) {
            wrapper.eq("status", status);
        }
        
        // 分类过滤
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        
        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("process_name", keyword)
                            .or()
                            .like("process_key", keyword));
        }
        
        wrapper.orderByDesc("create_time");
        
        Page<ProcessDesign> result = processDesignMapper.selectPage(pageParam, wrapper);
        
        // 转换为VO
        Page<ProcessDesignVO> voPage = new Page<>(page, pageSize, result.getTotal());
        List<ProcessDesignVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 查询流程设计详情
     */
    public ProcessDesignVO getById(Long id) {
        ProcessDesign design = processDesignMapper.selectById(id);
        if (design == null) {
            throw new RuntimeException("流程设计不存在");
        }
        return convertToVO(design);
    }
    
    /**
     * 保存草稿
     */
    @Transactional
    public ProcessDesignVO saveDraft(ProcessDesignDTO dto, String operator, String operatorName) {
        ProcessDesign design;
        String action;
        
        if (dto.getId() == null) {
            // 新建草稿
            design = new ProcessDesign();
            design.setStatus("DRAFT");
            design.setCreator(operator);
            design.setCreatorName(operatorName);
            
            // 自动生成版本号
            Integer maxVersion = processDesignMapper.getMaxVersion(dto.getProcessKey());
            design.setVersion(maxVersion + 1);
            
            action = "CREATE";
            log.info("创建新草稿: processKey={}, version={}", dto.getProcessKey(), design.getVersion());
        } else {
            // 更新已有草稿
            design = processDesignMapper.selectById(dto.getId());
            if (design == null) {
                throw new RuntimeException("流程设计不存在");
            }
            if (!"DRAFT".equals(design.getStatus())) {
                throw new RuntimeException("只能编辑草稿状态的流程");
            }
            action = "UPDATE";
            log.info("更新草稿: id={}, processKey={}", design.getId(), design.getProcessKey());
        }
        
        // 设置基本信息
        design.setProcessKey(dto.getProcessKey());
        design.setProcessName(dto.getProcessName());
        design.setBpmnXml(dto.getBpmnXml());
        design.setDescription(dto.getDescription());
        design.setCategory(dto.getCategory());
        
        // 保存或更新
        if (dto.getId() == null) {
            processDesignMapper.insert(design);
        } else {
            processDesignMapper.updateById(design);
        }
        
        // 记录历史
        recordHistory(design, action, "保存草稿", operator, operatorName);
        
        return convertToVO(design);
    }
    
    /**
     * 验证BPMN
     */
    public ValidationResultVO validate(ValidateDTO dto) {
        ValidationResultVO result = new ValidationResultVO();
        result.setValid(true);
        result.setErrors(new ArrayList<>());
        
        try {
            // 解析BPMN XML
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
                    new ByteArrayInputStream(dto.getBpmnXml().getBytes())
            );
            
            // 获取流程定义
            Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = 
                modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);
            if (processes.isEmpty()) {
                addError(result, null, null, "BPMN文件中没有找到流程定义");
                return result;
            }
            
            org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();
            result.setProcessKey(process.getId());
            result.setProcessName(process.getName());
            
            // 统计各类元素
            Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
            Collection<UserTask> userTasks = modelInstance.getModelElementsByType(UserTask.class);
            Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
            
            result.setStartEvents(startEvents.size());
            result.setUserTasks(userTasks.size());
            result.setEndEvents(endEvents.size());
            
            // 验证规则
            if (startEvents.size() != 1) {
                addError(result, null, null, "流程必须有且只有一个开始事件");
            }
            
            if (endEvents.isEmpty()) {
                addError(result, null, null, "流程必须至少有一个结束事件");
            }
            
            // 验证用户任务必须有assignee
            for (UserTask userTask : userTasks) {
                String assignee = userTask.getCamundaAssignee();
                if (!StringUtils.hasText(assignee)) {
                    addError(result, null, null, 
                            String.format("用户任务'%s'缺少assignee属性", userTask.getName()));
                }
            }
            
            // 如果有错误，设置为无效
            if (!result.getErrors().isEmpty()) {
                result.setValid(false);
            }
            
        } catch (Exception e) {
            log.error("BPMN验证失败", e);
            addError(result, null, null, "BPMN XML格式错误: " + e.getMessage());
            result.setValid(false);
        }
        
        return result;
    }
    
    /**
     * 发布流程
     */
    @Transactional
    public ProcessDesignVO publish(PublishDTO dto, String operator, String operatorName) {
        // 1. 查询草稿
        ProcessDesign design = processDesignMapper.selectById(dto.getId());
        if (design == null) {
            throw new RuntimeException("流程设计不存在");
        }
        if (!"DRAFT".equals(design.getStatus())) {
            throw new RuntimeException("只能发布草稿状态的流程");
        }
        
        // 2. 验证BPMN
        ValidateDTO validateDTO = new ValidateDTO();
        validateDTO.setBpmnXml(design.getBpmnXml());
        ValidationResultVO validation = validate(validateDTO);
        if (!validation.getValid()) {
            throw new RuntimeException("BPMN验证失败: " + 
                    validation.getErrors().stream()
                            .map(ValidationResultVO.ValidationError::getMessage)
                            .collect(Collectors.joining("; ")));
        }
        
        // 3. 部署到Camunda引擎
        String resourceName = design.getProcessKey() + "_v" + design.getVersion() + ".bpmn";
        Deployment deployment = repositoryService.createDeployment()
                .name(design.getProcessName() + " v" + design.getVersion())
                .addString(resourceName, design.getBpmnXml())
                .deploy();
        
        log.info("流程部署成功: deploymentId={}, resourceName={}", deployment.getId(), resourceName);
        
        // 4. 获取流程定义ID
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        
        if (processDefinition == null) {
            throw new RuntimeException("部署后未找到流程定义");
        }
        
        log.info("流程定义创建成功: processDefinitionId={}, key={}, version={}", 
                processDefinition.getId(), processDefinition.getKey(), processDefinition.getVersion());
        
        // 5. 更新设计状态
        design.setStatus("PUBLISHED");
        design.setDeploymentId(deployment.getId());
        design.setProcessDefinitionId(processDefinition.getId());
        design.setPublisher(operator);
        design.setPublisherName(operatorName);
        design.setPublishTime(LocalDateTime.now());
        design.setDeployedAt(LocalDateTime.now());
        processDesignMapper.updateById(design);
        
        // 6. 记录历史
        recordHistory(design, "PUBLISH", dto.getChangeDescription(), operator, operatorName);
        
        log.info("流程发布完成: id={}, processKey={}, version={}", 
                design.getId(), design.getProcessKey(), design.getVersion());
        
        return convertToVO(design);
    }
    
    /**
     * 创建新版本
     */
    @Transactional
    public ProcessDesignVO createNewVersion(Long baseId, String changeDescription, 
                                           String operator, String operatorName) {
        // 1. 查询基础版本
        ProcessDesign baseDesign = processDesignMapper.selectById(baseId);
        if (baseDesign == null) {
            throw new RuntimeException("流程设计不存在");
        }
        if (!"PUBLISHED".equals(baseDesign.getStatus())) {
            throw new RuntimeException("只能基于已发布的流程创建新版本");
        }
        
        // 2. 创建新版本草稿
        ProcessDesign newDesign = new ProcessDesign();
        newDesign.setProcessKey(baseDesign.getProcessKey());
        newDesign.setProcessName(baseDesign.getProcessName());
        newDesign.setBpmnXml(baseDesign.getBpmnXml());
        newDesign.setDescription(baseDesign.getDescription());
        newDesign.setCategory(baseDesign.getCategory());
        newDesign.setStatus("DRAFT");
        newDesign.setCreator(operator);
        newDesign.setCreatorName(operatorName);
        
        // 自动递增版本号
        Integer maxVersion = processDesignMapper.getMaxVersion(baseDesign.getProcessKey());
        newDesign.setVersion(maxVersion + 1);
        
        processDesignMapper.insert(newDesign);
        
        // 3. 记录历史
        recordHistory(newDesign, "CREATE", 
                String.format("基于v%d创建新版本: %s", baseDesign.getVersion(), changeDescription), 
                operator, operatorName);
        
        log.info("创建新版本成功: baseVersion={}, newVersion={}, id={}", 
                baseDesign.getVersion(), newDesign.getVersion(), newDesign.getId());
        
        return convertToVO(newDesign);
    }
    
    /**
     * 查询变更历史
     */
    public List<ProcessDesignHistoryVO> getHistory(Long designId) {
        QueryWrapper<ProcessDesignHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("design_id", designId);
        wrapper.orderByDesc("create_time");
        
        List<ProcessDesignHistory> histories = historyMapper.selectList(wrapper);
        return histories.stream()
                .map(this::convertHistoryToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除草稿
     */
    @Transactional
    public void deleteDraft(Long id) {
        ProcessDesign design = processDesignMapper.selectById(id);
        if (design == null) {
            throw new RuntimeException("流程设计不存在");
        }
        if (!"DRAFT".equals(design.getStatus())) {
            throw new RuntimeException("只能删除草稿状态的流程");
        }
        
        processDesignMapper.deleteById(id);
        log.info("删除草稿成功: id={}, processKey={}", id, design.getProcessKey());
    }
    
    /**
     * 记录变更历史
     */
    private void recordHistory(ProcessDesign design, String action, String changeDescription, 
                               String operator, String operatorName) {
        ProcessDesignHistory history = new ProcessDesignHistory();
        history.setDesignId(design.getId());
        history.setProcessKey(design.getProcessKey());
        history.setVersion(design.getVersion());
        history.setAction(action);
        history.setBpmnXml(design.getBpmnXml());
        history.setChangeDescription(changeDescription);
        history.setOperator(operator);
        history.setOperatorName(operatorName);
        
        historyMapper.insert(history);
    }
    
    /**
     * 添加验证错误
     */
    private void addError(ValidationResultVO result, Integer line, Integer column, String message) {
        ValidationResultVO.ValidationError error = new ValidationResultVO.ValidationError();
        error.setLine(line);
        error.setColumn(column);
        error.setMessage(message);
        result.getErrors().add(error);
    }
    
    /**
     * 转换为VO
     */
    private ProcessDesignVO convertToVO(ProcessDesign design) {
        ProcessDesignVO vo = new ProcessDesignVO();
        BeanUtils.copyProperties(design, vo);
        
        // 判断是否有更新版本
        if ("PUBLISHED".equals(design.getStatus())) {
            Integer maxVersion = processDesignMapper.getMaxVersion(design.getProcessKey());
            vo.setHasNewerVersion(maxVersion > design.getVersion());
        } else {
            vo.setHasNewerVersion(false);
        }
        
        return vo;
    }
    
    /**
     * 转换历史为VO
     */
    private ProcessDesignHistoryVO convertHistoryToVO(ProcessDesignHistory history) {
        ProcessDesignHistoryVO vo = new ProcessDesignHistoryVO();
        BeanUtils.copyProperties(history, vo);
        return vo;
    }
}

