package com.diom.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统权限实体类
 */
@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父权限ID（0为顶级）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 权限编码（唯一）
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限类型（MENU:菜单 BUTTON:按钮 API:接口）
     */
    @TableField("permission_type")
    private String permissionType;

    /**
     * 菜单路径（前端路由）
     */
    @TableField("menu_path")
    private String menuPath;

    /**
     * API匹配模式（如：/workflow/**）
     */
    @TableField("api_pattern")
    private String apiPattern;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态（0:禁用 1:启用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysPermission> children;
}

