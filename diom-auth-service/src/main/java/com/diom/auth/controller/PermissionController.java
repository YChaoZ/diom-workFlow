package com.diom.auth.controller;

import com.diom.auth.entity.SysPermission;
import com.diom.auth.service.PermissionService;
import com.diom.auth.service.RbacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RbacService rbacService;

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    public Map<String, Object> getPermissionTree() {
        List<SysPermission> tree = permissionService.getPermissionTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", tree);
        return response;
    }

    /**
     * 获取权限列表（按类型过滤）
     */
    @GetMapping("/list")
    public Map<String, Object> listPermissions(@RequestParam(required = false) String type) {
        List<SysPermission> permissions = permissionService.listPermissions(type);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", permissions);
        return response;
    }

    /**
     * 获取用户的所有权限
     */
    @GetMapping("/user/{username}")
    public Map<String, Object> getUserPermissions(@PathVariable String username) {
        List<SysPermission> permissions = permissionService.getUserPermissions(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", permissions);
        return response;
    }

    /**
     * 获取用户的所有权限编码列表
     */
    @GetMapping("/codes")
    public Map<String, Object> getUserPermissionCodes(@RequestParam String username) {
        List<String> codes = permissionService.getUserPermissionCodes(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", codes);
        return response;
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check")
    public Map<String, Object> checkPermission(
            @RequestParam String username,
            @RequestParam String permissionCode) {
        
        boolean hasPermission = rbacService.hasPermission(username, permissionCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", hasPermission);
        return response;
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{permissionId}")
    public Map<String, Object> getPermissionById(@PathVariable Long permissionId) {
        SysPermission permission = permissionService.getPermissionById(permissionId);
        
        Map<String, Object> response = new HashMap<>();
        if (permission != null) {
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", permission);
        } else {
            response.put("code", 404);
            response.put("message", "权限不存在");
        }
        return response;
    }

    /**
     * 创建权限
     */
    @PostMapping
    public Map<String, Object> createPermission(@RequestBody SysPermission permission) {
        try {
            Long permissionId = permissionService.createPermission(permission);
            
            // 清除权限缓存
            rbacService.clearAllPermissionCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "创建成功");
            response.put("data", permissionId);
            return response;
        } catch (Exception e) {
            log.error("创建权限失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{permissionId}")
    public Map<String, Object> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody SysPermission permission) {
        try {
            permission.setId(permissionId);
            permissionService.updatePermission(permission);
            
            // 清除权限缓存
            rbacService.clearAllPermissionCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新成功");
            return response;
        } catch (Exception e) {
            log.error("更新权限失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{permissionId}")
    public Map<String, Object> deletePermission(@PathVariable Long permissionId) {
        try {
            permissionService.deletePermission(permissionId);
            
            // 清除权限缓存
            rbacService.clearAllPermissionCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return response;
        } catch (Exception e) {
            log.error("删除权限失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }
}

