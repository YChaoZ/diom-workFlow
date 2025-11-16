package com.diom.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diom.auth.entity.SysRole;
import com.diom.auth.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    public Map<String, Object> listRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        IPage<SysRole> result = roleService.listRoles(page, size, keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        return response;
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    public Map<String, Object> getRoleById(@PathVariable Long roleId) {
        SysRole role = roleService.getRoleById(roleId);
        
        Map<String, Object> response = new HashMap<>();
        if (role != null) {
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", role);
        } else {
            response.put("code", 404);
            response.put("message", "角色不存在");
        }
        return response;
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Map<String, Object> createRole(@RequestBody SysRole role) {
        try {
            Long roleId = roleService.createRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "创建成功");
            response.put("data", roleId);
            return response;
        } catch (Exception e) {
            log.error("创建角色失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    public Map<String, Object> updateRole(@PathVariable Long roleId, @RequestBody SysRole role) {
        try {
            role.setId(roleId);
            roleService.updateRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新成功");
            return response;
        } catch (Exception e) {
            log.error("更新角色失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Map<String, Object> deleteRole(@PathVariable Long roleId) {
        try {
            roleService.deleteRole(roleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return response;
        } catch (Exception e) {
            log.error("删除角色失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/assign")
    public Map<String, Object> assignRolesToUser(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            String username = params.get("username").toString();
            List<Long> roleIds = (List<Long>) params.get("roleIds");
            
            roleService.assignRolesToUser(userId, username, roleIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "分配成功");
            return response;
        } catch (Exception e) {
            log.error("分配角色失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "分配失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/user/{username}")
    public Map<String, Object> getUserRoles(@PathVariable String username) {
        List<SysRole> roles = roleService.getUserRoles(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", roles);
        return response;
    }
}

