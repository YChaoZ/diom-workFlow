package com.diom.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.diom.auth.entity.SysPermission;
import com.diom.auth.mapper.SysPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 权限管理服务
 */
@Slf4j
@Service
public class PermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    /**
     * 获取权限树（所有权限按层级组织）
     */
    public List<SysPermission> getPermissionTree() {
        // 查询所有权限
        QueryWrapper<SysPermission> query = new QueryWrapper<>();
        query.eq("status", 1);
        query.orderByAsc("sort_order");
        List<SysPermission> allPermissions = permissionMapper.selectList(query);
        
        // 构建树形结构
        return buildTree(allPermissions, 0L);
    }

    /**
     * 获取权限列表（按类型过滤）
     */
    public List<SysPermission> listPermissions(String type) {
        QueryWrapper<SysPermission> query = new QueryWrapper<>();
        query.eq("status", 1);
        
        if (type != null && !type.isEmpty()) {
            query.eq("permission_type", type);
        }
        
        query.orderByAsc("sort_order");
        return permissionMapper.selectList(query);
    }

    /**
     * 根据用户名获取用户的所有权限
     */
    public List<SysPermission> getUserPermissions(String username) {
        return permissionMapper.selectPermissionsByUsername(username);
    }

    /**
     * 根据用户名获取用户的所有权限编码列表
     */
    public List<String> getUserPermissionCodes(String username) {
        List<SysPermission> permissions = permissionMapper.selectPermissionsByUsername(username);
        List<String> codes = new ArrayList<>();
        for (SysPermission perm : permissions) {
            codes.add(perm.getPermissionCode());
        }
        return codes;
    }

    /**
     * 获取权限详情
     */
    public SysPermission getPermissionById(Long permissionId) {
        return permissionMapper.selectById(permissionId);
    }

    /**
     * 创建权限
     */
    public Long createPermission(SysPermission permission) {
        permission.setCreateTime(new Date());
        permission.setUpdateTime(new Date());
        permissionMapper.insert(permission);
        log.info("创建权限成功: permissionId={}, permissionCode={}", permission.getId(), permission.getPermissionCode());
        return permission.getId();
    }

    /**
     * 更新权限
     */
    public void updatePermission(SysPermission permission) {
        permission.setUpdateTime(new Date());
        permissionMapper.updateById(permission);
        log.info("更新权限成功: permissionId={}, permissionCode={}", permission.getId(), permission.getPermissionCode());
    }

    /**
     * 删除权限
     */
    public void deletePermission(Long permissionId) {
        permissionMapper.deleteById(permissionId);
        log.info("删除权限成功: permissionId={}", permissionId);
    }

    /**
     * 构建权限树
     */
    private List<SysPermission> buildTree(List<SysPermission> allPermissions, Long parentId) {
        List<SysPermission> tree = new ArrayList<>();
        
        for (SysPermission perm : allPermissions) {
            if (perm.getParentId().equals(parentId)) {
                // 递归查找子权限
                List<SysPermission> children = buildTree(allPermissions, perm.getId());
                perm.setChildren(children);
                tree.add(perm);
            }
        }
        
        return tree;
    }
}

