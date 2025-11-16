package com.diom.web.domain.gateway;

import com.diom.web.domain.model.UserInfo;

/**
 * 用户网关接口（防腐层）
 * Domain 层定义接口，Infrastructure 层实现
 *
 * @author diom
 */
public interface UserGateway {

    /**
     * 根据用户 ID 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserInfo getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getUserByUsername(String username);
}

