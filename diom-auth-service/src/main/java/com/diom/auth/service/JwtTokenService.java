package com.diom.auth.service;

import com.diom.auth.config.JwtProperties;
import com.diom.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 服务
 *
 * @author diom
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 生成 Token
     *
     * @param user 用户信息
     * @return Token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Token 解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 获取用户 ID
     *
     * @param token Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return Long.valueOf(claims.getSubject());
        }
        return null;
    }

    /**
     * 从 Token 获取用户名
     *
     * @param token Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 刷新 Token
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        // 保留原有的 Claims，只更新时间
        Map<String, Object> newClaims = new HashMap<>(claims);
        
        return Jwts.builder()
                .setClaims(newClaims)
                .setSubject(claims.getSubject())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 判断 Token 是否过期
     *
     * @param claims Claims
     * @return 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取 Token 过期时间（秒）
     *
     * @return 过期时间
     */
    public Long getExpiration() {
        return jwtProperties.getExpiration();
    }
}

