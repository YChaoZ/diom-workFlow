package com.diom.auth.controller;

import com.diom.auth.dto.LoginRequest;
import com.diom.auth.dto.LoginResponse;
import com.diom.auth.dto.RegisterRequest;
import com.diom.auth.service.AuthService;
import com.diom.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author diom
 */
@RestController
@RequestMapping
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }

    /**
     * 刷新 Token
     *
     * @param authorization Token
     * @return 新 Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestHeader("Authorization") String authorization) {
        // 提取 Token
        String token = authorization.replace("Bearer ", "");
        String newToken = authService.refreshToken(token);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        return Result.success("Token 刷新成功", result);
    }

    /**
     * 验证 Token
     *
     * @param authorization Token
     * @return 用户 ID
     */
    @GetMapping("/validate")
    public Result<Map<String, Long>> validate(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = authService.validateToken(token);
        
        Map<String, Long> result = new HashMap<>();
        result.put("userId", userId);
        return Result.success("Token 有效", result);
    }

    /**
     * 获取当前用户信息
     *
     * @param authorization Token
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Map<String, Object> userInfo = authService.getUserInfo(token);
        return Result.success("获取用户信息成功", userInfo);
    }

    /**
     * 用户退出登录
     * JWT是无状态的，退出登录主要由前端删除token实现
     * 后端只需返回成功响应
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出登录成功");
    }

    /**
     * 健康检查
     *
     * @return OK
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Auth Service is running");
    }
    
    /**
     * 生成BCrypt密码（临时接口，用于测试）
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    @GetMapping("/generate-password")
    public Result<Map<String, Object>> generatePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        Map<String, Object> result = new HashMap<>();
        result.put("original", password);
        result.put("encoded", encoded);
        result.put("length", encoded.length());
        result.put("matches", passwordEncoder.matches(password, encoded));
        return Result.success(result);
    }
}

