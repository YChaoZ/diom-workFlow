package com.diom.flowable.config;

import com.diom.flowable.security.FlowableUserDetailsService;
import com.diom.flowable.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security配置
 * 
 * 配置说明：
 * 1. 一般的 workflow API：由Gateway统一认证，这里放行
 * 2. Flowable Modeler 路径：需要细粒度权限控制（PROCESS_DESIGNER权限）
 * 3. 启用JWT认证过滤器：从Gateway注入的Header或Token中提取用户信息
 * 4. 启用@PreAuthorize注解支持：用于方法级权限控制
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private FlowableUserDetailsService userDetailsService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为是无状态REST API）
            .csrf().disable()
            
            // 禁用Session（无状态）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            
            // 配置授权规则
            .authorizeRequests()
                // Actuator 健康检查端点公开
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Flowable Modeler 静态资源公开（JS/CSS/HTML等）
                .antMatchers(
                    "/flowable-modeler/**/*.html",
                    "/flowable-modeler/**/*.js",
                    "/flowable-modeler/**/*.css",
                    "/flowable-modeler/**/*.png",
                    "/flowable-modeler/**/*.jpg",
                    "/flowable-modeler/**/*.svg",
                    "/flowable-modeler/i18n/**",
                    "/flowable-modeler/editor-app/**",
                    "/flowable-modeler/app/**/*.html"
                ).permitAll()
                
                // Flowable Modeler REST API 需要认证和权限
                .antMatchers("/app/rest/**", "/modeler-app/rest/**")
                    .hasAuthority("PROCESS_DESIGNER")
                
                // Flowable REST API（如果启用）需要认证
                .antMatchers("/flowable-rest/**").authenticated()
                
                // 其他所有接口放行（由Gateway统一认证）
                .anyRequest().permitAll()
                .and()
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置用户服务
            .userDetailsService(userDetailsService)
            
            // 禁用表单登录
            .formLogin().disable()
            
            // 禁用HTTP Basic
            .httpBasic().disable();
    }
    
    // CORS配置已移除，由Gateway统一处理
}

