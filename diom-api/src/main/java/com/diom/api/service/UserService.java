package com.diom.api.service;

import com.diom.api.dto.UserDTO;

/**
 * 用户服务Dubbo接口
 * 
 * Provider: diom-auth-service
 * Consumer: diom-web-service, diom-workflow-service
 *
 * @author diom
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserDTO getUserByUsername(String username);

    /**
     * 验证用户是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色列表（逗号分隔）
     */
    String getUserRoles(Long userId);
}

