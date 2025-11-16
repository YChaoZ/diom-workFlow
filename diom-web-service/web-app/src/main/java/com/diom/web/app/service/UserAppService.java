package com.diom.web.app.service;

import com.diom.web.domain.gateway.UserGateway;
import com.diom.web.domain.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户应用服务
 * App 层负责业务编排，调用 Domain 层和 Infrastructure 层
 *
 * @author diom
 */
@Service
public class UserAppService {

    private static final Logger log = LoggerFactory.getLogger(UserAppService.class);

    @Autowired
    private UserGateway userGateway;

    /**
     * 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    public UserInfo getUserInfo(Long userId) {
        log.info("获取用户信息，userId: {}", userId);
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        UserInfo userInfo = userGateway.getUserById(userId);
        
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return userInfo;
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UserInfo getUserByUsername(String username) {
        log.info("根据用户名获取用户信息，username: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        UserInfo userInfo = userGateway.getUserByUsername(username);
        
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return userInfo;
    }
}

