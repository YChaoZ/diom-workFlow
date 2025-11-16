package com.diom.gateway.exception;

import com.diom.common.dto.Result;
import com.diom.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理器
 *
 * @author diom
 */
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result;
        HttpStatus status;

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = rse.getStatus();
            result = Result.fail(status.value(), rse.getReason());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.fail(status.value(), "网关内部错误");
            log.error("网关异常：", ex);
        }

        response.setStatusCode(status);

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                return bufferFactory.wrap(JsonUtil.toJson(result).getBytes());
            } catch (Exception e) {
                log.error("异常响应序列化失败", e);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}

