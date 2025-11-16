package com.diom.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 日志过滤器
 *
 * @author diom
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String path = request.getURI().getPath();
        String method = request.getMethod().toString();
        String remoteAddr = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("请求开始 -> 方法：{}，路径：{}，来源：{}", method, path, remoteAddr);

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;
                    log.info("请求结束 -> 方法：{}，路径：{}，状态码：{}，耗时：{}ms", 
                            method, path, statusCode, duration);
                })
        );
    }

    @Override
    public int getOrder() {
        return -99; // 优先级仅次于 JWT 过滤器
    }
}

