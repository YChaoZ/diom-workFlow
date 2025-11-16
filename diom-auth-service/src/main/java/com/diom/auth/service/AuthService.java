package com.diom.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diom.auth.dto.LoginRequest;
import com.diom.auth.dto.LoginResponse;
import com.diom.auth.dto.RegisterRequest;
import com.diom.auth.entity.User;
import com.diom.auth.mapper.UserMapper;
import com.diom.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 *
 * @author diom
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PermissionService permissionService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败：用户不存在，username={}", request.getUsername());
            throw new BizException("用户名或密码错误");
        }

        // 验证密码
        // 调试日志：打印密码信息（生产环境请删除）
        log.info("调试信息 - 输入密码: {}, 数据库密码哈希: {}", 
                request.getPassword(), user.getPassword().substring(0, Math.min(30, user.getPassword().length())));
        
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("调试信息 - 密码匹配结果: {}", matches);
        
        if (!matches) {
            log.warn("登录失败：密码错误，username={}", request.getUsername());
            throw new BizException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("登录失败：用户已禁用，username={}", request.getUsername());
            throw new BizException("用户已被禁用");
        }

        // 生成 Token
        String token = jwtTokenService.generateToken(user);

        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();

        log.info("用户登录成功：username={}, userId={}", user.getUsername(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpiration())
                .user(userInfo)
                .build();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, request.getEmail());
            existUser = userMapper.selectOne(queryWrapper);
            if (existUser != null) {
                throw new BizException("邮箱已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);

        log.info("用户注册成功：username={}, userId={}", user.getUsername(), user.getId());
    }

    /**
     * 刷新 Token
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        // 验证 Token
        if (!jwtTokenService.validateToken(token)) {
            throw new BizException("Token 无效或已过期");
        }

        // 刷新 Token
        String newToken = jwtTokenService.refreshToken(token);
        if (newToken == null) {
            throw new BizException("Token 刷新失败");
        }

        Long userId = jwtTokenService.getUserIdFromToken(token);
        log.info("Token 刷新成功：userId={}", userId);

        return newToken;
    }

    /**
     * 验证 Token
     *
     * @param token Token
     * @return 用户 ID
     */
    public Long validateToken(String token) {
        if (!jwtTokenService.validateToken(token)) {
            throw new BizException("Token 无效或已过期");
        }
        return jwtTokenService.getUserIdFromToken(token);
    }

    /**
     * 获取用户信息
     *
     * @param token Token
     * @return 用户信息
     */
    public Map<String, Object> getUserInfo(String token) {
        if (!jwtTokenService.validateToken(token)) {
            throw new BizException("Token 无效或已过期");
        }

        Long userId = jwtTokenService.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("status", user.getStatus());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("updateTime", user.getUpdateTime());
        
        // 添加角色信息
        userInfo.put("roles", roleService.getUserRoles(user.getUsername()));
        
        // 添加权限编码列表
        userInfo.put("permissions", permissionService.getUserPermissionCodes(user.getUsername()));

        return userInfo;
    }
}

