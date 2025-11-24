package com.diom.flowable.controller;

import com.diom.common.dto.Result;
import com.diom.flowable.entity.WorkflowDraft;
import com.diom.flowable.entity.WorkflowTemplate;
import com.diom.flowable.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模板和草稿 REST API
 *
 * @author diom
 */
@Slf4j
@RestController
@RequestMapping("/workflow/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    // ==================== 模板管理 ====================

    /**
     * 获取公开模板列表
     */
    @GetMapping("/public")
    public Result<List<WorkflowTemplate>> getPublicTemplates(
            @RequestParam(required = false) String processKey) {
        try {
            List<WorkflowTemplate> templates = templateService.getPublicTemplates(processKey);
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取公开模板失败", e);
            return Result.fail("获取公开模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的模板列表
     */
    @GetMapping("/my")
    public Result<List<WorkflowTemplate>> getMyTemplates(@RequestParam Long userId) {
        try {
            List<WorkflowTemplate> templates = templateService.getMyTemplates(userId);
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取我的模板失败", e);
            return Result.fail("获取我的模板失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取模板详情
     */
    @GetMapping("/{id}")
    public Result<WorkflowTemplate> getTemplateById(@PathVariable Long id) {
        try {
            WorkflowTemplate template = templateService.getTemplateById(id);
            if (template != null) {
                return Result.success(template);
            } else {
                return Result.fail(404, "模板不存在");
            }
        } catch (Exception e) {
            log.error("获取模板详情失败", e);
            return Result.fail("获取模板详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<WorkflowTemplate> createTemplate(@RequestBody WorkflowTemplate template) {
        try {
            WorkflowTemplate created = templateService.createTemplate(template);
            return Result.success("创建模板成功", created);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return Result.fail("创建模板失败: " + e.getMessage());
        }
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<WorkflowTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody WorkflowTemplate template) {
        try {
            template.setId(id);
            WorkflowTemplate updated = templateService.updateTemplate(template);
            return Result.success("更新模板成功", updated);
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return Result.fail("更新模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return Result.success("删除模板成功");
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.fail("删除模板失败: " + e.getMessage());
        }
    }

    /**
     * 使用模板（增加使用次数）
     */
    @PostMapping("/{id}/use")
    public Result<String> useTemplate(@PathVariable Long id) {
        try {
            templateService.useTemplate(id);
            return Result.success("使用模板成功");
        } catch (Exception e) {
            log.error("使用模板失败", e);
            return Result.fail("使用模板失败: " + e.getMessage());
        }
    }

    // ==================== 草稿管理 ====================

    /**
     * 获取我的草稿列表
     */
    @GetMapping("/draft/my")
    public Result<List<WorkflowDraft>> getMyDrafts(@RequestParam Long userId) {
        try {
            List<WorkflowDraft> drafts = templateService.getMyDrafts(userId);
            return Result.success(drafts);
        } catch (Exception e) {
            log.error("获取我的草稿失败", e);
            return Result.fail("获取我的草稿失败: " + e.getMessage());
        }
    }

    /**
     * 根据流程Key获取草稿
     */
    @GetMapping("/draft/process/{processKey}")
    public Result<List<WorkflowDraft>> getDraftsByProcessKey(
            @PathVariable String processKey,
            @RequestParam Long userId) {
        try {
            List<WorkflowDraft> drafts = templateService.getDraftsByProcessKey(userId, processKey);
            return Result.success(drafts);
        } catch (Exception e) {
            log.error("获取流程草稿失败", e);
            return Result.fail("获取流程草稿失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取草稿详情
     */
    @GetMapping("/draft/{id}")
    public Result<WorkflowDraft> getDraftById(@PathVariable Long id) {
        try {
            WorkflowDraft draft = templateService.getDraftById(id);
            if (draft != null) {
                return Result.success(draft);
            } else {
                return Result.fail(404, "草稿不存在");
            }
        } catch (Exception e) {
            log.error("获取草稿详情失败", e);
            return Result.fail("获取草稿详情失败: " + e.getMessage());
        }
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    public Result<WorkflowDraft> saveDraft(@RequestBody WorkflowDraft draft) {
        try {
            WorkflowDraft saved = templateService.saveDraft(draft);
            return Result.success("保存草稿成功", saved);
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return Result.fail("保存草稿失败: " + e.getMessage());
        }
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/draft/{id}")
    public Result<String> deleteDraft(@PathVariable Long id) {
        try {
            templateService.deleteDraft(id);
            return Result.success("删除草稿成功");
        } catch (Exception e) {
            log.error("删除草稿失败", e);
            return Result.fail("删除草稿失败: " + e.getMessage());
        }
    }

    /**
     * 从草稿创建模板
     */
    @PostMapping("/draft/{id}/to-template")
    public Result<WorkflowTemplate> draftToTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            String templateName = (String) params.get("templateName");
            String description = (String) params.get("description");
            Integer isPublic = (Integer) params.get("isPublic");

            WorkflowTemplate template = templateService.draftToTemplate(
                    id, templateName, description, isPublic);
            return Result.success("创建模板成功", template);
        } catch (Exception e) {
            log.error("从草稿创建模板失败", e);
            return Result.fail("从草稿创建模板失败: " + e.getMessage());
        }
    }
}

