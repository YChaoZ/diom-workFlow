package com.diom.gateway.filter;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * HTTP服务过滤器
 * 在LoadBalancer选择实例后，验证选中的实例是否为HTTP服务
 * 如果选中Dubbo端口，则重新选择
 * 
 * @author diom
 */
@Component
public class HttpOnlyLoadBalancerClientFilter implements GlobalFilter, Ordered {

    private final LoadBalancerClient loadBalancer;

    public HttpOnlyLoadBalancerClientFilter(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        
        if (url == null || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
            return chain.filter(exchange);
        }

        // 自定义选择逻辑：过滤HTTP服务
        String serviceId = url.getHost();
        ServiceInstance instance = chooseHttpInstance(serviceId);
        
        if (instance != null) {
            URI requestUrl = exchange.getRequest().getURI();
            String overrideScheme = instance.isSecure() ? "https" : "http";
            URI newUri = loadBalancer.reconstructURI(instance, requestUrl);
            
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);
        }
        
        return chain.filter(exchange);
    }

    /**
     * 选择HTTP服务实例
     * 过滤逻辑：
     * 1. 优先选择带有 service-type=http 的实例
     * 2. 如果没有，则排除 Dubbo 端口范围 (20000-21000)
     */
    private ServiceInstance chooseHttpInstance(String serviceId) {
        try {
            // 使用Spring Cloud LoadBalancer选择实例
            ServiceInstance instance = loadBalancer.choose(serviceId);
            
            if (instance == null) {
                return null;
            }

            // 验证是否为HTTP服务
            String serviceType = instance.getMetadata().get("service-type");
            int port = instance.getPort();
            
            // 如果是HTTP服务或者不是Dubbo端口，则返回
            if ("http".equalsIgnoreCase(serviceType) || port < 20000 || port > 21000) {
                return instance;
            }
            
            // 如果选中了Dubbo端口，返回null（让请求失败，而不是错误路由）
            return null;
            
        } catch (Exception e) {
            // 降级：返回null
            return null;
        }
    }

    @Override
    public int getOrder() {
        // 在LoadBalancer之后执行
        return 10101;
    }
}

