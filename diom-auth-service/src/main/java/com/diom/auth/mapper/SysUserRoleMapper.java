package com.diom.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diom.auth.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 删除用户的所有角色
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户名查询角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.username = #{username} AND r.status = 1")
    List<com.diom.auth.entity.SysRole> selectRolesByUsername(@Param("username") String username);
}

