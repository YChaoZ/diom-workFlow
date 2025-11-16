package com.diom.web.infrastructure.gateway;

import com.diom.api.dto.UserDTO;
import com.diom.api.service.UserService;
import com.diom.web.domain.gateway.UserGateway;
import com.diom.web.domain.model.UserInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户领域网关实现 - Dubbo方式
 * 
 * 通过Dubbo调用auth-service获取用户信息
 *
 * @author diom
 */
@Component
public class UserGatewayImpl implements UserGateway {

    private static final Logger log = LoggerFactory.getLogger(UserGatewayImpl.class);

    @DubboReference(version = "1.0.0", group = "diom", timeout = 3000, check = false)
    private UserService userService;

    @Override
    public UserInfo getUserById(Long userId) {
        log.info("通过Dubbo调用auth-service: getUserById({})", userId);
        try {
            UserDTO userDTO = userService.getUserById(userId);
            return convertToUserInfo(userDTO);
        } catch (Exception e) {
            log.error("Dubbo调用失败: getUserById({})", userId, e);
            // 降级：返回模拟数据
            return getUserByIdFallback(userId);
        }
    }

    @Override
    public UserInfo getUserByUsername(String username) {
        log.info("通过Dubbo调用auth-service: getUserByUsername({})", username);
        try {
            UserDTO userDTO = userService.getUserByUsername(username);
            return convertToUserInfo(userDTO);
        } catch (Exception e) {
            log.error("Dubbo调用失败: getUserByUsername({})", username, e);
            // 降级：返回模拟数据
            return getUserByUsernameFallback(username);
        }
    }

    /**
     * 转换DTO到领域模型
     */
    private UserInfo convertToUserInfo(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        return UserInfo.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .nickname(userDTO.getNickname())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .status(userDTO.getStatus())
                .build();
    }

    /**
     * 降级方法：getUserById
     */
    private UserInfo getUserByIdFallback(Long userId) {
        log.warn("使用降级数据: getUserById({})", userId);
        if (userId == 1L) {
            return UserInfo.builder()
                    .id(1L)
                    .username("admin")
                    .nickname("管理员")
                    .email("admin@example.com")
                    .status(0)
                    .build();
        } else if (userId == 2L) {
            return UserInfo.builder()
                    .id(2L)
                    .username("user_1")
                    .nickname("测试用户1")
                    .email("user1@example.com")
                    .status(0)
                    .build();
        }
        return null;
    }

    /**
     * 降级方法：getUserByUsername
     */
    private UserInfo getUserByUsernameFallback(String username) {
        log.warn("使用降级数据: getUserByUsername({})", username);
        if ("admin".equals(username)) {
            return UserInfo.builder()
                    .id(1L)
                    .username("admin")
                    .nickname("管理员")
                    .email("admin@example.com")
                    .status(0)
                    .build();
        } else if ("user_1".equals(username)) {
            return UserInfo.builder()
                    .id(2L)
                    .username("user_1")
                    .nickname("测试用户1")
                    .email("user1@example.com")
                    .status(0)
                    .build();
        }
        return null;
    }
}
