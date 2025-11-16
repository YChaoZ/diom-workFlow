package com.diom.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 *
 * @author diom
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret = "diom-workflow-secret-key-must-be-at-least-256-bits-long-for-HS256";

    /**
     * Token 过期时间（秒）
     */
    private Long expiration = 7200L;

    /**
     * Token 请求头名称
     */
    private String header = "Authorization";

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";
}

