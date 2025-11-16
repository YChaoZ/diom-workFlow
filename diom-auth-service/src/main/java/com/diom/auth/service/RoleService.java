package com.diom.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diom.auth.entity.SysRole;
import com.diom.auth.entity.SysRolePermission;
import com.diom.auth.entity.SysUserRole;
import com.diom.auth.mapper.SysPermissionMapper;
import com.diom.auth.mapper.SysRoleMapper;
import com.diom.auth.mapper.SysRolePermissionMapper;
import com.diom.auth.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 角色管理服务
 */
@Slf4j
@Service
public class RoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 分页查询角色列表
     */
    public IPage<SysRole> listRoles(int page, int size, String keyword) {
        Page<SysRole> pageParam = new Page<>(page, size);
        QueryWrapper<SysRole> query = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            query.like("role_name", keyword).or().like("role_code", keyword);
        }
        
        query.orderByAsc("sort_order");
        return roleMapper.selectPage(pageParam, query);
    }

    /**
     * 根据ID获取角色详情（包含权限列表）
     */
    public SysRole getRoleById(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role != null) {
            // 查询角色的权限列表
            List<com.diom.auth.entity.SysPermission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
            List<Long> permissionIds = new java.util.ArrayList<>();
            for (com.diom.auth.entity.SysPermission perm : permissions) {
                permissionIds.add(perm.getId());
            }
            role.setPermissionIds(permissionIds);
        }
        return role;
    }

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(SysRole role) {
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        roleMapper.insert(role);
        
        // 分配权限
        if (role.getPermissionIds() != null && !role.getPermissionIds().isEmpty()) {
            assignPermissionsToRole(role.getId(), role.getPermissionIds());
        }
        
        log.info("创建角色成功: roleId={}, roleCode={}", role.getId(), role.getRoleCode());
        return role.getId();
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);
        
        // 重新分配权限
        if (role.getPermissionIds() != null) {
            rolePermissionMapper.deleteByRoleId(role.getId());
            if (!role.getPermissionIds().isEmpty()) {
                assignPermissionsToRole(role.getId(), role.getPermissionIds());
            }
        }
        
        log.info("更新角色成功: roleId={}, roleCode={}", role.getId(), role.getRoleCode());
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 删除用户角色关联
        QueryWrapper<SysUserRole> query = new QueryWrapper<>();
        query.eq("role_id", roleId);
        userRoleMapper.delete(query);
        
        // 删除角色
        roleMapper.deleteById(roleId);
        
        log.info("删除角色成功: roleId={}", roleId);
    }

    /**
     * 为角色分配权限
     */
    private void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreateTime(new Date());
            rolePermissionMapper.insert(rolePermission);
        }
    }

    /**
     * 为用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, String username, List<Long> roleIds) {
        // 删除用户现有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 分配新角色
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setUsername(username);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(new Date());
            userRoleMapper.insert(userRole);
        }
        
        log.info("为用户分配角色成功: userId={}, username={}, roleIds={}", userId, username, roleIds);
    }

    /**
     * 获取用户的角色列表
     */
    public List<SysRole> getUserRoles(String username) {
        return userRoleMapper.selectRolesByUsername(username);
    }
}

