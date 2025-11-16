package com.diom.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diom.workflow.entity.WorkflowDraft;
import com.diom.workflow.entity.WorkflowTemplate;
import com.diom.workflow.mapper.WorkflowDraftMapper;
import com.diom.workflow.mapper.WorkflowTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板和草稿服务
 *
 * @author diom
 */
@Slf4j
@Service
public class TemplateService {

    @Autowired
    private WorkflowTemplateMapper templateMapper;

    @Autowired
    private WorkflowDraftMapper draftMapper;

    // ==================== 模板管理 ====================

    /**
     * 获取所有公开模板
     */
    public List<WorkflowTemplate> getPublicTemplates(String processKey) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowTemplate> query = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("is_public", 1);
        if (processKey != null && !processKey.isEmpty()) {
            query.eq("process_definition_key", processKey);
        }
        query.orderByDesc("use_count");
        query.orderByDesc("create_time");
        return templateMapper.selectList(query);
    }

    /**
     * 获取我的模板
     */
    public List<WorkflowTemplate> getMyTemplates(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowTemplate> query = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("creator_id", userId);
        query.orderByDesc("create_time");
        return templateMapper.selectList(query);
    }

    /**
     * 根据ID获取模板
     */
    public WorkflowTemplate getTemplateById(Long id) {
        return templateMapper.selectById(id);
    }

    /**
     * 创建模板
     */
    @Transactional
    public WorkflowTemplate createTemplate(WorkflowTemplate template) {
        templateMapper.insert(template);
        log.info("创建模板成功: id={}, name={}", template.getId(), template.getTemplateName());
        return template;
    }

    /**
     * 更新模板
     */
    @Transactional
    public WorkflowTemplate updateTemplate(WorkflowTemplate template) {
        templateMapper.updateById(template);
        log.info("更新模板成功: id={}", template.getId());
        return template;
    }

    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        templateMapper.deleteById(id);
        log.info("删除模板成功: id={}", id);
    }

    /**
     * 使用模板（增加使用次数）
     */
    @Transactional
    public void useTemplate(Long id) {
        WorkflowTemplate template = templateMapper.selectById(id);
        if (template != null) {
            template.setUseCount(template.getUseCount() + 1);
            templateMapper.updateById(template);
            log.info("使用模板: id={}, useCount={}", id, template.getUseCount());
        }
    }

    // ==================== 草稿管理 ====================

    /**
     * 获取我的草稿列表
     */
    public List<WorkflowDraft> getMyDrafts(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowDraft> query = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("creator_id", userId);
        query.orderByDesc("update_time");
        return draftMapper.selectList(query);
    }

    /**
     * 根据流程Key获取草稿
     */
    public List<WorkflowDraft> getDraftsByProcessKey(Long userId, String processKey) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowDraft> query = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("creator_id", userId);
        query.eq("process_definition_key", processKey);
        query.orderByDesc("update_time");
        return draftMapper.selectList(query);
    }

    /**
     * 根据ID获取草稿
     */
    public WorkflowDraft getDraftById(Long id) {
        return draftMapper.selectById(id);
    }

    /**
     * 保存草稿
     */
    @Transactional
    public WorkflowDraft saveDraft(WorkflowDraft draft) {
        if (draft.getId() == null) {
            // 新建草稿
            draftMapper.insert(draft);
            log.info("创建草稿成功: id={}, name={}", draft.getId(), draft.getDraftName());
        } else {
            // 更新草稿
            draftMapper.updateById(draft);
            log.info("更新草稿成功: id={}", draft.getId());
        }
        return draft;
    }

    /**
     * 删除草稿
     */
    @Transactional
    public void deleteDraft(Long id) {
        draftMapper.deleteById(id);
        log.info("删除草稿成功: id={}", id);
    }

    /**
     * 从草稿创建模板
     */
    @Transactional
    public WorkflowTemplate draftToTemplate(Long draftId, String templateName, String description, Integer isPublic) {
        WorkflowDraft draft = draftMapper.selectById(draftId);
        if (draft == null) {
            throw new RuntimeException("草稿不存在");
        }

        WorkflowTemplate template = new WorkflowTemplate();
        template.setTemplateName(templateName);
        template.setProcessDefinitionKey(draft.getProcessDefinitionKey());
        template.setTemplateData(draft.getDraftData());
        template.setDescription(description);
        template.setIsPublic(isPublic);
        template.setUseCount(0);
        template.setCreatorId(draft.getCreatorId());
        template.setCreatorName(draft.getCreatorName());

        templateMapper.insert(template);
        log.info("从草稿创建模板成功: draftId={}, templateId={}", draftId, template.getId());

        return template;
    }
}

