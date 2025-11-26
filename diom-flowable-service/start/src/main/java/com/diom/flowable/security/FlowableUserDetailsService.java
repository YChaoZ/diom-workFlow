package com.diom.flowable.security;

import com.diom.api.dto.UserDTO;
import com.diom.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Flowable 用户服务
 * 从 diom-auth-service 获取用户信息
 */
@Slf4j
@Service
public class FlowableUserDetailsService implements UserDetailsService {
    
    @DubboReference(check = false, timeout = 3000)
    private UserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user for Flowable Modeler: {}", username);
        
        try {
            // 从 Auth 服务获取用户信息
            UserDTO userDTO = userService.getUserByUsername(username);
            
            if (userDTO == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            
            // 转换权限
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // 获取用户角色
            String rolesStr = userService.getUserRoles(userDTO.getId());
            if (StringUtils.hasText(rolesStr)) {
                String[] roles = rolesStr.split(",");
                for (String role : roles) {
                    String trimmedRole = role.trim();
                    if (StringUtils.hasText(trimmedRole)) {
                        // 确保角色以 ROLE_ 开头
                        if (!trimmedRole.startsWith("ROLE_")) {
                            trimmedRole = "ROLE_" + trimmedRole.toUpperCase();
                        }
                        authorities.add(new SimpleGrantedAuthority(trimmedRole));
                    }
                }
            }
            
            // 添加 Modeler 访问权限（可以基于角色判断）
            if (hasModelerAccess(rolesStr)) {
                authorities.add(new SimpleGrantedAuthority("PROCESS_DESIGNER"));
            }
            
            // 如果没有任何权限，至少添加一个基本权限
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            return new User(
                userDTO.getUsername(),
                "N/A", // JWT 认证时不需要密码
                authorities
            );
            
        } catch (Exception e) {
            log.error("Failed to load user from Auth service: {}", username, e);
            throw new UsernameNotFoundException("Failed to load user: " + username, e);
        }
    }
    
    /**
     * 判断用户是否有 Modeler 访问权限
     * 示例：管理员或有特定角色的用户可以访问
     */
    private boolean hasModelerAccess(String rolesStr) {
        if (!StringUtils.hasText(rolesStr)) {
            return false;
        }
        
        String rolesUpper = rolesStr.toUpperCase();
        
        // 管理员可以访问
        if (rolesUpper.contains("ADMIN")) {
            return true;
        }
        
        // 流程设计师可以访问
        if (rolesUpper.contains("PROCESS_DESIGNER") || rolesUpper.contains("DESIGNER")) {
            return true;
        }
        
        // 工作流管理员可以访问
        if (rolesUpper.contains("WORKFLOW")) {
            return true;
        }
        
        return false;
    }
}

