package com.diom.flowable.service;

import com.diom.api.dto.UserDTO;
import com.diom.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 用户RPC服务封装
 * 
 * 供Delegate和Service使用，统一处理Dubbo调用和降级
 *
 * @author diom
 */
@Slf4j
@Service
public class UserRpcService {

    @DubboReference(version = "1.0.0", group = "diom", timeout = 3000, check = false)
    private UserService userService;

    /**
     * 获取用户信息（带降级）
     */
    public UserDTO getUserByUsername(String username) {
        try {
            log.debug("Dubbo调用auth-service: getUserByUsername({})", username);
            UserDTO user = userService.getUserByUsername(username);
            if (user != null) {
                log.info("成功获取用户信息: username={}, id={}", username, user.getId());
                return user;
            }
            log.warn("用户不存在: {}", username);
            return null;
        } catch (Exception e) {
            log.error("Dubbo调用失败，使用降级: getUserByUsername({})", username, e);
            return getUserByUsernameFallback(username);
        }
    }

    /**
     * 获取用户信息（带降级）
     */
    public UserDTO getUserById(Long userId) {
        try {
            log.debug("Dubbo调用auth-service: getUserById({})", userId);
            UserDTO user = userService.getUserById(userId);
            if (user != null) {
                log.info("成功获取用户信息: id={}, username={}", userId, user.getUsername());
                return user;
            }
            log.warn("用户不存在: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Dubbo调用失败，使用降级: getUserById({})", userId, e);
            return getUserByIdFallback(userId);
        }
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsByUsername(String username) {
        try {
            return userService.existsByUsername(username);
        } catch (Exception e) {
            log.error("Dubbo调用失败: existsByUsername({})", username, e);
            return false;
        }
    }

    /**
     * 获取用户角色
     */
    public String getUserRoles(Long userId) {
        try {
            return userService.getUserRoles(userId);
        } catch (Exception e) {
            log.error("Dubbo调用失败: getUserRoles({})", userId, e);
            return "ROLE_USER";
        }
    }

    /**
     * 降级方法：按用户名获取用户
     */
    private UserDTO getUserByUsernameFallback(String username) {
        log.warn("使用降级数据: getUserByUsername({})", username);
        if ("admin".equals(username)) {
            return UserDTO.builder()
                    .id(1L)
                    .username("admin")
                    .nickname("管理员(降级)")
                    .email("admin@example.com")
                    .status(0)
                    .build();
        }
        return null;
    }

    /**
     * 降级方法：按ID获取用户
     */
    private UserDTO getUserByIdFallback(Long userId) {
        log.warn("使用降级数据: getUserById({})", userId);
        if (userId == 1L) {
            return UserDTO.builder()
                    .id(1L)
                    .username("admin")
                    .nickname("管理员(降级)")
                    .email("admin@example.com")
                    .status(0)
                    .build();
        }
        return null;
    }
}

