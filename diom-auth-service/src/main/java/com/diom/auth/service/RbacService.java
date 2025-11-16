package com.diom.auth.service;

import com.diom.auth.entity.SysPermission;
import com.diom.auth.mapper.SysPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RBAC权限检查核心服务
 */
@Slf4j
@Service
public class RbacService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // 用户权限缓存（username -> List<SysPermission>）
    private final ConcurrentHashMap<String, List<SysPermission>> permissionCache = new ConcurrentHashMap<>();

    /**
     * 检查用户是否有指定权限编码
     */
    public boolean hasPermission(String username, String permissionCode) {
        List<SysPermission> permissions = getUserPermissions(username);
        
        for (SysPermission perm : permissions) {
            if (perm.getPermissionCode().equals(permissionCode)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查用户是否有访问指定API的权限
     */
    public boolean checkApiPermission(String username, String requestUri) {
        // 超级管理员跳过权限检查
        if ("admin".equals(username)) {
            return true;
        }
        
        List<SysPermission> permissions = getUserPermissions(username);
        
        for (SysPermission perm : permissions) {
            // 只检查API类型的权限
            if ("API".equals(perm.getPermissionType()) && perm.getApiPattern() != null) {
                // 使用Ant风格路径匹配
                if (pathMatcher.match(perm.getApiPattern(), requestUri)) {
                    log.debug("用户{}有权限访问API: {}", username, requestUri);
                    return true;
                }
            }
        }
        
        log.warn("用户{}无权限访问API: {}", username, requestUri);
        return false;
    }

    /**
     * 检查用户是否有访问指定菜单的权限
     */
    public boolean checkMenuPermission(String username, String menuPath) {
        List<SysPermission> permissions = getUserPermissions(username);
        
        for (SysPermission perm : permissions) {
            if ("MENU".equals(perm.getPermissionType()) && menuPath.equals(perm.getMenuPath())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取用户所有权限（带缓存）
     */
    private List<SysPermission> getUserPermissions(String username) {
        // 从缓存获取
        List<SysPermission> permissions = permissionCache.get(username);
        
        if (permissions == null) {
            // 从数据库查询
            permissions = permissionMapper.selectPermissionsByUsername(username);
            // 放入缓存
            permissionCache.put(username, permissions);
            log.debug("加载用户{}的权限到缓存, 权限数量: {}", username, permissions.size());
        }
        
        return permissions;
    }

    /**
     * 清除用户权限缓存
     */
    public void clearUserPermissionCache(String username) {
        permissionCache.remove(username);
        log.info("清除用户{}的权限缓存", username);
    }

    /**
     * 清除所有权限缓存
     */
    public void clearAllPermissionCache() {
        permissionCache.clear();
        log.info("清除所有权限缓存");
    }
}

