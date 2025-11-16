package com.diom.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security 配置
 *
 * @author diom
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离不需要）
                .csrf().disable()
                
                // 禁用表单登录
                .formLogin().disable()
                
                // 禁用 HTTP Basic
                .httpBasic().disable()
                
                // 禁用默认的logout配置（我们使用自定义的REST API）
                .logout().disable()
                
                // 设置会话管理为无状态
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                
                .and()
                // 配置权限
                .authorizeRequests()
                .antMatchers("/login", "/register", "/logout", "/health", "/actuator/**", "/generate-password", "/validate", "/refresh", "/userinfo", 
                             "/role/**", "/permission/**").permitAll()
                .anyRequest().authenticated();
    }

    /**
     * 密码编码器
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

