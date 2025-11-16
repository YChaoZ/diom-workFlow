package com.diom.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.auth.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据用户名查询用户的所有权限
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.username = #{username} AND p.status = 1")
    List<SysPermission> selectPermissionsByUsername(@Param("username") String username);
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
}

