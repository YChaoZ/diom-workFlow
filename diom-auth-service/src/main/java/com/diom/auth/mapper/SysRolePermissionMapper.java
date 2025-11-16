package com.diom.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.auth.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    
    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}

