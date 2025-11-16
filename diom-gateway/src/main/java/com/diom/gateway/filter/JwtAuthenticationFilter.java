package com.diom.gateway.filter;

import com.diom.common.constant.CommonConstant;
import com.diom.gateway.config.JwtProperties;
import com.diom.gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT 认证过滤器
 *
 * @author diom
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否在白名单中
        if (isWhitelist(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = extractToken(request);
        if (StringUtils.isBlank(token)) {
            log.warn("访问路径：{}，Token为空", path);
            return unauthorized(exchange.getResponse());
        }

        // 验证 Token
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                log.warn("访问路径：{}，Token解析失败", path);
                return unauthorized(exchange.getResponse());
            }

            // 将用户信息添加到请求头，传递给下游服务
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(CommonConstant.USER_ID_HEADER, userId)
                    .header(CommonConstant.USERNAME_HEADER, username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            log.info("用户：{} 访问路径：{}", username, path);
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("Token验证失败：{}", e.getMessage());
            return unauthorized(exchange.getResponse());
        }
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(jwtProperties.getHeader());
        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelist(String path) {
        for (String pattern : jwtProperties.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }
}

