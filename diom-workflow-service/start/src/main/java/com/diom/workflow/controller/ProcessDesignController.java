package com.diom.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diom.workflow.dto.ProcessDesignDTO;
import com.diom.workflow.dto.PublishDTO;
import com.diom.workflow.dto.ValidateDTO;
import com.diom.workflow.service.ProcessDesignService;
import com.diom.workflow.vo.ProcessDesignHistoryVO;
import com.diom.workflow.vo.ProcessDesignVO;
import com.diom.workflow.vo.ValidationResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程设计Controller
 * 前端通过角色控制菜单显示，只有超管能访问
 */
@Slf4j
@RestController
@RequestMapping("/workflow/api/process-design")
public class ProcessDesignController {
    
    @Autowired
    private ProcessDesignService processDesignService;
    
    /**
     * 查询流程设计列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Map<String, Object> params) {
        try {
            Page<ProcessDesignVO> page = processDesignService.list(params);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            
            Map<String, Object> data = new HashMap<>();
            data.put("total", page.getTotal());
            data.put("list", page.getRecords());
            result.put("data", data);
            
            return result;
        } catch (Exception e) {
            log.error("查询流程设计列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 查询流程设计详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        try {
            ProcessDesignVO design = processDesignService.getById(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", design);
            
            return result;
        } catch (Exception e) {
            log.error("查询流程设计详情失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 保存草稿
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody ProcessDesignDTO dto) {
        try {
            // 获取当前用户信息
            String[] userInfo = getCurrentUser();
            String username = userInfo[0];
            String displayName = userInfo[1];
            
            ProcessDesignVO design = processDesignService.saveDraft(dto, username, displayName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "保存成功");
            result.put("data", design);
            
            return result;
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "保存失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 验证BPMN（无需权限，所有人可用）
     */
    @PostMapping("/validate")
    public Map<String, Object> validate(@RequestBody ValidateDTO dto) {
        try {
            ValidationResultVO validation = processDesignService.validate(dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", validation.getValid() ? 200 : 400);
            result.put("message", validation.getValid() ? "验证通过" : "验证失败");
            result.put("data", validation);
            
            return result;
        } catch (Exception e) {
            log.error("验证BPMN失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "验证失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 发布流程
     */
    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody PublishDTO dto) {
        try {
            // 获取当前用户信息
            String[] userInfo = getCurrentUser();
            String username = userInfo[0];
            String displayName = userInfo[1];
            
            ProcessDesignVO design = processDesignService.publish(dto, username, displayName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "发布成功");
            result.put("data", design);
            
            return result;
        } catch (Exception e) {
            log.error("发布流程失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "发布失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 查询变更历史
     */
    @GetMapping("/{id}/history")
    public Map<String, Object> getHistory(@PathVariable Long id) {
        try {
            List<ProcessDesignHistoryVO> history = processDesignService.getHistory(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", history);
            
            return result;
        } catch (Exception e) {
            log.error("查询变更历史失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 创建新版本
     */
    @PostMapping("/{id}/new-version")
    public Map<String, Object> createNewVersion(@PathVariable Long id, 
                                                @RequestBody Map<String, String> params) {
        try {
            // 获取当前用户信息
            String[] userInfo = getCurrentUser();
            String username = userInfo[0];
            String displayName = userInfo[1];
            
            String changeDescription = params.get("changeDescription");
            ProcessDesignVO design = processDesignService.createNewVersion(
                    id, changeDescription, username, displayName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "新版本创建成功");
            result.put("data", design);
            
            return result;
        } catch (Exception e) {
            log.error("创建新版本失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 删除草稿
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        try {
            processDesignService.deleteDraft(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "删除成功");
            
            return result;
        } catch (Exception e) {
            log.error("删除草稿失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取当前用户信息
     * 从请求头中获取用户信息（由Gateway转发时添加）
     * @return [username, displayName]
     */
    private String[] getCurrentUser() {
        // TODO: 从请求头获取用户信息
        // 这里先返回默认值，实际应该从Gateway转发的Header中获取
        return new String[]{"admin", "管理员"};
    }
}

