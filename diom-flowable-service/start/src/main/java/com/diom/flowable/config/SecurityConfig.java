package com.diom.flowable.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Security配置
 * 注意：workflow-service的权限控制通过Gateway统一处理
 * 这里只需要：
 * 1. 放行所有接口（由Gateway转发过来的请求已经过认证）
 * 2. 启用@PreAuthorize注解支持（用于细粒度权限控制）
 * 3. CORS已由Gateway统一处理，这里不再配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为是无状态REST API）
            .csrf().disable()
            
            // 禁用Session（无状态）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            
            // 放行所有接口（由Gateway统一认证）
            .authorizeRequests()
                .anyRequest().permitAll()
                .and()
            
            // 禁用表单登录
            .formLogin().disable()
            
            // 禁用HTTP Basic
            .httpBasic().disable();
    }
    
    // CORS配置已移除，由Gateway统一处理
}

