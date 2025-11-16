package com.diom.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 *
 * @author diom
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
@MapperScan("com.diom.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("==================================");
        System.out.println("====  Auth 认证服务启动成功！  ====");
        System.out.println("====  Dubbo服务已暴露         ====");
        System.out.println("==================================");
    }
}

