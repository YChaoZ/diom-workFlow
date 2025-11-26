package com.diom.flowable.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Flowable Modeler 用户信息提供者
 * 将 Spring Security 的用户信息转换为 Flowable 需要的格式
 */
@Slf4j
@Component
public class FlowableModelerUserProvider {
    
    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的详细信息
     */
    public UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        
        return null;
    }
    
    /**
     * 检查当前用户是否有流程设计权限
     */
    public boolean hasModelerAccess() {
        UserDetails userDetails = getCurrentUserDetails();
        
        if (userDetails == null) {
            return false;
        }
        
        return userDetails.getAuthorities().stream()
            .anyMatch(auth -> "PROCESS_DESIGNER".equals(auth.getAuthority()) || 
                             "ROLE_ADMIN".equals(auth.getAuthority()));
    }
    
    /**
     * 获取当前用户的邮箱（如果有）
     */
    public String getCurrentUserEmail() {
        String username = getCurrentUsername();
        if (username != null) {
            // 可以从 Auth 服务获取真实邮箱，这里简化处理
            return username + "@diom.com";
        }
        return null;
    }
}

