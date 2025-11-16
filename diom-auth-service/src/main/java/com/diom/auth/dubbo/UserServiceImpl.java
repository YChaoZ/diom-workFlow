package com.diom.auth.dubbo;

import com.diom.api.dto.UserDTO;
import com.diom.api.service.UserService;
import com.diom.auth.entity.User;
import com.diom.auth.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户服务Dubbo实现
 * 
 * 暴露给其他服务调用
 *
 * @author diom
 */
@DubboService(version = "1.0.0", group = "diom", timeout = 3000)
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDTO getUserById(Long userId) {
        log.info("Dubbo调用: getUserById({})", userId);
        User user = userMapper.selectById(userId);
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.info("Dubbo调用: getUserByUsername({})", username);
        User user = userMapper.selectByUsername(username);
        return convertToDTO(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.info("Dubbo调用: existsByUsername({})", username);
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public String getUserRoles(Long userId) {
        log.info("Dubbo调用: getUserRoles({})", userId);
        // TODO: 实际项目中从角色表查询
        // 目前返回默认角色
        User user = userMapper.selectById(userId);
        if (user != null) {
            if ("admin".equals(user.getUsername())) {
                return "ROLE_ADMIN,ROLE_USER";
            }
            return "ROLE_USER";
        }
        return null;
    }

    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .build();
    }
}

